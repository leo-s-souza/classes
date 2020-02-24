/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes;

import static br.com.casaautomacao.casagold.classes.CSPKitCapp.VersionsAppSupported.ad_1_8_3;
import static br.com.casaautomacao.casagold.classes.CSPKitCapp.VersionsAppSupported.ad_2_1_3;
import static br.com.casaautomacao.casagold.classes.CSPKitCapp.VersionsAppSupported.io_1_6_9;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosAssinados;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosJson;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosJsonObjectFast;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocaisJson;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosMapeadosMd5;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorTransferenciaArquivos;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.getMd5;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQL;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe que centraliza as funções comuns ao CAPP e ao CAPP-LS(que roda no
 * contratante)
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 20/09/2016 - 14:58:19
 */
@Deprecated
public class CSPKitCapp {

    final public static String PATH_INFOS = PATH + "/infos-app";
    final private static HashMap<String, Date> lastMovimentacaoDev = new HashMap<>();
    final private static SimpleDateFormat lastMovimentacaoDevFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    final public static HashMap<String, HashMap<String, String>> relationLastMd5XFile = new HashMap<>();
    final public static CSPArquivosMapeadosMd5 arquivosParaDisponibilizar = new CSPArquivosMapeadosMd5();
    final private static HashMap<String, String> conteudoCacheArquivos = new HashMap<>();
    final private static HashMap<String, String> cnpjFromFileCache = new HashMap<>();
    final private static HashMap<String, Boolean> fileLiberadoToDevCache = new HashMap<>();
    final private static HashMap<String, VersionsAppSupported> appVersaoCache = new HashMap<>();
    private static CSPArquivos arquivoInfosTotalLista;
    private static CSPArquivos arquivoInfosTotalMd5;
    private static CSPArquivos listaContratantesFull;

    static {

        try {
            CSPKitCapp.arquivoInfosTotalLista = new CSPArquivos(getPathContratantes() + "/total-lista.txt");
            CSPKitCapp.arquivoInfosTotalMd5 = new CSPArquivos(getPathContratantes() + "/total-md5.txt");
            CSPKitCapp.listaContratantesFull = CSPArquivosAssinados.getInstance(getPathContratantes() + "/lista-contratantes.json");
        } catch (Exception ex) {
            CSPException.register(ex);
        }
    }

    /**
     * Retorna o caminho absoluto da pasta do dispositivo
     *
     * @param id String - Id do dispositivo
     * @return
     */
    public static String getPathDev(String id) {
        return getPathDispositivos() + "/" + (id).toLowerCase();
    }

    /**
     * Retorna o caminho absoluto da pasta do dispositivo
     *
     * @param id String - Id do usuário
     * @return
     */
    public static String getPathUser(String id) {
        return getPathUsuarios() + "/" + id;
    }

    /**
     * Retorna o caminho absoluto da pasta do contratante
     *
     * @param cnpj String - cnpj do contratante
     * @return
     */
    public static String getPathContratante(String cnpj) {
        return getPathContratantes() + "/" + cnpj;
    }

    /**
     * Retorna o caminho absoluto da pasta de conteúdos a serem enviados para os
     * dispositivos
     *
     * @param cnpj String - cnpj do contratante
     * @return
     */
    public static String getPathContratanteToSend(String cnpj) {
        return getPathContratante(cnpj) + "/enviar-app";
    }

    /**
     * Retorna o caminho absoluto da pasta de conteúdos a serem enviados para o
     * o contratante
     *
     * @param cnpj String - cnpj do contratante
     * @return
     */
    public static String getPathContratanteToReceive(String cnpj) {
        return getPathContratante(cnpj) + "/enviar-contratante";
    }

    /**
     * Retorna o caminho absoluto da pasta /contrantes
     *
     * @return
     */
    public static String getPathContratantes() {
        return PATH_INFOS + "/contratantes";
    }

    /**
     * Retorna o caminho absoluto da pasta /dispositivos
     *
     * @return
     */
    public static String getPathDispositivos() {
        return PATH_INFOS + "/dispositivos";
    }

    /**
     * Retorna o caminho absoluto da pasta /usuarios
     *
     * @return
     */
    public static String getPathUsuarios() {
        return PATH_INFOS + "/usuarios";
    }

    /**
     * Retorna o caminho absoluto da pasta /relacao-assets
     *
     * @return
     */
    public static String getPathRelacaoAssets() {
        return PATH_INFOS + "/relacao-assets";
    }

    /**
     * Retorna de forma coreta o id do dispositivo
     *
     * @param json JSONObject
     * @return
     */
    public static String getIdDevInJson(JSONObject json) {
        return CSPUtilidadesLangJson.getFromJson(json,
                "ID_DEV",
                CSPUtilidadesLangJson.getFromJson(json,
                        "ID_DISPOSITIVO",
                        CSPUtilidadesLangJson.getFromJson(json,
                                "ID_DIS",
                                ""
                        )
                )
        ).trim().toLowerCase();
    }

    /**
     *
     *
     * @param info JSONObject
     * @param sc Socket
     * @param onNeedActionCheckCodSolicitacao
     * @param onNeedActionNeedUpdateStart
     * @param onNeedGetIdUserFromDispositivoLogadoId
     * @param onNeedActionNeedUpdateEnd
     * @param onNeedGetModoRestaurante
     * @param onNeedIsDispositivoLaboratorio
     * @param onNeedGetCurrentValorBonusDev
     * @param hostsCapp
     * @return
     * @throws Exception
     */
    public static JSONObject atendeInfos(
            JSONObject info,
            Socket sc,
            AuxAll onNeedActionCheckCodSolicitacao,
            AuxAll onNeedActionNeedUpdateStart,
            AuxAll onNeedGetIdUserFromDispositivoLogadoId,
            AuxAll onNeedActionNeedUpdateEnd,
            AuxAll onNeedGetModoRestaurante,
            AuxAll onNeedIsDispositivoLaboratorio,
            AuxAll onNeedGetCurrentValorBonusDev,
            String... hostsCapp
    ) throws Exception {
        //    logInfo("App solicitando informações...");
        JSONObject r = new JSONObject();
        String acao = "carga";

        if (info.has("ACAO") && !info.isNull("ACAO")) {
            acao = info.getString("ACAO");
        }

        String idCell = getIdDevInJson(info);
        if (idCell.trim().isEmpty()) {
            return r;
        }
        ultimaMovimentacaoDev(idCell);
        switch (acao) {

            case "check-before-send-pedido":
                if (new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/force-logoff.txt").exists()) {
                    r.put("FORCE_LOGOFF", "s");
                }
                break;
            case "check-cod-solicitacao":
                r.put("STATUS", "no");
                onNeedActionCheckCodSolicitacao.run(r);
                break;

            case "check-garcom":
                if (info.has("PING")) {
                    r.put("STATUS", "ok-garcom");
                    break;
                }

                CSPInstrucoesSQLBase connContra = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE);

                if (info.has("VALIDA") && info.has("CNPJ") && info.has("CPF")) {
                    ResultSet rs = connContra.select("SELECT ATIVO FROM EGULA_DISPOSITIVO_GARCOM WHERE DISPOSITIVO = ? AND CPF = ?", idCell, info.get("CPF"));

                    if (rs.next()) {
                        if ("1".equals(rs.getString("ATIVO"))) {
                            r.put("STATUS", "ok-garcom");
                            break;
                        }
                    }

                    connContra.close();

                } else if (info.has("CNPJ") && !info.isNull("CNPJ") && info.has("CPF") && !info.isNull("CPF")) {
                    if (CSPUtilidadesApplication.getInfosAgenteAtivo().cnpj.equals(info.getString("CNPJ"))) {
                        if (!connContra.exists("EGULA_DISPOSITIVO_GARCOM", "DISPOSITIVO = ? AND CPF = ?", idCell, info.get("CPF"))) {
                            HashMap<String, Object> dados = new HashMap<>();

                            dados.put("ID", null);
                            dados.put("DISPOSITIVO", idCell);
                            dados.put("CPF", info.getString("CPF"));
                            dados.put("ATIVO", "0");

                            connContra.insertComposto("EGULA_DISPOSITIVO_GARCOM", dados);
                        }

                        connContra.close();
                        r.put("STATUS", "ok-garcom");
                        break;
                    }
                }

                r.put("STATUS", "no");
                break;

            case "need-update":
                if (info.has("FULL") && !info.isNull("FULL") && info.getBoolean("FULL")) {
                    //Quando o APP precisa uma atualização FULL
                    new CSPArquivos(CSPKitCapp.getPathDev(idCell) + "/lista-atual.txt").delete();
                    new CSPArquivos(CSPKitCapp.getPathDev(idCell) + "/lista-atual.fjson").delete();

                } else {
                    /**
                     * Quando não for um atualização full, verificamos se o APP
                     * é garçom de algum contratante. Se for, criamos um arquivo
                     * que fará o MG do contratante reiniciar para atualizar a
                     * lista de dispositivos em modo garçom.
                     */
                    try {
                        CSPInstrucoesSQLBase conn = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_APP);

                        if (conn.isOpen()) {
                            ResultSet rs = conn.select((StringBuilder sb) -> {
                                sb.append("SELECT ");
                                sb.append("     CNPJ ");
                                sb.append("FROM ");
                                sb.append("    LOJAS ");
                            });

                            while (rs.next()) {
                                String cnpj = rs.getString("CNPJ");

                                if (isAppGarcomToRestaurante(idCell, cnpj, onNeedGetModoRestaurante)) {
                                    new CSPArquivos(CSPKitCapp.getPathContratanteToReceive(cnpj) + "/restart-to-send-devs").setContent("");
                                    break;
                                }
                            }
                            conn.close();
                        }
                    } catch (Exception e) {
                        CSPException.register(e);
                    }
                }

                if ((info.has("APP_ASSETS_ID") && !info.isNull("APP_ASSETS_ID"))
                        || (info.has("APP_TOKEN_FCM_DEV") && !info.isNull("APP_TOKEN_FCM_DEV"))
                        || (info.has("APP_VERSION_DEV") && !info.isNull("APP_VERSION_DEV"))) {
                    registraDadosGeraisDevSimple(idCell, info);
                }

                final boolean isNewDev = isNewDev(idCell);
                final CSPArquivos inUpdating = new CSPArquivos(CSPKitCapp.getPathDev(idCell) + "/in-update.txt");
                onNeedActionNeedUpdateStart.run(idCell, info, isNewDev);

                /**
                 * Já mandamos para o app as notificações que ele precisa antes
                 * mesmo de processar a sua atualização
                 */
                JSONArray notificacoes = new JSONArray();
                auxSendNotificacoes(CSPKitCapp.getPathDev(idCell), notificacoes);
//                final String idUserFromApp = FrmModuloServerTratamentoUsuarios.getIdUserFromDispositivoLogadoId(idCell);
                final String idUserFromApp = (String) onNeedGetIdUserFromDispositivoLogadoId.run(idCell);

                if (idUserFromApp != null) {
                    auxSendNotificacoes(CSPKitCapp.getPathUser(idUserFromApp), notificacoes);
                }
                /**
                 * Notificações tem prioridade sobre atualizações
                 */
                if (notificacoes.length() > 0) {
                    r.put("NOTIFICACOES", notificacoes);
                    /**
                     * Assim que receber o lote de notificações não precisa mais
                     * ficar na central de 30 em 30seg
                     */
                    r.put("MODO_RESPOSTA", false);
                }

                new CSPArquivos(CSPKitCapp.getPathDev(idCell) + "/fcm-app-need-update-enviado.txt").delete();

                if (isNewDev) {
                    /// FrmModuloServerTratamentoUsuarios.registraStatusLoginUsuario(null, idCell, false);
                    new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/lista-atual.txt").delete();

                    r.put("LOADING", "s");

                    CSPKitCapp.preparaAtualizacaoDev(
                            idCell,
                            true,
                            false,
                            onNeedGetModoRestaurante,
                            onNeedIsDispositivoLaboratorio,
                            onNeedGetCurrentValorBonusDev,
                            hostsCapp
                    );

                    {
                        //para versões do android >= 1.6.4
                        new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/update-infos.txt").setContent("instalacao-inicial");
                        //para versões do android <= 1.6.3
                        CSPKitCapp.registraDadosGeraisDev(idCell, sc.getRemoteSocketAddress().toString(), info);
                    }
                } else {

                    r.put("LOADING", "n");
                }

                if (new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/update-infos.txt").exists()) {
                    r.put("SEND_INFOS_DEV", "s");
                    r.put("ONLY_BASIC", "n");
                }

                if (new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/sobe-lista.txt").exists()) {
                    r.put("SEND_LISTA", "s");
                }

                if (new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/sobe-log.txt").exists()) {
                    r.put("SEND_LOG_URGENTE", "s");
                }

                if (new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/force-logoff.txt").exists()) {
                    r.put("FORCE_LOGOFF", "s");
                }

                if (getVersionApp(idCell).maiorOuIgualA(VersionsAppSupported.ad_1_8_4)) {

                    r.put("SLEEP_ON_DOWNLOAD2", 500);
                    r.put("CALL_GC_ON_DOWNLOAD2", "s");
                    r.put("FORCE_CLEAR_CACHE_IMAGENS", "s");
                }

                int countIni = r.length();

                CSPKitCapp.listaArquivosParaAtualizar(idCell, r, inUpdating.exists());

                if (r.length() == countIni) {
                    /**
                     * Se não foi empregado nada para atualizar conteúdos no
                     * dispositivo vamos pedir pra ele upar o log
                     */
                    r.put("SEND_LOG", "s");
                    //Para resetar em <= 1.3.3
                    r.put("RESET_LOGS", "s");
                } else {
                    inUpdating.setContent(CSPUtilidadesLangDateTime.getTempoCompleto());
                }

//                        if (noIos && idCell.startsWith("io_")) {
//                        if (idCell.startsWith("io_")) {
//                            r.put("SEND_LOG", "n");
//                        }
                onNeedActionNeedUpdateEnd.run(r, idCell);

                break;

            case "update-infos-dev":
                new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/update-infos.txt").delete();
                registraDadosGeraisDev(idCell, sc.getRemoteSocketAddress().toString(), info);
                break;
            case "update-ok":
                //Quando o app finaliza a sua atualização
                CSPArquivosLocais uPa = new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/primeira-atualizacao.txt");
                if (uPa.exists() && !(uPa.getContent() + "").contains("Finalizou")) {
                    uPa.appendContent("Finalizou: " + CSPUtilidadesLangDateTime.getData() + " " + CSPUtilidadesLangDateTime.getHora());
                }
                new CSPArquivosLocaisJson(CSPKitCapp.getPathDev(idCell) + "/para-atualizar.json").setObject(new JSONObject());
                new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/in-update.txt").delete();
                break;
            case "send-lista":
                new CSPArquivosLocaisJson(CSPKitCapp.getPathDev(idCell) + "/lista-atual-dispositivo.json").setArray(CSPUtilidadesLangJson.getFromJson(info, "LISTA", new JSONArray()));
                new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/sobe-lista.txt").delete();
                break;
            case "about-logs":
                //SIZE_LOGS
                new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/infos-sobre-logs.txt")
                        .appendContent(CSPUtilidadesLangDateTime.getTempoCompleto() + "=tamanho-log-dispositivo:" + CSPUtilidadesLangJson.getFromJson(info, "SIZE_LOGS", (long) 0) + CSPUtilidadesSO.LINE_SEPARATOR);
                break;
            case "send-logs":
            case "send-log":
                String lgs = ">>recebido-em:" + CSPUtilidadesLangDateTime.getTempoCompleto() + "<<" + CSPUtilidadesSO.LINE_SEPARATOR;
                lgs += CSPUtilidadesLangJson.getFromJson(info, "LOGS", "").replace("<<n>>", CSPUtilidadesSO.LINE_SEPARATOR);

