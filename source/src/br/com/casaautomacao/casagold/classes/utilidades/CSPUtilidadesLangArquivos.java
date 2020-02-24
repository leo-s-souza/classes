/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.DIR_SEPARATOR;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Métodos de auxilio para linguagem no contexto de arquivos
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 27/10/2016 - 17:13:17
 */
public abstract class CSPUtilidadesLangArquivos extends CSPUtilidadesLang {

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
     * @param path
     * @return String
     * @throws java.lang.Exception
     */
    public static String getPrefixAdvanced(String path) throws Exception {
        StringBuilder r = new StringBuilder();
        for (String p : path.replaceFirst("smb://", "").replace("\\", "/").split("/")) {
            if (!p.isEmpty()) {

                if (CSPUtilidadesLangRede.isHostAddress(p)) {

                    r.append("//");
                    r.append(p);
                    continue;
                }
                r.append("/");
                r.append(p);
                break;
            }
        }

        return r.toString();
    }

    /**
     * O método percorre a pasta a procura do arquivo/pasta a ser fitrado,
     * estando ele na raiz, ou em subpastas.
     *
     * @param src - Pasta inicial para procura.
     * @param filtro String - Valor usado para filtrar os resultados, por
     * exemplo o nome.
     * @param filtroIgnorar String[] - Valores que devem ser ignorados.
     * @param filtroIsFile boolean - Determina se é procurado um arquivo ou
     * diretório.
     * @param isCaseInsensitive - Define se a comparação com o arquivo deve ser
     * CaseInsensitive
     * @throws Exception
     */
    public static String findFile(CSPArquivos src, String filtro, String[] filtroIgnorar, boolean filtroIsFile, boolean isCaseInsensitive) throws Exception {
        final StringBuilder r = new StringBuilder();
        findFile(src, filtro, filtroIgnorar, filtroIsFile, isCaseInsensitive, r);
        if (r.length() == 0) {
            return null;
        }
        return r.toString();
    }

    /**
     * O método percorre a pasta a procura do arquivo/pasta a ser fitrado,
     * estando ele na raiz, ou em subpastas.
     *
     * @param src - Pasta inicial para procura.
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
    public static void findFile(CSPArquivos src, String filtro, String[] filtroIgnorar, boolean filtroIsFile, boolean isCaseInsensitive, StringBuilder resultado) throws Exception {

        if (filtroIgnorar == null) {
            filtroIgnorar = new String[0];
        }

        if (src.isDir()) {

            for (CSPArquivos dir : src.listFiles()) {

                if (resultado.length() == 0) {

                    boolean ignorar = false;

                    if (filtroIgnorar != null) {
                        for (String filtroIgnorar1 : filtroIgnorar) {
                            if (dir.getAbsolutePath().endsWith(filtroIgnorar1)) {
                                ignorar = true;
                                break;
                            }
                        }
                    }

                    if (!ignorar) {
                        if (!filtroIsFile) {
                            if (dir.getAbsolutePath().endsWith(filtro)) {
                                resultado.append(dir.getAbsolutePath());
                            }
                        } else {
                            findFile(dir, filtro, filtroIgnorar, filtroIsFile, isCaseInsensitive, resultado);
                        }
                    }
                } else {
                    break;
                }
            }
        } else {
            if (resultado.length() == 0) {
                boolean ignorar = false;
                for (String filtroIgnorar1 : filtroIgnorar) {
                    if (src.getAbsolutePath().endsWith(filtroIgnorar1)) {
                        ignorar = true;
                        break;
                    }
                }

                if (!ignorar) {
                    if (filtroIsFile) {
                        if (isCaseInsensitive) {
                            if (src.getName().equalsIgnoreCase(filtro)) {
                                resultado.append(src.getAbsolutePath());
                            }
                        } else {
                            if (src.getName().equals(filtro)) {
                                resultado.append(src.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Lista os arquivos de uma pasta e suas subpastas.
     *
     * @param src CSPArquivos - Pasta que deve ser varrida.
     * @param ignoraFiles String[] - Lista com o nome de arquivos que devem ser
     * ignorados.
     * @return
     * @throws Exception
     */
    public static String[] listRecursive(CSPArquivos src, String[] ignoraFiles) throws Exception {
        final LinkedHashSet<String> files = new LinkedHashSet<>();

        if (src.isDir()) {
            CSPUtilidadesLangArquivos.listRecursive(src, files, ignoraFiles);
        }

        return files.toArray(new String[files.size()]);
    }

