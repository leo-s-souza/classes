/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

/**
 * 
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/12/2015 - 10:29:33
 */
@Deprecated
public interface InterfaceImportacao {

    /**
     * Executa a importação
     * @return boolean - Resultado da importação
     */
    public boolean run() throws Exception;
}
