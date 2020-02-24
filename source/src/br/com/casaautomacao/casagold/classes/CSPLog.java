/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.exit;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.lcfirst;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.ucfirst;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime.getTempoCompletoLimpo;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;

/**
 * Classe responsável pelos logs do sistema. <b style="color:#ff0000">Uso
 * restrito</b>
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 07/10/2015 - 16:57:09
 */
public abstract class CSPLog {

    private static CSPArquivos CURRENT_LOG;

    public static CSPArquivos getCURRENT_LOG() {
        return CURRENT_LOG;
    }

    /**
     * Método para reiniciar o arquivo de LOG. Finaliza o arquivo de LOG atual e
     * cria um novo.
     *
     */
    public static void reiniciaLog() {
        try {
            CURRENT_LOG = new CSPArquivos();
            CURRENT_LOG.setPath(CURRENT_LOG.getAbsolutePath() + "/logs/" + getTempoCompletoLimpo() + ".log");
            CURRENT_LOG.setContent("");
        } catch (Exception ex) {
            System.err.println("Problema classe de log");
            System.err.println(ex);
            System.err.println("Sistema finalizando...");
            exit();
        }
    }

    static {
        CSPLog.reiniciaLog();
    }

    /**
     * Grava o log
     *
     * @param t String - Mensagem a ser gravada
     */
    protected static synchronized void registra(String t) {
        try {
            double calcMemory = -1;
            long maxM = Runtime.getRuntime().maxMemory();
            if (maxM > 0) {
                long useM = Runtime.getRuntime().totalMemory();
                calcMemory = (useM * 100) / maxM;
            }

            String r = getTempoCompletoLimpo() + "(" + calcMemory + "%)>" + t;
            r = r.trim();
            if (r.contains(">Erro")) {
                //Erro é indiferente ao 'show'
                System.err.println(CSPUtilidadesLang.substring(r, 6, r.length()));
                System.err.flush();
            } else {
                System.out.println(CSPUtilidadesLang.substring(r, 6, r.length()));

            }
            System.err.flush();
            System.out.flush();
            CURRENT_LOG.appendContent(r + LINE_SEPARATOR);

            //Inicia um novo arquivo de log após o arquivo atual atingir 1mb
            if ((CURRENT_LOG.length() / 1024) > 5120) {
                CSPLog.reiniciaLog();
            }

        } catch (Exception e) {
            CSPException.register(e);
        }
    }

    /**
     * Cria a primeira linha de log do sistema
     *
     * @param padrao
     * @throws java.lang.Exception
     */
    public static void startLog(Class padrao) throws Exception {
        String more = "";

        String tmp = CSPUtilidadesApplication.getPathJar();
        if (tmp != null) {
            more += " EXE:" + tmp;
        }

        tmp = CSPUtilidadesApplication.getMd5Jar();
        if (tmp != null) {
            more += " MD5:" + tmp;
        }

        info("Iniciando... PID:" + CSPUtilidadesApplication.getPID() + " IP:" + CSPUtilidadesLangRede.getLocalIp() + " MAC:" + CSPUtilidadesLangRede.getMac() + "" + more);
    }

    /**
     * Log semi-detalhado.
     *
     * Indicado para realizar o log de processos não tão simples, como por
     * exemplo um for, percorrendo um arquivo
     *
     * @param form Class - Formulário ou Módulo em uso
     * @param info String - Informação sobre o processo
     */
    public static void info(Class form, String info) {
        registra(trataClass(form) + ">" + ucfirst(info).trim());
    }

    private static String trataClass(Class form) {

        String n = form.getName();
        int firstChar;
        firstChar = n.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            n = n.substring(firstChar);
        }
        return n;
    }

    /**
     * Log simples
     *
     * @param info String - String a ser registrada
     */
    public static void info(String info) {
        registra(info);
    }

    /**
     * Log simples 2
     *
     * @param info Object - Object a ser registrado
     */
    public static void info(Object info) {
        registra(info.toString());
    }
    /**
     * Conta a quantidade de comandos SQL realizada
     */
    private static int countSql = 1;

    /**
     * Log relacionado a classe SQL. <b style="color:#ff0000">Uso restrito!!</b>
     *
     * @param cls CSPInstrucoesSQL - Classe
     * @param sql String - sql realizado
     */
    public static synchronized void info(CSPInstrucoesSQLBase cls, String sql) {

        registra("SQL[" + countSql + "]>" + sql + ". DB:" + cls.getConfs().getSrcDriverJdbc());
        ++countSql;
    }

    /**
     * Log de <b style="color:#f00">erro</b>
     *
     * @param ms String - Mensagem
     */
    public static void error(String ms) {
        registra("Erro, " + lcfirst(ms));
    }

    /**
     * Log de <b style="color:#f00">erro</b> semi-detalhado
     *
     * @param form Class - Formulário ou Módulo em uso
     * @param info String - Informação sobre o processo
     */
    public static void error(Class form, String info) {
        registra("Erro, " + trataClass(form) + ">" + ucfirst(info).trim());

    }
}
