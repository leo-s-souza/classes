/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosJson;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocaisIni;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasFiscaisBase;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasNaoFiscaisBase;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPServidorComunicacao;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPServidorTransferenciaArquivos;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.exit;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.DIR_SEPARATOR;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Properties;
import org.firebirdsql.jdbc.FBSQLException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 28/09/2015 - 09:31:41
 */
public abstract class PadraoClasses {

    /**
     * Diretório dos arquivos de cache
     */
    public final static String PATH_CACHE = PATH + "/cache";

    /**
     * Armazena os dados de configurações do projeto. Caminhos de pastas,
     * códigos de contratantes....
     */
    private final static HashMap<String, String> dadosConfs = new HashMap<>();

    public static final String arquivoInicializacao = "gold.ini";
    private final CSPArquivosLocaisIni cSPArquivosInicializacao = new CSPArquivosLocaisIni(PATH + "/" + arquivoInicializacao);
    private final CSPArquivosLocais cSPArquivosVarreSistema = new CSPArquivosLocais();
    private boolean instrucoesBaseEncontrada = false;

    /**
     * Classe que guarda a instancia da impressora fiscal que está sendo usada
     * no sistema.
     */
    private static CSPImpressorasFiscaisBase impressoraFiscalVigente;

    /**
     * Impressoras não fiscais
     */
    private static LinkedHashMap<ImpressoraFiscalUso, CSPImpressorasNaoFiscaisBase> IMPRESSORAS_NAO_FISCAIS_VIGENTE = new LinkedHashMap<>();

    /**
     * Adiciona um valor ao hash de valores de configuração
     *
     * @param key String - Nome da posição
     * @param val String - Valor da posição
     * @param makeFolder boolean - Se o sistema deve tentar criar a uma pasta
     * com o conteúdo do valor
     */
    protected static void putDadosConf(String key, String val, boolean makeFolder) {
        dadosConfs.put(key, val);
        if (makeFolder) {
            new CSPArquivosLocais(val).mkdirs();
        }
    }

    /**
     * Retorna o valor da posição do hash de configuração
     *
     * @param key String - Nome da posição
     */
    public static String getDadosConf(String key) {

        if (dadosConfs.containsKey(key) && dadosConfs.get(key) != null && !dadosConfs.get(key).trim().isEmpty()) {
            return dadosConfs.get(key);
        }

        return null;
    }

    /**
     * Retornao caminho da pasta dos arquivos de dados comuns
     *
     * @return
     */
    public static String getPastaArquivosDadosComuns() {
        return PadraoClasses.dadosConfs.get("pasta-arquivos-dados-comuns");
    }

    /**
     * Retorna o código do contratante usando o sistema
     *
     */
    public static String getCodigoContratante() {
        return PadraoClasses.dadosConfs.get("codigo-contratante");
    }

    /**
     * Retorna o código da contabilidade do contratante usando o sistema
     *
     */
    public static String getCodigoContabilidadeContratante() {
        return PadraoClasses.dadosConfs.get("codigo-contabilidade-contratante");
    }

