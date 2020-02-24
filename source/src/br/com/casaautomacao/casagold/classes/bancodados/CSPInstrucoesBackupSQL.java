/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.modelos.ModelColunaTabela;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.getColunas;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

/**
 * Classe para manipulação do banco de Dados de ALTERACAÇÃO e EXCLUSÃO.<br/>
 * <b style="color:#ff0000">Uso restrito</b>
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 02/11/2015 - 16:21:00
 */
public class CSPInstrucoesBackupSQL {

    public static final String PRE_ALTERACAO_PATH = PATH + "/pre-alteracao/";

    public enum Type {

        ALTERACAO_GOLD("ALTERACAO_GOLD", "_ALTERACAO"),
        EXCLUSAO_GOLD("EXCLUSAO_GOLD", "_EXCLUSAO");
        public final String sufixFile;
        public final String sufixColun;

        private Type(String sufixColun, String sufixFile) {
            this.sufixFile = sufixFile;
            this.sufixColun = sufixColun;
        }

    }

    public static void precessDataInBackup() throws Exception {

        CSPArquivosLocais pre = new CSPArquivosLocais(PRE_ALTERACAO_PATH);
        if (!pre.exists() || !pre.isDir()) {
            return;
        }
        for (CSPArquivosLocais f : pre.listFiles(FileFileFilter.FILE)) {

            JSONObject in = CSPUtilidadesLangJson.getObject(f.getContent());

            Type type = toType(CSPUtilidadesLangJson.getFromJson(in, "TYPE", ""));
            String tabela = CSPUtilidadesLangJson.getFromJson(in, "TABELA", "");

            CSPInstrucoesSQLBase padrao = getConn(CSPUtilidadesLangJson.getFromJson(in, "HOST_BASE", "localhost"), CSPUtilidadesLangJson.getFromJson(in, "PATH_BASE", ""), null);
            CSPInstrucoesSQLBase alteracao = getConn(CSPUtilidadesLangJson.getFromJson(in, "HOST_BASE", "localhost"), CSPUtilidadesLangJson.getFromJson(in, "PATH_BASE", ""), type);

//            alteracao.setAutoCommit(false);
            if (padrao != null && alteracao != null) {

                analisaTabela(tabela, padrao, alteracao, type);

                final ArrayList<String> coln = new ArrayList<>();
                final ArrayList<Object> data = new ArrayList<>();
                final JSONObject dats = CSPUtilidadesLangJson.getFromJson(in, "DATA", new JSONObject());

                for (String c : dats.keySet()) {
                    coln.add(c);
                    data.add(dats.get(c));
                }

                if (type == Type.EXCLUSAO_GOLD) {
                    coln.add("EXCLUSAO_GOLD");
                    data.add(alteracao.max(tabela, "EXCLUSAO_GOLD", null) + 1);
                }
                alteracao.execute("INSERT INTO " + tabela + " (" + StringUtils.join(coln, ",") + ") VALUES (" + CSPUtilidadesLang.repeatStringWithSeparator("?", ",", coln.size()) + ")", data);
            }
            f.delete();
        }
        clearCacheConns();
    }

    /**
     * Identifica o enum Type com base no nome
     *
     * @param s
     * @return
     */
    private static Type toType(String s) {
        for (Type t : Type.values()) {
            if (t.name().equals(s)) {
                return t;
            }
        }
        return Type.ALTERACAO_GOLD;
    }
    private final static HashMap<String, CSPInstrucoesSQLBase> cacheCons = new HashMap<>();

