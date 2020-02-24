/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.sincronizacao;

import br.com.casaautomacao.casagold.classes.FrmModuloPaiBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;

/**
 * Sincronização de arquivos
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 20/12/2016 - 17:32:07
 */
public class CSPSincronizacaoArquivos extends CSPSincronizacao {

    private final String pathMonitorar;
    private final String remoteUserHostPath;
    private final String strRsync;
    private boolean sincronizando = false;

    /**
     * Sincronização
     *
     * @param idSync String - Id da sincronização. Já que é possível sincronizar
     * mais de um conteúdo por vez
     * @param remoteHost String - Host/ip do outro servidor com que a base será
     * sincronizada
     * @param isMaster boolean - Se a aplicação(esta) é o master
     * @param pathMonitorar CSPArquivos[] - Pastas a serem monitoradas
     */
    public CSPSincronizacaoArquivos(String idSync, String remoteHost, boolean isMaster, String pathMonitorar, String remoteUserHostPath) {
        super(idSync, remoteHost, isMaster);

        this.pathMonitorar = pathMonitorar.endsWith("/") ? pathMonitorar : pathMonitorar + "/";
        this.remoteUserHostPath = remoteUserHostPath.endsWith("/") ? remoteUserHostPath : remoteUserHostPath + "/";

        final StringBuilder rsync = new StringBuilder();
        rsync.append("rsync -ru ");
        rsync.append(this.pathMonitorar);
        rsync.append(" ");
        rsync.append(this.remoteUserHostPath);
        this.strRsync = rsync.toString();
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

            switch (info[0].toString()) {
                case "check-before-sync":
                    return !this.sincronizando;
            }

            return false;

        });

        FrmModuloPaiBase.simpleThread(() -> {

            /**
             * Dispara, conforme o intervalo, o processo de sincronização
             */
            this.syncPath();

        }, this.getIntervalSync());

    }

    /**
     * Valida e sincroniza o path
     *
     * @throws Exception
     */
    private synchronized void syncPath() throws Exception {

        final boolean check = this.sendInfo("check-before-sync");

        if (check) {

            this.sincronizando = true;

            CSPUtilidadesSO.runCommandInSo(this.strRsync);

            this.sincronizando = false;

        }
    }
}
