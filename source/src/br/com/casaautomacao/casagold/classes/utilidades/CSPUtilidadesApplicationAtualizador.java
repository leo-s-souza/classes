/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Métodos de auxilio para recursos restritos ao processo de atualização
 * CasaGold.
 *
 * @author Matheus Felipe Amelco <producao5@casaatuomacao.com.br>
 * @date 13/12/2016 - 09:53:43
 */
public abstract class CSPUtilidadesApplicationAtualizador extends CSPUtilidadesApplication {

    /**
     * Realiza o backup de arquivos.
     *
     * @param dest CSPArquivos - Local para onde os arquivos serão backupeados.
     * @param arquivos String[] - Arquivos que serão backupeados.
     * @throws Exception
     */
    public static void backupArquivos(CSPArquivos dest, String[] arquivos) throws Exception {
        if (dest != null && arquivos != null) {

            for (String file : arquivos) {
                CSPArquivos arquivoOriginal = new CSPArquivos(file);
                CSPArquivos arquivoBackup = new CSPArquivos(file.replace(PATH, dest.getAbsolutePath()));

                if (arquivoOriginal.exists()) {
                    if (arquivoBackup.exists()) {
                        //Se o backup já existir, substituimos o antigo.
                        arquivoOriginal.replace(arquivoBackup);
                    } else {
                        arquivoOriginal.copy(arquivoBackup);
                    }
                }
            }
        }
    }
    /**
     * Retorna a lista de todas as bases existentes no caminho informado exceto
     * nas pastas de backup e atualização.
     *
     * @param pastaSistema String - Pasta do sistema
     * @return
     * @throws java.lang.Exception
     */
    public static ArrayList<CSPArquivos> encontraBases(String pastaSistema) throws Exception {

        CSPArquivos pastaProcurar = new CSPArquivos(pastaSistema);

        ArrayList<CSPArquivos> resultado = new ArrayList<>();

        CSPUtilidadesApplicationAtualizador.encontraArquivos(pastaProcurar, ".fdb", null, new String[]{"contratantes", "infos-app", "atualizacao", "atualizacoes", "sped fiscal", "adm_gold", "backup", "atualizacao_temp", "temp"})
                .stream().forEach((caminhoArquivo) -> {
                    try {
                        resultado.add(new CSPArquivos(caminhoArquivo));
                    } catch (Exception ex) {
                        CSPException.register(ex);
                    }
                });

        return resultado;
    }

    private static String srcUltimaPastaCache;
    private static LinkedHashSet<String> filesCache;

    /**
     *
     * @param src CSPArquivos -recebe a conexao com a pasta do sistema.
     * @param nomeParcial String - deve receber parte do nome dos arquivos que
     * devem ser encontrados
     * @return
     * @throws Exception
     */
    private static ArrayList<String> encontraArquivos(CSPArquivos src, String nomeParcial, String[] ignoraFiles, String[] ignoraDirs) throws Exception {
        if (!src.getAbsolutePath().equals(srcUltimaPastaCache)) {
            srcUltimaPastaCache = src.getAbsolutePath();

            CSPUtilidadesLangArquivos.listRecursive(new CSPArquivos(src.getAbsolutePath()), filesCache = new LinkedHashSet<>(), ignoraFiles, ignoraDirs);
        }

        ArrayList<String> resultado = new ArrayList<>();

        filesCache.stream().forEach((file) -> {
            if (nomeParcial == null || nomeParcial.trim().equals("")) {
                resultado.add(file);
            } else {
                if (file.contains(nomeParcial)) {
                    resultado.add(file);
                }
            }
        });

        return resultado;
    }

}
