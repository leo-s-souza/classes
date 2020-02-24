/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.DIR_SEPARATOR;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe para trabalhar com o cache
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 28/08/2015 - 17:37:33
 */
public class CSPCache {

    private CSPArquivosJson cache;

    /**
     *
     * @param cache Cache - Arquivo de cache
     */
    public CSPCache(Cache cache) throws Exception {
        this.setCache(cache);
    }

    public CSPCache() {
        this.cache = null;
    }

    public void setCache(Cache cache) throws Exception {
        if (this.cache == null || !this.cache.getAbsolutePath().contains(cache.file)) {
            this.cache = new CSPArquivosJson(PadraoClasses.PATH_CACHE + DIR_SEPARATOR);
            this.cache.mkdirs();
            this.cache.setPath(PadraoClasses.PATH_CACHE + DIR_SEPARATOR + cache.file + ".json");
        }
    }

    /**
     * Retorna se o arquivo já existe
     *
     * @return
     */
    public boolean exists() throws Exception {
        return this.cache.exists();
    }

    /**
     * Deleta o arquivo de cache
     *
     * @return
     * @throws java.io.IOException
     */
    public boolean delete() throws Exception {
        return this.cache.delete();
    }

    /**
     * Retorna os registros do cache
     *
     * @return
     * @throws java.io.IOException
     */
    public JSONArray get() throws Exception {
        JSONObject c = this.cache.getObject();
        if (c == null) {
            return new JSONArray();
        }
        return CSPUtilidadesLangJson.getFromJson(c, "REGISTROS", new JSONArray());
    }

    /**
     * Retorna o primeiro registro do cache
     *
     * @return
     * @throws java.io.IOException
     */
    public JSONObject getOne() throws Exception {
        final JSONArray arr = this.get();
        if (arr.length() > 0) {
            return arr.getJSONObject(0);
        }
        return new JSONObject();
    }

    /**
     * seta o valor par ao primeiro registro do cache
     *
     * @param dados
     * @throws java.lang.Exception
     */
    public void setToOne(JSONObject dados) throws Exception {
        JSONArray registros = new JSONArray();
        registros.put(dados);
        JSONObject objJson = new JSONObject();
        objJson.put("REGISTROS", registros);
        this.cache.setObject(objJson);
    }

    /**
     * Adiciona um novo registro ao cache
     *
     * @param dado
     * @throws java.lang.Exception
     */
    public void add(JSONObject dado) throws Exception {
        this.add(new JSONArray() {
            {
                put(dado);
            }
        });
    }

    /**
     * Adiciona novos registros ao cache
     *
     * @param dados
     * @throws java.lang.Exception
     */
    public void add(JSONArray dados) throws Exception {
        JSONArray registros = this.get();
        for (int i = 0; i < dados.length(); i++) {
            registros.put(dados.get(i));
        }
        JSONObject objJson = new JSONObject();
        objJson.put("REGISTROS", registros);
        this.cache.setObject(objJson);
    }

    /**
     * Remove um registro do cache
     *
     * @param colunaChave
     *
     * @param valorChave
     * @throws java.lang.Exception
     */
    public void remove(String colunaChave, String valorChave) throws Exception {
        //valida o cache
        JSONArray registros = this.get();
        for (int i = 0; i < registros.length(); i++) {
            if (valorChave.equals(CSPUtilidadesLangJson.getFromJson(registros.getJSONObject(i), colunaChave, ""))) {
                registros.remove(i);
            }
        }
        JSONObject objJson = new JSONObject();
        objJson.put("REGISTROS", registros);
        this.cache.setObject(objJson);
    }

    public void addObject(String key, Object value) throws Exception {
        this.cache.appendObject(new JSONObject() {
            {
                put(key, value);
            }
        });
    }

    /**
     *
     */
    public enum Cache {

        /**
         * Cache das bases usadas em rotinas de relatórios
         */
        BASES_RELATORIOS("bases_relatorios"),

        /**
         * Cache de IPS
         */
        IPS("endereco_rede"),
        /**
         * Cache de Siglas
         */
        SIGLAS("siglas"),
        /**
         * Cache de Formulários
         */
        FORMULARIOS_FONTE("formularios"),
        /**
         * Cache de Autenticaçoes por host
         */
        SMB_AUTENTICACOES("smb_autenticacoes"),
        /**
         * Cache de md5 do CMG
         */
        CMG_ALL_MD5("all_md5_cmg"),
        /**
         * Cache da relação MACxCNPJxMD5 do CMG
         */
        CMG_LIBERACAO_MAC("liberacao_mac"),
        /**
         * Cache dos demais CMG's
         */
        CMG_LISTA_HOST_CMG("lista-hosts-cmg"),
        /**
         * Lista de servidores disponíveis para o CMG
         */
        MG_LISTA_HOST_CMG("lista-hosts-cmg"),
        /**
         * Lista de servidores disponíveis para o CAPP
         */
        MG_LISTA_HOST_CAPP("lista-hosts-capp"),
        /**
         * Cache de dados MG
         */
        MG_DADOS("mg"),
        /**
         * Lista de portas seriais do sistema.
         */
        LISTA_PORTA_SERIAL("lista_porta_serial"),
        /**
         * Lista de md5 das bases do sistema.
         */
        LISTA_MD5_BASES("lista_md5_bases"),
        /**
         * Lista de caminhos anteriores acessados pela tela de arquivos.
         */
        CAMINHOS_ANTERIORES("caminhos-anteriores");

        final String file;

        private Cache(String file) {
            this.file = file;
        }
    }

}
