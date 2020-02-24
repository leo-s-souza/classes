/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas;

/**
 * Interface para o tratamento do retorno das pendencias
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 30/09/2016 - 07:49:50
 */
public interface InterfaceServidorTarefaAcao {

    /**
     * Finaliza a tarefa em questão
     *
     * @throws Exception
     */
    public void finaliza() throws Exception;

    /**
     * Finaliza todas as tarefas do grupo
     *
     * @throws Exception
     */
    public void finalizaAll() throws Exception;

    /**
     * Define o valor de resposta da tarefa
     *
     * @param response String ... - Valor de resposta
     * @throws Exception
     */
    public void resposta(String... response) throws Exception;
    
    /**
     * 
     * @return
     * @throws Exception 
     */
    public String getHostOrigem() throws Exception;
    
    /**
     * Atalho para os logs do módulo
     *
     * @param ms String - Mensagem a ser gravada
     */
    public void logInfo(String ms);
    
        /**
     * Atalho para os logs de erro do módulo
     *
     * @param ms String - Mensagem a ser gravada
     */
    public void logError(String ms) ;
}
