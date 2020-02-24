/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Arrays;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import jcifs.smb.SmbFileOutputStream;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 * Classe para manipulação de arquivos remotos (SMB)
 *
 * @author Jean Regis
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
@Deprecated
public class CSPArquivosRemotos implements InterfaceCSPArquivos {

    @Override
    @Deprecated
    public String getFormatDefault() {
        return "*";
    }

    /**
     * Caminho absoluto.
     */
    private String path;

    /**
     * Caminho de uma pasta ou arquivo com base no caminho absoluto.
     */
    private String name = "";

    /**
     * Variável de autenticação.
     */
    private NtlmPasswordAuthentication auth;

    /**
     *
     *
     * @param caminho
     */
    @Deprecated
    public CSPArquivosRemotos(String caminho) {
        this.setPath(caminho);
    }

    /**
     * Construtor com User e Password.
     *
     * @param user
     * @param password
     */
    @Deprecated
    public CSPArquivosRemotos(String user, String password) {
        this.auth = new NtlmPasswordAuthentication("", user, password);
    }

    /**
     * Construtor com caminho absoluto, User e Password.
     *
     * @param caminho
     * @param user
     * @param password
     */
    @Deprecated
    public CSPArquivosRemotos(String caminho, String user, String password) {
        setPath(caminho);
        this.auth = new NtlmPasswordAuthentication("", user, password);
    }

    @Override
    @Deprecated
    public void setPath(String path) {
        path = path.trim().replace("\\", "/").replace(":", "");
        //É preciso das //
        if (!path.startsWith("//")) {
            path = "//" + path;
        }
        //Para não gerar problema é removida a última barra, caso exista
        if (path.length() > 0 && path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        //Para ajudar
        if (path.startsWith("smb:")) {
            path = path.substring(4);
        }
        //Para ajudar
        if (path.startsWith("smb")) {
            path = path.substring(3);
        }
        path = "smb:" + path;
        this.path = path.replace("smb://smb//", "smb://");
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

    /**
     * Define autenticação da conexão
     *
     * @param auth NtlmPasswordAuthentication - Autenticação SMB
     */
    @Deprecated
    public void setAuth(NtlmPasswordAuthentication auth) {
        this.auth = auth;
    }

    /**
     * Retorna autenticação.
     *
     * @return NtlmPasswordAuthentication
     */
    @Deprecated
    public NtlmPasswordAuthentication getAuth() {
        return auth;
    }

    @Override
    @Deprecated
    public boolean exists() throws SmbException, MalformedURLException {
        SmbFile g = this.objSmbFile();
        if (g != null) {
            return g.exists();
        }
        return false;
    }

    @Override
    @Deprecated
    public boolean isFile() throws SmbException, MalformedURLException {
        if (this.exists()) {
            return this.objSmbFile().isFile();
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean isDir() throws SmbException, MalformedURLException {
        if (this.exists()) {
            return this.objSmbFile().isDirectory();
        }

        return false;
    }

    @Override
    @Deprecated
    public long length() throws SmbException, MalformedURLException {
        if (this.exists()) {
            return this.objSmbFile().length();
        }

        return 0;
    }

    @Override
    @Deprecated
    public boolean rename(String para) throws SmbException, MalformedURLException {
        this.objSmbFile().renameTo(new SmbFile(para, auth));
        return true;
    }

    @Override
    @Deprecated
    public boolean mkdirs() throws SmbException, MalformedURLException {
        if (!this.exists()) {
            SmbFile file = this.objSmbFile();

            file.mkdirs();
            return file.exists();

        }

        return true;
    }

    /**
     * Realiza a cópia do arquivo.
     *
     * @param destino Para onde vai ser copiado
     * @return boolean
     */
    @Deprecated
    public boolean copy(CSPArquivosRemotos destino) throws SmbException, MalformedURLException {
        if (this.exists()) {
            SmbFile x = this.objSmbFile();
            SmbFile y = new SmbFile(destino.getAbsolutePath(), destino.getAuth());
            if (!x.getPath().equals(y.getPath())) {
                x.copyTo(y);
                return cccs(y, x);
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean delete() throws SmbException, MalformedURLException {
        if (this.exists()) {
            this.objSmbFile().delete();
            return true;
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean setContent(String content) throws IOException {
        String tmp = this.getPath();
        String tmp2 = this.getName();
        String arquivo = this.getAbsolutePath();
        this.setPath(arquivo.substring(0, arquivo.length() - this.objSmbFile().getName().length()));
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
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(new SmbFileOutputStream(new SmbFile(abs, this.getAuth())), "UTF-8"))) {
                writer.write(content);
                writer.close();
            }

            return true;
        }

        return false;
    }

    @Override
    @Deprecated
    public boolean setBytes(byte[] bytes) throws IOException {
        this.setContent("gold-transfer");//Cria um arquivo para ser sobreescrito
        if (this.exists()) {

            try (SmbFileOutputStream out = new SmbFileOutputStream(this.objSmbFile())) {
                out.write(bytes);
                return true;
            }
        }
        return false;
    }

    /**
     * Retorna um objeto do tipo SmbFile do caminho atual
     *
     * @return SmbFile
     * @throws java.net.MalformedURLException
     * @throws jcifs.smb.SmbException
     */
    @Deprecated
    public SmbFile objSmbFile() throws MalformedURLException, SmbException {
        SmbFile o = new SmbFile(this.getAbsolutePath(), auth);
        if (o.exists()) {
            if (o.isDirectory()
                    && !this.getAbsolutePath().endsWith("/")) {
                o = new SmbFile(this.getAbsolutePath() + "/", auth);
            }
        }
        return o;
    }

    @Override
    @Deprecated
    public String getContent() throws SmbException, IOException {
        if (this.exists()) {
            SmbFile smbFile = this.objSmbFile();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(new SmbFileInputStream(smbFile), "UTF-8"));
            String linha = buffer.readLine();
            String end = "";

            while (linha != null) {
                end += linha + "\n";
                linha = buffer.readLine();
            }
            buffer.close();
            return end;
        }

        return null;
    }

    @Override
    @Deprecated
    public byte[] getBytes() throws IOException {
        return IOUtils.toByteArray(this.objSmbFile());
    }

    /**
     * Valida cópia de arquivo.
     *
     * @param x
     * @param z
     * @return
     */
    @Deprecated
    private boolean cccs(SmbFile x, SmbFile z) throws SmbException {
        return x.length() == z.length();
    }

    @Override
    @Deprecated
    public boolean copy(String destino) throws SmbException, MalformedURLException {
        return this.copy(new CSPArquivosRemotos(destino, auth.getUsername(), auth.getPassword()));
    }

    @Override
    @Deprecated
    public String[] list() throws SmbException, MalformedURLException {
        String[] l;
        SmbFile o = this.objSmbFile();
        l = o.list();
        if (l != null) {
            Arrays.sort(l);
            return l;
        }
        return new String[0];
    }

    @Override
    @Deprecated
    public String md5() throws IOException {
        try (InputStream in = this.objSmbFile().getInputStream()) {
            return DigestUtils.md5Hex(in);
        }
    }

    @Override
    @Deprecated
    public String getFormat() throws MalformedURLException, SmbException {
        return FilenameUtils.getExtension(this.getAbsolutePath());
    }

    @Override
    @Deprecated
    public String getContentType() {
        throw new UnsupportedOperationException("Método AINDA não suportado. Contate o responsável");
    }

    @Override
    @Deprecated
    public String getExtension() {
        return FilenameUtils.getExtension(this.getAbsolutePath());
    }

}
