/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas.grupos;

import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;

/**
 * Comandos destinados ao CMG
 *
 * Usar o prefixo!:
 *
 * TO_CMG_ 
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 05/10/2016 - 07:55:51
 */
public enum ToCmg {

    TO_CMG_RESTART_LOGS(true),
    TO_CMG_CHECK_IS_ONLINE(false),
    TO_CMG_AUTENTICACAO_MAC(false),
    TO_CMG_REGISTRA_COMANDOS_TO_CONTRATANTE(false),
    TO_CMG_REBUILD_CONFIGS(false),
    TO_CMG_PREPARA_LOGS_PROCESSAMENTO(false),
    TO_CMG_DELETE_LOGS_PROCESSAMENTO(false),
    TO_CMG_PREPARA_RHG(false),
    TO_CMG_SYNC_RHG(false),
    TO_CMG_SYNC_VERSOES_MG(false),
    TO_CMG_UPDATE_MD5_BASES_AUTOMATICAS(false),
    TO_CMG_SEND_INFOS_CONTRATANTE_FULL(false),
    TO_CMG_PREPARA_BASE_OLD_CAPP(false),
    
    ;
    final public  CSPServidorTarefas.ServerSupport serv = CSPServidorTarefas.ServerSupport.CMG;
    final public  boolean isAsync;

    private ToCmg(boolean isAsync) {
        this.isAsync = isAsync;
    }
}
