/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * Classe que implementa o recurso de assinar o nome do arquivo com o MD5 do seu
 * conteúdo.
 *
 * A classe auxilia na escrita/leitura e exclusao do arquivo.
 *
 *
 * A classe vai assinar o arquivo no seguinte layout: nome123Arquivo-{MD5}.txt
 *
 *
 * Se já existir um arquivo já assinado, ou não, o mesmo será removido ao gravar
 * um novo.
 *
 *
 *
 * O getContent(), e afins, retornará o primeiro arquivo com md5 que encontrar.
 * Se já existir um arquivo, mas sem assinatura esse será retornado na
 * inexistência dos demais
 *
 *
 * O recurso de 'appendContent' não vai funcionar.
 *
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
public class CSPArquivosAssinados {

    private final static LinkedHashMap<CSPArquivos, String> lastPath = new LinkedHashMap<>();

    public static CSPArquivos getInstance() throws Exception {
        return getInstance(null);
    }

    public static CSPArquivos getInstance(String src) throws Exception {
        return new CSPArquivos(src) {

            @Override
            public boolean setBytes(byte[] bytes) throws java.lang.Exception {
                throw new UnsupportedOperationException("CSPArquivosAssinados não suporta o método :(");
            }

            @Override
            public byte[] getBytes() throws Exception {
                    String full = this.getAbsolutePath();
                for (File f : new File(FilenameUtils.getFullPath(src)).listFiles()) {
                    if (checkName(f, src)) {
                        this.setPath(f.getAbsolutePath());
                        return super.getBytes();
                    }
                }
                this.setPath(full);
                return null;
            }

            @Override
            public boolean delete() throws Exception {
                String full = this.getAbsolutePath();
                for (File f : new File(FilenameUtils.getFullPath(src)).listFiles()) {
                    if (checkName(f, src)) {
                        this.setPath(f.getAbsolutePath());
                        super.delete();
                    }
                }
                this.setPath(full);

                return true;
            }

            @Override
            public synchronized boolean setContent(String content, boolean onlyAppend) throws Exception {
                String full = this.getAbsolutePath();

                this.delete();//Deleta o atual, ou atuais ;)

                File f = File.createTempFile("foo", ".gold");

                FileUtils.writeStringToFile(f, content, Charset.forName("UTF-8"));
                String md5;
                try (InputStream in = new FileInputStream(f)) {
                    md5 = DigestUtils.md5Hex(in);
                }
                final String last = FilenameUtils.getFullPath(src) + FilenameUtils.getBaseName(src) + "-" + md5 + "." + FilenameUtils.getExtension(src);
                this.setPath(last);
                lastPath.put(this, last);
                f.delete();
                super.setContent(content, false);
                this.setPath(full);
                return true;
            }

            @Override
            public String getContent() throws Exception {
                String full = this.getAbsolutePath();
                for (File f : new File(FilenameUtils.getFullPath(src)).listFiles()) {
                    if (checkName(f, src)) {
                        this.setPath(f.getAbsolutePath());
                        return super.getContent();
                    }
                }
                this.setPath(full);
                return null;
            }

            @Override
            public String getMd5() throws Exception {
                String full = this.getAbsolutePath();
                for (File f : new File(FilenameUtils.getFullPath(src)).listFiles()) {
                    if (checkName(f, src)) {
                        this.setPath(f.getAbsolutePath());
                        return super.getMd5();
                    }
                }
                this.setPath(full);
                return null;
            }

        };
    }

    /**
     * Retorna o caminho absoluto do arquivo gerado por uma instancia de
     * CSPArquivosLocais gerada por esta classe
     *
     * @param ar CSPArquivosLocais - Instancia gerada por esta classe
     * @return
     */
    public static String getLastAbsolutePathFile(CSPArquivos ar) {
        return lastPath.get(ar);
    }

    /**
     * *
     * Verifica se a string é o arquivo em questão com o MD5, ou mesmo sem
     * nenhum MD5
     *
     * @return
     */
    private static boolean checkName(File src, String full) {
        String r = src.getAbsolutePath();
        if (r != null) {
            String bname1 = FilenameUtils.getBaseName(r);
            String bname2 = FilenameUtils.getBaseName(full);
            return src.isFile() && (bname1.startsWith(bname2 + "-") || bname1.startsWith(bname2 + "." + FilenameUtils.getExtension(r)) || bname1.equals(bname2));
        }
        return false;
    }

}
