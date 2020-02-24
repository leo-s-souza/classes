/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas.grupos;

import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;

/**
 * Comandos destinados ao retaguarda
 *
 * Usar o prefixo!:
 *
 * TO_RET_ 
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 05/10/2016 - 07:55:51
 */
public enum ToRet {

    TO_RET_ATUALIZA_PAINEL_PEDIDOS(false),
    TO_RET_RESTART(true),
    TO_RET_IS_ONLINE(true),;
    final public  CSPServidorTarefas.ServerSupport serv = CSPServidorTarefas.ServerSupport.RET;
    final public  boolean isAsync;

    private ToRet(boolean isAsync) {
        this.isAsync = isAsync;
    }
}
