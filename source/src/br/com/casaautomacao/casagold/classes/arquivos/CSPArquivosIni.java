/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * lasse para manipulação de arquivos .ini
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 17/08/2016 - 18:42:26
 */
public class CSPArquivosIni extends CSPArquivos {

    public CSPArquivosIni() throws Exception {
        super();
    }

    public CSPArquivosIni(String path) throws Exception {
        super(path);
    }

    @Override
    public String getFormatDefault() {
        return "ini";
    }



    
    
    /**
     * Retorna as informações contidas no arquivo
     *
     * @return
     */
    public Properties getInfos() throws Exception {
        if (this.exists()) {
            Properties goldIni = new Properties();
            try (FileInputStream fIS = this.objFileInputStream()) {
                goldIni.load(fIS);
            }
            return goldIni;
        }
        return null;

    }

    /**
     * Grava as informações no arquivo
     *
     * @param prop Properties - Informações a serem gravadas
     */
    public void setInfos(Properties prop) throws Exception {
        String end = "";
        end = prop.entrySet().stream().map((entrySet) -> entrySet.getKey() + "=" + entrySet.getValue() + LINE_SEPARATOR).reduce(end, String::concat);
        this.setContent(end);
    }
}
