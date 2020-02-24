/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangArquivos;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede.getLocalIp;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.StringJoiner;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

/**
 * Classe para manipulação de arquivos locais e remotos
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 17/08/2016 - 11:04:41
 */
public class CSPArquivos {

    private boolean isRemote;
    private File objFile;
    private SmbFile objSmbFile;
    private static Autenticacao defAutenticacao = null;
    private String encoding;

    public CSPArquivos() throws Exception {
        this("");
    }

    public CSPArquivos(String path) throws Exception {

        this.setPath(path);
        this.setEncoding("UTF-8");
    }

    public static void setDefAutenticacao(Autenticacao defAutenticacao) {
        CSPArquivos.defAutenticacao = defAutenticacao;
    }

    /**
     * Retorna qual é o formato padrão de arquivos da classe
     *
     * @return String - Caso seja * entende-se que é aceito todo o tipo
     */
    public String getFormatDefault() {
        return "*";
    }

    /**
     * Retorna se a classe está manipulando o arquivo remotamente
     *
     * @return
     */
    public boolean isRemote() {
        return isRemote;
    }

    private final void setIsRemote(boolean isRemote) {
        this.isRemote = isRemote;
    }

    /**
     * Define um caminho absoluno
     *
     * @param path String - Caminho absoluto
     * @throws java.lang.Exception
     */
    public final void setPath(String path) throws Exception {

        if (path == null) {
            path = "";
        }

        path = path.replace("\\", "/").trim();
        //Para não gerar problema é removida a última barra, caso exista
        if (path.length() > 0 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (path.startsWith("smb:")) {
            path = path.substring(4);// Em teoria vai ficar duas //
        }

        this.setIsRemote(path.startsWith("//"));

        if (this.isRemote()) {
            /**
             * Quando apontamos o caminho de um arquivo/pasta com o ip corremos
             * o risco de estarmos apontando para a própria máquina. Sendo assim
             * precisamos validar para que caso for um endereço da própria
             * máquina possamos apenas trabalhar como se fosse local
             */
            final String[] hostAndPath = CSPUtilidadesLang.getHostAndPathFromString(path);

            if (CSPUtilidadesLangRede.isLocalAddress(hostAndPath[0])) {
                path = hostAndPath[1];
                this.setIsRemote(false);
            } else {
                /**
                 * Mesmo que o caminho seja remoto vamos tentar trabalhar no
                 * mesmo usando o compartilhamento de rede do próprio SO
                 */
                try {
                    /**
                     * Para isso vamos tentar verificar se caminho base é
                     * válido, ou seja, existe e nos permite ler/escrever
                     */
                    String base = FilenameUtils.getPrefix(path.replace(CSPUtilidadesSO.isSoWindows() ? "/" : "\\", "\\"));// C:/
//                System.out.println(base);
                    base = base.replace("\\", "/").trim();
//                System.out.println(base);
                    base += FilenameUtils.getPath(path).replaceFirst(base, "").split("/")[0];// casa
//                System.out.println(base);
                    //base => c:/casa
                    File t = new File(base.replace(":", ""));

//                System.out.println(t.exists());
//                System.out.println(t.canRead());
//                System.out.println(t.canWrite());
                    this.setIsRemote(!(t.exists() && t.canRead() && t.canWrite()));

                } catch (Exception e) {
//                e.printStackTrace();
                }
            }
        }

        if (this.isRemote()) {
            path = path.replace(":", "").trim();
            //É preciso das //
            if (!path.startsWith("//")) {
                path = "//" + path;
            }
            //Para ajudar
            if (path.startsWith("smb:")) {
                path = path.substring(4);
            }
            //Para ajudar
            if (path.startsWith("smb")) {
                path = path.substring(3);
            }

            path = ("smb:" + path).replace("smb://smb//", "smb://");

            try {
                this.objSmbFile = new SmbFile(path);
                if (!path.endsWith("/") && this.isDir()) {
                    this.objSmbFile = new SmbFile(path + "/");
                }
            } catch (SmbAuthException e) {
                if (defAutenticacao != null) {

                    if (path.startsWith("smb:")) {
                        path = path.substring(4);// Em teoria vai ficar duas //
                    }

                    final String[] t = CSPUtilidadesLang.extraiHostAndPath(path);
                    final String[] vali = defAutenticacao.validate(t[0], t[1]);
                    final NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(vali[2], vali[0], vali[1]);

                    path = ("smb:" + path).replace("smb://smb//", "smb://");

                    this.objSmbFile = new SmbFile(path, auth);

                    try {
                        if (!path.endsWith("/") && this.isDir()) {
                            this.objSmbFile = new SmbFile(path + "/", auth);
                        }
                    } catch (SmbAuthException ee) {

                    }
                }
            }

        } else {
            if (CSPUtilidadesSO.isSoWindows()) {

                final String[] t = CSPUtilidadesLang.extraiHostAndPath(path);

                if (!t[0].equals("localhost")) {
                    if (!CSPUtilidadesLangRede.isLocalAddress(t[0]) || t[0].equals(getLocalIp())) {

                        /**
                         * Quando estamos trabalhando com um endereço que mesmo
                         * sendo remoto é acessível sem o uso da classe SMB
                         * precisamos converter de / para \ novamente
                         */
                        path = path.replace("/", "\\").replace(":", "").trim();
                    }
                }

            }

            this.objFile = new File(path);
        }

    }

    /**
     * Retorna a codificação utilizada
     *
     * @return
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Retorna a exata codificaçao utilizada no arquivo
     *
     * @return
     * @throws java.lang.Exception
     */
    public String getEncodingUsedIntoFile() throws Exception {
        if (isRemote()) {
            return null;
        }
        final FileInputStream oFiS = this.objFileInputStream();
        final InputStreamReader iSr = new InputStreamReader(oFiS);

        final String r = iSr.getEncoding();

        iSr.close();
        oFiS.close();

        return r;

    }

    /**
     * Define a codificação utilizada para ler e escrever arquivos
     *
     * @param encoding
     */
    public final void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Retorna o caminho absoluto.
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getAbsolutePath() throws Exception {
        if (this.isRemote()) {
            return (this.objSmbFile.getPath() + "").replace("\\", "/").trim();
        } else {
            return (this.objFile.getAbsolutePath() + "").replace("\\", "/").trim();
        }
    }

    /**
     * Retorna o caminho da pasta do arquivo SEM o prefixo!
     *
     * <pre>
     * C:\a\b\c.txt --> a\b\
     * ~/a/b/c.txt  --> a/b/
     * a.txt        --> ""
     * a/b/c        --> a/b/
     * a/b/c/       --> a/b/
     * </pre>
     *
     * <p>
     * Esse método exclui o prefixo, se quiser o prefixo, veja:
     * {@link #getFullPath()}.
     * </p>
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getPath() throws Exception {
        String tmp = this.getAbsolutePath();
        if (tmp.endsWith("/")) {
            tmp = CSPUtilidadesLang.substring(tmp, 0, tmp.length() - 1);
        }

        return FilenameUtils.getPath(tmp);
    }

    /**
     * Retorna o nome do arquivo ou pasta em questão. Exemplo:
     * <pre>
     * a/b/c.txt --> c.txt
     * a.txt     --> a.txt
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getName() throws Exception {
        String tmp = this.getAbsolutePath();
        if (tmp.endsWith("/")) {
            tmp = CSPUtilidadesLang.substring(tmp, 0, tmp.length() - 1);
        }
        return FilenameUtils.getName(tmp);
    }

    /**
     * Retorna o nome do arquivo ou pasta em questão. Mas com o número de
     * 'pastas anteriores' informado. Exemplo:
     * <pre>
     * a/b/c.txt --> b/c.txt
     * a.txt     --> a.txt
     * a/b/c     --> b/c
     * </pre>
     *
     * @param maxParents int - Número de 'pastas anteriores' desejado
     * @return String
     * @throws java.lang.Exception
     */
    public String getNameWhitParent(int maxParents) throws Exception {
        if (maxParents < 2) {
            return this.getName();
        }

        final ArrayList<String> r = new ArrayList<>();
        final List<String> tmp = Arrays.asList(this.getFullPath().split("/"));

        Collections.reverse(tmp);

        for (int i = 0; i < tmp.size(); i++) {
            if (i < maxParents) {
                r.add(tmp.get(i));
            }
        }

        Collections.reverse(r);

        r.add(this.getName());

        return StringUtils.join(r, "/");
    }

    /**
     * Retorna o caminho DA PASTA do arquivo COM o prefixo. Exemplo:
     * <pre>
     * C:\a\b\c.txt --> C:\a\b
     * ~/a/b/c.txt  --> ~/a/b
     * a.txt        --> ""
     * a/b/c        --> a/b/
     * a/b/c/       --> a/b/
     * C:           --> C:
     * C:\          --> C:\
     * ~            --> ~
     * ~/           --> ~
     * ~user        --> ~user
     * ~user/       --> ~user
     * </pre>
     *
     * <p>
     * Esse método retorna cmo prefixo, se quiser sem o prefixo, veja:
     * {@link #getPath()}.
     * </p>
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getFullPath() throws Exception {
        String tmp = this.getAbsolutePath();
        if (tmp.endsWith("/")) {
            tmp = CSPUtilidadesLang.substring(tmp, 0, tmp.length() - 1);
        }
        return FilenameUtils.getFullPathNoEndSeparator(tmp);
    }

    /**
     * Retorna o content type (mime-type) do arquivo.
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getContentType() throws Exception {
        if (this.isRemote()) {
            return null;
        } else {
            return Files.probeContentType(this.objFile.toPath());
        }
    }

    /**
     * Retorna o nome do arquivo ou pasta em questão. Mas sem a extensão.
     * Exemplo:
     * <pre>
     * a/b/c.txt --> c
     * a.txt     --> a
     * a/b/c     --> c
     * a/b/c/    --> ""
     * </pre>
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getBaseName() throws Exception {

        return FilenameUtils.getBaseName(this.getAbsolutePath());
    }

    /**
     * Retorna a extensão do arquivo. Exemplo:
     * <pre>
     * foo.txt      --> "txt"
     * a/b/c.jpg    --> "jpg"
     * a/b.txt/c    --> ""
     * a/b/c        --> ""
     * </pre>
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getExtension() throws Exception {

        return FilenameUtils.getExtension(this.getAbsolutePath());
    }

    /**
     * Retorna o prefixo do caminho. Exemplo:
     * <pre>
     * Windows:
     * a\b\c.txt           --> ""          --> relative
     * \a\b\c.txt          --> "\"         --> current drive absolute
     * C:a\b\c.txt         --> "C:"        --> drive relative
     * C:\a\b\c.txt        --> "C:\"       --> absolute
     * \\server\a\b\c.txt  --> "\\server\" --> UNC
     *
     * Unix:
     * a/b/c.txt           --> ""          --> relative
     * /a/b/c.txt          --> "/"         --> absolute
     * ~/a/b/c.txt         --> "~/"        --> current user
     * ~                   --> "~/"        --> current user (slash added)
     * ~user/a/b/c.txt     --> "~user/"    --> named user
     * ~user               --> "~user/"    --> named user (slash added)
     * </pre>
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getPrefix() throws Exception {
        if (CSPUtilidadesSO.isSoWindows()) {
            return FilenameUtils.getPrefix(this.getAbsolutePath().replace("/", "\\"));
        }
        return FilenameUtils.getPrefix(this.getAbsolutePath());
    }

    /**
     * Retorna o prefixo do caminho, mas usando um algoritmo que retornara as
     * informaçoes com mais precisao. Exemplo:
     * <pre>
     * Windows:
     * a\b\c.txt           --> ""          --> relative
     * \a\b\c.txt          --> "\"         --> current drive absolute
     * C:a\b\c.txt         --> "C:"        --> drive relative
     * C:\a\b\c.txt        --> "C:\"       --> absolute
     * \\server\a\b\c.txt  --> "\\server\" --> UNC
     *
     * Unix:
     * a/b/c.txt           --> ""          --> relative
     * /a/b/c.txt          --> "/"         --> absolute
     * ~/a/b/c.txt         --> "~/"        --> current user
     * ~                   --> "~/"        --> current user (slash added)
     * ~user/a/b/c.txt     --> "~user/"    --> named user
     * ~user               --> "~user/"    --> named user (slash added)
     * </pre>
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getPrefixAdvanced() throws Exception {
        return CSPUtilidadesLangArquivos.getPrefixAdvanced(this.getAbsolutePath());
    }

    /**
     * Retorna o time da última modificação
     *
     * @return -1 em caso não existir
     * @throws Exception
     */
    public long getLastModified() throws Exception {
        if (this.exists()) {
            if (this.isRemote()) {
                return this.objSmbFile.getLastModified();
            } else {
                return this.objFile.lastModified();
            }
        }
        return -1;
    }

    /**
     * Valida a existência do caminho.
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean exists() throws Exception {
        if (this.isRemote()) {
            return this.objSmbFile.exists();
        } else {
            return this.objFile.exists();
        }
    }

    /**
     * Retorna se é possível ler o arquivo
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean canRead() throws Exception {
        if (!this.exists()) {
            return false;
        }

        if (this.isRemote()) {
            return this.objSmbFile.canRead();
        } else {
            return this.objFile.canRead();
        }
    }

    /**
     * Retorna se é possível escrever o arquivo
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean canWrite() throws Exception {
        if (this.isRemote()) {
            return this.objSmbFile.canWrite();
        } else {
            return this.objFile.canWrite();
        }
    }

    /**
     * Retorna se é possível executar o arquivo
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean canExecute() throws Exception {
        if (this.isRemote()) {
            return false;
        } else {
            return this.objFile.canExecute();
        }
    }

    /**
     * Define se é possível ler o arquivo
     *
     * @param e boolean - Definição
     * @throws java.lang.Exception
     */
    public void setCanRead(boolean e) throws Exception {
        if (!this.exists()) {
            return;
        }

        if (!this.isRemote()) {
            this.objFile.setReadable(e, false);
        }
    }

    /**
     * Define se é possível escrever o arquivo
     *
     * @param e boolean - Definição
     * @throws java.lang.Exception
     */
    public void setCanWrite(boolean e) throws Exception {
        if (!this.isRemote()) {
            this.objFile.setWritable(e, false);
        }
    }

    /**
     * Define se é possível executar o arquivo
     *
     * @param e boolean - Definição
     * @throws java.lang.Exception
     */
    public void setCanExecute(boolean e) throws Exception {
        if (!this.isRemote()) {
            this.objFile.setExecutable(e, false);
        }
    }

    /**
     * Define todas as configurações de permissões de arquivos
     *
     * @param r boolean - Definição do 'read'
     * @param w boolean - Definição do 'write'
     * @param e boolean - Definição do 'execute'
     * @throws java.lang.Exception
     */
    public void setCanAll(boolean r, boolean w, boolean e) throws Exception {
        this.setCanRead(r);
        this.setCanWrite(w);
        this.setCanExecute(e);
    }

    /**
     * Define todas as configurações de permissões de uma pasta e seu conteúdo.
     * Não é recursivo!
     *
     * @param r boolean - Definição do 'read'
     * @param w boolean - Definição do 'write'
     * @param e boolean - Definição do 'execute'
     * @throws java.lang.Exception
     */
    public void setCanAllFolderAndContent(boolean r, boolean w, boolean e) throws Exception {

        this.setCanAll(r, w, e);

        if (this.isFile()) {
            return;
        }

        for (CSPArquivos f : this.listFiles()) {
            f.setCanAll(r, w, e);
        }

    }

    /**
     * Define todas as configurações de permissões de arquivos
     *
     * @param all boolean - Definição geral
     * @throws java.lang.Exception
     */
    public void setCanAll(boolean all) throws Exception {
        this.setCanAll(all, all, all);
    }

    /**
     * Define todas as configurações de permissões de uma pasta e seu conteúdo.
     * Não é recursivo!
     *
     * @param all boolean - Definição geral
     * @throws java.lang.Exception
     */
    public void setCanAllFolderAndContent(boolean all) throws Exception {
        this.setCanAllFolderAndContent(all, all, all);
    }

    /**
     * Valida se o caminho é um arquivo.
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean isFile() throws Exception {
        if (this.exists()) {
            if (this.isRemote()) {
                return this.objSmbFile.isFile();
            } else {
                return this.objFile.isFile();
            }
        }
        return false;
    }

    /**
     * Valida se o caminho é uma pasta.
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean isDir() throws Exception {
        if (this.exists()) {
            if (this.isRemote()) {
                return this.objSmbFile.isDirectory();
            } else {
                return this.objFile.isDirectory();
            }
        }
        return false;
    }

    /**
     * Retorna o tamanho do arquivo.
     *
     * @return long
     * @throws java.lang.Exception
     */
    public long length() throws Exception {
        if (this.exists()) {
            if (this.isRemote()) {
                return this.objSmbFile.length();
            } else {
                return this.objFile.length();
            }
        }

        return 0;
    }

    /**
     * Renomeia o caminho
     *
     * @param para CSPArquivos - Caminho absoluto do destino
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean rename(String para) throws Exception {
        return this.rename(new CSPArquivos(para));
    }

    /**
     * Renomeia o caminho
     *
     * @param para CSPArquivos - Caminho absoluto do destino
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean rename(CSPArquivos para) throws Exception {
        if (this.exists()) {
            if (this.isRemote()) {
                this.objSmbFile.renameTo(para.objSmbFile);
                return true;
            } else {
                return this.objFile.renameTo(para.objFile);
            }
        }
        return false;
    }

    /**
     * Cria os diretórios e subdiretórios do caminho setado.
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean mkdirs() throws Exception {
        if (!this.exists()) {
            if (this.isRemote()) {
                this.objSmbFile.mkdirs();
                return true;
            } else {
                return this.objFile.mkdirs();
            }
        }
        return this.isDir();
    }

    /**
     * Realiza a cópia para o caminho. O método não irá copiar caso já exista um
     * arquivo/pasta com o mesmo nome no destino!
     *
     * @param destino String - Caminho destino do arquivo/pasta
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean copy(String destino) throws Exception {
        return this.copy(new CSPArquivos(destino));
    }

    /**
     * Realiza a cópia para o caminho. O método não irá copiar caso já exista um
     * arquivo/pasta com o mesmo nome no destino!
     *
     * @param destino CSPArquivos - Caminho destino do arquivo/pasta
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean copy(CSPArquivos destino) throws Exception {
        if (this.exists() && (!destino.exists() || destino.isDir())) {
            if (this.isRemote() && destino.isRemote()) {

                // remoto => remoto
                if (this.isDir()) {
                    destino.mkdirs();
                    final CSPArquivos l = new CSPArquivos();
                    for (String f : this.list()) {
                        l.setPath(this.getAbsolutePath() + f);

                        l.copy(destino.getAbsolutePath() + "/" + f);
                    }

                } else {
                    this.objSmbFile.copyTo(destino.objSmbFile);
                }

            } else if (this.isRemote() && !destino.isRemote()) {

                // remoto => local
                if (this.isDir()) {
                    destino.mkdirs();
                    final CSPArquivos l = new CSPArquivos();
                    for (String f : this.list()) {
                        l.setPath(this.getAbsolutePath() + f);

                        l.copy(destino.getAbsolutePath() + "/" + f);
                    }

                } else {
//                    destino.setBytes(this.getBytes());
                    new CSPArquivos(destino.getFullPath()).mkdirs();
                    try (FileOutputStream write = destino.objFileOutputStream(); SmbFileInputStream read = new SmbFileInputStream(this.objSmbFile);) {

                        final byte[] buf = new byte[16 * 1024 * 1024];
                        int len;
                        while ((len = read.read(buf)) > 0) {
                            write.write(buf, 0, len);
                        }
                        write.close();
                        read.close();
                    }
                }

            } else if (!this.isRemote() && destino.isRemote()) {

                // local => remoto
                if (this.isDir()) {

                    if (!destino.getAbsolutePath().endsWith("/")) {
                        destino.setPath(destino.getAbsolutePath() + "/");
                    }

                    destino.mkdirs();

                    for (CSPArquivos l : this.listFiles()) {
                        l.copy(destino.getAbsolutePath() + "/" + l.getName());
                    }

                } else {
//                    destino.setBytes(this.getBytes());
                    new CSPArquivos(destino.getFullPath()).mkdirs();
                    try (SmbFileOutputStream write = new SmbFileOutputStream(destino.objSmbFile); FileInputStream read = this.objFileInputStream()) {

                        final byte[] buf = new byte[16 * 1024 * 1024];
                        int len;
                        while ((len = read.read(buf)) > 0) {
                            write.write(buf, 0, len);
                        }
                        write.close();
                        read.close();
                    }
                }

            } else {

                // local => local
                if (this.isDir()) {

                    destino.mkdirs();

                    for (CSPArquivos l : this.listFiles()) {
                        l.copy(destino.getAbsolutePath() + "/" + l.getName());
                    }

                } else {
                    if (destino.isDir()) {
                        //Copiaremos o arquivo para dentro da pasta já existente
                        final CSPArquivos tmp = new CSPArquivos(destino.getAbsolutePath() + "/" + this.getName());
                        if (tmp.exists()) {
                            return false;
                        }
                        Files.copy(this.objFile.toPath(), tmp.objFile.toPath());
                    } else {
                        new CSPArquivos(destino.getFullPath()).mkdirs();
                        Files.copy(this.objFile.toPath(), destino.objFile.toPath());
                    }
                }

            }
            return destino.exists();
        }
        return false;
    }

    /**
     * Efetua a subistituição do arquivo/pasta de destino pelo atual. Ou seja,
     * se já existir um arquivo/pasta no destino o mesmo será excluído e o atual
     * será copiado para o lugar
     *
     * @param destino CSPArquivos - Caminho destino do arquivo
     * @return
     * @throws java.lang.Exception
     */
    public boolean replace(String destino) throws Exception {
        return this.replace(new CSPArquivos(destino));
    }

    /**
     * Efetua a subistituição do arquivo/pasta de destino pelo atual. Ou seja,
     * se já existir um arquivo/pasta no destino o mesmo será excluído e o atual
     * será copiado para o lugar
     *
     * @param destino CSPArquivos - Caminho destino do arquivo
     * @return
     * @throws java.lang.Exception
     */
    public boolean replace(CSPArquivos destino) throws Exception {
        if (destino.delete()) {
            return this.copy(destino);
        }
        return false;
    }

    /**
     * Realiza a exclusão do caminho.
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean delete() throws Exception {
        if (this.exists()) {
            if (this.isRemote()) {
                this.objSmbFile.delete();
            } else {
                FileDeleteStrategy.FORCE.delete(this.objFile.getAbsoluteFile());
            }
            return true;
        }
        return true;
    }

    /**
     * Define o conteúdo do arquivo.
     *
     * @param content String - Conteúdo a ser gravado
     *
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean setContent(String content) throws Exception {
        return this.setContent(content, false);
    }

    /**
     * Adiciona conteúdo ao final do arquivo
     *
     * @param content String - Conteúdo a ser gravado
     *
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean appendContent(String content) throws Exception {
        return this.setContent(content, true);
    }

    /**
     * Adiciona conteúdo ao final do arquivo
     *
     * @param content String - Conteúdo a ser gravado
     * @param onlyAppend
     *
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean setContent(String content, boolean onlyAppend) throws Exception {
        return this.setContent(content, onlyAppend, true);
    }

    /**
     * Define o conteúdo do arquivo.
     *
     * @param content String - Conteúdo a ser gravado
     * @param onlyAppend
     * @param toLowerCase
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public synchronized boolean setContent(String content, boolean onlyAppend, boolean toLowerCase) throws Exception {
        String tmp = this.getAbsolutePath();
        this.setPath(FilenameUtils.getFullPath(tmp));
        if (this.mkdirs()) {
            this.setPath(tmp);

            final StringBuilder abs = new StringBuilder();

            { //Nome e extensão do arquivo precisam ser salvas sempre em minúsculo!
                //Corrigindo o fefe, isso é só no linux, por ser case sensitive.
                abs.append(FilenameUtils.getFullPath(tmp));
                abs.append("/");
                if (toLowerCase) {
                    abs.append(this.getBaseName().toLowerCase());
                } else {
                    abs.append(this.getBaseName());
                }
                if (!this.getFormatDefault().equals("*")) {
                    abs.append(".");
                    abs.append(this.getFormatDefault().toLowerCase());
                } else if (!this.getExtension().trim().isEmpty()) {
                    abs.append(".");
                    abs.append(this.getExtension().toLowerCase());
                }
            }

            if (this.isRemote()) {
                this.setPath(abs.toString());
                try (Writer writer = new BufferedWriter(new OutputStreamWriter(new SmbFileOutputStream(this.objSmbFile), this.getEncoding()))) {
                    writer.write(content);
                    writer.close();
                }
                this.setPath(tmp);
            } else {

                File f = new File(abs.toString());
                if (f.exists() == false || (f.exists() && f.isFile())) {
                    FileUtils.writeStringToFile(f, content, Charset.forName(this.getEncoding()), onlyAppend);
                } else {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Define o conteúdo do arquivo, em bytes.
     *
     * @param bytes byte - Conteúdo a ser gravado
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean setBytes(byte[] bytes) throws Exception {

        if (this.isRemote()) {
            this.setContent("gold-transfer");//Cria um arquivo para ser sobreescrito
            if (this.exists()) {

                try (SmbFileOutputStream out = new SmbFileOutputStream(this.objSmbFile)) {
                    out.write(bytes);
                    return true;
                }
            }
        } else {
            String tmp = this.getAbsolutePath();
            this.setPath(FilenameUtils.getFullPath(tmp));
            if (this.mkdirs()) {
                this.setPath(tmp);

                Files.write(Paths.get(tmp), bytes);
                return true;
            }
        }

        return false;
    }

    /**
     * Retorna o conteúdo do arquivo.
     *
     * @return String
     * @throws java.lang.Exception
     */
    public synchronized String getContent() throws Exception {
        return this.getContent(this.getEncoding());
    }

    /**
     * Retorna o conteúdo do arquivo.
     *
     * @param forceEncoding String - Determina qual sera a docificaçao usada
     * @return String
     * @throws java.lang.Exception
     */
    public synchronized String getContent(String forceEncoding) throws Exception {
        if (this.exists() && this.isFile()) {
            if (isRemote) {
                final BufferedReader buffer = new BufferedReader(new InputStreamReader(new SmbFileInputStream(this.objSmbFile), forceEncoding));
                String linha = buffer.readLine();
                final StringJoiner end = new StringJoiner("\n");

                while (linha != null) {
                    end.add(linha);
                    linha = buffer.readLine();
                }
                buffer.close();
                return end.toString();
            } else {
//                final FileInputStream in = this.objFileInputStream();
//                
//                final String r = IOUtils.toString(in, forceEncoding);
//                
//                in.close();
//                
//                return r;
                return new String(this.getBytes(), forceEncoding);
            }
        }
        return null;
    }

    /**
     * Retorna o conteúdo do arquivo.
     *
     * @return String
     * @throws java.lang.Exception
     */
    public synchronized LinkedHashSet<String> getContentList() throws Exception {
        LinkedHashSet<String> retorno = new LinkedHashSet<>();

        if (this.exists() && this.isFile()) {
            if (!isRemote) {
                try (BufferedReader buffer = new BufferedReader(new InputStreamReader(this.objFileInputStream()))) {
                    String linha = buffer.readLine();

                    while (linha != null) {
                        retorno.add(linha);
                        linha = buffer.readLine();
                    }
                }
            }
        }

        return retorno;
    }

    /**
     * Retorna o conteúdo do arquivo, em bytes.
     *
     * @return String
     * @throws java.lang.Exception
     */
    public byte[] getBytes() throws Exception {
        if (this.isRemote()) {
            return IOUtils.toByteArray(this.objSmbFile);
        } else {
            final FileInputStream objFileInputStream = this.objFileInputStream();
            byte[] r = IOUtils.toByteArray(objFileInputStream);
            objFileInputStream.close();
            return r;
        }
    }

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String[] list() throws Exception {
        return this.list(null);
    }

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @param filter FilenameFilter - Filtro para os arquivos
     * @return String
     * @throws java.lang.Exception
     */
    public String[] list(FilterName filter) throws Exception {
        String[] l;

        if (this.isRemote()) {
            if (filter == null) {
                l = this.objSmbFile.list();

            } else {

                l = this.objSmbFile.list((SmbFile sf, String string) -> filter.accept(
                        sf.getPath(),
                        string
                ));
            }
        } else {
            if (filter == null) {
                l = this.objFile.list();
            } else {
                l = this.objFile.list((File dir, String name) -> filter.accept(
                        dir.getAbsolutePath(),
                        name
                ));
            }
        }

        if (l != null) {
            Arrays.sort(l);
            return l;
        }

        return new String[0];
    }

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @return CSPArquivos[]
     * @throws java.lang.Exception
     */
    public CSPArquivos[] listFiles() throws Exception {
        return this.listFiles(null);
    }

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @param filter FileFilter - filtro
     * @return CSPArquivos[]
     * @throws java.lang.Exception
     */
    public CSPArquivos[] listFiles(FilterFile filter) throws Exception {

        final CSPArquivos[] r;

        if (!this.isRemote()) {
            File[] l;
            if (filter == null) {
                l = this.objFile.listFiles();

            } else {
                l = this.objFile.listFiles((File pathname) -> {
                    return filter.accept(
                            FilenameUtils.getFullPath(pathname.getAbsolutePath()),
                            FilenameUtils.getName(pathname.getAbsolutePath()),
                            pathname.isFile());
                });
            }

            if (l != null) {
                Arrays.sort(l);
            } else {
                l = new File[0];
            }

            r = new CSPArquivos[l.length];

            for (int i = 0; i < l.length; i++) {
                r[i] = new CSPArquivos(this.getAbsolutePath() + "/" + l[i].getName());
            }

        } else {

            SmbFile[] l = this.objSmbFile.listFiles();

            if (l == null) {
                l = new SmbFile[0];
            }

            r = new CSPArquivos[l.length];

            for (int i = 0; i < l.length; i++) {
                r[i] = new CSPArquivos(this.getAbsolutePath() + l[i].getName());
            }

        }

        return r;
    }

    /**
     * Retorna o MD5 do arquivo
     *
     * @return String
     * @throws java.lang.Exception
     */
    public String getMd5() throws Exception {

        if (this.isRemote()) {
            /**
             * Não vamos dar suporte para arquivos remotos gigantes
             */
            if (this.length() <= 15000) {

                final CSPArquivos tmp = new CSPArquivos(CSPUtilidadesSO.PATH_TEMP + "/tmp-md5-" + this.getName());

                if (this.replace(tmp)) {

                    final String tmpMd5 = tmp.getMd5();

                    if (tmp.delete()) {
                        return tmpMd5;
                    }

                }
            }

            return null;
        }

        try (InputStream in = this.objFileInputStream()) {
            return DigestUtils.md5Hex(in);
        }

    }

    /**
     * Retorna o objeto File do arquivo. Somente local.
     *
     * @see CSPArquivos#objSmbFile()
     * @return File
     * @throws Exception
     */
    public File objFile() throws Exception {
        if (this.isRemote()) {
            return null;
        }
        return this.objFile;
    }

    /**
     * Retorna o objeto File do arquivo. Somente remoto.
     *
     * @see CSPArquivos#objFile()
     * @return SmbFile
     * @throws Exception
     */
    public SmbFile objSmbFile() throws Exception {
        if (!this.isRemote()) {
            return null;
        }
        return this.objSmbFile;
    }

    /**
     * Retorna o objeto FileInputStream do caminho absoluto atual
     *
     * @return FileInputStream
     * @throws java.io.FileNotFoundException
     */
    public FileInputStream objFileInputStream() throws Exception {
        if (this.isRemote()) {
            return null;
        }
        return new FileInputStream(this.getAbsolutePath());
    }

    /**
     * Retorna o objeto FileOutputStream do caminho absoluto atual
     *
     * @return FileOutputStream
     * @throws java.io.FileNotFoundException
     */
    public FileOutputStream objFileOutputStream() throws Exception {
        if (this.isRemote()) {
            return null;
        }
        return new FileOutputStream(this.getAbsolutePath());
    }

    /**
     * Gera um zip dos arquivos recebidos por parâmetro
     *
     * @param src CSPArquivos[] - Arquivos a serem adicionados ao ZIP
     * @return
     * @throws java.lang.Exception
     */
    public boolean zipFiles(CSPArquivos... src) throws Exception {

        return CSPUtilidadesLangArquivos.zipFiles(this, src);

    }

    /**
     * Descompacta todos os arquivos, se somente arquivos, contidos na raiz do
     * zip
     *
     * @param destino CSPArquivos - Destino. Precisa ser uma pasta válida!
     * @return
     * @throws java.lang.Exception
     */
    public boolean unzipFiles(CSPArquivos destino) throws Exception {

        return CSPUtilidadesLangArquivos.unzipFiles(destino, this);
    }

    /**
     * Retorna o conteúdo do arquivo de forma interativa pelas linhas do mesmo
     *
     * @return
     * @throws Exception
     */
    public LineIterator getIteratorLineFile() throws Exception {
        return IOUtils.lineIterator(this.objFileInputStream(), getEncoding());
    }

    /**
     * O método percorre a pasta a procura do arquivo/pasta a ser fitrado,
     * estando ele na raiz, ou em subpastas.
     *
     * @param filtro String - Valor usado para filtrar os resultados, por
     * exemplo o nome.
     * @param filtroIgnorar String[] - Valores que devem ser ignorados.
     * @param filtroIsFile boolean - Determina se é procurado um arquivo ou
     * diretório.
     * @param isCaseInsensitive - Define se a comparação com o arquivo deve ser
     * CaseInsensitive
     * @param resultado StringBuilder - StringBuilder a ser alimentado com
     * ocaminho do arquivo quando encontrado.
     * @throws Exception
     */
    public void findFile(String filtro, String[] filtroIgnorar, boolean filtroIsFile, boolean isCaseInsensitive, StringBuilder resultado) throws Exception {
        CSPUtilidadesLangArquivos.findFile(this, filtro, filtroIgnorar, filtroIsFile, isCaseInsensitive, resultado);
    }

    /**
     * O método percorre a pasta a procura do arquivo/pasta a ser fitrado,
     * estando ele na raiz, ou em subpastas.
     *
     * @param filtro String - Valor usado para filtrar os resultados, por
     * exemplo o nome.
     * @param filtroIgnorar String[] - Valores que devem ser ignorados.
     * @param filtroIsFile boolean - Determina se é procurado um arquivo ou
     * diretório.
     * @param isCaseInsensitive - Define se a comparação com o arquivo deve ser
     * CaseInsensitive
     * @throws Exception
     */
    public String findFile(String filtro, String[] filtroIgnorar, boolean filtroIsFile, boolean isCaseInsensitive) throws Exception {
        return CSPUtilidadesLangArquivos.findFile(this, filtro, filtroIgnorar, filtroIsFile, isCaseInsensitive);
    }

    /**
     * Preenche um LinkedHashSet com todos os arquivos da pasta e suas
     * subpastas.
     *
     * @param files LinkedHashSet - Set que é preenchido com os caminhos
     * absolutos dos arquivos encontrados.
     * @param ignoraFiles String[] - Lista com o nome de arquivos que devem ser
     * ignorados.
     * @throws Exception
     */
    public void listRecursive(LinkedHashSet<String> files, String[] ignoraFiles) throws Exception {
        if (this.isDir()) {
            CSPUtilidadesLangArquivos.listRecursive(this, files, ignoraFiles);
        }
    }

    /**
     * Retorna todos os arquivos da pasta e suas subpastas.
     *
     * @param ignoraFiles String[] - Lista com o nome de arquivos que devem ser
     * ignorados.
     * @return
     * @throws Exception
     */
    public String[] listRecursive(String[] ignoraFiles) throws Exception {
        return CSPUtilidadesLangArquivos.listRecursive(this, ignoraFiles);
    }

    public interface Autenticacao {

        /**
         *
         * @param host
         * @param path
         * @return [0] => usuário, [1] => senha, [2] => domínio
         * @throws java.lang.Exception
         */
        public String[] validate(String host, String path) throws Exception;
    }

    public static class FileInfo {

        public final String name;
        public final String absolutePath;
        public final boolean isFile;
        public final boolean exists;
        public final String md5;
        public final long lastUpdate;
        public final long size;

        public FileInfo(String name, String absolutePath, boolean isFile, String md5, long lastUpdate, long size, boolean exists) {
            this.name = name;
            this.absolutePath = absolutePath;
            this.isFile = isFile;
            this.md5 = md5;
            this.lastUpdate = lastUpdate;
            this.size = size;
            this.exists = exists;
        }

    }

    /**
     * Interface de filtro de arquivo
     */
    public interface FilterName {

        /**
         * Codição de aceitação
         *
         * @param dir String - Caminho absoluto até a pasta em que se encontra o
         * arquivo
         * @param name String - Nome do arquivo
         * @return boolean
         */
        public boolean accept(String dir, String name);
    }

    /**
     * Interface de filtro de arquivo
     */
    public interface FilterFile {

        /**
         * Codição de aceitação
         *
         * @param dir String - Caminho absoluto até a pasta em que se encontra o
         * arquivo
         * @param name String - Nome do arquivo
         * @param isFile String - Se é um arquivo, caso não será uma pasta
         * @return boolean
         */
        public boolean accept(String dir, String name, boolean isFile);
    }
}
