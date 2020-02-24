/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import org.jdom2.Element;

/**
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 * @date 09/11/2016
 */
public abstract class CSPUtilidadesLangXML extends CSPUtilidadesLang{

    /**
     * Busca o elemento do XML pelo nome da tag passada.
     *
     * @param nome Nome da tag a ser procurada no XML.
     * @param elmt Elemento/Tag principal que contém os filhos a serem
     * percorridos a procura do elemento passado.
     * @return Element
     * @throws Exception
     */
    public static Element getElement(String nome, Element elmt) throws Exception {
        if (elmt.getName().equals(nome)) {
            return elmt;
        }

        for (Element element : elmt.getChildren()) {
            if (element.getName().equals(nome)) {
                return element;
            } else {
                Element el = getElement(nome, element);
                if (el != null) {
                    if (el.getName().equals(nome)) {
                        return el;
                    }
                }
            }
        }

        return null;
    }

}
