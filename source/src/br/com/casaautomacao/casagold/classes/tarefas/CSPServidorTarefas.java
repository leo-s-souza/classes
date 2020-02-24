/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.FrmModuloPaiBase;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosJson;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.exceptions.UnrecognizedTaskException;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPServidorComunicacao;
import br.com.casaautomacao.casagold.classes.tarefas.grupos.ToCapp;
import br.com.casaautomacao.casagold.classes.tarefas.grupos.ToCmg;
import br.com.casaautomacao.casagold.classes.tarefas.grupos.ToMg;
import br.com.casaautomacao.casagold.classes.tarefas.grupos.ToPdv;
import br.com.casaautomacao.casagold.classes.tarefas.grupos.ToRet;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe para receber e 'alocar' nos processos as tarefas recebidas.
 *
 *
 * Cada módulo/formulário registra na classe um evento que será disparado toda
 * vez que uma nova pendencia/tarefa for registrada para determinado 'grupo',
 * determinados pelo enum correspondente
 *
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 24/02/2016 - 17:52:46
 */
public class CSPServidorTarefas {

    protected static ServerSupport SERVER;
    private static CSPArquivosJson STORAGE_TASKS_OKS;
    private static CSPArquivosJson STORAGE_TASKS;
    private static final HashMap<String, InterfaceServidorTarefa> MODULOS_REGISTRADOS = new HashMap<>();
    private static final HashMap<String, ArrayList<TarefaPendente>> TAREFAS_PENDENTES = new HashMap<>();

    /**
     * Servidor de Tarefas
     *
     * @param serv ServerSupport - De qual servidor se trata
     * @throws Exception
     */
    public CSPServidorTarefas(ServerSupport serv) throws Exception {
        SERVER = serv;
        FrmModuloPaiBase.simpleThread(() -> {
            CSPTarefasBase.reveTarefasPendentes();
        });

        STORAGE_TASKS = new CSPArquivosJson(PATH + "/pendencias/" + SERVER.name().toLowerCase() + ".json");
        STORAGE_TASKS_OKS = new CSPArquivosJson(PATH + "/pendencias/finalizadas/" + SERVER.name().toLowerCase() + ".json");
    }

    /**
     * Define a resposta para a tarefa. Para finalizar a tarefa utilize o {@link #finalizaTarefa(br.com.casaautomacao.casagold.classes.tarefas.CSPTarefas.Grupos, java.lang.String)
     * }!
     *
     * @param grupoStr String - Grupo da tarefa
     * @param codigo int - Código da tarefa
     * @param resonse String[] - Resposta
     * @throws java.lang.Exception
     */
    private static void setRespostaTarefa(String grupoStr, String codigo, String... resonse) throws Exception {
        final JSONObject fo;

        {//bugfix
            JSONObject tmp = STORAGE_TASKS_OKS.getObject();
            if (tmp == null) {
                fo = new JSONObject();
            } else {
                fo = tmp;
            }
        }

        JSONObject foo = new JSONObject();

        if (fo.has(grupoStr) && !fo.isNull(grupoStr)) {
            foo = fo.getJSONObject(grupoStr);
        }

        foo.put(codigo, new JSONArray(resonse));

        fo.put(grupoStr, foo);
        STORAGE_TASKS_OKS.setObject(fo);

    }

    /**
     * Finaliza uma tarefa
     *
     * @param grupoStr String - Grupo da tarefa
     * @param codigo int - Código da tarefa
     * @throws java.lang.Exception
     */
    private static void finalizaTarefa(String grupoStr, String codigo) throws Exception {

        if (STORAGE_TASKS.exists()) {
            final JSONObject fo;

            {//bugfix
                JSONObject tmp = STORAGE_TASKS.getObject();
                if (tmp == null) {
                    fo = new JSONObject();
                } else {
                    fo = tmp;
                }
            }

            if (fo.has(grupoStr) && !fo.isNull(grupoStr)) {
                JSONArray fa = fo.getJSONArray(grupoStr);

                for (int i = 0; i < fa.length(); i++) {
                    JSONArray ff = fa.getJSONArray(i);

                    if (ff != null && ff.length() > 0) {
                        if (ff.get(0).equals(codigo)) {
                            fa.remove(i);
                            break;
                        }
                    }
                }

                fo.put(grupoStr, fa);
                STORAGE_TASKS.setObject(fo);
            }
        }
    }

