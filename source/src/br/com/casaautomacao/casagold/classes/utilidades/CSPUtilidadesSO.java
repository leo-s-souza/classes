/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.SuperRunneable;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.getMd5;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime.getTempoCompleto;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.WinRegistry.RegStatusWindows;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * Métodos de auxilio para o sistema operacional
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 04/04/2016 - 14:53:43
 */
public abstract class CSPUtilidadesSO {

    /**
     * Caminho da pasta temporaria do SO
     */
    public final static String PATH_TEMP = System.getProperty("java.io.tmpdir");

    /**
     * Caminho da pasta "home" do usuário
     */
    public final static String PATH_HOME = System.getProperty("user.home");

    /**
     * "/" de separação de diretórios
     */
    public final static String DIR_SEPARATOR = File.separator;

    /**
     * String para quebra de linha do SO
     */
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");

    public enum SO {

        SO_NAO_SUPORTADO,
        SO_WINDOWS,
        SO_LINUX
    }

    /**
     * Retorna qual é o sistema operacional
     *
     * @return int - <b style="color:#ff0000">Use as constantes</b>:
     * <br/>
     * <b>SO_NAO_SUPORTADO</b>: Sistema operacional nao suportado
     * <br/>
     * <b>SO_WINDOWS</b>: Sistema operacional windows
     * <br/>
     * <b>SO_LINUX</b>: Sistema operacional linux
     */
    public static SO getSO() {
        String os = System.getProperty("os.name").toLowerCase();
        //Windows
        if (os.contains("win")) {
            return SO.SO_WINDOWS;
        }
        //Linux
        if (os.contains("nux") || os.contains("nix") || os.contains("aix")) {
            return SO.SO_LINUX;
        }
        /* //Mac
         if (os.contains("mac")) {
         }
         //Solaris
         if (os.contains("sunos")) {
         }*/
        return SO.SO_NAO_SUPORTADO;
    }

    /**
     * Executa o fonte somente no Sistema operacional windows
     *
     * @param run SuperRunneable - Ação a ser executada
     * @throws java.lang.Exception
     */
    public static void runOnlyInWindows(SuperRunneable run) throws Exception {
        if (getSO() == SO.SO_WINDOWS) {
            run.run();
        }
    }

    /**
     * Executa o fonte somente no Sistema operacional linux
     *
     * @param run SuperRunneable - Ação a ser executada
     * @throws java.lang.Exception
     */
    public static void runOnlyInLinux(SuperRunneable run) throws Exception {
        if (getSO() == SO.SO_LINUX) {
            run.run();
        }
    }

    /**
     * Executa o fonte somente em sistemas não suportados
     *
     * @param run SuperRunneable - Ação a ser executada
     * @throws java.lang.Exception
     */
    public static void runOnlyInOthers(SuperRunneable run) throws Exception {
        if (getSO() == SO.SO_NAO_SUPORTADO) {
            run.run();
        }
    }

    /**
     * Executa um arquivo jar conforme o SO
     *
     * @param src String - Caminho do arquivo
     * @param parms String[] - Argumentos adicionais para o jar
     * @throws IOException
     */
    public static void startJar(String src, String... parms) throws Exception {
        final String command[] = new String[3 + parms.length];
        command[0] = isSoWindows() ? "java.exe" : "java";
        command[1] = "-jar";
        command[2] = src;

        for (int i = 3; i < command.length; i++) {
            command[i] = parms[i - 3];
        }

        runProcessInSo(command);

    }

    /**
     * Tenta 'matar' um processo pelo seu ID
     *
     * @param id long - ID do processo
     * @throws IOException
     */
    public static void kill(long id) throws IOException {
        String command = "kill -9";
        if (isSoWindows()) {
            command = "taskkill /F /PID";
        }
        runCommandInSo(command + " " + id);
    }

    /**
     * Tenta 'matar' um processo pelo nome do seu 'executável'
     *
     * @param name String - Nome do processo
     * @throws IOException
     */
    public static void killall(String name) throws IOException {
        if (isSoWindows()) {
            runCommandInSo("taskkill /im " + name + " /f");
        } else {
            runCommandInSo("killall -9 " + name);
        }
    }

