/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.compartilhamento.CSPServidorCompartilhamento;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.tarefas.CSPServidorTarefas;
import br.com.casaautomacao.casagold.classes.tarefas.InterfaceServidorTarefaAcao;
import java.util.ArrayList;
import org.json.JSONArray;

/**
 * Classe de utilidades relacionadas ao CMG e CAPP
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 30/09/2016 - 08:23:02
 */
public abstract class CSPUtilidadesCentrais extends CSPUtilidadesApplication {

    /**
     * Monta as tarefas auxiliares para o processamento de logs
     *
     * @param tarefaListStr String - Tarefa que deverá ser responsável por
     * preparar/fornecer os logs quando necessário
     * @param tarefaDeleteStr String - Tarefa que deverá ser responsável por
     * excluir os arquivos já processados
     * @param serverFile CSPServidorCompartilhamento - Objeto do servidor de
     * arquivos
     * @throws Exception
     */
    public static void tarefasAuxiliaresProcessamentoLogs(String tarefaListStr, String tarefaDeleteStr, CSPServidorCompartilhamento serverFile) throws Exception {
        final String logsCmg = CSPUtilidadesSO.getValSharedGold("CMG_PATH") + "/logs";
        final String logsCapp = CSPUtilidadesSO.getValSharedGold("CAPP_PATH") + "/logs";

        CSPServidorTarefas.registerModule(tarefaListStr, (String cod, String[] parametros, InterfaceServidorTarefaAcao acao) -> {

            final long maxSize = Long.parseLong(parametros[0]);
            final boolean force = Boolean.parseBoolean(parametros[1]);
            final boolean isCapp = parametros.length >= 3 ? Boolean.parseBoolean(parametros[2]) : false;
            final CSPArquivos[] lista = new CSPArquivos((isCapp ? logsCapp : logsCmg))
                    .listFiles((String dir, String name, boolean isFile) -> isFile && name.endsWith(".log"));
            final ArrayList<CSPArquivos> zipList = new ArrayList<>();
            boolean processLastLog = lista.length == 1;

            if (lista.length > 1) {

                //LOGs ANTIGOS
                for (int i = 0; i < lista.length - 1; i++) {
                    zipList.add(lista[i]);
                }

                processLastLog = true;

            }

            if (processLastLog) {
                //UNICO/ULTIMO LOG
                CSPArquivos last = lista[lista.length - 1];

                if (force) {
                    zipList.add(last);
                } else if (maxSize <= last.length() / 1000) {
                    zipList.add(last);
                }
            }

            if (zipList.size() > 0) {

                final CSPArquivos file = serverFile.preparaArquivoDownload(true, zipList.toArray(new CSPArquivos[zipList.size()]));

                acao.resposta(file.getName());

            } else {

                acao.resposta("no");

            }

            acao.finalizaAll();
        });

        CSPServidorTarefas.registerModule(tarefaDeleteStr, (String cod, String[] parametros, InterfaceServidorTarefaAcao acao) -> {

            final JSONArray list = new JSONArray(parametros[0]);
            final CSPArquivos f = new CSPArquivos();
            final boolean isCapp = parametros.length >= 2 ? Boolean.parseBoolean(parametros[1]) : false;

            for (Object l : list) {
                f.setPath((isCapp ? logsCapp : logsCmg) + "/" + l);
                f.delete();
            }

            acao.finalizaAll();
        });
    }

}
