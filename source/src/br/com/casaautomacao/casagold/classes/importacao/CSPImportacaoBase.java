/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.modelos.ModelColunaTabela;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.getColunas;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por importar os registros de uma base firebird para o
 * sistema
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/12/2015 - 10:10:21
 */
@Deprecated
public class CSPImportacaoBase extends CSPImportacao implements InterfaceImportacao {

    private final CSPInstrucoesSQLBase origem;

    /**
     *
     * Importação de registros
     *
     * @param contratate CSPInstrucoesSQLBase - Conexão com a base contratante
     * @param importacao String - Número da importação
     * @param historicoImportacao String - Código do histórico da importação.
     * @param destino CSPInstrucoesSQLBase - Conexão com a base de destino
     * @param origem CSPInstrucoesSQLBase - Conexão de origem dos dados
     *
     * @param estrangeiros HashMap<String, CSPInstrucoesSQLBase> - Bases
     * estrangeiras
     */
    public CSPImportacaoBase(CSPInstrucoesSQLBase contratate, String importacao, String historicoImportacao, CSPInstrucoesSQLBase destino, CSPInstrucoesSQLBase origem, HashMap<String, CSPInstrucoesSQLBase> estrangeiros) {
        super(contratate, importacao, historicoImportacao, destino, estrangeiros);
        this.origem = origem;
    }
    private ArrayList<String[]> relacaoTabelasDestinoOrigem = new ArrayList<>();

    /**
     * Realiza a configuraçao da relação tabela origem X tabela destino
     */
    private boolean confTabelaDestinoOrigem() throws SQLException {
        ResultSet result = conn.select("SELECT DISTINCT ORIGEM_TABELA, DESTINO_TABELA FROM FRR_IMPORTACAO_RELACAO WHERE ORIGEM_TABELA IS NOT NULL AND IMPORTACAO_CODIGO = ? ORDER BY INDICE_RELACAO ASC", numImportacao);
        this.relacaoTabelasDestinoOrigem = new ArrayList<>();

        while (result.next()) {
            this.relacaoTabelasDestinoOrigem.add(new String[]{result.getString("DESTINO_TABELA"), result.getString("ORIGEM_TABELA")});
        }
        //  this.relacaoTabelasDestinoOrigem.add(new String[]{"PRODUTOS", "PRODUTOS"});
        //this.relacaoTabelasDestinoOrigem.add(new String[]{"EST_LANCAMENTO_ENTRADAS", "ENTRADA_PRODUTOS"});
        // this.relacaoTabelasDestinoOrigem.add(new String[]{"EST_LANCAMENTO_ENTRADAS_ITENS", "ENTRADA_PRODUTOS_ITENS"});
        return true;

    }

    /**
     * Infelizmente algumas bases não possuem todas as FKs, para esses casos as
     * repassamos manualmente
     *
     * @return
     */
    private ArrayList<String[]> getChavesEstrangeirasForcada(String tabela) {
        ArrayList<String[]> fks = new ArrayList<>();
        if (tabela.equals("ENTRADA_PRODUTOS")) {
            fks.add(new String[]{"CODIGO_FORNECEDOR", "FORNECEDORES", "CODIGO"});
        }
        if (tabela.equals("ENTRADA_PRODUTOS_ITENS")) {
            fks.add(new String[]{"CODIGO_FORNECEDOR", "FORNECEDORES", "CODIGO"});
        }
        return fks;
    }

