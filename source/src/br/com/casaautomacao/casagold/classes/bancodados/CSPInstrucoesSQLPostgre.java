package br.com.casaautomacao.casagold.classes.bancodados;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import static br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase.PATH_PRE_SQL;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.exceptions.OnlyStopException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * O certo a se fazer era refatorar e reestruturar as classes de conexão com o
 * banco feitas pelo Fernando. Apesar de funcionar como objeto, a estrutura é
 * muito truncada/fixa/estática, o que deixa muito ruim a implementação de
 * comunicação com novos bancos. Como não dá tempo de implementar antes da minha
 * saída, fiz a versão gambiarra/rápida.
 */
public class CSPInstrucoesSQLPostgre extends CSPInstrucoesSQLBase {

    /**
     * Inicia InstrucoesSQL com a conexão aberta.
     *
     * @param host Host/Ip do servidor.
     * @param caminho No postgre é só o nome base mesmo, já que o servidor com a
     * base e a conexão são controlados pelo mesmo, então não é necessário
     * passar o caminho.
     * @param usuario Usuário do servidor.
     * @param senha Senha do servidor.
     * @param porta Porta da conexão.
     * @param codificacao Codificação usada.
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLPostgre(String host, String caminho, String usuario, String senha, int porta, String codificacao) throws Exception {
        super(host, caminho, usuario, senha, porta, codificacao);
    }

    /**
     * Inicia InstrucoesSQL com a conexão aberta. Porta e codificação fixa.
     *
     * @param host Host/Ip do servidor.
     * @param caminho No postgre é só o nome base mesmo, já que o servidor com a
     * base e a conexão são controlados pelo mesmo, então não é necessário
     * passar o caminho.
     * @param usuario Usuário do servidor.
     * @param senha Senha do servidor.
     * @throws java.lang.Exception
     */
    public CSPInstrucoesSQLPostgre(String host, String caminho, String usuario, String senha) throws Exception {
        this(host, caminho, usuario, senha, 5432, "UTF8");
    }

    /**
     * Conecta com o banco.
     *
     * @param host Host/Ip do servidor.
     * @param caminho No postgre é só o nome base mesmo, já que o servidor com a
     * base e a conexão são controlados pelo mesmo, então não é necessário
     * passar o caminho.
     * @param user Usuário do servidor.
     * @param pass Senha do servidor.
     * @param porta Porta da conexão.
     * @param encoding Codificação usada.
     * @throws java.lang.Exception
     */
    @Override
    protected void connect(String host, String caminho, String user, String pass, int porta, String encoding) throws Exception {
        final String jdbcDriverConn = "//" + (CSPUtilidadesLangRede.isLocalAddress(host) ? "localhost" : host)
                + ":" + porta + "/" + caminho + "?encoding=" + encoding;
        final String nomeBase = caminho;
        this.confs = null;
        this.conn = null;

        if (ENABLE_GENERATE_SQL_LOGS) {
            new CSPArquivos(PATH_PRE_SQL).mkdirs();
        }

        try {
            Class.forName("org.postgresql.Driver");

            if (AO_CONNECTAR != null) {
                AO_CONNECTAR.run(host, caminho, user, pass, porta, encoding, nomeBase);
            }

            CSPLog.info("try connect: " + nomeBase + ". DB: " + jdbcDriverConn);
            this.conn = DriverManager.getConnection("jdbc:postgresql:" + jdbcDriverConn, user, pass);

            if (this.conn != null) {
                this.confs = new ConfsBase(host, caminho, user, pass, encoding, porta, jdbcDriverConn);
                CSPLog.info("connected: " + nomeBase + ". DB: " + jdbcDriverConn);
                return;
            } else {
                throw new SQLException("error connect!");
            }
        } catch (Exception ex) {
            CSPLog.info("error on try connect: " + nomeBase + ". DB: " + jdbcDriverConn);

            //Solicita que seja disparado o erro 3
            CSPException.register(ex, 3, jdbcDriverConn, this.getBaseUsada().getNumErroConn());

            this.conn = null;
            this.confs = null;
        }

        throw new OnlyStopException();
    }

