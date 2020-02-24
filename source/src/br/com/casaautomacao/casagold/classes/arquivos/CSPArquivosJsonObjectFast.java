/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import org.json.JSONObject;

/**
 * Variação da classe de manipulação de arquivos de json.
 *
 * Essa classe é destinada para arquivos json que necessitam ser atualizados de
 * forma constante. Em processo de multiplas threads o appendObject da outra
 * classe se torna um problema. Essa tem como objectivo contronar isso
 * implementando uma forma mais 'thread safe'.
 *
 *
 * Seria basicamente uma variação do formato json, onde em vez de toda vez ao
 * salvarmos gerarmos um novo jsonObject estaremos realizando um 'append' de uma
 * informação.
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 25/10/2016 - 13:54:44
 */
public class CSPArquivosJsonObjectFast extends CSPArquivos {

    public CSPArquivosJsonObjectFast() throws Exception {
        super();
    }

    public CSPArquivosJsonObjectFast(String path) throws Exception {
        super(path);
    }

    @Override
    public String getFormatDefault() {
        return "fjson";
    }

    /**
     * Adiciona um novo valor ao arquivo. A ordem nao e garantida quando usado
     * em multiplas threads
     *
     * @param key String - Key para identificar
     * @param val Object - Valor
     * @throws Exception
     */
    public void appendObject(String key, Object val) throws Exception {
        this.appendContent(new JSONObject() {
            {
                put(key, val);
            }
        }.toString() + CSPUtilidadesSO.LINE_SEPARATOR);
    }

    /**
     * Efetua a leitura do arquivo e monta um json com o seu conteúdo. Mesmo que
     * o arquivo contenha 'keys' repetidas sera apenas retornado as últimas
     * adicionadas
     *
     * @return JSONObject
     */
    public JSONObject getObject() throws Exception {
        if (!this.isFile()) {
            return new JSONObject();
        }

        final JSONObject r = new JSONObject();

        for (String line : this.getContent().split(CSPUtilidadesSO.LINE_SEPARATOR)) {
            CSPUtilidadesLangJson.appendObject(r, new JSONObject(line));
        }

        return r;
    }

    /**
     * De tempos em tempos é recomendado utilizar essa função para recontruir o
     * arquivo com apenas o conteúdo mais recente
     */
    public void rebuildContent() throws Exception {
        final JSONObject loaded = this.getObject();
        
        this.delete();
        
        for (String k : loaded.keySet()) {
            this.appendObject(k, loaded.get(k));
        }
    }
}
