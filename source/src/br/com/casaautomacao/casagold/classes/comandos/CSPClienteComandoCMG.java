/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.comandos;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.compartilhamento.CSPClienteCompartilhamentoBase;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao;
import static br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao.Servico.CCAS;
import static br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao.getHostByHostBuilder;
import static br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPTransferenciaArquivos.Servico.CCAS_ARQUIVOS_UPLOAD;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;

import org.json.JSONObject;

/**
 * Controla o envio e recebimento de comandos do servidor.
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public class CSPClienteComandoCMG {

    private final CSPComunicacao socketClient;

    public CSPClienteComandoCMG() {
        this.socketClient = new CSPComunicacao();
    }

    /**
     * Envia comando para o servidor tanto de ADM como de SC.
     *
     * @param comando String - Tipo do comando ex(GSCV, AAMG).
     * @param informacoesPadrao JSONObject - recebe um json com informações
     * padrao entre adm e sc.
     * @param params JSONObject - parametros enviados para o servidor.
     * @param arquivos CSPArquivos[] - array de arquivos que devem ser enviados
     * para o servidor.
     * @return
     * @throws Exception
     */
    public boolean enviaComando(String comando, JSONObject informacoesPadrao, JSONObject params, CSPArquivos[] arquivos) throws Exception {

        informacoesPadrao.put("TIPO_COMANDO", comando);

        if (params != null && params.length() > 0) {
            informacoesPadrao.put("PARAMS", params.toString());
        }

        if (arquivos != null && arquivos.length != 0) {
            informacoesPadrao.put("NEED_UPLOAD_FILE", true);
        }

        long id = CSPUtilidadesLangJson.getFromJson(this.getSocketClient().comunica(CSPComunicacao.Servico.CMGCAPI, informacoesPadrao), "ID", (long) 0);

        if (id <= 0) {
            return false;
        }

        /**
         * Caso exitam arquivos recebidos pelo parametro "arquivos" o mesmo deve
         * ser enviado ao servidor
         */
        if (arquivos != null && arquivos.length > 0) {
            CSPArquivos arq = new CSPArquivos(CSPUtilidadesSO.PATH_TEMP + "/" + CSPUtilidadesLangDateTime.getTempoCompletoLimpo() + ".zip");

            arq.zipFiles(arquivos);

            if (CSPClienteCompartilhamentoBase.uploadFileBase(CCAS, CCAS_ARQUIVOS_UPLOAD, "arquivo-" + String.valueOf(id), getHostByHostBuilder(CCAS.hostType, CCAS.port, null, 0),arq)) {
                arq.delete();
            }
        }

        return true;
    }

    /**
     * Retorna o socket utilizado pelos métodos.
     *
     * @return
     */
    public CSPComunicacao getSocketClient() {
        return socketClient;
    }
}
