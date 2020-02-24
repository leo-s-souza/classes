/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.exceptions;

import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;

/**
 * Exception para disparada caso o cliente de tarefas seja notificado que a tarefa não foi reconhecida
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 05/10/2016 - 16:20:33
 */
public class UnrecognizedTaskException extends Exception{

   final public CSPServidorTarefas.ServerSupport server;

    public UnrecognizedTaskException(CSPServidorTarefas.ServerSupport server) {
        this.server = server;
    }
   

 
}