    /**
     * Retorna uma conexão com a base solicitada
     *
     * @param host String - Host da base
     * @param path String - Caminho absoluto da base
     * @param tp Type - Quando setado como null não tentará criar nenhum
     * alteração/exclusão
     * @return
     * @throws Exception
     */
    private static CSPInstrucoesSQLBase getConn(String host, String path, Type tp) throws Exception {
        String nomeArquivo;
        if (tp != null) {

            CSPArquivosLocais arq = new CSPArquivosLocais(path);
            nomeArquivo = arq.objFile().getName();
            nomeArquivo = nomeArquivo.split("\\.")[0]; //tira a extenção

            String ext = arq.getExtension();

            nomeArquivo += tp.sufixFile;

            if (!ext.startsWith("\\.")) {
                ext = "." + ext;
            }
            nomeArquivo = arq.objFile().getPath().replace(arq.objFile().getName(), nomeArquivo + ext);
        } else {
            nomeArquivo = path;
        }
        String key = host + nomeArquivo;
        if (!cacheCons.containsKey(key) || cacheCons.get(key) == null) {
            cacheCons.put(key, new CSPInstrucoesSQLBase(host, nomeArquivo));
        }
        return cacheCons.get(key);
    }

    /**
     * Limpa o cache de conexões (fecha-as) criadas pelo {@link CSPInstrucoesBackupSQL#getConn(java.lang.String, java.lang.String, br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesBackupSQL.Type)
     * }
     */
    private static void clearCacheConns() throws SQLException {
        for (Map.Entry<String, CSPInstrucoesSQLBase> c : cacheCons.entrySet()) {
            c.getValue().close();
        }
        cacheCons.clear();
    }

    /**
     * Armazena em arquivos os dados para replicar posteriormente as informações
     * na base de backup
     *
     * @param tabela String
     * @param conn
     *
     * @param tp Type
     * @param data HashMap<String, Object>
     */
    public static void saveDataInBackup(String tabela, CSPInstrucoesSQLBase conn, Type tp, HashMap<String, Object> data) {
        try {

            CSPArquivosLocais pre = new CSPArquivosLocais(PRE_ALTERACAO_PATH);
            pre.setName((CSPUtilidadesLangDateTime.getTempoCompletoLimpo() + "-" + tabela + "-" + conn.getBaseUsada().name() + ".json").toLowerCase());
            pre.setContent(new JSONObject() {
                {
                    put("TABELA", tabela);
                    put("HOST_BASE", conn.getConfs().getHost());
                    put("PATH_BASE", conn.getConfs().getPath());
                    put("TYPE", tp.name());
                    put("DATA", data);
                }
            }.toString());
        } catch (Exception ex) {
            CSPException.register(ex);
        }

    }

    /**
     * Analisa a tabela no Banco padrão verificando se houve alguma alteração na
     * sua estrutura para ser aplicada com base na conexão passada pelo
     * parâmetro. E se não existir cria a mesma
     *
     * @param tabela String - Tabela
     * @param padrao Connection - Conexão original
     * @return
     */
    private static boolean analisaTabela(String tabela, CSPInstrucoesSQLBase padrao, CSPInstrucoesSQLBase backup, Type tp) throws SQLException {
        if (existeTabela(tabela, backup)) {
//            return true;
            return ajustaTabela(tabela, padrao, backup, tp);
        } else {
            return replicaTabela(tabela, padrao, backup, tp);
        }
    }

    /**
     * Retorna se a tabela existe na base conectada
     *
     * @param tabela String - Nome da tabela
     * @return boolean
     */
    private static boolean existeTabela(String tabela, CSPInstrucoesSQLBase con) throws SQLException {

        ResultSet exi = con.query("SELECT COUNT(*) FROM RDB$RELATION_FIELDS, RDB$FIELDS"
                + "  WHERE RDB$RELATION_FIELDS.RDB$RELATION_NAME = '" + tabela + " '"
                + "  AND (RDB$RELATION_FIELDS.RDB$FIELD_SOURCE = RDB$FIELDS.RDB$FIELD_NAME)"
                + "  AND (RDB$FIELDS.RDB$SYSTEM_FLAG <> 1)");

        if (exi.next()) {
            if (exi.getInt(1) > 0) {
                return true;
            }
        }
        return false;

    }

