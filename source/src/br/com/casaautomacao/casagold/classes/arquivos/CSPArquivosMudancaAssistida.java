/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.LinkedHashSet;

/**
 * Classe para interagir com as mudanças em tempo real nos arquivos mapeados
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 26/01/2017 - 08:16:00
 */
public class CSPArquivosMudancaAssistida {

    final private LinkedHashSet<FileInfo> hashSetFiles = new LinkedHashSet<>();
    private OnUpdate onUpdate;

    /**
     * Adiciona arquivo/pasta para ser monitorado
     *
     * @param file CSPArquivos
     */
    public void add(CSPArquivos file) throws Exception {
        this.add(file, null);
    }

    /**
     * Adiciona arquivo/pasta para ser monitorado
     *
     * @param file CSPArquivos
     * @param parent FileInfo - Qual o diretorio pai deste
     */
    private FileInfo add(CSPArquivos file, FileInfo partent) throws Exception {

        final FileInfo fileInfo = new FileInfo(file.getAbsolutePath(), file.isFile(), file.getLastModified());

        fileInfo.parent = partent;
        fileInfo.children = null;

        if (!file.isFile()) {

            final CSPArquivos[] listFiles = file.listFiles();

            fileInfo.children = new FileInfo[listFiles.length];

            for (int i = 0; i < listFiles.length; i++) {

                fileInfo.children[i] = this.add(listFiles[i], fileInfo);

            }

        }

        this.hashSetFiles.add(fileInfo);

        return fileInfo;
    }

    /**
     * Adiciona arquivo/pasta para ser monitorado
     *
     * @param file String
     */
    public void add(String file) throws Exception {

        this.add(new CSPArquivos(file));
    }

    /**
     * Inicia o processo de controle dos arquivos
     *
     * @throws Exception
     */
    public void start() throws Exception {

        this.hashSetFiles.stream().filter((f) -> !(f.isFile)).forEachOrdered((f) -> {
            CSPUtilidadesLang.simpleThread(() -> {
                this.monitoraDiretorio(f);
            });
        });

    }

    /**
     * Monitora o diretório
     *
     * @throws Exception
     */
    private void monitoraDiretorio(FileInfo file) throws Exception {

        WatchService watcher = FileSystems.getDefault().newWatchService();
        Path dir = Paths.get(file.path);
        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);

        while (true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException ex) {
                return;
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path fileName = ev.context();

                if (kind == ENTRY_CREATE || kind == ENTRY_MODIFY) {
                    
                    FileInfo findFileInfo = this.updateAndGetFileInfo(fileName.toString(), file);
                   
                    if (!findFileInfo.isFile) {
                        
                        CSPUtilidadesLang.simpleThread(() -> {
                            this.monitoraDiretorio(findFileInfo);
                        });
                        
                    }
                    
                    this.callOnUpdate(findFileInfo,
                            kind == ENTRY_CREATE ? TypeUpdate.CREATE : TypeUpdate.MODIFY
                    );

                } else if (kind == ENTRY_DELETE) {

                    this.callOnUpdate(
                            this.getAndRemoveFileInfo(fileName.toString(), file),
                            TypeUpdate.DELETE
                    );

                }

            }

            boolean valid = key.reset();
            if (!valid) {
                break;
            }
        }

    }

    /**
     * Procura no set o arquivo, encontrando-o irá remover ele da lista, mas
     * antes irá retornar o antigo objeto
     *
     *
     * @return
     */
    private FileInfo getAndRemoveFileInfo(String name, FileInfo parent) throws Exception {

        final String path = parent.path + "/" + name;
        FileInfo r = null;
        for (FileInfo f : this.hashSetFiles) {

            if (f.path.equals(path)) {

                r = new FileInfo(f.path, f.isFile, f.lastChange);
                r.parent = f.parent;
                r.children = f.children;
                this.hashSetFiles.remove(f);
                break;

            }
        }
        return r;
    }

    /**
     * Procura no set o arquivo, encontrando-o irá atualizar suas informações.
     * Não encontrando irá verificar se existe e caso sim adicionará ao mapa as
     * informções.
     *
     * Em casos de não existir no mapa, e não existir o arquivo será retornado
     * null
     *
     *
     * @return
     */
    private FileInfo updateAndGetFileInfo(String name, FileInfo parent) throws Exception {

        final String path = parent.path + "/" + name;

        this.getAndRemoveFileInfo(name, parent);

        final CSPArquivos f = new CSPArquivos(path);

        if (!f.exists()) {

            return null;

        }

        return this.add(f, parent);
    }

    /**
     * Retorna a lista completa de arquivos e pastas mapeados
     *
     * @return
     */
    public LinkedHashSet<FileInfo> getFullList() {
        return this.hashSetFiles;
    }

    /**
     * Configura o evento disparado diante a uma atualização
     *
     * @param onUpdate OnUpdate
     */
    public void setOnUpdate(OnUpdate onUpdate) {
        this.onUpdate = onUpdate;
    }

    private void callOnUpdate(FileInfo f, TypeUpdate type) {
        try {
            this.onUpdate.run(f, type);
        } catch (Exception ex) {
            CSPException.register(ex);
        }

    }

    /**
     * Representa um arquivo/pasta mapeado
     */
    public class FileInfo {

        final public String path;
        final public boolean isFile;
        public long lastChange;
        public FileInfo parent;
        public FileInfo[] children;

        public FileInfo(String path, boolean isFile, long lastChange) {
            this.path = path;
            this.isFile = isFile;
            this.lastChange = lastChange;
        }

    }

    /**
     * Evento de mudança
     */
    public interface OnUpdate {

        public void run(FileInfo file, TypeUpdate type) throws Exception;
    }

    /**
     * Tipo de mudança
     */
    public enum TypeUpdate {
        CREATE,
        DELETE,
        MODIFY
    }
}
