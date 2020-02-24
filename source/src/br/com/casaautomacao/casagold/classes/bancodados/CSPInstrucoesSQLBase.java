/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import static br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase.Bases._BASE_CUSTOMIZADA;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.exceptions.OnlyStopException;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime.getTempoCompletoLimpo;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL.hasColumn;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL.resultSetToList;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL.resultSetToMap;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.getColunasNomes;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

/**
 * Classe para manipulação do banco de Dados.
 *
 * O sistema conecta de duas formas, via constante ou via caminho absoluto.
 *
 * @author Fernando Batels <luisfbatels@gmail.com> ele fez a parte errada do
 * fonte. Hue.
 * @date 11/08/2015 - 08:21:51
 */
public class CSPInstrucoesSQLBase {

    /**
     * Pasta de destino dos logs
     */
    protected static final String PATH_PRE_SQL = PATH + "/pre-sql";

    /**
     * Define se esta hablitada api para gerar logs no pre-sql
     */
    protected static boolean ENABLE_GENERATE_SQL_LOGS = true;

    /**
     * Define se esta hablitada api o alteração gold
     */
    private static boolean ENABLE_ALTERACAO_GOLD = true;

    protected boolean insertCompostoPossuiAlteracao = false;

    /**
     * Recebe a conexão principal.
     */
    protected Connection conn = null;

    /**
     * Dispararado antes de toda conexão
     */
    protected static AoConectar AO_CONNECTAR;

    /**
     * Configurações vigentes para a base conectada. Alimentado no #connect()
     */
    protected ConfsBase confs;

    /**
     * Mapa das configurações de base
     */
    private static LinkedHashMap<Bases, ConfsBase> mapConfsBases = new LinkedHashMap<>();