                new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/logs-dispositivo.log").appendContent(lgs);
                new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/sobe-log.txt").delete();
                new CSPArquivosLocais(CSPKitCapp.getPathDev(idCell) + "/infos-sobre-logs.txt")
                        .appendContent(CSPUtilidadesLangDateTime.getTempoCompleto() + "=log-recebido-sucesso" + CSPUtilidadesSO.LINE_SEPARATOR);
                r.put("RESET_LOGS", "s");
                break;

        }

        //  logInfo("App solicitando informações...OK");
        return r;

    }

    /**
     * Atualiza a configuração que determina o host de comunicação do
     * dispositivo
     *
     *
     * @param id Sting - ID do dispositivo
     * @param host String - Host destinado. Null para anular a configuração
     * atual
     */
    public static void updateHostDefaultInDev(String id, String host) throws Exception {
        if (getVersionApp(id).maiorOuIgualA(VersionsAppSupported.ad_1_7_6)) {
            updateConfsLocalAppFile(id, "DEFAULT_HOST", host);
        }
    }

    /**
     * Atualiza o conteúdo do arquivo de configurações locais do app em questão.
     * Dentro desse arquivo fica armazenada toda a configuração de hosts, modos
     * e afins.
     *
     * Valores de bônus e outros possuem cada um seu arquivo
     *
     * @param id String - ID do dispositivo
     * @param key String - Key de identificacao do valor a ser
     * adicionado/alterado no dispositivo
     * @param val Object - Valor a ser adicionado
     * @throws Exception
     */
    public static void updateConfsLocalAppFile(String id, String key, Object val) throws Exception {
        if (getVersionApp(id).maiorOuIgualA(VersionsAppSupported.ad_1_7_6)) {

            new CSPArquivosJson(
                    CSPKitCapp.getPathDev(id)
                    + "/" + id + "-confs-locais-app.json"
            ).appendObject(key, val);
        }
    }

    /**
     * Extraí o cnpj de um caminho de arquivo comum no capp
     *
     * @param file
     * @return
     */
    public static String extractCnpjFromStringFile(String file) {
        if (file == null || file.trim().isEmpty()) {
            return null;
        }

        file = file.trim();

        if (cnpjFromFileCache.containsKey(file)) {
            return cnpjFromFileCache.get(file);
        }

        String cnpj = file.trim().replaceAll("[^0123456789]", "");
        if (cnpj.length() == 14) {
            cnpjFromFileCache.put(file, cnpj);
            return cnpj;
        }

        if (file.contains("/")) {
            cnpj = file.split("/")[0].trim();
            if (cnpj.length() == 14) {
                cnpjFromFileCache.put(file, cnpj);
                return cnpj;
            }
        }

        if (file.contains("_")) {
            cnpj = file.split("_")[0].trim();
            if (cnpj.length() == 14) {
                cnpjFromFileCache.put(file, cnpj);
                return cnpj;
            }
        }
        cnpjFromFileCache.put(file, null);
        return null;
    }

    /**
     * Adiciona no json de resposta a lista de download para o app e já remove
     * de 'lista-atual.txt' os arquivos que o app deverá excluir
     *
     *
     * @param id String - Id do celular
     * @param r JSONObject - Json de resposta
     * @param isInUpdating boolean - Informa se o app se encontra em um processo
     * de atualização no momento
     */
    private static void listaArquivosParaAtualizar(String id, JSONObject r, boolean isInUpdating) throws Exception {

        if (getVersionApp(id).maiorOuIgualA(ad_1_8_3, io_1_6_9)) {

            CSPArquivosJson tmp = new CSPArquivosJson(CSPKitCapp.getPathDev(id) + "/para-atualizar.json");
            ArrayList<String> add = new ArrayList<>();//Adicionar
            ArrayList<String> rm = new ArrayList<>();//Remover
            if (tmp.exists()) {
                JSONObject a = tmp.getObject();
                if (a != null) {

                    auxListaArquivosParaAtualizar(r, a, add, "ADD");

                    auxListaArquivosParaAtualizar(r, a, rm, "RM");

                    {
                        //para versões do android <= 1.6.3
                        if (a.has("RN") && !a.isNull("RN")) {
                            JSONArray newRn = new JSONArray();
                            for (Object t : a.getJSONArray("RN")) {

                                if (add.contains(((JSONObject) t).getString("D"))) {
                                    //Se não estiver na lista de download não o vamos renomear
                                    newRn.put((JSONObject) t);
                                }
                            }
                            a.put("RN", newRn);
                        }
                    }
                    auxListaArquivosParaAtualizar(r, a, null, "RN");

                    auxListaArquivosParaAtualizar(r, a, null, "LIBERAR_EM");
                }
            }

            return;
        }

        /**
         * É preciso atualizar a lista local de arquivos
         */
        String listaAtualContent = "";
        CSPArquivosLocais listaAtual = new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/lista-atual.txt");
        if (listaAtual.exists()) {
            listaAtualContent = listaAtual.getContent() + "";
        }

        CSPArquivosLocaisJson l = new CSPArquivosLocaisJson(CSPKitCapp.getPathDev(id) + "/para-atualizar.json");
        ArrayList<String> add = new ArrayList<>();//Adicionar
        ArrayList<String> rm = new ArrayList<>();//Remover
        if (l.exists()) {
            JSONObject a = l.getObject();
            if (a != null) {

                {
                    if (a.has("ADD") && !a.isNull("ADD")) {
                        JSONArray newAdd = new JSONArray();
                        for (Object t : a.getJSONArray("ADD")) {
                            /**
                             * isInUpdating: Compreendemos que se chegou true
                             * foi por que ocorreu algum problema na ultima
                             * atualização, que no caso pode ser a mesma
                             * atualmente, e o app voltou para tentar novamente.
                             * Sendo assim precisamos mandar tudo, por garantia
                             */
//                            if (isInUpdating || (t + "").contains("lista-contratante")) {
                            if (isInUpdating || (t + "").contains("lista-contratante") || !listaAtualContent.contains((t + "").trim())) {
                                //Se já estiver na lista de arquivos atuais não o vamos baixar
                                CSPArquivos check = new CSPArquivos(CSPKitCapp.getPathContratantes());
                                check.setPath(check.getFullPath() + "/" + (t + "").trim());
                                trataNomeFileAppToServer(check);
                                if (check.exists() && check.isFile()) {

                                    newAdd.put((t + "").trim());

                                } else {
                                    CSPLog.error(CSPKitCapp.class,
                                            check.getAbsolutePath() + " não encontrado!");
                                }
                            } else if (!isInUpdating && !(t + "").contains("lista-contratante")) {
                                CSPArquivos check = new CSPArquivos(CSPKitCapp.getPathContratantes());
                                if (check.isFile()) {
                                    registraFimDownloadArquivoPreparadoDev(id, check.getName());
                                    CSPLog
                                            .error(CSPKitCapp.class,
                                                    check.getAbsolutePath() + " removido da lista de preparo por estar presente na lista de atuais!");
                                }

                            }
                        }
                        a.put("ADD", newAdd);
                    }
                }

                auxListaArquivosParaAtualizar(r, a, add, "ADD");

                auxListaArquivosParaAtualizar(r, a, rm, "RM");

                {
                    //para versões do android <= 1.6.3
                    if (a.has("RN") && !a.isNull("RN")) {
                        JSONArray newRn = new JSONArray();
                        for (Object t : a.getJSONArray("RN")) {

                            if (add.contains(((JSONObject) t).getString("D"))) {
                                //Se não estiver na lista de download não o vamos renomear
                                newRn.put((JSONObject) t);
                            }
                        }
                        a.put("RN", newRn);
                    }
                }
                auxListaArquivosParaAtualizar(r, a, null, "RN");

                auxListaArquivosParaAtualizar(r, a, null, "LIBERAR_EM");
            }
        }
        //l.setObject(new JSONObject());

        /**
         * Aqui só vamos 'remover' da lista o que o app deverá remover ao
         * efetuar o download
         */
        for (String ln : listaAtualContent.split(LINE_SEPARATOR)) {
            if (rm.contains(ln.trim()) && !add.contains(ln.trim())) {
                listaAtualContent = listaAtualContent.replace(ln + LINE_SEPARATOR, "");

            }
        }

        //Replace para remover as linhas em branco
        listaAtual.setContent(listaAtualContent.replaceAll("(?m)^[ \t]*\r?\n", ""));

    }

    /**
     * Retorna se o app é um vendedor externo do contratante
     *
     * @param id Sting - ID do dispositivo
     * @param cnpj String - CNPJ do restaurante
     * @param onNeedGetModoRestaurante
     * @return
     * @throws java.lang.Exception
     */
    public static boolean isAppVendedorExternoToRestaurante(String id, String cnpj, AuxAll onNeedGetModoRestaurante) throws Exception {

        ModoRestaurante modo = (ModoRestaurante) onNeedGetModoRestaurante.run(cnpj);

        if (modo.equals(ModoRestaurante.RESTRITO_AUTORIZACAO)) {
            CSPArquivosJson liberados = new CSPArquivosJson(CSPKitCapp.getPathContratante(cnpj) + "/dispositivos-liberados.json");
            if (liberados.exists() && liberados.isFile()) {

                JSONObject o = CSPUtilidadesLangJson.getFromJson(liberados.getObject(), id, (JSONObject) null);
                if (o != null) {
                    return CSPUtilidadesLangJson.getFromJson(o, "IS_VENDEDOR_EXTERNO", "n").equals("s");
                }

            }
        }

        return false;
    }

    /**
     * Retorna se o app é um garçom externo do contratante
     *
     * @param id Sting - ID do dispositivo
     * @param cnpj String - CNPJ do restaurante
     * @param onNeedGetModoRestaurante AuxAll
     * @return
     * @throws java.lang.Exception
     */
    public static boolean isAppGarcomToRestaurante(String id, String cnpj, AuxAll onNeedGetModoRestaurante) throws Exception {

        ModoRestaurante modo = (ModoRestaurante) onNeedGetModoRestaurante.run(cnpj);

        if (modo.equals(ModoRestaurante.GARCOM)
                || modo.equals(ModoRestaurante.GARCOM_TESTES)) {
            CSPArquivosJson liberados = new CSPArquivosJson(CSPKitCapp.getPathContratante(cnpj) + "/dispositivos-liberados.json");
            if (liberados.exists() && liberados.isFile()) {

                JSONObject o = CSPUtilidadesLangJson.getFromJson(liberados.getObject(), id, (JSONObject) null);
                if (o != null) {
                    return CSPUtilidadesLangJson.getFromJson(o, "IS_GARCOM", "n").equals("s");
                }

            }
        }

        return false;
    }

    /**
     * Converte o padrão do nome do arquivo usado no servidor para o caminho
     * usado no app para efetuar o download.
     *
     * Exemplo:
     * /home/casa/central-app/infos-app/contratantes/141257000161/enviar-app/imagem.png
     * => 141257000161_e_imagem.png
     *
     * @param file CSPArquivo
     * @throws Exception
     */
    public static String trataNomeFileServerToApp(CSPArquivos file) throws Exception {
        final String abls = file.getAbsolutePath();
        return !(abls.contains("/enviar-app/") || abls.contains("/central-imagens/"))
                ? file.getName()
                : file.getAbsolutePath()
                        .replace(getPathContratantes() + "/", "")
                        .replace("/", "_")
                        .replace("_enviar-app_", "_e_");
// .replace("central-imagens_", "ci_")
    }

    /**
     * Converte o padrão do nome do arquivo usado no app para o caminho usado no
     * server.
     *
     * Exemplo: 141257000161_e_imagem.png =>
     * /home/casa/central-app/infos-app/contratantes/141257000161/enviar-app/imagem.png
     *
     * @param arquivo CSPArquivo
     * @throws Exception
     */
    public static void trataNomeFileAppToServer(CSPArquivos arquivo) throws Exception {
        final String name = arquivo.getName();
        if (name.endsWith("app.json")
                || name.contains("-bonus")
                || name.contains("-logado")) {
            arquivo.setPath(CSPKitCapp.getPathDev(name.split("-")[0]) + "/" + name);//Vamos ter aqui o id dele

        } else {
            if (name.contains("__enviar-app__")) {//Antigo
                arquivo.setPath(CSPKitCapp.getPathContratantes() + "/" + name.replace("__enviar-app__", "/enviar-app/"));
            } else {
                arquivo.setPath(CSPKitCapp.getPathContratantes() + "/" + name.replace("_e_", "/enviar-app/"));
            }
        }

        if (!arquivo.exists() && !arquivo.isFile()) {
            final String ablsBk = arquivo.getAbsolutePath();
            final String ablsNew = ablsBk.replace(
                    name,
                    name.replace("_", "/")
            );
            arquivo.setPath(ablsNew);
            if (!arquivo.isFile()) {
                arquivo.setPath(ablsBk);
            }
        }

    }

    /**
     * Método para auxiliar alimentar as variáveis do método
     * {@link #listaArquivosParaAtualizar(java.lang.String, org.json.JSONObject) }
     *
     * @param r
     * @param o
     * @param arr
     * @param key
     */
    private static void auxListaArquivosParaAtualizar(JSONObject r, JSONObject o, ArrayList<String> arr, String key) {
        if (o.has(key) && !o.isNull(key)) {
            r.put(key, o.get(key));
            if (arr != null) {

                for (Object t : o.getJSONArray(key)) {
                    arr.add(("" + t).trim());
                }
            }
        }
    }

    /**
     *
     * Registra que foi preparado conteúdo para ser baixado pelo app
     *
     * @param idDev String - ID do dispositivo
     * @param files LinkedHashSet<String> - Nomes dos arquivos a serem baixados
     * @param liberarEm String - Qual arquivo será o ultimo a ser baixado antes
     * de liberar o usuário
     * @throws Exception
     */
    public static void registraPreparoArquivosDownloadDev(String idDev, boolean isNew, LinkedHashSet<String> files, String liberarEm) throws Exception {
        /**
         * Dessa forma ganhamos velocidade
         */
        final CSPArquivosLocais o = new CSPArquivosLocais(CSPKitCapp.getPathDev(idDev) + "/atualizacao.txt");
        o.appendContent(new JSONObject() {
            {
                put("HORARIO_PREPARO", CSPUtilidadesLangDateTime.getTempoCompleto());
                put("IS_INICIAL", isNew);
                put("LISTA", files);
                put("LIBERAR_EM", liberarEm);

            }
        }.toString() + CSPUtilidadesSO.LINE_SEPARATOR);

    }

    /**
     * Registra o inicio do download do arquivo pelo app
     */
    public static synchronized void registraInicioDownloadArquivoPreparadoDev(String idDev, String file, long size, String ip) throws IOException {
        if (idDev != null && !idDev.trim().isEmpty() && !idDev.equals("?")) {
            final CSPArquivosLocais o = new CSPArquivosLocais(CSPKitCapp.getPathDev(idDev) + "/atualizacao.txt");
            o.appendContent(new JSONObject() {
                {
                    put("HORARIO_INICIO_DOWNLOAD", CSPUtilidadesLangDateTime.getTempoCompleto());
                    put("ARQUIVO", file);
                    put("IP", ip);
                    put("TAMANHO", "" + size);

                }
            }.toString() + CSPUtilidadesSO.LINE_SEPARATOR);
        }
    }

    /**
     * Registra o fim do download do arquivo pelo app
     *
     * @param idDev
     * @param file
     * @throws java.lang.Exception
     */
    public static synchronized void registraFimDownloadArquivoPreparadoDev(String idDev, String file) throws Exception {
        if (idDev != null && !idDev.trim().isEmpty() && !idDev.equals("?")) {

            final CSPArquivosLocais o = new CSPArquivosLocais(CSPKitCapp.getPathDev(idDev) + "/atualizacao.txt");
            o.appendContent(new JSONObject() {
                {
                    put("HORARIO_FIM_DOWNLOAD", CSPUtilidadesLangDateTime.getTempoCompleto());
                    put("ARQUIVO", file);

                }
            }.toString() + CSPUtilidadesSO.LINE_SEPARATOR);

//                    if (idDev.equals("ad_lgd85534392548")) {
            if (getVersionApp(idDev).maiorOuIgualA(ad_1_8_3, io_1_6_9)) {
                new CSPArquivosJsonObjectFast(getPathDev(idDev) + "/lista-atual.fjson")
                        .appendObject(file, new CSPArquivos(file).getMd5());
            } else {
                new CSPArquivosLocais(CSPKitCapp.getPathDev(idDev) + "/lista-atual.txt")
                        .appendContent(FilenameUtils.getName(file.replace("/enviar-app/", "_e_")) + CSPUtilidadesSO.LINE_SEPARATOR);
            }
        }
    }

    /**
     * Analisa o JSON informado e registra as informações pelo app enviadas
     *
     * @param idDev
     * @param data
     * @throws java.lang.Exception
     */
    public static void registraDadosGeraisDevSimple(String idDev, JSONObject data) throws Exception {
        if (idDev != null && !idDev.trim().isEmpty() && !idDev.equals("?")) {

            final CSPArquivos o = new CSPArquivos();
            //version
            final String version = CSPUtilidadesLangJson.getFromJson(data, "APP_VERSION_DEV", "0.0.0");
            if (version != null && !version.equals("0.0.0")) {
                o.setPath(CSPKitCapp.getPathDev(idDev) + "/current-version-app.txt");
                o.setContent(version);
                appVersaoCache.remove(idDev);
            }

            //Token api Google Firebase Cloud Messaging 
            final String token = CSPUtilidadesLangJson.getFromJson(data, "APP_TOKEN_FCM_DEV", "?");
            if (token != null && !token.equals("?")) {
                o.setPath(CSPKitCapp.getPathDev(idDev) + "/current-token-fcm-app.txt");
                o.setContent(token);
            }

            //ID do compilado de assets
            final String assets = CSPUtilidadesLangJson.getFromJson(data, "APP_ASSETS_ID", "?");
            if (assets != null && !assets.equals("?")) {
                o.setPath(CSPKitCapp.getPathDev(idDev) + "/current-assets-id-app.txt");
                o.setContent(assets);
            }
        }
    }

    /**
     * Analisa o JSON informado e registra as informações pelo app enviadas
     *
     * @param idDev
     * @param ip
     * @param data
     * @throws java.lang.Exception
     */
    public static void registraDadosGeraisDev(String idDev, String ip, JSONObject data) throws Exception {
        if (idDev != null && !idDev.trim().isEmpty() && !idDev.equals("?")) {

            new CSPArquivos(CSPKitCapp.getPathDev(idDev) + "/dados-gerais.txt")
                    .appendContent(new JSONObject() {
                        {
                            put("HORARIO", CSPUtilidadesLangDateTime.getTempoCompleto());
                            put("IP", ip);
                            put("APP_VERSION_DEV", CSPUtilidadesLangJson.getFromJson(data, "APP_VERSION_DEV", (String) null));
                            put("APP_TOKEN_FCM_DEV", CSPUtilidadesLangJson.getFromJson(data, "APP_TOKEN_FCM_DEV", (String) null));
                            put("TELEFONE", CSPUtilidadesLangJson.getFromJson(data, "TELEFONE_DEV", (String) null));
                            put("OPERADORA", CSPUtilidadesLangJson.getFromJson(data, "OPERADORA_DEV", (String) null));
                            put("MODELO", CSPUtilidadesLangJson.getFromJson(data, "MODEL_DEV", (String) null));
                            put("FABRICANTE", CSPUtilidadesLangJson.getFromJson(data, "MANUFACTURER_DEV", (String) null));
                            put("SO_VERSION", CSPUtilidadesLangJson.getFromJson(data, "SO_VERSION_DEV", (String) null));
                            put("SO", CSPUtilidadesLangJson.getFromJson(data, "SO_DEV", (String) null));
                            put("SDV_VERSION", CSPUtilidadesLangJson.getFromJson(data, "SDK_DEV", (String) null));
                            put("LATITUDE", CSPUtilidadesLangJson.getFromJson(data, "LATITUDE_DEV", 0.0));
                            put("LONGITUDE", CSPUtilidadesLangJson.getFromJson(data, "LONGITUDE_DEV", 0.0));

                        }
                    }.toString() + CSPUtilidadesSO.LINE_SEPARATOR);

            registraDadosGeraisDevSimple(idDev, data);

        }
    }

    private static void auxSendNotificacoes(String idFolder, JSONArray add) throws Exception {

        final CSPArquivosJson src = new CSPArquivosJson(idFolder + "/notificacoes-pendentes.json");

        if (src.exists() && src.getArray() != null) {

            final JSONArray a = src.getArray();

            for (int i = 0; i < a.length(); i++) {
                add.put(a.getJSONObject(i));
            }

            src.setArray(new JSONArray());
        }

    }

    /**
     * 'Cadastra' uma notificação da central para que o DISPOSITIVO assim que
     * possível possa recebe-la
     *
     * @param idDev String - ID do app
     * @param msg String - Mensagem do a ser enviada
     * @throws Exception
     */
    public static void addNotificacaoDevAvisoExtra(String idDev, String msg) throws Exception {
        addNotificacaoDevAvisoExtra(idDev, msg, 0);
    }

    /**
     * 'Cadastra' uma notificação da central para que o DISPOSITIVO assim que
     * possível possa recebe-la
     *
     * @param idDev String - ID do app
     * @param msg String - Mensagem do a ser enviada
     * @param moreBonus double - Bônus adicional
     * @throws Exception
     */
    public static void addNotificacaoDevAvisoExtra(String idDev, String msg, double moreBonus) throws Exception {
        addNotificacaoDev(idDev, "05", "egula", new JSONArray() {
            {
                put(msg);
            }
        }, moreBonus > 0, moreBonus);
    }

    /**
     * 'Cadastra' uma notificação na central para que o DISPOSITIVO assim que
     * possível possa recebe-la
     *
     * @param idDev String - ID do app
     * @param idMes String - Código da mensagem do restaurante
     * @param resp String - CNPJ do restaurante responsável pela mensagem
     * @param parms JSONArray - Valores das marcações da mensagem
     * @throws Exception
     */
    public static void addNotificacaoDev(String idDev, String idMes, String resp, JSONArray parms) throws Exception {
        addNotificacaoDev(idDev, idMes, resp, parms, false, 0);
    }

    /**
     *
     * 'Cadastra' uma notificação na central para que o DISPOSITIVO assim que
     * possível possa recebe-la
     *
     * @param idDev String - ID do app
     * @param idMes String - Código da mensagem do restaurante
     * @param resp String - CNPJ do restaurante responsável pela mensagem
     * @param parms JSONArray - Valores das marcações da mensagem
     * @param isToBonus boolean - Define que a notifcação irá alterar o valor do
     * bonus do app
     * @param newBonus double - Novo valor do bonus do app, caso o for realmente
     * alterado
     * @throws Exception
     */
    public static void addNotificacaoDev(String idDev, String idMes, String resp, JSONArray parms, boolean isToBonus, double newBonus) throws Exception {
        new CSPArquivosLocaisJson(CSPKitCapp.getPathDev(idDev) + "/notificacoes-pendentes.json").appendArray(new JSONArray().put(new JSONObject() {
            {
                put("ID_MENSAGEM", idMes);
                put("ID_CONTRATANTE", resp);
                put("PARMS_MENSAGEM", parms);
                if (isToBonus) {
                    put("NEW_BONUS", newBonus);
                }
            }
        }));
    }

    /**
     * Atualiza o arquivo "lista-contratantes".
     *
     * Esse arquivo dita a lista de contratantes disponíveis no app
     *
     * @param con CSPInstrucoesSQLBase - Conexão com base do contratante
     * @param cnpj String - CNPJ do contratante a ser gerado
     * @param okPelaServico
     * @param name
     * @param isAcessoRestrito
     * @param onNeedBeforeSaveLista
     * @throws java.lang.Exception
     */
    public static void updateListaContratantesFile(
            CSPInstrucoesSQLBase con,
            String cnpj,
            boolean okPelaServico,
            String name,
            boolean isAcessoRestrito,
            AuxAll onNeedBeforeSaveLista
    ) throws Exception {

        final CSPArquivosLocais toSave = new CSPArquivosLocais(CSPKitCapp.getPathContratanteToReceive(cnpj) + "/imagens-nao-encontradas-lista-contratantes.txt");
        final CSPArquivosLocais pathImgs = new CSPArquivosLocais(CSPKitCapp.getPathContratanteToSend(cnpj));
        toSave.delete();

        CSPLog
                .info(CSPKitCapp.class,
                        "Atualizando arquivo 'lista-contratantes', registro contratante " + cnpj + "...");

        JSONArray d = CSPUtilidadesLangJson.getArray(listaContratantesFull.getContent());
        JSONArray old = d == null ? new JSONArray() : d;
        int index = -1;

        for (int i = 0; i < old.length(); i++) {
            if (old.getJSONObject(i).getString("ID").equals(cnpj)) {
                index = i;
                break;
            }
        }

        ResultSet se = con.select((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("     r.FANTASIA, ");
            sb.append("     r.APP_BANNER_PRINCIPAL, ");
            sb.append("     r.APP_BARRA_SUPERIOR, ");
            sb.append("     r.APP_NOTIFICACAO, ");
            sb.append("     r.APP_APRESENTACAO_BANNER, ");
            sb.append("     r.APP_BANNER_COMPARTILHADA ");
            sb.append("FROM ");
            sb.append("     AGENTES r ");
            sb.append("WHERE ");
            sb.append("     r.CNPJ = ?");
        }, cnpj);

        if (okPelaServico && se.next()) {

            JSONObject o = new JSONObject();

            {//Dados básicos

//            o.put("DESTAQUE", "1".equals(se.getString("APP_APRESENTACAO_BANNER")) ? "s" : "n");
                o.put("DESTAQUE", "n");
                o.put("ID", cnpj);
                o.put("NOME", name == null ? se.getString("FANTASIA") : name);

                if (isAcessoRestrito) {
                    o.put("IS_ACESSO_RESTRITO", "s");
                }

                o.put("IMAGEM", buildSrcImgApp(se.getString("APP_BANNER_COMPARTILHADA"), cnpj, "AGENTES", cnpj, toSave, pathImgs));

                if (!pathImgs.exists() || !pathImgs.isFile()) {
                    o.put("IMAGEM", buildSrcImgApp(se.getString("APP_BANNER_PRINCIPAL"), cnpj, "AGENTES", cnpj, toSave, pathImgs));
                }

                o.put("IMAGEM_FULL", buildSrcImgApp(se.getString("APP_BANNER_PRINCIPAL"), cnpj, "AGENTES", cnpj, toSave, pathImgs));
                o.put("ICONE", buildSrcImgApp(se.getString("APP_BARRA_SUPERIOR"), cnpj, "AGENTES", cnpj, toSave, pathImgs));
                o.put("ICONE_SECUNDARIO", buildSrcImgApp(se.getString("APP_NOTIFICACAO"), cnpj, "AGENTES", cnpj, toSave, pathImgs));
            }

            {//Acréscimos
                final JSONArray arr = new JSONArray();
                se = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     a.ACRESCIMO_CODIGO, ");
                    sb.append("     COALESCE(a.DESCRICAO, '') AS DESCRICAO, ");
                    sb.append("     iif(a.COBRANCA_MODELO = 1,'s','n') AS IN_PORCENT, ");
                    sb.append("     iif(a.COBRANCA_MODELO = 1,ah.ACRESCIMO_PORCENTO,a.COBRANCA_VALOR) AS VAL, ");
                    sb.append("     ah.HORA_INICIAL, ");
                    sb.append("     ah.HORA_FINAL ");
                    sb.append("FROM ");
                    sb.append("     ACRESCIMOS a ");
                    sb.append("     LEFT JOIN ");
                    sb.append("         ADICIONAL_HORARIO ah ");
                    sb.append("             ON a.ACRESCIMO_CODIGO = ah.ACRESCIMO_CODIGO ");

                });

                while (se.next()) {
                    JSONObject t = new JSONObject();
                    t.put("INICIO", se.getString("HORA_INICIAL"));
                    t.put("FIM", se.getString("HORA_FINAL"));
                    t.put("VAL", se.getDouble("VAL"));
                    t.put("DESCRICAO", se.getString("DESCRICAO"));
                    t.put("ID", se.getString("ACRESCIMO_CODIGO"));
                    t.put("IN_PORCENT", se.getString("IN_PORCENT"));
                    arr.put(t);
                }

                o.put("ACRESCIMOS", arr);
            }

            {//Formas de pagamento
                final JSONArray arr = new JSONArray();

                se = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("    fp.FPAGAMENTO_CODIGO, ");
                    sb.append("    fp.DESCRICAO, ");
                    sb.append("    iif(LOWER(fp.TROCO) = 'x','s',null) AS WITH_TROCO ");
                    sb.append("FROM ");
                    sb.append("    FORMAS_PAGAMENTO fp ");
                    sb.append("WHERE ");
                    sb.append("    LOWER(fp.APP) = 'x' ");
                    sb.append("ORDER BY ");
                    sb.append("    fp.TROCO DESC, fp.FPAGAMENTO_CODIGO ASC ");
                });

                while (se.next()) {
                    JSONObject t = new JSONObject();
                    t.put("ID", se.getString("FPAGAMENTO_CODIGO"));
                    t.put("DESCRICAO", se.getString("DESCRICAO"));
                    t.put("WITH_TROCO", se.getString("WITH_TROCO"));
                    arr.put(t);
                }

                //Temporário
                JSONObject tt = new JSONObject();
                tt.put("ID", "eb");//eb de 'E-Gula Bônus', OK??
                tt.put("DESCRICAO", "Bônus");
                arr.put(tt);

                o.put("FORMAS_PAGAMENTO", arr);
            }

            { //Formas de entrega
                final JSONArray arr = new JSONArray();
                se = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("    r.EPEDIDO_CODIGO, ");
                    sb.append("    r.ACRESCIMO_CODIGO,");
                    sb.append("    r.DESCRICAO ");
                    sb.append("FROM ");
                    sb.append("    ENTREGA_PEDIDO r ");
                    sb.append("WHERE ");
                    sb.append("     LOWER(r.APP) = 'x'");
                });

                while (se.next()) {
                    JSONObject t = new JSONObject();
                    t.put("ID", se.getString("EPEDIDO_CODIGO"));
                    t.put("DESCRICAO", se.getString("DESCRICAO"));
                    t.put("ACRES_ID", se.getString("ACRESCIMO_CODIGO"));
                    //Temporário
                    if (se.getString("DESCRICAO").toLowerCase().endsWith("meu endereço")) {
                        t.put("SHOW_FORMAS_PAGAMENTO", "s");
                        t.put("IMAGEM", "ic_entrega.png");
                    } else {
                        t.put("IMAGEM", "ic_mercado.png");
                    }
                    arr.put(t);
                }

                o.put("FORMAS_ENTREGA", arr);
            }

            {//Horários Atendimento

                //Tabela Antiga: AGENTES_ATENDIMENTO. 29/11/16
                //Esse if deve ser removido assim que todos os contratantes 
                //estiverem com as bases atualziadas, com a nova tabela de horários.
                if (CSPUtilidadesLangInstrucoesSQL.hasTabela(con, "AGENTES_ATENDIMENTO")) {
                    final JSONArray arr = new JSONArray();

                    se = con.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("    r.HORARIO_CODIGO, ");
                        sb.append("    CASE r.CONFIGURACAO_SEMANA ");
                        sb.append("        WHEN '4' THEN '2' ");
                        sb.append("        WHEN '3' THEN '1' ");
                        sb.append("        WHEN '2' THEN '7' ");
                        sb.append("        WHEN '1' THEN '6' ");
                        sb.append("    ELSE '1,2,3,4,5,6' ");
                        sb.append("    END DIA_SEMANA_APLICAR, ");
                        sb.append("    r.HORARIO_INICIAL, ");
                        sb.append("    r.HORARIO_FINAL, ");
                        sb.append("    COALESCE(r.VALOR_ACRESCIMO,0) AS VALOR_ACRESCIMO, ");
                        sb.append("    CASE r.ENTREGA_FORMA ");
                        sb.append("        WHEN '3' THEN 'Entrega Somente' ");
                        sb.append("        WHEN '2' THEN 'Atendimento Somente' ");
                        sb.append("        WHEN '1' THEN 'Atendimento e Entrega' ");
                        sb.append("    ELSE 'Sem Atendimento' ");
                        sb.append("    END ENTREGA_FORMA_DESC ");
                        sb.append("FROM ");
                        sb.append("    AGENTES_ATENDIMENTO r ");
                    });

                    boolean hasContent = false;

                    while (se.next()) {
                        hasContent = true;

                        JSONObject t = new JSONObject();
                        t.put("ID", se.getString("HORARIO_CODIGO"));
                        t.put("DESCRICAO", se.getString("ENTREGA_FORMA_DESC"));
                        t.put("DIA_SEMANA_APLICAR", se.getString("DIA_SEMANA_APLICAR"));
                        t.put("INICIO", se.getString("HORARIO_INICIAL"));
                        t.put("FIM", se.getString("HORARIO_FINAL"));
                        t.put("VAL", se.getString("VALOR_ACRESCIMO"));

                        arr.put(t);
                    }

                    if (hasContent) {
                        o.put("HORARIOS_ATENDIMENTO", arr);
                    }
                }

                //Tabela Nova: ADM_CONTRATANTE_HORARIOS. 29/11/16
                if (CSPUtilidadesLangInstrucoesSQL.hasTabela(con, "ADM_CONTRATANTE_HORARIOS")) {
                    final JSONArray arr = new JSONArray();

                    se = con.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("    r.HORARIO_CODIGO, ");
                        sb.append("    CASE r.DIA_SEMANA ");
                        sb.append("        WHEN '1' THEN '2' ");
                        sb.append("        WHEN '2' THEN '3' ");
                        sb.append("        WHEN '3' THEN '4' ");
                        sb.append("        WHEN '4' THEN '5' ");
                        sb.append("        WHEN '5' THEN '6' ");
                        sb.append("        WHEN '6' THEN '7' ");
                        sb.append("        WHEN '7' THEN '1' ");
                        sb.append("    ELSE '1,2,3,4,5,6,7' ");
                        sb.append("    END DIA_SEMANA_APLICAR, ");
                        sb.append("    r.HORARIO_INICIAL, ");
                        sb.append("    r.HORARIO_FINAL, ");
                        sb.append("    CASE r.ATENDIMENTO_TIPO ");
                        sb.append("        WHEN '1' THEN 'Somente Balcão' ");
                        sb.append("        WHEN '2' THEN 'Balcão e Entrega' ");
                        sb.append("    ELSE 'Sem Atendimento' ");
                        sb.append("    END ENTREGA_FORMA_DESC ");
                        sb.append("FROM ");
                        sb.append("    ADM_CONTRATANTE_HORARIOS r ");
                    });

                    boolean hasContent = false;

                    while (se.next()) {
                        hasContent = true;

                        JSONObject t = new JSONObject();
                        t.put("ID", se.getString("HORARIO_CODIGO"));
                        t.put("DESCRICAO", se.getString("ENTREGA_FORMA_DESC"));
                        t.put("DIA_SEMANA_APLICAR", se.getString("DIA_SEMANA_APLICAR"));
                        t.put("INICIO", se.getString("HORARIO_INICIAL"));
                        t.put("FIM", se.getString("HORARIO_FINAL"));
                        t.put("VAL", "0.00");

                        arr.put(t);
                    }

                    if (hasContent) {
                        o.put("HORARIOS_ATENDIMENTO", arr);
                    }
                }

                if (CSPUtilidadesLangInstrucoesSQL.hasTabela(con, "ADM_CONTRATANTE_HORARIOS_ESPECI")) {
                    final JSONArray arr = new JSONArray();

                    se = con.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("    r.ESPECIAL_CODIGO, ");
                        sb.append("    r.DATA AS DIA_ESPECIAL, ");
                        sb.append("    r.HORARIO_INICIAL, ");
                        sb.append("    r.HORARIO_FINAL, ");
                        sb.append("    r.ATENDIMENTO_TIPO AS ENTREGA_FORMA_COD, ");
                        sb.append("    CASE r.ATENDIMENTO_TIPO ");
                        sb.append("        WHEN '1' THEN 'Somente Balcão' ");
                        sb.append("        WHEN '2' THEN 'Balcão e Entrega' ");
                        sb.append("    ELSE 'Sem Atendimento' ");
                        sb.append("    END ENTREGA_FORMA_DESC ");
                        sb.append("FROM ");
                        sb.append("    ADM_CONTRATANTE_HORARIOS_ESPECI r ");
                        sb.append("WHERE ");
                        sb.append("     DATA >= 'NOW' ");
                    });

                    LinkedHashSet<Integer> diasAplicar = new LinkedHashSet<>();

                    while (se.next()) {
                        String msg;

                        if (o.has("HORARIOS_ATENDIMENTO")) {
                            JSONArray ar = o.getJSONArray("HORARIOS_ATENDIMENTO");

                            Calendar c = Calendar.getInstance();
                            c.setTime(se.getDate("DIA_ESPECIAL"));
                            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

                            for (int k = 0; k < ar.length(); k++) {
                                JSONObject b = ar.getJSONObject(k);

                                if (b.getString("DIA_SEMANA_APLICAR").contains(String.valueOf(dayOfWeek)) && !diasAplicar.contains(Integer.parseInt(b.getString("DIA_SEMANA_APLICAR").trim()))) {
                                    diasAplicar.add(Integer.parseInt(b.getString("DIA_SEMANA_APLICAR").trim()));

                                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                                    if (se.getString("ENTREGA_FORMA_COD").trim().equals("3")) {
                                        msg = "1";

                                    } else if (sdf.parse(se.getString("HORARIO_INICIAL")).getTime() > sdf.parse(b.getString("INICIO")).getTime()
                                            || sdf.parse(se.getString("HORARIO_FINAL")).getTime() < sdf.parse(b.getString("FIM")).getTime()) {
                                        msg = "2";

                                    } else {
                                        continue;
                                    }

                                    JSONObject t = new JSONObject();
                                    t.put("ID", se.getString("ESPECIAL_CODIGO"));
                                    t.put("DESCRICAO", se.getString("ENTREGA_FORMA_DESC"));
                                    t.put("DIA_ESPECIAL", se.getDate("DIA_ESPECIAL"));
                                    t.put("INICIO", se.getString("HORARIO_INICIAL"));
                                    t.put("FIM", se.getString("HORARIO_FINAL"));
                                    t.put("VAL", "0.00");
                                    t.put("MSG", msg);

                                    arr.put(t);
                                }
                            }
                        }
                    }

                    o.put("HORARIOS_ATENDIMENTO_ESPECIAL", arr);
                }

            }

            {//Mesas

                if (CSPUtilidadesLangInstrucoesSQL.hasTabela(con, "MESAS")) {
                    final JSONArray arr = new JSONArray();

                    se = con.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("    r.MESA_CODIGO, ");
                        sb.append("    COALESCE(r.MESA_TITULO,r.MESA_CODIGO) AS MESA_TITULO, ");
                        sb.append("    r.LUGARES ");
                        sb.append("FROM ");
                        sb.append("    MESAS r ");
                    });

                    while (se.next()) {

                        JSONObject t = new JSONObject();
                        t.put("ID", se.getString("MESA_CODIGO"));
                        t.put("TITULO", se.getString("MESA_TITULO"));
                        t.put("CAPACIDADE", se.getInt("LUGARES"));
                        /**
                         * Quantidade de colunas que deve ocupar na linha no
                         * app.
                         *
                         * De momento equivale a quantidade de lugares
                         */
                        t.put("SPAN_SIZE", se.getInt("LUGARES"));

                        arr.put(t);
                    }

                    o.put("MESAS", arr);
                    /**
                     * É preciso informar ao app qual será o número máximo de
                     * colunas usado
                     */
