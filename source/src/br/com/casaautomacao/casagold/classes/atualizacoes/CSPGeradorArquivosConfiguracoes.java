/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.atualizacoes;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.PadraoClasses;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosIni;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosJson;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.extraiHostAndPath;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.getHostAndPathFromString;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson.appendObject;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson.getFromJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe responsável por manter os arquivos JSONs de configurações locais
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 09/10/2015 - 13:56:37
 */
@Deprecated
public class CSPGeradorArquivosConfiguracoes {

    private final CSPInstrucoesSQLBase connConfig;
    private final CSPInstrucoesSQLBase connDados;
    private String host;
    private boolean isLocal = false;
    private CSPArquivosJson objHost;

    private String pastaServidor;
    private String pastaParRotinas;
    private String pastaParFormularios;
    private String pastaParTemas;
    private String pastaParMensagens;
    private String pastaParDc;
    private String pastaParHelp;
    private String formulario;

    /**
     * Construtor padrão
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com uma base de configurações
     * @param connDados
     *
     * @param host String - Destino da geração. Quando local passar "127.0.0.1",
     * "localhost" ou o próprio IP
     * @throws java.lang.Exception
     */
    public CSPGeradorArquivosConfiguracoes(CSPInstrucoesSQLBase conn, CSPInstrucoesSQLBase connDados, String host) throws Exception {
        this.connConfig = conn;
        this.connDados = connDados;
        this.host = host;

        this.isLocal = CSPUtilidadesLangRede.isLocalAddress(host);
        this.objHost = new CSPArquivosJson();

        ResultSet select = this.connConfig.select("SELECT SERVIDOR_CONTRATANTE FROM PARAMETROS_GERAIS WHERE SERVIDOR_CONTRATANTE is not null and SERVIDOR_CONTRATANTE <> ''");
        if (select.next()) {
            JSONObject data = new JSONObject(select.getString("SERVIDOR_CONTRATANTE"));
            this.pastaServidor = getFromJson(data, "CPDESTINO1", "");
            this.pastaParRotinas = this.pastaServidor + "/" + getFromJson(data, "CPDESTINO2", "");
            this.pastaParFormularios = this.pastaParRotinas + "/" + getFromJson(data, "CPDESTINO3", "");
            this.pastaParTemas = this.pastaParRotinas + "/" + getFromJson(data, "CPDESTINO4", "");
            this.pastaParMensagens = this.pastaParRotinas + "/" + getFromJson(data, "CPDESTINO5", "");
            this.pastaParDc = this.pastaServidor + "/" + getFromJson(data, "CPDESTINO7", "");
            this.pastaParHelp = this.pastaServidor + "/" + getFromJson(data, "CPDESTINO8", "");
        }

    }

    /**
     * Construtor padrão
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com uma base de configurações
     * @param connDados
     *
     * @param host String - Destino da geração. Quando local passar "127.0.0.1",
     * "localhost" ou o próprio IP
     * @param formulario Formulário que será gerado os arquivos.
     * @throws java.lang.Exception
     */
    public CSPGeradorArquivosConfiguracoes(CSPInstrucoesSQLBase conn, CSPInstrucoesSQLBase connDados, String host, String formulario) throws Exception {
        this(conn, connDados, host);
        this.formulario = formulario;
    }

    /**
     * Auxilia no uso do this.objHost
     *
     * @param src
     */
    private void auxSetPath(String... src) throws Exception {
        this.objHost.setPath(this.buildAuxSetPath(src));
    }

    /**
     * Auxilia no uso do this.objHost
     *
     * @param src
     */
    private String buildAuxSetPath(String... src) throws Exception {
        final StringJoiner tmp = new StringJoiner("/");

        if (!isLocal) {
            tmp.add("/");
            tmp.add(this.host);
        }

        for (String s : src) {
            tmp.add(s);
        }
//        System.out.println(tmp);
        return tmp.toString();
    }

    /**
     * Retorna se existe as configurações para poder trabalhar
     *
     * @return
     */
    public boolean existeConfiguracoes() {
        return this.pastaServidor != null;
    }

    /**
     * Retorna se foi possível detectar que no destino existe um sistema
     * instalado
     *
     * @return
     * @throws java.lang.Exception
     */
    public boolean verificaInstalacaoSistema() throws Exception {
        if (this.pastaServidor != null) {
            this.auxSetPath(this.pastaServidor);
            return objHost.exists();
        } else {
            return false;
        }
    }