    /**
     * Ajusta a tabela da conexão para que fique igual a recebida pelo parâmetro
     *
     * @param tabela
     * @param padrao
     * @return
     */
    private static boolean ajustaTabela(String tabela, CSPInstrucoesSQLBase padrao, CSPInstrucoesSQLBase backup, Type tp) throws SQLException {
        final ArrayList<ModelColunaTabela> colsA = new ArrayList<>();
        final ArrayList<ModelColunaTabela> colsB = new ArrayList<>();
        tratamentoDefaultColuns(colsA, getColunas(tabela, backup), false, tabela, tp);
        tratamentoDefaultColuns(colsB, getColunas(tabela, padrao), true, tabela, tp);

        String sql = "";//getSqlDiferencaTabela(tabela, padrao, colsA, colsB);

//        String sql =getInstrucoesDiferentes(tabela, padrao,getColunas(tabela, padrao), getColunas(tabela, this), false, d, fix + "_GOLD");
        sql = sql.replaceAll("\\s+", " ").trim();
        sql = sql.replaceAll("; ", ";").trim();
        sql = sql.replaceAll(CSPUtilidadesSO.LINE_SEPARATOR, "").trim();
        sql = CSPUtilidadesLang.removeDuplicates(sql, ";");

        if (sql != null) {
            for (String s : sql.split(";")) {
                if (s != null && !s.trim().isEmpty()) {
                    backup.execute(s);
                }
            }
        }

        return true;
    }

    /**
     * Replica a tabela da base recebida no parametro para conexão atual
     *
     * @param tabela Tabela a ser replicada.
     * @param padrao
     * @return
     */
    private static boolean replicaTabela(String tabela, CSPInstrucoesSQLBase padrao, CSPInstrucoesSQLBase backup, Type tp) throws SQLException {
        /**
         *
         * A tabela de exclusao tem sempre e somente uma primary key:
         *
         * EXCLUSAO_GOLD: Armazenaria o valor que difere um registro do outro na
         * base padrão, sempre em ordem crescente.
         *
         *
         * A tabela de alteração sempre terá pelo mesmo o ALTERECAO_GOLD como
         * PK:
         *
         * ALTERACAO_GOLD: Cada registro na tabela original começa com ele em 1
         *
         *
         */
        final ArrayList<ModelColunaTabela> cols = new ArrayList<>();
        tratamentoDefaultColuns(cols, getColunas(tabela, padrao), true, tabela, tp);

//        backup.execute(getSqlCompletoTabela(tabela, cols) + ";".trim());
        return true;
    }

    private static void tratamentoDefaultColuns(ArrayList<ModelColunaTabela> news, ArrayList<ModelColunaTabela> olds, boolean addDefs, String tab, Type fix) {

        /**
         *
         * A tabela de exclusao tem sempre e somente uma primary key:
         *
         * EXCLUSAO_GOLD: Armazenaria o valor que difere um registro do outro na
         * base padrão, sempre em ordem crescente.
         *
         *
         * A tabela de alteração sempre terá pelo mesmo o ALTERECAO_GOLD como
         * PK:
         *
         * ALTERACAO_GOLD: Cada registro na tabela original começa com ele em 1
         *
         *
         */
        news.add(new ModelColunaTabela(null, fix.sufixColun, 0, 0, "INTEGER", true, false, true, false, tab, null, null, null, null, null));
        if (addDefs) {
            news.add(new ModelColunaTabela(null, "DATA_" + fix.sufixColun, 0, 0, "DATE", false, false, false, false, tab, null, null, null, null, null));
            news.add(new ModelColunaTabela(null, "HORA_" + fix.sufixColun, 0, 0, "TIME", false, false, false, false, tab, null, null, null, null, null));
        }

        /**
         * Os campos unique nao devem ser usados nas bases de
         * alteraçao/exclusao, pois sempre teremos registros duplicados
         */
        for (ModelColunaTabela col : olds) {
            if ((!addDefs && col.getNome().trim().toLowerCase().equals((fix.sufixColun).toLowerCase())) || !col.getNome().trim().toLowerCase().equals((fix.sufixColun).toLowerCase())) {
                news.add(new ModelColunaTabela(col.getId(), col.getNome(), col.getTamanho(), col.getEscala(), col.getTipo(), col.isPrimaryKey(), false, //Nao pode ser Foreign Key
                        col.isPrimaryKey(),//Só pode ser Not Null se for PK
                        false,//Nao pode ser Unique
                        tab, null, null, null, null, null
                ));
            }
        }
    }

}
