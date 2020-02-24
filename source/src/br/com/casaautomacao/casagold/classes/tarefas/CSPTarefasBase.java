/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.tarefas;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.exceptions.UnrecognizedTaskException;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao;
import static br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao.getHostByHostBuilder;
import static br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas.SERVER;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe para registrar/gerenciar pendências/tarefas efetuadas a um servidor de
 * pendências/tarefas.
 *
 * Na maioria dos casos a pendencia se trata de uma "solicitação" do
 * retaguarda/pdv para o monitor.
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 24/02/2016 - 17:52:23
 */
public abstract class CSPTarefasBase {

    /**
     * Registra uma nova tarefa
     *
     * @param grupoStr String - grupo/categoria da tarefa
     * @param parametros String... - Parâmetros da tarefa
     * @return String - Código da tarefa criada
     * @throws java.lang.Exception
     */
    protected static String novaTarefa(String grupoStr, CSPServidorTarefas.ServerSupport servDest, String... parametros) throws Exception {
        return novaTarefa(grupoStr, servDest, true, parametros);
    }

    /**
     * Registra uma nova tarefa
     *
     * @param grupoStr String - grupo/categoria da tarefa
     * @param saveOnFail boolean - Se deve ser registrado que a tarefa falhou
     * para tentar novamente mais tarde
     * @param parametros String... - Parâmetros da tarefa
     * @return String - Código da tarefa criada
     * @throws java.lang.Exception
     */
    protected static String novaTarefa(String grupoStr, CSPServidorTarefas.ServerSupport servDest, boolean saveOnFail, String... parametros) throws Exception {
        return novaTarefaOtherHost(grupoStr, servDest, getHostByHostBuilder(servDest.servico.hostType, servDest.servico.port, null, 0), saveOnFail, parametros);
    }

    /**
     * Registra uma nova tarefa
     *
     * @param grupoStr String - grupo/categoria da tarefa
     * @param host String - Servidor que irá atender
     * @param parametros String... - Parâmetros da tarefa
     * @return String - Código da tarefa criada
     * @throws java.lang.Exception
     */
    protected static String novaTarefaOtherHost(String grupoStr, CSPServidorTarefas.ServerSupport servDest, String host, String... parametros) throws Exception {
        return novaTarefaOtherHost(grupoStr, servDest, host, true, parametros);
    }

    /**
     * Registra uma nova tarefa
     *
     * @param grupoStr String - grupo/categoria da tarefa
     * @param servDest
     * @param host String - Servidor que irá atender
     * @param saveOnFail boolean - Se deve ser registrado que a tarefa falhou
     * para tentar novamente mais tarde
     * @param parametros String... - Parâmetros da tarefa
     * @return String - Código da tarefa criada
     * @throws java.lang.Exception
     */
    protected static String novaTarefaOtherHost(String grupoStr, CSPServidorTarefas.ServerSupport servDest, String host, boolean saveOnFail, String... parametros) throws Exception {

        final String[] parametrosAux = parametros == null ? new String[0] : parametros;

        final JSONObject re = new CSPComunicacao().comunicaOtherHost(servDest.servico, (String host1, int port) -> true, host, new JSONObject() {
            {
                put("ACAO", "new-tarefa");
                put("GRUPO", grupoStr);
                put("PARAMETROS", new ArrayList<>(Arrays.asList(parametrosAux)));
            }
        });

        if (re != null) {

            final String status = CSPUtilidadesLangJson.getFromJson(re, "STATUS", "no");

            if (!status.equals("no")) {

                if (status.equals("unrecognized")) {
                    final String servStr = CSPUtilidadesLangJson.getFromJson(re, "UNRECOGNIZED_AUTHOR", servDest.toString());
                    for (CSPServidorTarefas.ServerSupport s : CSPServidorTarefas.ServerSupport.values()) {
                        if (s.toString().equals(servStr)) {

                            throw new UnrecognizedTaskException(s);
                        }
                    }

                    throw new UnrecognizedTaskException(servDest);
                }

                if (re.has("CODIGO_TAREFA")) {
                    return re.getString("CODIGO_TAREFA");
                }

            }

        }

        if (saveOnFail) {
            /**
             * Se caiu aqui é porque deu alguma merda. Então nesse momento
             * jogamos a tarefa para dentro de um json da vida
             */
            CSPArquivosLocais t = new CSPArquivosLocais(PATH + "/pendencias/enviadas/" + SERVER.name().toLowerCase() + ".txt");
            t.appendContent(new JSONObject() {
                {
                    put("GRUPO", grupoStr);
                    put("HOST", host);
                    put("DATA_HORA", CSPUtilidadesLangDateTime.getTempoCompleto());
                    put("PARAMETROS", new ArrayList<>(Arrays.asList(parametrosAux)));
                }
            }.toString() + CSPUtilidadesSO.LINE_SEPARATOR);
        }

        return null;
    }

