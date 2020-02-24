/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.exceptions.OnlyStopException;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.RunneableExecutor;
import java.security.InvalidParameterException;
import java.util.LinkedHashSet;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Módulo pai do sistema.
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/11/2015 - 07:23:40
 */
abstract public class FrmModuloPaiBase {

    /**
     * Lista de ScheduledExecutorService abertos no sistema.
     */
    public static LinkedHashSet<ScheduledExecutorService> executorList;

    /**
     * Atalho para os logs do módulo
     *
     * @param ms String - Mensagem a ser gravada
     */
    public void logInfo(String ms) {

        CSPLog.info(this.getClass(), ms);
    }

    /**
     * Atalho para os logs de erro do módulo
     *
     * @param ms String - Mensagem a ser gravada
     */
    protected void logError(String ms) {
        CSPLog.error(this.getClass(), ms);
    }

    /**
     * Atalho para os logs de erro do módulo e para o módulo
     *
     * @param ms String - Mensagem a ser gravada
     * @throws
     * br.com.casaautomacao.casagold.classes.exceptions.OnlyStopException
     */
    protected void logErrorBreak(String ms) throws OnlyStopException {
        this.logError(ms);
        throw new OnlyStopException();
    }

    /**
     * Onde o módulo irá funcionar
     *
     * @throws Exception
     */
    protected abstract void run() throws Exception;

    /**
     * Executa determinado módulo
     *
     * @param modulo Class - Classe do módulo a ser executado
     */
    public static void runModule(Class modulo) {
        runModule(modulo, () -> true);
    }

    public interface PreValidacaoModule {

        public boolean check() throws Exception;
    }

    /**
     * Executa determinado módulo
     *
     * @param modulo Class - Classe do módulo a ser executado
     * @param check PreValidacaoModule - Validação realizada para verificar se o
     * módulo pode ou não iniciar. Exemplo: Verificar se o CNPJ está presente na
     * base...
     */
    @SuppressWarnings("UseSpecificCatch")
    public static void runModule(Class modulo, PreValidacaoModule check) {
        simpleThread(() -> {
            if (check.check()) {
                try {
                    CSPArquivos onyModule = new CSPArquivos(PATH + "/only-module");
                    if (onyModule.exists()) {
                        if (!onyModule.getContent().trim().contains(modulo.getName())) {
                            return;
                        }
                    }

                    Object obj = modulo.newInstance();
                    if (obj instanceof FrmModuloPaiBase) {
                        ((FrmModuloPaiBase) obj).logInfo("Module starting...");
                        simpleThread(() -> {
                            ((FrmModuloPaiBase) obj).run();
                        });
                        ((FrmModuloPaiBase) obj).logInfo("Module starting...OK");
                    } else {
                        throw new InvalidParameterException("The class " + modulo + " is not module!!");
                    }
                } catch (Exception ex) {
                    CSPException.register(ex, false);//Um módulo não pode parar a aplicação
                    CSPLog.info(modulo, "Module restarting...");
                    runModule(modulo, check);
                }
            } else {
                CSPLog.info(modulo, "Not started. Validation not allow");
            }
        });
    }
    
    public static void runModuleCapp(Class modulo) throws Exception {
        runModuleCapp(modulo, () -> {
            return true;
        });
    }

    public static void runModuleCapp(Class modulo, PreValidacaoModule check) throws Exception {
        if (check.check()) {
            try {
                CSPArquivos onyModule = new CSPArquivos(PATH + "/only-module");
                if (onyModule.exists()) {
                    if (!onyModule.getContent().trim().contains(modulo.getName())) {
                        return;
                    }
                }

                Object obj = modulo.newInstance();
                if (obj instanceof FrmModuloPaiBase) {
                    ((FrmModuloPaiBase) obj).logInfo("Module starting...");
                    simpleThread(() -> {
                        ((FrmModuloPaiBase) obj).run();
                    });
                    ((FrmModuloPaiBase) obj).logInfo("Module starting...OK");
                } else {
                    throw new InvalidParameterException("The class " + modulo + " is not module!!");
                }
            } catch (Exception ex) {
                CSPException.register(ex, false);//Um módulo não pode parar a aplicação
                CSPLog.info(modulo, "Module restarting...");
                runModule(modulo, check);
            }
        } else {
            CSPLog.info(modulo, "Not started. Validation not allow");
        }
    }

    /**
     * Executa thread com try catch
     *
     * @param th SimpleTh - Código a ser executado
     */
    protected void simpleTh(CSPUtilidadesLang.SuperRunneable th) {
        CSPUtilidadesLang.simpleThread(th, 0);
    }

    /**
     * Executa thread com try catch
     *
     * @param th SimpleTh - Código a ser executado
     * @param interval int - Intervalo de execução (Em milissegundos)
     */
    protected void simpleTh(CSPUtilidadesLang.SuperRunneable th, int interval) {
        CSPUtilidadesLang.simpleThread(th, interval);
    }

    /**
     * Executa thread com try catch
     *
     * @param th SimpleTh - Código a ser executado
     */
    public static void simpleThread(CSPUtilidadesLang.SuperRunneable th) {
        CSPUtilidadesLang.simpleThread(th, 0);
    }

    /**
     * Executa um bloco de código de tempos em tempos.
     *
     * @param task Código a ser executado.
     * @param initialDelay Delay inicial em segundos.
     * @param period Periodo a ser executado em segundos.
     */
    public static void executor(RunneableExecutor task, int initialDelay, int period) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(() -> {
            try {
                task.run(executor);
            } catch (Exception ex) {
                CSPException.register(ex);
                executor.shutdown();
            }
        }, initialDelay, period, TimeUnit.SECONDS);

        if (getExecutorList() == null) {
            setExecutorList(new LinkedHashSet<>());
        }
        getExecutorList().add(executor);
    }

    /**
     * Executa thread com try catch
     *
     * @param th SimpleTh - Código a ser executado
     * @param interval int - Intervalo de execução (Em milissegundos)
     */
    public static void simpleThread(CSPUtilidadesLang.SuperRunneable th, int interval) {
        CSPUtilidadesLang.simpleThread(th, interval);
    }

    /**
     * Finaliza todo ScheduledExecutorService aberto.
     */
    public static void shutDownExecutors() {
        if (getExecutorList() != null) {
            getExecutorList().stream().filter((ex) -> (!ex.isShutdown())).forEachOrdered((ex) -> {
                ex.shutdown();
            });
        }
    }

    /**
     * Retorna a lista de ScheduledExecutorService abertos no sistema.
     *
     * @return LinkedHashSet(ScheduledExecutorService)
     */
    public static LinkedHashSet<ScheduledExecutorService> getExecutorList() {
        return executorList;
    }

    /**
     * Seta a lista de ScheduledExecutorService abertos no sistema.
     *
     * @param executorList
     */
    public static void setExecutorList(LinkedHashSet<ScheduledExecutorService> executorList) {
        FrmModuloPaiBase.executorList = executorList;
    }
}
