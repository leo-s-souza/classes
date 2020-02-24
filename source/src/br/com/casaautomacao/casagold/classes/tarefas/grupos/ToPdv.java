/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas.grupos;

import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;

/**
 * Comandos destinados ao Pdv
 *
 * Usar o prefixo!:
 *
 * TO_PDV_
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 05/10/2016 - 07:55:51
 */
public enum ToPdv {
    TO_PDV_IS_ONLINE(true),
    TO_PDV_ATUALIZA_ABASTECIDA(false),;

    final public CSPServidorTarefas.ServerSupport serv = CSPServidorTarefas.ServerSupport.PDV;
    final public boolean isAsync;

    private ToPdv(boolean isAsync) {
        this.isAsync = isAsync;
    }

}