    /**
     * Retorna uma lista contendo o PID e os parametros de cada processo.
     *
     * @param name String - Nome do programa/executavel a ser filtrado
     * @return Object[][] - Onde: 0 => PID | 1 => Argumentos
     * @throws Exception
     */
    public static Object[][] getListPidAndParametersByName(String name) throws Exception {

        final ArrayList<Object[]> r = new ArrayList<>();

        if (isSoWindows()) {
            /**
             * Inspirado em
             * http://superuser.com/questions/683021/how-to-get-the-command-that-invoked-a-task-with-tasklist
             */
            Process runProcessInSo = CSPUtilidadesSO.runProcessInSo("wmic", "process", "where", "caption=\"" + name + "\"", "get", "commandline,processid", "/format:csv");
            final String[] output = CSPUtilidadesSO.getOutputFromProcess(runProcessInSo).split(CSPUtilidadesSO.LINE_SEPARATOR);

            for (String o : output) {

                if (!o.trim().isEmpty()) {

                    String[] spl = o.split(",");
                    if (spl.length == 3) {
                        spl[2] = spl[2].replaceAll("[^0123456789]", "").trim();
                        if (spl[2].isEmpty()) {
                            continue;
                        }
                        r.add(new Object[]{
                            new Long(spl[2]),//PID
                            spl[1].trim()//Argumentos
                        });
                    }
                }
            }

        } else {
            /**
             * No linux bastava o ps -eAo "%p,%c,%a" que teriamos todas as
             * informaçoes de uma unica vez, mas aparentemente o java nao
             * conseguiu ler a linha por completo, perdendo assim informaçoes
             * dos parametros.
             */
            Process runProcessInSo = CSPUtilidadesSO.runProcessInSo("ps", "-eAo", "\"%p,%c\"");

            final String[] output = CSPUtilidadesSO.getOutputFromProcess(runProcessInSo).split(CSPUtilidadesSO.LINE_SEPARATOR);

            for (String o : output) {
                o = o.replace("\"", "");
                if (!o.trim().isEmpty()) {
                    String[] spl = o.split(",");

                    if (spl.length == 2) {
                        if (!spl[1].trim().equals(name)) {
                            continue;
                        }

                        //PID
                        spl[0] = spl[0].replaceAll("[^0123456789]", "").trim();

                        if (spl[0].isEmpty()) {
                            continue;
                        }

                        final String catResult = CSPUtilidadesSO.getOutputFromProcess(
                                CSPUtilidadesSO.runProcessInSo("cat", "/proc/" + spl[0] + "/cmdline")
                        ).replace(CSPUtilidadesSO.LINE_SEPARATOR, "").trim();

                        r.add(new Object[]{
                            new Long(spl[0]),//PID
                            catResult//Argumentos
                        });

                    }
                }
            }
        }

        if (r.isEmpty()) {
            return new Object[0][0];
        }

        return r.toArray(new Object[r.size()][]);
    }

    /**
     * Atalho que retorna se é um SO Windows :(
     *
     * @return
     */
    public static boolean isSoWindows() {
        return getSO() == SO.SO_WINDOWS;
    }

    /**
     * Atalho que retorna se é um SO Linux :)
     *
     * @return
     */
    public static boolean isSoLinux() {
        return getSO() == SO.SO_LINUX;
    }

    /**
     * Atalho que retorna se é um SO não suportado
     *
     * @return
     */
    public static boolean isSoNaoSuportado() {
        return getSO() == SO.SO_NAO_SUPORTADO;
    }

    /**
     * Executa de forma temporária um script .sh no linux.
     *
     * @param script String - Conteúdo
     * @return Process
     * @throws IOException
     */
    public static Process runShellScriptInLinux(String script) throws Exception {
        if (!isSoLinux()) {
            return null;
        }
        final CSPArquivos tmp = new CSPArquivos(CSPUtilidadesSO.PATH_TEMP + "/gold-shell-" + getTempoCompleto() + ".sh");
        tmp.setContent(script);

        return CSPUtilidadesSO.runCommandInSo("/bin/bash " + tmp.getAbsolutePath());

    }

