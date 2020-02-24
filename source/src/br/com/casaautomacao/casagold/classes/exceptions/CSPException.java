/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.exceptions;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication;
import java.util.ArrayList;

/**
 * Classe para tratar as Exceptions do sistema
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 21/01/2016 - 11:14:33
 */
public class CSPException {

    private static final ArrayList<Error> acoes = new ArrayList<>();

    /**
     * Registra uma Exception
     *
     * @param ex Exception
     */
    public static void register(Throwable ex) {
        register(ex, new Object[0]);
    }

    /**
     * Registra uma Exception
     *
     * @param ex Exception
     * @param restart boolean - Determina se o sistema deve reinciar
     */
    public static void register(Throwable ex, boolean restart) {
        register(ex, restart, new Object[0]);

    }

    /**
     * Registra uma Exception
     *
     * @param ex Exception
     * @param params Object[] - Parametros adicionais
     *
     */
    public static void register(Throwable ex, Object... params) {
        register(ex, false, params);
    }

    /**
     * Registra uma Exception
     *
     * @param ex Exception
     * @param restart boolean - Determina se o sistema deve reinciar
     * @param params Object[] - Parametros adicionais
     */
    public static void register(Throwable ex, boolean restart, Object... params) {
        if (ex instanceof OnlyStopException == false) {
            acoes.stream().forEach((a) -> {
                a.run(ex, (Object[]) params);
            });
        }
        if (restart) {
            try {
                CSPUtilidadesApplication.restart(CSPException.class);
            } catch (Exception e) {
                CSPException.register(ex);
            }
        }
    }

    /**
     * <b style="color:#ff0000">Uso restrito!!</b>
     */
    public interface Error {

        public void run(Throwable ex, Object[] params);
    }

    /**
     * <b style="color:#ff0000">Uso restrito!!</b>
     *
     * @param a
     */
    public static void addAction(Error a) {
        acoes.add(a);
    }
}