    /**
     * Finaliza um grupo de tarefas.
     *
     * @param grupoStr String - Grupo da tarefa
     * @throws java.lang.Exception
     */
    public static void finalizaTarefaGrupo(String grupoStr) throws Exception {
        if (STORAGE_TASKS.exists()) {
            final JSONObject fo;

            {//bugfix
                JSONObject tmp = STORAGE_TASKS.getObject();
                if (tmp == null) {
                    fo = new JSONObject();
                } else {
                    fo = tmp;
                }
            }

            if (fo.has(grupoStr) && !fo.isNull(grupoStr)) {
                if (fo.length() > 1) {
                    fo.remove(grupoStr);
                    STORAGE_TASKS.setObject(fo);
                } else {
                    STORAGE_TASKS.setObject(new JSONObject("{" + grupoStr + ":[]}"));
                }
            }
        }
    }

    /**
     * Carrega as tarefas pendentes diretamente do json responsável pelo backup
     *
     * @throws java.lang.Exception
     */
    private void loadTarefasPendentes() throws Exception {
        if (STORAGE_TASKS.exists()) {
            final JSONObject fo;

            {//bugfix
                JSONObject tmp = STORAGE_TASKS.getObject();
                if (tmp == null) {
                    fo = new JSONObject();
                } else {
                    fo = tmp;
                }
            }
            for (Object n : fo.names()) {
                final String gr = (String) n;
                final AuxInfosGrupos infoGr = CSPServidorTarefas.identificaInfosGrupo(gr);
                if (gr != null) {
                    if (fo.has((String) n) && !fo.isNull((String) n)) {
                        for (Object f : fo.getJSONArray((String) n)) {
                            ArrayList<String> fa = new ArrayList<>();
                            for (Object a : (JSONArray) f) {
                                fa.add(a.toString());
                            }
                            String id = fa.get(0);
                            fa.remove(0);

                            this.addTarefaPendente(gr, null, infoGr.isAsync, id, Arrays.copyOf(fa.toArray(), fa.toArray().length, String[].class), false);
                        }
                    }
                }
            }

        }
    }

    /**
     * Adiciona uma tarefa a lista de pendentes
     *
     * @throws java.lang.Exception
     */
    private void addTarefaPendente(String grupoStr, String hostOrigem, boolean isAsync, String codigo, String[] parametros, boolean saveInFile) throws Exception {
        if (hostOrigem == null) {
            hostOrigem = "localhost";
        }

        grupoStr = grupoStr.toUpperCase();

        ArrayList<TarefaPendente> get = TAREFAS_PENDENTES.get(grupoStr);
        if (get == null) {
            get = new ArrayList<>();
        }

        get.add(new TarefaPendente(parametros, codigo));

        TAREFAS_PENDENTES.put(grupoStr, get);
        if (saveInFile) {
            JSONObject fo = null;
            if (STORAGE_TASKS.exists()) {
                try {
                    /**
                     * Em determinados momentos o sistema pode g
                     */
                    fo = STORAGE_TASKS.getObject();
                } catch (Exception ex) {
                    CSPException.register(ex);
                }
            }
            if (fo == null) {
                fo = new JSONObject();
            }
            JSONArray fa = new JSONArray();
            if (fo.has(grupoStr) && !fo.isNull(grupoStr)) {
                fa = fo.getJSONArray(grupoStr);
            }
            ArrayList<String> arr = new ArrayList<>();
            arr.add(codigo + "-" + hostOrigem);
            arr.addAll(Arrays.asList(parametros));
            fa.put(arr);
            fo.put(grupoStr, fa);
            STORAGE_TASKS.setObject(fo);
        }

        InterfaceServidorTarefa mod = MODULOS_REGISTRADOS.get(grupoStr);

        if (mod == null) {
            CSPLog.error(CSPServidorTarefas.class, grupoStr + " não será atendido imediatamente!");
        } else {
            if (isAsync) {
                final String ho = hostOrigem;
                final String gr = grupoStr;
                FrmModuloPaiBase.simpleThread(() -> {
                    callAoReceber(mod, gr, codigo + "-" + ho, parametros);
                });
            } else {
                callAoReceber(mod, grupoStr, codigo + "-" + hostOrigem, parametros);
            }
        }
    }

    /**
     * Remove o registro de um módulo
     *
     * @param grupoStr String - Grupo a ser removido
     * @param removeTasks boolean - Se deve ou não remover as tarefas
     *
     * @throws java.lang.Exception
     */
    public static void unregisterModule(String grupoStr, boolean removeTasks) throws Exception {
        grupoStr = grupoStr.toUpperCase();
        if (MODULOS_REGISTRADOS.containsKey(grupoStr)) {
            if (removeTasks) {
                TAREFAS_PENDENTES.put(grupoStr, new ArrayList<>());
            }
            MODULOS_REGISTRADOS.remove(grupoStr);
        }
    }

