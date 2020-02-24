/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Classe que implementa um mapa com a relação de caminhos e md5.
 *
 * A analisa, com base na última modificação, o md5 dos arquivos mantendo assim
 * um mapa com a relação caminhoXmd5 sempre atualizado
 *
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
@Deprecated
public class CSPArquivosMapeadosMd5 {

    private final Map<CSPArquivos, Date> relationFileXLastUpdate;
    private final Map<CSPArquivos, String> relationFileXLastMd5;
    private long lastUpdate = 0;
    private CSPArquivos[] lastGetListResult = new CSPArquivos[0];

    @Deprecated
    public CSPArquivosMapeadosMd5() {
        this.relationFileXLastUpdate = Collections.synchronizedMap(new LinkedHashMap<>());
        this.relationFileXLastMd5 = Collections.synchronizedMap(new LinkedHashMap<>());
    }

    /**
     * Efetua o 'reset' das informações cacheadas
     */
    @Deprecated
    public final void clearAll() {
        this.relationFileXLastMd5.clear();
        this.relationFileXLastMd5.clear();
    }

    /**
     * Alias para {@link #addFileToMonitore(br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos)
     * }
     */
    @Deprecated
    public void addFileToMonitore(String file) throws Exception {
        this.addFileToMonitore(new CSPArquivos(file));
    }

    /**
     * Adiciona um novo arquivo/pasta para o monitoramento. Quando adicionado
     * uma pasta será feito o processo recursivo para monitorar o conteúdo da
     * mesma
     *
     * @param file CSPArquivos
     */
    @Deprecated
    public void addFileToMonitore(CSPArquivos file) throws Exception {

        if (file != null && file.exists()) {

            if (file.isFile()) {

                this.updateInfosFile(file, null);

            } else {

                for (CSPArquivos f : file.listFiles()) {
                    this.addFileToMonitore(f);
                }

            }

        }

    }

    /**
     * Verifica e atualiza as informações sobre o arquivo nos mapas internos
     *
     * @param file
     */
    @Deprecated
    private void updateInfosFile(CSPArquivos file, Iterator<CSPArquivos> iLastUpdate) throws Exception {

        if (file.isFile()) {
            this.relationFileXLastMd5.put(file, file.getMd5());
            this.relationFileXLastUpdate.put(file, new Date(file.getLastModified()));
        } else if (iLastUpdate != null) {
            iLastUpdate.remove();
            this.relationFileXLastMd5.remove(file);
//            this.relationFileXLastUpdate.remove(file);
        }
    }

    /**
     * Realiza o update constante das informações
     *
     */
    @Deprecated
    private void updateInfos() throws Exception {
        final long now = new Date().getTime();
        if ((now - this.lastUpdate) <= 25000) {
            return;
        }

        this.lastUpdate = now;

        synchronized (this.relationFileXLastUpdate) {

            final Set<CSPArquivos> keySet = this.relationFileXLastUpdate.keySet();
            final Iterator<CSPArquivos> i = keySet.iterator();
            while (i.hasNext()) {

                CSPArquivos e = i.next();

                if (!e.isFile()) {
                    this.updateInfosFile(e, i);
                    continue;
                }

                if (e.getLastModified() != this.relationFileXLastUpdate.get(e).getTime()) {
                    this.updateInfosFile(e, i);
                }

            }

        }

    }

    @Deprecated
    public Date getFileLastUpdate(CSPArquivos file) throws Exception {
        this.updateInfos();
        synchronized (this.relationFileXLastUpdate) {
            if (!this.relationFileXLastUpdate.containsKey(file)) {
                this.updateInfosFile(file, null);
            }

            if (this.relationFileXLastUpdate.containsKey(file)) {
                return this.relationFileXLastUpdate.get(file);
            }

            return null;
        }
    }

    @Deprecated
    public String getFileLastMd5(CSPArquivos file) throws Exception {
        this.updateInfos();
        synchronized (this.relationFileXLastMd5) {
            if (!this.relationFileXLastMd5.containsKey(file)) {
                this.updateInfosFile(file, null);
            }

            if (this.relationFileXLastMd5.containsKey(file)) {
                return this.relationFileXLastMd5.get(file);
            }

            return null;
        }
    }

    @Deprecated
    public synchronized CSPArquivos[] getFiles() throws Exception {
        final long now = new Date().getTime();
        if ((now - this.lastUpdate) <= 25000) {
            return this.lastGetListResult;
        }

        this.updateInfos();

        synchronized (this.relationFileXLastUpdate) {
            final Set<CSPArquivos> keySet = this.relationFileXLastUpdate.keySet();
            final Iterator<CSPArquivos> i = keySet.iterator();
            this.lastGetListResult = new CSPArquivos[this.relationFileXLastUpdate.size()];
            int count = 0;

            while (i.hasNext()) {
                this.lastGetListResult[count] = i.next();
                count++;
            }

            return this.lastGetListResult;
        }
    }

    @Deprecated
    public synchronized Map<CSPArquivos, String> getFilesXLastMd5() throws Exception {

        this.updateInfos();

        return this.relationFileXLastMd5;

    }

}
