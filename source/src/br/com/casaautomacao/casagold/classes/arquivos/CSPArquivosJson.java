/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe para manipulação de arquivos JSON. A classe automaticamente adiciona o
 * ".json" nos caminhos dos arquivos
 *
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 17/08/2016 - 18:42:59
 */
public class CSPArquivosJson extends CSPArquivos {
    
    public CSPArquivosJson() throws Exception {
        super();
    }
    
    public CSPArquivosJson(String path) throws Exception {
        super(path);
    }
    
    @Override
    public String getFormatDefault() {
        return "json";
    }

    /**
     * Retorna o objeto das informações do JSON
     *
     * @return JSONObject
     */
    public JSONObject getObject() throws Exception {
        JSONObject r = CSPUtilidadesLangJson.getObject(this.getContent());
        if (r == null) {
            r = new JSONObject();
        }
        return r;
    }

    /**
     * Retorna o array das informações do JSON
     *
     * @return JSONArray
     */
    public JSONArray getArray() throws Exception {
        JSONArray r = CSPUtilidadesLangJson.getArray(this.getContent());
        if (r == null) {
            r = new JSONArray();
        }
        return r;
    }

    /**
     * Grava o objeto no arquivo
     *
     * @param obj JSONObject - Json que a ser gravado
     */
    public synchronized void setObject(JSONObject obj) throws Exception {
        this.setContent(obj.toString());
    }

    /**
     * Grava o array no arquivo
     *
     * @param arr JSONArray - Json(Array) que a ser gravado
     */
    public void setArray(JSONArray arr) throws Exception {
        this.setContent(arr.toString());
    }

    /**
     * Grava, adicionando, a nova posiçãp no objeto
     *
     * @param key String - Nova key a ser adicionada
     * @param val Object - Novo value a ser adicionado na key
     */
    public synchronized void appendObject(String key, Object val) throws Exception {
        this.appendObject(new JSONObject() {
            {
                put(key, val);
            }
        });
    }

    /**
     * Grava, adicionando, as novas posições no objeto
     *
     * @param obj JSONObject - Json que a ser gravado
     */
    public synchronized void appendObject(JSONObject obj) throws Exception {
        JSONObject old = this.exists() ? this.getObject() : null;
        this.setObject(CSPUtilidadesLangJson.appendObject(old, obj));
    }

    /**
     * Grava, removendo, a posição no objeto
     *
     * @param key String - Key a ser removida
     */
    public void removeObjectByKey(String key) throws Exception {
        
        final JSONObject old = this.exists() ? this.getObject() : new JSONObject();
        
        if (old.has(key)) {
            old.remove(key);
            this.setObject(old);
        }
    }

    /**
     * Grava, adicionando, a nova posiçãp no array
     *
     *
     * @param val Object - Novo value a ser adicionado
     */
    public void appendArray(Object val) throws Exception {
        this.appendArray(new JSONArray() {
            {
                put(val);
            }
        });
    }

    /**
     * Grava, adicionando, as novas posições no array
     *
     * @param arr JSONArray - Json(Array) que a ser gravado
     */
    public void appendArray(JSONArray arr) throws Exception {
        JSONArray old = this.exists() ? this.getArray() : null;
        this.setArray(CSPUtilidadesLangJson.appendArray(old, arr));
    }

    /**
     * Grava, adicionando, as novas posições no array um JSONObject
     *
     * @param obj JSONObject - Json que a ser gravado em uma nova posição do
     * array atual
     */
    public void appendArray(JSONObject obj) throws Exception {
        JSONArray old = CSPUtilidadesLangJson.appendArray(this.exists() ? this.getArray() : null, new JSONArray());
        old.put(obj);
    }

    /**
     * Grava, removendo, a posição no objeto com base no valor do mesmo
     *
     * @param val Object - Valor do objeto no array a ser removido
     */
    public void removeArrayByValue(Object val) throws Exception {
        
        final JSONArray old = this.exists() ? this.getArray() : new JSONArray();
        
        int i = 0;
       
        for (Object o : old) {
         
            if (o != null && val != null && o.equals(val)) {
                old.remove(i);
                this.setArray(old);
            }
            i++;
            
        }
        
    }
    
}
