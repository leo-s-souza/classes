/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosMultiplosCaminhos;
import br.com.casaautomacao.casagold.classes.arquivos.InterfaceCSPArquivos;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.modelos.ModelColunaTabela;
import br.com.casaautomacao.casagold.classes.modelos.ModelTabela;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.extraiHostAndPath;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilidades para o proceso de exportação de ddl de um base
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 15:59:07
 */
public abstract class CSPUtilidadesLangInstrucoesSQLExportaDDL extends CSPUtilidadesLangInstrucoesSQL {

    private static final Pattern PATTER_BEGIN = Pattern.compile("(?i)( begin |begin | begin|begin[\n\r])");
    private static final Pattern PATTER_END = Pattern.compile("(?i)( end |end | end;|;end;| end |end[\n\r]|end;$|end$)");

    /**
     * Exporta o DDL da base usando o isql do firebird
     *
     * @param conn
     * @return
     * @throws Exception
     */
    private static CSPArquivos exportDDLToFileWithIsqlFb(CSPInstrucoesSQLBase conn) throws Exception {
        final CSPArquivos caminhoSqlGerado = new CSPArquivos(CSPUtilidadesSO.PATH_TEMP + "/isql_ddl_dump_" + CSPUtilidadesLangDateTime.getTempoCompletoLimpo() + ".sql");
        final String[] caminho = new String[1];

        //Se for no Windows, temos que procurar a pasta de instalação do Firebird
        if (CSPUtilidadesSO.isSoWindows()) {
            CSPArquivosMultiplosCaminhos.list(new CSPArquivosLocais(), new CSPArquivosMultiplosCaminhos.OnListWithEnd() {
                private CSPArquivosLocais fdb25 = null;

                @Override
                public boolean run(InterfaceCSPArquivos file) throws Exception {
                    if (fdb25 != null) {
                        return false;
                    }

                    CSPArquivosLocais arqDeProgramas = (CSPArquivosLocais) file;
                    arqDeProgramas.setName("Firebird");
                    if (arqDeProgramas.exists() && arqDeProgramas.isDir()) {

                        for (CSPArquivosLocais fdbFolder : arqDeProgramas.listFiles()) {
                            String originalNem = fdbFolder.getName();

                            if (fdbFolder.isDir()) {
                                fdbFolder.setName(originalNem + "/bin/isql.exe");

                                if (fdbFolder.getName().toLowerCase().replace("_", "").startsWith("firebird25")) {

                                    if (fdbFolder.exists() && fdbFolder.isFile() && fdb25 == null) {
                                        fdb25 = fdbFolder;
                                        fdb25.setName(originalNem);
                                    }
                                }
                            }
                        }
                    }
                    return true;
                }

                @Override
                public void onEnd() throws Exception {
                    if (fdb25 == null) {
                        CSPLog.info("Não foi possível encontrar uma versão do Firebird 2.5");

                    } else {
                        caminho[0] = fdb25.getAbsolutePath();
                    }
                }
            }, System.getenv("ProgramFiles"), "C:/Program Files", "C:/Program Files (x86)");

            //Se não encontrarmos a pasta, verificamos no registro do Windows...
            if (caminho[0].trim().isEmpty()) {
                //Para sistemas x86
                caminho[0] = CSPUtilidadesSO.WinRegistry.readString(CSPUtilidadesSO.WinRegistry.KeyRegWindows.HKEY_LOCAL_MACHINE, "SOFTWARE\\Firebird Project\\Firebird Server\\Instances", "DefaultInstance");

                if (caminho[0].trim().isEmpty()) {
                    //Para sistemas x64
                    caminho[0] = CSPUtilidadesSO.WinRegistry.readString(CSPUtilidadesSO.WinRegistry.KeyRegWindows.HKEY_LOCAL_MACHINE, "software\\Wow6432Node\\Firebird Project\\Firebird Server\\Instances", "DefaultInstance");
                }
            }

            String[] hostAndPath = extraiHostAndPath(conn.getConfs().getHost() + "/" + conn.getConfs().getPath());

            Process runCommandInSo = CSPUtilidadesSO.runCommandInSo("\"" + caminho[0] + "/bin/isql.exe\" -ex -o \"" + caminhoSqlGerado.getAbsolutePath() + "\" -u SYSDBA -p masterkey " + hostAndPath[0] + "/" + conn.getConfs().getPort() + ":\"" + hostAndPath[1] + "\"");
            runCommandInSo.waitFor();

        } else {
            //No Linux basta executar o "isql-fb".
            String[] hostAndPath = extraiHostAndPath(conn.getConfs().getHost() + "/" + conn.getConfs().getPath());

            Process runShellScriptInLinux = CSPUtilidadesSO.runShellScriptInLinux("isql-fb -ex -o \"" + caminhoSqlGerado.getAbsolutePath() + "\" -u SYSDBA -p masterkey " + hostAndPath[0] + "/" + conn.getConfs().getPort() + ":\"" + hostAndPath[1] + "\"");
            runShellScriptInLinux.waitFor();

            if (!caminhoSqlGerado.isFile() || caminhoSqlGerado.length() == 0) {
                //Aquele bugfix maroto
                runShellScriptInLinux = CSPUtilidadesSO.runShellScriptInLinux("isql-fb -ex -o \"" + caminhoSqlGerado.getAbsolutePath() + "\" -u ISQL -p cri2isql " + hostAndPath[0] + "/" + conn.getConfs().getPort() + ":\"" + hostAndPath[1] + "\"");
                runShellScriptInLinux.waitFor();
            }

        }

        if (!caminhoSqlGerado.isFile() || caminhoSqlGerado.length() == 0) {
            throw new IOException("problemas-com-arquivo-dump");
        }
        return caminhoSqlGerado;
    }

    /**
     * Exporta o ddl da base e retorna a string já minimamente higienizada
     *
     * @param conn
     * @return
     * @throws Exception
     */
    public static String exportDDLWithIsqlFb(CSPInstrucoesSQLBase conn) throws Exception {
        return exportDDLToFileWithIsqlFb(conn).getContent()
                .replaceAll("(?m)^SET SQL DIALECT.*", "")
                .replaceAll("(?m)^COMMIT WORK.*", "")
                .replaceAll("(?m)^SET AUTODD.*", "")
                .replaceAll("(?m)^SET TERM.*", "")
                .replace("^", ";")
                .replace(" ;", ";")
                .replaceAll("(?m)^[ \t]*\r?\n", "")//Linhas em branco
                .replace("  ", " ")
                .replace("CREATE OR ALTER PROCEDURE", "CREATE PROCEDURE")//fdb 3.*
                .trim();

    }

    /**
     * Procura no sql e separa os comandos
     *
     * @param toAdd LinkedHashSet<String>
     * @param exportedDll String
     */
    private static void findAndPutCommandsFromExportedDDL(LinkedHashSet<String> toAdd, String exportedDll, String startFind, String stopFind, String splitCommandBy, OnPut onPut) throws Exception {
        findAndPutCommandsFromExportedDDL(toAdd, exportedDll, startFind, stopFind, splitCommandBy, onPut, false);
    }

    /**
     * Procura no sql e separa os comandos
     *
     * @param toAdd LinkedHashSet<String>
     * @param exportedDll String
     */
    private static void findAndPutCommandsFromExportedDDL(LinkedHashSet<String> toAdd, String exportedDll, String startFind, String stopFind, String splitCommandBy, OnPut onPut, boolean useLastPositionInFisrtSplit) throws Exception {

        String[] split = exportedDll.split(startFind);

        if (split.length > 1) {
            if (useLastPositionInFisrtSplit) {
                split = split[split.length - 1].split(stopFind);
            } else {
                split = split[1].split(stopFind);
            }

            for (String ln : split[0].split(splitCommandBy)) {
                if (ln != null) {
                    ln = ln.replaceAll("([\\n\\r]+\\s*)*$", " ").trim();
                    ln = ln.replaceAll("(?i)( ;)", ";").trim();
                }
                if (onPut != null) {

                    toAdd.add(onPut.run(ln));
                } else {
                    toAdd.add(ln);
                }

            }
        }

    }

