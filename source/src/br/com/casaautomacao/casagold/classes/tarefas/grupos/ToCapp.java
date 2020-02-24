/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas.grupos;

import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;

/**
 * Comandos destinados ao CAPP
 *
 * Usar o prefixo!:
 *
 * TO_CAPP_ 
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 05/10/2016 - 07:55:51
 */
public enum ToCapp {

    TO_CAPP_ATUALIZA_DADOS_CONTRATANTE_APP(false),
    TO_CAPP_ATUALIZA_DADOS_DISPOSITIVO(false),
    TO_CAPP_CONVERTE_BONUS(false),
    TO_CAPP_ATUALIZA_PERCENTUAIS_LOJA(false),
    TO_CAPP_RESTART_LOGS(false),
    TO_CAPP_CHECK_IS_ONLINE(false),
    TO_CAPP_UPDATE_BONUS(false),
    TO_CAPP_PREPARA_LOGS_PROCESSAMENTO(false),
    TO_CAPP_DELETE_LOGS_PROCESSAMENTO(false),
    TO_CAPP_PREPARA_ASSETS_BUILD_APP(false),
    TO_CAPP_EXCLUI_BONUS_PEDIDOS(false),    
    TO_CAPP_SYNC_API(false),    
    TO_CAPP_REDIRECIONA_LOJA(false),    
    ;
    final public  CSPServidorTarefas.ServerSupport serv = CSPServidorTarefas.ServerSupport.CAPP;
    final public  boolean isAsync;

    private ToCapp(boolean isAsync) {
        this.isAsync = isAsync;
    }
}