    @Override
    public boolean run() throws Exception {

        this.arquivoOrigem = new File(this.origem.getConfs().getPath()).getName();
        //Recebe as tabelas que tem importações gravadas no codigo
        CSPLog.info(this.getClass(), "importação " + this.numImportacao + " inicinado " + this.origem.getConfs().getPath() + "=>" + this.destino.getConfs().getPath() + "...");

        this.confTabelaDestinoOrigem();
        for (String[] i : relacaoTabelasDestinoOrigem) {

            String out = i[0];
            String in = i[1];

            //Recebe as colunas da tabelae iguala as colunas do FDB origem
            //e define em que coluna do FDB destino
            //os valores do FDB origem vão ser gravados.
            this.confRelacaoDestinoOrigemDados(out, in);
            //Monta o select para ser utilizado no resultset abaixo.
            ArrayList<ModelColunaTabela> c = getColunas(in, origem);
            String sel = montaSelectImportacao(c, in, out);
            CSPLog.info(this.getClass(), "importação " + this.numImportacao + " inicinado " + this.origem.getConfs().getPath() + "(" + in + ")=>" + this.destino.getConfs().getPath() + "(" + out + ")...");
            ArrayList<String[]> fks = new ArrayList<>();
            c.stream().filter((a) -> ((Boolean) a.isForeignKey())).forEach((a) -> {
                fks.add(new String[]{
                    a.getNome(),//Campo nessa tabela
                    a.getReferNameTableForeignKey(),//Tabela de origem
                    a.getReferNameColumnForeignKey()//Tabela de Destino
                });
            });
            this.getChavesEstrangeirasForcada(in).stream().forEach((fk) -> {
                fks.add(fk);
            });
            ResultSet resultFdb = origem.select("SELECT " + sel + " FROM " + in);
            ArrayList<HashMap<String, String>> tmp = new ArrayList<>();

            while (resultFdb.next()) {
                //HashMap usado para enviar os dados para serem gravados na tabela.
                HashMap<String, String> dados = new HashMap<>();
                //Percorre as colunas que vão receber os dados.
                for (Map.Entry<String, String> e : this.getRelacaoTabelasColunasDestinoOrigem(out, in).entrySet()) {
                    //Verifica se a coluna de destino está relacionada a uma coluna de origem.
                    if (e.getValue() != null && sel.contains(e.getValue())) {

                        dados.put(e.getKey(), resultFdb.getString(e.getValue()));
                    }

                }
                tmp.add(dados);
            }
            for (HashMap<String, String> t : tmp) {
                this.destino.setAutoCommit(false);
                //envia os dados ja prontos para a gravação na tabela.

                this.efetuaImportacao(out, in, t, fks);

            }

            this.destino.commit();//Commitamos por tabela
        }
        this.destino.setAutoCommit(true);
        this.registraHistorico();
        CSPLog.info(this.getClass(), "importação " + this.numImportacao + " finalizada " + this.origem.getConfs().getPath() + "=>" + this.destino.getConfs().getPath());
        return true;
    }

    /**
     * Metodo para montar o select caso alguma coluna da tabela seja do tipo
     * CHAR.
     *
     */
    private String montaSelectImportacao(ArrayList<ModelColunaTabela> c, String in, String out) throws SQLException {
        HashMap<String, String> t = this.getRelacaoTabelasColunasDestinoOrigem(out, in);
        String[] resultado = new String[c.size()];
        int i = 0;
        for (ModelColunaTabela lista : c) {
            boolean ok = false;
            for (Map.Entry<String, String> a : t.entrySet()) {
                if (a.getValue() != null) {
                    if (a.getValue().trim().equals(lista.getNome())) {
                        ok = true;
                        break;
                    }
                }
            }
            //Percorre o array com as colunas da tabela de origem e caso o tipo da coluna 
            //seja CHAR ele faz o CAST para um VARCHAR de tamanho (500).
            if (ok) {

                if (lista.getTipo().trim().equals("CHAR")) {
                    int size = 500;
                    if (lista.getTamanho() > 0) {
                        size = lista.getTamanho();
                    }
                    resultado[i] = "cast(" + lista.getNome() + " as varchar(" + size + ")) as " + lista.getNome();
                } else {
                    resultado[i] = "" + lista.getNome();
                }

            }
            i++;
        }

        return String.join(",", resultado);
    }

}