//                    o.put("MESAS_MAX_SPAN_SIZE", con.max("MESAS", "LUGARES", null));
                    o.put("MESAS_MAX_SPAN_SIZE", 6);

                }

            }

            if (index != -1) {
                old.put(index, o);
            } else {
                old.put(o);
            }

        } else {
            /**
             * Se entrou aqui é por que o restaurante não existe ou não pode
             * aparecer no app.
             *
             * Caso ele já esteja aparecendo vamos remover ele, pois entendemos
             * que desse momento em diante ele não está mais habilitado
             */
            if (index != -1) {
                old.remove(index);
            }
        }

        final String tmp = onNeedBeforeSaveLista.run(old).toString();
        listaContratantesFull.delete();
        listaContratantesFull.setContent(tmp);
        CSPLog
                .info(CSPKitCapp.class,
                        "Atualizando arquivo 'lista-contratantes', registro contratante " + cnpj + "...OK");
    }

    /**
     * Novo preparador de atualização de conteúdo para dispositivos
     *
     * @param id String - Id do celular
     * @param isNewDev boolean - Se é um dispositivo novo
     * @param isForceUpdate boolean - Se é pra forçar a atualização
     * @param onNeedGetModoRestaurante
     * @param onNeedIsDispositivoLaboratorio
     * @param onNeedGetCurrentValorBonusDev
     * @param hostsCapp
     * @throws java.lang.Exception
     */
    public static synchronized void preparaAtualizacaoDevNew(
            String id,
            boolean isNewDev,
            boolean isForceUpdate,
            AuxAll onNeedGetModoRestaurante,
            AuxAll onNeedIsDispositivoLaboratorio,
            AuxAll onNeedGetCurrentValorBonusDev,
            String... hostsCapp
    ) throws Exception {
        final CSPArquivosJson listaContratantesApp = new CSPArquivosJson(CSPKitCapp.getPathDev(id));
        final CSPArquivosJson regiaoCidadesApp = new CSPArquivosJson();
        final CSPArquivosJson listaHostsApp = new CSPArquivosJson();
        final CSPArquivosJson confsLocaisApp = new CSPArquivosJson(CSPKitCapp.getPathDev(id) + "/" + id + "-confs-locais-app.json");
        final CSPArquivosJsonObjectFast listaAtualApp = new CSPArquivosJsonObjectFast(CSPKitCapp.getPathDev(id) + "/lista-atual.fjson");
        final boolean isDevLab = (boolean) onNeedIsDispositivoLaboratorio.run(id);
        final VersionsAppSupported versionApp = getVersionApp(id);
        final LinkedHashSet<String> add = new LinkedHashSet<>();//Adicionar
        final LinkedHashSet<String> rm = new LinkedHashSet<>();//Remover
        final LinkedHashSet<JSONObject> rn = new LinkedHashSet<>();//Renomear

        if (isNewDev) {//tmp
            final CSPArquivos tmp = new CSPArquivos();

            for (Map.Entry<String, String> e
                    : buildListRelationLastMd5(id)
                            .entrySet()) {

                tmp.setPath(e.getKey());
                tmp.delete();

                tmp.setPath(e.getValue());
                tmp.delete();

            }
        }

        {// Uso dos assets

            if (isNewDev && getVersionApp(id).maiorOuIgualA(ad_2_1_3)) {
                /**
                 * Partindo dessa versão possuímos no dispositivo boa parte das
                 * imagens que ele vai precisaria baixar.
                 *
                 * Dessa forma diminuímos a carga sobre os nossos servidores.
                 *
                 * Para tal usamos um ID de controle, para saber o que o
                 * dispositivo vai possuir de conteúdo em seu compilado
                 */
                final CSPArquivos assets = new CSPArquivos(CSPKitCapp.getPathDev(id) + "/current-assets-id-app.txt");

                if (assets.isFile()) {
                    final String idAssets = assets.getContent();

                    assets.setPath(getPathRelacaoAssets() + "/" + idAssets + "/relacao.json");
                    if (assets.isFile()) {

                        JSONObject object = CSPUtilidadesLangJson.getObject(assets.getContent());

                        for (Object n : object.names()) {
                            String key = (String) n;
                            listaAtualApp.appendObject(key, object.get(key));

                        }

                        CSPLog.info(CSPKitCapp.class,
                                "lista-atual-dev-alimentada-com-assets(" + id + "):" + idAssets);

                    } else {
                        CSPLog.error(CSPKitCapp.class,
                                "assets-nao-encontrados:" + idAssets);
                    }
                }

            }
        }

        {//lista de contratantes customizada
            listaContratantesApp.setCanAllFolderAndContent(true);
            listaContratantesApp.setPath(CSPKitCapp.getPathDev(id) + "/" + id + "-lista-contratantes-app.json");
            listaContratantesApp.delete();
            listaContratantesApp.setContent(listaContratantesFull.getContent());
            updateConteudoCacheArquivo(id + "-lista-contratantes", listaContratantesApp.getContent());
        }

        {//lista de regiões/cidades customizada
            regiaoCidadesApp.setPath(CSPKitCapp.getPathDev(id) + "/" + id + "-regiao-cidades-app.json");
            regiaoCidadesApp.delete();
            regiaoCidadesApp.setContent(CSPArquivosAssinados.getInstance(getPathContratantes() + "/regiao-cidades.json").getContent());
            updateConteudoCacheArquivo(id + "-regiao-cidades", regiaoCidadesApp.getContent());
        }

        {//lista de hosts do capp do app
            listaHostsApp.setPath(CSPKitCapp.getPathDev(id) + "/" + id + "-lista-capp-hosts-app.json");

            JSONArray jay = new JSONArray();

            for (String host : hostsCapp) {
                jay.put(host);
            }

            if (versionApp.maiorOuIgualA(VersionsAppSupported.ad_2_3)&&jay.length() == 0) {
                jay.put("177.54.11.198");
            }

            listaHostsApp.setObject(new JSONObject().put("DEFAULT", jay));

            try {
                CSPInstrucoesSQLBase conn = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_APP);

                ResultSet rs = conn.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     CNPJ ");
                    sb.append("FROM ");
                    sb.append("    LOJAS ");
                });

                while (rs.next()) {
                    String cnpj = rs.getString("CNPJ");

                    if (isAppGarcomToRestaurante(id, cnpj, onNeedGetModoRestaurante)) {
                        listaHostsApp.setObject(new JSONObject() {
                            {
                                put("DEFAULT", new CSPArquivosJson(getPathContratante(cnpj) + "/lista-local-ips.json").getArray());
                            }
                        });

                        listaHostsApp.appendObject(cnpj, new CSPArquivosJson(getPathContratante(cnpj) + "/lista-local-ips.json").getArray());

                        break;
                    }
                }

                conn.close();
            } catch (Exception e) {
                CSPException.register(e);
            }
        }

        if (isNewDev || isForceUpdate) {
            /**
             * Em casos onde o app foi resetado vamos mandar pra ele o seu bonus
             * via arquivo mesmo. Dessa forma o app nao fica sem o bonus ate a
             * primeira notificaçao referente ao mesmo
             */
            String srcBonus = id + "-bonus-app";
            add.add(srcBonus);

            new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/" + srcBonus).setContent(onNeedGetCurrentValorBonusDev.run(id) + "");
        }

        /**
         * Caso seja um dispositivo livre ele irá excluir as informações dos
         * contratantes referentes a testes.
         */
        if (!isDevLab) {
            JSONArray d = listaContratantesApp.getArray();
            JSONArray old = d == null ? new JSONArray() : d;
            JSONArray novo = new JSONArray();

            for (int i = 0; i < old.length(); i++) {
                final String cnpj = old.getJSONObject(i).getString("ID");

                if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, false)) {
                    novo.put(old.get(i));
                }
            }

            listaContratantesApp.setArray(novo);
        }

        {
            /**
             * Aqui vamos tratar a ordem de exibição dos contratantes no APP
             * (ranking)
             */
            final CSPArquivosLocais ranking = new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/ranking.json");

            if (isNewDev) {
                ranking.delete();
            }

//            
            final HashMap<String, String> relationCpnjCidade = new HashMap<>();
            final HashMap<String, Integer> relationCidadePosition = new HashMap<>();
            final HashMap<String, Integer> relationCidadeQtdeRestaurantes = new HashMap<>();

            {
                /**
                 * Vamos alimentar os mapas para conseguirmos controlar a ordem
                 * das cidades
                 */
                for (Object t : regiaoCidadesApp.getArray()) {
                    JSONObject tt = (JSONObject) t;
                    final String idCity = CSPUtilidadesLangJson.getFromJson(tt, "ID", "?");
                    relationCidadePosition.put(idCity, 0);
                    int count = 0;

                    for (Object ttt : CSPUtilidadesLangJson.getFromJson(tt, "CONTRATANTES", new JSONArray())) {
                        relationCpnjCidade.put(ttt.toString(), idCity);
                        count++;
                    }
                    relationCidadeQtdeRestaurantes.put(idCity, count);
                }
            }

            final JSONArray current = listaContratantesApp.getArray();
            final JSONArray newContrs = new JSONArray();
            if (ranking.exists() && ranking.isFile()) {
                final JSONArray listRanking = CSPUtilidadesLangJson.getArray(ranking.getContent());
                for (int r = 0; r < listRanking.length(); r++) {

                    final JSONObject rank = listRanking.getJSONObject(r);

                    for (int c = 0; c < current.length(); c++) {

                        final JSONObject contr = current.getJSONObject(c);
                        final String idContr = contr.getString("ID");
                        if (idContr.equals(rank.getString("ID"))) {

                            if (CSPUtilidadesLangJson.getFromJson(
                                    rank,
                                    "IS_FULL",
                                    false
                            )) {
                                contr.put("IMAGEM", contr.getString("IMAGEM_FULL"));
                                contr.put("DESTAQUE", "s");
                            } else {
                                contr.put("DESTAQUE", "n");
                            }

                            newContrs.put(contr);
                            current.remove(c);

                            final String idCidade = relationCpnjCidade.get(idContr);
                            relationCidadePosition.put(
                                    idCidade,
                                    relationCidadePosition.get(idCidade) + c + 1
                            );

                            break;
                        }
                    }

                }
            }

            //O que sobrou
            for (int c = 0; c < current.length(); c++) {

                final JSONObject contr = current.getJSONObject(c);
                newContrs.put(contr);

            }

            if (newContrs.length() > 0) {

                {
                    if (relationCpnjCidade.size() > 0) {

                        /**
                         * Todos os restaurantes que pertencem a uma cidade com
                         * menos de 4 restaurantes devem ser exibidos em modo
                         * linha exclusiva
                         */
                        for (int c = 0; c < newContrs.length(); c++) {

                            final JSONObject contr = newContrs.getJSONObject(c);

                            if (relationCpnjCidade.containsKey(
                                    contr.getString("ID")
                            )
                                    && relationCpnjCidade.get(
                                            contr.getString("ID")
                                    ) != null
                                    && relationCidadeQtdeRestaurantes.get(
                                            relationCpnjCidade.get(
                                                    contr.getString("ID")
                                            )
                                    ) < 4) {
                                contr.put("IMAGEM", contr.getString("IMAGEM_FULL"));
                                contr.put("DESTAQUE", "s");
                            }

                        }
                    }

                }

                listaContratantesApp.setArray(newContrs);
            }

            if (relationCpnjCidade.size() > 0) {

                JSONArray newCidades = regiaoCidadesApp.getArray();

                for (Object t : newCidades) {
                    final JSONObject contr = ((JSONObject) t);
                    contr.put("TEMP", relationCidadePosition.get(contr.getString("ID")));
                }

                newCidades = CSPUtilidadesLangJson.sortJsonArray(newCidades, "TEMP", true, false);

                for (Object t : newCidades) {
                    ((JSONObject) t).remove("TEMP");
                }

                regiaoCidadesApp.setArray(newCidades);

            } else {

                regiaoCidadesApp.delete();
            }

//            }
        }

        {

            /**
             * Configurações finais de acesso e ranking
             */
            JSONArray current = listaContratantesApp.getArray();

            JSONArray currentNew = new JSONArray();

            final ArrayList<JSONObject> sendedToFinal = new ArrayList<>();

            if (versionApp.maiorOuIgualA(VersionsAppSupported.ad_1_7_1) && current.toString().contains("IS_ACESSO_RESTRITO")) {
                current = CSPUtilidadesLangJson.sortJsonArray(current, "IS_ACESSO_RESTRITO", false, true);
            }

            for (int c = 0; c < current.length(); c++) {

                final String cnpj = current.getJSONObject(c).getString("ID");

                final ModoRestaurante modo = (ModoRestaurante) onNeedGetModoRestaurante.run(cnpj);

                if (versionApp.maiorOuIgualA(VersionsAppSupported.ad_1_7_1, VersionsAppSupported.io_1_7_6)) {

                    if (modo.equals(ModoRestaurante.RESTRITO_AUTORIZACAO)
                            || modo.equals(ModoRestaurante.GARCOM)
                            || modo.equals(ModoRestaurante.GARCOM_TESTES)) {

                        if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, true)) {

                            current.getJSONObject(c).put("DESTAQUE", "s");

                            current.getJSONObject(c).put("IMAGEM", current.getJSONObject(c).getString("IMAGEM_FULL"));

                            if (modo.equals(ModoRestaurante.GARCOM)
                                    || modo.equals(ModoRestaurante.GARCOM_TESTES)) {
                                current.getJSONObject(c).put("IS_MODO_GARCOM", isAppGarcomToRestaurante(id, cnpj, onNeedGetModoRestaurante) ? "s" : "n");
                            } else {
                                current.getJSONObject(c).put("IS_MODO_VENDEDOR_EXTERNO", isAppVendedorExternoToRestaurante(id, cnpj, onNeedGetModoRestaurante) ? "s" : "n");

                            }
                            current.getJSONObject(c).remove("IS_ACESSO_RESTRITO");
//                            currentNew.put(current.getJSONObject(c));
                        } else {

                            sendedToFinal.add(current.getJSONObject(c));

                            continue;
                        }
                    }
                } else if (id.startsWith("io_")) {

                    /**
                     * Bugfix para o iOS
                     */
                    current.getJSONObject(c).put("DESTAQUE", "s");

                    current.getJSONObject(c).put("IMAGEM", current.getJSONObject(c).getString("IMAGEM_FULL"));

                }

                if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, true)) {

                    currentNew.put(current.getJSONObject(c));

                }

            }

            sendedToFinal.stream().forEach((s) -> {
                currentNew.put(s);
            });

            listaContratantesApp.setArray(currentNew);

        }

        {
            try {
                /**
                 * Comparamos o conteúdo disponível com o que existe no app para
                 * gerarmos uma atualização
                 */
                listaAtualApp.rebuildContent();
            } catch (Exception e) {
                CSPException.register(e);
            }

            final JSONObject current = listaAtualApp.getObject();
            final ArrayList<CSPArquivos> listTmp = new ArrayList<>(Arrays.asList(arquivosParaDisponibilizar.getFiles()));

            listTmp.add(listaContratantesApp);
            listTmp.add(regiaoCidadesApp);
            listTmp.add(confsLocaisApp);
            listTmp.add(listaHostsApp);

            if (isNewDev) {
                add.add(listaContratantesApp.getName());
                add.add(regiaoCidadesApp.getName());
                add.add(listaHostsApp.getName());
            }

            for (CSPArquivos file : listTmp) {

                if (!file.isFile()) {

                    continue;
                }

                final String lastMd5 = CSPUtilidadesLangJson.getFromJson(current, file.getAbsolutePath(), (String) null);

                if (lastMd5 == null || !lastMd5.equals(arquivosParaDisponibilizar.getFileLastMd5(file))) {

                    String name = trataNomeFileServerToApp(file);

                    if (arquivosParaDisponibilizar.getFileLastMd5(file) == null) {

                        rm.add(name);

                    } else {

                        if (isFileLiberadoToApp(id, versionApp, name, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio)) {

                            add.add(name);

                            if (lastMd5 != null) {
                                rm.add(name);
                            }

                        } else if (lastMd5 != null) {

                            rm.add(name);

                        }

                    }

                }

            }

        }

        /**
         * Por uma questão de usabilidade ordenamos quais serão os primeiros
         * arquivos a serem baixados e depois de qual o app já pode liberar o
         * usuário
         */
        String liberaEm = "?";
        {
            final String contratantesContent = listaContratantesApp.getContent();
            final String cidadesContent = regiaoCidadesApp.getContent() + "";
            final LinkedHashSet<String> presentesNoLista = new LinkedHashSet<>();
            final List<String> addAux = new ArrayList<>(add);
            Collections.sort(addAux);

            add.clear();
            add.addAll(addAux);

            for (String aa : add) {
                if (aa.endsWith(".json") || aa.contains("-app") || aa.startsWith("ic_")) {
                    presentesNoLista.add(aa);
                    liberaEm = aa;
                }
            }
//            System.out.println(liberaEm);
            for (String aa : add) {
                if (contratantesContent.contains(aa) || cidadesContent.contains(aa)) {
                    if (!presentesNoLista.contains(aa)) {
                        presentesNoLista.add(aa);

                        liberaEm = aa;
                    }
                }
            }
//            System.out.println(liberaEm);

            presentesNoLista.addAll(add);
            add.clear();
            add.addAll(presentesNoLista);
        }

        {//Bugfix

            {//bonus
                String srcBonus = id + "-bonus-app";
                if (new CSPArquivos(CSPKitCapp.getPathDev(id) + "/" + srcBonus).isFile()) {
                    rm.add(srcBonus);
                    add.add(srcBonus);
                }
            }

            {//informações sobre login
                final CSPArquivos logado = new CSPArquivos();

                for (String a : new String[]{"dados", "bonus", "id"}) {
                    logado.setPath(CSPKitCapp.getPathDev(id) + "/" + id + "-" + a + "-logado");
                    if (logado.isFile()) {
                        rm.add(logado.getName());
                        add.add(logado.getName());
                    }
                }

            }
        }

        //Para alguns arquivos no app é necessário um nome padrão
        for (String a : add) {
            if (a.startsWith("lista-contratantes") || a.equals(listaContratantesApp.getName())) {
                //Lista de todos os restaurantes
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "contratantes");
                    }
                });
            } else if (a.contains("confs-locais-app")) {
                //Arquivo de configurações locais do app
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "confs-locais");
                    }
                });
            } else if (a.contains("regiao-cidades")) {
                //Configurações de cidades no dispositivo
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "regiao-cidades");
                    }
                });
            } else if (a.contains("lista-capp-hosts-app")) {

                //Configurações de hosts para o dispositivo
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "lista-capp-hosts");
                    }
                });
            } else if (a.startsWith("notificacoes-egula")) {
                //Notificações do próprio APP
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "notificacoes-egula");
                    }
                });
            } else if (a.startsWith("ic_")) {
                //Icones

                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", a.split("-")[0] + "." + FilenameUtils.getExtension(a));
                    }
                });
            } else if (a.contains("_e_notificacoes-")) {
                //Notificações dos restaurantes
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "notificacoes-" + a.split("_e_")[0]);
                    }
                });
            } else if (a.contains("_e_categorias-")) {
                //Conteúdo de cada restaurante
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "categorias-" + a.split("_e_")[0]);
                    }
                });
            } else if (a.contains("-bonus-app")) {
                //Bonus do app, quando existir
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "bonus");
                    }
                });
            } else if (a.contains("dados-logado")) {
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "dados-logado");
                    }
                });
            } else if (a.contains("bonus-logado")) {
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "bonus-logado");
                    }
                });
            } else if (a.contains("id-logado")) {
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "id-logado");
                    }
                });
            }
        }
