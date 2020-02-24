/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas.grupos;

import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;

/**
 * Comandos destinados ao MG
 *
 * Usar os prefixos!:
 *
 * TO_MG_
 *
 * TO_MG_AUX_ => Forma auxiliar. Geralmente usado para redirecionar um comando
 * do RET para o CMG/CAPP
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 05/10/2016 - 07:55:51
 */
public enum ToMg {

    TO_MG_ATUALIZA_DADOS_APP(true),
    TO_MG_ATUALIZA_DADOS_APP_ADM(true),
    TO_MG_CONFIRMA_PEDIDO(false),
    TO_MG_CANCELA_PEDIDO(false),
    TO_MG_LIBERA_APP(true),
    TO_MG_ENVIA_NOTIFICACAO_APP(true),
    TO_MG_COMPARTILHAMENTO_ARQUIVOS(false),
    TO_MG_REBUILD_CONFIGS(false),
    TO_MG_COMUNICA_FULL(true),
    TO_MG_SOLICITA_ATUALIZACAO(true),
    TO_MG_PROCESSA_LOGS(true),
    TO_MG_RESTART(true),
    TO_MG_IS_ONLINE(true),
    TO_MG_BACKUP_ARQUIVOS(true),
    TO_MG_GET_INFOS(false),
    TO_MG_SOLICITA_GERACAO_SPED(false),
    TO_MG_ATUALIZA_LISTA_BASES_AUTOMATICAS(false),
    //AUX MG
    TO_MG_AUX_MG_CONFIGURA_PROCESSA_LOGS(false),
    TO_MG_AUX_MG_PROCESSA_LOGS(true),
    TO_MG_AUX_MG_SYNC_API(false),
    //AUX CAPP
    TO_MG_AUX_CAPP_ATUALIZA_DADOS_CONTRATANTE_APP(false),
    TO_MG_AUX_CAPP_CHECK_IS_ONLINE(false),
    TO_MG_AUX_CAPP_UPDATE_BONUS(false),
    
    TO_MG_AUX_CAPP_CONVERTE_BONUS(false),
    TO_MG_AUX_CAPP_ATUALIZA_PERCENTUAIS_LOJA(false),
    //AUX CMG
    TO_MG_AUX_CMG_CHECK_IS_ONLINE(false),
    TO_MG_AUX_CMG_AUTENTICACAO_MAC(false),
    TO_MG_AUX_CMG_REDIRECT_HOST(false),
    TO_MG_AUX_CMG_RESET_BASE_BACKUP(false),
    TO_MG_AUX_CMG_REBUILD_CONFIGS(false),
    TO_MG_AUX_CMG_DOWNLOAD_FILE(false),
    TO_MG_AUX_CMG_UPLOAD_FILE(false),
    TO_MG_AUX_CMG_RESPONDE_CONVERSAO_BONUS(false),
    TO_MG_AUX_CMG_DPSC(false),
    TO_MG_AUX_CMG_MONITORA_BASES_AUTOMATICAS(false),
    
    TO_MG_AUX_CMG_ADM_NOTIFICA_UP_REGISTROS(false),
    TO_MG_AUX_CMG_ADM_INTEGRACAO_UPLOAD(false),
    TO_MG_AUX_CAPP_ADM_NOTIFICA_UP_REGISTROS(false),
    TO_MG_AUX_CMG_ADM_INTEGRACAO_UPDATE_ESTRUTURA(false),
    TO_MG_AUX_CMG_UPLOAD_ARQUIVO_VERSAO(false);
    final public CSPServidorTarefas.ServerSupport serv = CSPServidorTarefas.ServerSupport.MG;
    final public boolean isAsync;

    private ToMg(boolean isAsync) {
        this.isAsync = isAsync;
    }
}