    /**
     * Analisa e gera os arquivos de configurações
     *
     * @return
     * @throws java.lang.Exception
     */
    public boolean geraArquivos() throws Exception {
        if (this.pastaServidor != null) {
            CSPLog.info(this.getClass(), "Gerando arquivos...");
            {
                CSPLog.info(this.getClass(), "Gerando arquivos: inicialização...");
                if (!this.geraArquivoInicializacao()) {
                    return false;
                }
            }
            {
                CSPLog.info(this.getClass(), "Gerando arquivos: rotinas...");
                if (!this.geraArquivosRotinas()) {
                    return false;
                }
            }
            {
                CSPLog.info(this.getClass(), "Gerando arquivos: temas...");
                if (!this.geraArquivosTemas()) {
                    return false;
                }
                if (!this.geraArquivosMensagensTemas()) {
                    return false;
                }
            }
            {
                CSPLog.info(this.getClass(), "Gerando arquivos: mensagens...");
                if (!this.geraArquivosMensagens()) {
                    return false;
                }
            }
            {
                CSPLog.info(this.getClass(), "Gerando arquivos: dados comuns...");
                if (!this.geraArquivosDC(this.connConfig, "PAIS_CODIGO", "DESCRICAO", "PAIS", "jdc_paises")) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "ESTADO_CODIGO", "SIGLA", "ESTADO", "jdc_estados", new String[]{"PAIS_CODIGO", "DESCRICAO"})) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "CODIGO_CIDADE", "DESCRICAO", "MUNICIPIOS", "jdc_municipios", new String[]{"PAIS_CODIGO", "ESTADO_CODIGO", "SIGLA"}, "SELECT DISTINCT A.*, B.* FROM MUNICIPIOS A, ESTADO B WHERE A.ESTADO_CODIGO = B.ESTADO_CODIGO")) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "CODIGO", "DESCRICAO", "DC_SPED_FINALIDADE", "jdc_speds_finalidades")) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "CODIGO", "DESCRICAO", "DC_SPED_PERFIL", "jdc_speds_perfil")) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "CODIGO", "DESCRICAO", "DC_SPED_ATIVIDADE", "jdc_speds_atividade")) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "CODIGO", "VERSAO", "DC_SPED_VERSAO", "jdc_speds_versao", new String[]{"INICIO"})) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "CODIGO", "DESCRICAO", "DC_SPED_PRODUTOS_TIPO_ITEM", "jdc_speds_produtos_tipo_item")) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "ICMS_CODIGO", "DESCRICAO", "DC_ICMS", "jdc_icms", new String[]{"ALIQUOTA"})) {
                    return false;
                }
