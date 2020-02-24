/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.atualizacoes;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosJson;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.getHostAndPathFromString;
import java.sql.ResultSet;
import java.util.StringJoiner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Gera o arquivo de inicialização/configurações da aplicação
 *
 * @author Fernando Batels<luisfbatels@gmail.com>
 */
public class CSPGeradorArquivosConfiguracoesNovo {

    /**
     * Json de configuração.
     */
    private final CSPArquivosJson startGoldJson;

    private final CSPInstrucoesSQLBase conn;

    /**
     * Gera o arquivo de inicialização/configurações da aplicação
     *
     * @param connContratantes CSPInstrucoesSQLBase - Dados contratantes
     * @throws Exception
     */
    public CSPGeradorArquivosConfiguracoesNovo(CSPInstrucoesSQLBase connContratantes) throws Exception {
        this.startGoldJson = new CSPArquivosJson(CSPUtilidadesApplication.PATH + "/startgold.json");
        this.conn = connContratantes;
    }

    /**
     * Gera os arquivos de inicialização do sistema.
     *
     * @throws Exception
     */
    public void geraArquivos() throws Exception {
        geraArquivoInicializacao();
    }

    /**
     * Gera o arquivo de inicialização do sistema.
     *
     * @throws java.lang.Exception
     */
    private void geraArquivoInicializacao() throws Exception {
        CSPLog.info(this.getClass(), "Gerando arquivos: inicialização...");

        final JSONObject toSave = new JSONObject();
        {//Bases Default
            if (this.conn == null) {
                toSave.put("DADOS_CONTRATANTE_HOST", "localhost");
                toSave.put("DADOS_CONTRATANTE_CAMINHO", auxGeraArquivoInicializacao("DADOS_CONTRATANTE-nova.fdb", new String[]{"atualizacao_temp", "logs", "backup", "atualizacao", "atualizacoes"}, true, CSPUtilidadesApplication.PATH + "/Base de Dados/DADOS_CONTRATANTE-nova.fdb"));
            } else {
                toSave.put("DADOS_CONTRATANTE_HOST", this.conn.getConfs().getHost());
                toSave.put("DADOS_CONTRATANTE_CAMINHO", this.conn.getConfs().getPath());
            }
        }

        {//Módulos Hábilitados MG
            ResultSet select = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE).select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("    mh.NOME_FONTE ");
                sb.append("FROM ");
                sb.append("    MODULO_MG_HABILITADO mh ");
                sb.append("JOIN ");
                sb.append("    ESTACAO_MG em ON em.ID = mh.ESTACAO_MG_ID ");
                sb.append("WHERE ");
                sb.append("    em.IS_ATUAL = 1 ");
            });

            JSONArray ja = new JSONArray();

            while (select.next()) {
                ja.put(select.getString("NOME_FONTE"));
            }

            toSave.put("MODULOS_MG", ja);
        }

        this.getStartGoldJson().setObject(toSave);
        CSPLog.info(getClass(), "arquivo de inicialização gerado em: " + this.getStartGoldJson().getAbsolutePath());
    }

    /**
     * Método auxiliar para auxGeraArquivoInicializacao. O método percorre a
     * pasta do servidor de destino a procura do arquivo/pasta a ser fitrado
     *
     * @param filtro String - Valor usado para filtrar os resultados, por
     * exemplo o nome de uma base
     * @param filtroIgnorar String[] - Valores que devem ser ignorados
     * @param filtroIsFile boolean - Determina se é procurado um arquivo ou
     * diretório
     * @param valorDefault String - Caso não for encontrado o que é buscado é
     * assumido esse valor
     * @return String
     */
    private String auxGeraArquivoInicializacao(String filtro, String[] filtroIgnorar, boolean filtroIsFile, String valorDefault) throws Exception {
        String resultado = new CSPArquivos(this.buildAuxSetPath(CSPUtilidadesApplication.PATH)).findFile(filtro, filtroIgnorar, filtroIsFile, true);

        if (resultado == null) {
            resultado = new CSPArquivos(this.buildAuxSetPath(CSPUtilidadesApplication.PATH)).findFile(filtro, filtroIgnorar, filtroIsFile, false);
        }

        if (resultado != null) {
            return getHostAndPathFromString(resultado)[1];

        }

        return valorDefault;
    }

    /**
     * Auxilia no uso do objHost.
     *
     * @param src
     * @see #objHost
     */
    private String buildAuxSetPath(String... src) throws Exception {
        final StringJoiner tmp = new StringJoiner("/");

        for (String s : src) {
            tmp.add(s);
        }
        return tmp.toString();
    }

    /**
     * Retorna o Json de configuração.
     *
     * @return CSPArquivosJson
     * @see #objHost
     */
    private CSPArquivosJson getStartGoldJson() {
        return startGoldJson;
    }
}
