/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.sincronizacao;

import br.com.casaautomacao.casagold.classes.FrmModuloPaiBase;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.modelos.ModelColunaTabela;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL;
import java.security.InvalidAlgorithmParameterException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.StringJoiner;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Sincronização de bases
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 20/12/2016 - 17:32:07
 */
public class CSPSincronizacaoDatabase extends CSPSincronizacao {

    final private CSPInstrucoesSQLBase conn;
    final private CSPInstrucoesSQLBase connSync;
    final private HashMap<String, ArrayList<ModelColunaTabela>> mapTabelaColunas = new HashMap<>();
    final private boolean cuidaRanges;

    /**
     * <h3>Sincronização de bases</h3>
     *
     * A base a ser sincronizada <b>PRECISA</b> da estrutura exemplificada no
     * arquivo
     * br.com.casaautomacao.casagold.classes.sincronizacao.estrutura-db-sync.sql
     *
     * @param idSync String - ID para isolar a sincronização
     * @param remoteHost String - Host/ip do outro servidor com que a base será
     * sincronizada
     * @param isMaster boolean - Se é o host master da sincronizaçao
     * @param conn CSPInstrucoesSQLBase - Conexão válida com a base a ser
     * sincronizada
     *
     * @param cuidaRanges boolean - Determina se essa sincronização deve tratar
     * do ranges
     */
    public CSPSincronizacaoDatabase(String idSync, String remoteHost, boolean isMaster, String hostPathBase, String passBase, String hostPathBaseSync, String passBaseSync, boolean cuidaRanges) throws Exception {

        super(idSync, remoteHost, isMaster);

        this.conn = new CSPInstrucoesSQLBase(hostPathBase, "SYNC", passBase);
        this.connSync = new CSPInstrucoesSQLBase(hostPathBaseSync, "SYNC", passBaseSync);
        this.cuidaRanges = cuidaRanges;
    }