//                if (!this.geraArquivosDC(this.connDados, "UNIDADE_CODIGO", "DESCRICAO", "PRODUTOS_UNIDADES", "jdc_produtos_unidades")) {
//                    return false;
//                }
                if (!this.geraArquivosDC(this.connConfig, "PAGAMENTO_CODIGO", "DESCRICAO", "DC_PAGAMENTO", "jdc_pagamentos", new String[]{"SPED_CODIGO"})) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "FRETE_CODIGO", "DESCRICAO", "DC_FRETE", "jdc_fretes", new String[]{"SPED_CODIGO_FRETE"})) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "UNIDADE_CODIGO", "DESCRICAO", "DC_UNIDADES", "jdc_unidades", new String[]{"DECIMAIS_QUANTIDADE", "DECIMAIS_VENDA"})) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "CFOP_CODIGO", "DESCRICAO", "DC_CFOP", "jdc_cfop")) {
                    return false;
                }
                if (!this.geraArquivosDC(this.connConfig, "DFISCAIS_CODIGO", "DESCRICAO", "DC_DOCUMENTOS_FISCAIS", "jdc_documentos_fiscais", new String[]{"SPED_CODIGO", "EXIGE_CHAVE_ENTRADA"})) {
                    return false;
                }
            }
            {
                CSPLog.info(this.getClass(), "Gerando arquivos: help...");
                if (!this.geraArquivosHelp()) {
                    return false;
                }
            }
            CSPLog.info(this.getClass(), "Gerando arquivos...OK");
            return true;
        } else {
            return false;
        }
    }
    ////////////////////////////////// INICIALIZACAO //////////////////////////////////

    /**
     * Gera o arquivo de inicialização do sistema sem nenhuma adição ao arquivo.
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean geraArquivoInicializacao() throws Exception {
        return this.geraArquivoInicializacao(new JSONObject());
    }

    /**
     * Gera o arquivo de inicialização do sistema.
     *
     * @param adds JSONObject - Recebe valor que deve ser adicionado no arquivo
     * json.
     * @return
     * @throws java.lang.Exception
     */
    public boolean geraArquivoInicializacao(JSONObject adds) throws Exception {

        objHost.mkdirs();
        final JSONObject toSave = new JSONObject();

        {//Arquivo de inicializaçao
            final String[] ini = new String[]{"gold"};
            CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "PARAMETROS_GERAIS", "SERVIDOR_INTALADOR", (JSONObject data) -> {
                ini[0] = getFromJson(data, "CPARQUIVOS1", "-");

            });

            this.auxSetPath(pastaServidor, ini[0]);
            {//bugfix
                if (isLocal) {//para evitar problemas

                    /**
                     * Existem alguns casos onde já existe um arquivo de
                     * inicialização configurado. Nos casos onde o arquivo
                     * apontado é diferente do novo vamos deletar o arquivo que
                     * apontado para conter as informações de inicialização e o
                     * arquivo que as aponta
                     */
                    final CSPArquivosIni fix = new CSPArquivosIni(this.pastaServidor + "/" + PadraoClasses.arquivoInicializacao);
                    if (fix.isFile() && fix.getInfos() != null) {
                        if (fix.getInfos().getProperty("init") != null) {
                            final CSPArquivos auxFix = new CSPArquivos(fix.getInfos().getProperty("init"));
                            if (!objHost.getAbsolutePath().equals(auxFix.getAbsolutePath())) {
                                if (auxFix.isFile()) {
                                    auxFix.delete();
                                    fix.delete();
                                }
                            }
                        }
                    }
                }

            }

//            if (objHost.exists()) {
//                CSPUtilidadesLangJson.appendObject(toSave, objHost.getObject());
//            }
        }

        {//Contratante ativo
            toSave.put("CONTRATANTE", "000000");
            toSave.put("CONTABILIDADE", "000000");
            ResultSet select = this.connConfig.select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("    r.CONTRATANTE, ");
                sb.append("    r.CONTABILIDADE ");
                sb.append("FROM ");
                sb.append("    CONF_PARTICULARES_CONTRATANTE r ");
                sb.append("WHERE ");
                sb.append("    r.CONTRATANTE is not null AND ");
                sb.append("    r.CONTABILIDADE is not null AND ");
                sb.append("    r.CONTRATANTE <> '' AND ");
                sb.append("    r.CONTABILIDADE <> '' ");
            });

            if (select.next()) {
                toSave.put("CONTRATANTE", select.getString("CONTRATANTE"));
                toSave.put("CONTABILIDADE", select.getString("CONTABILIDADE"));
            }
        }

        {//Bases Default
            toSave.put("CONFIG_GOLD_HOST", "localhost");
            toSave.put("CONFIG_GOLD_CAMINHO", auxGeraArquivoInicializacao("CONFIG_GOLD.fdb", new String[]{"atualizacao_temp", "logs", "backup", "atualizacao"}, true, pastaServidor + "/Base de Dados/CONFIG_GOLD.fdb"));
            toSave.put("DADOS_CONTRATANTE_HOST", "localhost");
            toSave.put("DADOS_CONTRATANTE_CAMINHO", auxGeraArquivoInicializacao("DADOS_CONTRATANTE.fdb", new String[]{"atualizacao_temp", "logs", "backup", "atualizacao"}, true, pastaServidor + "/Base de Dados/DADOS_CONTRATANTE.fdb"));
        }

        if (adds.length() > 0) {
            appendObject(toSave, adds);
        }

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "CONF_PARTICULARES_CONTRATANTE", "PARAMETROS_GERAL", (JSONObject data) -> {
            String[] tCmg = extraiHostAndPath(getFromJson(data, "CP1ARQUIVOS1", ""));
            toSave.put("BI_SERVIDORES_HOST", tCmg[0]);
            toSave.put("BI_SERVIDORES_CAMINHO", tCmg[1]);
            toSave.put("BI_SERVIDORES_INTERVALO_REGISTRO_CONSUMO_RAM", getFromJson(data, "CPVALOR5", 0));

            toSave.put("CMG_LOGS_IS_ENABLED_PROCESSA", CSPUtilidadesLangRede.isLocalAddress(getFromJson(data, "CPIP2", "no")));
//            toSave.put("CMG_LOGS_HOST_CENTRAL", getFromJson(data, "CPIP1", ""));
            tCmg = extraiHostAndPath(
                    getFromJson(data, "CPARQUIVOS1",
                            //Compatibilidade é bom
                            getFromJson(data, "CPARQUIVOS1_CAMINHO", "")
                    )
            );
            toSave.put("CMG_LOGS_BICMG_HOST", tCmg[0]);
            toSave.put("CMG_LOGS_BICMG_CAMINHO", tCmg[1]);
            toSave.put("CMG_LOGS_MAX_SIZE", getFromJson(data, "CPVALOR1", 0));
            toSave.put("CMG_LOGS_MAX_SIZE_PROCESSADOS", getFromJson(data, "CPVALOR4", 0));
            tCmg = extraiHostAndPath(getFromJson(data, "CPDESTINO1", ""));
            toSave.put("CMG_LOGS_HOST_BACKUP", tCmg[0]);
            toSave.put("CMG_LOGS_CAMINHO_BACKUP", tCmg[1]);

            toSave.put("CAPP_LOGS_IS_ENABLED_PROCESSA",
                    getFromJson(data, "CPATIVACAO2", "").equals("X")
                    && CSPUtilidadesLangRede.isLocalAddress(getFromJson(data, "CPIP2", "no")));
//            toSave.put("CAPP_LOGS_HOST_CENTRAL", getFromJson(data, "CPIP1", ""));
            String[] tCapp = extraiHostAndPath(
                    getFromJson(data, "CPARQUIVOS2",
                            //Compatibilidade é bom
                            getFromJson(data, "CPARQUIVOS2_CAMINHO", "")
                    )
            );
            toSave.put("CAPP_LOGS_BICAPP_HOST", tCapp[0]);
            toSave.put("CAPP_LOGS_BICAPP_CAMINHO", tCapp[1]);
            toSave.put("CAPP_LOGS_MAX_SIZE", getFromJson(data, "CPVALOR2", 0));
            toSave.put("CAPP_LOGS_MAX_SIZE_PROCESSADOS", getFromJson(data, "CPVALOR3", 0));
            tCapp = extraiHostAndPath(getFromJson(data, "CPDESTINO2", ""));
            toSave.put("CAPP_LOGS_HOST_BACKUP", tCapp[0]);
            toSave.put("CAPP_LOGS_CAMINHO_BACKUP", tCapp[1]);
            {
                /**
                 * Como pode existir mais de um CMG/CAPP vamos prover os devido
                 * suporte
                 *
                 */
                final ResultSet select = this.connConfig.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("    r.SERVIDOR_CODIGO, ");
                    sb.append("    r.IP_ACESSO_EXTERNO, ");
                    sb.append("    r.TIPO_SERVIDOR, ");
                    sb.append("    r.IP_ACESSO_INTERNO ");
                    sb.append("FROM ");
                    sb.append("    SERVIDORES_CAPP_CMG r ");
                });

                final JSONArray arrHostsCmg = new JSONArray();
                final JSONArray arrHostsCapp = new JSONArray();

                while (select.next()) {

                    if (select.getString("TIPO_SERVIDOR").equals("0") || select.getString("TIPO_SERVIDOR").equals("2")) {
                        arrHostsCmg.put(new JSONObject() {
                            {
                                put("CODIGO", select.getString("SERVIDOR_CODIGO"));
                                put("IP_EXTERNO", select.getString("IP_ACESSO_EXTERNO"));
                                put("IP_INTERNO", select.getString("IP_ACESSO_INTERNO"));
                            }
                        });
                    }

                    if (select.getString("TIPO_SERVIDOR").equals("0") || select.getString("TIPO_SERVIDOR").equals("1")) {
                        arrHostsCapp.put(new JSONObject() {
                            {
                                put("CODIGO", select.getString("SERVIDOR_CODIGO"));
                                put("IP_EXTERNO", select.getString("IP_ACESSO_EXTERNO"));
                                put("IP_INTERNO", select.getString("IP_ACESSO_INTERNO"));
                            }
                        });
                    }

                }

                toSave.put("CMG_LOGS_HOSTS", arrHostsCmg);
                toSave.put("CAPP_LOGS_HOSTS", arrHostsCapp);
            }
        });

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "CONF_PARTICULARES_CONTRATANTE", "MG_LOCAL", (JSONObject data) -> {
            toSave.put("MG_ATENDIMENTO_LOCAL_ENABLED", getFromJson(data, "CPATIVACAO1", "").equals("X"));
            toSave.put("MG_ATENDIMENTO_LOCAL_TIMEOUT_DEFAULT", getFromJson(data, "CPVALOR1", -1));
        });

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "PARAMETROS_GERAIS", "SERVIDOR_CONTRATANTE", (JSONObject data) -> {
            toSave.put("PASTA_SERVIDOR", getFromJson(data, "CPDESTINO1", ""));
            toSave.put("PASTA_PAR_ROTINAS", getFromJson(data, "CPDESTINO2", ""));
            toSave.put("PASTA_PAR_FORMULARIOS", getFromJson(data, "CPDESTINO3", ""));
            toSave.put("PASTA_PAR_TEMAS", getFromJson(data, "CPDESTINO4", ""));
            toSave.put("PASTA_PAR_MSG", getFromJson(data, "CPDESTINO5", ""));
            toSave.put("PASTA_PAR_MSG_IMAGENS", getFromJson(data, "CPDESTINO6", ""));
            toSave.put("PASTA_PAR_DC", getFromJson(data, "CPDESTINO7", ""));
            toSave.put("PASTA_PAR_HELP", getFromJson(data, "CPDESTINO8", ""));
            toSave.put("PASTA_PAR_APP", getFromJson(data, "CPDESTINO9", ""));
            toSave.put("PASTA_PAR_APP_IMAGENS", getFromJson(data, "CPDESTINO10", ""));
            toSave.put("BACKUP_TEMP", getFromJson(data, "CPDESTINO11", ""));
            toSave.put("PASTA_PAR_ADMIN", getFromJson(data, "CPDESTINO12", ""));
            toSave.put("PASTA_PAR_ADMIN_XML", getFromJson(data, "CPDESTINO13", ""));
//            toSave.put("PASTA_PAR_INTEGRACAO", getFromJson(data, "CPDESTINO14", ""));
            String[] dadosAlteracao = extraiHostAndPath(getFromJson(data, "CP1ARQUIVOS1", ""));
            toSave.put("INTEGRACAO_BASE_APP_HOST", dadosAlteracao[0]);
            toSave.put("INTEGRACAO_BASE_APP_CAMINHO", dadosAlteracao[1]);
            toSave.put("PASTA_PAR_INTEGRACAO_IMAGENS", getFromJson(data, "CPDESTINO15", ""));
        });

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "PARAMETROS_GERAIS", "SERVIDOR_CASAVISUAL", (JSONObject data) -> {
            String[] tCasaVisual = extraiHostAndPath(getFromJson(data, "CPARQUIVOS1", ""));
            toSave.put("PADRAO_CASAVISUAL_HOST", tCasaVisual[0]);
            toSave.put("PADRAO_CASAVISUAL_CAMINHO", tCasaVisual[1]);
        });

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "SERVIDOR_EGULA", "PARAMETROS_GERAIS", (JSONObject data) -> {
            String[] tCapp = extraiHostAndPath(getFromJson(data, "CP1ARQUIVOS1", ""));
            toSave.put("CAPP_BASE_APP_HOST", tCapp[0]);
            toSave.put("CAPP_BASE_APP_CAMINHO", tCapp[1]);
            toSave.put("CAPP_PASTA_IMAGENS_ADMINISTRATIVAS", getFromJson(data, "CPDESTINO1", ""));
        });

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "PARAMETROS_GERAIS", "SERVIDOR_INTEGRACAO", (JSONObject data) -> {
            String[] tCapp = extraiHostAndPath(getFromJson(data, "CPARQUIVOS1", ""));
            toSave.put("INTEGRACAO_BASE_APP_HOST_CENTRAL", tCapp[0]);
            toSave.put("INTEGRACAO_BASE_APP_CAMINHO_CENTRAL", tCapp[1]);
            toSave.put("INTEGRACAO_PASTA_IMAGENS_CENTRAL", getFromJson(data, "CPDESTINO1", ""));
        });

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "PARAMETROS_GERAIS", "SERVIDOR_BACKUP_ON_LINE", (JSONObject data) -> {
            String[] tBack = extraiHostAndPath(getFromJson(data, "CPDESTINO1", ""));
            toSave.put("BACKUP_ONLINE_BASES_HOST", tBack[0]);
            toSave.put("BACKUP_ONLINE_BASES_CAMINHO", tBack[1]);
        });

        CSPUtilidadesLangInstrucoesSQL.auxReadInfosJsonCampo(this.connConfig, "PARAMETROS_GERAIS", "SERVIDOR_SPED_CASAVISUAL", (JSONObject data) -> {
            toSave.put("PASTA_SERVIDOR_SPED_CASAVISUAL", getFromJson(data, "CPDESTINO1", ""));
        });

        for (Object n : toSave.names()) {
            if (toSave.get((String) n) != null && toSave.get((String) n).getClass().equals(String.class)) {
                toSave.put((String) n, (toSave.get((String) n) + "").trim().replace("\\", "/"));//Pra não dar erros :)
            }
        }

        toSave.remove("PARAMETROS_GERAL");
        toSave.remove("PARAMETRO_GERAL");
        toSave.remove("PARAMETROS_GERAIS");
        toSave.remove("ALTERACAO_GOLD");
        toSave.remove("MG_LOCAL");

        objHost.setObject(toSave);
        CSPLog.info(getClass(), "arquivo de inicialização gerado em: " + this.objHost.getAbsolutePath());

        
        return true;
    }

    /**
     * Método auxiliar para {@link #auxGeraArquivoInicializacao(java.lang.String, java.lang.String)
     * }. O método percorre a pasta do servidor de destino a procura do
     * arquivo/pasta a ser fitrado
     *
     * @param filtro String - Valor usado para filtrar os resultados, por
     * exemplo o nome de uma base
     * @param filtroIgnorar String[] - Valores que devem ser ignorados
     * @param filtroIsFile boolean - Determina se é procurado um arquivo ou
     * diretório
     * @param valorDefault String - Caso não for encontrado o que é buscado é
     * assumido esse valor
     * @return
     */
    private String auxGeraArquivoInicializacao(String filtro, String[] filtroIgnorar, boolean filtroIsFile, String valorDefault) throws Exception {
        //Só percorrer a pasta this.pastaServidor, ir filtrando pelo filtro
        //e ir ignorando os valores em filtroIgnorar
        //Considerar também filtroIsFile
        //Usar endsWith() e não equals()!!!
//System.out.println(this.buildAuxSetPath(this.pastaServidor));
        String resultado = new CSPArquivos(this.buildAuxSetPath(this.pastaServidor)).findFile(filtro, filtroIgnorar, filtroIsFile, true);
//        System.out.println(resultado);
        if (resultado == null) {
            resultado = new CSPArquivos(this.buildAuxSetPath(this.pastaServidor)).findFile(filtro, filtroIgnorar, filtroIsFile, false);
        }
//        System.out.println(resultado);

        if (resultado != null) {

            return getHostAndPathFromString(resultado)[1];

        }

//        System.out.println(valorDefault);
        return valorDefault;
    }

    ////////////////////////////////// INICIALIZACAO //////////////////////////////////
    ///////////////////////////////////// ROTINAS /////////////////////////////////////
    /**
     * Analisa e gera os arquivos referentes as rotinas do sistema
     *
     * @return
     * @throws java.lang.Exception
     */
    public boolean geraArquivosRotinas() throws Exception {

        this.auxSetPath(this.pastaParFormularios);
        objHost.mkdirs();

        String[] efeitosPossiveis = new String[]{"CP_TITULO_FOCO", "CP_FUNDO_EDIT_FOCO", "CP_LETRA_EDIT_FOCO", "CP_TITULO_FOCO_ERRO", "CP_FUNDO_EDIT_FOCO_ERRO", "CP_LETRA_EDIT_FOCO_ERRO", "CP_TITULO_SFOCO_SALTERACAO", "CP_FUNDO_EDIT_SFOCO_SALTERACAO", "CP_LETRA_EDIT_SFOCO_SALTERACAO", "CP_TITULO_SFOCO_ALTERADO", "CP_FUNDO_EDIT_SFOCO_ALTERADO", "CP_LETRA_EDIT_SFOCO_ALTERADO", "CP_TITULO_SFOCO_ESP", "CP_FUNDO_EDIT_SFOCO_ESP", "CP_LETRA_EDIT_SFOCO_ESP"};
        ResultSet forms;

        if (formulario == null) {
            forms = this.connConfig.select("SELECT * FROM FORMULARIOS_PARAMETROS");
        } else {
            forms = this.connConfig.select("SELECT * FROM FORMULARIOS_PARAMETROS WHERE FORMULARIO = ?", formulario);
        }

        HashMap<String, Object> tmpForms = new HashMap<>();//Por não tem como dois select ao mesmo tempo

        while (forms.next()) {
            tmpForms.put(forms.getString("FORMULARIO"), (new HashMap<String, Object>() {
                {
                    put("TITULO", forms.getString("TITULO"));
                    if (forms.getString("TEMA_CODIGO") != null && !forms.getString("TEMA_CODIGO").isEmpty()) {
                        put("TEMA_CODIGO", forms.getString("TEMA_CODIGO"));
                    } else {
                        put("EFEITOS", (new HashMap<String, String>() {
                            {
                                for (String ef : efeitosPossiveis) {
                                    put(ef, forms.getString(ef));
                                }
                                put("FORMULARIO_FUNDO", forms.getString("FORMULARIO_FUNDO"));//Não é comum aos componentes
                            }
                        }));
                    }
                    put("COMPONENTES", new JSONArray());
                }
            }));
        }

        for (Map.Entry<String, Object> e : tmpForms.entrySet()) {
            HashMap<String, Object> t = (HashMap<String, Object>) e.getValue();
            JSONArray arr = new JSONArray();
            ResultSet comps = connConfig.select("SELECT * FROM CAMPOS_FORMULARIOS WHERE FORMULARIO = ?", e.getKey());
            while (comps.next()) {
                arr.put(new HashMap<String, Object>() {
                    {
                        put("CAMPO", comps.getString("CAMPO"));

                        String valorDefault = comps.getString("CP_DEFAUT");
                        if (valorDefault != null) {
                            if (!valorDefault.trim().equals("")) {
                                put("DEFAULT", valorDefault);
                            }
                        }

                        put("LABEL", comps.getString("TITULO"));

                        //Um componente pode ter um efeito proprio, e este tem prioridade total
                        put("EFEITOS", (new HashMap<String, String>() {
                            {

                                for (String ef : efeitosPossiveis) {
                                    ////Pela logica o componente nao precisa setar o seu efeito se este ja esta presente no tema ou formulario
                                    //      if (comps.getString(ef) != null && (!comps.getString(ef).equals(tmpDadosForm.get(ef)))) {
                                    put(ef, comps.getString(ef));
                                    //    }
                                }

                            }
                        }));
                        if (((HashMap<String, String>) get("EFEITOS")).isEmpty()) {
                            remove("EFEITOS");
                        }
                    }

                });
            }
            t.put("COMPONENTES", arr);

            this.auxSetPath(this.pastaParFormularios, e.getKey() + ".json");
            objHost.setContent(new JSONObject(t).toString());
        }

        this.auxSetPath(this.pastaParFormularios);
        CSPLog.info(getClass(), "arquivos de rotinas gerados em: " + this.objHost.getAbsolutePath());
        return true;

    }

    ////////////////////////////////// ROTINAS //////////////////////////////////
    ////////////////////////////////// TEMAS //////////////////////////////////
    /**
     * Analisa e gera os arquivos referentes aos temas do sistema
     */
    private boolean geraArquivosTemas() throws Exception {

        this.auxSetPath(this.pastaParTemas);
        objHost.mkdirs();

        ResultSet temas = this.connConfig.select("SELECT DISTINCT A.*, B.* FROM TEMAS AS A, TEMAS_ROTINAS AS B WHERE B.TEMA_CODIGO = A.TEMA_CODIGO");

        while (temas.next()) {
            this.auxSetPath(this.pastaParTemas, "tema_" + temas.getString("TEMA_CODIGO") + ".json");
            objHost.setContent(new JSONObject(new HashMap<String, Object>() {
                {
                    put("DESCRICAO", temas.getString("DESCRICAO"));
                    put("TEMA_CODIGO", temas.getString("TEMA_CODIGO"));
                    put("ATIVACAO", temas.getString("ATIVACAO"));
                    put("CP_TITULO_FOCO", temas.getString("CP_TITULO_FOCO"));
                    put("CP_FUNDO_EDIT_FOCO", temas.getString("CP_FUNDO_EDIT_FOCO"));
                    put("CP_LETRA_EDIT_FOCO", temas.getString("CP_LETRA_EDIT_FOCO"));
                    put("CP_TITULO_FOCO_ERRO", temas.getString("CP_TITULO_FOCO_ERRO"));
                    put("CP_FUNDO_EDIT_FOCO_ERRO", temas.getString("CP_FUNDO_EDIT_FOCO_ERRO"));
                    put("CP_LETRA_EDIT_FOCO_ERRO", temas.getString("CP_LETRA_EDIT_FOCO_ERRO"));
                    put("CP_TITULO_SFOCO_SALTERACAO", temas.getString("CP_TITULO_SFOCO_SALTERACAO"));
                    put("CP_FUNDO_EDIT_SFOCO_SALTERACAO", temas.getString("CP_FUNDO_EDIT_SFOCO_SALTERACAO"));
                    put("CP_LETRA_EDIT_SFOCO_SALTERACAO", temas.getString("CP_LETRA_EDIT_SFOCO_SALTERACAO"));
                    put("CP_TITULO_SFOCO_ALTERADO", temas.getString("CP_TITULO_SFOCO_ALTERADO"));
                    put("CP_FUNDO_EDIT_SFOCO_ALTERADO", temas.getString("CP_FUNDO_EDIT_SFOCO_ALTERADO"));
                    put("CP_LETRA_EDIT_SFOCO_ALTERADO", temas.getString("CP_LETRA_EDIT_SFOCO_ALTERADO"));
                    put("CP_TITULO_SFOCO_ESP", temas.getString("CP_TITULO_SFOCO_ESP"));
                    put("CP_FUNDO_EDIT_SFOCO_ESP", temas.getString("CP_FUNDO_EDIT_SFOCO_ESP"));
                    put("CP_LETRA_EDIT_SFOCO_ESP", temas.getString("CP_LETRA_EDIT_SFOCO_ESP"));
                    put("FORMULARIO_FUNDO", temas.getString("FORMULARIO_FUNDO"));
                }
            }).toString());

        }

        this.auxSetPath(this.pastaParTemas);
        CSPLog.info(getClass(), "arquivos de temas gerados em: " + this.objHost.getAbsolutePath());
        return true;

    }

    /**
     * Gera os arquivos referentes aos temas das mensagens do sistema
     */
    private boolean geraArquivosMensagensTemas() throws Exception {

        this.auxSetPath(this.pastaParTemas);

        objHost.mkdirs();

        ResultSet temas = this.connConfig.select("SELECT DISTINCT A.*, B.* FROM TEMAS AS A, DC_MENSAGEM_PADROES AS B WHERE B.TEMA_CODIGO = A.TEMA_CODIGO");

        while (temas.next()) {
            this.auxSetPath(this.pastaParTemas, "Tema_Mensagem_" + temas.getString("TEMA_CODIGO") + ".json");
            objHost.setContent(new JSONObject(new HashMap<String, Object>() {
                {
                    put("DESCRICAO", temas.getString("DESCRICAO"));
                    put("TEMA_CODIGO", temas.getString("TEMA_CODIGO"));
                    put("ATIVACAO", temas.getString("ATIVACAO"));
                    put("COR_FUNDO_FORMULARIO", temas.getString("COR_FUNDO_FORMULARIO"));
                    put("COR_LETRA_CONTEUDO", temas.getString("COR_LETRA_CONTEUDO"));
                    put("FONTE", temas.getString("FONTE"));
                    put("FONTE_TAMANHO", temas.getString("FONTE_TAMANHO"));
                }
            }).toString());
        }

        this.auxSetPath(this.pastaParTemas);
        CSPLog.info(getClass(), "arquivos de temas de mensagens gerados em: " + this.objHost.getAbsolutePath());
        return true;

    }

    ////////////////////////////////// TEMAS //////////////////////////////////
    ////////////////////////////////// MENSAGENS //////////////////////////////////
    /**
     * Gera os arquivos referentes as mensagens do sistema
     */
    private boolean geraArquivosMensagens() throws Exception {
        this.auxSetPath(this.pastaParMensagens);

        objHost.mkdirs();

        ResultSet msg = this.connConfig.select("SELECT A.MENSAGEM_CODIGO, A.MENSAGEM_GRUPO, A.MENSAGEM_TEXTO, "
                + "CASE WHEN (A.TEMA_CODIGO IS NULL) THEN "
                + "    CASE WHEN (B.TEMA_CODIGO IS NULL) THEN "
                + "        NULL "
                + "    ELSE "
                + "        B.TEMA_CODIGO "
                + "    END "
                + "ELSE "
                + "    A.TEMA_CODIGO "
                + "END AS TEMA_CODIGO, A.COR_FUNDO_FORMULARIO AS \"COR_FUNDO_A\", A.COR_LETRA_CONTEUDO AS \"COR_LETRA_A\", B.COR_FUNDO_FORMULARIO AS \"COR_FUNDO_B\", B.COR_LETRA_CONTEUDO AS \"COR_LETRA_B\" "
                + "FROM DC_MENSAGENS A, DC_MENSAGEM_GRUPOS B "
                + "WHERE A.MENSAGEM_GRUPO = B.MENSAGEM_GRUPO");

        while (msg.next()) {
            this.auxSetPath(this.pastaParMensagens, msg.getString("MENSAGEM_CODIGO") + "_M" + ".json");
            objHost.setContent(new JSONObject(new HashMap<String, Object>() {
                {
                    put("MENSAGEM_TEXTO", msg.getString("MENSAGEM_TEXTO"));
                    put("MENSAGEM_CODIGO", msg.getString("MENSAGEM_CODIGO"));
                    put("MENSAGEM_GRUPO", msg.getString("MENSAGEM_GRUPO"));

                    //Se não tem cores personalizadas, vai de acordo com as padroes do tema
                    if (msg.getString("COR_FUNDO_A") == null || msg.getString("COR_LETRA_A") == null) {
                        //Se nao retorna nenhum tema_codigo, entao o grupo tem cores personalizadas
                        if (msg.getString("TEMA_CODIGO") == null) {
                            put("TEMA_CODIGO", "");
                            put("COR_FUNDO_FORMULARIO", msg.getString("COR_FUNDO_B"));
                            put("COR_LETRA_CONTEUDO", msg.getString("COR_LETRA_B"));
                        } else {
                            put("TEMA_CODIGO", msg.getString("TEMA_CODIGO"));
                            put("COR_FUNDO_FORMULARIO", "");
                            put("COR_LETRA_CONTEUDO", "");
                        }
                    } else {
                        put("TEMA_CODIGO", "");
                        put("COR_FUNDO_FORMULARIO", msg.getString("COR_FUNDO_A"));
                        put("COR_LETRA_CONTEUDO", msg.getString("COR_LETRA_A"));
                    }
                }
            }).toString());
        }

        this.auxSetPath(this.pastaParMensagens);
        CSPLog.info(getClass(), "arquivos de mensagens gerados em: " + this.objHost.getAbsolutePath());
        return true;

    }

    ////////////////////////////////// MENSAGENS //////////////////////////////////
    ////////////////////////////////// DC //////////////////////////////////
    /**
     * Gera os arquivos de dados comuns
     *
     * @param colCodigo String - Coluna que armazena o código
     * @param colDesc String - Coluna que armazena a descrição
     * @param tabela String - Tabela onde buscar as informações
     * @param arquivoFinal String - Arquivo a ser gerado
     * @return boolean
     */
    private boolean geraArquivosDC(CSPInstrucoesSQLBase conn, String colCodigo, String colDesc, String tabela, String arquivoFinal) throws Exception {
        return this.geraArquivosDC(conn, colCodigo, colDesc, tabela, arquivoFinal, new String[0], null);
    }

    /**
     * Gera os arquivos de dados comuns
     *
     * @param colCodigo String - Coluna que armazena o código
     * @param colDesc String - Coluna que armazena a descrição
     * @param tabela String - Tabela onde buscar as informações
     * @param arquivoFinal String - Arquivo a ser gerado
     * @param select String - Quando é necessário alterar o select padrão
     * @return boolean
     */
    private boolean geraArquivosDC(CSPInstrucoesSQLBase conn, String colCodigo, String colDesc, String tabela, String arquivoFinal, String select) throws Exception {
        return this.geraArquivosDC(conn, colCodigo, colDesc, tabela, arquivoFinal, new String[0], select);
    }

    /**
     * Gera os arquivos de dados comuns
     *
     * @param colCodigo String - Coluna que armazena o código
     * @param colDesc String - Coluna que armazena a descrição
     * @param tabela String - Tabela onde buscar as informações
     * @param arquivoFinal String - Arquivo a ser gerado
     * @param moreColumn String[] - Quando é preciso adicionar mais informações
     * @return boolean
     */
    private boolean geraArquivosDC(CSPInstrucoesSQLBase conn, String colCodigo, String colDesc, String tabela, String arquivoFinal, String[] moreColumn) throws Exception {
        return this.geraArquivosDC(conn, colCodigo, colDesc, tabela, arquivoFinal, moreColumn, null);
    }

    /**
     * Gera os arquivos de dados comuns
     *
     * @param colCodigo String - Coluna que armazena o código
     * @param colDesc String - Coluna que armazena a descrição
     * @param tabela String - Tabela onde buscar as informações
     * @param arquivoFinal String - Arquivo a ser gerado
     * @param moreColumn String[] - Quando é preciso adicionar mais informações
     * @param select String - Quando é necessário alterar o select padrão
     * @return boolean
     */
    private boolean geraArquivosDC(CSPInstrucoesSQLBase conn, String colCodigo, String colDesc, String tabela, String arquivoFinal, String[] moreColumn, String select) throws Exception {
        this.auxSetPath(this.pastaParDc);
        objHost.mkdirs();

        HashMap<String, Object> t = new HashMap<>();

        ResultSet s = conn.select("SELECT MAX(CHARACTER_LENGTH(TRIM(" + colDesc + "))) AS TAMANHO, MAX(CHARACTER_LENGTH(TRIM(" + colCodigo + "))) AS TAMANHO_CODIGO FROM " + tabela);

        JSONObject arr = new JSONObject();
        while (s.next()) {
            arr.put(colDesc, s.getString("TAMANHO"));
            arr.put(colCodigo, s.getString("TAMANHO_CODIGO"));
        }

        t.put("TAMANHOS", arr);

        ResultSet rs;
        if (select == null) {
            rs = conn.select("SELECT * FROM " + tabela);
        } else {
            rs = conn.select(select);
        }

        JSONArray arr2 = new JSONArray();
        while (rs.next()) {
            arr2.put(new HashMap<String, String>() {
                {
                    put(colCodigo, rs.getString(colCodigo));
                    put(colDesc, rs.getString(colDesc));
                    for (String m : moreColumn) {
                        put(m, rs.getString(m));
                    }
                }
            });
        }

        t.put("REGISTROS", arr2);

        this.auxSetPath(this.pastaParDc, arquivoFinal + ".json");
        objHost.setContent(new JSONObject(t).toString().trim());

        CSPLog.info(getClass(), "arquivo de dados comuns gerado em: " + this.objHost.getAbsolutePath());
        return true;

    }

    ////////////////////////////////// DC //////////////////////////////////
    ////////////////////////////////// HELP //////////////////////////////////
    /**
     * Gera os arquivos referentes as menasgens de ajuda do sistema
     */
    private boolean geraArquivosHelp() throws Exception {
        this.auxSetPath(this.pastaParHelp);
        objHost.mkdirs();

        ResultSet rSFormularios = this.connConfig.select("SELECT r.FORMULARIO AS NOME,r.TITULO AS TITULO,r.HELP AS H FROM FORMULARIOS_PARAMETROS r");

        while (rSFormularios.next()) {

            this.auxSetPath(this.pastaParHelp, rSFormularios.getString("NOME"));
            objHost.setObject(new JSONObject(new HashMap() {
                {
                    put("NOME", rSFormularios.getString("NOME"));
                    put("TITULO", rSFormularios.getString("TITULO"));
                    put("CONTEUDO", rSFormularios.getString("H"));
                }
            }));
        }

        rSFormularios.close();

        ResultSet rSMensagens = this.connConfig.select("SELECT r.MENSAGEM_CODIGO AS CODIGO,r.MENSAGEM_TEXTO AS TEXTO,r.HELP AS H FROM DC_MENSAGENS r");
        while (rSMensagens.next()) {

            this.auxSetPath(this.pastaParHelp, "msg" + rSMensagens.getString("CODIGO"));
            objHost.setObject(new JSONObject(new HashMap() {
                {
                    put("NOME", rSMensagens.getString("CODIGO"));
                    put("TITULO", rSMensagens.getString("TEXTO"));
                    put("CONTEUDO", rSMensagens.getString("H"));
                }
            }));
        }

        rSMensagens.close();

        this.auxSetPath(this.pastaParHelp);
        CSPLog.info(getClass(), "arquivos de helps gerados em: " + this.objHost.getAbsolutePath());
        return true;

    }
    ////////////////////////////////// HELP //////////////////////////////////
}
