/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 17/12/2015 - 10:15:16
 */
@Deprecated
public interface InterfaceCSPArquivosJson extends InterfaceCSPArquivos {

    /**
     * Retorna o objeto das informações do JSON
     *
     * @return JSONObject
     */
    @Deprecated
    public JSONObject getObject() throws Exception;

    /**
     * Retorna o array das informações do JSON
     *
     * @return JSONArray
     */
    @Deprecated
    public JSONArray getArray() throws Exception;

    /**
     * Grava o objeto no arquivo
     *
     * @param obj JSONObject - Json que a ser gravado
     */
    @Deprecated
    public void setObject(JSONObject obj) throws Exception;

    /**
     * Grava o array no arquivo
     *
     * @param arr JSONArray - Json(Array) que a ser gravado
     */
    @Deprecated
    public void setArray(JSONArray arr) throws Exception;

    /**
     * Grava, adicionando, as novas posições no objeto
     *
     * @param obj JSONObject - Json que a ser gravado
     */
    @Deprecated
    public void appendObject(JSONObject obj) throws Exception;

    /**
     * Grava, adicionando, as novas posições no array
     *
     * @param arr JSONArray - Json(Array) que a ser gravado
     */
    @Deprecated
    public void appendArray(JSONArray arr) throws Exception;

    /**
     * Grava, adicionando, as novas posições no array um JSONObject
     *
     * @param obj JSONObject - Json que a ser gravado em uma nova posição do
     * array atual
     */
    @Deprecated
    public void appendArray(JSONObject obj) throws Exception;
}
