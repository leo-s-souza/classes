/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.modelos.ModelColunaTabela;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.getColunas;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Utilidades para o proceso de exportação de dml de um base
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 15:59:07
 */
public abstract class CSPUtilidadesLangInstrucoesSQLExportaDML extends CSPUtilidadesLangInstrucoesSQL {

    /**
     * Adiciona na lista os inserts gerados do processo de extração de todos os
     * valores da tabela
     *
     * @param conn CSPInstrucoesSQLBase
     * @param table String
     * @param list LinkedHashSet<String>
     * @throws Exception
     */
    public static void putDMLFullTable(CSPInstrucoesSQLBase conn, String table, LinkedHashSet<String> list) throws Exception {

        if (conn.count(table, null) == 0) {
            return;
        }

        StringJoiner fields = new StringJoiner(", ");

        ResultSet rs;

        rs = conn.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("      TRIM(RRF.RDB$FIELD_NAME) AS CAMPO ");
            sb.append("FROM ");
            sb.append("      RDB$RELATION_FIELDS RRF ");
            sb.append("WHERE ");
            sb.append("      RRF.RDB$RELATION_NAME NOT STARTING WITH 'RDB$' ");
            sb.append("AND ");
            sb.append("      RRF.RDB$RELATION_NAME = ? ");
            sb.append("ORDER BY ");
            sb.append("      RRF.RDB$FIELD_POSITION");
        }, table);

        while (rs.next()) {
            fields.add("\"" + rs.getString(1) + "\"");
        }

        if (fields.length() > 0) {
            rs = conn.select("SELECT " + fields + " FROM " + table);

            if (rs != null) {

                while (rs.next()) {

                    StringBuilder insert = new StringBuilder();
                    insert.append("INSERT INTO ").append(table).append(" (").append(fields).append(") VALUES (");

                    for (int x = 1; x <= rs.getMetaData().getColumnCount(); x++) {

                        if (x != 1) {
                            insert.append(", ");
                        }

                        if (rs.getString(x) == null || rs.getString(x).equals("null")) {
                            insert.append("null");
                        } else {
                            insert.append("'").append(rs.getString(x).replace("'", "''")).append("'");
                        }
                    }

                    insert.append(");").append(CSPUtilidadesSO.LINE_SEPARATOR);

                    list.add(insert.toString());
                }
            }
        }

    }

    /**
     * Adiciona na lista o comando para atualizar o generator da base
     *
     * @param conn CSPInstrucoesSQLBase
     * @param generator String
     * @param list LinkedHashSet<String>
     * @throws Exception
     */
    public static void putDMLGeneratorValue(CSPInstrucoesSQLBase conn, String generator, LinkedHashSet<String> list) throws Exception {

        ResultSet select = conn.selectOneRow("select GEN_ID(" + generator + ", 0) from RDB$DATABASE");

        if (select != null) {
            list.add("SET GENERATOR " + generator + " TO " + select.getLong(1) + ";");
        } else {
            list.add("SET GENERATOR " + generator + " TO 0;");
        }

    }

    /**
     * Gera e adiciona na lista o DML necessário para corrigir a diferença de
     * informações na tabela
     *
     * @param table String
     * @param original CSPInstrucoesSQLBase
     * @param nova CSPInstrucoesSQLBase
     * @param forceDeleteDependentes boolean
     * @param set LinkedHashSet<String>
     * @throws Exception
     */
    public static void putDMLDiferencaTable(String table, CSPInstrucoesSQLBase original, CSPInstrucoesSQLBase nova, boolean forceDeleteDependentes, LinkedHashSet<String> set) throws Exception {

        final ArrayList<ModelColunaTabela> tabelasA = getColunas(table, nova);
        final ArrayList<ModelColunaTabela> tabelasB = getColunas(table, original);

        final LinkedHashSet<String> valorPkA = new LinkedHashSet<>();
        final LinkedHashSet<String> valorPkB = new LinkedHashSet<>();

        ArrayList<HashMap<String, Object>> arrayA;
        ArrayList<HashMap<String, Object>> arrayB;

        /**
         * Pegas os campos e os valores da tabela da baseA
         */
        arrayA = nova.selectInMap("SELECT * FROM " + table);

        for (HashMap<String, Object> hashA1 : arrayA) {
            StringJoiner temp = new StringJoiner("; ");

            for (Map.Entry<String, Object> entrySet : hashA1.entrySet()) {
                String key = entrySet.getKey();
                Object value = entrySet.getValue();

                for (ModelColunaTabela col : tabelasA) {
                    if (col.isPrimaryKey() && col.getNome().equals(key)) {
                        temp.add(key + " = '" + value + "'");
                        break;
                    }
                }
            }

            hashA1.put("KEY_GOLD", temp.toString());
            valorPkA.add(temp.toString());
        }

        /**
         * Pegas os campos e os valores da tabela da baseB
         */
        arrayB = original.selectInMap("SELECT * FROM " + table);

        for (HashMap<String, Object> hashB1 : arrayB) {
            StringJoiner temp = new StringJoiner("; ");

            for (Map.Entry<String, Object> entrySet : hashB1.entrySet()) {
                String key = entrySet.getKey();
                Object value = entrySet.getValue();

                for (ModelColunaTabela col : tabelasB) {
                    if (col.isPrimaryKey() && col.getNome().equals(key)) {
                        temp.add(key + " = '" + value + "'");
                        break;
                    }
                }
            }

            hashB1.put("KEY_GOLD", temp.toString());
            valorPkB.add(temp.toString());
        }

        //Delete
        for (String valor : valorPkA) {
            if (!valorPkB.contains(valor)) {
                if (valor.trim().isEmpty()) {

                    set.add("DELETE FROM " + table.trim());
                } else {
                    set.add("DELETE FROM " + table.trim() + " WHERE " + valor.replace("; ", " AND "));
                    if (forceDeleteDependentes) {
                        final ArrayList<String> tmp = new ArrayList<>();

                        trataDeletesDependentes(nova, table, valor, tmp);

                        /**
                         * Inverte a ordem dos DELETES DOS DEPENDENTES para que
                         * possa deletar de forma correta as tabelas com
                         * ligações à outras.
                         */
                        Collections.reverse(tmp);

                        set.addAll(tmp);

                        tmp.clear();
                    }
                }
            }
        }

        // Update
        for (HashMap<String, Object> hashB1 : arrayB) {
            boolean existe = false;
            Object pks = null;

            for (HashMap<String, Object> hashA1 : arrayA) {
                pks = hashB1.get("KEY_GOLD");

                if (hashA1.containsValue(pks)) {
                    existe = true;
                    StringJoiner colunaValor = new StringJoiner(", ");

                    for (Map.Entry<String, Object> entrySetB : hashB1.entrySet()) {
                        String keyB = entrySetB.getKey();
                        Object valueB = entrySetB.getValue();

                        if (valueB != null && valueB.toString().contains("'")) {
                            valueB = valueB.toString().replace("'", "''");
                        }

                        Object tempB = valueB == null ? "null" : ("'" + valueB + "'").replaceAll("\\s+", " ");

                        for (Map.Entry<String, Object> entrySetA : hashA1.entrySet()) {
                            String keyA = entrySetA.getKey();
                            Object valueA = entrySetA.getValue();

                            if (valueA != null && valueA.toString().contains("'")) {
                                valueA = valueA.toString().replace("'", "''");
                            }

                            Object tempA = valueA == null ? "null" :("'" + valueA + "'").replaceAll("\\s+", " ");
                            if (keyA.equals(keyB) && !tempA.equals(tempB)) {
//                            System.out.println(keyA+"=>"+tempA+"<>"+tempB);
//                            System.out.println(tempA.toString().trim().equals(tempB.toString().trim()));

                                colunaValor.add(keyA + " = " + tempB);
                            }
                        }
                    }

                    if (colunaValor.length() > 0) {
                        if (pks.toString().trim().isEmpty()) {
                            set.add("UPDATE " + table + " SET " + colunaValor.toString());

                        } else {
                            set.add("UPDATE " + table + " SET " + colunaValor.toString() + " WHERE " + pks.toString().replace("; ", " AND "));
                        }
                    }
                }
            }

            // Insert
            if (!existe) {
                ArrayList<String> fields = new ArrayList<>();
                ArrayList<Object> valores = new ArrayList<>();
                hashB1.remove("KEY_GOLD");

                for (Map.Entry<String, Object> entrySetB : hashB1.entrySet()) {
                    String keyB = entrySetB.getKey();
                    Object valueB = entrySetB.getValue();

                    /**
                     * Quando o valor tem apóstrofo (') temos que duplica-lo,
                     * pra não dar problema no SQL, já que Strings são definidas
                     * por (');
                     */
                    if (valueB != null && valueB.toString().contains("'")) {
                        valueB = valueB.toString().replace("'", "''");
                    }

                    Object temp = valueB == null ? "null" : "'" + valueB + "'";

                    fields.add(keyB);
                    valores.add(temp);
                }

                StringBuilder insert = new StringBuilder();

                insert.append("INSERT INTO ")
                        .append(table)
                        .append(" (")
                        .append(fields.toString().replace("[", "").replace("]", ""))
                        .append(") VALUES (");

                for (Object valor : valores) {
                    insert.append(valor.toString()).append(", ");
                }

                insert.delete(insert.length() - 2, insert.length());
                insert.append(");");

                set.add(insert.toString());
            }
        }

        tabelasA.clear();
        tabelasB.clear();
        valorPkA.clear();
        valorPkB.clear();
        arrayA.clear();
        arrayB.clear();

    }

    /**
     * Verifica se a tabela tem dependentes. Se tiver, avalia se existe algum
     * registro que usa o valor da PK e cria o comando de DELETE para este.
     *
     * @param conn - Conexão com a base que será alterada.
     * @param tabela - Tabela que será verificada se tem dependentes.
     * @param valor - Valor da(s) chave(s).
     *
     * @throws Exception
     */
    private static void trataDeletesDependentes(CSPInstrucoesSQLBase conn, String tabela, String valor, ArrayList<String> set) throws Exception {
        ArrayList<HashMap<String, Object>> deps;

        deps = conn.selectInMap((StringBuilder sb) -> {
            sb.append("SELECT LIST(TRIM(s.RDB$FIELD_NAME), ',') AS CAMPO, i.RDB$RELATION_NAME AS TABELA, LIST(TRIM(s2.RDB$FIELD_NAME), ',') AS CAMPO_REFERENCIADO");
            sb.append(" FROM RDB$INDEX_SEGMENTS s");
            sb.append(" LEFT JOIN RDB$INDICES i ON i.RDB$INDEX_NAME = s.RDB$INDEX_NAME");
            sb.append(" LEFT JOIN RDB$RELATION_CONSTRAINTS rc ON rc.RDB$INDEX_NAME = s.RDB$INDEX_NAME");
            sb.append(" LEFT JOIN RDB$REF_CONSTRAINTS refc ON rc.RDB$CONSTRAINT_NAME = refc.RDB$CONSTRAINT_NAME");
            sb.append(" LEFT JOIN RDB$RELATION_CONSTRAINTS rc2 ON rc2.RDB$CONSTRAINT_NAME = refc.RDB$CONST_NAME_UQ");
            sb.append(" LEFT JOIN RDB$INDICES i2 ON i2.RDB$INDEX_NAME = rc2.RDB$INDEX_NAME ");
            sb.append(" LEFT JOIN RDB$INDEX_SEGMENTS s2 ON i2.RDB$INDEX_NAME = s2.RDB$INDEX_NAME ");
            sb.append(" WHERE rc.RDB$CONSTRAINT_TYPE IS NOT NULL AND  i2.RDB$RELATION_NAME = ? ");
            sb.append(" AND s.RDB$FIELD_POSITION = s2.RDB$FIELD_POSITION");
            sb.append(" GROUP BY i.RDB$RELATION_NAME");
        }, tabela);

        for (HashMap<String, Object> dep : deps) {
            LinkedHashMap<String, String> tabelaCampo = new LinkedHashMap<>();
            String[] camposRef = null;

            String cam = (String) dep.get("CAMPO_REFERENCIADO");
            String tab = (String) dep.get("TABELA");

            if (cam.contains(valor.split(" = ")[0])) {
                trataDeletesDependentes(conn, tab, valor, set);

                if (cam.contains(",")) {
                    camposRef = cam.split(",");
                }

                if (camposRef != null) {
                    for (String camp : camposRef) {
                        tabelaCampo.put(tab, camp);
                    }
                } else {
                    tabelaCampo.put(tab, cam);
                }

                for (Map.Entry<String, String> entrySet : tabelaCampo.entrySet()) {
                    String table = entrySet.getKey();
                    String campo = entrySet.getValue();

                    ResultSet select = conn.select("SELECT " + campo + " FROM " + table + " WHERE " + valor.replace("; ", " AND "));

                    if (select.next()) {
                        set.add("DELETE FROM " + table.trim() + " WHERE " + valor.replace("; ", " AND "));
                    }
                }
            }
        }
        deps.clear();
    }

}
