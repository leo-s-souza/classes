/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

/**
 *
 * @author cautomacao
 */
@Deprecated
public interface InterfaceCSPArquivos {

    /**
     * Retorna qual é o formato padrão de arquivos da classe
     *
     * @return String - Caso seja * entende-se que é aceito todo o tipo
     */
    @Deprecated
    public String getFormatDefault();

    /**
     * Define um caminho absoluto
     *
     * @param path String - Caminho absoluto
     */
    @Deprecated
    public void setPath(String path);

    /**
     * Retorna o caminho
     *
     * @return String
     */
    @Deprecated
    public String getPath();

    /**
     * Define o nome de uma pasta ou arquivo com base no "path", caminho
     * absoluto.
     *
     * @param name String - Nome do arquivo/basta. Com base no path
     */
    @Deprecated
    public void setName(String name);

    /**
     * Retorna o nome do arquivo ou pasta em questão.
     *
     * @return boolean
     */
    @Deprecated
    public String getName();

    /**
     * Retorna o caminho absoluto
     *
     * @return String
     */
    @Deprecated
    public String getAbsolutePath();

    /**
     * Valida a existência do caminho.
     *
     * @return boolean
     */
    @Deprecated
    public boolean exists() throws Exception;

    /**
     * Valida se o caminho é um arquivo.
     *
     * @return boolean
     */
    @Deprecated
    public boolean isFile() throws Exception;

    /**
     * Valida se o caminho é uma pasta.
     *
     * @return boolean
     */
    @Deprecated
    public boolean isDir() throws Exception;

    /**
     * Retorna o tamanho do arquivo.
     *
     * @return long
     */
    @Deprecated
    public long length() throws Exception;

    /**
     * Renomeia o caminho
     *
     * @param para String - Caminho absoluto do destino
     *
     * @return boolean
     */
    @Deprecated
    public boolean rename(String para) throws Exception;

    /**
     * Cria os diretórios e subdiretórios do caminho setado.
     *
     * @return boolean
     */
    @Deprecated
    public boolean mkdirs() throws Exception;

    /**
     * Realiza a cópia do caminho.
     *
     * @param destino String - Caminho destino do arquivo
     *
     * @return boolean
     */
    @Deprecated
    public boolean copy(String destino) throws Exception;

    /**
     * Realiza a exclusão do caminho.
     *
     * @return boolean
     */
    @Deprecated
    public boolean delete() throws Exception;

    /**
     * Define o conteúdo do arquivo.
     *
     * @param content String - Conteúdo a ser gravado
     *
     * @return boolean
     */
    @Deprecated
    public boolean setContent(String content) throws Exception;

    /**
     * Define o conteúdo do arquivo, em bytes.
     *
     * @param bytes byte - Conteúdo a ser gravado
     *
     * @return boolean
     */
    @Deprecated
    public boolean setBytes(byte[] bytes) throws Exception;

    /**
     * Retorna o conteúdo do arquivo.
     *
     * @return String
     */
    @Deprecated
    public String getContent() throws Exception;

    /**
     * Retorna o conteúdo do arquivo, em bytes.
     *
     * @return String
     */
    @Deprecated
    public byte[] getBytes() throws Exception;

    /**
     * Lista os arquivos e pastas do diretório em ordem crescente
     *
     * @return String
     */
    @Deprecated
    public String[] list() throws Exception;

    /**
     * Retorna o MD5 do arquivo
     *
     * @return String
     */
    @Deprecated
    public String md5() throws Exception;

    /**
     * Retorna o formato do arquivo
     *
     * @return String
     */
    @Deprecated
    public String getFormat() throws Exception;

    /**
     * Retorna o content type (mime-type) do arquivo
     *
     * @return String
     */
    @Deprecated
    public String getContentType() throws Exception;

    /**
     * Retorna a extensão do arquivo
     *
     * @return String
     */
    @Deprecated
    public String getExtension();

}