    /**
     * Exporta o ddl de criação dos domains
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateDomains(LinkedHashSet<String> to, String exportedDll) throws Exception {

        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                "(?m)^\\/\\* Domain definitions \\*\\/",
                "(?m)^\\/\\* ",
                ";",
                (String val) -> val.replaceAll("\\s+", " ")
        );

    }

    /**
     * Exporta o ddl de criação dos generators
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateGenerators(LinkedHashSet<String> to, String exportedDll) throws Exception {

        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                "(?m)^\\/\\* Generators or sequences \\*\\/",
                "(?m)^\\/\\* ",
                ";",
                (String val) -> val.replaceAll("\\s+", " ")
        );

    }

    /**
     * Exporta o ddl de criação dos indeces e foreign key's
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateIndices(LinkedHashSet<String> to, String exportedDll) throws Exception {

        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                "(?m)^\\/\\* Index definitions.*",
                "(?m)(^\\/\\*|^ALTER TABLE) ",
                ";",
                (String val) -> {
                    if (val == null || !val.trim().startsWith("CREATE ")) {
                        return null;
                    }
                    return val.replaceAll("\\s+", " ");
                }
        );

    }

    /**
     * Exporta o ddl de criação as constrains de foreign key's
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateFks(LinkedHashSet<String> to, String exportedDll) throws Exception {

        boolean normalMode = exportedDll.contains("/* Index definitions");

        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                normalMode ? "(?m)^\\/\\* Index definitions.*" : "(?m)^\\/\\* Table.*",
                "(?m)^\\/\\* ",
                ";",
                (String val) -> {
                    if (val == null || !val.trim().contains(" FOREIGN KEY ")) {
                        return null;
                    }
                    if (!val.trim().startsWith("ALTER TABLE ")) {
                        val = "ALTER TABLE " + val;
                    }
                    return val.replaceAll("\\s+", " ");
                },
                !normalMode
        );

    }

    /**
     * Exporta o ddl de criação das exception
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateExceptions(LinkedHashSet<String> to, String exportedDll) throws Exception {

        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                "(?m)^\\/\\* Exceptions.*",
                "(?m)^\\/\\*",
                ";",
                (String val) -> val.replaceAll("\\s+", " ")
        );

    }

    /**
     * Exporta o ddl de criação das tables
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateTables(LinkedHashSet<String> to, String exportedDll) throws Exception {

        /**
         * Pegamos do ddl somente os comentários que possam informar os nomes
         * das tabelas
         */
        final Matcher nomesTabelas = Pattern.compile("(?m)(^\\/\\* Table.*)").matcher(exportedDll);