    /**
     * Executa um arquivo bat
     *
     * @param src String - Caminho do arquivo
     * @return Process
     * @throws IOException
     */
    public static Process runBatchFileInWindows(String src) throws Exception {
        return runBatchFileInWindows(src, false);
    }

    /**
     * Executa um arquivo bat
     *
     * @param src String - Caminho do arquivo
     * @param requestAdminAccess boolean - Se deve ser executado requisitando a
     * permissao do admin
     * @return Process
     * @throws IOException
     */
    public static Process runBatchFileInWindows(String src, boolean requestAdminAccess) throws Exception {
        if (!isSoWindows()) {
            return null;
        }
//        String srcCall = src;
//        if (requestAdminAccess) {
//
//            final File file = File.createTempFile("gold-call-admin-", ".bat");
//            final String fileGetAdmin = "get-admin-" + CSPUtilidadesLang.getTempoCompletoLimpo() + ".vbs";
//            FileWriter fw = new java.io.FileWriter(file);
//            fw.write("@echo off " + LINE_SEPARATOR
//                    + ":-------------------------------------" + LINE_SEPARATOR
//                    + "IF \"%PROCESSOR_ARCHITECTURE%\" EQU \"amd64\" ( " + LINE_SEPARATOR
//                    + "     >nul 2>&1 \"%SYSTEMROOT%\\SysWOW64\\cacls.exe\" \"%SYSTEMROOT%\\SysWOW64\\config\\system\" " + LINE_SEPARATOR
//                    + ") ELSE ( " + LINE_SEPARATOR
//                    + "     >nul 2>&1 \"%SYSTEMROOT%\\system32\\cacls.exe\" \"%SYSTEMROOT%\\system32\\config\\system\" " + LINE_SEPARATOR
//                    + ") " + LINE_SEPARATOR
//                    + "if '%errorlevel%' NEQ '0' ( " + LINE_SEPARATOR
//                    + "    goto UACPrompt " + LINE_SEPARATOR
//                    + ") else ( goto gotAdmin ) " + LINE_SEPARATOR
//                    + ":UACPrompt " + LINE_SEPARATOR
//                    + "    echo Set UAC = CreateObject^(\"Shell.Application\"^) > \"%temp%\\" + fileGetAdmin + "\" " + LINE_SEPARATOR
//                    + "    set params = %*:\"=\"\" " + LINE_SEPARATOR
//                    + "    echo UAC.ShellExecute \"cmd.exe\", \"/c \"\"%~s0\"\" %params%\", \"\", \"runas\", 0 >> \"%temp%\\" + fileGetAdmin + "\" " + LINE_SEPARATOR
//                    + "    \"%temp%\\" + fileGetAdmin + "\" " + LINE_SEPARATOR
//                    + "    del \"%temp%\\" + fileGetAdmin + "\" " + LINE_SEPARATOR
//                    + "    exit /B " + LINE_SEPARATOR
//                    + ":gotAdmin " + LINE_SEPARATOR
//                    + "    pushd \"%CD%\" " + LINE_SEPARATOR
//                    + "    CD /D \"%~dp0\" " + LINE_SEPARATOR
//                    + ":-------------------------------------" + LINE_SEPARATOR
//                    + new CSPArquivosLocais(src).getContent());
//            fw.close();
//            srcCall = file.getAbsolutePath();
//        }
        if (requestAdminAccess) {
            return runVisualBasicScriptInWindows(
                    "Set UAC = CreateObject(\"Shell.Application\")" + LINE_SEPARATOR
                    + "UAC.ShellExecute \"" + src + "\", \"\", \"\", \"runas\", 1"
            );
        }
        return runProcessInSo("cmd", "/c", "start", "/b", src);
    }

    /**
     * Executa o comando no SO. Para conseguir uma string com a resposta do
     * processo use: {@link CSPUtilidadesSO#getOutputFromProcess(java.lang.Process)
     * }
     *
     * @param comand String - Comando
     * @return Process
     * @throws IOException
     */
    public static Process runCommandInSo(String comand) throws IOException {
        CSPLog.info("Command '" + comand + "' sended to SO");
        return Runtime.getRuntime().exec(comand);
    }