    private final HashMap<String, Boolean> lastTabelaAlteracao = new HashMap<>();

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param fdb Bases - Banco a ser conectado.
     *
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(Bases fdb) throws Exception {
        ConfsBase c = getConfs(fdb);

        connect(c.host, c.path, c.user, c.pass, c.port, c.encoding);

    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param caminho String - Caminho absoluto
     *
     * @param usuario String - Usuário
     *
     * @param senha String - Senha
     *
     * @param porta int - Porta da conexão
     *
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String caminho, String usuario, String senha, int porta) throws Exception {
        String[] t = CSPUtilidadesLang.extraiHostAndPath(caminho);
        this.connect(t[0], t[1], usuario, senha, porta, null);
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param caminho String - Caminho absoluto
     *
     * @param usuario String - Usuário
     *
     * @param senha String - Senha
     *
     * @param porta int - Porta da conexão
     *
     * @param codificacao String - Codificaçao usada
     *
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String caminho, String usuario, String senha, int porta, String codificacao) throws Exception {
        String[] t = CSPUtilidadesLang.extraiHostAndPath(caminho);
        this.connect(t[0], t[1], usuario, senha, porta, codificacao);
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param caminho String - Caminho absoluto
     *
     * @param usuario String - Usuário
     *
     * @param senha String - Senha
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String caminho, String usuario, String senha) throws Exception {
        String[] t = CSPUtilidadesLang.extraiHostAndPath(caminho);
        this.connect(t[0], t[1], usuario, senha, getConfs(_BASE_CUSTOMIZADA).getPort(), getConfs(_BASE_CUSTOMIZADA).getEncoding());
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param host String - Host/Ip
     *
     * @param caminho String - Caminho absoluto
     *
     * @param usuario String - Usuário
     *
     * @param senha String - Senha
     *
     * @param porta int - Porta da conexão
     *
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String host, String caminho, String usuario, String senha, int porta) throws Exception {

        this.connect(host, caminho, usuario, senha, porta, null);
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param host String - Host/Ip
     *
     * @param caminho String - Caminho absoluto
     *
     * @param usuario String - Usuário
     *
     * @param senha String - Senha
     *
     * @param porta int - Porta da conexão
     *
     * @param codificacao String - Codificaçao usada
     *
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String host, String caminho, String usuario, String senha, int porta, String codificacao) throws Exception {
        this.connect(host, caminho, usuario, senha, porta, codificacao);
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param host String - Host/Ip
     *
     * @param caminho String - Caminho absoluto
     *
     * @param usuario String - Usuário
     *
     * @param senha String - Senha
     *
     *
     * @param codificacao String - Codificaçao usada
     *
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String host, String caminho, String usuario, String senha, String codificacao) throws Exception {
        this.connect(host, caminho, usuario, senha, getConfs(_BASE_CUSTOMIZADA).getPort(), codificacao);
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param host String - Host/Ip
     *
     * @param caminho String - Caminho absoluto
     *
     * @param usuario String - Usuário
     *
     * @param senha String - Senha
     *
     *
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String host, String caminho, String usuario, String senha) throws Exception {
        this.connect(host, caminho, usuario, senha, getConfs(_BASE_CUSTOMIZADA).getPort(), null);
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param host String - Host/Ip
     *
     * @param caminho String - Caminho absoluto
     *
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLBase(String host, String caminho) throws Exception {
        this.connect(host, caminho, getConfs(_BASE_CUSTOMIZADA).getUser(), getConfs(_BASE_CUSTOMIZADA).getPass(), getConfs(_BASE_CUSTOMIZADA).getPort(), getConfs(_BASE_CUSTOMIZADA).getEncoding());
    }

    /**
     * Altera o status do auto commit da classe
     *
     * @param status boolean - Novo status
     * @throws java.sql.SQLException
     */
    public void setAutoCommit(boolean status) throws SQLException {
        if (this.conn != null) {
            this.conn.setAutoCommit(status);
        }
    }

    /**
     * Retorna o status do auto commit
     *
     * @return
     * @throws java.sql.SQLException
     */
    public boolean isAutoCommit() throws SQLException {
        if (this.conn != null) {
            return this.conn.getAutoCommit();
        }

        return false;
    }

    /**
     * Executa o commit da conexão
     *
     * @return boolean
     * @throws java.sql.SQLException
     */
    public boolean commit() throws SQLException {
        if (this.conn != null) {
            if (!this.isAutoCommit()) {
                this.conn.commit();
                CSPLog.info("commit. DB:" + getConfs().getSrcDriverJdbc());
            }

            return true;
        }

        return false;
    }

    /**
     * Executa o rollback da conexão
     *
     * @return boolean
     * @throws java.sql.SQLException
     */
    public boolean rollback() throws SQLException {
        if (this.conn != null) {
            if (!this.isAutoCommit()) {
                this.conn.rollback();
                CSPLog.info("rollback. DB:" + getConfs().getSrcDriverJdbc());
            }

            return true;
        }

        return false;
    }

    /**
     * Conecta com o banco
     *
     * @param host String - Host/Ip
     * @param caminho String - Caminho absoluto
     * @param user String - Usuário
     * @param pass String - Senha
     * @param porta int - Porta da conexão
     * @param encoding
     * @throws java.lang.Exception
     */
    protected void connect(String host, String caminho, String user, String pass, int porta, String encoding) throws Exception {
        if (ENABLE_GENERATE_SQL_LOGS) {
            new CSPArquivos(PATH_PRE_SQL).mkdirs();
        }

        caminho = caminho.replaceAll("\\\\", "/");

        //bugfix
        if (!caminho.contains("/var/samba/linuxgold-c")) {
            caminho = caminho.replaceAll("/linuxgold-c/", "/var/samba/linuxgold-c/");
        }

        final String nomeBase = FilenameUtils.getBaseName(caminho);

        if (encoding == null) {
            encoding = "UTF8";
        }

        final String jdbcDriverConn = "//" + (CSPUtilidadesLangRede.isLocalAddress(host) ? "localhost" : host) + ":" + porta + "/" + caminho + "?encoding=" + encoding;

        this.confs = null;

        this.conn = null;

        try {
            Class.forName("org.firebirdsql.jdbc.FBDriver");

            if (AO_CONNECTAR != null) {
                AO_CONNECTAR.run(host, caminho, user, pass, porta, encoding, nomeBase);
            }

            CSPLog.info("try connect: " + nomeBase + ". DB: " + jdbcDriverConn);
            this.conn = DriverManager.getConnection("jdbc:firebirdsql:" + jdbcDriverConn, user, pass);

            if (this.conn != null) {

                this.confs = new ConfsBase(host, caminho, user, pass, encoding, porta, jdbcDriverConn);
                CSPLog.info("connected: " + nomeBase + ". DB: " + jdbcDriverConn);
                return;

            } else {
                throw new SQLException("error connect!");

            }

        } catch (Exception ex) {
            CSPLog.info("error on try connect: " + nomeBase + ". DB: " + jdbcDriverConn);

            CSPException.register(ex, 3, jdbcDriverConn, this.getBaseUsada().numErroConn);//Solicita que seja disparado o erro 3

            this.conn = null;
            this.confs = null;

        }

        throw new OnlyStopException();//Para parar a aplicação

    }

    /**
     * Retorna a conexão
     *
     * @return
     */
    public Connection getConn() {
        return conn;
    }

    /**
     * Retorna todas as informações sobre a base conectada. Disponível somente
     * após a conexão.
     *
     * @return String
     */
    public ConfsBase getConfs() {
        return this.confs;
    }

    /**
     * Fecha a conexão com a base
     *
     * @throws java.sql.SQLException
     */
    public void close() throws SQLException {

//        if (conn != null && this.isOpen()) {
//        if (conn != null && this.isOpen()) {
        conn.close();
//        }

        CSPLog.info("closing connection. DB:" + getConfs().getSrcDriverJdbc());
    }

    /**
     * Retorna o estado da conexão.
     *
     * @return true para conexão aberta e false para fechada.
     * @throws java.sql.SQLException
     */
    public boolean isOpen() throws SQLException {

        if (conn != null) {
            return !conn.isClosed();
        } else {
            return false;
        }

    }

    /**
     * Retorna o objeto DatabaseMetaData da conexão
     *
     * @return DatabaseMetaData
     * @throws java.sql.SQLException
     */
    public DatabaseMetaData getMetaData() throws SQLException {

        return conn.getMetaData();

    }

    /**
     * Usada no insertComposto. Verifica se realmente houve alguma alteração no
     * update passado comparado com as informações que estão na base, para
     * somente assim executar o SQL.
     *
     * @param tabela
     * @param vCampos
     * @param valoreses
     * @param where
     * @param valoresWhere
     * @return
     * @throws java.sql.SQLException
     */
    protected boolean isSemAlteracao(String tabela, String[] vCampos, Object[] valoreses, String where, Object... valoresWhere) throws SQLException {
        StringJoiner campos = new StringJoiner(", ");
        ResultSet result;
        boolean nAchou = true;
        for (String a : vCampos) {
            campos.add(a);
        }
        if (where.trim().endsWith("WHERE")) {
            where = where.replace("WHERE", "");
        }
        result = select("SELECT " + campos.toString() + "  FROM " + tabela + where, valoresWhere);

        if (result.next()) {
            for (int y = 1; y <= result.getMetaData().getColumnCount(); y++) {
                String resultado = "";

                if (valoreses[y - 1] == null) {
                    valoreses[y - 1] = "";
                }

                if (result.getString(y) != null) {
                    resultado = result.getString(y).trim();
                }

                if (!resultado.equals(valoreses[y - 1])) {
                    nAchou = false;
                    break;
                }
            }
        }

        return nAchou;
    }

    /**
     * O ponto de interrogação(?) é um valor que é passado no segundo parâmetro.
     * O Select é passado normalmente por uma string e os valores que serão
     * setados por variáveis ou campos são concatenados em um array na
     * respectiva ordem em que os pontos(?) são informados.
     *
     * EXEMPLO:
     * <ul>
     * <li> ResultSet result = sql.select("SELECT * FROM TABELA WHERE CAMPO =
     * ?", "valor.1"); </li>
     * </ul>
     *
     * @param select
     * @param valor Object - Primeiro
     * @return ResultSet
     * @throws java.sql.SQLException
     */
    public ResultSet select(String select, Object... valor) throws SQLException {
        AuxPreparedStatement ps = new AuxPreparedStatement(conn, select);
        putValuesOnPepareStatement(ps, valor);
        ResultSet r = ps.getPrepared().executeQuery();
        ps.sentToLog(getConfs().getSrcDriverJdbc());

        return r;
    }

    /**
     * O ponto de interrogação(?) é um valor que é passado no segundo parâmetro.
     * O Select é passado normalmente por uma string e os valores que serão
     * setados por variáveis ou campos são concatenados em um array na
     * respectiva ordem em que os pontos(?) são informados.
     *
     *
     * @param select
     * @param valores
     * @return ResultSet
     * @throws java.sql.SQLException
     */
    public ResultSet select(String select, ArrayList<Object> valores) throws SQLException {

        return select(select, valores.toArray());
    }

    /**
     * Seria uma variação para o select. Será preparado uma única vez o sql e
     * todos os values serão executados sobre esse único sql preparado.
     *
     * No caso do select usamos isso para chamar procedures no firebird.
     *
     * O result se não pode ser retornado, pois como é possível enviar mais de
     * um grupo de registros ficaria retornado somente o último resultset.
     *
     * A aplicação desse select é apenas executar procedures e afins que retorne
     * valores
     *
     *
     * @param sql
     * @param valores
     * @throws java.sql.SQLException
     */
    public void executeMultipleRuns(CSPUtilidadesLang.StringBuilderShortcut sql, LinkedHashSet< Object[]> valores) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sql.run(sb);

        this.executeMultipleRuns(sb.toString(), valores);
    }

