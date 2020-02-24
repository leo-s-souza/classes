/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocaisExcel;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe responsável por importar os registro de um arquivo excel para o
 * sistema
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/12/2015 - 10:10:41
 */
@Deprecated
public class CSPImportacaoExcel extends CSPImportacao implements InterfaceImportacao {

    private final CSPArquivosLocaisExcel origem;

    /**
     * Importação de dados de um XLS
     *
     * @param contratante CSPInstrucoesSQLBase - Conexão com a base contratante
     * @param importacao String - Número da importação
     * @param historicoImportacao - Código do histórico da importação.
     * @param destino CSPInstrucoesSQLBase - Conexão com a base de destino
     * @param excel CSPArquivosLocaisExcel - Excel de origem
     * @param estrangeiros HashMap<String, CSPInstrucoesSQLBase> - Bases
     * estrangeiras
     */
    public CSPImportacaoExcel(CSPInstrucoesSQLBase contratante, String importacao, String historicoImportacao, CSPInstrucoesSQLBase destino, CSPArquivosLocaisExcel excel, HashMap<String, CSPInstrucoesSQLBase> estrangeiros) {
        super(contratante, importacao, historicoImportacao, destino, estrangeiros);
        this.origem = excel;
    }

    /**
     * Busca as tabelas de destino
     *
     * @return
     */
    private ArrayList<String> encontraTabelasDestino() throws SQLException {
        ResultSet result = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("     DESTINO_TABELA ");
            sb.append("FROM ");
            sb.append("     FRR_IMPORTACAO_RELACAO ");
            sb.append("WHERE ");
            sb.append("     IMPORTACAO_CODIGO = ? ");
            sb.append("GROUP BY ");
            sb.append("     DESTINO_TABELA");
        }, numImportacao);
        ArrayList<String> resultado = new ArrayList<>();

        while (result.next()) {
            resultado.add(result.getString("DESTINO_TABELA"));
        }

        return resultado;
    }

    @Override
    public boolean run() throws Exception {
        this.arquivoOrigem = new File(this.origem.getAbsolutePath()).getName();

        this.destino.setAutoCommit(false);
        CSPLog.info(this.getClass(), "importação " + this.numImportacao + " inicinado " + this.origem.getAbsolutePath() + "=>" + this.destino.getConfs().getPath() + "...");
        //Pega os dados do arquivo xls e joga em uma matriz.
        Object[][] dadosXls = origem.getInformacoes();

        /*
         * Pega os nomes das colunas
         */
        String[] nomeColunas = new String[dadosXls[0].length];
        System.arraycopy(dadosXls[0], 0, nomeColunas, 0, dadosXls[0].length);

        //Recebe as tabelas que tem importações gravadas no codigo do cPIdentificação1.
        //Percorre as tabelas do banco que tem importações gravadas.
        for (String tabela : encontraTabelasDestino()) {

            //Recebe as colunas da tabela e iguala as colunas do arquivo xls
            //e define em que coluna do banco de dados
            //os valores do arquivo xls vão ser gravados.
            this.confRelacaoDestinoOrigemDados(tabela, null);

            HashMap<String, String> descAdicionalNcm = new HashMap<>();

            //Percorre os dados do arquivo xls.
            for (int j = 1; j < dadosXls.length; j++) {

                //HashMap usado para enviar os dados para serem gravados na tabela.
                HashMap<String, String> dados = new HashMap<>();

                //Percorre as colunas que vão receber os dados.
                for (Map.Entry<String, String> e : getRelacaoTabelasColunasDestinoOrigem(tabela, null).entrySet()) {
                    String out = e.getKey();
                    String in = e.getValue();

                    int colunaXls = -1;

                    //Verifica se a coluna da tabela tem algum dado para receber do arquivo de origem.
                    if (in != null) {

                        //Verifica a coluna que vai receber o dado do arquivo e 
                        //pega a posição em que o dado está no arquivo xls.
                        for (int i = 0; i < nomeColunas.length; i++) {
                            if (nomeColunas[i] != null) {
                                if (nomeColunas[i].trim().equals(in.trim())) {
                                    colunaXls = i;
                                    break;
                                }
                            }
                        }

                        String dadosPut = (dadosXls[j][colunaXls].toString() + "").trim();

                        if (tabela.equals("DC_NCM")) {
                            dados.put(out, dadosPut);
                        } else {
                            dados.put(out, dadosPut.replace(".0", ""));
                        }

                    }
                }

                //Aqui serão feitos os tratamentos necessários para gravar os valores ncm no banco de dados.
                if (tabela.equals("DC_NCM")) {

                    if ((dados.get("NCM_CODIGO") != null) && (dados.get("DESCRICAO") != null)) {

                        String codigo = dados.get("NCM_CODIGO").replace(".", "");
                        String descricao = dados.get("DESCRICAO").replace("-", "");

                        if (codigo.equals("01051110")) {
                            System.out.println("teste");
                        }

                        if (codigo.length() == 8) {

                            dados.put("NCM_CODIGO", codigo);

                            StringBuilder adicionalDesc = new StringBuilder();

                            String c = "";
                            boolean colocaBarra = false;
                            for (int i = 0; i < codigo.length(); i++) {
                                c = c + codigo.charAt(i);
                                if (descAdicionalNcm.get(c) != null) {
                                    if (colocaBarra) {
                                        adicionalDesc.append(" / ");
                                    }
                                    adicionalDesc.append(descAdicionalNcm.get(c));
                                    colocaBarra = true;
                                }
                            }

                            if (colocaBarra) {
                                adicionalDesc.append(" / ");
                            }
                            dados.put("DESCRICAO", adicionalDesc.append(descricao).toString());

                            //envia os dados ja prontos para a gravação na tabela.
                            this.efetuaImportacao(tabela, null, dados, null);

                        } else {
                            if (codigo.length() > 0) {
                                descAdicionalNcm.put(codigo, descricao);
                            }
                        }
                    }
                } else {
                    //envia os dados ja prontos para a gravação na tabela.
                    this.efetuaImportacao(tabela, null, dados, null);
                }

            }
        }
        this.destino.commit();
        this.destino.setAutoCommit(true);
        this.registraHistorico();
        CSPLog.info(this.getClass(), "importação " + this.numImportacao + " finalizada " + this.origem.getAbsolutePath() + "=>" + this.destino.getConfs().getPath());
        return true;
    }

}