    /**
     * Executa um processo no SO. Para conseguir uma string com a resposta do
     * processo use: {@link CSPUtilidadesSO#getOutputFromProcess(java.lang.Process)
     * }
     *
     * @param comand String... - Comando
     * @return Process
     * @throws Exception
     */
    public static Process runProcessInSo(String... comand) throws Exception {
        CSPLog.info("Process '" + String.join(" ", comand) + "' sended to SO");
        ProcessBuilder pb = new ProcessBuilder(comand);

        //Leia: https://pt.wikipedia.org/wiki/Fluxos_padrão
        pb.redirectErrorStream(true);

        return pb.start();
    }

    /**
     * Retorna uma string do resultado do de um Process
     *
     * @param process Process - Processo a ser usado
     * @return String - Resposta do processo
     *
     */
    public static String getOutputFromProcess(Process process) throws IOException, InterruptedException {

        String s;
        final StringBuilder r = new StringBuilder();
        final BufferedReader stdInput = new BufferedReader(
                new InputStreamReader(
                        process.getInputStream()
                )
        );

        process.waitFor();

        while ((s = stdInput.readLine()) != null) {
            r.append(s);
            r.append(CSPUtilidadesSO.LINE_SEPARATOR);
        }

        return r.toString();
    }

    /**
     * Executa de forma temporária um script .bat no windows. Após a execução o
     * java vai tentar excluir o .bat temporário.
     *
     * @param script String - Script a ser executado
     * @return Process
     * @throws Exception
     */
    public static Process runBatchScriptInWindows(String script) throws Exception {
        return runBatchScriptInWindows(script, false);
    }

    /**
     * Executa de forma temporária um script .bat no windows. Após a execução o
     * java vai tentar excluir o .bat temporário.
     *
     * @param script String - Script a ser executado
     * @param requestAdminAccess boolean - requisita permissão do admin
     * @return Process
     * @throws Exception
     */
    public static Process runBatchScriptInWindows(String script, boolean requestAdminAccess) throws Exception {
        if (!isSoWindows()) {
            return null;
        }
        File file = File.createTempFile(getMd5(script) + getTempoCompleto(), ".bat");
        FileWriter fw = new java.io.FileWriter(file);
        fw.write(script);
        fw.close();
        return runBatchFileInWindows(file.getAbsolutePath(), requestAdminAccess);

    }

    /**
     * Executa um arquivo vbs
     *
     * @param src String - Caminho do arquivo
     * @return Process
     * @throws IOException
     */
    public static Process runVisualBasicFileInWindows(String src) throws Exception {
        if (!isSoWindows()) {
            return null;
        }
        return runProcessInSo("cscript", "/nologo", src);
    }

    /**
     * Executa de forma temporária um script .vbs no windows. Após a execução o
     * java vai tentar excluir o .vbs temporário.
     *
     * @param script String - Script a ser executado
     * @return
     * @throws Exception
     */
    public static Process runVisualBasicScriptInWindows(String script) throws Exception {
        if (!isSoWindows()) {
            return null;
        }
        File file = File.createTempFile(getMd5(script) + getTempoCompleto(), ".vbs");
        FileWriter fw = new java.io.FileWriter(file);
        fw.write(script);
        fw.close();
        return runVisualBasicFileInWindows(file.getAbsolutePath());
    }

