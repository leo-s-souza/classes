/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.compartilhamento;

import br.com.casaautomacao.casagold.classes.FrmModuloPaiBase;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPServidorTransferenciaArquivos;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPTransferenciaArquivos;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe responsável por servir o compartilhamento arquivos do locais/remotas
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/09/2016 - 17:28:00
 */
public class CSPServidorCompartilhamento {

    private final CSPServidorTransferenciaArquivos ss = new CSPServidorTransferenciaArquivos();
    private final String pathTransferenciaUpload;
    private final String pathTransferenciaDownload;
    private final HashMap<String, JSONObject> relacaoMD5xDados = new HashMap<>();
    private final HashMap<String, OnUploadCompletoInfos> relacaoIdFilexAcao = new HashMap<>();

    public CSPServidorCompartilhamento(String pathTransferenciaUpload, String pathTransferenciaDownload) {
        if (pathTransferenciaUpload.trim().isEmpty()) {
            this.pathTransferenciaUpload = "";
        } else {
            this.pathTransferenciaUpload = pathTransferenciaUpload + "/";
        }

        if (pathTransferenciaDownload.trim().isEmpty()) {
            this.pathTransferenciaDownload = "";
        } else {
            this.pathTransferenciaDownload = pathTransferenciaDownload + "/";
        }
    }

    public String getPathTransferenciaUpload() {
        return pathTransferenciaUpload;
    }

    public String getPathTransferenciaDownload() {
        return pathTransferenciaDownload;
    }

    
    
    /**
     * Inicia o servidor de informações. Necessário para o registro de upload e
     * afins.
     *
     *
     * @param srv CSPComunicacao.Servico - Serviço onde irá atender
     */
    public final void startServerInfos(CSPComunicacao.Servico srv) {
        FrmModuloPaiBase.simpleThread(() -> {

            /**
             * Recebe e trata as solicitações
             */
            ss.recebeClient(srv, (Socket sc, JSONObject info) -> {
                CSPArquivos filePath = new CSPArquivos(info.getString("FILE_PATH"));

                switch (info.getString("ACAO")) {
                    case "on-upload":
                        filePath.setPath(pathTransferenciaUpload + info.getString("FILE_PATH"));
                        return onUpload(filePath, info);

                    case "on-download":
                        filePath.setPath(pathTransferenciaDownload + info.getString("FILE_PATH"));
                        return onDownload(filePath, info);

                    case "on-delete":
                        return onDelete(filePath, info);

                    case "on-list":
                        return onList(filePath, info);
                    case "on-mkdir":
                        return onMkdir(filePath, info);

                    case "on-infos":
                        return onInfosFile(filePath, info);

                }

                return null;
            });
        });
    }

    /**
     * Inicia o servidor de download de arquivos
     *
     * @param srv CSPTransferenciaArquivos.Servico - Serviço onde irá atender
     * @throws Exception
     */
    public final void startServerDownload(CSPTransferenciaArquivos.Servico srv) throws Exception {
        new CSPArquivos(pathTransferenciaUpload).mkdirs();
        this.ss.serveArquivo(srv, pathTransferenciaDownload, (Socket sc, CSPArquivos file, JSONObject input) -> {
            return file.canRead();
        }, (Socket sc, CSPArquivos file, JSONObject input) -> {
            if (file.getAbsolutePath().startsWith(pathTransferenciaDownload + "rmg-")) {
                file.delete();
            }
        });
    }

    /**
     * Inicia o servidor de upload de arquivos
     *
     * @param srv CSPTransferenciaArquivos.Servico - Serviço onde irá atender
     * @throws Exception
     */
    public final void startServerUpload(CSPTransferenciaArquivos.Servico srv) throws Exception {
        new CSPArquivos(pathTransferenciaUpload).mkdirs();

        this.ss.recebeArquivo(srv, pathTransferenciaUpload, (Socket sc, CSPArquivos file, JSONObject input) -> true, 
                (Socket sc, CSPArquivos file, JSONObject input) -> {
            final String md5 = file.getMd5();

            if (relacaoMD5xDados.containsKey(md5)) {

                final JSONObject data = relacaoMD5xDados.get(md5);
                final String idFile = CSPUtilidadesLangJson.getFromJson(data.getJSONObject("PARAMS"), "ID", (String) null);
                final CSPArquivos destino = new CSPArquivos();
                final String to = CSPUtilidadesLangJson.getFromJson(data.getJSONObject("PARAMS"), "TO", (String) null);

                if (to != null) {

                    final String path = FilenameUtils.getFullPath(data.getJSONObject("PARAMS").getString("TO"));
                    final String name = FilenameUtils.getName(data.getJSONObject("PARAMS").getString("TO"));

                    destino.setPath(path + "/" + name);

                    if (destino.exists()) {
                        if (destino.isDir()) {
                            destino.setPath(data.getJSONObject("PARAMS").getString("TO") + "/" + FilenameUtils.getName(file.getAbsolutePath()));
                        } else {
//                        destino.delete();
                            return;
                        }
                    } else {
                        destino.setPath(path);
                        destino.mkdirs();
                        destino.setPath(path + "/" + name);
                    }

                    if (file.copy(destino.getAbsolutePath())) {
                        file.delete();
                    }

                } else {
                    destino.setPath(file.getAbsolutePath());
                }

                if (idFile != null) {

                    if (relacaoIdFilexAcao.containsKey(idFile)) {

                        final OnUploadCompletoInfos tmp = relacaoIdFilexAcao.get(idFile);

                        final ArrayList<String> moreData = new ArrayList<>();

                        for (Object t : CSPUtilidadesLangJson.getFromJson(data.getJSONObject("PARAMS"), "MORE", new JSONArray())) {
                            moreData.add(t.toString());
                        }
                        
                        tmp.acao.run(
                                destino, 
                                idFile, 
                                moreData.toArray(new String[moreData.size()]), 
                                sc.getRemoteSocketAddress().toString(), 
                                CSPUtilidadesLangRede.getMac());

                        if (tmp.isDescartavel) {
                            relacaoIdFilexAcao.remove(idFile);
                        }

                    }
                }

            }
        });
    }