    /**
     * Realiza o insert ou o update.
     *
     * @param isInsert Insert/update -> true/false
     * @param tabela Tabela para o insert/update.
     * @param dados Dados a serem inseridos/atualizados.
     * @param where Usado no update
     * @param valoresWhere Valores usados nas interrogações ("?") do where.
     * @return
     * @throws java.lang.Exception
     */
    @Override
    public boolean insertComposto(boolean isInsert, String tabela, Map<String, Object> dados, String where, Object... valoresWhere) throws Exception {
        if (dados == null || tabela == null) {
            return false;
        }
        this.insertCompostoPossuiAlteracao = false;

        if (isInsert) {
            if (!insert(tabela, dados)) {
                return false;
            }

            this.insertCompostoPossuiAlteracao = true;
        } else {
            String[] campos = new String[dados.size()];
            Object[] valores = new Object[dados.size()];
            int x = 0;
            if (where == null) {
                where = "";
            }

            for (Map.Entry<String, Object> entry : dados.entrySet()) {
                campos[x] = entry.getKey();
                valores[x] = entry.getValue();
                ++x;
            }

            if (isSemAlteracao(tabela.trim(), campos, valores, " WHERE " + where, valoresWhere)) {
                this.insertCompostoPossuiAlteracao = false;
                return true;
            }

            if (!update(tabela, dados, where, valoresWhere)) {
                return false;
            }

            this.insertCompostoPossuiAlteracao = true;
        }
        return true;
    }

    /**
     * Faz o delete na base
     *
     * @param tabela Tabela de onde será excluído o registro.
     * @param where Where para encontrar o valor a ser deletado.
     * @param valores Valores usados nas interrogações ("?") do where.
     * @return boolean
     * @throws java.lang.Exception
     */
    @Override
    public boolean deleteComposto(String tabela, String where, Object... valores) throws Exception {
        return this.delete(tabela, where, valores);
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
    @Override
    protected void putValuesOnPepareStatement(AuxPreparedStatement aps, Object[] valores) throws SQLException {
        String toLog = this.buildStrLogParamters(aps, valores);
        CSPLog.info(this, toLog);

        aps.setToLog(toLog);

        if (valores != null && valores.length > 0) {
            int pos = 0;
            for (Object arg : valores) {
                pos++;
                if (arg == null) {
                    aps.getPrepared().setNull(pos, aps.getPrepared().getParameterMetaData().getParameterType(pos));
                } else if (arg instanceof Date) {
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
                } else if (arg instanceof Boolean) {
                    aps.getPrepared().setBoolean(pos, (Boolean) arg);
                } else {
                    if (StringUtils.isEmpty((CharSequence) arg)) {
                        aps.getPrepared().setNull(pos, aps.getPrepared().getParameterMetaData().getParameterType(pos));
                    } else {
                        switch (aps.getPrepared().getParameterMetaData().getParameterType(pos)) {
                            case Types.BIGINT:
                                aps.getPrepared().setLong(pos, Long.valueOf(arg.toString()));
                                break;
                            case Types.BIT:
                            case Types.BOOLEAN:
                                aps.getPrepared().setBoolean(pos, Boolean.valueOf(arg.toString()));
                                break;
                            case Types.DECIMAL:
                            case Types.DOUBLE:
                                aps.getPrepared().setDouble(pos, Double.valueOf(arg.toString()));
                                break;
                            case Types.FLOAT:
                                aps.getPrepared().setFloat(pos, Float.valueOf(arg.toString()));
                                break;
                            case Types.TINYINT:
                            case Types.INTEGER:
                            case Types.SMALLINT:
                            case Types.NUMERIC:
                                aps.getPrepared().setInt(pos, Integer.valueOf(arg.toString()));
                                break;
                            default:
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
                                break;
                        }
                    }
                }
            }
        }
    }
}