//        /**
//         * Por experiencias passadas, precisamos forçar o app a deletar os
//         * conteúdo que serão baixados novamente
//         */
//        for (String a : add) {
//            rm.add(a);
//        }

        if (isForceUpdate) {
            for (JSONObject a : rn) {
                rm.add(a.getString("D"));
                rm.add(a.getString("P"));

            }
            rm.add("logs");
        }

        JSONObject t = new JSONObject();
        if (!isNewDev || isForceUpdate) {
            t.put("RM", rm);
        }
        t.put("ADD", add);
        t.put("RN", rn);
        if (add.size() > 0) {
            t.put("LIBERAR_EM", liberaEm);
        }

        if (add.size() == 0 && rm.size() == 0 && rn.size() == 0) {
            t = new JSONObject();
        }

        String s = t.toString();

        if (!isDevLab) {
            s = s.replace(listaContratantesApp.getName(), listaContratantesApp.getName().toLowerCase());
        }
        /**
         * Arquivo que vai conter o que o app precisa baixar/atualizar/remover
         */
        new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/para-atualizar.json").setContent(s);
        registraPreparoArquivosDownloadDev(id, isNewDev, add, liberaEm);
        new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/in-update.txt").delete();

    }

    /**
     * Prepara toda atualização de conteúdo para o ID.
     *
     * Esse novo preparador se baseia somente por MD5, não mais por nomes.
     *
     * @param id String - Id do celular
     * @param isNewDev boolean - Se é um dispositivo novo
     * @param isForceUpdate boolean - Se é pra forçar a atualização
     * @param onNeedGetModoRestaurante
     * @param onNeedIsDispositivoLaboratorio
     * @param onNeedGetCurrentValorBonusDev
     * @param hostsCapp
     * @throws java.lang.Exception
     */
    @Deprecated
    public static void preparaAtualizacaoDev(
            String id,
            boolean isNewDev,
            boolean isForceUpdate,
            AuxAll onNeedGetModoRestaurante,
            AuxAll onNeedIsDispositivoLaboratorio,
            AuxAll onNeedGetCurrentValorBonusDev,
            String... hostsCapp
    ) throws Exception {
        if (getVersionApp(id).maiorOuIgualA(ad_1_8_3, io_1_6_9)) {
//        if (id.equals("ad_lgd85534392548")) {
            preparaAtualizacaoDevNew(id, isNewDev, isForceUpdate, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, onNeedGetCurrentValorBonusDev, hostsCapp);

            return;
        }

        final HashMap<String, String> relacaoLastMd5XArquivo = buildListRelationLastMd5(id);

        if (isNewDev) {
            final CSPArquivos tmp = new CSPArquivos();

            for (Map.Entry<String, String> e : relacaoLastMd5XArquivo.entrySet()) {

                tmp.setPath(e.getKey());
                tmp.delete();

                tmp.setPath(e.getValue());
                tmp.delete();

            }
        }

        final CSPArquivosJson listaContratantesApp = new CSPArquivosJson(CSPKitCapp.getPathDev(id));
        {

            listaContratantesApp.setCanAllFolderAndContent(true);
            listaContratantesApp.setPath(CSPKitCapp.getPathDev(id) + "/" + id + "-lista-contratantes-app.json");
            listaContratantesApp.delete();
            listaContratantesApp.setContent(listaContratantesFull.getContent());

        }

        final CSPArquivosJson regiaoCidadesApp = new CSPArquivosJson();
        {

            regiaoCidadesApp.setPath(CSPKitCapp.getPathDev(id) + "/" + id + "-regiao-cidades-app.json");
            regiaoCidadesApp.delete();
            regiaoCidadesApp.setContent(CSPArquivosAssinados.getInstance(getPathContratantes() + "/regiao-cidades.json").getContent());

        }

        String listaAtualContent = "";
        final boolean isDevLab = (boolean) onNeedIsDispositivoLaboratorio.run(id);
        final VersionsAppSupported versionApp = getVersionApp(id);
        final CSPArquivosLocais listaAtual = new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/lista-atual.txt");

        if (isForceUpdate) {
            listaAtual.setContent("");
        }

        if (listaAtual.exists()) {
            listaAtualContent = listaAtual.getContent() + "";
        }

        final LinkedHashSet<String> addIdeal = new LinkedHashSet<>();//Adicionar
        final LinkedHashSet<String> add = new LinkedHashSet<>();//Adicionar
        final LinkedHashSet<String> rm = new LinkedHashSet<>();//Remover
        final LinkedHashSet<JSONObject> rn = new LinkedHashSet<>();//Renomear
        final LinkedHashSet<String> oldNames = new LinkedHashSet<>(Arrays.asList(listaAtualContent.split(LINE_SEPARATOR)));
        final LinkedHashSet<String> newNames = new LinkedHashSet<>();
        final String[] atuais = ("" + arquivoInfosTotalLista.getContent()).trim().split(LINE_SEPARATOR);
        final CSPArquivosLocais contra = new CSPArquivosLocais(CSPKitCapp.getPathContratantes());

        for (String atual : atuais) {
            if (atual.startsWith("lista-contratante")
                    || atual.startsWith("notificacoes-egula")
                    || atual.startsWith("ic_")
                    || atual.startsWith("central-imagens_")) {
//            if (atual.startsWith("notificacoes-egula") || atual.startsWith("ic_")) {
                if (atual.startsWith("central-imagens_municipios")) {
//                    if (getVersionApp(id).maiorOuIgualA(VersionsAppSupported.ad_1_8_3)) {
//                        newNames.add(atual);
//                    }
                } else {
                    newNames.add(atual);
                }

            } else if (!atual.startsWith("regiao-cidades")) {
                contra.setName(atual.replace("_e_", "/enviar-app/"));
                if (contra.isFile()) {
                    newNames.addAll(new ArrayList<>(Arrays.asList((contra.getContent() + "").trim().split(LINE_SEPARATOR))));
                }
            }
        }

//        if (getVersionApp(id).maiorOuIgualA(VersionsAppSupported.ad_1_8_3)) {
//            newNames.add(regiaoCidadesApp.getName());
//        }
        //Arquivos a serem removidos
        oldNames.stream().filter((n) -> (n != null && !n.trim().isEmpty() && !newNames.contains(n.trim()) && isFileLiberadoToApp(id, versionApp, n, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio))).forEach((n) -> {
            //Um arquivo para ser removido não pode estar na nova lista 
            rm.add(n.replace("/", "__"));
        });

        //Arquivos a serem adicionados
        newNames.stream().filter((n) -> (n != null && !n.trim().isEmpty() && isFileLiberadoToApp(id, versionApp, n, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio))).forEach((n) -> {
            //Um arquivo para ser adiconado não pode estar esta na lista antiga
            if (!oldNames.contains(n.trim())) {
                add.add(n.replace("/", "__"));
            }
            addIdeal.add(n.replace("/", "__"));
        });

        if (isNewDev || isForceUpdate) {
            /**
             * Em casos onde o app foi resetado vamos mandar pra ele o seu bonus
             * via arquivo mesmo. Dessa forma o app nao fica sem o bonus ate a
             * primeira notificaçao referente ao mesmo
             */
            String srcBonus = id + "-bonus-app";
            add.add(srcBonus);
            new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/" + srcBonus).setContent(onNeedGetCurrentValorBonusDev.run(id) + "");
        }

        /**
         * Caso seja um dispositivo livre ele irá excluir as informações dos
         * contratantes referentes a testes.
         */
        if (!isDevLab) {
            JSONArray d = listaContratantesApp.getArray();
            JSONArray old = d == null ? new JSONArray() : d;
            JSONArray novo = new JSONArray();

            for (int i = 0; i < old.length(); i++) {
                final String cnpj = old.getJSONObject(i).getString("ID");

                if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, false)) {
                    novo.put(old.get(i));
                }
            }
            listaContratantesApp.setArray(novo);
        }

        {
            /**
             * Aqui vamos tratar a ordem de exibição dos contratantes no APP
             * (ranking)
             */
            final CSPArquivosLocais ranking = new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/ranking.json");

            if (isNewDev) {
                ranking.delete();
            }

            if (ranking.exists() && ranking.isFile()) {

                final JSONArray current = listaContratantesApp.getArray();
                final JSONArray newContrs = new JSONArray();
                final JSONArray listRanking = CSPUtilidadesLangJson.getArray(ranking.getContent());

                for (int r = 0; r < listRanking.length(); r++) {

                    final JSONObject rank = listRanking.getJSONObject(r);

                    for (int c = 0; c < current.length(); c++) {

                        final JSONObject contr = current.getJSONObject(c);

                        if (contr.getString("ID").equals(rank.getString("ID"))) {

                            if (CSPUtilidadesLangJson.getFromJson(
                                    rank,
                                    "IS_FULL",
                                    false
                            )) {
                                contr.put("IMAGEM", contr.getString("IMAGEM_FULL"));
                                contr.put("DESTAQUE", "s");
                            } else {
                                contr.put("DESTAQUE", "n");
                            }

                            newContrs.put(contr);
                            current.remove(c);
                            break;
                        }
                    }

                }

                //O que sobrou
                for (int c = 0; c < current.length(); c++) {
                    newContrs.put(current.getJSONObject(c));
                }

                if (newContrs.length() > 0) {
                    listaContratantesApp.setArray(newContrs);
                    add.add(listaContratantesApp.getName());
                }

            }
        }

        {

            JSONArray current = listaContratantesApp.getArray();
            JSONArray currentNew = new JSONArray();
            final ArrayList<JSONObject> sendedToFinal = new ArrayList<>();

            if (versionApp.maiorOuIgualA(VersionsAppSupported.ad_1_7_1) && current.toString().contains("IS_ACESSO_RESTRITO")) {
                current = CSPUtilidadesLangJson.sortJsonArray(current, "IS_ACESSO_RESTRITO", false, true);
            }

            for (int c = 0; c < current.length(); c++) {

                final String cnpj = current.getJSONObject(c).getString("ID");
                final ModoRestaurante modo = (ModoRestaurante) onNeedGetModoRestaurante.run(cnpj);

                if (versionApp.maiorOuIgualA(VersionsAppSupported.ad_1_7_1)) {

                    if (modo.equals(ModoRestaurante.RESTRITO_AUTORIZACAO)
                            || modo.equals(ModoRestaurante.GARCOM)
                            || modo.equals(ModoRestaurante.GARCOM_TESTES)) {
                        if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, true)) {

                            current.getJSONObject(c).put("DESTAQUE", "s");
                            current.getJSONObject(c).put("IMAGEM", current.getJSONObject(c).getString("IMAGEM_FULL"));
                            if (modo.equals(ModoRestaurante.GARCOM)
                                    || modo.equals(ModoRestaurante.GARCOM_TESTES)) {
                                current.getJSONObject(c).put("IS_MODO_GARCOM", isAppGarcomToRestaurante(id, cnpj, onNeedGetModoRestaurante) ? "s" : "n");
                            } else {
                                current.getJSONObject(c).put("IS_MODO_VENDEDOR_EXTERNO", isAppVendedorExternoToRestaurante(id, cnpj, onNeedGetModoRestaurante) ? "s" : "n");
                            }
                            current.getJSONObject(c).remove("IS_ACESSO_RESTRITO");
//                            currentNew.put(current.getJSONObject(c));
                        } else {
                            sendedToFinal.add(current.getJSONObject(c));

                            continue;
                        }
                    }
                } else if (id.startsWith("io_")) {
                    /**
                     * Bugfix para o iOS
                     */
                    current.getJSONObject(c).put("DESTAQUE", "s");
                    current.getJSONObject(c).put("IMAGEM", current.getJSONObject(c).getString("IMAGEM_FULL"));
                }

                if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, true)) {
                    currentNew.put(current.getJSONObject(c));
                }

            }

            sendedToFinal.stream().forEach((s) -> {
                currentNew.put(s);
            });

            listaContratantesApp.setArray(currentNew);
            add.add(listaContratantesApp.getName());

        }