    static {
        CSPException.addAction(new CSPException.Error() {

            @Override
            public void run(Throwable ex, Object[] params) {
                if (ex.getClass().equals(FBSQLException.class)) {
                    CSPLog.error("Problem with last SQl executed. " + this.trataException(ex));
                } else {
                    CSPLog.error("Erro (exception): " + this.trataException(ex));
                }
            }

            private String trataException(Throwable ex) {
                String exM = LINE_SEPARATOR;
                exM += "        Message: " + ex.getMessage() + LINE_SEPARATOR;
                exM += "        Localized Mensage: " + ex.getLocalizedMessage() + LINE_SEPARATOR;
                exM += "        Trace:" + LINE_SEPARATOR;
                StringWriter errors = new StringWriter();
                ex.printStackTrace(new PrintWriter(errors));
                exM += "		" + errors.toString().replace("	", "		");
                return exM;
            }

        });
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            CSPException.register((Throwable) e);
        });

        CSPServidorComunicacao.setDefaultConectando((Socket sc, JSONObject input) -> true);
        CSPServidorComunicacao.setDefaultDesconectado((Socket sc, JSONObject input, JSONObject output) -> {
        });
        CSPServidorTransferenciaArquivos.setDefaultConectando((Socket sc, CSPArquivos file, JSONObject input) -> true);
        CSPServidorTransferenciaArquivos.setDefaultDesconectado((Socket sc, CSPArquivos file, JSONObject input) -> {
        });

        CSPComunicacao.setValidadeDefault((String host, int port) -> true);
        CSPArquivos.setDefAutenticacao((String host, String path) -> {
            CSPLog.info("usando dados default para autenticação SMB(//" + host + "/" + path + ")!");
            return new String[]{"cautomacao", "cri$$@2011", ""};
        });

        CSPComunicacao.setHostBuilderDefault((int numTentativa, CSPComunicacao.ServicoAuxHost servType, int port) -> {
            switch (servType) {
                case CAPP:
                case CMG:
                    return "177.54.11.197";
                case CAPP_TASK:
                case CMG_TASK:
                    return "192.168.1.204";
//                    return "192.168.0.84";
                case LOCAL:
                    return "localhost";
                default:
                    return null;
            }
        });

        /**
         * Configuração 'default' para as conexões com bases no sistema
         */
        CSPInstrucoesSQLBase.setInfosBase(CSPInstrucoesSQLBase.Bases._BASE_CUSTOMIZADA, "localhost", "", "SYSDBA", "masterkey", 3050, "UTF8");

    }

    protected void onJsonInfosNotFound() throws Exception {
        CSPLog.error("JSON de configurações não encontrado!");
        exit();
    }

    @Deprecated
    protected void onRunningPathNotEqualsJson(CSPArquivosLocaisIni GOLDini) throws Exception {
        CSPLog.error("O diretório especificado no JSON de configuração não é o mesmo da aplicação no momento");
        exit();
    }

    protected void onJsonInfosOk(JSONObject data, String path) throws Exception {

    }

    /**
     * Validações Iniciais
     *
     * @throws Exception
     */
    @Deprecated
    final protected void initValidations() throws Exception {
        instrucoesBaseEncontrada = false;
        if (!cSPArquivosInicializacao.exists()) {
            /**
             * Primeiro listamos os arquivos da pasta atual
             */
            for (File f : new File(PATH).listFiles()) {
                if (f.exists() && f.isFile()) {
                    this.encontraInstrucoesArquivoInicializacao(f.getAbsolutePath());
                }
            }
            if (instrucoesBaseEncontrada != true) {
                this.encontraInstrucoesArquivoInicializacao(PATH);
            }
            if (instrucoesBaseEncontrada != true) {
                this.onJsonInfosNotFound();
            }
        }
        String caminhoEsperado = null;
        Properties goldIni = getPropIniGold();
        if (goldIni != null) {
            if (goldIni.getProperty("init") != null) {

                String get = new CSPArquivosLocais(goldIni.getProperty("init")).getContent();
                if (CSPUtilidadesLangJson.isJson(get)) {

                    JSONObject ar = new JSONObject(get);
                    if (!ar.isNull("PASTA_SERVIDOR")) {

                        caminhoEsperado = ar.getString("PASTA_SERVIDOR");
                        this.onJsonInfosOk(ar, caminhoEsperado);
                        //Define o código do contratnte
                        PadraoClasses.dadosConfs.put("codigo-contratante", ar.getString("CONTRATANTE"));
                        //Define o código da contabilidade do contratnte
                        PadraoClasses.dadosConfs.put("codigo-contabilidade-contratante", ar.getString("CONTABILIDADE"));

                    }
                }
            }
        }
        if (caminhoEsperado == null || !caminhoEsperado.equals(PATH)) {
            this.onRunningPathNotEqualsJson(this.cSPArquivosInicializacao);
        }

    }

    protected final void initValidationsNovo() throws Exception {
        CSPArquivosJson a = new CSPArquivosJson(PATH + "/startgold.json");

        if (a.isFile() && CSPUtilidadesLangJson.isJson(a.getContent())) {
            JSONObject get = a.getObject();
            if (get != null && get.names().length() > 0) {
                this.onJsonInfosOk(get, PATH);
                return;
            }
        }

        this.onJsonInfosNotFound();
    }

    private Properties getPropIniGold() throws IOException {
        return cSPArquivosInicializacao.getInfos();
    }

    /**
     * Varre o sistema buscando as informações do arquivo de inicializacao
     */
    private void encontraInstrucoesArquivoInicializacao(String base) throws Exception {
        cSPArquivosVarreSistema.setPath(base);
        if (cSPArquivosVarreSistema.isDir()) {
            for (String d : cSPArquivosVarreSistema.list()) {
                this.encontraInstrucoesArquivoInicializacao(base + "/" + d);
            }
        } else if (cSPArquivosVarreSistema.getPath().toLowerCase().endsWith(".json")) {
            String get = cSPArquivosVarreSistema.getContent();
            if (CSPUtilidadesLangJson.isJson(get)) {
                try {
                    JSONObject ar = new JSONObject(get);
                    if (!ar.isNull("PASTA_SERVIDOR")) {
                        CSPLog.info("Arquivo inicialização encontrado: " + cSPArquivosVarreSistema.getAbsolutePath());
                        if (instrucoesBaseEncontrada != true) {
                            Properties p = new Properties();
                            p.put("init", cSPArquivosVarreSistema.getAbsolutePath().replace(DIR_SEPARATOR, "/"));
                            cSPArquivosInicializacao.setInfos(p);
                            instrucoesBaseEncontrada = true;
                        }
                    }
                } catch (JSONException e) {

                }
            }

        }
    }

    /**
     * Retorna a impressora fiscal vigente no sistema.
     *
     * @return CSPImpressorasFiscaisBase
     * @see #impressoraFiscalVigente
     */
    public static CSPImpressorasFiscaisBase getImpressoraFiscalVigente() {
        return impressoraFiscalVigente;
    }

    /**
     * Seta a impressora fiscal vigente no sistema.
     *
     * @param impressoraFiscalVigente
     * @throws java.lang.Exception
     * @see #impressoraFiscalVigente
     */
    public static void setImpressoraFiscalVigente(CSPImpressorasFiscaisBase impressoraFiscalVigente) throws Exception {
        PadraoClasses.impressoraFiscalVigente = impressoraFiscalVigente;
        PadraoClasses.impressoraFiscalVigente.start();
    }

    /**
     * Retorna a impressora default não fiscal vigente no sistema.
     *
     * @return CSPImpressorasNaoFiscaisBase
     * @see #setImpressoraNaoFiscalVigente
     */
    public static CSPImpressorasNaoFiscaisBase getImpressoraNaoFiscalVigente() {
        return IMPRESSORAS_NAO_FISCAIS_VIGENTE.get(ImpressoraFiscalUso.DEFAULT);
    }

    /**
     * Retorna a impressora não fiscal vigente no sistema.
     *
     * @param uso ImpressoraFiscalUso
     * @return CSPImpressorasNaoFiscaisBase
     * @see #setImpressoraNaoFiscalVigente
     */
    public static CSPImpressorasNaoFiscaisBase getImpressoraNaoFiscalVigente(ImpressoraFiscalUso uso) {
        return IMPRESSORAS_NAO_FISCAIS_VIGENTE.get(uso);
    }

    /**
     * Seta a impressora default não fiscal vigente no sistema.
     *
     * @param impressora CSPImpressorasNaoFiscaisBase
     * @throws java.lang.Exception
     * @see #getImpressoraNaoFiscalVigente
     */
    public static void setImpressoraNaoFiscalVigente(CSPImpressorasNaoFiscaisBase impressora) throws Exception {
        setImpressoraNaoFiscalVigente(ImpressoraFiscalUso.DEFAULT, impressora);
    }

    /**
     * Configura as impressoras não fiscais do sistema
     *
     * @param uso ImpressoraFiscalUso
     * @param impressora CSPImpressorasNaoFiscaisBase
     * @throws java.lang.Exception
     * @see #getImpressoraNaoFiscalVigente
     */
    public static void setImpressoraNaoFiscalVigente(ImpressoraFiscalUso uso, CSPImpressorasNaoFiscaisBase impressora) throws Exception {
        impressora.start();
        IMPRESSORAS_NAO_FISCAIS_VIGENTE.put(uso, impressora);
    }

    public enum ImpressoraFiscalUso {
        DEFAULT,
        PEDIDO_EGULA
    }
}
