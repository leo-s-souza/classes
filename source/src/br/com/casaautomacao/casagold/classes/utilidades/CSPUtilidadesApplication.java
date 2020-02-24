/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.PadraoClasses;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.modelos.ModelAgenteJuridico;
import br.com.casaautomacao.casagold.classes.modelos.ModelBairro;
import br.com.casaautomacao.casagold.classes.modelos.ModelCidade;
import br.com.casaautomacao.casagold.classes.modelos.ModelContratanteAtivo;
import br.com.casaautomacao.casagold.classes.modelos.ModelEndereco;
import br.com.casaautomacao.casagold.classes.modelos.ModelEstado;
import br.com.casaautomacao.casagold.classes.modelos.ModelPais;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL.hasColumn;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.getColunasNomes;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.isSoWindows;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.startJar;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Métodos de auxilio para recursos mais restritos a nossa aplicação
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 04/04/2016 - 14:49:24
 */
public abstract class CSPUtilidadesApplication {

    private static boolean ENABLE_CALL_EXIT = true;

    private static boolean ENABLE_CALL_RESTART = true;

    /**
     * Caminho atual do sistema
     */
    public final static String PATH = System.getProperty("user.dir").replace("\\", "/");

    /**
     * Configura se a api deve ou não executar o fechamento do processo atual ao
     * usar o método #exit()
     *
     * @param e boolean
     */
    public static void setEnableCallExit(boolean e) {
        CSPLog.info(CSPUtilidadesApplication.class, "Configuração de fechamento do sistema alterada para:" + e);
        CSPUtilidadesApplication.ENABLE_CALL_EXIT = e;
    }

    /**
     * Configura se a api deve ou não executar a reinicialização do processo
     * atual ao usar o método #restart()
     *
     * @param e boolean
     */
    public static void setEnableCallRestart(boolean e) {
        CSPLog.info(CSPUtilidadesApplication.class, "Configuração de reinicialização do sistema alterada para:" + e);
        CSPUtilidadesApplication.ENABLE_CALL_RESTART = e;
    }

    /**
     * Fecha o sistema
     */
    public static void exit() {

        if (!ENABLE_CALL_EXIT) {
            CSPLog.info(CSPUtilidadesApplication.class, "Fechamento do sistema abortado conforme configuração");
            return;
        }

        //Centraliza para poder gerar os LOGs
        CSPLog.info(CSPUtilidadesApplication.class, "Sistema finalizado");
        System.exit(0);
    }

    /**
     * Reinicia o sistema.
     * <b style="color:#f00">O SISTEMA NÃO REINICIA QUANDO USADO PELO
     * NETBEANS!</b>
     *
     * @param responsavel Class - Classe que solicitou o reinciamento
     * @param restartEm int - Tempo que deve ser aguadardado antes de reiniciar
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static void restart(Class responsavel, int restartEm) throws Exception {

        if (!ENABLE_CALL_RESTART) {
            CSPLog.info(CSPUtilidadesApplication.class, "Reinicialização do sistema abortada conforme configuração");
            return;
        }

        if (restartEm > 0) {
            CSPLog.info(CSPUtilidadesApplication.class, "Reiniciando em " + restartEm + "ms...");
            Thread.sleep(restartEm);
        }
        restart(responsavel);

    }

    /**
     * Reinicia o sistema.
     * <b style="color:#f00">O SISTEMA NÃO REINICIA QUANDO USADO PELO
     * NETBEANS!</b>
     *
     * @param responsavel Class - Classe que solicitou o reinciamento
     * @throws java.io.IOException
     * @throws java.net.URISyntaxException
     */
    public static void restart(Class responsavel) throws Exception {

        if (!ENABLE_CALL_RESTART) {
            CSPLog.info(CSPUtilidadesApplication.class, "Reinicialização do sistema abortada conforme configuração");
            return;
        }

        CSPLog.info(CSPUtilidadesApplication.class, "Sistema reiniciando...");

        CodeSource codeSource = responsavel.getProtectionDomain().getCodeSource();

        if (codeSource != null) {

            startJar(new File(codeSource.getLocation().toURI()).getName());

            exit();

        }

    }

