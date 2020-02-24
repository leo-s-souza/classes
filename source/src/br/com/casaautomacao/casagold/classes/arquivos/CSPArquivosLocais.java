/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.DIR_SEPARATOR;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 * Classe para manipulação de arquivos locais
 *
 * @author Jean Regis
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
@Deprecated
public class CSPArquivosLocais implements InterfaceCSPArquivos {

    @Override
    public String getFormatDefault() {
        return "*";
    }

    /**
     * Caminho absoluto.
     */
    String path;

    /**
     * Caminho de uma pasta ou arquivo com base no caminho absoluto.
     */
    private String name = "";

    /**
     *
     * Classe para manipulação de arquivos locais
     */
    @Deprecated
    public CSPArquivosLocais() {
    }

    /**
     *
     * Classe para manipulação de arquivos locais
     *
     * @param path String - Caminho absoluto
     */
    @Deprecated
    public CSPArquivosLocais(String path) {

        setPath(path);
    }

    @Override
    @Deprecated
    public void setPath(String path) {
        path = path.trim().replace("\\", "/");
        //Para não gerar problema é removida a última barra, caso exista
        if (path.length() > 0 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        this.path = path;
    }

    @Override
    @Deprecated
    public String getPath() {
        return path;
    }

    @Override
    @Deprecated
    public void setName(String name) {
        name = name.trim().replace("\\", "/");
        //Para não gerar problema é removida a última barra, caso exista
        if (name.length() > 0 && name.endsWith("/")) {
            name = name.substring(0, name.length() - 1);
        }
        //Para não gerar problema é removida a primeira barra, caso exista
        if (name.length() > 0 && name.startsWith("/")) {
            name = name.substring(1);
        }

        this.name = name;
    }

    @Override
    @Deprecated
    public String getName() {
        return name;
    }

    @Override
    @Deprecated
    public String getAbsolutePath() {
        if (!this.name.trim().isEmpty()) {
            return this.path + "/" + this.name;
        } else {
            return this.path;
        }
    }

    @Override
    @Deprecated
    public boolean exists() {
        return this.objFile().exists();
    }

    @Override
    @Deprecated
    public boolean isFile() {
        if (this.exists()) {
            return this.objFile().isFile();
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean isDir() {
        if (this.exists()) {
            return this.objFile().isDirectory();
        }

        return false;
    }

    @Override
    @Deprecated
    public long length() {
        if (this.exists()) {
            return this.objFile().length();
        }

        return 0;
    }

    @Override
    @Deprecated
    public boolean rename(String para) {
        return this.objFile().renameTo(new File(para));
    }

    @Override
    @Deprecated
    public boolean mkdirs() {
        if (!this.exists()) {
            return this.objFile().mkdirs();
        }
        //se ele existir
        return this.objFile().exists();
    }

    /**
     * Realiza a cópia do arquivo.
     *
     * @param destino CSPArquivosLocais - Destindo da cópia
     *
     * @return boolean
     */
    @Deprecated
    public boolean copy(CSPArquivosLocais destino) throws IOException {
        if (this.exists()) {
            File x = this.objFile();
            destino.delete();
            File y = new File(destino.getAbsolutePath());
            Files.copy(x.toPath(), y.toPath());
            return cccs(y, x);
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean copy(String destino) throws IOException {
        return this.copy(new CSPArquivosLocais(destino));
    }

    /**
     * Realiza a cópia do arquivo.
     *
     * @param destino File - Destindo da cópia
     *
     * @return boolean
     */
    @Deprecated
    public boolean copy(File destino) throws IOException {
        if (this.exists()) {
            File x = this.objFile();
            Files.copy(x.toPath(), destino.toPath());
            return cccs(destino, x);
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean delete() throws IOException {
        if (this.exists()) {
            /* if (getSO() == SO.SO_WINDOWS) {
             //Infelizmente é preciso apelar
             String fName = this.objFile().getCanonicalPath();
             File bat = new File(PATH_TEMP+"/gold-delete-force-"+getMd5(fName)+".bat");
             FileUtils.writeStringToFile(bat, "DEL \"" + fName+ "\"", Charset.forName("UTF-8"), false);
             runBatchFileInWindows(bat.getAbsolutePath());
             } else {
             */
            FileDeleteStrategy.FORCE.delete(this.objFile().getAbsoluteFile());
            //}
        }
        return true;
    }

    /**
     * Retorna o objeto do tipo File do caminho atual
     *
     * @return File
     */
    @Deprecated
    public File objFile() {
        return new File(this.getAbsolutePath());
    }

    @Override
    @Deprecated
    public boolean setContent(String content) throws Exception {
        return this.setContent(content, false);
    }

    @Deprecated
    public boolean appendContent(String content) throws IOException {
        return this.setContent(content, true);
    }

    @Deprecated
    public boolean setContent(String content, boolean onlyAppend) throws IOException {
        String tmp = this.getPath();
        String tmp2 = this.getName();
        String[] arquivo = this.getAbsolutePath().replace("\\", "/").split("/");
        String caminho = "";
        for (int i = 0; i < arquivo.length - 1; i++) {
            caminho += arquivo[i] + "/";
        }
        this.setPath(caminho);
        this.setName("");
        if (this.mkdirs()) {
            this.setPath(tmp);
            this.setName(tmp2);

            String abs = this.getAbsolutePath();
            if (!this.getFormatDefault().equals("*")) {
                int idx = abs.lastIndexOf("/");
                String onlyName = idx >= 0 ? abs.substring(idx + 1) : name;
                abs = abs.replace(onlyName, onlyName.toLowerCase());
                if (!abs.endsWith(this.getFormatDefault())) {
                    abs += this.getFormatDefault();
                }
            }
            File f = new File(abs);
            if (f.exists() == false || (f.exists() && f.isFile())) {
                FileUtils.writeStringToFile(f, content, Charset.forName("UTF-8"), onlyAppend);
            } else {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean setBytes(byte[] bytes) throws IOException {
        String tmp = this.getPath();
        String tmp2 = this.getName();
        String[] arquivo = this.getAbsolutePath().replace("\\", "/").split("/");
        String caminho = "";
        for (int i = 0; i < arquivo.length - 1; i++) {
            caminho += arquivo[i] + "/";
        }
        this.setPath(caminho);
        this.setName("");
        if (this.mkdirs()) {
            this.setPath(tmp);
            this.setName(tmp2);

            Files.write(Paths.get(this.getAbsolutePath()), bytes);
            return true;
        }
        return false;
    }

    /**
     * Retorna o objeto FileInputStream do caminho absoluto atual
     *
     * @return FileInputStream
     * @throws java.io.FileNotFoundException
     */
    @Deprecated
    public FileInputStream objFileInputStream() throws FileNotFoundException {
        return new FileInputStream(this.getAbsolutePath());
    }

    /**
     * Retorna o objeto FileOutputStream do caminho absoluto atual
     *
     * @return FileOutputStream
     * @throws java.io.FileNotFoundException
     */
    @Deprecated
    public FileOutputStream objFileOutputStream() throws FileNotFoundException {
        return new FileOutputStream(this.getAbsolutePath());
    }

    @Override
    @Deprecated
    public String getContent() throws IOException {
        if (this.exists()) {
            FileInputStream in = this.objFileInputStream();
            String r = IOUtils.toString(in, "UTF-8");
            in.close();
            return r;
        }
        return null;
    }

    @Override
    @Deprecated
    public byte[] getBytes() throws FileNotFoundException, IOException {
        return IOUtils.toByteArray(this.objFileInputStream());
    }

    /**
     * Valida cópia de arquivo.
     *
     * @param x
     * @param z
     * @return
     */
    @Deprecated
    private boolean cccs(File x, File z) {
        return x.length() == z.length();
    }

    @Override
    @Deprecated
    public String[] list() {
        return this.list(null);
    }

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @param filter FilenameFilter - filtro
     * @return String
     */
    @Deprecated
    public String[] list(FilenameFilter filter) {
        String[] l = this.objFile().list(filter);
        if (l != null) {
            Arrays.sort(l);
            return l;
        } else {
            return new String[0];
        }
    }

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @return CSPArquivosLocais
     */
    @Deprecated
    public CSPArquivosLocais[] listFiles() {
        return this.listFiles(null);
    }

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @param filter FileFilter - filtro
     * @return CSPArquivosLocais
     */
    @Deprecated
    public CSPArquivosLocais[] listFiles(FileFilter filter) {
        File[] l = this.objFile().listFiles(filter);
        if (l != null) {
            Arrays.sort(l);

        } else {
            l = new File[0];
        }
        CSPArquivosLocais[] r = new CSPArquivosLocais[l.length];
        for (int i = 0; i < l.length; i++) {
            r[i] = new CSPArquivosLocais(this.getAbsolutePath());
            r[i].setName(l[i].getName());
        }
        return r;
    }

    @Override
    @Deprecated
    public String md5() throws FileNotFoundException, IOException {
        try (InputStream in = this.objFileInputStream()) {
            return DigestUtils.md5Hex(in);
        }

    }

    @Override
    @Deprecated
    public String getFormat() {
        return FilenameUtils.getExtension(this.getAbsolutePath());
    }

    @Override
    @Deprecated
    public String getContentType() throws IOException {
        return Files.probeContentType(this.objFile().toPath());
    }

    /**
     * Compacta o arquivo. Compactação simples. Cria apenas um arquivo zipado
     * com arquivo dentro
     *
     * @param dest String - Destino
     * @return
     */
    @Deprecated
    public boolean zipFile(String dest) throws IOException {
        byte[] buffer = new byte[1024];

        FileOutputStream fos = new FileOutputStream(dest);
        ZipOutputStream zos = new ZipOutputStream(fos);
        ZipEntry ze = new ZipEntry(this.objFile().getName());
        zos.putNextEntry(ze);
        FileInputStream in = new FileInputStream(this.getAbsolutePath());

        int len;
        while ((len = in.read(buffer)) > 0) {
            zos.write(buffer, 0, len);
        }

        in.close();
        zos.closeEntry();

        zos.close();

        return true;
    }

    /**
     * Descompacta o arquivo. Descompactação simples. Extraí somente os arquivos
     * e coloca na pasta raíz
     *
     * @param dest String - Destino
     * @return
     */
    @Deprecated
    public boolean unZipFile(String dest) throws IOException {
        File folder = new File(dest);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        ZipFile zipFile = new ZipFile(this.getAbsolutePath());
        Enumeration<?> enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();
            String nome = new File(zipEntry.getName()).getName();
            File file = new File(folder + DIR_SEPARATOR + nome);
            if (nome.endsWith("/")) {
                continue;
            }
            File parent = file.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            InputStream is = zipFile.getInputStream(zipEntry);
            FileOutputStream fos = new FileOutputStream(file);
            byte[] bytes = new byte[1024];
            int length;
            while ((length = is.read(bytes)) >= 0) {
                fos.write(bytes, 0, length);
            }
            is.close();
            fos.close();

        }
        zipFile.close();

        return true;
    }

    @Override
    @Deprecated
    public String getExtension() {
        return FilenameUtils.getExtension(this.getAbsolutePath());
    }
}
