/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL.hasColumn;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Métodos de auxilio para linguagem no contexto de json
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 27/10/2016 - 17:13:17
 */
public abstract class CSPUtilidadesLangJson extends CSPUtilidadesLang {

    /**
     * Verifica se a key possui valor no JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @return boolean
     */
    public static boolean checkKeyJson(JSONObject o, String key) {
        return o != null && o.has(key) && !o.isNull(key);
    }

    /**
     * Verifica se a key possui valor no JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key String - Key de identificação
     * @return boolean
     */
    public static boolean checkKeyJson(JSONArray o, int key) {
        return o != null && o.length() >= key && !o.isNull(key);
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty String - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return String
     */
    public static String getFromJson(JSONObject o, String key, String valIfEmpty) {
        if (checkKeyJson(o, key) && !o.getString(key).trim().isEmpty()) {
            return o.getString(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty int - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static int getFromJson(JSONObject o, String key, int valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getInt(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty long - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static long getFromJson(JSONObject o, String key, long valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getLong(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty double - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static double getFromJson(JSONObject o, String key, double valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getDouble(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty Object - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static Object getFromJson(JSONObject o, String key, Object valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.get(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty boolean - Valor caso não encontrado valor ou o mesmo
     * for inválido
     * @return int
     */
    public static boolean getFromJson(JSONObject o, String key, boolean valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getBoolean(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty JSONObject - Valor caso não encontrado valor ou o mesmo
     * for inválido
     * @return int
     */
    public static JSONObject getFromJson(JSONObject o, String key, JSONObject valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getJSONObject(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONObject - Json a ser lido
     * @param key String - Key de identificação
     * @param valIfEmpty JSONArray - Valor caso não encontrado valor ou o mesmo
     * for inválido
     * @return int
     */
    public static JSONArray getFromJson(JSONObject o, String key, JSONArray valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getJSONArray(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty String - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return String
     */
    public static String getFromJson(JSONArray o, int key, String valIfEmpty) {
        if (checkKeyJson(o, key) && !o.getString(key).trim().isEmpty()) {
            return o.getString(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty int - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static int getFromJson(JSONArray o, int key, int valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getInt(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty long - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static long getFromJson(JSONArray o, int key, long valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getLong(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty double - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static double getFromJson(JSONArray o, int key, double valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getDouble(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty Object - Valor caso não encontrado valor ou o mesmo for
     * inválido
     * @return int
     */
    public static Object getFromJson(JSONArray o, int key, Object valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.get(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty boolean - Valor caso não encontrado valor ou o mesmo
     * for inválido
     * @return int
     */
    public static boolean getFromJson(JSONArray o, int key, boolean valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getBoolean(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty JSONObject - Valor caso não encontrado valor ou o mesmo
     * for inválido
     * @return int
     */
    public static JSONObject getFromJson(JSONArray o, int key, JSONObject valIfEmpty) {
        if (o != null && o.length() >= key && !o.isNull(key)) {
            return o.getJSONObject(key);
        }
        return valIfEmpty;
    }

    /**
     * Método indicado para realizar a leitura de dados de um JSON
     *
     * @param o JSONArray - Json a ser lido
     * @param key int - Key de identificação
     * @param valIfEmpty JSONArray - Valor caso não encontrado valor ou o mesmo
     * for inválido
     * @return int
     */
    public static JSONArray getFromJson(JSONArray o, int key, JSONArray valIfEmpty) {
        if (checkKeyJson(o, key)) {
            return o.getJSONArray(key);
        }
        return valIfEmpty;
    }

    /**
     *
     * @param teste String - Verifica se a String é um JSON válido
     * @return boolean
     */
    public static boolean isJson(String teste) {
        if (teste != null) {
            try {
                new JSONObject(teste);
            } catch (JSONException ex) {
                try {
                    new JSONArray(teste);
                } catch (JSONException ex1) {
                    return false;
                }
            }
            return true;
        } else {
            return false;

        }

    }

    /**
     * Converte de forma centralizada uma string para um JSONObject
     *
     * @param c String
     * @return
     * @throws IOException
     */
    public static JSONObject getObject(String c) throws IOException {
        if (isJson(c)) {
            return new JSONObject(c);
        } else {
            return null;
        }
    }

    /**
     * Converte de forma centralizada uma string para um JSONArray
     *
     * @param c String
     * @return
     * @throws IOException
     */
    public static JSONArray getArray(String c) throws IOException {

        if (isJson(c)) {
            if (c.startsWith("{") && c.endsWith("}")) {
                c = "[" + c + "]";
            }
            return new JSONArray(c);
        } else {
            return null;
        }
    }

    /**
     * Reordena o JSONArray de JSONObject
     *
     * @param arr JSONArray - JSONArray a ser trabalhado
     * @param keyInSubObject String - Key onde encontra-se o valor a ser
     * comparado
     * @param isNumeric boolean - Se o valor é númerico
     * @param inAsc boolean - Se os valores devem ser ordenados de forma
     * crescente, caso contrário irá ser decrescente
     * @return
     */
    public static JSONArray sortJsonArray(JSONArray arr, String keyInSubObject, boolean isNumeric, boolean inAsc) {
        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            jsonValues.add(arr.getJSONObject(i));
        }

        Collections.sort(jsonValues, (JSONObject a, JSONObject b) -> {
            if (isNumeric) {

                final double aa = getFromJson(a, keyInSubObject, 999999999);
                final double bb = getFromJson(b, keyInSubObject, 999999999);
                if (aa == bb) {
                    return 0;
                }

                return aa < bb ? -1 : 1;
            }

            return getFromJson(a, keyInSubObject, "zzzzzzzzz").compareTo(getFromJson(b, keyInSubObject, "zzzzzzzzz"));
        });

        if (!inAsc) {
            Collections.reverse(jsonValues);
        }

        for (int i = 0; i < arr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }
        return sortedJsonArray;
    }

    /**
     * Adiciona um JSONObject em um JSONObject. O método 'mescla' as keys
     *
     * @param old JSONObject - Antigo JSONObject
     * @param obj JSONObject - JSONObject a ser adicionado
     * @return
     */
    public static JSONObject appendObject(JSONObject old, JSONObject obj) {
        
        if (old == null) {
            old = new JSONObject();
        }
       
        if (obj.names() != null) {

            for (Object n : obj.names()) {
                old.put((String) n, obj.get((String) n));
            }
        }
        
        return old;
    }

    /**
     * Adiciona um JSONArray em um JSONArray. O método acrescenta no final do
     * JSONArray as 'keys' do novo
     *
     * @param old JSONArray - Antigo JSONArray
     * @param arr JSONArray - JSONArray a ser adicionado
     * @return
     */
    public static JSONArray appendArray(JSONArray old, JSONArray arr) {
        if (old == null) {
            old = new JSONArray();
        }
        for (int i = 0; i < arr.length(); i++) {
            old.put(arr.get(i));
        }
        return old;
    }

    /**
     * Auxilia no processo de 'put' de valores de um ResultSet para um
     * JSONObject
     *
     * @param key String - Key no ResultSet/JSONObject
     * @param rs ResultSet - ResultSet(input)
     * @param j JSONObject - JSONObject(output)
     * @param valorDefault Object - Valor de default caso não exista no
     * resultSet ou venha nulo
     * @throws SQLException
     */
    public static void resultSetToJson(String key, ResultSet rs, JSONObject j, Object valorDefault) throws SQLException {
        resultSetToJson(key, rs, key, j, valorDefault);
    }

    /**
     * Auxilia no processo de 'put' de valores de um ResultSet para um
     * JSONObject
     *
     * @param key String - Key no ResultSet
     * @param rs ResultSet - ResultSet(input)
     * @param keyOnJ String - Key no JSONObject
     * @param j JSONObject - JSONObject(output)
     * @param valorDefault Object - Valor de default caso não exista no
     * resultSet ou venha nulo
     * @throws SQLException
     */
    public static void resultSetToJson(String key, ResultSet rs, String keyOnJ, JSONObject j, Object valorDefault) throws SQLException {
        if (hasColumn(rs, key) && rs.getObject(key) != null) {
            j.put(keyOnJ, rs.getObject(key));
        } else {
            j.put(keyOnJ, valorDefault);
        }
    }

}