//        System.out.println(add);
//        System.out.println("++++++++++++++++++");
        /**
         * Por uma questão de usabilidade ordenamos quais serão os primeiros
         * arquivos a serem baixados e depois de qual o app já pode liberar o
         * usuário
         */
        String liberaEm = "?";
        {
            final String contratantesContent = listaContratantesApp.getContent();
            final String cidadesContent = regiaoCidadesApp.getContent();
            final LinkedHashSet<String> presentesNoLista = new LinkedHashSet<>();
            final List<String> addAux = new ArrayList<>(add);
            Collections.sort(addAux);
            add.clear();
            add.addAll(addAux);
            for (String aa : add) {
                if (aa.endsWith(".json") || aa.contains("-app") || aa.startsWith("ic_")) {
                    presentesNoLista.add(aa);
                    liberaEm = aa;
                }
            }
            for (String aa : add) {
                if (contratantesContent.contains(aa)) {
                    presentesNoLista.add(aa);
                    liberaEm = aa;
                }
            }

            presentesNoLista.addAll(add);
            add.clear();
            add.addAll(presentesNoLista);
        }
//        System.out.println(add);

        {//Bugfix

            {//last-md5-..
                final CSPArquivos lastMd5 = new CSPArquivos();
                final CSPArquivos tmp = new CSPArquivos();

                for (Map.Entry<String, String> e : relacaoLastMd5XArquivo.entrySet()) {
                    lastMd5.setPath(e.getKey());
                    tmp.setPath(e.getValue());
//                    System.out.println(tmp.getAbsolutePath());
//                    System.out.println(tmp.isFile());
                    if (lastMd5.isFile() && tmp.isFile()) {
                        if (!tmp.getMd5().equals(lastMd5.getContent())) {
                            add.add(tmp.getName());
                        } else {
                            add.remove(tmp.getName());
                            rm.remove(tmp.getName());
                        }
                    } else if (tmp.isFile() && !add.contains(tmp.getName())) {
                        add.add(tmp.getName());
                    }
                }
            }

            {//bonus
                String srcBonus = id + "-bonus-app";
                if (new CSPArquivos(CSPKitCapp.getPathDev(id) + "/" + srcBonus).isFile()) {
                    rm.add(srcBonus);
                    add.add(srcBonus);
                }
            }

            {//informações sobre login
                final CSPArquivos logado = new CSPArquivos();

                for (String a : new String[]{"dados", "bonus", "id"}) {
                    logado.setPath(CSPKitCapp.getPathDev(id) + "/" + id + "-" + a + "-logado");
                    if (logado.isFile()) {
                        rm.add(logado.getName());
                        add.add(logado.getName());
                    }
                }

            }
        }

        {//bugfix
            final CSPArquivos file = new CSPArquivos();
            final LinkedHashSet<String> tmp = new LinkedHashSet<>();

            for (String a : add) {
                file.setPath(getPathContratantes() + "/" + a);
                trataNomeFileAppToServer(file);
                if (file.isFile()) {
                    tmp.add(a);

                } else {
                    CSPLog.error(CSPKitCapp.class,
                            a + " removido da lista 'add' no preparo por não existir");
                }
            }
            add.clear();
            add.addAll(tmp);
        }

        {//bugfix

            for (String a : add) {
                if (a.startsWith("lista-contratantes") && !a.equals(listaContratantesApp.getName())) {
                    add.remove(a);
                    if (liberaEm.equals(a)) {
                        liberaEm = listaContratantesApp.getName();
                    }
                    break;
                }
            }

        }

        //Para alguns arquivos no app é necessário um nome padrão
        for (String a : add) {
            if (a.startsWith("lista-contratantes") || a.equals(listaContratantesApp.getName())) {
                //Lista de todos os restaurantes
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "contratantes");
                    }
                });
            } else if (a.contains("confs-locais-app")) {
                //Arquivo de configurações locais do app
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "confs-locais");
                    }
                });
            } else if (a.contains("regiao-cidades")) {
                //Configurações de cidades no dispositivo
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "regiao-cidades");
                    }
                });
            } else if (a.startsWith("notificacoes-egula")) {
                //Notificações do próprio APP
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "notificacoes-egula");
                    }
                });
            } else if (a.startsWith("ic_")) {
                //Icones

                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", a.split("-")[0] + "." + FilenameUtils.getExtension(a));
                    }
                });
            } else if (a.contains("_e_notificacoes-")) {
                //Notificações dos restaurantes
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "notificacoes-" + a.split("_e_")[0]);
                    }
                });
            } else if (a.contains("_e_categorias-")) {
                //Conteúdo de cada restaurante
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "categorias-" + a.split("_e_")[0]);
                    }
                });
            } else if (a.contains("-bonus-app")) {
                //Bonus do app, quando existir
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "bonus");
                    }
                });
            } else if (a.contains("dados-logado")) {
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "dados-logado");
                    }
                });
            } else if (a.contains("bonus-logado")) {
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "bonus-logado");
                    }
                });
            } else if (a.contains("id-logado")) {
                rn.add(new JSONObject() {
                    {
                        put("D", a);
                        put("P", "id-logado");
                    }
                });
            }
        }
        /**
         * Por experiencias passadas, precisamos forçar o app a deletar os
         * conteúdo que serão baixados novamente
         */
        for (String a : add) {
            rm.add(a);
        }

        if (isForceUpdate) {
            for (JSONObject a : rn) {
                rm.add(a.getString("D"));
                rm.add(a.getString("P"));

            }
            rm.add("logs");
        }

        JSONObject t = new JSONObject();
        if (!isNewDev || isForceUpdate) {
            t.put("RM", rm);
        }
        t.put("ADD", add);
        t.put("RN", rn);
        if (add.size() > 0) {
            t.put("LIBERAR_EM", liberaEm);
        }

        if (add.size() == 0 && rm.size() == 0 && rn.size() == 0) {
            t = new JSONObject();
        }

        String s = t.toString();

        if (!isDevLab) {
            s = s.replace(listaContratantesApp.getName(), listaContratantesApp.getName().toLowerCase());
        }
        /**
         * Arquivo que vai conter o que o app precisa baixar/atualizar/remover
         */
        new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/para-atualizar.json").setContent(s);
        new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/lista-ideal.txt").setContent(String.join(CSPUtilidadesSO.LINE_SEPARATOR, addIdeal));
        registraPreparoArquivosDownloadDev(id, isNewDev, add, liberaEm);
        new CSPArquivosLocais(CSPKitCapp.getPathDev(id) + "/in-update.txt").delete();

    }

    /**
     * Retorna se o arquivo está liberado para o app
     *
     * @param id String - ID dev
     * @return
     * @throws SQLException
     */
    private static synchronized boolean isFileLiberadoToApp(
            String id,
            VersionsAppSupported version,
            String arquivo,
            AuxAll onNeedGetModoRestaurante,
            AuxAll onNeedIsDispositivoLaboratorio
    ) {

        try {
            if (getVersionApp(id).maiorOuIgualA(ad_1_8_3, io_1_6_9)) {

                arquivo = arquivo.trim();

                final String cnpj = extractCnpjFromStringFile(arquivo);

                if (cnpj == null) {
                    {
                        /**
                         * Só vai para o app o conteúdo restrito a ele
                         */
                        final String name = FilenameUtils.getName(arquivo);

                        if (name.startsWith("ad_") || name.startsWith("io_")) {

                            if (!name.startsWith(id)) {
                                return false;
                            }
                        }

                    }
                    return true;
                }

                final ModoRestaurante modo = (ModoRestaurante) onNeedGetModoRestaurante.run(cnpj);

                if ((boolean) onNeedIsDispositivoLaboratorio.run(id) && (modo.equals(ModoRestaurante.TESTES_LABORATORIO)
                        || modo.equals(ModoRestaurante.GARCOM_TESTES))) {
                    return isFileLiberadoToDevAux(arquivo, cnpj, id);
                }

                if (modo.equals(ModoRestaurante.RESTRITO_AUTORIZACAO)) {
                    if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, true)) {
                        return isFileLiberadoToDevAux(arquivo, cnpj, id);
                    } else {

                        if (arquivo.contains("notificacoes")) {
                            return true;
                        }

                        if (listaContratantesFull.getContent().contains(arquivo)) {
                            return true;
                        }

                    }

                }

                if (modo.equals(ModoRestaurante.HABILITADO)
                        || modo.equals(ModoRestaurante.GARCOM)) {
                    return isFileLiberadoToDevAux(arquivo, cnpj, id);
                }

                return false;

            }
        } catch (Exception e) {
            CSPException.register(e);
        }

        try {

            arquivo = arquivo.trim();

            final String cnpj = extractCnpjFromStringFile(arquivo);

            if (cnpj == null) {
                return true;
            }

            final ModoRestaurante modo = (ModoRestaurante) onNeedGetModoRestaurante.run(cnpj);

            if ((boolean) onNeedIsDispositivoLaboratorio.run(id) && modo.equals(ModoRestaurante.TESTES_LABORATORIO)) {
                return true;
            }

            if (modo.equals(ModoRestaurante.RESTRITO_AUTORIZACAO) && version.maiorOuIgualA(VersionsAppSupported.ad_1_7_1)) {
                if (isAppLiberadoToRestaurante(id, cnpj, onNeedGetModoRestaurante, onNeedIsDispositivoLaboratorio, true)) {
                    return true;
                } else {
                    if (arquivo.contains("notificacoes")) {
                        return true;
                    }

                    if (listaContratantesFull.getContent().contains(arquivo)) {
                        return true;
                    }

                }

            }

            return modo.equals(ModoRestaurante.HABILITADO);

        } catch (Exception e) {
            CSPException.register(e);
        }

        return false;
    }

    private static synchronized boolean isFileLiberadoToDevAux(String arquivo, String cnpj, String id) throws Exception {

        if (arquivo.contains("_e_")) {
            arquivo = arquivo.split("_e_")[1];
        }

//        System.out.print(arquivo);
        if (arquivo.endsWith(".json")) {
//            System.out.println("=>OK");
            return true;
        }

        final String arquivoId = arquivo + "-" + id + "-" + cnpj;

        if (fileLiberadoToDevCache.containsKey(arquivoId)) {
            return fileLiberadoToDevCache.get(arquivoId);
        }

        for (String f : new String[]{
            cnpj + "-categorias",
            id + "-lista-contratantes",
            id + "-regiao-cidades"
        }) {
            final String ff = getConteudoCacheArquivo(f);
            if (ff != null) {
                if (ff.contains(arquivo + "\"")) {
                    fileLiberadoToDevCache.put(arquivoId, true);
                    return true;
                }
            }
        }

        fileLiberadoToDevCache.put(arquivoId, false);
        return false;
    }

    /**
     * Retorna se o app está liberado para acessar o conteúdo do contratante
     *
     * @param id Sting - ID do dispositivo
     * @param cnpj String - CNPJ do restaurante
     * @param onNeedGetModoRestaurante
     * @param onNeedIsDispositivoLaboratorio
     * @param onlyWithLiberation boolean - Determina se a validação deve
     * considerar se o dispositivo está liberado para para o restaurante, caso
     * contrário apenas validará se o dispositivo tem capacidade de ser liberado
     * @return
     * @throws java.lang.Exception
     */
    public static boolean isAppLiberadoToRestaurante(
            String id,
            String cnpj,
            AuxAll onNeedGetModoRestaurante,
            AuxAll onNeedIsDispositivoLaboratorio,
            boolean onlyWithLiberation
    ) throws Exception {

        ModoRestaurante modo = (ModoRestaurante) onNeedGetModoRestaurante.run(cnpj);

        if (modo.equals(ModoRestaurante.NAO_HABILITADO)) {
            return false;
        }

        if (modo.equals(ModoRestaurante.HABILITADO) || modo.equals(ModoRestaurante.GARCOM)) {
            return true;
        }

        if ((boolean) onNeedIsDispositivoLaboratorio.run(id) && modo.equals(ModoRestaurante.TESTES_LABORATORIO)) {
            return true;
        }

        if (modo.equals(ModoRestaurante.RESTRITO_AUTORIZACAO)
                || (modo.equals(ModoRestaurante.GARCOM_TESTES) && (boolean) onNeedIsDispositivoLaboratorio.run(id))) {

            if (getVersionApp(id).maiorOuIgualA(VersionsAppSupported.ad_1_7_1, VersionsAppSupported.io_1_7_6)) {

                if (!onlyWithLiberation) {
                    return true;
                }

                final CSPArquivosJson liberados = new CSPArquivosJson(CSPKitCapp.getPathContratante(cnpj) + "/dispositivos-liberados.json");

                if (liberados.exists() && liberados.isFile()) {

                    final JSONObject o = CSPUtilidadesLangJson.getFromJson(liberados.getObject(), id, (JSONObject) null);

                    if (o != null) {
                        if (modo.equals(ModoRestaurante.RESTRITO_AUTORIZACAO)) {
                            return true;
                        } else {
                            if (getVersionApp(id).maiorOuIgualA(VersionsAppSupported.ad_1_7_6, VersionsAppSupported.io_1_7_6)) {
                                return CSPUtilidadesLangJson.getFromJson(o, "IS_GARCOM", "n").equals("s");
                            }
                        }
                    }

                }
            }
        }

        return false;
    }

    /**
     * Retorna se é um ID novo, e já cria a pasta para o mesmo
     *
     * @param id String - Id do celular
     * @return
     * @throws java.lang.Exception
     */
    public static boolean isNewDev(String id) throws Exception {
        CSPArquivos user = new CSPArquivos(CSPKitCapp.getPathDev(id));
        user.mkdirs();
        user.setPath(getPathDev(id) + "/lista-atual.txt");
        if (getVersionApp(id).maiorOuIgualA(ad_1_8_3, io_1_6_9)) {
//            if (id.equals("ad_lgd85534392548")) {
            user.delete();
            user.setPath(getPathDev(id) + "/lista-atual.fjson");
        }

        return !user.exists();
    }

    /**
     * Método responsável por centralizar o atendimento de pedidos
     *
     * @param info JSONObject
     * @param onNeedNewCodPedido
     * @param onNeedGetIdUserFromDispositivoLogadoId
     * @param onNeedRegistraStatusLoginUsuario
     * @param onNeedGetDadosUsuario
     * @param onNeedAddNewEnderecoUser
     * @param onNeedIsAppVendedorExternoToRestaurante
     * @param onNeedAppendCnpjRelacionadoFromUser
     * @param onNeedGetCurrentValorBonusUserLivre
     * @param onNeedAddNewBonusBloqueadoUser
     * @param onNeedAddNotificacaoUser
     * @param onNeedUpdateCodigoPedido
     * @param onNeedEndNewPedido
     * @return
     * @throws Exception
     */
    public static JSONObject atendePedidos(
            JSONObject info,
            AuxAll onNeedNewCodPedido,
            AuxAll onNeedGetIdUserFromDispositivoLogadoId,
            AuxAll onNeedRegistraStatusLoginUsuario,
            AuxAll onNeedGetDadosUsuario,
            AuxAll onNeedAddNewEnderecoUser,
            AuxAll onNeedIsAppVendedorExternoToRestaurante,
            AuxAll onNeedAppendCnpjRelacionadoFromUser,
            AuxAll onNeedGetCurrentValorBonusUserLivre,
            AuxAll onNeedAddNewBonusBloqueadoUser,
            AuxAll onNeedAddNotificacaoUser,
            AuxAll onNeedUpdateCodigoPedido,
            AuxAll onNeedEndNewPedido
    ) throws Exception {

        final String cnpjContratante = info.getString("ID_CONTRATANTE");//Vai chegar o cnpj aqui...
        /**
         * Cada dispositivo possui um prefixo:
         *
         * AD_ => Android
         *
         * IO_ => Ios (Apple)
         */
        final String idDispositivo = getIdDevInJson(info);
        final String idUsuario = CSPUtilidadesLangJson.getFromJson(info, "ID_USUARIO", "?");
        final HashMap<String, String> dadosI = new HashMap<>();
        final String idPedido = onNeedNewCodPedido.run((Object[]) null).toString();
        final String totalShowInDispositivo = info.getString("TOTAL_PEDIDO_EXIBIDO");//Total exibido no dispositivo

        ultimaMovimentacaoDev(idDispositivo);
        dadosI.put("NOME", info.getString("NOME").replace("\\(", "").replace("\\)", "").trim());
        {
            String fone = info.getString("TELEFONE").replaceAll("[^0-9]", "");
            //Bugfix: 047988717345 -> 47988717345
            if (fone.length() >= 12) {
                fone = CSPUtilidadesLang.substring(fone, 1, 12);
            }
            
            dadosI.put("FONE", fone);
        }
        dadosI.put("ENDERECO", CSPUtilidadesLangJson.getFromJson(info, "ENDERECO", (String) null));
        dadosI.put("NUMERO", CSPUtilidadesLangJson.getFromJson(info, "NUMERO", (String) null));
        dadosI.put("BAIRRO", CSPUtilidadesLangJson.getFromJson(info, "BAIRRO", (String) null));
        dadosI.put("CIDADE", CSPUtilidadesLangJson.getFromJson(info, "CIDADE", (String) null));
        dadosI.put("ENTREGA", CSPUtilidadesLangJson.getFromJson(info, "ENTREGA", (String) null));
        dadosI.put("MESA", CSPUtilidadesLangJson.getFromJson(info, "MESA", (String) null));
        /**
         * 'eb' -> 'e-GULA BÔNUS'
         */
        dadosI.put("PAGAMENTO", CSPUtilidadesLangJson.getFromJson(info, "PAGAMENTO", (String) null));
        dadosI.put("ACRESCIMO_TOTAL", CSPUtilidadesLangJson.getFromJson(info, "ACRESCIMOS", (String) null));
        dadosI.put("TROCO", CSPUtilidadesLangJson.getFromJson(info, "TROCO", 0.0) + "");
        dadosI.put("PEDIDO_NUMERO", idPedido);
        dadosI.put("DISPOSITIVO_ID", idDispositivo);
        dadosI.put("DATA_EMISSAO", CSPUtilidadesLangDateTime.getData());
        dadosI.put("HORA_EMISSAO", CSPUtilidadesLangDateTime.getHora());
        dadosI.put("STATUS", "0");//0 => Pendente
        dadosI.put("PEDIDO_TOTAL", totalShowInDispositivo);
        /**
         * Comum em liberações
         */
        dadosI.put("AGENTE_CNPJ", CSPUtilidadesLangJson.getFromJson(info, "AGENTE_CNPJ", (String) null));
        dadosI.put("SOLICITACAO_IDENTIFICACAO", CSPUtilidadesLangJson.getFromJson(info, "AGENTE_CNPJ", (String) null));

        if (!idUsuario.equals("?")) {
            dadosI.put("NOME", dadosI.get("NOME") + " (" + idUsuario + ")");
//            if (FrmModuloServerTratamentoUsuarios.getIdUserFromDispositivoLogadoId(idDispositivo) == null) {
            if (onNeedGetIdUserFromDispositivoLogadoId.run(idDispositivo) == null) {
                // #fix bug
//                FrmModuloServerTratamentoUsuarios.registraStatusLoginUsuario(idUsuario, idDispositivo, true);
                onNeedRegistraStatusLoginUsuario.run(idUsuario, idDispositivo, true);
            }
            /**
             * Caso o usuário estiver logado vamos veirificar se o mesmo não
             * usou outro endereço e caso sim vamos cadastrar
             */
//            JSONObject usr = FrmModuloServerTratamentoUsuarios.getDadosUsuario(idUsuario, false);
            JSONObject usr = (JSONObject) onNeedGetDadosUsuario.run(idUsuario, false);
            if (usr != null && dadosI.get("ENDERECO") != null) {
                JSONArray ends = usr.getJSONArray("ENDERECOS");
                boolean needNew = true;
                for (int i = 0; i < ends.length(); i++) {
                    JSONObject end = ends.getJSONObject(i);
                    if (end.getString("LOGRADOURO").trim().equalsIgnoreCase(dadosI.get("ENDERECO").trim())
                            && end.getString("NUMERO").trim().equalsIgnoreCase(dadosI.get("NUMERO").trim())
                            && end.getString("BAIRRO").trim().equalsIgnoreCase(dadosI.get("BAIRRO").trim())
                            && end.getString("CIDADE").trim().equalsIgnoreCase(dadosI.get("CIDADE").trim())) {
                        needNew = false;
                        break;
                    }
                }
                if (needNew) {
                    onNeedAddNewEnderecoUser.run(usr.getString("CPF"), dadosI.get("ENDERECO").trim(), dadosI.get("NUMERO").trim(), dadosI.get("BAIRRO").trim(), dadosI.get("CIDADE").trim());
//                    FrmModuloServerTratamentoUsuarios.addNewEnderecoUser(usr.getString("CPF"), dadosI.get("ENDERECO").trim(), dadosI.get("NUMERO").trim(), dadosI.get("BAIRRO").trim(), dadosI.get("CIDADE").trim());
                }
            }

//            if (!CSPKitCapp.isAppVendedorExternoToRestaurante(idDispositivo, cnpjContratante)) {
            if (!(boolean) onNeedIsAppVendedorExternoToRestaurante.run(idDispositivo, cnpjContratante)) {
                /**
                 * Usários que requerem NF para a empresa
                 */

//                appendCnpjRelacionadoFromUser(idUsuario, dadosI);
                onNeedAppendCnpjRelacionadoFromUser.run(idUsuario, dadosI);
            }

        } else {
//            FrmModuloServerTratamentoUsuarios.registraStatusLoginUsuario(null, idDispositivo, false);
            onNeedRegistraStatusLoginUsuario.run(null, idDispositivo, false);
        }

        CSPArquivosLocaisJson catFalhos = new CSPArquivosLocaisJson((getPathContratanteToReceive(cnpjContratante) + "/itens-problematicos.json").replace("enviar-contratante/enviar-contratante/", "enviar-contratante/"));
        JSONArray oldFalhos = new JSONArray();
        if (catFalhos.exists()) {
            JSONArray t = catFalhos.getArray();
            if (t != null) {
                oldFalhos = t;
            }
        }

        JSONObject dados = new JSONObject();
        for (Map.Entry<String, String> d : dadosI.entrySet()) {
            dados.put(d.getKey(), d.getValue());
        }

        CSPArquivosLocaisJson cat = new CSPArquivosLocaisJson((getPathContratanteToReceive(cnpjContratante) + "/pedidos-pendentes.json").replace("enviar-contratante/enviar-contratante/", "enviar-contratante/"));
        JSONArray old = new JSONArray();
        if (cat.exists()) {
            JSONArray t = cat.getArray();
            if (t != null) {
                old = t;
            }
        }

        JSONArray itens = new JSONArray();
        //Registramos os itens
        int codItem = 1;

        for (Object o : info.getJSONArray("ITENS")) {
            JSONObject item = (JSONObject) o;

            //Só vai pro MG o que o CAPP entender que é 'cadastrável'
            if (item.has("ID_PRODUTO") && !item.isNull("ID_PRODUTO") && !item.getString("ID_PRODUTO").equals("--")) {
                HashMap<String, String> hash = new HashMap<>();
                hash.put("ITENS_NUMERO", codItem + "");
                hash.put("PEDIDO_NUMERO", idPedido);
                hash.put("PRODUTO_CODIGO", item.getString("ID_PRODUTO"));
                hash.put("QUANTIDADE", "" + item.getInt("QTDE_PRODUTO"));
                hash.put("PRECO_VENDA", item.getString("PRECO_PRODUTO"));
                hash.put("GRUPO_CODIGO", item.getString("ID_CATEGORIA"));
                hash.put("TAMANHO", CSPUtilidadesLangJson.getFromJson(item, "TAMANHO", (String) null));
                hash.put("OPCIONAIS", CSPUtilidadesLangJson.getFromJson(item, "OPCIONAIS_ADD", (String) null));
                hash.put("SABORES", CSPUtilidadesLangJson.getFromJson(item, "SABORES", (String) null));
                hash.put("INGREDIENTES_ADICIONAIS", CSPUtilidadesLangJson.getFromJson(item, "INGREDIENTES_ADICIONAIS", (String) null));
                //Sim, é INGREDIENTES_EDITADOS na base e INGREDIENTES_REMOVIDOS no JSON!
                hash.put("INGREDIENTES_EDITADOS", CSPUtilidadesLangJson.getFromJson(item, "INGREDIENTES_REMOVIDOS", (String) null));
                itens.put(hash);
            } else {
                //O que não for é armazenado na central dentro do diretório do contratante
                item.put("PEDIDO_NUMERO", idPedido);
                item.put("DISPOSITIVO_ID", idDispositivo);
                oldFalhos.put(item);
            }
            ++codItem;
        }
        if (idUsuario.equals("?")
                || !"eb".equals(dadosI.get("PAGAMENTO"))
                || (dadosI.get("PAGAMENTO") != null
                && !dadosI.get("PAGAMENTO").trim().isEmpty()
                && dadosI.get("PAGAMENTO").equals("eb")
                && Double.parseDouble(totalShowInDispositivo) <= (double) onNeedGetCurrentValorBonusUserLivre.run(idUsuario))) {
            dados.put("ITENS", itens);

            old.put(dados);
            cat.setArray(old);
            catFalhos.setArray(oldFalhos);//Itens com problemas!
            if (!idUsuario.equals("?")) {
//                addNewBonusBloqueadoUser(idUsuario, idDispositivo, cnpjContratante, idPedido, Double.parseDouble(totalShowInDispositivo), "eb".equals((dadosI.get("PAGAMENTO") + "").trim()));
                onNeedAddNewBonusBloqueadoUser.run(idUsuario, idDispositivo, cnpjContratante, idPedido, Double.parseDouble(totalShowInDispositivo), "eb".equals((dadosI.get("PAGAMENTO") + "").trim()));
            }
        } else {
//            CSPKitCapp.addNotificacaoUser(idUsuario + "", "03", "egula", new JSONArray());
            onNeedAddNotificacaoUser.run(idUsuario + "", "03", "egula", new JSONArray());
        }

//        updateCodigoPedido(idPedido);
        onNeedUpdateCodigoPedido.run(idPedido);
//                System.out.println(CSPKitCapp.isRestauranteMonitoradoPeloAuxiliar(cnpjContratante));
        FrmModuloPaiBase.simpleThread(() -> {
            onNeedEndNewPedido.run(cnpjContratante, dadosI, itens, totalShowInDispositivo, idPedido, idDispositivo);
        });
        return new JSONObject();
    }

    /**
     * Cria um arquivo de texto no caminho passado. Usado para a última
     * movimentacao do dispositivo.
     *
     * @param idDev String - ID do dispositivo
     * @throws Exception
     */
    public static void ultimaMovimentacaoDev(String idDev) throws Exception {
        if (idDev != null && !idDev.trim().isEmpty() && !idDev.equals("?")) {

            CSPArquivosLocais movimentacao = new CSPArquivosLocais(getPathDev(idDev));
            movimentacao.setName("ultima-movimentacao.txt");
            movimentacao.setContent(lastMovimentacaoDevFormat.format(new Date()));
            lastMovimentacaoDev.put(idDev, new Date());
        }
    }

    /**
     * Retorna a última movimentação do dispositivo
     *
     * @param idDev String - ID do dispositivo
     * @return
     */
    public static Date getUltimaMovimentacaoDev(String idDev) throws Exception {
        final String def = "01/01/2016 00:00:00";

        if (idDev != null && !idDev.trim().isEmpty() && !idDev.equals("?")) {

            if (lastMovimentacaoDev.containsKey(idDev) && lastMovimentacaoDev.get(idDev) != null) {
                return lastMovimentacaoDev.get(idDev);
            }

            final CSPArquivos last = new CSPArquivos(getPathDev(idDev) + "/ultima-movimentacao.txt");
            if (last.exists() && last.isFile() && last.getContent() != null && !last.getContent().trim().isEmpty()) {
                lastMovimentacaoDev.put(idDev, lastMovimentacaoDevFormat.parse(last.getContent()));
                return lastMovimentacaoDev.get(idDev);
            }

            last.setContent(def);
        }

        return lastMovimentacaoDevFormat.parse(def);
    }

    /**
     * Retorna o caminho ideal para o app
     *
     * @param src String - Src da imagem na base
     * @param cnpj String - CNPJ do contratante
     */
    private static synchronized String buildSrcImgApp(String src, String cnpj, String tabela, String cod, CSPArquivosLocais toSave, CSPArquivosLocais pathImgs) throws IOException {

        String log = null;

        if (src == null || src.trim().isEmpty()) {
            log = "?>campo-imagem-nulo>solicitante:" + tabela + "->" + cod;
        } else if (src.contains("/") || src.contains("\\") || src.contains(":")) {
            log = "?>campo-imagem-com-caminho-absoluto>solicitante:" + tabela + "->" + cod;
            src = FilenameUtils.getName(src);
        }

        if (src != null && !src.trim().isEmpty()) {
            pathImgs.setName(src);
            if (!pathImgs.exists()) {
                log = pathImgs.getAbsolutePath() + ">nao-existe>solicitante:" + tabela + "->" + cod;

            }
        }

        if (log != null) {
            CSPLog.error(CSPKitCapp.class,
                    log);
            toSave.appendContent(log + CSPUtilidadesSO.LINE_SEPARATOR);
        }

        return (cnpj + "_e_" + src);
    }

    /**
     * Atualiza o arquivo "categorias" do contratante.
     *
     * Esse arquivo dita todo o conteúdo do app
     *
     * @param con CSPInstrucoesSQLBase - Conexão com base do contratante
     * @param cnpj String - CNPJ do contratante a ser gerado
     * @throws java.lang.Exception
     */
    public static void updateCategoriaContratanteFile(CSPInstrucoesSQLBase con, String cnpj) throws Exception {

        final CSPArquivosLocais toSave = new CSPArquivosLocais(CSPKitCapp.getPathContratanteToReceive(cnpj) + "/imagens-nao-encontradas.txt");
        final CSPArquivosLocais pathImgs = new CSPArquivosLocais(CSPKitCapp.getPathContratanteToSend(cnpj));
        toSave.delete();
        final HashMap<String, String> relacaoCodProdToAppCod = new HashMap<>();
        int countGroupsWithOne = 0;
        int indexLastGroupWithOne = -1;
        /**
         * Sem choro por causa dos while seguidos de for, isso foi necessário,
         * pois o JDBC fica fechando a porra do ResultSet quando instanciamos
         * outro
         */

        CSPLog
                .info(CSPKitCapp.class,
                        "Atualizando arquivo 'categoria' do contratante " + cnpj + "...");

        mkdirPastaContratante(cnpj);
        /**
         * Categorias/grupos do app
         */
        JSONArray arrCats = new JSONArray();

        ResultSet cats = con.select((StringBuilder sb) -> {
            sb.append("SELECT");
            sb.append("     r.GRUPOS_CODIGO,");
            sb.append("     r.DESCRICAO,");
            sb.append("     r.APP_IMAGEM,");
            sb.append("     iif((");
            sb.append("         select");
            sb.append("             count(c.GRUPOS_CODIGO)");
            sb.append("         FROM");
            sb.append("             PRODUTOS_GRUPOS c ");
            sb.append("         WHERE ");
            sb.append("             c.GRUPOS_CODIGO like r.GRUPOS_CODIGO||'.%'");
            sb.append("     ) > 0, 's', 'n') AS FROM_SUB, ");
            sb.append("     iif((  ");
            sb.append("         select  ");
            sb.append("             count(c.GRUPOS_CODIGO)  ");
            sb.append("         FROM  ");
            sb.append("             PRODUTOS_GRUPOS c ");
            sb.append("         WHERE  ");
            sb.append("             LOWER(c.APP) = 'x' ");
            sb.append("      ) = 1, 's', 'n') AS BACK_TO_MAIN ");
            sb.append("FROM");
            sb.append("     PRODUTOS_GRUPOS r ");
            sb.append("WHERE ");
            sb.append("     LOWER(r.APP) = 'x' ");
            sb.append("ORDER BY ");
            sb.append("     iif(r.APP_INDICE is not null, r.APP_INDICE, 100000) ASC, r.GRUPOS_CODIGO ASC ");
        });// ORDER BY r.DESTAQUE DESC

        while (cats.next()) {

            arrCats.put(new JSONObject() {
                {
                    put("ID", cats.getString("GRUPOS_CODIGO"));
                    put("TITULO", cats.getString("DESCRICAO"));
                    put("IMAGEM", buildSrcImgApp(cats.getString("APP_IMAGEM"), cnpj, "PRODUTOS_GRUPOS", cats.getString("GRUPOS_CODIGO"), toSave, pathImgs));
                    put("PRODUTOS", new JSONArray());
                    /**
                     * Dita se a categoria vai trabalhar no modo pizza...
                     * (escolher tamanho, opcionais e sabores)
                     */
                    put("IS_MODO_PIZZA", "n");
                    /**
                     * Dita se a categoria vai trabalhar no modo fórmula...
                     * (escolher tamanho, ingredientes e opcionais). Anula o
                     * modo PIZZA
                     */
                    put("IS_MODO_FORMULA", "n");
                    put("IS_FROM_SUB", cats.getString("FROM_SUB"));//Dita se categoria irá 'comportar' outras categorias
                    put("BACK_TO_MAIN", cats.getString("BACK_TO_MAIN"));//Dita se ao pressionar o 'back' deve jogar direto pra home

                }
            });
        }
        for (int g = 0; g < arrCats.length(); g++) {

            JSONObject o = arrCats.getJSONObject(g);
            String idCat = o.getString("ID");
            if (!idCat.contains(".")) {
                countGroupsWithOne++;
                indexLastGroupWithOne = g;
            }

            /**
             * Tamanhos disponíveis da categoria
             */
            JSONArray arrTamanhosCats = new JSONArray();
            ResultSet tamsCats = con.select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("     gru.TAMANHO_CODIGO,");
                sb.append("     tm.DESCRICAO,");
                sb.append("     gru.DESCRICAO_COMPLEMENTAR_APP,");
                sb.append("     gru.PRECO_VENDA,");
                sb.append("     gru.NUMERO_LIMITE_SABORES,");
                sb.append("     gru.NUMERO_LIMITE_OPCIONAIS,");
                sb.append("     gru.APP_IMAGEM,");
                sb.append("     gru.GRUPOS_CODIGO ");
//                sb.append("     gru.OPCIONAL_ACRESCIMO ");
                sb.append("FROM ");
                sb.append("     PRODUTOS_GRUPOS_TAMANHOS gru, ");
                sb.append("     TAMANHOS tm ");
                sb.append("WHERE ");
                sb.append("     tm.TAMANHO_CODIGO = gru.TAMANHO_CODIGO AND ");
                sb.append("     gru.GRUPOS_CODIGO = ? ");
                sb.append("ORDER BY ");
                sb.append("     gru.PRECO_VENDA DESC");
            }, idCat);

            while (tamsCats.next()) {
                arrTamanhosCats.put(new JSONObject() {
                    {
                        put("ID", tamsCats.getString("TAMANHO_CODIGO"));
                        put("DESCRICAO", (tamsCats.getString("DESCRICAO") + "").trim());
                        put("VALOR", tamsCats.getString("PRECO_VENDA"));
//                        put("VALOR_AC", tamsCats.getString("OPCIONAL_ACRESCIMO"));
                        put("VALOR_AC", tamsCats.getString("PRECO_VENDA"));
                        put("LIMITE_SABORES", tamsCats.getString("NUMERO_LIMITE_SABORES"));
                        put("LIMITE_OPCIONAIS", tamsCats.getString("NUMERO_LIMITE_OPCIONAIS"));
                        put("IMAGEM", buildSrcImgApp(tamsCats.getString("APP_IMAGEM"), cnpj, "PRODUTOS_GRUPOS_TAMANHOS", tamsCats.getString("TAMANHO_CODIGO"), toSave, pathImgs));
                        put("DESCRICAO_COMPLEMENTAR", tamsCats.getString("DESCRICAO_COMPLEMENTAR_APP"));
                    }
                });
            }

            o.put("TAMANHOS", arrTamanhosCats);

            if (arrTamanhosCats.length() > 0) {
                o.put("IS_MODO_PIZZA", "s");
            }

            /**
             * Ingredientes disponíveis da categoria
             */
            JSONArray arrGruIngredientesCats = new JSONArray();
            ResultSet grIgresCats = con.select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("     r.GRUPO_INGREDIENTE_CODIGO, ");
                sb.append("     r.DESCRICAO ");
                sb.append("FROM ");
                sb.append("     INGREDIENTES_GRUPOS r ");
                sb.append("WHERE ");
                sb.append("    (SELECT ");
                sb.append("        COUNT(pgi.PRODUTO_CODIGO) ");
                sb.append("    FROM ");
                sb.append("        PRODUTOS_GRUPOSXINGREDIENTES pgi ");
                sb.append("    WHERE ");
                ///   sb.append("        LOWER(pgi.APP) = 'x' AND ");
                sb.append("        pgi.GRUPOS_CODIGO = ? AND ");
                sb.append("        pgi.GRUPO_INGREDIENTE_CODIGO = r.GRUPO_INGREDIENTE_CODIGO ");
                sb.append("    ) > 0");
            }, idCat);

            while (grIgresCats.next()) {
                arrGruIngredientesCats.put(new JSONObject() {
                    {
                        put("ID", grIgresCats.getString("GRUPO_INGREDIENTE_CODIGO"));
                        put("DESCRICAO", (grIgresCats.getString("DESCRICAO") + "").trim());
                        put("INGREDIENTES", new JSONArray());
                    }
                });
            }

            if (arrGruIngredientesCats.length() > 0) {

                for (int i = 0; i < arrGruIngredientesCats.length(); i++) {

                    JSONObject gruIngredientes = arrGruIngredientesCats.getJSONObject(i);

                    JSONArray arrIngredientesCats = new JSONArray();
                    ResultSet igresCats = con.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("     pgi.PRODUTO_CODIGO, ");
                        sb.append("     pgi.VALOR_ADICIONAL, ");
                        sb.append("     p.DESCRICAO ");
                        sb.append("FROM ");
                        sb.append("     PRODUTOS_GRUPOSXINGREDIENTES pgi, ");
                        sb.append("     PRODUTOS p ");
                        sb.append("WHERE ");
                        sb.append("     p.PRODUTO_CODIGO = pgi.PRODUTO_CODIGO AND ");
                        ///sb.append("     LOWER(pgi.APP) = 'x' AND ");
                        sb.append("     pgi.GRUPOS_CODIGO = ? AND ");
                        sb.append("     pgi.GRUPO_INGREDIENTE_CODIGO = ? ");

                    }, idCat, gruIngredientes.getString("ID"));

                    while (igresCats.next()) {
                        arrIngredientesCats.put(new JSONObject() {
                            {
                                put("ID", igresCats.getString("PRODUTO_CODIGO"));
                                put("DESCRICAO", (igresCats.getString("DESCRICAO") + "").trim());
                                put("VALOR", igresCats.getString("VALOR_ADICIONAL"));
                            }
                        });
                        o.put("IS_MODO_PIZZA", "n");
                        o.put("IS_MODO_FORMULA", "s");
                    }

                    gruIngredientes.put("INGREDIENTES", arrIngredientesCats);
                }
            }

            o.put("INGREDIENTES_GRUPOS", arrGruIngredientesCats);

            if (o.getString("IS_MODO_PIZZA").equals("s") || o.getString("IS_MODO_FORMULA").equals("s")) {

                /**
                 * Opcionais disponíveis da categoria. Borda é um opcional, por
                 * exemplo.
                 */
                JSONArray arrOpsCat = new JSONArray();

                ResultSet opsCat = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     oi.ITEM_CODIGO,");
                    sb.append("     o.OPCIONAIS_CODIGO, ");
                    sb.append("     oi.DESCRICAO, ");
                    sb.append("     pgo.ADICIONAL_PERCENTUAL, ");
                    sb.append("     iif(pgo.ADICIONAL_PERCENTUAL = 0,pgo.ADICIONAL_VALOR,0) AS ADICIONAL_VALOR, ");
                    sb.append("     o.DESCRICAO AS OPCIONAIS_TITULO, ");
                    sb.append("     o.MODALIDADE AS OPCIONAIS_MODALIDADE, ");
                    sb.append("     pgo.APP_IMAGEM, ");
                    sb.append("     iif(LOWER(oi.DESCRICAO) like '%sem borda%' OR  ");
                    sb.append("             ( ");
                    sb.append("                 o.MODALIDADE = 1 AND  ");
                    sb.append("                 ( ");
                    sb.append("                 SELECT  ");
                    sb.append("                     FIRST 1 oii.ITEM_CODIGO  ");
                    sb.append("                 FROM  ");
                    sb.append("                     OPCIONAIS_ITEM oii ");
                    sb.append("                 WHERE  ");
                    sb.append("                     oii.OPCIONAIS_CODIGO = oi.OPCIONAIS_CODIGO ");
                    sb.append("             ) = oi.ITEM_CODIGO ");
                    sb.append("      ), 's', 'n') AS IS_DEFAULT ");
                    sb.append("FROM ");
                    sb.append("     OPCIONAIS o, ");
                    sb.append("     OPCIONAIS_ITEM oi,");
                    sb.append("     PRODUTOS_GRUPOS_OPCIONAIS pgo ");
                    sb.append("WHERE ");
                    sb.append("     oi.OPCIONAIS_CODIGO = pgo.OPCIONAIS_CODIGO AND ");
                    sb.append("     oi.ITEM_CODIGO = pgo.ITEM_CODIGO AND ");
                    sb.append("     o.OPCIONAIS_CODIGO = pgo.OPCIONAIS_CODIGO AND ");
                    sb.append("     pgo.GRUPOS_CODIGO = ?");
                }, idCat);

                while (opsCat.next()) {

                    String cod = opsCat.getString("OPCIONAIS_CODIGO");
                    JSONObject jsonT = new JSONObject();
                    jsonT.put("OPCS", new JSONArray());

                    for (int i = 0; i < arrOpsCat.length(); i++) {
                        JSONObject tt = arrOpsCat.getJSONObject(i);
                        if (tt.getString("ID").equals(cod)) {
                            jsonT = tt;
                            arrOpsCat.remove(i);
                            break;
                        }
                    }

                    JSONArray arrT = jsonT.getJSONArray("OPCS");
                    arrT.put(new JSONObject() {
                        {
                            put("ID", opsCat.getString("ITEM_CODIGO"));
                            put("DESCRICAO", opsCat.getString("DESCRICAO"));
                            put("VALOR", opsCat.getString("ADICIONAL_VALOR"));
                            put("PERCENTUAL", opsCat.getString("ADICIONAL_PERCENTUAL"));
                            put("IMAGEM", buildSrcImgApp(opsCat.getString("APP_IMAGEM"), cnpj, "PRODUTOS_GRUPOS_OPCIONAIS", opsCat.getString("ITEM_CODIGO"), toSave, pathImgs));
                            put("ALLOW_AC", false);
                            put("IS_DEFAULT", opsCat.getString("IS_DEFAULT"));
                            put("IS_HIDE_VAL", cnpj.equals("07720640000115"));
                        }
                    });

                    jsonT.put("ID", cod);
                    jsonT.put("TITULO", opsCat.getString("OPCIONAIS_TITULO"));
                    jsonT.put("MODALIDADE", opsCat.getString("OPCIONAIS_MODALIDADE"));

                    arrOpsCat.put(jsonT);
                }

                for (int i = 0; i < arrOpsCat.length(); i++) {

                    JSONObject tt = arrOpsCat.getJSONObject(i);

                    /**
                     * Cada opcional pode ter um preço diferenciado para cada
                     * tamanho em especifico. Para evitar que o app fique
                     * processando vamos apenas mandar para o mesmo o valor a
                     * ser somado ao opcional para cada tamanho disponível
                     */
                    JSONObject adcObj = new JSONObject();
                    ResultSet adic = con.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("     gtoa.VALOR_ADICIONAL, ");
                        sb.append("     (pgt.TAMANHO_CODIGO || '-' || gtoa.ITEM_CODIGO) AS TAMANHO_CODIGO ");
                        sb.append("FROM ");
                        sb.append("     GRUPO_TAMANHO_OPCIONAL_ADICIONA gtoa, ");
                        sb.append("     PRODUTOS_GRUPOS_TAMANHOS pgt ");
                        sb.append("WHERE ");
                        sb.append("     pgt.TAMANHO_CODIGO = gtoa.TAMANHO_CODIGO AND ");
                        sb.append("     pgt.GRUPOS_CODIGO = gtoa.GRUPOS_CODIGO AND ");
                        sb.append("     gtoa.OPCIONAIS_CODIGO = ? AND ");
                        sb.append("     gtoa.GRUPOS_CODIGO = ? ");
                    }, tt.getString("ID"), idCat);

                    boolean jaAdded = false;

                    while (adic.next()) {
                        adcObj.put(adic.getString("TAMANHO_CODIGO"), adic.getDouble("VALOR_ADICIONAL"));

                        if (!jaAdded) {

                            for (int ii = 0; ii < tt.getJSONArray("OPCS").length(); ii++) {
                                tt.getJSONArray("OPCS").getJSONObject(ii).put("ALLOW_AC", true);
                            }

                            jaAdded = true;
                        }
                    }

                    tt.put("ADICIONAIS_TAMANHO", adcObj);
                }

                o.put("OPCIONAIS", arrOpsCat);
            }

            /**
             * Produtos disponíveis na categoria
             */
            JSONArray arrProds = new JSONArray();
            ResultSet prods = con.select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("     r.PRODUTO_CODIGO, ");
                sb.append("     r.APP_CODIGO, ");
                sb.append("     iif(r.APP_DESCRICAO is null,r.DESCRICAO,r.APP_DESCRICAO) AS DESCRICAO, ");
                sb.append("     r.PRECO_VENDA,  ");
                sb.append("     r.APP_IMAGEM ");
                sb.append("FROM ");
                sb.append("     PRODUTOS r ");
                sb.append("WHERE ");
                sb.append("     LOWER(r.APP) = 'x' AND ");
                sb.append("     r.GRUPOS_CODIGO = ?");
                sb.append("ORDER BY ");
                sb.append("     iif(r.APP_INDICE is not null,r.APP_INDICE,100000) ASC, CAST(r.PRODUTO_CODIGO AS FLOAT) ASC ");
            }, idCat);

            while (prods.next()) {

                arrProds.put(new JSONObject() {
                    {
                        put("ID", prods.getString("PRODUTO_CODIGO"));
                        put("TITULO", prods.getString("DESCRICAO"));
                        put("IMAGEM", buildSrcImgApp(prods.getString("APP_IMAGEM"), cnpj, "PRODUTOS", prods.getString("PRODUTO_CODIGO"), toSave, pathImgs));
                        put("DESCRICAO", prods.getString("DESCRICAO"));
                        put("PRECO", prods.getString("PRECO_VENDA"));
                        if (prods.getString("APP_CODIGO") != null) {
                            relacaoCodProdToAppCod.put(prods.getString("PRODUTO_CODIGO"), prods.getString("APP_CODIGO"));
                        }
                        //   put("COD_OPCIONAIS_DISPONIVEIS", prods.getString("COD_OPCIONAIS_DISPONIVEIS"));
                        //  put("USA_OPCIONAIS", prods.getString("USA_OPCIONAIS"));
                    }
                });
            }

            for (Object p : arrProds) {

                JSONObject oo = (JSONObject) p;
                String idProd = oo.getString("ID");

                /**
                 * Cada produto pode ter o seu tamanho especifico
                 */
                JSONArray arrTamanhosProd = new JSONArray();
                ResultSet tamsProd = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     pro.TAMANHO_CODIGO,");
                    sb.append("     tm.DESCRICAO,");
                    sb.append("     pro.DESCRICAO_COMPLEMENTAR_APP,");
                    sb.append("     pro.PRECO_VENDA ");
                    sb.append("FROM ");
                    sb.append("     PRODUTOS_TAMANHOS pro, ");
                    sb.append("     TAMANHOS tm  ");
                    sb.append("WHERE ");
                    sb.append("     tm.TAMANHO_CODIGO = pro.TAMANHO_CODIGO  AND ");
                    sb.append("     pro.PRODUTO_CODIGO = ? ");
                    sb.append("ORDER BY ");
                    sb.append("     pro.PRECO_VENDA DESC");
                }, idProd);

                while (tamsProd.next()) {
                    arrTamanhosProd.put(new JSONObject() {
                        {
                            put("ID", tamsProd.getString("TAMANHO_CODIGO"));
                            put("DESCRICAO", (tamsProd.getString("DESCRICAO") + "").trim());
                            put("VALOR", tamsProd.getString("PRECO_VENDA"));
                            put("DESCRICAO_COMPLEMENTAR", tamsProd.getString("DESCRICAO_COMPLEMENTAR_APP"));
                        }
                    });
                }

                oo.put("TAMANHOS", arrTamanhosProd);

                /**
                 * Bem como seus ingredientes
                 */
                JSONArray arrIngrsBases = new JSONArray();
                ResultSet ingrsBases = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     ing.INGREDIENTE, ");
                    sb.append("     iif(prod.APP_DESCRICAO is null,prod.DESCRICAO,prod.APP_DESCRICAO) AS DESCRICAO, ");
                    sb.append("     iif(ing.CONFIGURACAO <> 1,'s',null)  AS REMOVIVEL, ");
                    sb.append("     iif(ing.CONFIGURACAO = 4,");
                    sb.append("     ing.PRODUTO_SUBSTITUICAO, ");
                    sb.append("     iif(0 <> (");
                    sb.append("         SELECT ");
                    sb.append("             COUNT(inga.INGREDIENTE) ");
                    sb.append("         FROM ");
                    sb.append("             PRODUTOS_INGREDIENTES inga ");
                    sb.append("         WHERE ");
                    sb.append("             inga.PRODUTO_SUBSTITUICAO = ing.INGREDIENTE");
                    sb.append("     ),ing.INGREDIENTE,'-')) AS GRUPO_SUBIST ");
                    sb.append("FROM ");
                    sb.append("     PRODUTOS_INGREDIENTES ing, ");
                    sb.append("     PRODUTOS prod ");
                    sb.append("WHERE ");
                    sb.append("     ing.PRODUTO_CODIGO = ? AND ");
                    sb.append("     prod.PRODUTO_CODIGO = ing.INGREDIENTE AND ");
                    sb.append("     (");
                    sb.append("         (ing.CONFIGURACAO = 1 OR ing.CONFIGURACAO = 2) OR ");
                    sb.append("         (ing.CONFIGURACAO = 4 AND ing.PRODUTO_SUBSTITUICAO in (");
                    sb.append("                                                             SELECT ");
                    sb.append("                                                                     inga.INGREDIENTE ");
                    sb.append("                                                             FROM ");
                    sb.append("                                                                     PRODUTOS_INGREDIENTES inga ");
                    sb.append("                                                             WHERE ");
                    sb.append("                                                                     inga.PRODUTO_CODIGO = ing.PRODUTO_CODIGO AND ");
                    sb.append("                                                                     (inga.CONFIGURACAO = 1 OR inga.CONFIGURACAO = 2)");
                    sb.append("                                                               )");
                    sb.append("         )");
                    sb.append("     )");
                    sb.append("ORDER BY");
                    sb.append("     iif(ing.APP_INDICE is not null, ing.APP_INDICE,ing.INGREDIENTE) ASC");

                }, idProd);

                while (ingrsBases.next()) {

                    arrIngrsBases.put(new JSONObject() {
                        {
                            put("ID", ingrsBases.getString("INGREDIENTE"));
                            put("DESCRICAO", ingrsBases.getString("DESCRICAO"));
                            put("IS_REMOVIVEL", ingrsBases.getString("REMOVIVEL"));
                            put("GRUPO_SUBIST", ingrsBases.getString("GRUPO_SUBIST"));

                        }
                    });
                }

                oo.put("INGREDIENTES_BASE", arrIngrsBases);
                /**
                 * E também ingredientes adicionais
                 */
                JSONArray arrIngrsAdicionais = new JSONArray();
                ResultSet ingrsAdicionais = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     ing.INGREDIENTE, ");
                    sb.append("     iif(prod.APP_DESCRICAO is null,prod.DESCRICAO,prod.APP_DESCRICAO) AS DESCRICAO, ");
                    sb.append("     ing.VALOR_ADICIONAL,");
                    sb.append("     iif(ing.CONFIGURACAO = 4,ing.PRODUTO_SUBSTITUICAO,iif(0 <> (");
                    sb.append("                                                                 SELECT ");
                    sb.append("                                                                     COUNT(inga.INGREDIENTE) ");
                    sb.append("                                                                 FROM ");
                    sb.append("                                                                     PRODUTOS_INGREDIENTES inga ");
                    sb.append("                                                                 WHERE ");
                    sb.append("                                                                     inga.PRODUTO_SUBSTITUICAO = ing.INGREDIENTE");
                    sb.append("                                                                 ),ing.INGREDIENTE,'-'");
                    sb.append("     )) AS GRUPO_SUBIST ");
                    sb.append("FROM ");
                    sb.append("     PRODUTOS_INGREDIENTES ing, ");
                    sb.append("     PRODUTOS prod ");
                    sb.append("WHERE  ");
                    sb.append("     ing.PRODUTO_CODIGO = ? AND ");
                    sb.append("     prod.PRODUTO_CODIGO = ing.INGREDIENTE AND ");
                    sb.append("     (");
                    sb.append("         ing.CONFIGURACAO = 3 OR ");
                    sb.append("         (ing.CONFIGURACAO = 4 AND ing.PRODUTO_SUBSTITUICAO in  (");
                    sb.append("                                                                 SELECT ");
                    sb.append("                                                                         inga.INGREDIENTE ");
                    sb.append("                                                                 FROM ");
                    sb.append("                                                                         PRODUTOS_INGREDIENTES inga ");
                    sb.append("                                                                 WHERE ");
                    sb.append("                                                                         inga.PRODUTO_CODIGO = ing.PRODUTO_CODIGO AND ");
                    sb.append("                                                                         inga.CONFIGURACAO = 3)");
                    sb.append("         )");
                    sb.append("     )");
                    sb.append("ORDER BY");
                    sb.append("     iif(ing.APP_INDICE is not null, ing.APP_INDICE,ing.INGREDIENTE) ASC");
                }, idProd);

                while (ingrsAdicionais.next()) {
                    arrIngrsAdicionais.put(new JSONObject() {
                        {
                            put("ID", ingrsAdicionais.getString("INGREDIENTE"));
                            put("DESCRICAO", ingrsAdicionais.getString("DESCRICAO"));
                            put("VALOR", ingrsAdicionais.getString("VALOR_ADICIONAL"));
                            put("GRUPO_SUBIST", ingrsAdicionais.getString("GRUPO_SUBIST"));
                        }
                    });
                }

                oo.put("INGREDIENTES_ADICIONAIS", arrIngrsAdicionais);

                /**
                 * Opcionais disponíveis do produto. Borda é um opcional, por
                 * exemplo.
                 */
                JSONArray arrOpsProd = new JSONArray();

                ResultSet opsProd = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     oi.ITEM_CODIGO, ");
                    sb.append("     o.OPCIONAIS_CODIGO, ");
                    sb.append("     oi.DESCRICAO, ");
                    sb.append("     po.ADICIONAL_PERCENTUAL, ");
                    sb.append("     iif(po.ADICIONAL_PERCENTUAL = 0,po.ADICIONAL_VALOR,0) AS ADICIONAL_VALOR, ");
                    sb.append("     o.DESCRICAO AS OPCIONAIS_TITULO, ");
                    sb.append("     o.MODALIDADE AS OPCIONAIS_MODALIDADE, ");
                    sb.append("     null AS APP_IMAGEM, ");
                    sb.append("    'n' AS IS_DEFAULT ");
                    sb.append("FROM ");
                    sb.append("     OPCIONAIS o, ");
                    sb.append("     OPCIONAIS_ITEM oi, ");
                    sb.append("     PRODUTOS_OPCIONAIS po ");
                    sb.append("WHERE ");
                    sb.append("     oi.OPCIONAIS_CODIGO = po.OPCIONAIS_CODIGO AND ");
                    sb.append("     oi.ITEM_CODIGO = po.ITEM_CODIGO AND ");
                    sb.append("     o.OPCIONAIS_CODIGO = po.OPCIONAIS_CODIGO AND ");
                    sb.append("     po.PRODUTO_CODIGO = ? ");

                }, idProd);

                while (opsProd.next()) {

                    final String cod = opsProd.getString("OPCIONAIS_CODIGO");
                    JSONObject jsonT = new JSONObject();

                    jsonT.put("OPCS", new JSONArray());

                    for (int i = 0; i < arrOpsProd.length(); i++) {
                        JSONObject tt = arrOpsProd.getJSONObject(i);
                        if (tt.getString("ID").equals(cod)) {
                            jsonT = tt;
                            arrOpsProd.remove(i);
                            break;
                        }
                    }

                    JSONArray arrT = jsonT.getJSONArray("OPCS");

                    arrT.put(new JSONObject() {
                        {
                            put("ID", opsProd.getString("ITEM_CODIGO"));
                            put("DESCRICAO", opsProd.getString("DESCRICAO"));
                            put("VALOR", opsProd.getString("ADICIONAL_VALOR"));
                            put("PERCENTUAL", opsProd.getString("ADICIONAL_PERCENTUAL"));
                            //put("IMAGEM", buildSrcImgApp(opsCat.getString("APP_IMAGEM"), cnpj, "PRODUTOS_GRUPOS_OPCIONAIS", opsCat.getString("ITEM_CODIGO"), toSave, pathImgs));
                            put("IMAGEM", "");
                            put("ALLOW_AC", false);
                            put("IS_DEFAULT", opsProd.getString("IS_DEFAULT"));
                            put("IS_HIDE_VAL", cnpj.equals("07720640000115"));
                        }
                    });

                    jsonT.put("ID", cod);
                    jsonT.put("TITULO", opsProd.getString("OPCIONAIS_TITULO"));
                    jsonT.put("MODALIDADE", opsProd.getString("OPCIONAIS_MODALIDADE"));

                    arrOpsProd.put(jsonT);
                }

                oo.put("OPCIONAIS", arrOpsProd);

            }

            o.put("PRODUTOS", arrProds);

            JSONArray arrSabrs = new JSONArray();
            /**
             * Sabores disponíveis na categoria
             */
            ResultSet ingrsSabs = con.select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("     sb.SABORES_CODIGO, ");
                sb.append("     sb.DESCRICAO, ");
                sb.append("     gru.ADICIONAL_VALOR ");
                sb.append("FROM ");
                sb.append("     PRODUTOS_GRUPO_SABORES gru, ");
                sb.append("     SABORES sb ");
                sb.append("WHERE ");
                sb.append("     sb.SABORES_CODIGO = gru.SABORES_CODIGO AND ");
                sb.append("     gru.GRUPOS_CODIGO = ? ");
                sb.append("ORDER BY ");
                sb.append("     sb.DESCRICAO ASC ");
            }, idCat);

            while (ingrsSabs.next()) {
                arrSabrs.put(new JSONObject() {
                    {
                        put("ID", ingrsSabs.getString("SABORES_CODIGO"));
                        put("DESCRICAO", ingrsSabs.getString("DESCRICAO"));
                        //Valor adicional individual 
                        put("VALOR", 0);
                        /**
                         * Valor adicional para calculo em grudo. Encontra-se o
                         * maior valor entre os selecionados pelo user e usa-o
                         * como valor de acréscimo
                         */
                        put("VALOR_UNICO_MAIOR", ingrsSabs.getDouble("ADICIONAL_VALOR"));
                    }
                });
            }

            for (Object p : arrSabrs) {

                JSONObject oo = (JSONObject) p;
                String idProd = oo.getString("ID");

                /**
                 * Um sabor pode possuir o seus ingredientes
                 */
                JSONArray arrIngrsBases = new JSONArray();
                ResultSet ingrsBases = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     ing_sab.PRODUTO_CODIGO,");
                    sb.append("     iif(prod.APP_DESCRICAO is null,prod.DESCRICAO,prod.APP_DESCRICAO) AS DESCRICAO, ");
                    sb.append("     iif(ing_sab.CONFIGURACAO = 2,'s',null) AS REMOVIVEL ");
                    sb.append("FROM ");
                    sb.append("     PRODUTOS_GRUPOS_SABORES_INGREDI ing_sab, ");
                    sb.append("     PRODUTOS prod ");
                    sb.append("WHERE ");
                    sb.append("     ing_sab.GRUPOS_CODIGO = ? AND ");
                    sb.append("     ing_sab.SABORES_CODIGO = ? AND ");
                    sb.append("     prod.PRODUTO_CODIGO = ing_sab.PRODUTO_CODIGO AND ");
                    sb.append("     (ing_sab.CONFIGURACAO = 1 OR ing_sab.CONFIGURACAO = 2)");
                }, idCat, idProd);

                while (ingrsBases.next()) {

                    arrIngrsBases.put(new JSONObject() {
                        {
                            put("ID", ingrsBases.getString("PRODUTO_CODIGO"));
                            put("DESCRICAO", ingrsBases.getString("DESCRICAO"));
                            put("IS_REMOVIVEL", ingrsBases.getString("REMOVIVEL"));
                            //    put("GRUPO_SUBIST", ingrsBases.getString("GRUPO_SUBIST"));

                        }
                    });
                }

                oo.put("INGREDIENTES_BASE", arrIngrsBases);

                /**
                 * Bem como também pode possuir ingredientes adicionais
                 */
                JSONArray arrIngrsAdicionais = new JSONArray();

                ResultSet ingrsAdicionais = con.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     ing_sab.PRODUTO_CODIGO, ");
                    sb.append("     iif(prod.APP_DESCRICAO is null,prod.DESCRICAO,prod.APP_DESCRICAO) AS DESCRICAO, ");
                    sb.append("     ing_sab.VALOR_ADICIONAL ");
                    sb.append("FROM ");
                    sb.append("     PRODUTOS_GRUPOS_SABORES_INGREDI ing_sab, ");
                    sb.append("     PRODUTOS prod ");
                    sb.append("WHERE ");
                    sb.append("     ing_sab.GRUPOS_CODIGO = ? AND ");
                    sb.append("     ing_sab.SABORES_CODIGO = ? AND ");
                    sb.append("     prod.PRODUTO_CODIGO = ing_sab.PRODUTO_CODIGO AND ");
                    sb.append("     ing_sab.CONFIGURACAO = 3");

                }, idCat, idProd);

                while (ingrsAdicionais.next()) {

                    arrIngrsAdicionais.put(new JSONObject() {
                        {
                            put("ID", ingrsAdicionais.getString("PRODUTO_CODIGO"));
                            put("DESCRICAO", ingrsAdicionais.getString("DESCRICAO"));
                            put("VALOR", ingrsAdicionais.getString("VALOR_ADICIONAL"));
                            ///put("GRUPO_SUBIST", ingrsBases.getString("GRUPO_SUBIST"));
                        }
                    });

                }

                oo.put("INGREDIENTES_ADICIONAIS", arrIngrsAdicionais);

            }

            o.put("SABORES", arrSabrs);

        }

        if (countGroupsWithOne == 1) {
            /**
             * Sempre quando só existir uma categoria inicial vamos forçar o app
             * redirecionar para a mesma ao abrir a tela de categorias
             */
            arrCats.getJSONObject(indexLastGroupWithOne).put("IS_FORCE_REDIRECT", "s");
        }

        String content = arrCats.toString();

        for (Map.Entry<String, String> rp : relacaoCodProdToAppCod.entrySet()) {
            /**
             * Vamos trocar todos o códigos de produtos por um código mais
             * simplificado, o código APP
             */
            content = content.replace("\"" + rp.getKey() + "\"", "\"" + rp.getValue() + "-a\"");// "-a" -> identifica que é do app
        }

        CSPArquivosAssinados.getInstance(CSPKitCapp.getPathContratanteToSend(cnpj) + "/categorias.json").setContent(content);
        updateConteudoCacheArquivo(cnpj + "-categorias", content);
        CSPLog
                .info(CSPKitCapp.class,
                        "Atualizando arquivo 'categoria' do contratante " + cnpj + "...OK");
    }

    /**
     * Cria a estrutura de pastas base do contratante
     *
     * @param cnpj String - CNPJ contratante
     */
    public static void mkdirPastaContratante(String cnpj) throws Exception {
        CSPArquivos contratante = new CSPArquivos(CSPKitCapp.getPathContratante(cnpj));

        contratante.mkdirs();
        contratante.setCanAllFolderAndContent(true);

        //Arquivos a serem enviados para os apps
        contratante.setPath(CSPKitCapp.getPathContratanteToSend(cnpj));
        contratante.mkdirs();
        contratante.setCanAllFolderAndContent(true);

        //Arquivos, informações, a serem enviadas ao contratante. Por exemplo.: pedidos, notificações....
        contratante.setPath(CSPKitCapp.getPathContratanteToReceive(cnpj));
        contratante.mkdirs();
        contratante.setCanAllFolderAndContent(true);

    }

    /**
     * Atualiza o arquivo atual.txt do contratante.
     *
     * Esse arquivo armazeta todos os MD5, de todos os arquivos do contratante
     * que podem ser enviados aos apps
     *
     * @param cnpj String - CNPJ contratante
     */
    public static void updateAtualFileContratante(String cnpj) throws Exception {

        mkdirPastaContratante(cnpj);

        CSPArquivosLocais contratante = new CSPArquivosLocais(CSPKitCapp.getPathContratanteToSend(cnpj));

        String files = "";
        String atual = null;

        String needContains = CSPKitCapp.listaContratantesFull == null ? "" : CSPKitCapp.listaContratantesFull.getContent(); //Vamos validar se os arquivos aqui listados estão sendo utilizados em algum lugar

        for (String file : contratante.list()) {
            if (file.startsWith("categorias-")) {
                contratante.setName(file);
                needContains += contratante.getContent();
                break;
            }
        }

        contratante.setName("");

        for (String file : contratante.list()) {
            if (!file.startsWith("atual-")) {
                if (!file.startsWith("categorias-") && !file.startsWith("notificacoes-")) {
                    /**
                     * Se um arquivo não é utilizado o mesmo não pode pertencer
                     * a lista de arquivos que podem ser baixados
                     */
                    if (!needContains.contains(file)) {
                        //contratante.setName(file);
                        // contratante.delete();
                        // contratante.setName("");
                        continue;
                    }
                }
                files += cnpj + "_e_" + file + LINE_SEPARATOR;
            } else {
                atual = file;
            }
        }

        contratante.setName("");

        if (atual != null) {
            contratante.setName(atual);
            contratante.delete();
        }

        contratante.setName("atual-" + getMd5(files) + ".txt");
        contratante.setContent(files);

        updateTotalFileContratantes();
    }

    /**
     * Atualiza o arquivo 'total.txt', que armazena o md5 e o nome de todos os
     * arquivos 'atual.txt' + 'lista-contratante.txt'
     *
     * O md5 é usado pelo app para verificar se precisa ser atualizado
     *
     */
    public static void updateTotalFileContratantes() throws Exception {

        final CSPArquivos contra = new CSPArquivos(CSPKitCapp.getPathContratantes());
        final StringBuilder total = new StringBuilder();
        final String listaContra = CSPKitCapp.listaContratantesFull.getContent();

        for (CSPArquivos file : contra.listFiles()) {
//            System.out.println(file.getName());
//            System.out.println(file.getName().equals(file.getName().replaceAll("[^0-9]", "")));
            if (file.getName().startsWith("lista-contratante")
                    || file.getName().startsWith("notificacoes-egula")
                    || file.getName().startsWith("regiao-cidades")
                    || file.getName().startsWith("ic_")) {

                total.append(file.getName());
                total.append(LINE_SEPARATOR);

            } else if (file.getName().equals(file.getName().replaceAll("[^0-9]", ""))) {

                contra.setPath(file.getAbsolutePath() + "/enviar-app");

                if (contra.exists() && contra.isDir()) {

                    for (String f : contra.list()) {

                        if (f.startsWith("atual-") && listaContra.contains(file.getName())) {

                            total.append(file.getName());
                            total.append("_e_");
                            total.append(f);
                            total.append(LINE_SEPARATOR);

                            break;

                        }

                    }

                }
            }
        }
        {//central de imagens
            contra.setPath(CSPKitCapp.getPathContratantes() + "/central-imagens/");
            for (CSPArquivos file : contra.listFiles()) {
                if (file.isDir()) {
                    for (CSPArquivos f : file.listFiles()) {
                        if (f.isFile()) {
                            total.append("central-imagens_");
                            total.append(file.getName());
                            total.append("_");
                            total.append(f.getName());
                            total.append(LINE_SEPARATOR);
                        }
                    }
                }
            }
        }

        CSPKitCapp.arquivoInfosTotalLista.setContent(total.toString());
        CSPKitCapp.arquivoInfosTotalMd5.setContent(CSPKitCapp.arquivoInfosTotalLista.getMd5());
    }

    private static HashMap<String, String> buildListRelationLastMd5(String id) {
        if (relationLastMd5XFile.containsKey(id)) {
            return relationLastMd5XFile.get(id);
        }
        final String tmp = getPathDev(id);
        final HashMap<String, String> r = new HashMap() {
            {
                {
                    put(tmp + "/last-md5-lista-contratantes.txt", tmp + "/" + id + "-lista-contratantes-app.json");
                    put(tmp + "/last-md5-confs-locais.txt", tmp + "/" + id + "-confs-locais-app.json");
                    put(tmp + "/last-md5-regiao-cidades.txt", tmp + "/" + id + "-regiao-cidades-app.json");
                }
            }
        };

        relationLastMd5XFile.put(id, r);

        return r;
    }

    public static InterfaceServidorTransferenciaArquivos.Conectando getInterfaceServidorTransferenciaArquivosConectandoArquivos() throws Exception {
        return (Socket sc, CSPArquivos arquivo, JSONObject infos) -> {
            CSPKitCapp.trataNomeFileAppToServer(arquivo);

            String idDev = getIdDevInJson(infos);

            if (idDev.isEmpty()) {
                idDev = "?";
            }
            ultimaMovimentacaoDev(idDev);

            if (!"?".equals(idDev) && arquivo.exists()) {
                CSPKitCapp.registraInicioDownloadArquivoPreparadoDev(idDev, arquivo.getAbsolutePath(), arquivo.length(), sc.getRemoteSocketAddress().toString());
                if (arquivo.getAbsolutePath().endsWith("app.json") && !getVersionApp(idDev).maiorOuIgualA(ad_1_8_3, io_1_6_9)) {
                    for (Map.Entry<String, String> e : buildListRelationLastMd5(idDev).entrySet()) {
                        if (arquivo.getAbsolutePath().endsWith(e.getValue())) {
                            new CSPArquivos(e.getKey()).setContent(arquivo.getMd5());
                            break;

                        }
                    }
                }
//                        return true;
            }

            if (!arquivo.exists()) {
                CSPLog.error(CSPKitCapp.class,
                        arquivo.getAbsolutePath() + " não existe!");
            }

            return arquivo.exists();
        };
    }

    public static InterfaceServidorTransferenciaArquivos.Desconectado getInterfaceServidorTransferenciaArquivosDesconectadoArquivos() throws Exception {
        return (Socket sc, CSPArquivos arquivo, JSONObject infos) -> {

            String idDev = getIdDevInJson(infos);

            if (idDev.isEmpty()) {
                idDev = "?";
            }
            ultimaMovimentacaoDev(idDev);

            if (!"?".equals(idDev) && arquivo.exists()) {

                CSPKitCapp.registraFimDownloadArquivoPreparadoDev(idDev, arquivo.getAbsolutePath());

                if (arquivo.getAbsolutePath().endsWith(idDev + "-bonus-app") && arquivo.isFile()) {
                    arquivo.delete();
                }

                if (arquivo.getAbsolutePath().endsWith("-logado") && arquivo.isFile()) {
                    arquivo.delete();
                }
            }

        };
    }

    /**
     * Atualiza o conteúdo que pode ser disponibilizado
     */
    public static void updateListaDisponibilizar() throws Exception {

//        arquivosParaDisponibilizar.clearAll();
        arquivosParaDisponibilizar.addFileToMonitore(CSPKitCapp.getPathContratantes() + "/central-imagens");

        for (CSPArquivos file : new CSPArquivos(CSPKitCapp.getPathContratantes()).listFiles()) {
            if ( /*file.getName().startsWith("lista-contratante")
                     ||*/file.getName().startsWith("notificacoes-egula")
                    /*|| file.getName().startsWith("regiao-cidades")*/
                    || file.getName().startsWith("ic_")) {

                arquivosParaDisponibilizar.addFileToMonitore(file);

            } else if (file.getName().equals(file.getName().replaceAll("[^0-9]", ""))) {
                final String cnpj = file.getName();
                file.setPath(file.getAbsolutePath() + "/enviar-app");
                if (file.exists() && file.isDir()) {

                    for (CSPArquivos f : file.listFiles()) {

                        if (!f.getName().startsWith("atual-")) {
                            arquivosParaDisponibilizar.addFileToMonitore(f);
                            if (f.getName().startsWith("categorias-")) {
                                updateConteudoCacheArquivo(cnpj + "-categorias", f.getContent());
                            }
                        }

                    }

                }
            }
        }
    }

    /**
     * Alimenta o cache de conteúdo dos arquivos importantes para o controle do
     * app. Nao necessariamente deve-se usar o nome completo do arquivo, mas sim
     * uma referencia ao mesmo. Exemplo: CNPJ+NOME ou ID+NOME
     *
     * @param arquivo String - Key de identificacao do arquivo, evitar uso de
     * nomes com md5
     * @param conteudo String
     */
    private static void updateConteudoCacheArquivo(String arquivo, String conteudo) {
//        System.out.println(arquivo);
        if (conteudo == null || conteudo.trim().isEmpty()) {
            conteudoCacheArquivos.put(arquivo, null);
        } else {
            conteudoCacheArquivos.put(arquivo, conteudo);
        }
        fileLiberadoToDevCache.clear();
    }

    /**
     * Retorna o conteudo do cache do arquivo controlado
     *
     * @param arquivo String - Key de identificacao do arquivo, evitar uso de
     * nomes com md5
     * @return null em caso nao existir
     */
    private static String getConteudoCacheArquivo(String arquivo) {
        if (conteudoCacheArquivos.containsKey(arquivo)) {
            return conteudoCacheArquivos.get(arquivo);
        }

        return null;
    }

    /**
     * Retorna a versão do app
     *
     * @param idDev String - ID do dispositivo
     * @return
     * @throws Exception
     */
    public static VersionsAppSupported getVersionApp(String idDev) throws Exception {

        if (appVersaoCache.containsKey(idDev)) {
            return appVersaoCache.get(idDev);
        }

        final CSPArquivos version = new CSPArquivos(CSPKitCapp.getPathDev(idDev) + "/current-version-app.txt");

        if (version.exists() && version.isFile()) {
            final String t = version.getContent().replace(".", "_").replace("\n", "").trim();
            final boolean isAd = idDev.startsWith("ad_");

            for (VersionsAppSupported vers : VersionsAppSupported.values()) {
                if (vers.isAndroid == isAd) {
                    if (isAd) {
                        //No android recebemos a string completa da versão
                        if (vers.name().equals("ad_" + t)) {
                            appVersaoCache.put(idDev, vers);
                            return vers;
                        }
                    } else {
                        //No iOS recebemos o número inteiro da versão
                        if (Integer.valueOf(t.replaceAll("[^0-9]", "")) == vers.num) {
                            appVersaoCache.put(idDev, vers);
                            return vers;
                        }
                    }
                }
            }
        }
        appVersaoCache.put(idDev, VersionsAppSupported._unknown);

        return VersionsAppSupported._unknown;

    }

    public interface AuxAll {

        public Object run(Object... arg) throws Exception;
    }

    /**
     * Modos/Status possiveis para restaurantes/lojas do app
     *
     */
    public enum ModoRestaurante {

        NAO_HABILITADO(0), //indiponível para todos
        HABILITADO(1), //disponível para todos
        TESTES_LABORATORIO(2), //disponível para somente dispositivos laboratório
        RESTRITO_AUTORIZACAO(3), //disponível para todos, mas necessita da liberação para ser acessível
        GARCOM(4),//disponível para todos, mas com direito ao recurso de atendimento local habilitado
        GARCOM_TESTES(5);//disponível para somente dispositivos laboratório, mas com direito ao recurso de atendimento local habilitado
        public final int val;

        private ModoRestaurante(int val) {
            this.val = val;
        }

        public static ModoRestaurante identificaEnum(int val) {
            for (ModoRestaurante e : values()) {
                if (e.val == val) {
                    return e;
                }
            }
            return NAO_HABILITADO;
        }
    }

    /**
     * Versões suportadas/disponíveis do app para os SO.
     *
     * Manter o padrão de nomenclatura e documentação!
     */
    public enum VersionsAppSupported {

        io_1_6(7, false),// >= 2016-08-16 17:00
        io_1_6_1(8, false),// >= 2016-09-01 18:40
        io_1_6_2(9, false),// >= 2016-09-12 07:50
        io_1_6_3(10, false),// >= 2016-09-13 12:00
        io_1_6_4(11, false),// >= 2016-09-14 12:00
        io_1_6_7(14, false),// >= 2016-09-16 19:30
        io_1_6_8(15, false),// >= 2016-09-22 16:30
        io_1_6_9(16, false),// >= 2016-09-26 11:00
        io_1_7(17, false),// >= 2016-09-29 18:00
        io_1_7_1(18, false),// >= 2016-10-03 20:00
        io_1_7_2(19, false),// >= 2016-10-07 12:00
        io_1_7_3(20, false),// >= 2016-10-07 12:00
        io_1_7_4(21, false),// >= 2016-10-07 12:00
        io_1_7_5(22, false),// >= 2016-11-01 15:00
        io_1_7_6(23, false),// >= 2016-11-21 12:00 
        io_1_7_7(24, false),// >= ?
        io_1_7_8(25, false),// >= ?
        io_1_7_9(26, false),// >= ?
        io_1_8(27, false),// >= ?
        io_1_8_1(28, false),// >= ?
        io_1_8_2(29, false),// >= ?
        io_1_8_3(30, false),// >= ?
        io_1_8_4(31, false),// >= ?
        io_1_8_5(32, false),// >= ?
        io_1_8_6(33, false),// >= ?
        io_1_8_7(34, false),// >= ?
        io_1_8_8(35, false),// >= ?
        io_1_8_9(36, false),// >= ?
        io_1_9(37, false),// >= ?
        io_1_9_1(38, false),// >= ?
        io_1_9_2(39, false),// >= ?
        io_1_9_3(40, false),// >= ?
        io_1_9_4(41, false),// >= ?
        io_1_9_5(42, false),// >= ?
        io_1_9_6(43, false),// >= ?
        io_1_9_7(44, false),// >= ?
        io_1_9_8(45, false),// >= ?
        io_1_9_9(46, false),// >= ?
        io_2_0(47, false),// >= ?
        //
        ad_1_6_9(37, true),// >= 2016-08-09 15:30
        ad_1_7(38, true),// >= 2016-08-23 15:00
        ad_1_7_1(39, true),// >= 2016-08-23 15:00
        ad_1_7_2(40, true),// >= 2016-09-01 07:00
        ad_1_7_3(41, true),// >= 2016-09-05 16:15
        ad_1_7_4(42, true),// >= 2016-09-13 12:00
        ad_1_7_5(43, true),// >= 2016-09-20 11:00
        ad_1_7_6(44, true),// >= 2016-09-20 20:00
        ad_1_7_7(45, true),// >= 2016-09-29 15:00
        ad_1_7_8(46, true),// >= 2016-10-03 13:00
        ad_1_7_9(47, true),// >= 2016-10-03 18:00
        ad_1_8(48, true),// >= 2016-10-04 08:30
        ad_1_8_1(49, true),// >= 2016-10-06 16:00
        ad_1_8_2(50, true),// >= 2016-10-19 11:00
        ad_1_8_3(51, true),// >= 2016-10-21 11:00
        ad_1_8_4(52, true),// >= 2016-10-26 12:00
        ad_1_8_5(53, true),// >= 2016-10-31 15:00
        ad_1_8_6(54, true),// >= 2016-11-03 17:00
        ad_1_8_7(55, true),// >= ?
        ad_1_8_8(56, true),// >= ?
        ad_1_8_9(57, true),// >= ?
        ad_1_9(58, true),// >= ?
        //-------------------------refatorada----------------------------
        ad_2_0(59, true),// >= 2016-10-18 09:00
        ad_2_0_1(60, true),// >= 2016-11-08 13:30
        ad_2_0_2(61, true),// >= 2016-11-?
        ad_2_0_3(62, true),// >= 2016-11-?
        ad_2_0_4(63, true),// >= 2016-11-?
        ad_2_0_5(64, true),// >= 2016-11-?
        ad_2_0_6(65, true),// >= 2016-11-23 09:00
        ad_2_0_7(66, true),// >= 2016-11-29 09:00
        ad_2_0_8(67, true),// >= 2016-11-30 09:00
        ad_2_0_9(68, true),// >= 2016-12-05 13:00
        ad_2_1(69, true),// >= 2016-12-06 09:00
        ad_2_1_1(70, true),// >= 20-12-07 08:00
        ad_2_1_2(71, true),// >= ?
        ad_2_1_3(72, true),// >= ?
        ad_2_1_4(73, true),// >= ?
        ad_2_1_5(74, true),// >= ?
        ad_2_1_6(75, true),// >= ?
        ad_2_1_7(76, true),// >= ?
        ad_2_1_8(77, true),// >= ?
        ad_2_1_9(78, true),// >= ?
        ad_2_2(79, true),// >= ?
        ad_2_2_1(80, true),// >= ?
        ad_2_2_2(81, true),// >= ?
        ad_2_2_3(82, true),// >= ?
        ad_2_2_4(83, true),// >= ?
        ad_2_2_5(84, true),// >= ?
        ad_2_2_6(85, true),// >= ?
        ad_2_2_7(86, true),// >= ?
        ad_2_2_8(87, true),// >= ?
        ad_2_2_9(88, true),// >= ?
        ad_2_3(89, true),// >= ?

        //eGULA Garçom
        ad_1_0_0g(1000, true),// >= ?
        ad_1_0_1g(1001, true),// >= ?
        ad_1_0_2g(1002, true),// >= ?
        ad_1_0_3g(1003, true),// >= ?
        ad_1_0_4g(1004, true),// >= ?
        ad_1_0_5g(1005, true),// >= ?
        //
        _unknown(-1, false);// >= 0000-00-00 00:00
        private final int num;
        private final boolean isAndroid;

        private VersionsAppSupported(int num, boolean isAndroid) {
            this.num = num;
            this.isAndroid = isAndroid;
        }

        /**
         * Retorna se a versão é maior ou igual a comparada
         *
         * @param b VersionsAppSupported
         * @return
         */
        public boolean maiorOuIgualA(VersionsAppSupported b) {
            if (this == _unknown || b == _unknown) {
                return false;
            }

            if (this.isAndroid == b.isAndroid) {
                return this.num >= b.num;
            }

            return false;
        }

        /**
         * Retorna se a versão é maior ou igual as comparadas. Usada em casos
         * onde é necessário comparar iphone e android
         *
         * @param b VersionsAppSupported
         * @param bb VersionsAppSupported
         * @return
         */
        public boolean maiorOuIgualA(VersionsAppSupported b, VersionsAppSupported bb) {
            if (this.maiorOuIgualA(b)) {
                return true;
            }

            return this.maiorOuIgualA(bb);
        }

        public String getVersionStr() {
            return this.name()
                    .replace("_", ".")
                    .replace("ad.", "")
                    .replace("io.", "");
        }

    }

}
