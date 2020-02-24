/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.sincronizacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.compartilhamento.CSPClienteCompartilhamentoBase;
import br.com.casaautomacao.casagold.classes.compartilhamento.CSPServidorCompartilhamento;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPTransferenciaArquivos;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.PATH_TEMP;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import org.json.JSONObject;

/**
 * Sincronização manual de dados e arquivos
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 16/08/2017 - 11:32:07
 */
public class CSPSincronizacaoManual extends CSPSincronizacao {

    private final LinkedHashMap<String, Recebido> recebido = new LinkedHashMap<>();
    private final CSPServidorCompartilhamento serverCompartilhamento;
    private final CSPComunicacao.Servico servicoInfos;
    private final CSPTransferenciaArquivos.Servico servicoInfosFile;

    /**
     * Sincronização
     *
     * @param idSync String - Id da sincronização. Já que é possível sincronizar
     * mais de um conteúdo por vez
     * @param remoteHost String - Host/ip do outro servidor com que a base será
     * sincronizada
     * @param isMaster boolean - Se a aplicação(esta) é o master
     * @param serverCompartilhamento CSPServidorCompartilhamento
     * @param sInfo Servico
     * @param sFile Servico
     */
    public CSPSincronizacaoManual(String idSync, String remoteHost, boolean isMaster, CSPServidorCompartilhamento serverCompartilhamento, CSPComunicacao.Servico sInfo, CSPTransferenciaArquivos.Servico sFile) {
        super(idSync, remoteHost, isMaster);
        this.serverCompartilhamento = serverCompartilhamento;
        this.servicoInfos = sInfo;
        this.servicoInfosFile = sFile;
    }

    /**
     * Inicia o processo
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception {
        super.start();

        this.setOnReceiveInfo((Object... info) -> {
            /**
             * Quando for apenas informações é usada essa api
             */
            switch (info[0].toString()) {
                case "data":
                    this.aoReceber(info[1].toString(), new JSONObject(info[2].toString()), null);
                    return true;
            }

            return false;

        });

        this.serverCompartilhamento.addListnerOnUploadCompleto(getIdSync() + "-data-file", (CSPArquivos file, String id, String[] info, String hostClient, String macClient) -> {
            /**
             * Quando temos arquivos envolvidos no processo
             */
            final CSPArquivos tmp = new CSPArquivos(PATH_TEMP + "/" + file.getName());
            final LinkedHashSet<CSPArquivos> arqs = new LinkedHashSet<>();
           
            file.unzipFiles(tmp);

            this.aoReceber(info[0], new JSONObject(info[1]), tmp.listFiles());

            file.delete();
            tmp.delete();
    
        }, false);
    
    }

    /**
     * Trata o evento de recebimento de novas infos
     *
     *
     * @throws Exception
     */
    private synchronized void aoReceber(String idRef, JSONObject infos, CSPArquivos[] arquivos) throws Exception {
        final Recebido env = this.recebido.get(idRef);

        if (env != null) {

            if (arquivos == null) {
                arquivos = new CSPArquivos[0];
            }

            final String log = "Sincronizado(id:" + idRef + ";infos:" + infos + ";num-arquivos:" + arquivos.length + ")...";

            CSPLog.info(this.getClass(), log);
            env.run(infos, arquivos);
            CSPLog.info(this.getClass(), log + "OK");

        }
    }

    /**
     * Registra um evento que será disparado ao receber do outro host novas
     * informações
     *
     * @param idRef String - Referência para identificar o que esta sendo
     * sincronizado. Nome de uma tabela, por exemplo
     * @param env Recebido - Evento disparado
     */
    public void putEventSync(String idRef, Recebido env) {
        this.recebido.put(idRef, env);
    }

    /**
     * Envia ao outro hosts as informações que precisam ser sincronizadas
     *
     * @param idRef String - Referência para identificar o que esta sendo
     * sincronizado. Nome de uma tabela, por exemplo
     * @param infos JSONObject - Informações
     * @param arquivos CSPArquivos[] - Arquivos a serem enviados
     * @throws Exception
     */
    public void sendSync(String idRef, JSONObject infos, CSPArquivos[] arquivos) throws Exception {

        if (arquivos == null || arquivos.length == 0) {

            this.sendInfo("data", idRef, infos.toString());

        } else {

            final CSPArquivos arquivo = new CSPArquivos(PATH_TEMP + "/" + CSPUtilidadesLang.getMd5(idRef + infos.toString()));

            arquivo.zipFiles(arquivos);

            CSPClienteCompartilhamentoBase.uploadFileBase(
                    this.servicoInfos,
                    this.servicoInfosFile,
                    getIdSync() + "-data-file",
                    this.getRemoteHost(),
                    arquivo,
                    idRef,
                    infos.toString()
            );

        }

    }

    public static interface Recebido {

        public void run(JSONObject infosRecebidas, CSPArquivos[] arquivosRecebidos) throws Exception;

    }

}