        while (nomesTabelas.find()) {

            findAndPutCommandsFromExportedDDL(to,
                    exportedDll,
                    "(?m)^\\/\\* Table: " + nomesTabelas.group().split("^\\/\\* Table: ")[1].split(",")[0],
                    "(?m)^\\/\\* ",
                    ";",
                    (String val) -> {
                        if (val.contains("CREATE TABLE")) {

                            String r = val.split("\\*\\/")[1].replaceAll("\\s+", " ").replaceAll("CONSTRAINT .* PR", "PR").trim();

                            if (r.contains("COMPUTED BY")) {
                                r = r.replaceAll("\\w+ (\\w+\\(\\w+, \\w+\\)|\\w+\\(\\w+,\\w+\\)|\\w+\\(\\w+\\)|\\w+) (COMPUTED BY.*?[,\\)])", "");
                                r = r.replaceAll("\\w+ (COMPUTED BY.*?[,\\)])", "");
                                r = r.replaceAll(", \\)\\),", ",");
                                r = r.replaceAll(",,|, ,", ",");
                                r = r.replaceAll("\\(,", "\\(");
                                r = r.replaceAll(",\\)|, \\)", "\\)");
                            }

                            return r;
                        } else {
                            return null;
                        }
                    }
            );
        }

    }

    /**
     * Exporta o ddl de criação das views
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateViews(LinkedHashSet<String> to, String exportedDll) throws Exception {

        /**
         * Pegamos do ddl somente os comentários que possam informar os nomes
         * das views
         */
        final Matcher nomesViews = Pattern.compile("(?m)(^\\/\\* View.*)").matcher(exportedDll);

        while (nomesViews.find()) {
//            System.out.println("++++++++++++++++++++++++++++++++");
//            System.out.println("++++++++++++++++++++++++++++++++");
//            System.out.println(nomesViews.group());
//            System.out.println("++++++++++++++++++++++++++++++++");
//            System.out.println("++++++++++++++++++++++++++++++++");
            findAndPutCommandsFromExportedDDL(to,
                    exportedDll,
                    "(?m)\\/\\* View: " + nomesViews.group().split("^\\/\\* View: ")[1].split(",")[0] + ",",
                    "(?m)\\/\\* ",
                    "(?m)\\/\\* ",
                    (String val) -> {
//                        System.out.println("+++++++++++++++++++++++++++++++++");
//                        System.out.println(val);
                        if (val.contains("CREATE VIEW")) {
//                            System.out.println("+++++++++++++++++++++++++++++++++");
//                            System.out.println(val);
//                            System.out.println("-------------------");
                            val = "CREATE VIEW" + (val.split("CREATE VIEW")[1]);
//                            
//                            System.out.println(val);
//                            System.out.println("---------------------------------");
                            return trataContentPsql(val, "CREATE VIEW").replaceAll("\\s+", " ");
                        } else {
                            return null;
                        }
                    }
            );
        }

    }

    /**
     * Exporta o ddl de criação das procedures - Parte 1.
     *
     * Somente os create. Necessário para executar a sequencia:
     *
     * cria procedures vazias(pois alguma pode usar uma view que ainda não
     * existirá);
     *
     * cria views(se alguma view usar alguma procedure irá compilar)
     *
     * alter nas procedures adicionando seu fonte(se alguma procedure usa uma
     * view agora conseguirá compilar)
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateProceduresPart1(LinkedHashSet<String> to, String exportedDll) throws Exception {

        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                "(?m)^\\/\\* Stored procedures.*",
                "(?m)^ALTER PROCEDURE",
                "END;",
                (String val) -> {
                    if (!val.contains("CREATE PROCEDURE")) {
                        return null;
                    }

                    val = val.replaceAll("\\s+", " ") + ";";

                    val = val.replace("BEGIN SUSP;", "BEGIN SUSPEND; END;");
                    val = val.replace("BEGIN EXIT;", "BEGIN EXIT; END;");
                    val = val.replace("ENDEND", "END END");

                    val = val.replaceAll("; ;", ";");
                    return val;
                }
        );

    }

    /**
     * Exporta o ddl de criação das procedures - Parte 2
     *
     * Exporta o conteúdo interno das procedures, para que seja possível usa-las
     * de fato
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateProceduresPart2(LinkedHashSet<String> to, String exportedDll) throws Exception {

        final String stopFind = "(?m)^\\/\\*";
        final String splitCommandBy = "(?m)(?i)(^;|^ ;$|^END;| END;$|^END;$|\tEND;$|;END;$)";
        final OnPut onPut = (String val) -> {
            return trataContentPsql(val, "ALTER PROCEDURE");
        };

        //fdb 3.*
        findAndPutCommandsFromExportedDDL(to, exportedDll,
                "(?m)^\\/\\* Stored procedures bodies.*",
                stopFind, splitCommandBy, onPut);

        findAndPutCommandsFromExportedDDL(to, exportedDll,
                "(?m)^\\/\\* Stored procedures.*",
                stopFind, splitCommandBy, onPut);
    }

    /**
     * Exporta o ddl de criação dos triggers
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateTriggers(LinkedHashSet<String> to, String exportedDll) throws Exception {
        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                "(?m)^\\/\\* Triggers.*",
                "(?m)^\\/\\*",
                "(?m)(?i)(^;|^END;|END;$)",
                (String val) -> {
                    return trataContentPsql(val, "CREATE TRIGGER");
                }
        );
    }

    /**
     * Exporta o ddl de criação dos comentátios feitos pela base
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateComments(LinkedHashSet<String> to, String exportedDll) throws Exception {
        findAndPutCommandsFromExportedDDL(to,
                exportedDll,
                "(?m)^\\/\\* Comments for.*",
                "(?m)^\\/\\*",
                "';",
                (String val) -> {
                    if (!val.contains("COMMENT ON")) {
                        return null;
                    }
                    return val.replaceAll("\\s+", " ") + "';";
                }
        );
    }

    /**
     * Centraliza os tratamentos para o conteúdo do begin/end de uma
     * procedure/trigger
     *
     * @param sql
     * @return
     */
    private static String trataContentPsql(String sql, String key) {

        if (!sql.contains(key)) {

            return null;
        }

        sql = sql.split(key)[1];
        sql = key + sql;

        sql = sql.replaceAll("\\/\\*([\\S\\s]+?)\\*\\/", "").replaceAll("(?s)/\\*.*?\\*/", "");

        int beginCount = 0;
        int endCount = 0;
        Matcher matcher;
        matcher = PATTER_BEGIN.matcher(sql);
        while (matcher.find()) {
            beginCount++;
        }
        matcher = PATTER_END.matcher(sql);
        while (matcher.find()) {
            endCount++;
        }

        if (beginCount > endCount) {
            sql = sql + "\nEND;";
        }

        sql = sql.replaceAll("(?m)^[ \t]*\r?\n", "  ");

        if (sql.contains("--")) {

            StringBuilder toFixCommentInline = new StringBuilder();

            for (String s : sql.split("\n")) {
                toFixCommentInline.append(s.split("--")[0]);
                toFixCommentInline.append("\n");
            }

            sql = toFixCommentInline.toString();
        }

        sql = sql.replaceAll("\\s+", " ");

        sql = sql.replaceAll("(?i)SUSP END", "SUSPEND; END").trim();

        sql = sql.replaceAll("(?i)SUSP$", "SUSPEND;").trim();

        if (sql.toUpperCase().endsWith("SUSPEND")) {
            sql = sql + ";END;";
        } else if (sql.toUpperCase().endsWith("SUSPEND;")) {
            sql = sql + "END;";
        }
        sql = sql.replaceAll("(?i)END ^", "END").trim();
        sql = sql.replaceAll("(?i)ENDEND", "END END").trim();
        sql = sql.replaceAll("(?i) ASdeclare ", " AS declare ").trim();
        sql = sql.replaceAll("(?i) ASSELECT ", " AS SELECT ").trim();

        return sql;
    }

    /**
     * Extraí da conexão as infromações a respeito das tabelas
     *
     * @param conn
     * @return
     * @throws java.sql.SQLException
     */
    public static ArrayList<ModelTabela> getTabelas(CSPInstrucoesSQLBase conn) throws SQLException {
        ArrayList<ModelTabela> column = new ArrayList<>();

        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT DISTINCT ");
            sb.append("     TRIM(RRF.RDB$RELATION_NAME) AS NOME, ");
            sb.append("     COUNT(0) AS NUM_CAMPOS ");
            sb.append("FROM ");
            sb.append("     RDB$RELATION_FIELDS RRF, ");
            sb.append("     RDB$FIELDS RFL ");
            sb.append("WHERE ");
            sb.append("     RRF.RDB$FIELD_SOURCE = RFL.RDB$FIELD_NAME ");
            sb.append("     AND RFL.RDB$SYSTEM_FLAG = 0 ");
            sb.append("     AND RRF.RDB$VIEW_CONTEXT IS NULL ");
            sb.append("GROUP BY ");
            sb.append("     NOME;");
        });

        while (rs.next()) {
            column.add(new ModelTabela(rs.getString("NOME").trim(), rs.getInt("NUM_CAMPOS")));
        }

        return column;
    }

    /**
     * Extraí da conexão os nomes das tabelas presentes
     *
     * @param conn
     * @return
     * @throws java.sql.SQLException
     */
    public static ArrayList<String> getTabelasNomes(CSPInstrucoesSQLBase conn) throws SQLException {
        ArrayList<String> tables = new ArrayList<>();

        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT DISTINCT ");
            sb.append("     TRIM(RRF.RDB$RELATION_NAME) ");
            sb.append("FROM ");
            sb.append("     RDB$RELATION_FIELDS RRF, ");
            sb.append("     RDB$FIELDS RFL ");
            sb.append("WHERE ");
            sb.append("     RRF.RDB$FIELD_SOURCE = RFL.RDB$FIELD_NAME ");
            sb.append("     AND RRF.RDB$SYSTEM_FLAG = 0 ");
            sb.append("     AND RRF.RDB$VIEW_CONTEXT IS NULL;");
        });

        while (rs.next()) {
            tables.add(rs.getString(1));
        }

        return tables;
    }

    /**
     * Extraí da conexão as infromações a respeito das colunas da tabela
     *
     * @param conn
     * @param table
     * @return
     * @throws java.sql.SQLException
     */
    public static ArrayList<ModelColunaTabela> getColunas(String table, CSPInstrucoesSQLBase conn) throws SQLException {
        return getColunas(conn, table);
    }

    /**
     * Extraí da conexão as infromações a respeito das colunas da tabela
     *
     * @param conn
     * @param table
     * @return
     * @throws java.sql.SQLException
     */
    public static ArrayList<ModelColunaTabela> getColunas(CSPInstrucoesSQLBase conn, String table) throws SQLException {
        ArrayList<ModelColunaTabela> columns = new ArrayList<>();

        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT DISTINCT UPPER(TRIM(RRF.RDB$FIELD_NAME)) AS CAMPO ,");
            sb.append("      iif(RFL.RDB$computed_source is not null, 'true', 'false') as IS_COMPUTED_BY,  ");
            sb.append("      CASE RTP.RDB$TYPE_NAME  ");
            sb.append("          WHEN 'VARYING'  THEN  'VARCHAR'  ");
            sb.append("          WHEN 'LONG' THEN iif(RFL.RDB$FIELD_SCALE != 0,'NUMERIC','INTEGER')  ");
            sb.append("          WHEN 'SHORT'    THEN  'SMALLINT'  ");
            sb.append("          WHEN 'DOUBLE'   THEN  'DOUBLE PRECISION'  ");
            sb.append("          WHEN 'FLOAT'    THEN  'DOUBLE PRECISION'  ");
            sb.append("          WHEN 'INT64'    THEN  'NUMERIC'  ");
            sb.append("          WHEN 'TEXT'     THEN  'CHAR'  ");
            sb.append("      ELSE RTP.RDB$TYPE_NAME END TIPO_CAMPO,");
            sb.append("      CASE RTP.RDB$TYPE_NAME  ");
            sb.append("          WHEN  'VARYING' THEN RFL.RDB$CHARACTER_LENGTH");
            sb.append("          WHEN  'TEXT' THEN RFL.RDB$CHARACTER_LENGTH");
            sb.append("      ELSE  RFL.RDB$FIELD_PRECISION END AS TAMANHO,");
            sb.append("      RFL.RDB$FIELD_SCALE AS ESCALA,");
            sb.append("         IIF(  EXISTS(   SELECT      FIRST 1 1");
            sb.append("                            FROM        RDB$RELATION_CONSTRAINTS  RCN");
            sb.append("                            INNER JOIN  RDB$INDEX_SEGMENTS        ISG     ON    RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME AND");
            sb.append("                                                                                ISG.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME");
            sb.append("                            WHERE       RCN.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY' AND");
            sb.append("                                        RCN.RDB$RELATION_NAME =  RRF.RDB$RELATION_NAME),");
            sb.append("                  'true',");
            sb.append("                  'false')  AS  IS_PRIMARY_KEY,");
            sb.append("         IIF(  EXISTS(   SELECT      FIRST 1 1");
            sb.append("                            FROM        RDB$RELATION_CONSTRAINTS  RCN");
            sb.append("                            INNER JOIN  RDB$INDEX_SEGMENTS        ISG     ON    RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME AND");
            sb.append("                                                                                ISG.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME");
            sb.append("                            WHERE       RCN.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY' AND");
            sb.append("                                        RCN.RDB$RELATION_NAME =  RRF.RDB$RELATION_NAME),");
            sb.append("                  'true',");
            sb.append("                  'false')  AS  IS_FOREIGN_KEY,");
            sb.append("         IIF(  EXISTS(   SELECT      FIRST 1 1");
            sb.append("                            FROM        RDB$RELATION_CONSTRAINTS  RCN");
            sb.append("                            INNER JOIN  RDB$INDEX_SEGMENTS        ISG     ON    RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME AND");
            sb.append("                                                                                ISG.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME");
            sb.append("                            WHERE       RCN.RDB$CONSTRAINT_TYPE = 'UNIQUE' AND");
            sb.append("                                        RCN.RDB$RELATION_NAME =  RRF.RDB$RELATION_NAME),");
            sb.append("                  'true',");
            sb.append("                  IIF(  EXISTS(   SELECT      FIRST 1 1");
            sb.append("                  FROM      RDB$INDICES RI");
            sb.append("                     INNER JOIN RDB$INDEX_SEGMENTS    RIS     ON    RIS.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME AND");
            sb.append("                                                                   RRF.RDB$FIELD_NAME = RIS.RDB$FIELD_NAME");
            sb.append("                     WHERE     RI.RDB$INDEX_NAME  = RIS.RDB$INDEX_NAME AND");
            sb.append("                               RI.RDB$UNIQUE_FLAG = 1 AND");
            sb.append("                               RI.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME),");
            sb.append("                     'true',");
            sb.append("                     'false')");
            sb.append("                  )  AS  IS_UNIQUE,");
            sb.append("          (SELECT ");
            sb.append("                 RCN.RDB$CONSTRAINT_NAME");
            sb.append("           FROM ");
            sb.append("                RDB$RELATION_CONSTRAINTS RCN ");
            sb.append("                INNER JOIN RDB$INDEX_SEGMENTS ISG ON RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME AND ISG.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME ");
            sb.append("           WHERE");
            sb.append("             RCN.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY' AND RCN.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME)  AS CONSTRAINT_NAME_PK,");
            sb.append("          (SELECT FIRST 1 ");
            sb.append("                 RCN.RDB$CONSTRAINT_NAME");
            sb.append("           FROM ");
            sb.append("                RDB$RELATION_CONSTRAINTS RCN ");
            sb.append("                INNER JOIN RDB$INDEX_SEGMENTS ISG ON RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME AND ISG.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME ");
            sb.append("           WHERE");
            sb.append("             RCN.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY' AND RCN.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME)  AS CONSTRAINT_NAME_FK,");
            sb.append("           (SELECT FIRST 1 S2.RDB$FIELD_NAME");
            sb.append("                  FROM RDB$INDEX_SEGMENTS s");
            sb.append("                 LEFT JOIN RDB$INDICES i ON i.RDB$INDEX_NAME = s.RDB$INDEX_NAME");
            sb.append("                 LEFT JOIN RDB$RELATION_CONSTRAINTS rc ON rc.RDB$INDEX_NAME = s.RDB$INDEX_NAME");
            sb.append("                 LEFT JOIN RDB$REF_CONSTRAINTS refc ON rc.RDB$CONSTRAINT_NAME = refc.RDB$CONSTRAINT_NAME");
            sb.append("                 LEFT JOIN RDB$RELATION_CONSTRAINTS rc2 ON rc2.RDB$CONSTRAINT_NAME = refc.RDB$CONST_NAME_UQ");
            sb.append("                 LEFT JOIN RDB$INDICES i2 ON i2.RDB$INDEX_NAME = rc2.RDB$INDEX_NAME ");
            sb.append("                 LEFT JOIN RDB$INDEX_SEGMENTS s2 ON i2.RDB$INDEX_NAME = s2.RDB$INDEX_NAME AND s.RDB$FIELD_POSITION = s2.RDB$FIELD_POSITION ");
            sb.append("             WHERE");
            sb.append("                 i.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME AND");
            sb.append("                 S.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME AND");
            sb.append("                 rc.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY'");
            sb.append("             ORDER BY i.RDB$INDEX_ID DESC) AS REFER_NAME_COLUMN_FK,");
            sb.append("           (SELECT FIRST 1 i2.RDB$RELATION_NAME ");
            sb.append("                  FROM RDB$INDEX_SEGMENTS s");
            sb.append("                 LEFT JOIN RDB$INDICES i ON i.RDB$INDEX_NAME = s.RDB$INDEX_NAME");
            sb.append("                 LEFT JOIN RDB$RELATION_CONSTRAINTS rc ON rc.RDB$INDEX_NAME = s.RDB$INDEX_NAME");
            sb.append("                 LEFT JOIN RDB$REF_CONSTRAINTS refc ON rc.RDB$CONSTRAINT_NAME = refc.RDB$CONSTRAINT_NAME");
            sb.append("                 LEFT JOIN RDB$RELATION_CONSTRAINTS rc2 ON rc2.RDB$CONSTRAINT_NAME = refc.RDB$CONST_NAME_UQ");
            sb.append("                 LEFT JOIN RDB$INDICES i2 ON i2.RDB$INDEX_NAME = rc2.RDB$INDEX_NAME ");
            sb.append("                 LEFT JOIN RDB$INDEX_SEGMENTS s2 ON i2.RDB$INDEX_NAME = s2.RDB$INDEX_NAME ");
            sb.append("             WHERE");
            sb.append("                 i.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME AND");
            sb.append("                 s.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME AND");
            sb.append("                 rc.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY'");
            sb.append("             ORDER BY i.RDB$INDEX_ID DESC) AS REFER_NAME_TABLE_FK,");
            sb.append("          (SELECT ");
            sb.append("                 RCN.RDB$CONSTRAINT_NAME");
            sb.append("           FROM ");
            sb.append("                RDB$RELATION_CONSTRAINTS RCN ");
            sb.append("                INNER JOIN RDB$INDEX_SEGMENTS ISG ON RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME AND ISG.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME ");
            sb.append("           WHERE");
            sb.append("             RCN.RDB$CONSTRAINT_TYPE = 'UNIQUE' AND RCN.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME)  AS CONSTRAINT_NAME_UNIQUE,");
            sb.append("             IIF(RRF.RDB$NULL_FLAG= 1,'true','false')  AS  IS_NOT_NULL,");
            sb.append("RDB$FIELD_SOURCE as ID ");
            sb.append("FROM        RDB$RELATION_FIELDS   RRF ");
            sb.append("INNER JOIN  RDB$FIELDS            RFL     ON    RFL.RDB$FIELD_NAME = RRF.RDB$FIELD_SOURCE ");
            sb.append("INNER JOIN  RDB$TYPES             RTP     ON    RTP.RDB$TYPE = RFL.RDB$FIELD_TYPE AND");
            sb.append("                                                RTP.RDB$FIELD_NAME = 'RDB$FIELD_TYPE' ");
            sb.append("LEFT JOIN   RDB$INDEX_SEGMENTS    RIS     ON    RIS.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME AND");
            sb.append("                                                EXISTS (  SELECT      FIRST 1 1");
            sb.append("                                                          FROM        RDB$INDICES   IND");
            sb.append("                                                          INNER JOIN  RDB$REF_CONSTRAINTS   RFC   ON    RFC.RDB$CONSTRAINT_NAME = IND.RDB$INDEX_NAME");
            sb.append("                                                          WHERE       IND.RDB$INDEX_NAME = RIS.RDB$INDEX_NAME AND");
            sb.append("                                                                      IND.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME) ");
            sb.append("LEFT JOIN   RDB$REF_CONSTRAINTS   RFC     ON    RFC.RDB$CONSTRAINT_NAME = RIS.RDB$INDEX_NAME ");
            sb.append("LEFT JOIN   RDB$INDEX_SEGMENTS    RIS2    ON    RIS2.RDB$INDEX_NAME = RFC.RDB$CONST_NAME_UQ AND");
            sb.append("                                                RIS2.RDB$FIELD_POSITION = RIS.RDB$FIELD_POSITION ");
            sb.append("WHERE       RRF.RDB$RELATION_NAME NOT STARTING WITH 'RDB$' ");
            sb.append("AND UPPER(RRF.RDB$RELATION_NAME) = ? ");
            sb.append("AND ( ");
            sb.append("     SELECT ");
            sb.append("          count(0)");
            sb.append("     FROM ");
            sb.append("          RDB$RELATIONS ");
            sb.append("     WHERE ");
            sb.append("          RDB$RELATIONS.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME ");
            sb.append("          AND RDB$RELATIONS.RDB$RELATION_TYPE = 0 ");
            sb.append(") > 0 ");
            sb.append("ORDER BY RRF.RDB$FIELD_POSITION");
        }, table);

        while (rs.next()) {
            String tipo = (rs.getString("TIPO_CAMPO").replace("CHARVARCHAR", "VARCHAR").replace("SMALLINT", "NUMERIC")).trim();
            columns.add(new ModelColunaTabela(
                    rs.getString("ID").trim(),
                    rs.getString("CAMPO").trim(),
                    "INTEGER".equals(tipo) ? 32 : rs.getInt("TAMANHO"),
                    Integer.parseInt(rs.getString("ESCALA").replace("-", "")),
                    tipo,
                    rs.getBoolean("IS_PRIMARY_KEY"),
                    rs.getBoolean("IS_FOREIGN_KEY"),
                    rs.getBoolean("IS_NOT_NULL"),
                    rs.getBoolean("IS_UNIQUE"),
                    table,
                    rs.getString("CONSTRAINT_NAME_PK") == null ? null : rs.getString("CONSTRAINT_NAME_PK").trim(),
                    rs.getString("CONSTRAINT_NAME_FK") == null ? null : rs.getString("CONSTRAINT_NAME_FK").trim(),
                    rs.getString("REFER_NAME_TABLE_FK") == null ? null : rs.getString("REFER_NAME_TABLE_FK").trim(),
                    rs.getString("REFER_NAME_COLUMN_FK") == null ? null : rs.getString("REFER_NAME_COLUMN_FK").trim(),
                    rs.getString("CONSTRAINT_NAME_UNIQUE") == null ? null : rs.getString("CONSTRAINT_NAME_UNIQUE").trim(),
                    rs.getBoolean("IS_COMPUTED_BY")
            ));
        }

        return columns;
    }

    /**
     * Extraí da conexão os nomes das colunas da tabela presentes
     *
     * @param conn CSPInstrucoesSQLBase
     * @param tabela String
     * @return
     * @throws java.sql.SQLException
     */
    public static ArrayList<String> getColunasNomes(String tabela, CSPInstrucoesSQLBase conn) throws SQLException {
        return getColunasNomes(conn, tabela);
    }

    /**
     * Extraí da conexão os nomes das colunas da tabela presentes
     *
     * @param conn CSPInstrucoesSQLBase
     * @param tabela String
     * @return
     * @throws java.sql.SQLException
     */
    public static ArrayList<String> getColunasNomes(CSPInstrucoesSQLBase conn, String tabela) throws SQLException {
        ArrayList<String> arr = new ArrayList<>();
        ResultSet result = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("      TRIM(RRF.RDB$FIELD_NAME) AS CAMPO ");
            sb.append("FROM ");
            sb.append("        RDB$RELATION_FIELDS   RRF ");
            sb.append("INNER JOIN ");
            sb.append("     RDB$FIELDS            RFL     ON    RFL.RDB$FIELD_NAME = RRF.RDB$FIELD_SOURCE ");
            sb.append("INNER JOIN ");
            sb.append("     RDB$TYPES             RTP     ON    RTP.RDB$TYPE = RFL.RDB$FIELD_TYPE AND ");
            sb.append("                                                 RTP.RDB$FIELD_NAME = 'RDB$FIELD_TYPE' ");
            sb.append("LEFT JOIN   ");
            sb.append("     RDB$INDEX_SEGMENTS    RIS     ON    RIS.RDB$FIELD_NAME = RRF.RDB$FIELD_NAME AND ");
            sb.append("                                                 EXISTS (  SELECT      FIRST 1 1 ");
            sb.append("                                                           FROM        RDB$INDICES   IND ");
            sb.append("                                                           INNER JOIN  RDB$REF_CONSTRAINTS   RFC   ON    RFC.RDB$CONSTRAINT_NAME = IND.RDB$INDEX_NAME ");
            sb.append("                                                           WHERE       IND.RDB$INDEX_NAME = RIS.RDB$INDEX_NAME AND ");
            sb.append("                                                                       IND.RDB$RELATION_NAME = RRF.RDB$RELATION_NAME) ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$REF_CONSTRAINTS   RFC     ON    RFC.RDB$CONSTRAINT_NAME = RIS.RDB$INDEX_NAME ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$INDEX_SEGMENTS    RIS2    ON    RIS2.RDB$INDEX_NAME = RFC.RDB$CONST_NAME_UQ AND ");
            sb.append("     RIS2.RDB$FIELD_POSITION = RIS.RDB$FIELD_POSITION ");
            sb.append("LEFT  JOIN ");
            sb.append("     RDB$RELATION_CONSTRAINTS RRC  ON    RFC.RDB$CONST_NAME_UQ = RRC.RDB$CONSTRAINT_NAME AND ");
            sb.append("     RRC.RDB$CONSTRAINT_TYPE = 'PRIMARY KEY' ");
            sb.append("WHERE ");
            sb.append("     RRF.RDB$RELATION_NAME NOT STARTING WITH 'RDB$' AND ");
            sb.append("     RRF.RDB$RELATION_NAME = ?");
        }, tabela);
        while (result.next()) {
            arr.add(result.getString("CAMPO").trim());
        }
        return arr;
    }

    /**
     * Retorna a ordem das tabelas em que os inserts devem ser executados na
     * base de dados.
     *
     * @param conn CSPInstrucoesSQLBase
     *
     * @return
     * @throws SQLException
     */
    public static LinkedHashSet<String> getTabelasNomeOrdemInsert(CSPInstrucoesSQLBase conn) throws SQLException {

        LinkedHashSet<String> ordemTabelas = new LinkedHashSet<>();

        for (String tabela : getTabelasNomes(conn)) {
            ordenaTabelasNomesInsert(conn, tabela, ordemTabelas);
        }

        return ordemTabelas;

    }

    /**
     * Recebe a tabela e verifica se a mesma é ligada a alguma outra tabela para
     * colocar a tabela referencia na frente da tabela que faz a referencia.
     *
     * @param conn CSPInstrucoesSQLBase
     *
     * @param tabela - recebe a tabela que deve ser verificada.
     * @param ordemTabelas - recebe as tabelas já em ordem para fazer o insert.
     * @throws SQLException
     */
    public static void ordenaTabelasNomesInsert(CSPInstrucoesSQLBase conn, String tabela, LinkedHashSet<String> ordemTabelas) throws SQLException {
        if (!ordemTabelas.contains(tabela)) {
            LinkedHashSet<String> fks = getFksTabela(conn, tabela);

            if (fks.isEmpty()) {
                ordemTabelas.add(tabela);

            } else {
                for (String fk : fks) {
                    ordenaTabelasNomesInsert(conn, fk, ordemTabelas);
                }

                ordemTabelas.add(tabela);
            }
        }
    }

    /**
     * Retorna as tabelas que são FK da passada por parâmetro
     *
     * @param conn CSPInstrucoesSQLBase
     * @param table String
     * @return
     * @throws SQLException
     */
    public static LinkedHashSet<String> getFksTabela(CSPInstrucoesSQLBase conn, String table) throws SQLException {
        LinkedHashSet<String> tables = new LinkedHashSet<>();

        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("     TRIM(IND2.RDB$RELATION_NAME) AS REFERENCES_TABLE ");
            sb.append("FROM ");
            sb.append("     RDB$INDEX_SEGMENTS ISG ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$INDICES IND ON IND.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$RELATION_CONSTRAINTS RCN ON RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$REF_CONSTRAINTS RFC ON RCN.RDB$CONSTRAINT_NAME = RFC.RDB$CONSTRAINT_NAME ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$RELATION_CONSTRAINTS RCN2 ON RCN2.RDB$CONSTRAINT_NAME = RFC.RDB$CONST_NAME_UQ ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$INDICES IND2 ON IND2.RDB$INDEX_NAME = RCN2.RDB$INDEX_NAME ");
            sb.append("LEFT JOIN ");
            sb.append("     RDB$INDEX_SEGMENTS ISG2 ON IND2.RDB$INDEX_NAME = ISG2.RDB$INDEX_NAME ");
            sb.append("WHERE ");
            sb.append("     RCN.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY' ");
            sb.append("AND ");
            sb.append("     IND.RDB$RELATION_NAME = ? ");
            sb.append("AND ");
            sb.append("     IND2.RDB$RELATION_NAME <> IND.RDB$RELATION_NAME ");
            sb.append("ORDER BY ");
            sb.append("     ISG.RDB$FIELD_POSITION");
        }, table);

        while (rs.next()) {
            tables.add(rs.getString(1));
        }

        return tables;
    }

    /**
     * Retorna o nome de todas os Generators do banco.
     *
     * @param conn CSPInstrucoesSQLBase
     * @return
     * @throws SQLException
     */
    public static LinkedHashSet<String> getGeneratorsNomes(CSPInstrucoesSQLBase conn) throws SQLException {
        LinkedHashSet<String> generators = new LinkedHashSet<>();

        ResultSet rs = conn.select("SELECT DISTINCT TRIM(RDB$GENERATOR_NAME) FROM RDB$GENERATORS WHERE RDB$SYSTEM_FLAG = 0;");

        while (rs.next()) {
            generators.add(rs.getString(1));
        }

        return generators;
    }

    /**
     * Retorna o nome de todas as exceptions do banco.
     *
     * @param conn CSPInstrucoesSQLBase
     * @return
     * @throws SQLException
     */
    public static LinkedHashSet<String> getExceptionsNomes(CSPInstrucoesSQLBase conn) throws SQLException {
        LinkedHashSet<String> generators = new LinkedHashSet<>();

        ResultSet rs = conn.select("SELECT r.RDB$EXCEPTION_NAME FROM RDB$EXCEPTIONS r");

        while (rs.next()) {
            generators.add(rs.getString(1));
        }

        return generators;
    }

    /**
     * Retorna o nome de todas as views do banco.
     *
     * @param conn CSPInstrucoesSQLBase
     *
     * @return LinkedHashSet<String> - Array com o nome de todas as views do
     * banco.
     * @throws SQLException
     */
    public static LinkedHashSet<String> getViewsNomes(CSPInstrucoesSQLBase conn) throws SQLException {
        LinkedHashSet<String> views = new LinkedHashSet<>();

        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("    DISTINCT TRIM(RDB$VIEW_NAME) AS VIEWS, ");
            sb.append("    count(v.RDB$RELATION_NAME) ");
            sb.append("FROM ");
            sb.append("    RDB$VIEW_RELATIONS v ");
            sb.append("group by ");
            sb.append("    1 ");
            sb.append("order by ");
            sb.append("    2 desc, 1 asc  ");
        });

        while (rs.next()) {
            views.add(rs.getString(1));
        }

        return views;
    }

    /**
     * Retorna o nome de todos os Triggers do banco.
     *
     * @param conn CSPInstrucoesSQLBase - Recebe a conexão com a base de dados
     *
     * @return LinkedHashSet(String) - Array com o nome de todos os triggers da
     * base.
     * @throws SQLException
     */
    public static LinkedHashSet<String> getTriggersNomes(CSPInstrucoesSQLBase conn) throws SQLException {
        LinkedHashSet<String> triggers = new LinkedHashSet<>();

        ResultSet rs = conn.select("SELECT TRIM(RDB$TRIGGER_NAME) FROM RDB$TRIGGERS WHERE RDB$SYSTEM_FLAG=0;");

        while (rs.next()) {
            triggers.add(rs.getString(1));
        }

        return triggers;
    }

    /**
     * Retorna o nome de todas as Procedures do banco.
     *
     * @param conn CSPInstrucoesSQLBase
     *
     * @return
     * @throws SQLException
     */
    public static LinkedHashSet<String> getProceduresNomes(CSPInstrucoesSQLBase conn) throws SQLException {
        LinkedHashSet<String> procedures = new LinkedHashSet<>();

        ResultSet rs = conn.select("SELECT DISTINCT TRIM(RDB$PROCEDURE_NAME) FROM RDB$PROCEDURES;");

        while (rs.next()) {
            procedures.add(rs.getString(1));
        }

        return procedures;
    }

    /**
     * Retorna o nome de todas os Domains do banco.
     *
     * @param conn CSPInstrucoesSQLBase
     *
     * @return
     * @throws SQLException
     */
    public static LinkedHashSet<String> getDomainsNomes(CSPInstrucoesSQLBase conn) throws SQLException {
        LinkedHashSet<String> domains = new LinkedHashSet<>();

        ResultSet rs = conn.select(
                "SELECT F.RDB$FIELD_NAME FROM RDB$FIELDS F LEFT JOIN RDB$RELATION_FIELDS RF"
                + " ON RF.RDB$FIELD_SOURCE = F.RDB$FIELD_NAME WHERE F.RDB$FIELD_NAME NOT CONTAINING 'RDB$' AND F.RDB$SYSTEM_FLAG = 0"
        );

        while (rs.next()) {
            domains.add(rs.getString(1).trim());
        }

        return domains;
    }

    /**
     * Retorna o nome de todos os indeces do banco.
     *
     * @param conn CSPInstrucoesSQLBase
     * @param where
     *
     * @return
     * @throws SQLException
     */
    public static LinkedHashSet<String> getIndicesNomes(CSPInstrucoesSQLBase conn, String where) throws SQLException {
        LinkedHashSet<String> indices = new LinkedHashSet<>();

        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("	r.RDB$INDEX_NAME ");
            sb.append("FROM ");
            sb.append("	RDB$INDICES r ");
            sb.append("where ");
            sb.append("	r.RDB$RELATION_NAME not like 'RDB$%' ");
            if (where != null) {
                sb.append(" AND ");
                sb.append(where);

            }
        });

        while (rs.next()) {
            indices.add(rs.getString(1).trim());
        }

        return indices;
    }

    /**
     * Retorna o nome+tabela de todas as constrains do banco.
     *
     * @param conn CSPInstrucoesSQLBase
     * @param where
     *
     * @return LinkedHashSet<String[]> - [0] => nome constraint | [1] => tabela
     * @throws SQLException
     */
    public static LinkedHashSet<String[]> getConstraintsNomes(CSPInstrucoesSQLBase conn, String where) throws SQLException {
        LinkedHashSet<String[]> indices = new LinkedHashSet<>();

        ResultSet rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("     RCN.RDB$CONSTRAINT_NAME, ");
            sb.append("     RDB$RELATION_NAME, ");
            sb.append("     RCN.RDB$CONSTRAINT_TYPE ");
            sb.append("FROM ");
            sb.append("     RDB$RELATION_CONSTRAINTS RCN ");
            sb.append("INNER JOIN ");
            sb.append("     RDB$INDEX_SEGMENTS ISG ");
            sb.append("             ON RCN.RDB$INDEX_NAME = ISG.RDB$INDEX_NAME ");
            if (where != null) {
                sb.append("WHERE ");
                sb.append(where);
                sb.append(" ");
            }
            sb.append("GROUP BY ");
            sb.append("     1, 2, 3 ");
            sb.append("ORDER BY ");
            sb.append("      3 ASC ");
        });

        while (rs.next()) {
            indices.add(new String[]{rs.getString(1).trim(), rs.getString(2).trim()});
        }

        return indices;
    }

    /**
     * Gera e adiciona o drop para o trigger
     *
     * @param nome String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLDropTrigger(String nome, LinkedHashSet<String> set) {
        set.add("DROP TRIGGER \"" + nome + "\"");
    }

    /**
     * Gera e adiciona o drop para o index
     *
     * @param nome String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLDropIndex(String nome, LinkedHashSet<String> set) {
        set.add("DROP INDEX \"" + nome + "\"");
    }

    /**
     * Gera e adiciona o ddl para fazer a coluna retornar null no seu computed
     * by
     *
     * @param tabela String
     * @param coluna String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLEmptyComputedBy(String tabela, String coluna, LinkedHashSet<String> set) {
        set.add("ALTER TABLE  \"" + tabela + "\" alter  \"" + coluna + "\" COMPUTED BY (null);");

    }

    /**
     * Gera e adiciona o ddl para fazer a view rotornar 0 em todas as colunas.
     *
     * Isso se faz necessário para fugir das dependências da view
     *
     * @param nome String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLEmptyView(String nome, LinkedHashSet<String> set, CSPInstrucoesSQLBase conn) throws Exception {

        final ArrayList<String> colunasNomes = getColunasNomes(conn, nome);
        final StringBuilder sb = new StringBuilder();
        sb.append("alter view \"");
        sb.append(nome);
        sb.append("\" (");
        for (String colunasNome : colunasNomes) {
            sb.append(colunasNome);
            sb.append(",");

        }
        sb.append(") as select ");
        for (String colunasNome : colunasNomes) {
            sb.append("0,");
        }
        sb.append(" from  RDB$DATABASE");

        set.add(sb.toString().replace(",)", ")").replace(", ", " "));
        colunasNomes.clear();
    }

    /**
     * Gera e adiciona o drop para a view
     *
     * @param nome String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLDropView(String nome, LinkedHashSet<String> set) {
        set.add("DROP VIEW \"" + nome + "\"");
    }

    /**
     * Gera e adiciona o drop para a procedure
     *
     * @param nome String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLDropProcedure(String nome, LinkedHashSet<String> set) {
        set.add("DROP PROCEDURE \"" + nome + "\"");
    }

    /**
     * Gera e adiciona o drop para a tabela
     *
     * @param nome String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLDropTable(String nome, LinkedHashSet<String> set) {
        set.add("DROP TABLE " + nome);
    }

    /**
     * Gera e adiciona o drop para a exception
     *
     * @param nome String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLDropException(String nome, LinkedHashSet<String> set) {
        set.add("DROP EXCEPTION \"" + nome + "\"");
    }

    /**
     * Gera e adiciona o drop para o index
     *
     * @param nome String
     * @param tabela String
     * @param set LinkedHashSet<String>
     */
    public static void putDDLDropConstraint(String nome, String tabela, LinkedHashSet<String> set) {
        set.add("ALTER TABLE " + tabela + " DROP CONSTRAINT \"" + nome + "\"");
    }

    /**
     * Gera e adiciona os ddl necessários para corrigir a diferença entre as
     * tabelas
     *
     * @param table String
     * @param original CSPInstrucoesSQLBase
     * @param nova CSPInstrucoesSQLBase
     * @param exportedDllOriginal
     * @param exportedDllNova
     * @param set LinkedHashSet<String>
     * @throws java.lang.Exception
     */
    public static void putDDLDiferencaTable(String table, CSPInstrucoesSQLBase original, CSPInstrucoesSQLBase nova, String exportedDllOriginal, String exportedDllNova, LinkedHashSet<String> set) throws Exception {

        final ArrayList<ModelColunaTabela> tabelasNaBaseOriginal = getColunas(table, original);
        final ArrayList<ModelColunaTabela> tabelasNaBaseASerAtualizada = getColunas(table, nova);

        boolean teveAlteracao = tabelasNaBaseOriginal.size() != tabelasNaBaseASerAtualizada.size();

        tabelasNaBaseOriginal.removeIf(t -> t.isComputedBy());
        tabelasNaBaseASerAtualizada.removeIf(t -> t.isComputedBy());

        if (!teveAlteracao) {
            for (ModelColunaTabela ori : tabelasNaBaseOriginal) {

                if (teveAlteracao) {
                    break;
                }

                boolean faltaColunaNaNova = true;

                for (ModelColunaTabela nw : tabelasNaBaseASerAtualizada) {
                    if (nw.getNome().equals(ori.getNome())) {
                        faltaColunaNaNova = false;
                        if (nw.getEscala() != ori.getEscala()
                                || nw.getTamanho() != ori.getTamanho()
                                || nw.isForeignKey() != ori.isForeignKey()
                                || nw.isPrimaryKey() != ori.isPrimaryKey()
                                || nw.isNotNull() != ori.isNotNull()
                                || nw.isUnique() != ori.isUnique()) {
                            teveAlteracao = true;

                        }

                        break;
                    }
                }

                if (!teveAlteracao) {
                    // Falta coluna na tabela nova
                    teveAlteracao = faltaColunaNaNova;

                }
            }
        }

        if (teveAlteracao) {

            final LinkedHashSet<String> tmp = new LinkedHashSet<>();

            putDDLCreateTables(tmp, exportedDllOriginal);

            if (nova.count(table, null) == 0) {
                /**
                 * Como não existe nada na tabela basta recriar a mesma
                 */
                for (String sql : tmp) {
                    if (sql != null && sql.startsWith("CREATE TABLE " + table + " ")) {
                        putDDLDropTable(table, set);
                        set.add(sql);
                        break;
                    }

                }

            } else {
                /**
                 * Para manter a integridade da base o processo de ajuste em
                 * tabelas povoadas é mais elaborado que de uma limpa
                 */

                final String newNameTmp = "ALT_" + CSPUtilidadesLang.substring(CSPUtilidadesLang.getMd5(table + CSPUtilidadesLangDateTime.getTempoCompletoLimpo()), 0, 20);

                for (String sql : tmp) {
                    if (sql != null && sql.startsWith("CREATE TABLE " + table + " ")) {

                        boolean isCancelada = false;
                        final StringBuilder sb = new StringBuilder();
                        sb.append("insert into ");
                        sb.append(newNameTmp);
                        sb.append(" (");

                        for (int i = 0; i < tabelasNaBaseOriginal.size(); i++) {
                            sb.append(tabelasNaBaseOriginal.get(i).getNome());
                            if (i < tabelasNaBaseOriginal.size() - 1) {
                                sb.append(",");
                            }
                        }

                        sb.append(") select ");

                        for (int i = 0; i < tabelasNaBaseOriginal.size(); i++) {
                            ModelColunaTabela nwTab = tabelasNaBaseOriginal.get(i);
                            boolean existeNaOld = false;
                            for (ModelColunaTabela oldTab : tabelasNaBaseASerAtualizada) {
                                if (oldTab.getNome().equals(nwTab.getNome())) {

                                    existeNaOld = true;

                                    if (nwTab.isNotNull() && !oldTab.isNotNull()) {
                                        sb.append("coalesce(");
                                    }

                                    if ((nwTab.getTipo().equals("VARCHAR")
                                            || nwTab.getTipo().equals("CHAR"))
                                            && nwTab.getTamanho() < oldTab.getTamanho()) {
                                        sb.append("SUBSTRING(");
                                        sb.append(nwTab.getNome());
                                        sb.append(" FROM 1 FOR ");
                                        sb.append(nwTab.getTamanho());
                                        sb.append(")");
                                    } else {
                                        sb.append(nwTab.getNome());
                                    }

                                    if (nwTab.isNotNull() && !oldTab.isNotNull()) {
                                        sb.append(",0)");
                                    }

                                    break;
                                }
                            }

                            if (!existeNaOld) {
                                if (nwTab.isNotNull()) {
                                    if (nwTab.isForeignKey()) {
                                        /**
                                         * Se é fk NÂO podemos trabalhar com um
                                         * valor default.
                                         *
                                         * Sendo assim não existe o que fazer
                                         * para tratar, nos resta descartar a
                                         * tabela e a criar novamente
                                         */
                                        putDDLDropTable(table, set);
                                        set.add(sql);
                                        isCancelada = true;
                                        break;
                                    }

                                    switch (nwTab.getTipo()) {

                                        case "TIMESTAMP":
                                            sb.append("'01.01.1990, 00:00:00.000'");
                                            break;
                                        case "DATE":
                                            sb.append("'01.01.1990'");
                                            break;
                                        case "TIME":
                                            sb.append("'00:00:00.000'");
                                            break;
                                        case "BOOLEAN":
                                            sb.append(false);
                                            break;
                                        default:
                                            sb.append("1");
                                    }
                                } else {
                                    sb.append("null");
                                }
                            }

                            if (i < tabelasNaBaseOriginal.size() - 1) {
                                sb.append(",");
                            }
                        }
                        sb.append(" from ");
                        sb.append(table);

                        if (isCancelada) {
                            break;
                        }

                        //Cria tabela temporária já na nova estrutura
                        set.add(sql.replace("CREATE TABLE " + table + " ", "CREATE TABLE " + newNameTmp + " "));

                        //Realoca para ela as informações
                        set.add(sb.toString());

                        //Remove a tabela original
                        putDDLDropTable(table, set);

                        //Cria a tabela definifiva já na nova estrutura
                        set.add(sql);

                        //Realoca para ela as informações
                        set.add("insert into " + table + " select * from " + newNameTmp);

                        //Remove a tabela temporária
                        putDDLDropTable(newNameTmp, set);

                        break;
                    }

                }

            }
            tmp.clear();
        }

        tabelasNaBaseOriginal.clear();
        tabelasNaBaseASerAtualizada.clear();

    }

    /**
     * Exporta o ddl de alteração dos campos computedBy da base
     *
     * @param to LinkedHashSet<String>
     * @param exportedDll exportedDll
     * @throws java.lang.Exception
     */
    public static void putDDLCreateComputedsBy(LinkedHashSet<String> to, String exportedDll) throws Exception {
        final ArrayList<String> jaGerado = new ArrayList<>();
        /**
         * Como provemos suporte para mais de uma versão do firebird e de SO
         * precisamos cobrir todas as variações necessárias
         */
        {//Formato 1
            findAndPutCommandsFromExportedDDL(to,
                    exportedDll,
                    "(?m)(^\\/\\* Computed.*)",
                    "(?m)^\\/\\*",
                    "(?m)^\\/\\*",
                    (String val) -> {

                        if (val == null || !val.contains("COMPUTED BY")) {
                            return null;
                        }

                        val = val.replace("\n", "").replaceAll("\\s+", " ").trim();

                        final String table = val.replaceAll("(?i)((ALTER TABLE ))", "").replaceAll("(?i)((ALTER).*)", "").trim();
                        final String col = val.replaceAll("(?i)((ALTER TABLE) \\w+ \\w+)", "").replaceAll("(?i)((type).*)", "").trim();
                        jaGerado.add(table);
                        final StringBuilder sb = new StringBuilder();
                        sb.append("EXECUTE block as ");
                        sb.append("BEGIN ");
                        sb.append("    if (exists( ");
                        sb.append("        select 1 from RDB$RELATION_FIELDS rf ");
                        sb.append("            where rf.RDB$RELATION_NAME = '").append(table).append("' and rf.RDB$FIELD_NAME = '").append(col).append("')) ");
                        sb.append("    then ");
                        sb.append("        execute statement '").append(val).append("'; ");
                        sb.append("    else  ");
                        val = val.replace(" ALTER ", " ADD ").replace(" TYPE ", " ");
                        sb.append("        execute statement '").append(val).append("'; ");
                        sb.append("END ");

                        return sb.toString();
                    }
            );
        }

        {//Formato 2
            final Matcher nomesTabelas = Pattern.compile("(?m)(^\\/\\* Table.*)").matcher(exportedDll);

            while (nomesTabelas.find()) {

                findAndPutCommandsFromExportedDDL(to,
                        exportedDll,
                        "(?m)^\\/\\* Table: " + nomesTabelas.group().split("^\\/\\* Table: ")[1].split(",")[0],
                        "(?m)^\\/\\* ",
                        ";",
                        (String val) -> {
//                            if (val.contains("CREATE TABLE") && val.contains("COMPUTED BY")) {
                            if (val.contains("CREATE TABLE") && val.contains("COMPUTED BY (")) {

                                final String table = val.replaceAll("\r|\n", "").replaceAll("(?i)(.*?TABLE |\\(.*)", "").trim();

                                if (jaGerado.contains(table)) {
                                    return null;
                                }

                                for (String s : val.split("\r\n")) {
                                    if (s.contains("COMPUTED BY")) {

                                        final String col = s.trim().replaceAll(" .*", "").trim();
                                        final String aux = s.trim().replaceAll("\\),", "\\)").trim();

                                        final StringBuilder sb = new StringBuilder();
                                        sb.append("EXECUTE block as ");
                                        sb.append("BEGIN ");
                                        sb.append("    if (exists( ");
                                        sb.append("        select 1 from RDB$RELATION_FIELDS rf ");
                                        sb.append("            where rf.RDB$RELATION_NAME = '").append(table).append("' and rf.RDB$FIELD_NAME = '").append(col).append("')) ");
                                        sb.append("    then ");
                                        sb.append("        execute statement 'ALTER TABLE ").append(table).append(" alter ").append(aux).append("'; ");
                                        sb.append("    else  ");
                                        sb.append("        execute statement 'ALTER TABLE ").append(table).append(" ADD ").append(aux).append("'; ");
                                        sb.append("END ");

                                        to.add(sb.toString());
                                    }
                                }
                            }
                            return null;
                        }
                );
            }
        }

    }

    private interface OnPut {

        public String run(String val) throws Exception;
    }

}