    /**
     * Registra um módulo para realizar determinada ação sempre que uma tarefa
     * for iniciada/concluída
     *
     * @param grupoStr String
     * @param ie InterfaceServidorTarefa
     */
    public static void registerModule(String grupoStr, InterfaceServidorTarefa ie) {
        grupoStr = grupoStr.toUpperCase();
        if (MODULOS_REGISTRADOS.containsKey(grupoStr)) {
            ie = null;
            return;
        }

        MODULOS_REGISTRADOS.put(grupoStr, ie);

        CSPLog.info(CSPServidorTarefas.class, grupoStr + " registrado!");

        ArrayList<TarefaPendente> get = TAREFAS_PENDENTES.get(grupoStr);
        if (get == null) {
            get = new ArrayList<>();
        }

        InterfaceServidorTarefa mod = MODULOS_REGISTRADOS.get(grupoStr);

        if (mod != null) {

            for (TarefaPendente t : get) {

                /**
                 * Ele deve executar as tarefas uma a uma, pois as mesmas foram
                 * agendadas antes do módulo estar registrado.
                 *
                 * Executar em threads pode acarretar em trabalho duplicado.
                 *
                 * Outro ponto é que o evento precisa estar dentro de um try
                 * catch para que uma tarefa não afete de forma negativa as
                 * demais
                 */
                try {
                    callAoReceber(mod, grupoStr, t.getCodigo(), t.getParamentros());
                } catch (Exception ex) {
                    CSPException.register(ex);
                }
            }
        }
    }

    /**
     * Auxiliar para o recebimento de tarefas
     */
    private static void callAoReceber(InterfaceServidorTarefa mod, String grupoStr, String cod, String... parametros) throws Exception {
        mod.aoReceber(cod, parametros, new InterfaceServidorTarefaAcao() {

            @Override
            public void finaliza() throws Exception {
                CSPServidorTarefas.finalizaTarefa(grupoStr, cod);
            }

            @Override
            public void finalizaAll() throws Exception {
                CSPServidorTarefas.finalizaTarefaGrupo(grupoStr);
            }

            @Override
            public void resposta(String... response) throws Exception {
                CSPServidorTarefas.setRespostaTarefa(grupoStr, cod, response);
            }

            @Override
            public String getHostOrigem() throws Exception {
                return getHostFromCodTarefa(cod);
            }

            @Override
            public void logError(String ms) {
                CSPLog.error("server-task>" + ms);
            }

            @Override
            public void logInfo(String ms) {
                CSPLog.info("server-task>" + ms);
            }

        });
    }

    /**
     * Retorna o host da estação que enviou a tarefa.
     *
     * @param cod String - Codigo da tarefa
     * @return Retorna o host, ou se não for possível, retorna "localhost"
     */
    private static String getHostFromCodTarefa(String cod) {
        try {
            if (cod.contains("-")) {
                return cod.split("-")[1];
            }
        } catch (Exception e) {
            CSPException.register(e);
        }

        return "localhost";
    }

    /**
     * Cria uma nova tarefa pendente.
     *
     * @param grupoStr
     * @param hostOrigem
     * @param isAsync
     * @param parametros
     * @return
     * @throws Exception
     */
    private String newTarefaPendente(String grupoStr, String hostOrigem, boolean isAsync, String[] parametros) throws Exception {
        String id = CSPUtilidadesLangDateTime.getTempoCompletoLimpo();
        this.addTarefaPendente(grupoStr, hostOrigem, isAsync, id, parametros, true);

        return id + "-" + hostOrigem;
    }