    /**
     * Inicia o processo
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        super.start();

        this.createStructureInDbSync();

        this.setOnReceiveInfo((Object... info) -> {

            switch (info[0].toString()) {
                case "request-sync":
                    this.trataRequestSync(Long.valueOf(info[1].toString()));
                    return true;
                case "data-to-sync":
                    this.trataDataJsonReceived(new JSONArray(info[1].toString()));
                    return true;
            }

            return false;

        });

        FrmModuloPaiBase.simpleThread(() -> {

            /**
             * Dispara, conforme o intervalo, o processo de sincronização
             */
            this.requestSyncForOtherHost();

        }, this.getIntervalSync());

    }

    /**
     * Trata a solicitação de requisição de sincronização
     *
     * @param lastId long
     * @throws Exception
     */
    private void trataRequestSync(long lastId) throws Exception {
        final JSONArray jsonToSync = this.getJsonToSync(lastId);
        if (jsonToSync.length() > 0) {
            this.sendInfo("data-to-sync", jsonToSync.toString());
        }

    }

    /**
     * Efetua todo o tratamento sobre o json de dados recebido. Esse tratamento
     * consiste no processo de replicar na base e reagir sobre os conflitos
     *
     * @param arr JSONArray
     */
    private synchronized void trataDataJsonReceived(JSONArray arr) throws Exception {

        for (Object t : arr) {

            final JSONObject row = (JSONObject) t;
            final String tab = row.getString("_TA");
            final long id = row.getLong("_ID");

            switch (row.getInt("_TI")) {
                case 0:
                    this.trataInsertReceived(id, tab, castJsonObjectToObjectAux(row));
                    break;
                case 1:
                    this.trataUpdateReceived(id, tab, castJsonObjectToObjectAux(row), row.getString("_W"));
                    break;
                case 2:
                    this.trataDeleteReceived(id, tab, row.getString("_W"));
                    break;
                default:
                    throw new InvalidAlgorithmParameterException("tipo-invalido");
            }

        }

    }

    /**
     * Efetua o tratamento para o insert
     *
     * @param table String - Tabela
     * @param data LinkedHashMap
     * @throws Exception
     */
    private void trataInsertReceived(long id, String table, LinkedHashMap<String, Object> data) throws Exception {

        final StringJoiner cols = new StringJoiner(",");
        final StringJoiner colsVal = new StringJoiner(",");
        final LinkedHashSet<Object> vals = new LinkedHashSet<>();

        data.entrySet().stream().forEach((e) -> {
            cols.add(e.getKey());
            colsVal.add("?");
            vals.add(e.getValue());

        });

        this.getConn().execute(
                "insert into " + table + "(" + cols.toString() + ") values (" + colsVal.toString() + ")",
                vals
        );

        this.getConnSync().execute("insert into UPDATE_VAL_RECEBIDO (ID_RECEBIDO) values (?);", id);

    }

    /**
     * Efetua o tratamento para o update
     *
     * @param table String - Tabela
     * @param id long - Id de controle
     * @param date Date - Horario do registro
     * @param data DataColumn[]
     * @throws Exception
     */
    private void trataUpdateReceived(long id, String table, LinkedHashMap<String, Object> data, String where) throws Exception {
        final StringJoiner cols = new StringJoiner(",");
        final LinkedHashSet<Object> vals = new LinkedHashSet<>();

        data.entrySet().stream().forEach((e) -> {
            cols.add(e.getKey() + " = ?");

            vals.add(e.getValue());

        });

        this.getConn().execute(
                "update " + table + " set " + cols.toString() + " where " + where,
                vals
        );

        this.getConnSync().execute("insert into UPDATE_VAL_RECEBIDO (ID_RECEBIDO) values (?);", id);

    }

    /**
     * Efetua o tratamento para o delete
     *
     * @param table String - Tabela
     * @param id long - Id de controle
     * @param date Date - Horario do registro
     * @param data DataColumn[]
     * @throws Exception
     */
    private void trataDeleteReceived(long id, String table, String where) throws Exception {

        this.getConn().execute("delete from " + table + " where " + where);

        this.getConnSync().execute("insert into UPDATE_VAL_RECEBIDO (ID_RECEBIDO) values (?);", id);
    }

    /**
     * Solicita ao outro host que traga as novas informações para serem
     * replicadas aqui
     */
    private synchronized void requestSyncForOtherHost() throws Exception {
        this.sendInfo(
                "request-sync",
                Long.toString(this.getConnSync().selectOneRow("select coalesce(max(r.ID_RECEBIDO), 0) from UPDATE_VAL_RECEBIDO r").getLong(1))
        );
    }

    /**
     * Prepara e retorna todo o conteúdo para ser enviado ao outro host
     *
     * @return
     * @throws Exception
     */
    private JSONArray getJsonToSync(long lastIdSended) throws Exception {

        final JSONArray r = new JSONArray();

        final ResultSet rs = this.getConnSync().select((StringBuilder sb) -> {
            sb.append("select ");
            sb.append("    r.ID, ");
            sb.append("    r.TABELA, ");
            sb.append("    r.TIPO, ");
            sb.append("    r.WHERE_STR ");
            sb.append("from ");
            sb.append("    VW_INFOS_ALTERACAO r ");
            sb.append("where ");
            sb.append("    r.ID > ?");
        }, lastIdSended);

        while (rs.next()) {

            final JSONObject tmp = new JSONObject();

            tmp.put("_ID", rs.getLong("ID"));
            tmp.put("_TI", rs.getInt("TIPO"));
            tmp.put("_TA", rs.getString("TABELA"));
            if (rs.getInt("TIPO") > 0) {
                tmp.put("_W", rs.getString("WHERE_STR"));
            }

            if (rs.getInt("TIPO") == 2) {

                r.put(tmp);

            } else {

                final ResultSet rss = this.getConn().select("select * from " + rs.getString("TABELA") + " where " + rs.getString("WHERE_STR"));

                if (rss.next()) {

                    final ResultSetMetaData rsmd = rss.getMetaData();

                    final int columns = rsmd.getColumnCount();

                    for (int x = 1; x <= columns; x++) {
                        tmp.put(rsmd.getColumnLabel(x), rss.getObject(rsmd.getColumnLabel(x)));
                    }

                    r.put(tmp);

                }

            }

        }

        return r;
    }

    /**
     * Cria a estrutura da base de sincronização usada
     */
    private void createStructureInDbSync() throws Exception {

        if (CSPUtilidadesLangInstrucoesSQL.hasTabela(this.getConnSync(), "UPDATE_VAL")) {
            return;
        }

        if (this.isCuidaRanges()) {
            this.getConnSync().execute("CREATE GENERATOR GEN_RANGE_ID;");
        }
        this.getConnSync().execute("CREATE GENERATOR GEN_UPDATE_VAL_ID;");
        this.getConnSync().execute("CREATE GENERATOR GEN_UPDATE_VAL_PK_ID;");
        this.getConnSync().execute("CREATE GENERATOR GEN_UPDATE_VAL_RECE_ID;");

        if (this.isCuidaRanges()) {
            this.getConnSync().execute((StringBuilder sb) -> {
                sb.append("CREATE TABLE RANGE ");
                sb.append("( ");
                sb.append("  ID integer NOT NULL, ");
                sb.append("  DE bigint NOT NULL, ");
                sb.append("  ATE bigint NOT NULL, ");
                sb.append("  NOME_GENERATOR varchar(100) NOT NULL, ");
                sb.append("  NOME_TABELA varchar(100) NOT NULL, ");
                sb.append("  EM_USO smallint, ");
                sb.append("  PRIMARY KEY (ID) ");
                sb.append(")");
            });
        }

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("CREATE TABLE UPDATE_VAL ");
            sb.append("( ");
            sb.append("  ID bigint NOT NULL, ");
            sb.append("  TABELA varchar(100) NOT NULL, ");
            sb.append("  TIPO_ALTERACAO smallint NOT NULL, ");
            sb.append("  HORARIO timestamp NOT NULL, ");
            sb.append("  PRIMARY KEY (ID) ");
            sb.append(")");
        });

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("CREATE TABLE UPDATE_VAL_PK ");
            sb.append("( ");
            sb.append("  ID bigint NOT NULL, ");
            sb.append("  COLUNA varchar(100) NOT NULL, ");
            sb.append("  VAL bigint NOT NULL, ");
            sb.append("  UPDATE_VAL_ID bigint NOT NULL, ");
            sb.append("  PRIMARY KEY (ID) ");
            sb.append(")");
        });

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("CREATE TABLE UPDATE_VAL_RECEBIDO ");
            sb.append("( ");
            sb.append("  ID bigint NOT NULL, ");
            sb.append("  ID_RECEBIDO bigint NOT NULL, ");
            sb.append("  HORARIO timestamp NOT NULL, ");
            sb.append("  PRIMARY KEY (ID) ");
            sb.append(")");
        });

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("create view VW_INFOS_ALTERACAO (ID, TABELA, TIPO, WHERE_STR, COLS_STR) ");
            sb.append("as   ");
            sb.append("select  ");
            sb.append("    r.ID,  ");
            sb.append("    r.TABELA, ");
            sb.append("    r.TIPO_ALTERACAO, ");
            sb.append("    list( ");
            sb.append("        rr.COLUNA || ' = ' || rr.VAL, ");
            sb.append("        ' and ' ");
            sb.append("    ), ");
            sb.append("     list( rr.COLUNA,';') ");
            sb.append("from  ");
            sb.append("    UPDATE_VAL r ");
            sb.append("inner join UPDATE_VAL_PK rr ");
            sb.append("    on rr.UPDATE_VAL_ID = r.ID ");
            sb.append("group by  ");
            sb.append("    1, 2, 3;");
        });

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("CREATE TRIGGER UPDATE_VAL_BI FOR UPDATE_VAL ACTIVE ");
            sb.append("before insert POSITION 0 ");
            sb.append("AS ");
            sb.append("BEGIN ");
            sb.append("    NEW.ID = GEN_ID(GEN_UPDATE_VAL_ID, 1); ");
            sb.append("END");
        });

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("CREATE TRIGGER UPDATE_VAL_PK_BI FOR UPDATE_VAL_PK ACTIVE ");
            sb.append("before insert POSITION 0 ");
            sb.append("AS ");
            sb.append("BEGIN ");
            sb.append("    NEW.ID = GEN_ID(GEN_UPDATE_VAL_PK_ID, 1); ");
            sb.append("END");
        });

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("CREATE TRIGGER UPDATE_VAL_RECEBIDO_BI FOR UPDATE_VAL_RECEBIDO ACTIVE ");
            sb.append("before insert POSITION 0 ");
            sb.append("AS ");
            sb.append("BEGIN ");
            sb.append("    NEW.ID = GEN_ID(GEN_UPDATE_VAL_RECE_ID, 1); ");
            sb.append("    new.HORARIO = 'now';");
            sb.append("END");
        });

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("alter table UPDATE_VAL_PK ADD CONSTRAINT FK_UPDATE_VAL_PK_0 ");
            sb.append("  FOREIGN KEY (UPDATE_VAL_ID) REFERENCES UPDATE_VAL (ID) ON UPDATE NO ACTION ON DELETE NO ACTION;");
        });

        if (this.isCuidaRanges()) {
            this.getConnSync().execute((StringBuilder sb) -> {
                sb.append("CREATE PROCEDURE PR_CHECK_AND_SET_NEW_RANGE_GEN ( ");
                sb.append("    TABELA varchar(100), ");
                sb.append("    NEW_VAL bigint");
                sb.append(") as ");
                sb.append("declare variable next_range bigint default null; ");
                sb.append("declare variable next_range_id bigint default null; ");
                sb.append("declare variable atual_range bigint default null; ");
                sb.append("declare variable gen_range varchar(100) default null; ");
                sb.append("declare variable source_sync varchar(100) default '").append(this.getConn().getConfs().getHost()).append(":").append(this.getConn().getConfs().getPath()).append("'; ");
                sb.append("declare variable user_sync varchar(20) default '").append(this.getConn().getConfs().getUser()).append("'; ");
                sb.append("declare variable pass_user_sync varchar(20) default '").append(this.getConn().getConfs().getPass()).append("'; ");
                sb.append("begin ");
                sb.append("     select  ");
                sb.append("        r.ID as ATUAL, ");
                sb.append("        r.NOME_GENERATOR, ");
                sb.append("        min(rr.ID) as next_range_id, ");
                sb.append("        min(rr.DE) as next_range_de ");
                sb.append("    from  ");
                sb.append("        RANGE r ");
                sb.append("    left join RANGE rr ");
                //Próximo range disponível
                sb.append("        on rr.NOME_TABELA = r.NOME_TABELA ");
                sb.append("        and ((:NEW_VAL <= 0 and rr.ID = r.ID) ");
                sb.append("            or ");
                sb.append("            (rr.EM_USO is null and rr.DE >= r.ATE)) ");
                sb.append("    where  ");
                sb.append("        r.NOME_TABELA = :TABELA ");
                sb.append("        and r.EM_USO = 1  ");
                //Não podemos deixar chegar muito perto do limite máximo do range
                sb.append("        and ((:NEW_VAL <= 0) ");
                sb.append("             or  ");
                sb.append("             (r.ATE - :NEW_VAL <= 3)) ");
                sb.append("    group by     ");
                sb.append("        r.id, r.NOME_GENERATOR ");
                sb.append("    into  ");
                sb.append("        :atual_range, :gen_range, :next_range_id, :next_range; ");
                sb.append("         ");
                sb.append("    if (:next_range is not null) then  ");
                sb.append("    begin  ");
                sb.append("        update RANGE set EM_USO = null where id = :atual_range; ");
                sb.append("        update RANGE set EM_USO = 1 where id = :next_range_id; ");

                sb.append("        execute statement 'SET GENERATOR ' || :gen_range || ' to ' || :next_range || ';' ");
                sb.append("    on EXTERNAL data source :source_sync as USER :user_sync password :pass_user_sync; ");
                sb.append("    end  ");
                sb.append("end");
            });

            this.getConnSync().execute((StringBuilder sb) -> {
                sb.append("CREATE TRIGGER RANGE_BI FOR RANGE ACTIVE ");
                sb.append("before insert POSITION 0 ");
                sb.append("AS ");
                sb.append("BEGIN  ");
                sb.append("    NEW.ID = GEN_ID(GEN_RANGE_ID, 1);  ");
                sb.append("    if ((select count(1) from range where NOME_TABELA = new.NOME_TABELA and EM_USO = 1) = 0) then   ");
                sb.append("         new.EM_USO = 1;  ");
                sb.append("END");
            });

            this.getConnSync().execute((StringBuilder sb) -> {
                sb.append("CREATE TRIGGER RANGE_AI FOR RANGE ACTIVE ");
                sb.append("after insert POSITION 0 ");
                sb.append("AS ");
                sb.append("declare variable source_sync varchar(100) default '").append(this.getConn().getConfs().getHost()).append(":").append(this.getConn().getConfs().getPath()).append("'; ");
                sb.append("declare variable user_sync varchar(20) default 'SYNC'; ");
                sb.append("declare variable pass_user_sync varchar(20) default '").append(this.getConnSync().getConfs().getPass()).append("'; ");
                sb.append("declare variable current_id_tab bigint default 0; ");
                sb.append("BEGIN  ");
                sb.append("    execute statement 'select GEN_ID(' || new.NOME_GENERATOR || ', 0) from RDB$DATABASE' ");
                sb.append("    on EXTERNAL data source :source_sync as USER :user_sync password :pass_user_sync ");
                sb.append("    into :current_id_tab; ");
                sb.append("    execute procedure PR_CHECK_AND_SET_NEW_RANGE_GEN (new.NOME_TABELA, :current_id_tab); ");
                sb.append("END");
            });
        }

        this.getConnSync().execute((StringBuilder sb) -> {
            sb.append("create PROCEDURE PR_REGISTRA_UPDATE_VAL ( ");
            sb.append("    TABELA varchar(100), ");
            sb.append("    TIPO_ALTERACAO smallint, ");
            sb.append("    COL_PK_1 varchar(100), ");
            sb.append("    VAL_PK_1 smallint, ");
            sb.append("    COL_PK_2 varchar(100) DEFAULT null, ");
            sb.append("    VAL_PK_2 smallint DEFAULT null, ");
            sb.append("    COL_PK_3 varchar(100) DEFAULT null, ");
            sb.append("    VAL_PK_3 smallint DEFAULT null, ");
            sb.append("    COL_PK_4 varchar(100) DEFAULT null, ");
            sb.append("    VAL_PK_4 smallint DEFAULT null ) ");
            sb.append("AS ");
            sb.append("declare variable id bigint; ");
            sb.append("declare variable aux smallint; ");
            sb.append("BEGIN ");

            if (this.isCuidaRanges()) {
                sb.append("    if (:col_pk_2 is null) then  ");
                //O controle de ranges não se aplica a tabelas com PK composta
                sb.append("        execute procedure PR_CHECK_AND_SET_NEW_RANGE_GEN (:TABELA, :VAL_PK_1); ");
            }
            sb.append("   ");
            sb.append("    insert into UPDATE_VAL(tabela, tipo_alteracao, horario)  ");
            sb.append("        values (:tabela, :tipo_alteracao, 'now') ");
            sb.append("        returning id into :id; ");
            sb.append("     ");
            sb.append("    if (:col_pk_1 is not null) then  ");
            sb.append("        insert into UPDATE_VAL_PK(UPDATE_VAL_id, coluna, val) ");
            sb.append("            values (:id, :col_pk_1, :val_pk_1); ");
            sb.append("             ");
            sb.append("    if (:col_pk_2 is not null) then  ");
            sb.append("        insert into UPDATE_VAL_PK(UPDATE_VAL_id, coluna, val) ");
            sb.append("            values (:id, :col_pk_2, :val_pk_2); ");
            sb.append("             ");
            sb.append("    if (:col_pk_3 is not null) then  ");
            sb.append("        insert into UPDATE_VAL_PK(UPDATE_VAL_id, coluna, val) ");
            sb.append("            values (:id, :col_pk_3, :val_pk_3); ");
            sb.append("             ");
            sb.append("    if (:col_pk_4 is not null) then  ");
            sb.append("        insert into UPDATE_VAL_PK(UPDATE_VAL_id, coluna, val) ");
            sb.append("            values (:id, :col_pk_4, :val_pk_4); ");
            sb.append("END");
        });
    }

    private LinkedHashMap<String, Object> castJsonObjectToObjectAux(JSONObject data) {

        final LinkedHashMap<String, Object> r = new LinkedHashMap<>();

        for (Object t : data.names()) {

            final String nome = t.toString();

            if (nome.startsWith("_")) {
                continue;
            }

            r.put(nome, data.get(nome));

        }

        if (r.isEmpty()) {
            return null;
        }

        return r;
    }

    /**
     * Retorna uma conexao valida com a base
     *
     * @return
     */
    private CSPInstrucoesSQLBase getConn() {
        return conn;
    }

    private CSPInstrucoesSQLBase getConnSync() {
        return connSync;
    }

    private boolean isCuidaRanges() {
        return cuidaRanges;
    }

    /**
     * Retorna as colunas da tabela
     *
     * @param table String
     * @return
     */
    private ArrayList<ModelColunaTabela> getColunas(String table) throws SQLException {

        if (this.mapTabelaColunas.containsKey(table)) {
            return this.mapTabelaColunas.get(table);
        }

        this.mapTabelaColunas.put(table, CSPUtilidadesLangInstrucoesSQLExportaDDL.getColunas(table, this.getConn()));

        return getColunas(table);

    }

}