    /**
     * Verifica se existe tarefas não enviadas e caso exista o mesmo tenta
     * envia-las novamente
     *
     * @throws java.lang.Exception
     */
    public static void reveTarefasPendentes() throws Exception {
        CSPArquivosLocais old = new CSPArquivosLocais(PATH + "/pendencias/enviadas/" + SERVER.name().toLowerCase() + ".txt");
//        JSONArray arr = old.getArray();
//        if (arr == null) {
//            return;
//        }
        if (old.getContent() == null || old.getContent().trim().isEmpty()) {
            return;
        }

        String[] arr = old.getContent().split(CSPUtilidadesSO.LINE_SEPARATOR);
        StringBuilder sb = new StringBuilder();
        JSONObject obj;

        for (String arr1 : arr) {
            obj = new JSONObject(arr1);

            ArrayList<String> fa = new ArrayList<>();
            for (Object a : obj.getJSONArray("PARAMETROS")) {
                fa.add(a.toString());
            }

            final String gr = obj.getString("GRUPO");
            final CSPServidorTarefas.AuxInfosGrupos infoGr = CSPServidorTarefas.identificaInfosGrupo(gr);
            String[] prs = Arrays.copyOf(fa.toArray(), fa.toArray().length, String[].class);

            if (novaTarefaOtherHost(gr, infoGr.serv, CSPUtilidadesLangJson.getFromJson(obj, "HOST", "localhost"), false, prs) == null) {
                sb.append(obj.toString());
                sb.append(CSPUtilidadesSO.LINE_SEPARATOR);
            }
        }

        old.setContent(sb.toString());
    }

    /**
     * Retorna a resposta devolvida pelo servidor que executou a tarefa.
     *
     * @param grupo Grupos - Grupo da tarefa
     * @param id String - ID da tarefa gerado pelo servidor
     * @return Null em caso de não existir ou não ter sido executada
     * @throws java.lang.Exception
     */
    protected static String[] getResultTarefa(String grupoStr, CSPServidorTarefas.ServerSupport servDest, String id) throws Exception {
        return getResultTarefa(grupoStr, servDest, getHostByHostBuilder(servDest.servico.hostType, servDest.servico.port, null, 0), id);
    }

    /**
     * Retorna a resposta devolvida pelo servidor que executou a tarefa. Null em
     * caso de não existir ou não ter sido executada
     *
     * @param grupo Grupos - Grupo da tarefa
     * @param host String - Host onde foi executada a tarefa
     * @param id String - ID da tarefa gerado pelo servidor
     * @return
     */
    protected static String[] getResultTarefa(String grupoStr, CSPServidorTarefas.ServerSupport servDest, String host, String id) throws Exception {
        final JSONObject re = new CSPComunicacao().comunicaOtherHost(servDest.servico, (String host1, int port) -> true, host, new JSONObject() {
            {
                put("ACAO", "get-response-tarefa");
                put("GRUPO", grupoStr);
                put("CODIGO_TAREFA", id);
            }
        });

        if (re != null) {
            final JSONArray a = CSPUtilidadesLangJson.getFromJson(re, "RESPOSTA_TAREFA", (JSONArray) null);

            if (a != null) {
                final ArrayList<String> tmp = new ArrayList<>();

                for (Object j : a) {
                    if (j != null) {
                        tmp.add(j.toString());
                    }
                }

                return tmp.toArray(new String[tmp.size()]);
            }
        }

        return null;
    }
}