    /**
     * Inicia o servidor de tarefas
     *
     * @throws Exception
     */
    public void startServer() throws Exception {
        FrmModuloPaiBase.simpleThread(() -> {
            loadTarefasPendentes();
        });

        new CSPServidorComunicacao().recebeClient(SERVER.servico, (Socket sc, JSONObject input) -> true, (Socket sc, JSONObject info) -> {
            JSONObject r = new JSONObject();
            if (info.has("GRUPO") && !info.isNull("GRUPO")) {
                final String grupoStr = info.getString("GRUPO");
                final AuxInfosGrupos infoGr = identificaInfosGrupo(grupoStr);
                r.put("STATUS", "no");

                if (infoGr == null) {
                    r.put("STATUS", "unrecognized");
                    r.put("UNRECOGNIZED_AUTHOR", SERVER.toString());
                } else {

                    if (grupoStr != null) {
                        switch (CSPUtilidadesLangJson.getFromJson(info, "ACAO", "-")) {
                            case "new-tarefa":
                                if (info.has("PARAMETROS") && !info.isNull("PARAMETROS")) {
                                    JSONArray jsonArray = info.getJSONArray("PARAMETROS");
                                    ArrayList<String> tmp = new ArrayList<>();
                                    for (Object j : jsonArray) {
                                        if (j != null) {
                                            tmp.add(j.toString());
                                        }
                                    }
                                    String hostOrigem = sc.getRemoteSocketAddress().toString().replace("/", "").split(":")[0];

                                    try {
                                        r.put("CODIGO_TAREFA", newTarefaPendente(grupoStr, hostOrigem, infoGr.isAsync, tmp.toArray(new String[tmp.size()])));
                                        r.put("STATUS", "ok");
                                    } catch (UnrecognizedTaskException ex) {
                                        r.put("STATUS", "unrecognized");
                                        r.put("UNRECOGNIZED_AUTHOR", ex.server.toString());
                                    }
                                }
                                break;
                            case "get-response-tarefa":
                                //Retorna a resposta da tarefa
                                final String id = CSPUtilidadesLangJson.getFromJson(info, "CODIGO_TAREFA", (String) null);
                                if (id != null && STORAGE_TASKS_OKS.getContent() != null && !STORAGE_TASKS_OKS.getContent().trim().isEmpty()) {
                                    final JSONObject fo;
                                    {//bugfix
                                        JSONObject tmp = STORAGE_TASKS_OKS.getObject();
                                        if (tmp == null) {
                                            fo = new JSONObject();
                                        } else {
                                            fo = tmp;
                                        }
                                    }

                                    if (fo.has(grupoStr) && !fo.isNull(grupoStr)) {
                                        final JSONObject foo = fo.getJSONObject(grupoStr);
                                        r.put("STATUS", "ok");
                                        r.put("RESPOSTA_TAREFA", CSPUtilidadesLangJson.getFromJson(foo, id, (JSONArray) null));
                                    }
                                }

                                break;

                        }

                    }
                }
            }

            return r;
        });
    }

    protected static AuxInfosGrupos identificaInfosGrupo(String gr) {

        if (gr == null || gr.trim().isEmpty()) {
            return null;
        }

        if (gr.startsWith("TO_MG_")) {
            for (ToMg t : ToMg.values()) {
                if (t.toString().equals(gr)) {
                    return new AuxInfosGrupos(t.serv, t.isAsync);
                }
            }
        } else if (gr.startsWith("TO_CMG_")) {
            for (ToCmg t : ToCmg.values()) {
                if (t.toString().equals(gr)) {
                    return new AuxInfosGrupos(t.serv, t.isAsync);
                }
            }
        } else if (gr.startsWith("TO_CAPP_")) {
            for (ToCapp t : ToCapp.values()) {
                if (t.toString().equals(gr)) {
                    return new AuxInfosGrupos(t.serv, t.isAsync);
                }
            }
        } else if (gr.startsWith("TO_RET_")) {
            for (ToRet t : ToRet.values()) {
                if (t.toString().equals(gr)) {
                    return new AuxInfosGrupos(t.serv, t.isAsync);
                }
            }
        } else if (gr.startsWith("TO_PDV_")) {
            for (ToPdv t : ToPdv.values()) {
                if (t.toString().equals(gr)) {
                    return new AuxInfosGrupos(t.serv, t.isAsync);
                }
            }
        }

        return null;
    }

    public enum ServerSupport {

        CMG(CSPComunicacao.Servico.CTASK_CMG),
        CAPP(CSPComunicacao.Servico.CTASK_CAPP),
        PDV(CSPComunicacao.Servico.CTASK_PDV),
        RET(CSPComunicacao.Servico.CTASK_RET),
        MG(CSPComunicacao.Servico.CTASK_MG);
        public final CSPComunicacao.Servico servico;

        ServerSupport(CSPComunicacao.Servico servico) {
            this.servico = servico;
        }

    }

    public static class AuxInfosGrupos {

        final public CSPServidorTarefas.ServerSupport serv;
        final public boolean isAsync;

        public AuxInfosGrupos(ServerSupport serv, boolean isAsync) {
            this.serv = serv;
            this.isAsync = isAsync;
        }

    }

    private class TarefaPendente {

        private final String codigo;
        private final String[] paramentros;

        public TarefaPendente(String[] paramentros, String codigo) {
            this.paramentros = paramentros;
            this.codigo = codigo;
        }

        public String getCodigo() {
            return codigo;
        }

        public String[] getParamentros() {
            return paramentros;
        }

    }

}