    /**
     * Seria uma variação para o select. Será preparado uma única vez o sql e
     * todos os values serão executados sobre esse único sql preparado.
     *
     * No caso do select usamos isso para chamar procedures no firebird.
     *
     * O result se não pode ser retornado, pois como é possível enviar mais de
     * um grupo de registros ficaria retornado somente o último resultset.
     *
     * A aplicação desse select é apenas executar procedures e afins que retorne
     * valores
     *
     *
     * @param select
     * @param valores
     * @throws java.sql.SQLException
     */
    public void executeMultipleRuns(String select, LinkedHashSet< Object[]> valores) throws SQLException {

        if (valores.isEmpty()) {
            return;
        }

        boolean oldStatusCommit = isAutoCommit();
        setAutoCommit(false);

        AuxPreparedStatement ps = new AuxPreparedStatement(conn, select);
        int i = 0;

        for (Object[] valor : valores) {

            i++;

            putValuesOnPepareStatement(ps, valor);

            ps.getPrepared().addBatch();

            if (i % 1000 == 0 || i == valores.size()) {
                ps.getPrepared().executeBatch();
                i = 0;
            }

        }

        ps.getPrepared().close();

        commit();

        if (oldStatusCommit) {
            setAutoCommit(true);
        }
    }

    /**
     * Aliais para {@link #select(java.lang.String, java.lang.Object...) }
     *
     * @param sql
     * @param valor
     * @return
     * @throws SQLException
     */
    public ResultSet select(CSPUtilidadesLang.StringBuilderShortcut sql, Object... valor) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sql.run(sb);
        return this.select(sb.toString(), valor);
    }

    /**
     * Aliais para {@link #select(java.lang.String, java.util.ArrayList) }
     *
     * @param sql
     * @param valores
     * @return
     * @throws SQLException
     */
    public ResultSet select(CSPUtilidadesLang.StringBuilderShortcut sql, ArrayList<Object> valores) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sql.run(sb);
        return this.select(sb.toString(), valores.toArray());
    }

    /**
     * Aliais para {@link #select(java.lang.String, java.lang.Object...) }
     *
     * @param Select
     * @param valor Object -
     * @return ResultSet
     * @throws java.sql.SQLException
     */
    public ArrayList<HashMap<String, Object>> selectInMap(String Select, Object... valor) throws SQLException {
        return resultSetToMap(this.select(Select, valor));
    }

    /**
     * Aliais para {@link #select(java.lang.String, java.util.ArrayList) }
     *
     *
     * @param select
     * @param valores
     * @return ResultSet
     * @throws java.sql.SQLException
     */
    public ArrayList<HashMap<String, Object>> selectInMap(String select, ArrayList<Object> valores) throws SQLException {
        return resultSetToMap(this.select(select, valores));
    }

    /**
     * Aliais para {@link #select(br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.StringBuilderShortcut, java.lang.Object...)
     * }
     *
     * @param sql
     * @param valor
     * @return
     * @throws SQLException
     */
    public ArrayList<HashMap<String, Object>> selectInMap(CSPUtilidadesLang.StringBuilderShortcut sql, Object... valor) throws SQLException {
        return resultSetToMap(this.select(sql, valor));
    }

    /**
     * Aliais para {@link #select(br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.StringBuilderShortcut, java.util.ArrayList)
     * }
     *
     * @param sql
     * @param valores
     * @return
     * @throws SQLException
     */
    public ArrayList<HashMap<String, Object>> selectInMap(CSPUtilidadesLang.StringBuilderShortcut sql, ArrayList<Object> valores) throws SQLException {
        return resultSetToMap(this.select(sql, valores));
    }

    /**
     * Aliais para {@link #select(java.lang.String, java.lang.Object...) }
     *
     * @param select
     * @param valor
     * @return ResultSet
     * @throws java.sql.SQLException
     */
    public ArrayList<Object> selectInList(String select, Object... valor) throws SQLException {
        return resultSetToList(this.select(select, valor));
    }

    /**
     * Aliais para {@link #select(br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.StringBuilderShortcut, java.lang.Object...)
     * }
     *
     * @param sql
     * @param valor
     * @return
     * @throws SQLException
     */
    public ArrayList<Object> selectInList(CSPUtilidadesLang.StringBuilderShortcut sql, Object... valor) throws SQLException {
        return resultSetToList(this.select(sql, valor));
    }

    /**
     * Atalho para verificar se existe algum registro que atenda a condição
     *
     * @param tabela
     * @param condicao
     * @param valores
     * @return
     * @throws java.sql.SQLException
     */
    public boolean exists(String tabela, String condicao, Object... valores) throws SQLException {
        return this.count(tabela, condicao, valores) > 0;
    }

    /**
     * Atalho para realizar a função COUNT de uma tabela
     *
     * @param tabela
     * @return int
     * @throws java.sql.SQLException
     */
    public int count(String tabela) throws SQLException {

        return count(tabela, null);
    }

    /**
     * Atalho para realizar a função COUNT de uma tabela
     *
     * @param tabela
     * @param condicao
     * @param valores
     * @return
     * @throws java.sql.SQLException
     */
    public int count(String tabela, String condicao, Object... valores) throws SQLException {
        String sql = "SELECT COUNT(1) AS C FROM " + tabela + " r";

        if (condicao != null) {
            sql += "  WHERE " + condicao;
        }

        int val = 0;
        try (ResultSet r = this.select(sql, valores)) {
            if (r.next()) {
                val = Integer.parseInt(r.getString("C"));
            }
        }

        return val;
    }

    /**
     * Atalho para a função MAX de uma tabela
     *
     * @param tabela
     * @param coluna
     * @param condicao
     * @param valores
     * @return
     * @throws SQLException
     */
    public int max(String tabela, String coluna, String condicao, Object... valores) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(r." + coluna + " AS INTEGER)),0) AS S FROM " + tabela + " r";
        if (condicao != null) {
            sql += "  WHERE " + condicao;
        }

        int val = 0;
        try (ResultSet s = select(sql, valores)) {
            if (s.next()) {
                val = s.getInt("S");
            }
        }

        return val;
    }

    /**
     * Atalho para select's simples que retornem apenas uma linha
     *
     * @param sql
     * @param valor
     * @return
     * @throws SQLException
     */
    public ResultSet selectOneRow(CSPUtilidadesLang.StringBuilderShortcut sql, Object... valor) throws SQLException {
        ResultSet s = select(sql, valor);
        if (s.next()) {
            return s;
        }
        return null;
    }

    /**
     * Atalho para select's simples que retornem apenas uma linha
     *
     * @param select
     * @param valor
     * @return
     * @throws SQLException
     */
    public ResultSet selectOneRow(String select, Object... valor) throws SQLException {
        ResultSet s = select(select, valor);
        if (s.next()) {
            return s;
        }
        return null;
    }

    /**
     * Executa um script SQL
     *
     * @param sql String - Comando SQL a ser executado
     * @param valores
     *
     * @return boolean
     * @throws java.sql.SQLException
     */
    public boolean execute(String sql, Object... valores) throws SQLException {

        final AuxPreparedStatement p = new AuxPreparedStatement(conn, sql);

        putValuesOnPepareStatement(p, valores);

        if (p.getPrepared() != null) {

            p.getPrepared().execute();

            p.sentToLog(getConfs().getSrcDriverJdbc());

            boolean r = p.getPrepared().getUpdateCount() > -1;

            p.getPrepared().close();

            return r;
        }

        return false;
    }

    /**
     * Executa um script SQL
     *
     * @param sql String - Comando SQL a ser executado
     * @param valor
     *
     * @return boolean
     * @throws java.sql.SQLException
     */
    public boolean execute(CSPUtilidadesLang.StringBuilderShortcut sql, Object... valor) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sql.run(sb);
        return this.execute(sb.toString(), valor);
    }

    /**
     * Executa um script SQL
     *
     * @param sql String - Comando SQL a ser executado
     * @param valores
     *
     * @return boolean
     * @throws java.sql.SQLException
     */
    public boolean execute(String sql, ArrayList<Object> valores) throws SQLException {
        return execute(sql, new LinkedHashSet<>(valores));
    }

    /**
     * Executa um script SQL
     *
     * @param sql String - Comando SQL a ser executado
     * @param valores
     *
     * @return boolean
     * @throws java.sql.SQLException
     */
    public boolean execute(String sql, LinkedHashSet<Object> valores) throws SQLException {
        return this.execute(sql, valores.toArray());
    }

    /**
     * Aliais para {@link #select(java.lang.String, java.lang.Object...)}
     *
     * @param sql
     * @param valores
     * @return
     * @throws java.sql.SQLException
     */
    protected ResultSet query(String sql, Object... valores) throws SQLException {
        return this.select(sql, valores);
    }

    /**
     * Aliais para {@link  #query(java.lang.String, java.lang.Object...) }
     *
     * @param sql
     * @param valor
     * @return
     * @throws SQLException
     */
    public ResultSet query(CSPUtilidadesLang.StringBuilderShortcut sql, Object... valor) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sql.run(sb);
        return this.query(sb.toString(), valor);
    }

    /**
     * Joga para o prepareState os valores configurados.
     *
     * O ponto de interrogação(?) é um valor que é passado no segundo parâmetro.
     * O sql é passado normalmente por uma string. Os valores que serão setados
     * por variáveis, ou campos, são concatenados em um array, na respectiva
     * ordem em que os pontos(?) são informados.
     *
     * @param aps
     * @param valores
     * @throws java.sql.SQLException
     */
    protected void putValuesOnPepareStatement(AuxPreparedStatement aps, Object[] valores) throws SQLException {
        String toLog = this.buildStrLogParamters(aps, valores);
        CSPLog.info(this, toLog);

        aps.setToLog(toLog);

        if (valores != null && valores.length > 0) {
            int pos = 0;
            for (Object arg : valores) {
                pos++;
                if (arg == null) {
                    aps.getPrepared().setNull(pos, java.sql.Types.VARCHAR);
                    continue;
                }
                if (arg instanceof Date) {
                    aps.getPrepared().setTimestamp(pos, new Timestamp(((Date) arg).getTime()));
                } else if (arg instanceof Integer) {
                    aps.getPrepared().setInt(pos, (Integer) arg);
                } else if (arg instanceof Long) {
                    aps.getPrepared().setLong(pos, (Long) arg);
                } else if (arg instanceof Double) {
                    aps.getPrepared().setDouble(pos, (Double) arg);
                } else if (arg instanceof Float) {
                    aps.getPrepared().setFloat(pos, (Float) arg);
                } else if (arg instanceof BigDecimal) {
                    aps.getPrepared().setDouble(pos, ((BigDecimal) arg).doubleValue());
                } else {
                    if (StringUtils.isEmpty((CharSequence) arg)) {
                        switch (aps.getPrepared().getParameterMetaData().getParameterType(pos)) {
                            case Types.BIGINT:
                                aps.getPrepared().setLong(pos, 0);
                                break;
                            case Types.DECIMAL:
                            case Types.DOUBLE:
                                aps.getPrepared().setDouble(pos, 0);
                                break;
                            case Types.FLOAT:
                                aps.getPrepared().setFloat(pos, 0);
                                break;
                            case Types.TINYINT:
                            case Types.INTEGER:
                            case Types.SMALLINT:
                            case Types.NUMERIC:
                                aps.getPrepared().setInt(pos, 0);
                                break;
                            default:
                                aps.getPrepared().setString(pos, "");
                                break;
                        }
                    } else {
                        try {
                            String temp = (String) arg;
                            if (temp.matches("[a-z A-Z|[0-9]|[A-zÀ-ú]]+")) {//texto ou números
                                aps.getPrepared().setString(pos, temp);
                                /*============================================*/
                            } else if (temp.matches("^\\d{2}\\.\\d{2}\\.\\d{4}$")) {//dd.MM.yyyy
                                aps.getPrepared().setDate(
                                        pos,
                                        new java.sql.Date(
                                                new SimpleDateFormat("dd.MM.yyyy").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{2}-\\d{2}-\\d{4}$")) {//dd-MM-yyyy
                                aps.getPrepared().setDate(
                                        pos,
                                        new java.sql.Date(
                                                new SimpleDateFormat("dd-MM-yyyy").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{4}\\.\\d{2}\\.\\d{2}$")) {//yyyy.MM.dd
                                aps.getPrepared().setDate(
                                        pos,
                                        new java.sql.Date(
                                                new SimpleDateFormat("yyyy.MM.dd").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{4}-\\d{2}-\\d{2}$")) {//yyyy-MM-dd
                                aps.getPrepared().setDate(
                                        pos,
                                        new java.sql.Date(
                                                new SimpleDateFormat("yyyy-MM-dd").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{2}:\\d{2}:\\d{2}$")) {//HH:mm:ss
                                aps.getPrepared().setTime(
                                        pos,
                                        new Time(
                                                new SimpleDateFormat("HH:mm:ss").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{2}:\\d{2}$")) {//HH:mm
                                aps.getPrepared().setTime(
                                        pos,
                                        new Time(
                                                new SimpleDateFormat("HH:mm").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {//yyyy-MM-dd HH:mm:ss
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}:\\d{2}$")) {//yyyy.MM.dd HH:mm:ss
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("yyyy.MM.dd HH:mm:ss").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}$")) {//dd-MM-yyyy HH:mm:ss
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}$")) {//dd.MM.yyyy HH:mm:ss
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$")) {//yyyy-MM-dd HH:mm
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}$")) {//yyyy.MM.dd HH:mm
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("yyyy.MM.dd HH:mm").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}$")) {//dd-MM-yyyy HH:mm
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("dd-MM-yyyy HH:mm").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}$")) {//dd.MM.yyyy HH:mm
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("dd.MM.yyyy HH:mm").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$")) {//yyyy-MM-dd HH:mm:ss.SSS
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{4}\\.\\d{2}\\.\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$")) {//yyyy.MM.dd HH:mm:ss.SSS
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("yyyy.MM.dd HH:mm:ss.SSS").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$")) {//dd-MM-yyyy HH:mm:ss.SSS
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS").parse(temp).getTime()
                                        )
                                );
                            } else if (temp.matches("^\\d{2}\\.\\d{2}\\.\\d{4} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}$")) {//dd.MM.yyyy HH:mm:ss.SSS
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("dd.MM.yyyy HH:mm:ss.SSS").parse(temp).getTime()
                                        )
                                );
                                /*============================================*/
                            } else if (temp.matches("^[a-zA-Z]{3} [a-zA-Z]{3} \\d{2} \\d{2}:\\d{2}:\\d{2} [a-zA-Z]{3} \\d{4}$")) {//EEE MMM dd HH:mm:ss zzz yyyy
                                aps.getPrepared().setTimestamp(
                                        pos,
                                        new Timestamp(
                                                new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US).parse(temp).getTime()
                                        )
                                );
                            } else {
                                aps.getPrepared().setString(pos, temp);
                            }
                        } catch (ParseException | SQLException e1) {
                            CSPException.register(e1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Monta a string destinada ao log do sql
     *
     * @param aps
     * @param valores
     * @return
     */
    protected String buildStrLogParamters(AuxPreparedStatement aps, Object[] valores) {

        if (valores != null && valores.length > 0) {
            return aps.getSql() + ";--v:" + new JSONArray(valores).toString();
        }

        return aps.getSql() + "";
    }

    /**
     * Realiza o INSERT.
     *
     * @param tabela
     * @param dados HashMap<String, String>
     * @return true caso tenha executado normalmente.
     * @throws java.sql.SQLException
     */
    protected boolean insert(String tabela, Map<String, Object> dados) throws SQLException {

        StringJoiner vCampos = new StringJoiner(",");
        StringJoiner valoresK = new StringJoiner(",");
        ArrayList<Object> valoreses = new ArrayList<>();

        dados.entrySet().stream().map((entry) -> {
            vCampos.add(entry.getKey());
            return entry;
        }).forEach((entry) -> {

            valoreses.add(entry.getValue());
            valoresK.add("?");
        });
        return execute("INSERT INTO " + tabela + " (" + vCampos.toString() + ") VALUES (" + valoresK.toString() + ")", valoreses.toArray());
    }

    /**
     * Delete.
     *
     * @param tabela
     * @param where
     * @param valores
     * @return true caso tenha executado normalmente.
     * @throws java.sql.SQLException
     */
    protected boolean delete(String tabela, String where, Object... valores) throws SQLException {
        StringBuilder vBuffer = new StringBuilder();

        vBuffer.append("DELETE FROM ");
        vBuffer.append(tabela.trim());

        if (where != null) {
            if (!where.trim().isEmpty()) {
                vBuffer.append((char) 13).append((char) 10);
                vBuffer.append("WHERE ");
                vBuffer.append(where.trim());
            }
        }

        return this.execute(vBuffer.toString(), valores);
    }

    /**
     * Da mesma forma que o insertComposto esse método registra em uma outra
     * base as informações antes de excluílas da base original
     *
     * @param tabela String - Tabela de onde será excluído o registro
     *
     * @param where String
     * @param valores
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean deleteComposto(String tabela, String where, Object... valores) throws Exception {
        if (!ENABLE_ALTERACAO_GOLD) {
            return this.delete(tabela, where, valores);
        }
        HashMap<String, Object> dadosBk = new HashMap<>();
        ResultSet dadosAtual = select("SELECT * FROM " + tabela + " WHERE " + where, valores);

        if (dadosAtual.next()) {
            for (int y = 1; y <= dadosAtual.getMetaData().getColumnCount(); y++) {
                dadosBk.put(dadosAtual.getMetaData().getColumnName(y), dadosAtual.getString(y));
            }
        } else {
            return true;//Não tem o que deletar
        }
        if (!this.delete(tabela, where, valores)) {
            return false;
        }
        dadosBk.put("HORA_EXCLUSAO_GOLD", CSPUtilidadesLangDateTime.getHoraObj());
        dadosBk.put("DATA_EXCLUSAO_GOLD", CSPUtilidadesLangDateTime.getDataObj());
        CSPInstrucoesBackupSQL.saveDataInBackup(tabela, this, CSPInstrucoesBackupSQL.Type.EXCLUSAO_GOLD, dadosBk);
        return true;
    }

    /**
     * Update.
     *
     * @param tabela
     * @param dados HashMap<String, String>
     * @param where Where, Ex: "CAMPO = '1' AND CAMPO = '2' etc."
     * @param valoresWhere Object... - Quando usado o ?
     * @return true caso tenha executado normalmente.
     * @throws java.sql.SQLException
     */
    protected boolean update(String tabela, Map<String, Object> dados, String where, Object... valoresWhere) throws SQLException {

        StringBuilder vBuffer = new StringBuilder();
        ArrayList<String> vCampo = new ArrayList<>();
        ArrayList<Object> valores = new ArrayList<>();

        dados.entrySet().stream().map((entry) -> {
            vCampo.add(entry.getKey());
            return entry;
        }).forEach((entry) -> {
            valores.add(entry.getValue());
        });

        vBuffer.append("UPDATE ");
        vBuffer.append(tabela);
        vBuffer.append(" SET ");

        for (int x = 0; x < vCampo.size(); x++) {

            vBuffer.append(vCampo.get(x).trim());
            vBuffer.append(" = ?");

            if (x != vCampo.size() - 1) {
                vBuffer.append(", ");
            }

        }
        if (where != null) {
            if (!where.isEmpty()) {
                vBuffer.append((char) 13).append((char) 10);
                vBuffer.append("WHERE ");
                vBuffer.append(where.trim());
            }
            if (valoresWhere != null) {
                valores.addAll(Arrays.asList(valoresWhere));
            }
        }

        return this.execute(vBuffer.toString(), valores.toArray());
    }

    /**
     * Insert composto. Desenvolvido para auxiliar a gravação de dados junto ao
     * banco de alterações
     *
     *
     * @param isInsert boolan - insert/update -> true/false
     * @param tabela String - Tabela
     * @param dados Map<String, Object> - Dados a serem inseridos/atualizados
     * @return
     * @throws java.lang.Exception
     */
    public boolean insertComposto(boolean isInsert, String tabela, Map<String, Object> dados) throws Exception {
        return insertComposto(isInsert, tabela, dados, null);
    }

    /**
     * Insert composto. Desenvolvido para auxiliar a gravação de dados junto ao
     * banco de alterações
     *
     *
     * @param tabela String - Tabela
     * @param dados Map<String, Object> - Dados a serem inseridos/atualizados
     * @return
     * @throws java.lang.Exception
     */
    public boolean insertComposto(String tabela, Map<String, Object> dados) throws Exception {
        return insertComposto(true, tabela, dados, null);
    }

    /**
     * Retorna se o UPDATE realizado com o insertComposto() foi realizado ou
     * não.
     *
     * É considerado que foi realizado em casos de insert e em casos que existe
     * diferença entre o conteúdo atual na base do conteúdo enviado
     *
     * @return
     */
    public boolean insertCompostoPossuiAlteracao() {
        return insertCompostoPossuiAlteracao;
    }

    /**
     * Insert composto. Desenvolvido para auxiliar a gravação de dados junto ao
     * banco de alterações. Para o sistema gerar o histórico de alterações é
     * preciso que nessa tabela tenha o campo ALTERACAO_GOLD. Sem ele o sistema
     * não aplica o procedimento.
     *
     *
     * @param isInsert boolan - insert/update -> true/false
     * @param tabela String - Tabela
     * @param dados Map<String, String> - Dados a serem inseridos/atualizados
     * @param where String - Usado no update
     * @param valoresWhere Object... - Valores, quando usado "?", no where
     * @return
     * @throws java.lang.Exception
     */
    public boolean insertComposto(boolean isInsert, String tabela, Map<String, Object> dados, String where, Object... valoresWhere) throws Exception {
        if (dados == null || tabela == null) {
            return false;
        }
        this.insertCompostoPossuiAlteracao = false;

        if (isInsert) {
            if (this.existeAlteracaoGold(tabela)) {
                dados.put("ALTERACAO_GOLD", "0");
            }
            if (!insert(tabela, dados)) {
                return false;
            }

            this.insertCompostoPossuiAlteracao = true;
        } else {
            final HashMap<String, Object> dadosAlteracao = new HashMap<>();
            ResultSet result;

            String[] campos = new String[dados.size()];
            Object[] valores = new Object[dados.size()];
            int x = 0;
            String whereAux = "";

            if (where != null) {
                whereAux = " WHERE " + where;
            }

            for (Entry<String, Object> entry : dados.entrySet()) {
                campos[x] = entry.getKey();
                valores[x] = entry.getValue();
                ++x;
            }

            if (this.existeAlteracaoGold(tabela)) {
                if (isSemAlteracao(tabela.trim(), campos, valores, whereAux, valoresWhere)) {
                    this.insertCompostoPossuiAlteracao = false;
                    return true;
                }
                dados.entrySet().stream().forEach((e) -> {
                    dadosAlteracao.put(e.getKey(), e.getValue());
                });

                result = select("SELECT * FROM " + tabela + whereAux, valoresWhere);

                //faz um 'back-up' de todas as informações da tabela antes de alterar o seu registro
                if (result.next()) {
                    for (int y = 1; y <= result.getMetaData().getColumnCount(); y++) {
                        dadosAlteracao.put(result.getMetaData().getColumnName(y), result.getString(y));
                    }
                }

                /**
                 * Aqui ele calcula o ALTERACAO_GOLD e o VERSAO_ALTERACAO_GOLD
                 */
                result = select("SELECT COALESCE(MAX(ALTERACAO_GOLD), 1) FROM " + tabela + whereAux, valoresWhere);
                int alt = 0;

                if (result.next()) {
                    alt = result.getInt(1);
                }

                dadosAlteracao.put("ALTERACAO_GOLD", alt + "");//Pra garantir
                dados.put("ALTERACAO_GOLD", (alt + 1) + "");
//                new SimpleDateFormat("HH:mm:ss").parse(getHora());

                dadosAlteracao.put("HORA_ALTERACAO_GOLD", CSPUtilidadesLangDateTime.getHoraObj());
                dadosAlteracao.put("DATA_ALTERACAO_GOLD", CSPUtilidadesLangDateTime.getDataObj());
            }

            whereAux = "";

            if (where != null) {
                whereAux = where;
            }

            if (!update(tabela, dados, whereAux, valoresWhere)) {
                return false;
            }

            if (ENABLE_ALTERACAO_GOLD && !dadosAlteracao.isEmpty()) {
                CSPInstrucoesBackupSQL.saveDataInBackup(tabela, this, CSPInstrucoesBackupSQL.Type.ALTERACAO_GOLD, dadosAlteracao);
            }

            this.insertCompostoPossuiAlteracao = true;
        }
        return true;
    }

    /**
     * Grava composto. Executa de forma inteligente o insert/update. Quando não
     * existe o registro que atenda a condição estabelecida o mesmo será criado,
     * e logicamente caso já exista será atualizado.
     *
     *
     * @param tabela String - Tabela
     * @param dados Map<String, String> - Dados a serem inseridos/atualizados
     * @param where String - Usado no update
     * @param valoresWhere Object... - Valores, quando usado "?", no where
     * @return
     * @throws java.lang.Exception
     */
    public boolean gravaComposto(String tabela, Map<String, Object> dados, String where, Object... valoresWhere) throws Exception {
        return this.insertComposto(!this.exists(tabela, where, valoresWhere), tabela, dados, where, valoresWhere);

    }

    /**
     * Grava composto, variante que somente gravará se o registro for novo.
     * Executa de forma inteligente o insert/update. Quando não existe o
     * registro que atenda a condição estabelecida o mesmo será criado, e
     * logicamente caso já exista será atualizado.
     *
     *
     * @param tabela String - Tabela
     * @param dados Map<String, String> - Dados a serem inseridos/atualizados
     * @param where String - Usado no update
     * @param valoresWhere Object... - Valores, quando usado "?", no where
     * @return
     * @throws java.lang.Exception
     */
    public boolean gravaCompostoSomenteInsert(String tabela, Map<String, Object> dados, String where, Object... valoresWhere) throws Exception {
        if (!this.exists(tabela, where, valoresWhere)) {
            return this.insertComposto(true, tabela, dados, where, valoresWhere);
        }

        return true;
    }

    /**
     *
     * Grava composto super. Executa de forma inteligente o insert/update, mas
     * fornecendo uma interface para interagir com os valores antigos.
     * <b style="color:#ff0000">Destinada apenas para registros simples (updates
     * que afetem apenas uma linha)!</b> Quando não existe o registro que atenda
     * a condição estabelecida o mesmo será criado, e logicamente caso já exista
     * será atualizado.
     *
     *
     * @param tabela String - Tabela
     * @param dados LinkedHashMap<String, GravaCompostoColumn> - Dados a serem
     * inseridos/atualizados
     * @param where String - Usado no update
     * @param valoresWhere Object... - Valores, quando usado "?", no where
     *
     * @throws java.lang.Exception
     */
    public void gravaCompostoSuper(String tabela, LinkedHashMap<String, GravaCompostoColumn> dados, String where, Object... valoresWhere) throws Exception {

        final Map<String, Object> data = new HashMap<>();

        final boolean isInsert = !this.exists(tabela, where, valoresWhere);

        if (isInsert) {

            for (Entry<String, GravaCompostoColumn> e : dados.entrySet()) {
                data.put(e.getKey(), e.getValue().run(isInsert, null));
            }

            this.insertComposto(isInsert, tabela, data, where, valoresWhere);

        } else {
            String sql = "SELECT * FROM " + tabela + " r";
            if (where != null) {
                sql += "  WHERE " + where;
            }

            final ResultSet select = this.select(sql, valoresWhere);

            if (select.next()) {
                data.clear();

                for (Entry<String, GravaCompostoColumn> e : dados.entrySet()) {
                    data.put(e.getKey(), e.getValue().run(isInsert, select));
                }

                this.insertComposto(isInsert, tabela, data, where, valoresWhere);
            }
        }

    }

    public interface GravaCompostoColumn {

        /**
         * Destinada a realizar o tratamento da coluna
         *
         * @param isInsert boolean - se o valor vai ser um insert ou nao
         * @param oldData ResultSet - ResultSet com os antigos valores. Será
         * null em caso de insert
         * @return
         * @throws Exception
         */
        public Object run(boolean isInsert, ResultSet oldData) throws Exception;
    }

    /**
     * Retorna a base em uso
     *
     * @return String
     */
    public Bases getBaseUsada() {
        if (getConfs() != null) {

            for (Bases c : Bases.values()) {
                if (getConfs().path.equals(getConfs(c).path)) {
                    return c;
                }
            }
        }

        return _BASE_CUSTOMIZADA;
    }

    private boolean existeAlteracaoGold(String tabela) throws SQLException {
        if (!ENABLE_ALTERACAO_GOLD) {
            return false;
        }
        if (this.lastTabelaAlteracao.get(tabela) == null) {
            for (String n : getColunasNomes(tabela, this)) {
                if (n.equalsIgnoreCase("ALTERACAO_GOLD")) { //Se tiver o ALTERACAO_GOLD
                    this.lastTabelaAlteracao.put(tabela, true);
                    return true;
                }

            }
            this.lastTabelaAlteracao.put(tabela, false);
            return false;
        } else {
            return this.lastTabelaAlteracao.get(tabela);
        }
    }

    /**
     * Retorta a codificação default da base
     *
     * @return
     * @throws java.sql.SQLException
     */
    public String getDefaultCharacter() throws SQLException {
        ResultSet select = this.select("SELECT RDB$CHARACTER_SET_NAME AS ECO FROM RDB$DATABASE");

        if (select.next()) {
            return select.getString("ECO");
        }

        return "";
    }

    /**
     * Retorna as configurações vigentes para a base
     *
     * @param base Bases - A base que deseja retornar a informação.
     * @return ConfsBase
     */
    public static ConfsBase getConfs(Bases base) {
        if (!mapConfsBases.containsKey(base)) {
            return null;
        }

        return mapConfsBases.get(base);
    }

    /**
     * Registra informações de conexão com a base. Quando não exsitente passar
     * nullo
     *
     * @param base Bases - A base que deseja registrar as informações.
     * @param host String - Host de conexão da base.
     * @param caminho String - Caminho ABSOLUTO da base.
     * @param porta int - Porta de conexão com a base.
     *
     */
    public static void setInfosBase(Bases base, String host, String caminho, int porta) {
        setInfosBase(base, host, caminho, null, null, porta, null);
    }

    /**
     * Registra informações de conexão com a base. Quando não exsitente passar
     * nullo
     *
     * @param base Bases - A base que deseja registrar as informações.
     * @param host String - Host de conexão da base.
     * @param caminho String - Caminho ABSOLUTO da base.
     * @param user String - Usuário a ser usado na base
     * @param pass String - Senha do usuário a ser usada na base
     * @param porta int - Porta de conexão com a base.
     * @param encoding String - Codificação para a base
     *
     */
    public static void setInfosBase(Bases base, String host, String caminho, String user, String pass, int porta, String encoding) {
        if (caminho == null) {
            caminho = "";
        }

        caminho = caminho.replaceAll("\\\\", "/");

        if (user == null) {
            user = base.userDefault;
        }

        if (pass == null) {
            pass = base.passDefault;
        }

        if (porta == -1) {
            porta = base.portDefault;
        }

        if (encoding == null) {
            encoding = base.encodingDefault;
        }

        mapConfsBases.put(base, new ConfsBase(host, caminho, user, pass, encoding, porta));

    }

    /**
     * Seta variavel AO_CONNECTAR
     *
     * @param AO_CONNECTAR
     */
    public static void setEventoAoConectar(AoConectar AO_CONNECTAR) {
        CSPInstrucoesSQLBase.AO_CONNECTAR = AO_CONNECTAR;
    }

    /**
     * Enum das bases do sistema com suas respectivas confiturações default
     */
    public enum Bases {

        BASE_CONFIG(3),
        BASE_APP(74),
        BASE_CONTRATANTE(3),
        BASE_CASA_VISUAL(133, "ISO8859_1"),
        BASE_BICMG(3),
        BASE_BICAPP(3),
        BASE_BISERVIDORES(3),
        BASE_INTEGRACAO(3),
        BASE_ADM(3),
        _BASE_CUSTOMIZADA(3),;
        private final int numErroConn;
        private final String encodingDefault;
        private final String userDefault;
        private final String passDefault;
        private final int portDefault;

        private Bases(int numErroConn) {
            this(numErroConn, "UTF8", "SYSDBA", "masterkey", 3050);
        }

        private Bases(int numErroConn, String encoding) {
            this(numErroConn, encoding, "SYSDBA", "masterkey", 3050);
        }

        private Bases(int numErroConn, String user, String pass) {
            this(numErroConn, "UTF8", user, pass, 3050);
        }

        private Bases(int numErroConn, String encoding, String user, String pass, int port) {
            this.numErroConn = numErroConn;
            this.encodingDefault = encoding;
            this.userDefault = user;
            this.passDefault = pass;
            this.portDefault = port;
        }

        public int getNumErroConn() {
            return numErroConn;
        }
    }

    /**
     * Retorna se existe a coluna no ResultSet
     *
     * @param rs ResultSet
     * @param columnName String
     * @return
     * @throws SQLException
     */
    public boolean hasColumnSimple(ResultSet rs, String columnName) throws SQLException {
        return hasColumn(rs, columnName);
    }

    protected class AuxPreparedStatement {

        public final PreparedStatement prepared;
        private String toLog;
        private final String sql;

        public AuxPreparedStatement(Connection conn, String sql) throws SQLException {

            sql = sql.replaceAll("\\s+", " ").trim();
            sql = sql.replaceAll(LINE_SEPARATOR, "").trim();
            this.sql = sql;
            try {
                this.prepared = conn.prepareStatement(sql);

                this.prepared.closeOnCompletion();
            } catch (SQLException e) {

                CSPLog.error(AuxPreparedStatement.class, sql);

                throw new SQLException(e);
            }
        }

        public String getSql() {
            return sql;
        }

        public PreparedStatement getPrepared() {
            return prepared;
        }

        public void setToLog(String toLog) {
            this.toLog = toLog;
        }

        public void sentToLog(String pathToBase) {
            if (!ENABLE_GENERATE_SQL_LOGS) {
                return;
            }

            try {
                if (this.toLog != null && !this.toLog.trim().isEmpty() && !this.toLog.trim().toLowerCase().startsWith("select")) {
                    /**
                     * Somente se foi 'preparado corretamente' que mandamos para
                     * o pre-sql
                     */
                    CSPArquivosLocais tmp = new CSPArquivosLocais(PATH_PRE_SQL);
                    tmp.setName(getTempoCompletoLimpo() + ".sql");
                    tmp.setContent(this.toLog + ";--" + pathToBase);
                    tmp = null;//Para remover da memória
                }
            } catch (Exception ex) {
                CSPException.register(ex);
            }
        }

    }

    /**
     * Insert especifico para a base "Padrao.fdb"
     *
     * @param tabela
     * @param dados HashMap<String, String>
     * @return true caso tenha executado normalmente.
     * @throws java.sql.SQLException
     */
    public boolean insertOnPadrao(String tabela, Map<String, Object> dados) throws SQLException {
        if (Bases.BASE_CASA_VISUAL == getBaseUsada()) {
            return this.insert(tabela, dados);
        } else {
            throw new SQLException("Usado somente para conexao com Padrao.fdb");
        }
    }

    /**
     * Update especifico para a base "Padrao.fdb"
     *
     * @param tabela
     * @param dados HashMap<String, String>
     * @param where Where, Ex: "CAMPO = '1' AND CAMPO = '2' etc."
     * @param valoresWhere Object... - Quando usado o ?
     * @return true caso tenha executado normalmente.
     * @throws java.sql.SQLException
     */
    public boolean updateOnPadrao(String tabela, Map<String, Object> dados, String where, Object... valoresWhere) throws SQLException {
        if (Bases.BASE_CASA_VISUAL == getBaseUsada()) {
            return this.update(tabela, dados, where, valoresWhere);
        } else {
            throw new SQLException("Usado somente para conexao com Padrao.fdb");
        }
    }

    /**
     * Configura a geração das informações para o alteração gold
     *
     * @param e boolean
     */
    public static void setEnableAlteracaoGold(boolean e) {
        CSPInstrucoesSQLBase.ENABLE_ALTERACAO_GOLD = e;
    }

    /**
     * Configura a geração dos logs de sql para backup
     *
     * @param e boolean
     */
    public static void setEnableGenerateSqlLogs(boolean e) {
        CSPInstrucoesSQLBase.ENABLE_GENERATE_SQL_LOGS = e;
    }

    /**
     * Interface para disparar evento antes de toda conexão.
     */
    public interface AoConectar {

        public void run(String host, String caminho, String user, String pass, int porta, String encoding, String nomeIdentificacao) throws Exception;
    }

    /**
     * Configurações sobre a base conectada
     */
    public static class ConfsBase {

        private final String host;
        private final String path;
        private final String user;
        private final String pass;
        private final String encoding;
        private final int port;
        private final String srcDriverJdbc;

        protected ConfsBase(String host, String path, String user, String pass, String encoding, int port, String srcDriverJdbc) {
            this.host = host;
            this.path = path;
            this.user = user;
            this.pass = pass;
            this.encoding = encoding;
            this.port = port;
            this.srcDriverJdbc = srcDriverJdbc;
        }

        public ConfsBase(String host, String path, String user, String pass, String encoding, int port) {
            this(host, path, user, pass, encoding, port, null);
        }

        public String getHost() {
            return host;
        }

        public String getPath() {
            return path;
        }

        public String getUser() {
            return user;
        }

        public String getPass() {
            return pass;
        }

        public String getEncoding() {
            return encoding;
        }

        public int getPort() {
            return port;
        }

        public String getSrcDriverJdbc() {
            return srcDriverJdbc;
        }
    }
}