    /**
     * Retorna o HashMap do contratante em uso no momento
     *
     * @return HashMap
     * @throws java.lang.Exception
     * @deprecated
     * @see CSPUtilidadesApplication#getInfosAgenteAtivo()
     */
    @Deprecated
    public static HashMap<String, String> getRegistroDbContratante() throws Exception {
        return getInfosRegistroAgente(PadraoClasses.getCodigoContratante());
    }

    /**
     * Retorna um HashMap de informações sobre determinado AGENTE
     *
     * @param agenteCodigo String - Código do agente
     * @return HashMap
     * @throws java.lang.Exception
     * @deprecated
     * @see CSPUtilidadesApplication#getInfosAgenteFromDb(java.lang.String)
     */
    @Deprecated
    public static HashMap<String, String> getInfosRegistroAgente(String agenteCodigo) throws Exception {
        CSPInstrucoesSQLBase conn = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE);
        HashMap<String, String> h = new HashMap<>();
        ArrayList<String> colunas = getColunasNomes("AGENTES", conn);
        ResultSet sele = conn.select("SELECT * FROM AGENTES WHERE AGENTE_CODIGO = ?", agenteCodigo);
        if (sele.next()) {
            for (String coluna : colunas) {
                h.put(coluna, sele.getString(coluna));
            }
        }
        conn.close();
        return h;
    }

    /**
     * Retorna as informações do contratante em ativo no momento
     *
     * @return HashMap
     * @throws java.lang.Exception
     */
    @Deprecated
    public static InfoAgente getInfosAgenteAtivo() throws Exception {
        return getInfosAgenteFromDb(PadraoClasses.getCodigoContratante());
    }

    /**
     * Retorna informações sobre determinado AGENTE
     *
     * @param agenteCodigo String - Código do agente
     * @return
     *
     * @throws java.lang.Exception
     */
    @Deprecated
    public static InfoAgente getInfosAgenteFromDb(String agenteCodigo) throws Exception {
        CSPInstrucoesSQLBase conn = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE);
        InfoAgente r = null;

        ResultSet rs = conn.select("SELECT * FROM AGENTES WHERE AGENTE_CODIGO = ?", agenteCodigo);
        if (rs.next()) {
            r = new InfoAgente(
                    auxGetInfosAgenteFromDb(rs, "AGENTE_CODIGO"),
                    auxGetInfosAgenteFromDb(rs, "ALTERACAO_GOLD"),
                    auxGetInfosAgenteFromDb(rs, "CNPJ"),
                    auxGetInfosAgenteFromDb(rs, "CPF"),
                    auxGetInfosAgenteFromDb(rs, "IDENTIDADE"),
                    auxGetInfosAgenteFromDb(rs, "AGENTE_NOME"),
                    auxGetInfosAgenteFromDb(rs, "FANTASIA"),
                    auxGetInfosAgenteFromDb(rs, "PAIS"),
                    auxGetInfosAgenteFromDb(rs, "ESTADO"),
                    auxGetInfosAgenteFromDb(rs, "MUNICIPIO"),
                    auxGetInfosAgenteFromDb(rs, "CEP"),
                    auxGetInfosAgenteFromDb(rs, "LOGRADOURO"),
                    auxGetInfosAgenteFromDb(rs, "NUMERO"),
                    auxGetInfosAgenteFromDb(rs, "COMPLEMENTO"),
                    auxGetInfosAgenteFromDb(rs, "BAIRRO"),
                    auxGetInfosAgenteFromDb(rs, "IM"),
                    auxGetInfosAgenteFromDb(rs, "IE"),
                    auxGetInfosAgenteFromDb(rs, "CRC"),
                    auxGetInfosAgenteFromDb(rs, "USUARIO"),
                    auxGetInfosAgenteFromDb(rs, "SENHA"),
                    auxGetInfosAgenteFromDb(rs, "APP_BANNER_PRINCIPAL"),
                    auxGetInfosAgenteFromDb(rs, "APP_BARRA_SUPERIOR"),
                    auxGetInfosAgenteFromDb(rs, "APP_APRESENTACAO_BANNER"),
                    auxGetInfosAgenteFromDb(rs, "EMAIL"),
                    auxGetInfosAgenteFromDb(rs, "FONE")
            );
        }
        conn.close();
        return r;
    }

    /**
     * Retorna informações sobre determinado AGENTE
     *
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com o Padrao.fdb
     * @return
     *
     * @throws java.lang.Exception
     */
    @Deprecated
    public static InfoAgente getInfosAgenteAtivoCasaVisual(CSPInstrucoesSQLBase conn) throws Exception {
        InfoAgente r = null;

        ResultSet rs = conn.select("SELECT * FROM EMPRESA");
        if (rs.next()) {
            r = new InfoAgente(
                    auxGetInfosAgenteFromDb(rs, "NUMERO"),
                    null,//auxGetInfosAgenteFromDb(rs, "ALTERACAO_GOLD"),
                    auxGetInfosAgenteFromDb(rs, "CNPJ").replace("/", "").replace("-", "").replace(".", ""),
                    auxGetInfosAgenteFromDb(rs, "CPF_RESPONSAVEL").replace("/", "").replace("-", "").replace(".", ""),
                    null,//  auxGetInfosAgenteFromDb(rs, "IDENTIDADE"),
                    auxGetInfosAgenteFromDb(rs, "RAZAO_SOCIAL"),
                    auxGetInfosAgenteFromDb(rs, "FANTASIA"),
                    null,//auxGetInfosAgenteFromDb(rs, "PAIS"),
                    null,//auxGetInfosAgenteFromDb(rs, "ESTADO"),
                    auxGetInfosAgenteFromDb(rs, "CODIGO_IBGE"),
                    auxGetInfosAgenteFromDb(rs, "CEP").replace("/", "").replace("-", "").replace(".", ""),
                    auxGetInfosAgenteFromDb(rs, "ENDERECO"),
                    auxGetInfosAgenteFromDb(rs, "NUMERO"),
                    auxGetInfosAgenteFromDb(rs, "COMPLEMENTO"),
                    auxGetInfosAgenteFromDb(rs, "NOME_BAIRRO"),
                    auxGetInfosAgenteFromDb(rs, "INSCRICAO_MUNICIPAL"),
                    auxGetInfosAgenteFromDb(rs, "INSCR_ESTADUAL").replace("/", "").replace("-", "").replace(".", ""),
                    null,//auxGetInfosAgenteFromDb(rs, "CRC"),
                    null,//auxGetInfosAgenteFromDb(rs, "USUARIO"),
                    null,//auxGetInfosAgenteFromDb(rs, "SENHA"),
                    null,//auxGetInfosAgenteFromDb(rs, "APP_BANNER_PRINCIPAL"),
                    null,//auxGetInfosAgenteFromDb(rs, "APP_BARRA_SUPERIOR"),
                    null,//auxGetInfosAgenteFromDb(rs, "APP_APRESENTACAO_BANNER"),
                    null,//auxGetInfosAgenteFromDb(rs, "EMAIL"),
                    auxGetInfosAgenteFromDb(rs, "TELEFONE")
            );
        }
        return r;
    }

    @Deprecated
    private static String auxGetInfosAgenteFromDb(ResultSet r, String key) throws SQLException {
        if (hasColumn(r, key)) {
            String d = r.getString(key);
            if (d != null && !d.trim().isEmpty()) {
                return d;
            }
        }

        return null;
    }

    @Deprecated
    public static class InfoAgente {

        public final String id;
        public final String alteracaoGold;
        public final String cnpj;
        public final String cpf;
        public final String identidade;
        public final String nome;
        public final String nomeFantasia;
        public final String paisId;
        public final String estadoId;
        public final String cidadeId;
        public final String cep;
        public final String logradouro;
        public final String numero;
        public final String complemento;
        public final String bairro;
        public final String inscricaoMunicipal;
        public final String inscricaoEstadual;
        public final String crc;
        public final String usuario;
        public final String senha;
        public final String appBannerInicialSrc;
        public final String appIconBarraSuperiorSrc;
        public final String appBannerModoApresentacaoSrc;
        public final String email;
        public final String fone;

        public InfoAgente(String id, String alteracaoGold, String cnpj, String cpf, String identidade, String nome,
                String nomeFantasia, String paisId, String estadoId, String cidadeId, String cep, String logradouro, String numero,
                String complemento, String bairro, String inscricaoMunicipal, String inscricaoEstadual, String crc, String usuario,
                String senha, String appBannerInicialSrc, String appIconBarraSuperiorSrc, String appBannerModoApresentacaoSrc,
                String email, String fone
        ) {
            this.id = id;
            this.alteracaoGold = alteracaoGold;
            this.cnpj = cnpj;
            this.cpf = cpf;
            this.identidade = identidade;
            this.nome = nome;
            this.nomeFantasia = nomeFantasia;
            this.paisId = paisId;
            this.estadoId = estadoId;
            this.cidadeId = cidadeId;
            this.cep = cep;
            this.logradouro = logradouro;
            this.numero = numero;
            this.complemento = complemento;
            this.bairro = bairro;
            this.inscricaoMunicipal = inscricaoMunicipal;
            this.inscricaoEstadual = inscricaoEstadual;
            this.crc = crc;
            this.usuario = usuario;
            this.senha = senha;
            this.appBannerInicialSrc = appBannerInicialSrc;
            this.appIconBarraSuperiorSrc = appIconBarraSuperiorSrc;
            this.appBannerModoApresentacaoSrc = appBannerModoApresentacaoSrc;
            this.email = email;
            this.fone = fone;
        }

        /**
         * Retorna se é um contador
         *
         * @return
         */
        public boolean isContador() {
            return this.crc != null && !this.crc.trim().isEmpty();
        }

        /**
         * Se é uma pessoa física
         *
         * @return
         */
        public boolean isPessoaFisica() {
            return !this.isPessoaJuridica();
        }

        /**
         * Se é uma empresa
         *
         * @return
         */
        public boolean isPessoaJuridica() {
            return this.cnpj != null && !this.cnpj.trim().isEmpty();
        }

        /**
         * Se possui o app habilitado
         *
         * @return
         */
        public boolean isApp() {
            return this.appBannerInicialSrc != null && !this.appBannerInicialSrc.trim().isEmpty();
        }

    }

    /**
     * retorna modelo do contratante ativo.
     *
     * @param conn - conexão com a base na qual o contratante ativo deve ser
     * obtido.
     * @return
     * @throws Exception
     */
    public static ModelContratanteAtivo getContratanteAtivo(CSPInstrucoesSQLBase conn) throws Exception {

        ResultSet select = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("    r.AGENTE_ID, ");
            sb.append("    r.AGENTE_CNPJ, ");
            sb.append("    r.AGENTE_NOME,");
            sb.append("    r.AGENTE_NOME_FANTASIA, ");
            sb.append("    r.INSC_MUNICIPAL, ");
            sb.append("    r.INSC_ESTADUAL, ");
            sb.append("    r.ENDERECO_ID,");
            sb.append("    r.ENDERECO_LOGRADOURO, ");
            sb.append("    r.ENDERECO_NUMERO, ");
            sb.append("    r.ENDERECO_COMPLEMENTO,");
            sb.append("    r.ENDERECO_CEP, ");
            sb.append("    r.REFERENCIA, ");
            sb.append("    r.ENDERECO_BAIRRO_ID,");
            sb.append("    r.ENDERECO_BAIRRO_NOME, ");
            sb.append("    r.ENDERECO_CIDADE_ID, ");
            sb.append("    r.ENDERECO_CIDADE_NOME,");
            sb.append("    r.ENDERECO_ESTADO_ID, ");
            sb.append("    r.ENDERECO_ESTADO_UF, ");
            sb.append("    r.ENDERECO_PAIS_ID,");
            sb.append("    r.ENDERECO_PAIS_NOME ");
            sb.append("FROM ");
            sb.append("    VW_INFOS_CONTRATANTE_ATIVO r");
        });

        if (select.next()) {

            return new ModelContratanteAtivo(
                    new ModelAgenteJuridico(
                            select.getInt("AGENTE_ID"),
                            select.getString("AGENTE_NOME"),
                            select.getString("AGENTE_CNPJ"),
                            select.getString("AGENTE_NOME_FANTASIA"),
                            select.getInt("INSC_ESTADUAL"),
                            select.getString("INSC_MUNICIPAL")
                    ),
                    new ModelEndereco(
                            select.getInt("ENDERECO_ID"),
                            select.getString("ENDERECO_LOGRADOURO"),
                            select.getString("ENDERECO_COMPLEMENTO"),
                            select.getString("REFERENCIA"),
                            select.getString("ENDERECO_NUMERO"),
                            select.getString("ENDERECO_CEP"),
                            0,
                            0, new ModelBairro(
                                    select.getInt("ENDERECO_BAIRRO_ID"),
                                    select.getString("ENDERECO_BAIRRO_NOME"),
                                    new ModelCidade(
                                            select.getInt("ENDERECO_CIDADE_ID"),
                                            select.getString("ENDERECO_CIDADE_NOME"),
                                            null,
                                            new ModelEstado(
                                                    select.getInt("ENDERECO_ESTADO_ID"),
                                                    null,
                                                    select.getString("ENDERECO_ESTADO_UF"),
                                                    new ModelPais(
                                                            select.getInt("ENDERECO_PAIS_ID"),
                                                            select.getString("ENDERECO_PAIS_NOME"))
                                            )
                                    )
                            )
                    ));
        }

        return null;
    }

    /**
     * retorna modelo do contratante ativo.
     *
     * @return
     * @throws Exception
     */
    public static ModelContratanteAtivo getContratanteAtivo() throws Exception {

        CSPInstrucoesSQLBase conn = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE);

        ModelContratanteAtivo model = getContratanteAtivo(conn);

        conn.close();

        return model;
    }

    /**
     * Retorna o PID (Process ID) atual do monitor gold.
     *
     * @param isMgNovo - define de o pid a ser encontrado é do mg antigo ou do
     * mg novo.
     * @return -1 em caso de não encontrar(mg offline, por exemplo)
     * @throws java.lang.Exception
     */
    public static long getPIDMg(boolean isMgNovo) throws Exception {
        return (isMgNovo) ? getPIDOtherJar("monitorgoldnovo.jar") : getPIDOtherJar("monitorgold.jar");
    }

    /**
     * Retorna os PIDs (Process ID) atuais do monitor gold.
     *
     * @param isMgNovo - define de o pid a ser encontrado é do mg antigo ou do
     * mg novo.
     * @return
     * @throws java.lang.Exception
     */
    public static Long[] getPIDsMg(boolean isMgNovo) throws Exception {
        return (isMgNovo) ? getPIDsOthersJar("monitorgoldnovo.jar") : getPIDsOthersJar("monitorgold.jar");
    }
    
    /**
     * Retorna os PIDs (Process ID) atuais do Minerador CABI 2.0.
     *
     * @param isMgNovo - define de o pid a ser encontrado é do mg antigo ou do
     * mg novo.
     * @return
     * @throws java.lang.Exception
     */
    public static Long[] getPIDsMinerador20() throws Exception {
        return getPIDsOthersJar("MonitorCabi20");
    }

    /**
     * Retorna os PIDs (Process ID) atuais do Egula Impressor.
     *
     * @return
     * @throws java.lang.Exception
     */
    public static Long[] getPIDsEgulaImpressor() throws Exception {
        return getPIDsOthersJar("egulaimpressor.jar");
    }

    /**
     * Retorna os PIDs (Process ID) atuais do retaguarda gold.
     *
     * @return
     * @throws java.lang.Exception
     */
    public static Long[] getPIDsRetaguarda() throws Exception {
        return getPIDsOthersJar("principalgold.jar");
    }

    /**
     * Retorna os PIDs (Process ID) atuais do lançador gold.
     *
     * @return
     * @throws java.lang.Exception
     */
    public static Long[] getPIDsLancador() throws Exception {
        return getPIDsOthersJar("casagold.jar");
    }

    /**
     * Retorna os PIDs (Process ID) atuais do Retaguarda FX gold.
     *
     * @return
     * @throws java.lang.Exception
     */
    public static Long[] getPIDsRetaguardaFx() throws Exception {
        return getPIDsOthersJar("casagold_fx");
    }

    /**
     * Retorna o PID (Process ID) atual do casaGold.
     *
     * @return -1 em caso de não encontrar
     * @throws java.lang.Exception
     */
    public static long getPIDRetaguarda() throws Exception {
        long pidOtherJar = getPIDOtherJar("principalgold.jar");

        if (pidOtherJar == -1) {
            return getPIDOtherJar("casagold.jar");
        }

        return pidOtherJar;
    }

    /**
     * Retorna o PID (Process ID) do jar especificado
     *
     * @param name
     * @return -1 em caso de não encontrar
     * @throws java.lang.Exception
     */
    protected static long getPIDOtherJar(String name) throws Exception {

        long myId = -1;

        if (isSoWindows()) {

            for (Object[] d : CSPUtilidadesSO.getListPidAndParametersByName("javaw.exe")) {
//                CSPLog.info(CSPUtilidadesApplication.class,d[1]);
                if (d[1].toString().toLowerCase().contains(name)) {
                    if ((long) d[0] == getPID()) {
                        myId = getPID();
                        continue;
                    }
                    return (long) d[0];
                }
            }
        }

        for (Object[] d : CSPUtilidadesSO.getListPidAndParametersByName(isSoWindows() ? "java.exe" : "java")) {
//            CSPLog.info(CSPUtilidadesApplication.class,d[1]);
            if (d[1].toString().toLowerCase().contains(name)) {
                if ((long) d[0] == getPID()) {
                    myId = getPID();
                    continue;
                }
                return (long) d[0];
            }
        }

        return myId;
    }

    /**
     * Retorna os PIDs (Process ID) do jar especificado
     *
     * @param name
     * @return -1 em caso de não encontrar
     * @throws java.lang.Exception
     */
    protected static Long[] getPIDsOthersJar(String name) throws Exception {

        final LinkedHashSet<Long> r = new LinkedHashSet<>();

        if (isSoWindows()) {

            for (Object[] d : CSPUtilidadesSO.getListPidAndParametersByName("javaw.exe")) {
//                CSPLog.info(CSPUtilidadesApplication.class,d[1]);
                if (d[1].toString().toLowerCase().contains(name.toLowerCase())) {
                    r.add((long) d[0]);
                }
            }
        }

        for (Object[] d : CSPUtilidadesSO.getListPidAndParametersByName(isSoWindows() ? "java.exe" : "java")) {
//            CSPLog.info(CSPUtilidadesApplication.class,d[1]);
            if (d[1].toString().toLowerCase().contains(name.toLowerCase())) {
                r.add((long) d[0]);
            }
        }

        return r.toArray(new Long[r.size()]);
    }

    /**
     * Retorna o caminho absoluto do jar atual
     *
     * @return
     * @throws Exception
     */
    public static String getPathJar() throws Exception {
        CodeSource codeSource = CSPUtilidadesApplication.class.getProtectionDomain().getCodeSource();

        if (codeSource != null) {
            CSPArquivos f = new CSPArquivos(codeSource.getLocation().toURI().getPath());
            if (f.isFile()) {
                return f.getAbsolutePath();
            }
        }
        return null;
    }

    /**
     * Retorna o MD5 do jar atual
     *
     * @return
     * @throws Exception
     */
    public static String getMd5Jar() throws Exception {
        CodeSource codeSource = CSPUtilidadesApplication.class.getProtectionDomain().getCodeSource();

        if (codeSource != null) {
            CSPArquivos f = new CSPArquivos(codeSource.getLocation().toURI().getPath());
            if (f.isFile()) {
                return f.getMd5();
            }
        }
        return null;
    }

    /**
     * Retorna o PID (Process ID) atual da aplicação
     *
     * @return long
     */
    public static long getPID() {
        String processName = java.lang.management.ManagementFactory.getRuntimeMXBean().getName();
        return Long.parseLong(processName.split("@")[0]);
    }

    /**
     * Retorna o MD5 do atual executável
     *
     * @param cls Class - Classe que está solicitando
     * @return
     * @throws URISyntaxException
     * @throws IOException
     */
    public static String currentMD5(Class cls) throws URISyntaxException, IOException {
        CodeSource codeSource = cls.getProtectionDomain().getCodeSource();
        if (codeSource != null) {
            CSPArquivosLocais f = new CSPArquivosLocais(codeSource.getLocation().toURI().getPath());
            if (f.exists() && f.isFile()) {
                return f.md5();
            }
        }
        return null;
    }

}