    /**
     * Cria um atalho para uma pasta/arquivo no windows
     *
     * @param target String - Arquivo/pasta do qual será criado o atalho
     * @param dest String - Caminho de destino do atalho
     * @param workDir String - Diretório de execução
     * @param icon String - Caminho do icone do atalho
     * @param description String - Descrição do atalho
     * @throws java.lang.Exception
     */
    public static void makeShortcutIconInWindows(String target, String dest, String workDir, String icon, String description) throws Exception {
        if (!isSoWindows()) {
            return;
        }
        File tmp = File.createTempFile("gold-shortcut-", ".lnk");

        String vbs = "Set oWS = WScript.CreateObject(\"WScript.Shell\")" + LINE_SEPARATOR;
        vbs += "sLinkFile = \"" + tmp.getAbsolutePath() + "\"" + LINE_SEPARATOR;
        vbs += "Set oLink = oWS.CreateShortcut(sLinkFile)" + LINE_SEPARATOR;
        vbs += "oLink.TargetPath = \"" + target + "\"" + LINE_SEPARATOR;
        if (icon != null && !icon.trim().isEmpty()) {
            vbs += "oLink.IconLocation = \"" + icon + "\"" + LINE_SEPARATOR;
        }
        vbs += "oLink.Description = \"" + description + "\"" + LINE_SEPARATOR;
        vbs += "oLink.WorkingDirectory = \"" + workDir + "\"" + LINE_SEPARATOR;
        vbs += "oLink.Save" + LINE_SEPARATOR;
        vbs += "sTarget = \"" + dest + "\"" + LINE_SEPARATOR;
        vbs += "Set fso = CreateObject(\"Scripting.FileSystemObject\")" + LINE_SEPARATOR;
        vbs += "fso.CopyFile sLinkFile, sTarget " + LINE_SEPARATOR;
        runVisualBasicScriptInWindows(vbs).waitFor();

    }

    /**
     * Cria um atalho para uma pasta/arquivo no windows
     *
     * @param target String - Arquivo/pasta do qual será criado o atalho
     * @param name String - Nome do atalho a ser criado na area de trabalho
     * @param workDir String - Diretório de execução
     * @param icon String - Caminho do icone do atalho
     * @param description String - Descrição do atalho
     * @throws java.lang.Exception
     */
    public static void makeShortcutIconOnDesktopInWindows(String target, String name, String workDir, String icon, String description) throws Exception {
        makeShortcutIconInWindows(target, CSPUtilidadesSO.PATH_HOME + "/../Desktop/" + name + ".lnk", workDir, icon, description);
    }

    /**
     * Torna oculto um arquivo no windows. No linux basta usar o prefixo "."
     *
     * @param src String - Caminho do arquivo
     * @throws IOException
     */
    public static void hideFileInWindows(String src) throws IOException {
        if (!isSoWindows()) {
            return;
        }
        runCommandInSo("attrib +H " + src);
    }

    /**
     * Classe responsável por manipular os registros do windows.
     *
     *
     * Inspirada em:
     * http://stackoverflow.com/questions/62289/read-write-to-windows-registry-using-java/6163701#6163701
     *
     * @author Fernando Batels <luisfbatels@gmail.com>
     *
     */
    public static class WinRegistry {

        public enum KeyRegWindows {

            HKEY_CURRENT_USER(0x80000001, Preferences.userRoot()),
            HKEY_LOCAL_MACHINE(0x80000002, Preferences.systemRoot());
            //HKEY_LOCAL_MACHINE("HKLM"),
            //HKEY_CURRENT_USER("HKCU"),
            //HKEY_USERS("HKU"),
            //HKEY_CLASSES_ROOT("HKCR")
            ;
        final public int key;
            final public Preferences root;

            private KeyRegWindows(int key, Preferences root) {
                this.key = key;
                this.root = root;
            }

        }

        public enum RegStatusWindows {

            REG_SUCCESS(0),
            REG_NOTFOUND(2),
            REG_ACCESSDENIED(5);
            final public int key;

            private RegStatusWindows(int key) {
                this.key = key;
            }

        }

        public enum KeyStatusWindows {

            KEY_ALL_ACCESS(0xf003f),
            KEY_READ(0x20019);
            final public int key;

            private KeyStatusWindows(int key) {
                this.key = key;
            }

        }

        private static Class<? extends Preferences> userClass = Preferences.userRoot().getClass();
        private static Method regOpenKey = null;
        private static Method regCloseKey = null;
        private static Method regQueryValueEx = null;
        private static Method regEnumValue = null;
        private static Method regQueryInfoKey = null;
        private static Method regEnumKeyEx = null;
        private static Method regCreateKeyEx = null;
        private static Method regSetValueEx = null;
        private static Method regDeleteKey = null;
        private static Method regDeleteValue = null;

