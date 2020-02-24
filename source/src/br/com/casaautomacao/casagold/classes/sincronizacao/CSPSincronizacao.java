/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.sincronizacao;

import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;
import br.com.casaautomacao.casagold.classes.tarefas.CSPTarefasBase;
import br.com.casaautomacao.casagold.classes.tarefas.InterfaceServidorTarefaAcao;
import static br.com.casaautomacao.casagold.classes.tarefas.grupos.ToMg.TO_MG_AUX_MG_SYNC_API;
import java.util.LinkedHashMap;
import org.json.JSONArray;

/**
 * Classe base para a sincronização entre máquinas.
 *
 * O processo de sincronização baseia-se na ideia de um host como master e outro
 * como slave. Ou seja, sempre sera necessario um host como master e sempre o
 * seu conteúdo terá prioridade.
 *
 * Para ambos sempre será a mesma classe, pois dessa forma fica um canal de mão
 * dupla, facilitando o processo.
 *
 * Em caso de conflitos de informações sempre o master tem prioridade
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 20/12/2016 - 17:26:14
 */
public abstract class CSPSincronizacao {

    /**
     * Tarefa que usamos para sincronizar
     */
    final static private LinkedHashMap<String, OnReceiveInfo> relacaoReceive = new LinkedHashMap<>();

    final private String idSync;
    final private String remoteHost;
    final private boolean isMaster;
//    final private int intervalSync = 300000;//5min
    private int intervalSync = 30000;//30seg
    private String tarefaSync = TO_MG_AUX_MG_SYNC_API.toString();
    private CSPServidorTarefas.ServerSupport tarefaSyncServer = CSPServidorTarefas.ServerSupport.MG;

    /**
     * Sincronização
     *
     * @param idSync String - Id da sincronização. Já que é possível sincronizar
     * mais de um conteúdo por vez
     * @param remoteHost String - Host remoto com que esta sendo trabalhado
     * @param isMaster boolean - Se a aplicação(esta) é o master
     */
    public CSPSincronizacao(String idSync, String remoteHost, boolean isMaster) {
        this.remoteHost = remoteHost;
        this.isMaster = isMaster;
        this.idSync = idSync + "--" + (this.isMaster ? "m" : "s");
    }

    /**
     * Inicia o processo
     *
     * @throws Exception
     */
    public void start() throws Exception {
        CSPServidorTarefas.finalizaTarefaGrupo(this.tarefaSync);
        /**
         * Direcionamos cada mensagem ao seu evento
         */
        CSPServidorTarefas.registerModule(this.tarefaSync,
                (String cod, String[] parametros, InterfaceServidorTarefaAcao acao) -> {
                    boolean responseIsOk = false;

                    try {
                        final OnReceiveInfo ev = relacaoReceive.get(parametros[0]);
                        if (ev != null) {
                            final JSONArray prs = new JSONArray(parametros[1]);
                            final Object[] prss = new Object[prs.length()];

                            for (int i = 0; i < prss.length; i++) {
                                prss[i] = prs.get(i);
                            }

                            responseIsOk = ev.run(
                                    prss
                            );
                        }

                    } catch (Exception ex) {
                        CSPException.register(ex);

                    }

                    acao.resposta(responseIsOk ? "ok" : "no");

                    acao.finaliza();
                }
        );
    }

    /**
     * Envia uma informação ao outro host
     *
     *
     * @param info Object[] - Conteúdo
     * @return Retornará a confirmação da mensagem pelo outro host. Quando o
     * outro host for o master será retornado se o mesmo aceitou o que a
     * mensagem solicitou, e quando for o slave será retornado se o mesmo
     * concluíu o que foi solicitado
     * @throws java.lang.Exception
     */
    protected boolean sendInfo(Object... info) throws Exception {
        final CSPTarefas task = new CSPTarefas();
        final String taskId = task.novaTarefa(new String[]{
            this.idSync.endsWith("--m")
            ? this.idSync.replace("--m", "--s")
            : this.idSync.replace("--s", "--m"),
            new JSONArray(
            info
            ).toString()
        });

        if (taskId == null) {
            return false;
        }

        final String[] resultTarefa = task.getResultTarefa(taskId);

        if (resultTarefa == null || resultTarefa.length == 0) {
            return false;
        }

        return "ok".equals(resultTarefa[0]);
    }

    /**
     * Configura o evento que será disparado ao receber uma mensagem do outro
     * host
     *
     * @param ev OnReceiveInfo - Evento
     */
    protected void setOnReceiveInfo(OnReceiveInfo ev) {
        relacaoReceive.put(idSync, ev);
    }

    /**
     * Retorna se é o host master da sincronização
     *
     * @return
     */
    protected boolean isMaster() {
        return isMaster;
    }

    /**
     * Intervalo de tempo para syncronização
     *
     * @return
     */
    protected final int getIntervalSync() {
        return intervalSync;
    }

    /**
     * Retorna o id sync atual
     *
     * @return
     */
    protected String getIdSync() {
        return idSync.replace("--s", "").replace("--m", "");
    }

    /**
     * Retorna o ip do outro host
     *
     * @return
     */
    public String getRemoteHost() {
        return remoteHost;
    }

    /**
     * Configura o intervalo em ms para a sincronização
     * @param intervalSync
     */
    final protected void setIntervalSync(int intervalSync) {
        this.intervalSync = intervalSync;
    }

    /**
     * Configura a tarefa e o servidor que será usado no processo
     *
     * @param tarefaSync
     * @param server
     */
    public void setTarefaSync(String tarefaSync, CSPServidorTarefas.ServerSupport server) {
        this.tarefaSync = tarefaSync;
        this.tarefaSyncServer = server;
    }

    protected interface OnReceiveInfo {

        /**
         * Evento
         *
         * @param info Object[] - Conteúdo
         * @return
         */
        public boolean run(Object... info) throws Exception;
    }

    private class CSPTarefas extends CSPTarefasBase {

        public String novaTarefa(String... parametros) throws Exception {
            return novaTarefaOtherHost(tarefaSync, tarefaSyncServer, remoteHost, false, parametros);
        }

        public String[] getResultTarefa(String id) throws Exception {
            return getResultTarefa(tarefaSync, tarefaSyncServer, remoteHost, id);
        }
    }
}
