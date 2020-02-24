/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL.hasColumn;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe base para importação manual de informações
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 25/08/2016 - 08:09:01
 */
public abstract class CSPImportacaoManual {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-dd-MM");

    /**
     * Auxilia no mapeamento de pastas a procura de imagens para os registros
     * importados
     *
     * @param arr ArrayList<CSPArquivos>
     * @param toMap CSPArquivos
     * @throws Exception
     */
    protected void mapeiaPastas(ArrayList<CSPArquivos> arr, CSPArquivos toMap) throws Exception {
        for (CSPArquivos map : toMap.listFiles()) {

            if (map.isDir()) {
                this.mapeiaPastas(arr, map);
            } else {
                arr.add(map);
            }
        }
    }

    /**
     * Percorre um array mapeado a procura do arquivo requisitado, copia-o e
     * retorna o nome do mesmo
     *
     * @param arr ArrayList<CSPArquivos>
     * @param destino CSPArquivos
     * @param find String
     * @throws Exception
     */
    protected String copyAux(ArrayList<CSPArquivos> arr, CSPArquivos destino, String find) throws Exception {
        if (find == null || find.trim().isEmpty()) {
            return null;
        }
        final String bkPath = destino.getAbsolutePath();
        String r = null;
        for (CSPArquivos m : arr) {
            if (m.getName().toLowerCase().startsWith(find.toLowerCase())) {
                destino.setPath(bkPath + "/" + m.getName());
                destino.delete();

                m.copy(destino);

                r = destino.getName();
                break;
            }
        }
        destino.setPath(bkPath);
        return r;
    }

    /**
     * Realiza a limpeza da string
     *
     * @param rs ResultSet - ResultSet com as informações
     * @param key String - Key da informação no resultset
     * @param regex String - Expressão regular para limpar a string
     * @param valDefault String - Valor default caso o campo do retorno for
     * null/em branco
     * @return
     * @throws java.sql.SQLException
     */
    protected String sanitizeText(ResultSet rs, String key, String regex, String valDefault) throws SQLException {
        if (hasColumn(rs, key)) {
            String val = rs.getString(key);
            if (val != null && !val.trim().isEmpty()) {
                if (regex != null) {
                    val = val.replaceAll(regex, "");
                }
                if (val != null && !val.trim().isEmpty()) {
                    return val;
                }
            }
        }
        return valDefault;
    }

    /**
     * Realiza a limpeza da string e já a joga para o novo map
     *
     * @param rs ResultSet - ResultSet com as informações
     * @param dest ashMap<String, Object> - Mapa de destino das informações
     * @param key String - Key da informação no resultset
     * @param regex String - Expressão regular para limpar a string
     * @param valDefault String - Valor default caso o campo do retorno for
     * null/em branco
     * @throws java.sql.SQLException
     */
    protected void sanitizeTextAndPut(ResultSet rs, HashMap<String, Object> dest, String key, String regex, String valDefault) throws SQLException {
        dest.put(key, this.sanitizeText(rs, key, regex, valDefault));
    }

    /**
     * Realiza a limpeza da string
     *
     * @param rs ashMap<String, Object> - Mapa com as informações originais
     * @param key String - Key da informação no resultset
     * @param regex String - Expressão regular para limpar a string
     * @param valDefault String - Valor default caso o campo do retorno for
     * null/em branco
     * @return
     * @throws java.sql.SQLException
     */
    protected String sanitizeText(HashMap<String, Object> rs, String key, String regex, String valDefault) throws SQLException {
        if (rs.containsKey(key)) {

            Object valObject = rs.get(key);
            String val = null;
            if (valObject != null) {
                if (valObject instanceof java.sql.Date) {
                    val = this.sdf.format((java.sql.Date) valObject);
                } else {
                    val = String.valueOf(valObject);
                }
            }

            if (val != null && !val.trim().isEmpty()) {
                if (regex != null) {
                    val = val.replaceAll(regex, "");
                }
                if (val != null && !val.trim().isEmpty()) {
                    return val;
                }
            }
        }
        return valDefault;
    }

    /**
     * Realiza a limpeza da string e já a joga para o novo map
     *
     * @param rs ashMap<String, Object> - Mapa com as informações originais
     * @param dest ashMap<String, Object> - Mapa de destino das informações
     * @param key String - Key da informação no resultset
     * @param regex String - Expressão regular para limpar a string
     * @param valDefault String - Valor default caso o campo do retorno for
     * null/em branco
     * @throws java.sql.SQLException
     */
    protected void sanitizeTextAndPut(HashMap<String, Object> rs, HashMap<String, Object> dest, String key, String regex, String valDefault) throws SQLException {
        dest.put(key, this.sanitizeText(rs, key, regex, valDefault));
    }

    /**
     * Gera um novo código para a coluna na tabela
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com a base
     * @param table String - Tabela
     * @param column String - Coluna
     * @param toPad int - Números de digitos para ser gerado o 'zero a
     * esquerda'. -1 desabilita
     * @param where String - Codição para gerar o novo ID
     * @param whereVals Object - Valores para a condição
     * @return
     */
    protected String buildNewId(CSPInstrucoesSQLBase conn, String table, String column, int toPad, String where, Object... whereVals) throws SQLException {

        int max = conn.max(table, column, where, whereVals) + 1;

        if (toPad > 0) {
            return CSPUtilidadesLang.pad(String.valueOf(max), toPad, "0");
        }

        return String.valueOf(max);
    }

    /**
     * Gera um novo código para a coluna na tabela
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com a base
     * @param table String - Tabela
     * @param column String - Coluna
     * @param toPad int - Números de digitos para ser gerado o 'zero a
     * esquerda'. -1 desabilita
     * @return
     */
    protected String buildNewId(CSPInstrucoesSQLBase conn, String table, String column, int toPad) throws SQLException {
        return this.buildNewId(conn, table, column, toPad, null, new Object[0]);
    }

}
