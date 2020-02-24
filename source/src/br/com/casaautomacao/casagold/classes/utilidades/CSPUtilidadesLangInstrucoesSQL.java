/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import org.firebirdsql.management.FBManager;
import org.json.JSONObject;

/**
 * Métodos de auxilio para linguagem no contexto de banco de dados
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @author Matheus Felipe Amelco <producao5@casaautomacao.com.br>
 * @date 27/10/2016 - 17:13:17
 */
public abstract class CSPUtilidadesLangInstrucoesSQL extends CSPUtilidadesLang {

    /**
     * Retorna se existe a coluna no ResultSet
     *
     * @param rs ResultSet - Select das colunas da tabela.
     * @param columnName String - Nome da coluna a ser verificada.
     * @return
     * @throws SQLException
     */
    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnLabel(x))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 'Converte' um resultSet para uma lista de hashMap. Evitar grandes
     * consultas!
     *
     * @param rs ResultSet
     * @return
     * @throws SQLException
     */
    public static ArrayList<HashMap<String, Object>> resultSetToMap(ResultSet rs) throws SQLException {

        final ArrayList<HashMap<String, Object>> r = new ArrayList<>();
        final ResultSetMetaData rsmd = rs.getMetaData();
        final int columns = rsmd.getColumnCount();

        while (rs.next()) {

            final HashMap<String, Object> tmp = new HashMap<>();
            tmp.clear();

            for (int x = 1; x <= columns; x++) {
                switch (rsmd.getColumnType(x)) {
                    case java.sql.Types.BIGINT:
                    case java.sql.Types.INTEGER:
                    case java.sql.Types.TINYINT:

                        tmp.put(rsmd.getColumnLabel(x), rs.getInt(rsmd.getColumnLabel(x)));
                        break;
                    default:
                        tmp.put(rsmd.getColumnLabel(x), rs.getObject(rsmd.getColumnLabel(x)));
                        break;
                }

            }

            r.add(tmp);

        }

        return r;
    }

    /**
     * 'Converte' um resultSet para uma lista. É utilizado somente a primeira
     * posição. Evitar grandes consultas!
     *
     * @param rs ResultSet
     * @return
     * @throws SQLException
     */
    public static ArrayList<Object> resultSetToList(ResultSet rs) throws SQLException {

        final ArrayList<Object> r = new ArrayList<>();
        final ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {

            switch (rsmd.getColumnType(1)) {
                case java.sql.Types.VARCHAR:
                case java.sql.Types.CHAR:
                    r.add(rs.getString(1));
                    break;
                case java.sql.Types.BIGINT:
                case java.sql.Types.INTEGER:
                case java.sql.Types.TINYINT:
                    r.add(rs.getInt(1));
                    break;
                case java.sql.Types.DATE:
                case java.sql.Types.TIMESTAMP:
                    r.add(rs.getDate(1));
                    break;
                default:
                    r.add(rs.getObject(1));
                    break;
            }

        }

        return r;
    }

    /**
     * Retorna o PID de todos os processos conectado a base.
     *
     * @param conn CSPInstrucoesSQLBase- Base a ser verificada.
     * @return
     * @throws SQLException
     */
    public static long[] getPidsConnectedIntoBase(CSPInstrucoesSQLBase conn) throws SQLException {
        LinkedHashSet<Long> temp = new LinkedHashSet<>();

        ResultSet result = conn.select("SELECT MON$ATTACHMENTS.MON$REMOTE_PID AS PID FROM MON$ATTACHMENTS WHERE MON$ATTACHMENT_ID <> CURRENT_CONNECTION");

        while (result.next()) {
            long res = result.getLong((1));

            if (res != 0) {
                temp.add(res);
            }
        }

        long[] pids = new long[temp.size()];

        int i = 0;

        for (long q : temp) {
            pids[i++] = q;
        }

        return pids;
    }

    /**
     * Finaliza todos os processos que estiverem utilizando a base! Se não
     * conseguir finalizar o processo, desfaz a conexão dele.
     *
     * @param conn CSPInstrucoesSQLBase- Base a ser verificada.
     * @throws java.lang.Exception
     */
    public static void mataProcessosBase(CSPInstrucoesSQLBase conn) throws Exception {
        LinkedHashSet<String> att = new LinkedHashSet<>();

        ResultSet result = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("     MON$ATTACHMENTS.MON$ATTACHMENT_ID AS ATTACHMENT_ID,");
            sb.append("     MON$ATTACHMENTS.MON$REMOTE_PID AS PID,");
            sb.append("     MON$ATTACHMENTS.MON$REMOTE_PROCESS AS PROCESS");
            sb.append(" FROM ");
            sb.append("     MON$ATTACHMENTS");
            sb.append(" WHERE ");
            sb.append("     MON$ATTACHMENT_ID <> CURRENT_CONNECTION");
        });

        while (result.next()) {
            long pid = result.getLong("PID");
            String attachment = result.getString("ATTACHMENT_ID");
            String process = result.getString("PROCESS");

            if (pid != 0) {
                CSPUtilidadesSO.kill(pid);

            } else {
                att.add(attachment);
            }

            if (process != null) {
                CSPLog.info(CSPInstrucoesSQLBase.class.getName() + "> finalizando processo " + process);
            }
        }

        for (String at : att) {
            conn.execute("DELETE FROM MON$ATTACHMENTS WHERE MON$ATTACHMENT_ID = ?", at);
            CSPLog.info(CSPInstrucoesSQLBase.class.getName() + "> finalizando processo. attachment-id " + at);
        }
    }

    /**
     * Retorna se a tabela existe no banco.
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com o banco de dados.
     * @param tabela String - Tabela a ser verificada.
     * @return
     * @throws SQLException
     */
    public static boolean hasTabela(CSPInstrucoesSQLBase conn, String tabela) throws SQLException {
        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT DISTINCT");
            sb.append("     TRIM(RDB$RELATION_NAME) ");
            sb.append("FROM ");
            sb.append("     RDB$RELATION_FIELDS ");
            sb.append("WHERE ");
            sb.append("     RDB$SYSTEM_FLAG = 0 ");
            sb.append("     AND RDB$VIEW_CONTEXT IS NULL ");
            sb.append("     AND RDB$RELATION_NAME = ? ");
        }, tabela);

        return rs.next();
    }

    /**
     * Retorna se a View existe no banco.
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com o banco de dados.
     * @param view String - View a ser verificada.
     * @return
     * @throws SQLException
     */
    public static boolean hasView(CSPInstrucoesSQLBase conn, String view) throws SQLException {
        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT DISTINCT  ");
            sb.append("     RDB$VIEW_NAME ");
            sb.append("FROM ");
            sb.append("     RDB$VIEW_RELATIONS ");
            sb.append("WHERE ");
            sb.append("     RDB$VIEW_NAME = ? ");
        }, view);

        return rs.next();
    }

    /**
     * Retorna se a Procedure existe no banco.
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com o banco de dados.
     * @param procedure String - Procedure a ser verificada.
     * @return
     * @throws SQLException
     */
    public static boolean hasProcedure(CSPInstrucoesSQLBase conn, String procedure) throws SQLException {
        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("     RDB$PROCEDURE_NAME ");
            sb.append("FROM ");
            sb.append("     RDB$PROCEDURES ");
            sb.append("WHERE ");
            sb.append("     RDB$PROCEDURE_NAME = ? ");
        }, procedure);

        return rs.next();
    }

    /**
     * Auxilia no processo de leitura de informações de um formulário que
     * trabalha no modo json
     *
     * @param conn
     * @param tabela String
     * @param campo String
     * @param run AuxReadInfosJsonCampo
     * @throws Exception
     */
    public static void auxReadInfosJsonCampo(CSPInstrucoesSQLBase conn, String tabela, String campo, AuxReadInfosJsonCampo run) throws Exception {
        final ResultSet select = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append(campo);
            sb.append(" FROM ");
            sb.append(tabela);
            sb.append(" WHERE ");
            sb.append(campo);
            sb.append(" is not null AND ");
            sb.append(campo);
            sb.append(" <> '' AND ");
            sb.append(campo);
            sb.append(" <> '{}' ");
        });
        if (select.next()) {
            JSONObject a = new JSONObject(select.getString(campo));
            run.run(a);
        }
    }

    /**
     * Centraliza/Efetua o processo de execução do ddl/dml na base
     *
     * @param conn CSPInstrucoesSQLBase
     * @param arquivoSql CSPArquivos - arquivo contendo os Sql's
     * @throws java.lang.Exception
     */
    public static void executaSqlOnBase(CSPInstrucoesSQLBase conn, CSPArquivos arquivoSql) throws Exception {
        executaSqlOnBase(conn, arquivoSql.getContent());
    }

    /**
     * Centraliza/Efetua o processo de execução do ddl/dml na base
     *
     * @param conn CSPInstrucoesSQLBase
     * @param sql String - Sql
     * @throws java.lang.Exception
     */
    public static void executaSqlOnBase(CSPInstrucoesSQLBase conn, String sql) throws Exception {

        for (String s : sql.split("\n")) {
            if (s != null && !s.trim().isEmpty()) {

                conn.execute(s);
            }
        }
    }

    /**
     * Cria um banco limpo.
     *
     * @param host String - Host/Ip
     * @param caminho String - Caminho absoluto
     * @param user String - Usuário
     * @param pass String - Senha
     * @param porta int - Porta da conexão
     * @param encoding String
     * @throws java.lang.Exception
     */
    public static void criaBanco(String host, String caminho, String user, String pass, int porta, String encoding) throws Exception {
        FBManager fdb = new FBManager();
        fdb.setServer(host);

        fdb.setPort(porta);
        
      
        caminho = caminho.replaceAll("\\\\", "/");
        
        //bugfix
        if (!caminho.contains("/var/samba/linuxgold-c")) {
            caminho = caminho.replaceAll("/linuxgold-c/", "/var/samba/linuxgold-c/");
        }

        caminho = caminho.split("\\?encoding=")[0];

        fdb.start();
        fdb.createDatabase(caminho, user, pass);
        fdb.stop();
        if (encoding != null) {
            //Só usa essa conexão para definir o encoding mesmo
            CSPInstrucoesSQLBase conn = new CSPInstrucoesSQLBase(host, caminho, user, pass, porta, encoding);
            conn.execute("UPDATE rdb$database SET rdb$character_set_name='" + encoding + "'");
            conn.close();
        }

        CSPLog.info("new db created. src: //" + host + ":" + porta + "/" + caminho + "?encoding=" + encoding);

    }

    /**
     * Cria um banco limpo caso o mesmo não existir e já retorna uma conexão
     * válida com o mesmo
     *
     * @param host String - Host/Ip
     * @param caminho String - Caminho absoluto
     * @param user String - Usuário
     * @param pass String - Senha
     * @param porta int - Porta da conexão
     * @param encoding String
     * @return
     * @throws java.lang.Exception
     */
    public static CSPInstrucoesSQLBase criaBancoSeNaoExistir(String host, String caminho, String user, String pass, int porta, String encoding) throws Exception {

        try {
            return new CSPInstrucoesSQLBase(host, caminho, user, pass, porta, encoding);

        } catch (Exception ex) {
            criaBanco(host, caminho, user, pass, porta, encoding);
//            CSPException.register(ex);
        }

        return new CSPInstrucoesSQLBase(host, caminho, user, pass, porta, encoding);
    }

    /**
     * Interface Auxiliar para leitura de campos no formato JSON salvos na base.
     */
    public interface AuxReadInfosJsonCampo {

        /**
         * Run da interface.
         *
         * @param data
         * @throws Exception
         */
        public void run(JSONObject data) throws Exception;
    }

}
