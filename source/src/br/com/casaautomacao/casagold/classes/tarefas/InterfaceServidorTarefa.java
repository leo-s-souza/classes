/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas;

/**
 * Interface para o tratamento de pendencias
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 24/02/2016 - 18:14:55
 */
public interface InterfaceServidorTarefa {

    /**
     * Disparado ao receber uma nova pendencia
     *
     * @param cod String - Código da pendencia
     * @param parametros String[] - Parâmetros recebidos
     * @param acao InterfaceServidorTarefaAcao
     */
    public void aoReceber(String cod, String[] parametros, InterfaceServidorTarefaAcao acao) throws Exception;

}
