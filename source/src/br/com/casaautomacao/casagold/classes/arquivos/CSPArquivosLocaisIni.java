/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Classe para manipulação de arquiivos .ini
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 29/09/2015 - 08:02:34
 */
@Deprecated
public class CSPArquivosLocaisIni extends CSPArquivosLocais {

    @Override
    @Deprecated
    public String getFormatDefault() {
        return ".ini";
    }

    /**
     * Classe para manipulação de arquiivos .ini
     *
     * @param caminho String - Caminho absoluto
     */
    @Deprecated
    public CSPArquivosLocaisIni(String caminho) {
        super(caminho);
    }

    /**
     * Retorna as informações contidas no arquivo
     *
     * @return
     */
    @Deprecated
    public Properties getInfos() throws FileNotFoundException, IOException {
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
    @Deprecated
    public void setInfos(Properties prop) throws Exception {
        String end = "";
        end = prop.entrySet().stream().map((entrySet) -> entrySet.getKey() + "=" + entrySet.getValue() + LINE_SEPARATOR).reduce(end, String::concat);
        this.setContent(end);
    }
}
