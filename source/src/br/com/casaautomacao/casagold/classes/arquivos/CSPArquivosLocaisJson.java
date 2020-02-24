/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe para manipulação de arquivos JSON. A classe automaticamente adiciona o
 * ".json" nos caminhos dos arquivos
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 29/09/2015 - 08:03:09
 */
@Deprecated
public class CSPArquivosLocaisJson extends CSPArquivosLocais implements InterfaceCSPArquivosJson {

    @Deprecated
    public CSPArquivosLocaisJson() {
    }

    @Override
    @Deprecated
    public String getFormatDefault() {
        return ".json";
    }

    /**
     *
     * Classe para manipulação de arquivos JSON
     *
     * @param caminho String - Caminho absoluto
     */
    @Deprecated
    public CSPArquivosLocaisJson(String caminho) {
        super(caminho);
    }

    @Override
    @Deprecated
    public JSONObject getObject() throws IOException {
        return CSPUtilidadesLangJson.getObject(this.getContent());
    }

    @Override
    @Deprecated
    public JSONArray getArray() throws IOException {
        return CSPUtilidadesLangJson.getArray(this.getContent());
    }

    @Override
    @Deprecated
    public void setObject(JSONObject obj) throws Exception {
        this.setContent(obj.toString());
    }

    @Override
    @Deprecated
    public void setArray(JSONArray arr) throws Exception {
        this.setContent(arr.toString());
    }

    @Override
    @Deprecated
    public void appendObject(JSONObject obj) throws Exception {
        JSONObject old = this.exists() ? this.getObject() : null;
        this.setObject(CSPUtilidadesLangJson.appendObject(old, obj));
    }

    @Override
    @Deprecated
    public void appendArray(JSONArray arr) throws Exception {
        JSONArray old = this.exists() ? this.getArray() : null;
        this.setArray(CSPUtilidadesLangJson.appendArray(old, arr));
    }

    @Override
    @Deprecated
    public void appendArray(JSONObject obj) throws Exception {
        JSONArray old = CSPUtilidadesLangJson.appendArray(this.exists() ? this.getArray() : null, new JSONArray());
        old.put(obj);
    }

}
