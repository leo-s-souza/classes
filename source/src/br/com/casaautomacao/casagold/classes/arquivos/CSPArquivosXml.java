/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

/**
 * Insira aqui a descrição da classe
 *
 *
 * @author Vitor Bellini Federle <producao3@casaautomacao.com.br>
 * @date 20/11/2017 - 17:24:52
 */
public class CSPArquivosXml extends CSPArquivos {

    public CSPArquivosXml() throws Exception {
        super();
    }

    public CSPArquivosXml(String path) throws Exception {
        super(path);
    }

    @Override
    public String getFormatDefault() {
        return "xml";
    }
}