    /**
     * Preenche um LinkedHashSet com todos os arquivos de uma pasta e suas
     * subpastas.
     *
     * @param src CSPArquivos - Pasta que deve ser varrida.
     * @param files LinkedHashSet - Set que é preenchido com os caminhos
     * absolutos dos arquivos encontrados.
     * @param ignoraFiles String[] - Lista com o nome de arquivos que devem ser
     * ignorados.
     * @param ignoraPastas String[] - Lista com o nome de pastas que devem ser
     * ignoradas.
     * @throws Exception
     */
    public static void listRecursive(CSPArquivos src, LinkedHashSet<String> files, String[] ignoraFiles, String[] ignoraPastas) throws Exception {
        if (src.exists()) {
            boolean ignora = false;

            if (src.isDir()) {
                String old = src.getAbsolutePath();

                for (String dir : src.list()) {
                    boolean ignoraSrc = false;

                    if (ignoraPastas != null) {
                        for (String ignoraFile : ignoraPastas) {
                            if (dir.toLowerCase().equals(ignoraFile.toLowerCase())) {
                                ignoraSrc = true;
                                break;
                            }
                        }
                    }

                    if (!ignoraSrc) {
                        src.setPath(old + "/" + dir);
                        listRecursive(src, files, ignoraFiles);
                    }
                }

            } else {
                if (ignoraFiles != null) {
                    for (String ignoraFile : ignoraFiles) {
                        if (src.getBaseName().equals(ignoraFile)) {
                            ignora = true;
                            break;
                        } else if (src.getName().equals(ignoraFile)) {
                            ignora = true;
                            break;
                        }
                    }
                }

                if (!ignora) {
                    files.add(src.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Preenche um LinkedHashSet com todos os arquivos de uma pasta e suas
     * subpastas.
     *
     * @param src CSPArquivos - Pasta que deve ser varrida.
     * @param files LinkedHashSet - Set que é preenchido com os caminhos
     * absolutos dos arquivos encontrados.
     * @param ignoraFiles String[] - Lista com o nome de arquivos que devem ser
     * ignorados.
     * @throws Exception
     */
    public static void listRecursive(CSPArquivos src, LinkedHashSet<String> files, String[] ignoraFiles) throws Exception {
        CSPUtilidadesLangArquivos.listRecursive(src, files, ignoraFiles, null);
    }

    /**
     * Gera um zip dos arquivos recebidos por parâmetro
     *
     * @param dest CSPArquivos - Arquivo a ser gerado
     * @param src String[] - Arquivos a serem adicionados ao ZIP
     * @return
     * @throws java.lang.Exception
     */
    public static boolean zipFiles(CSPArquivos dest, String... src) throws Exception {
        final CSPArquivos[] srcs = new CSPArquivos[src.length];
        for (int i = 0; i < src.length; i++) {
            srcs[i] = new CSPArquivos(src[i]);

        }

        return zipFiles(dest, srcs);
    }

    /**
     * Gera um zip dos arquivos recebidos por parâmetro
     *
     * @param dest CSPArquivos - Arquivo a ser gerado
     * @param src CSPArquivos[] - Arquivos a serem adicionados ao ZIP
     * @return
     * @throws java.lang.Exception
     */
    public static boolean zipFiles(CSPArquivos dest, CSPArquivos... src) throws Exception {

        byte[] buffer = new byte[1024];
        final ZipOutputStream zout = new ZipOutputStream(dest.objFileOutputStream());

        for (CSPArquivos file : src) {
            if (file.isFile()) {
                final String name = file.getName();

                if (file.isRemote()) {
                    final String tmp = CSPUtilidadesSO.PATH_TEMP + "/tmp-aux-zip-gold-" + name;
                    file.replace(tmp);
                    file.setPath(tmp);
                }

                final FileInputStream fin = file.objFileInputStream();
                zout.putNextEntry(new ZipEntry(name));

                int length;
                while ((length = fin.read(buffer)) > 0) {
                    zout.write(buffer, 0, length);
                }

                zout.closeEntry();
                fin.close();
            }
        }
        zout.close();

        return true;

    }

    /**
     * Descompacta todos os arquivos, e somente arquivos, contidos na raiz do
     * zip
     *
     * @param destino CSPArquivos - Destino. Precisa ser uma pasta válida!
     * @param zip CSPArquivos - Arquivo a ser descompactado
     * @return
     * @throws java.lang.Exception
     */
    public static boolean unzipFiles(CSPArquivos destino, CSPArquivos zip) throws Exception {
        if (!destino.exists()) {
            destino.mkdirs();
        }
        if (!destino.exists() || !destino.isDir()) {
            return false;
        }

        ZipFile zipFile = new ZipFile(zip.getAbsolutePath());
        Enumeration<?> enu = zipFile.entries();
        while (enu.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) enu.nextElement();

            if (zipEntry.isDirectory()) {
                continue;
            }

            File file = new File(destino.getAbsolutePath() + DIR_SEPARATOR + zipEntry.getName());

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

}