        static {
            try {
                regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", new Class[]{int.class, byte[].class, int.class});
                regOpenKey.setAccessible(true);
                regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", new Class[]{int.class});
                regCloseKey.setAccessible(true);
                regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", new Class[]{int.class, byte[].class});
                regQueryValueEx.setAccessible(true);
                regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", new Class[]{int.class, int.class, int.class});
                regEnumValue.setAccessible(true);
                regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", new Class[]{int.class});
                regQueryInfoKey.setAccessible(true);
                regEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx", new Class[]{int.class, int.class, int.class});
                regEnumKeyEx.setAccessible(true);
                regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx", new Class[]{int.class, byte[].class});
                regCreateKeyEx.setAccessible(true);
                regSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx", new Class[]{int.class, byte[].class, byte[].class});
                regSetValueEx.setAccessible(true);
                regDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue", new Class[]{int.class, byte[].class});
                regDeleteValue.setAccessible(true);
                regDeleteKey = userClass.getDeclaredMethod("WindowsRegDeleteKey", new Class[]{int.class, byte[].class});
                regDeleteKey.setAccessible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * delete a value from a given key/value name
         *
         * @param key
         * @param value
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        public static RegStatusWindows deleteValue(KeyRegWindows h, String key, String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            key = key.replace("/", "\\");
            int[] handles = (int[]) regOpenKey.invoke(h.root, new Object[]{h.key, toCstr(key), KeyStatusWindows.KEY_ALL_ACCESS.key});

            if (RegStatusWindows.REG_ACCESSDENIED.key == handles[1]) {
                return RegStatusWindows.REG_ACCESSDENIED;
            } else if (RegStatusWindows.REG_NOTFOUND.key == handles[1]) {
                return RegStatusWindows.REG_NOTFOUND;
            }

            int rc = ((Integer) regDeleteValue.invoke(h.root, new Object[]{handles[0], toCstr(value)}));

            regCloseKey.invoke(h.root, new Object[]{handles[0]});
            if (RegStatusWindows.REG_ACCESSDENIED.key == rc) {
                return RegStatusWindows.REG_ACCESSDENIED;
            } else if (RegStatusWindows.REG_NOTFOUND.key == rc) {
                return RegStatusWindows.REG_NOTFOUND;
            } else {
                return RegStatusWindows.REG_SUCCESS;
            }
        }

        /**
         * Delete a given key
         *
         * @param key
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        public static RegStatusWindows deleteKey(KeyRegWindows h, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            key = key.replace("/", "\\");
            int rc = ((Integer) regDeleteKey.invoke(h.root, new Object[]{h.key, toCstr(key)}));
            if (RegStatusWindows.REG_ACCESSDENIED.key == rc) {
                return RegStatusWindows.REG_ACCESSDENIED;
            } else if (RegStatusWindows.REG_NOTFOUND.key == rc) {
                return RegStatusWindows.REG_NOTFOUND;
            } else {
                return RegStatusWindows.REG_SUCCESS;
            }
            //return rc;  // can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
        }

        /**
         * Read a value from key and value name
         *
         * @param key
         * @return the value
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        public static String readString(KeyRegWindows h, String key, String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            key = key.replace("/", "\\");
            int[] handles = (int[]) regOpenKey.invoke(h.root, new Object[]{h.key, toCstr(key), KeyStatusWindows.KEY_READ.key});

            if (handles[1] != RegStatusWindows.REG_SUCCESS.key) {
                return null;
            }

            byte[] valb = (byte[]) regQueryValueEx.invoke(h.root, new Object[]{handles[0], toCstr(value)});

            regCloseKey.invoke(h.root, new Object[]{handles[0]});

            return (valb != null ? new String(valb).trim() : null);
        }

        /**
         * Read value(s) and value name(s) form given key
         *
         * @param key
         * @return the value name(s) plus the value(s)
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        public static Map<String, String> readStringValues(KeyRegWindows h, String key) throws IllegalAccessException, InvocationTargetException {
            key = key.replace("/", "\\");
            HashMap<String, String> results = new HashMap<>();
            int[] handles = (int[]) regOpenKey.invoke(h.root, new Object[]{h.key, toCstr(key), KeyStatusWindows.KEY_READ.key});

            if (handles[1] != RegStatusWindows.REG_SUCCESS.key) {
                return null;
            }

            int[] info = (int[]) regQueryInfoKey.invoke(h.root, new Object[]{handles[0]});

            int count = info[0]; // count  
            int maxlen = info[3]; // value length max

            for (int index = 0; index < count; index++) {
                byte[] name = (byte[]) regEnumValue.invoke(h.root, new Object[]{handles[0], index, maxlen + 1});
                String value = readString(h, key, new String(name));
                results.put(new String(name).trim(), value);
            }

            regCloseKey.invoke(h.root, new Object[]{handles[0]});

            return results;
        }

        /**
         * Read the value name(s) from a given key
         *
         * @param key
         * @return the value name(s)
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        public static List<String> readStringSubKeys(KeyRegWindows h, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            key = key.replace("/", "\\");
            List<String> results = new ArrayList<>();
            int[] handles = (int[]) regOpenKey.invoke(h.root, new Object[]{h.key, toCstr(key), KeyStatusWindows.KEY_READ.key});

            if (handles[1] != RegStatusWindows.REG_SUCCESS.key) {
                return null;
            }

            int[] info = (int[]) regQueryInfoKey.invoke(h.root, new Object[]{handles[0]});

            int count = info[0]; // Fix: info[2] was being used here with wrong results. Suggested by davenpcj, confirmed by Petrucio
            int maxlen = info[3]; // value length max

            for (int index = 0; index < count; index++) {
                byte[] name = (byte[]) regEnumKeyEx.invoke(h.root, new Object[]{handles[0], index, maxlen + 1});
                results.add(new String(name).trim());
            }

            regCloseKey.invoke(h.root, new Object[]{handles[0]});

            return results;
        }

        /**
         * Create a key
         *
         * @param key
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        public static int[] createKey(KeyRegWindows h, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            key = key.replace("/", "\\");
            return (int[]) regCreateKeyEx.invoke(h.root, new Object[]{h.key, toCstr(key)});
        }

        /**
         * Write a value in a given key/value name
         *
         * @param key
         * @param valueName
         * @param value
         * @throws IllegalArgumentException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         */
        public static void writeStringValue(KeyRegWindows h, String key, String valueName, String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            key = key.replace("/", "\\");
            int[] handles = (int[]) regOpenKey.invoke(h.root, new Object[]{h.key, toCstr(key), KeyStatusWindows.KEY_ALL_ACCESS.key});

            regSetValueEx.invoke(h.root, new Object[]{handles[0], toCstr(valueName), toCstr(value)});

            regCloseKey.invoke(h.root, new Object[]{handles[0]});

        }

        // utility
        private static byte[] toCstr(String str) {
            byte[] result = new byte[str.length() + 1];

            for (int i = 0; i < str.length(); i++) {
                result[i] = (byte) str.charAt(i);
            }
            result[str.length()] = 0;
            return result;
        }
    }

    /**
     * Retorna o valor da variável de ambiente
     *
     * @param name String - Nome da variável
     * @return
     */
    public static Object getValEnvVariable(String name) {
        return System.getenv(name);
    }

    /**
     * Define um valor para a variável de ambiente
     *
     * @param name String - Nome da variável
     * @param val Object - Valor
     * @throws IOException
     */
    public static void setValEnvVariable(String name, Object val) throws Exception {
        if (isSoWindows()) {
            runProcessInSo("set", name + "=" + val);
        } else {

            runShellScriptInLinux("export " + name + "=\"" + val + "\"");
        }
    }

    /**
     * Retorna o valor gold compartilhado
     *
     * @param name String - Nome do valor
     * @return
     * @throws java.lang.Exception
     */
    public static Object getValSharedGold(String name) throws Exception {
        return new CSPArquivos(CSPUtilidadesSO.PATH_TEMP + "/gold-tmp-shared-" + name.toLowerCase()).getContent();
    }

    /**
     * Define um valor gold compartilhado
     *
     * @param name String - Nome da valor
     * @param val Object - Valor
     * @throws IOException
     */
    public static void setValSharedGold(String name, Object val) throws Exception {
        final CSPArquivos tmp = new CSPArquivos(CSPUtilidadesSO.PATH_TEMP + "/gold-tmp-shared-" + name.toLowerCase());
        if (val != null) {
            tmp.setContent(val.toString());

        } else {
            tmp.delete();
        }
    }

    /**
     * Verifica a quantidade total de RAM do sistema.
     *
     * @return long - Total de memória RAM.
     */
    public static long getMemoriaRAMTotalSO() {
        long retorno = 0;

        //Try necessário para os casos onde as DLLs não estão presentes.
        try {
            Sigar.load();
            retorno = new Sigar().getMem().getTotal() / 1024;

        } catch (SigarException e) {
            CSPLog.error("problema ao coletar informacoes da maquina");
            CSPException.register(e);
        }

        return retorno;
    }

    /**
     * Verifica a quantidade de memória RAM livre no momento.
     *
     * @return long - Total da memória RAM livre atualmente.
     */
    public static long getMemoriaRAMLivreSO() {
        long retorno = 0;

        //Try necessário para os casos onde as DLLs não estão presentes.
        try {
            Sigar.load();
            retorno = new Sigar().getMem().getActualFree() / 1024;

        } catch (SigarException e) {
            CSPLog.error("problema ao coletar informacoes da maquina");
            CSPException.register(e);
        }

        return retorno;
    }

    /**
     * Verifica a quantidade de memória RAM livre percentualmente.
     *
     * @return double - Memória RAM livre atualmente em percentual.
     */
    public static double getMemoriaRAMLivrePercentualSO() {
        double retorno = 0;

        //Try necessário para os casos onde as DLLs não estão presentes.
        try {
            Sigar.load();
            retorno = new Sigar().getMem().getFreePercent();

        } catch (SigarException e) {
            CSPLog.error("problema ao coletar informacoes da maquina");
            CSPException.register(e);
        }

        return retorno;
    }

    /**
     * Verifica a quantidade de memória RAM sendo usada no momento.
     *
     * @return long - Total da memória RAM usada atualmente.
     */
    public static long getMemoriaRAMUsadaSO() {
        long retorno = 0;

        //Try necessário para os casos onde as DLLs não estão presentes.
        try {
            Sigar.load();
            retorno = new Sigar().getMem().getActualUsed() / 1024;

        } catch (SigarException e) {
            CSPLog.error("problema ao coletar informacoes da maquina");
            CSPException.register(e);
        }

        return retorno;
    }

    /**
     * Verifica o uso da Memoria RAM percentualmente.
     *
     * @return double - Uso da memória RAM em percentual.
     */
    public static double getMemoriaRAMUsadaPercentualSO() {
        double retorno = 0;

        //Try necessário para os casos onde as DLLs não estão presentes.
        try {
            Sigar.load();
            retorno = new Sigar().getMem().getUsedPercent();

        } catch (SigarException e) {
            CSPLog.error("problema ao coletar informacoes da maquina");
            CSPException.register(e);
        }

        return retorno;
    }

    /**
     * Verifica o total de cores do processador <b>(Fisicos e virtuais)</b>.
     *
     * @return int - Quantidade de núcleos. (Fisicos e virtuais)
     */
    public static int getNucleosCpuSO() {
        int retorno = 0;

        //Try necessário para os casos onde as DLLs não estão presentes.
        try {
            Sigar.load();
            retorno = new Sigar().getCpuInfoList()[0].getTotalCores();

        } catch (SigarException e) {
            CSPLog.error("problema ao coletar informacoes da maquina");
            CSPException.register(e);
        }

        return retorno;
    }

    /**
     * Verifica a velocidade do processador.
     *
     * @return int - Velocidade, <b>em Mhz</b>, do processador.
     */
    public static int getSpeedCpuSO() {
        int retorno = 0;

        //Try necessário para os casos onde as DLLs não estão presentes.
        try {
            Sigar.load();
            retorno = new Sigar().getCpuInfoList()[0].getMhz();

        } catch (SigarException e) {
            CSPLog.error("problema ao coletar informacoes da maquina");
            CSPException.register(e);
        }

        return retorno;
    }
}