    private JSONObject onDownload(CSPArquivos file, JSONObject data) throws Exception {
        if (file.exists()) {
            return new JSONObject() {
                {
                    put("STATUS", "ok");
                    put("MD5", file.getMd5());
                }
            };

        }
        return new JSONObject();
    }

    /**
     * Auxilia no preparo de arquivos para download. Todos os arquivos
     * preparados serão compactados
     *
     * @param removeOnDownload boolean - Determina se o arquivo deve ser
     * excluído ao efetuar um download com sucesso
     * @param arquivos CSPArquivos... - Lista de arquivos a serem compactados
     * para serem fornecidos
     * @return
     * @throws java.lang.Exception
     */
    public CSPArquivos preparaArquivoDownload(boolean removeOnDownload, CSPArquivos... arquivos) throws Exception {

        new CSPArquivos(pathTransferenciaDownload).mkdirs();

        final CSPArquivos zip = new CSPArquivos(
                pathTransferenciaDownload
                + (removeOnDownload ? "rmg-" : "")
                + (CSPUtilidadesLang.getMd5(
                        CSPUtilidadesLangDateTime.getTempoCompletoLimpo()
                        + arquivos[0].getMd5()
                ))
        );

        zip.zipFiles(arquivos);
        return zip;
    }

    /**
     * Auxilia no preparo de arquivos já zipado para download.
     *
     * @param removeOnDownload boolean - Determina se o arquivo deve ser
     * excluído ao efetuar um download com sucesso
     * @param arquivo CSPArquivos - Arquivo a serem fornecidos
     * @return
     * @throws java.lang.Exception
     */
    public CSPArquivos preparaArquivoDownloadJaZipado(boolean removeOnDownload, CSPArquivos arquivo) throws Exception {

        new CSPArquivos(pathTransferenciaDownload).mkdirs();

        final CSPArquivos arq = new CSPArquivos(
                pathTransferenciaDownload
                + (removeOnDownload ? "rmg-" : "")
                + (CSPUtilidadesLang.getMd5(
                        CSPUtilidadesLangDateTime.getTempoCompletoLimpo()
                        + arquivo.getMd5()
                ))
        );

        arquivo.copy(arq);
        return arq;
    }

    private JSONObject onUpload(CSPArquivos file, JSONObject data) throws Exception {

        relacaoMD5xDados.put(data.getJSONObject("PARAMS").getString("MD5"), data);

        return new JSONObject() {
            {
                put("STATUS", "ok");
            }
        };

    }

    protected JSONObject onMkdir(CSPArquivos file, JSONObject data) throws Exception {
        return new JSONObject();//Somente MG deve prover
    }

    protected JSONObject onInfosFile(CSPArquivos file, JSONObject data) throws Exception {
        return new JSONObject();//Somente MG deve prover
    }

    protected JSONObject onDelete(CSPArquivos file, JSONObject data) throws Exception {
        return new JSONObject();//Somente MG deve prover
    }

    protected JSONObject onList(CSPArquivos file, JSONObject data) throws Exception {
        return new JSONObject();//Somente MG deve prover
    }

    /**
     * Registra o disparo de um evento assim que um arquivo que contenha o id
     * informado for recebido via upload
     *
     * @param id String - ID para identificar o evento
     * @param run OnUploadCompleto - Acao a ser disparada
     * @param isDescartavel booelan - Determina se essa ação deve ser removida
     * da memória após ser usada pela primeria vez. Ou seja, não irá atender
     * mais que um upload
     */
    public void addListnerOnUploadCompleto(String id, OnUploadCompleto run, boolean isDescartavel) {
        this.relacaoIdFilexAcao.put(id, new OnUploadCompletoInfos(id, isDescartavel, run));
    }

    /**
     * Interface destinada a controlar quando um upload foi realizado com
     * sucesso
     */
    public interface OnUploadCompleto {

        public void run(CSPArquivos file, String id, String[] params, String hostClient, String macClient) throws Exception;
    }

    /**
     * Classe que representa um registro de ação de upload
     */
    private class OnUploadCompletoInfos {

        private final String id;
        private final boolean isDescartavel;
        private final OnUploadCompleto acao;

        public OnUploadCompletoInfos(String id, boolean isDescartavel, OnUploadCompleto acao) {
            this.id = id;
            this.isDescartavel = isDescartavel;
            this.acao = acao;
        }

    }

}
