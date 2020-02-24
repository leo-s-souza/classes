/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.pad;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Months;
import org.joda.time.Seconds;
import org.joda.time.Years;

/**
 * Métodos de auxilio para linguagem no contexto de datas e horas
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 27/10/2016 - 17:13:17
 */
public abstract class CSPUtilidadesLangDateTime extends CSPUtilidadesLang {

    /**
     * Data atual do SO. No formato "dd.MM.yyyy"
     *
     * @return String
     */
    public static String getData() {
        return new SimpleDateFormat("dd.MM.yyyy").format(new Date());
    }

    /**
     * Retorna a string com a data e hora atual, mas modificados com os
     * parâmetros passados. Sendo valores positivos para aumentar a data e
     * valores negativos para diminuir
     *
     * @param formato String - Formato
     * @param dias int - Quantidade de dias para modificar
     * @param horas int - Quantidade de horas para modificar
     * @param minutos int - Quantidade de minutos para modificar
     * @param segundos int - Quantidade de segundos para modificar
     * @return Date
     */
    public static String getDataHora(String formato, int dias, int horas, int minutos, int segundos) {
        return new SimpleDateFormat(formato).format(getDataHoraObj(dias, horas, minutos, segundos));
    }

    /**
     * Retorna a string com a data e hora atual, mas modificados com os
     * parâmetros passados. Sendo valores positivos para aumentar a data e
     * valores negativos para diminuir
     *
     * @param formato String - Formato
     * @param dias int - Quantidade de dias para modificar
     * @return Date
     */
    public static String getDataHora(String formato, int dias) {
        return new SimpleDateFormat(formato).format(getDataHoraObj(dias));
    }

    /**
     * Retorna a string com a data e hora atual, mas modificados com os
     * parâmetros passados. Sendo valores positivos para aumentar a data e
     * valores negativos para diminuir
     *
     * @param formato String - Formato
     * @param horas int - Quantidade de horas para modificar
     * @param minutos int - Quantidade de minutos para modificar
     * @return Date
     */
    public static String getDataHora(String formato, int horas, int minutos) {
        return new SimpleDateFormat(formato).format(getDataHoraObj(horas, minutos));
    }

    /**
     * Retorna o objeto com a data e hora atual, mas modificados com os
     * parâmetros passados. Sendo valores positivos para aumentar a data e
     * valores negativos para diminuir
     *
     * @param dias int - Quantidade de dias para modificar
     * @return Date
     */
    public static Date getDataHoraObj(int dias) {
        return getDataHoraObj(dias, 0, 0, 0);
    }

    /**
     * Retorna o objeto com a data e hora atual, mas modificados com os
     * parâmetros passados. Sendo valores positivos para aumentar a data e
     * valores negativos para diminuir
     *
     * @param horas int - Quantidade de horas para modificar
     * @param minutos int - Quantidade de minutos para modificar
     * @return Date
     */
    public static Date getDataHoraObj(int horas, int minutos) {
        return getDataHoraObj(0, horas, minutos, 0);
    }

    /**
     * Retorna o objeto com a data e hora atual, mas modificados com os
     * parâmetros passados. Sendo valores positivos para aumentar a data e
     * valores negativos para diminuir
     *
     * @param dias int - Quantidade de dias para modificar
     * @param horas int - Quantidade de horas para modificar
     * @param minutos int - Quantidade de minutos para modificar
     * @param segundos int - Quantidade de segundos para modificar
     * @return Date
     */
    public static Date getDataHoraObj(int dias, int horas, int minutos, int segundos) {

        if (dias == 0 && horas == 0 && minutos == 0 && segundos == 0) {
            return new Date();
        }

        final Calendar cal = Calendar.getInstance();

        if (dias != 0) {
            cal.add(Calendar.DATE, dias);
        }
        if (horas != 0) {
            cal.add(Calendar.HOUR_OF_DAY, horas);
        }
        if (minutos != 0) {
            cal.add(Calendar.MINUTE, minutos);
        }
        if (segundos != 0) {
            cal.add(Calendar.SECOND, segundos);
        }

        return cal.getTime();
    }

    /**
     * Data de ontem em relação a data atual do SO. No formato "dd.MM.yyyy"
     *
     * @return
     * @deprecated
     * @see #getDataHoraObj(int)
     * @see #getDataHora(java.lang.String, int, int, int, int)
     */
    @Deprecated
    public static String getDataOntem() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return new SimpleDateFormat("dd.MM.yyyy").format(cal.getTime());
    }

    /**
     * Hora atual do SO. No formato "HH:mm:ss"
     *
     * @return String
     */
    public static String getHora() {
        return new SimpleDateFormat("HH:mm:ss").format(new Date());
    }

    /**
     * Retorna o objeto da Data do SO
     *
     * @return Date
     * @throws java.text.ParseException
     */
    public static Date getDataObj() throws ParseException {
        return new SimpleDateFormat("dd.MM.yyyy").parse(getData());
    }

    /**
     * Retorna o objeto da Hora do SO
     *
     * @return String
     * @throws java.text.ParseException
     */
    public static Date getHoraObj() throws ParseException {
        return new SimpleDateFormat("HH:mm:ss").parse(getHora());
    }

    /**
     * Hora completa atual do SO. No formato "yyyy-MM-dd-HH-mm-ss-SSS"
     *
     *
     * @return String
     */
    public static String getTempoCompleto() {
        return new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS").format(new Date());
    }

    /**
     * Hora completa atual do SO. No formato indicado para gravar em capos
     * Timesamp
     *
     *
     * @return String
     */
    public static String getTimeSamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * Hora completa atual do SO. No formato "yyyyMMddHHmmssSSS"
     *
     *
     * @return String
     */
    public static String getTempoCompletoLimpo() {
        return getTempoCompleto().replace("-", "");
    }

    /**
     * Formata o tempo passado por parametro
     *
     * @param formato String
     * @return
     * @see
     * https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
     */
    public static String formataDataHora(String formato) {
        return new SimpleDateFormat(formato).format(new Date());
    }

    /**
     * Formata o tempo passado por parametro
     *
     * @param time long
     * @param formato String
     * @return
     * @see
     * https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
     */
    public static String formataDataHora(long time, String formato) {
        return new SimpleDateFormat(formato).format(new Date(time));
    }

    /**
     * Formata o tempo passado por parametro
     *
     * @param time Date
     * @param formato String
     * @return
     * @see
     * https://docs.oracle.com/javase/tutorial/i18n/format/simpleDateFormat.html
     */
    public static String formataDataHora(Date time, String formato) {
        return new SimpleDateFormat(formato).format(time);
    }

    /**
     * Método que retorna a intervalo de tempo entre duas datas! Exemplo:
     * 01:01:2016 00:00:00 - 02:01:2016 08:30:00 vai retornar 32:30:00
     *
     * @param start Date - Data/Hora de inicio
     * @param end Date - Data/Hora de fim
     * @return String - Formato: HH:mm:ss
     *
     *
     */
    public static String intervaloTempo(Date start, Date end) {
        DateTime dt1 = new DateTime(start);
        DateTime dt2 = new DateTime(end);

        return Hours.hoursBetween(dt1, dt2).getHours() + ":" + pad(String.valueOf(Minutes.minutesBetween(dt1, dt2).getMinutes() % 60), 2, "0") + ":" + pad(String.valueOf(Seconds.secondsBetween(dt1, dt2).getSeconds() % 60), 2, "0");
    }

    /**
     * Retorna o intervalo de tempo entre "os dois tempos"
     *
     * @param start Date - Inicio
     * @param end Date - Fim
     * @param intervalo IntervaloTempo - Tipo do intervalo
     * @return
     */
    public static long getIntervaloTempo(Date start, Date end, IntervaloTempo intervalo) {
        switch (intervalo) {
            case ANOS:
                return Years.yearsBetween(new DateTime(start), new DateTime(end)).getYears();
            case MESES:
                return Months.monthsBetween(new DateTime(start), new DateTime(end)).getMonths();
            case DIAS:
                return Days.daysBetween(new DateTime(start), new DateTime(end)).getDays();
            case HORAS:
                return Hours.hoursBetween(new DateTime(start), new DateTime(end)).getHours();
            case MINUTOS:
                return Minutes.minutesBetween(new DateTime(start), new DateTime(end)).getMinutes();
            case SEGUNDOS:
                return Seconds.secondsBetween(new DateTime(start), new DateTime(end)).getSeconds();
        }
        return -1;
    }

    public enum IntervaloTempo {
        ANOS,
        MESES,
        DIAS,
        HORAS,
        MINUTOS,
        SEGUNDOS

    }

    /**
     * Converte segundos em tipo Hora.
     *
     * @param segundos int - Segundos a serem convertidos.
     * @param arredondaParaCima boolean - Arredonda os minutos para cima.
     * @return String - Hora convertida (HH:MM:SS).
     */
    public static String converteSegundosEmTipoHora(int segundos, boolean arredondaParaCima) {

        if (segundos == 0) {
            return "00:00";
        } else {

            int ss = segundos % 60;
            segundos /= 60;
            int min = segundos % 60;
            segundos /= 60;
            int hh = segundos % 24;

            if (arredondaParaCima) {
                if (ss != 0 && ss < 60) {
                    return colocaZero(hh) + ":" + colocaZero(min + 1) + ":" + colocaZero(ss);
                }
            } else {
                return colocaZero(hh) + ":" + colocaZero(min) + ":" + colocaZero(ss);
            }
        }
        return "00:00";
    }

    /**
     *  Coloca zero na frente para ajustao ao formato hora
     * 
     * @param n int - hora, minutos ou segundos.
     * @return String - Valor corrigido para formato de hora (zero na frente).
     */
    
    private static String colocaZero(int hora) {
        if (hora < 10) {
            return "0" + String.valueOf(hora);
        }
        return String.valueOf(hora);
    }

    /**
     * Classe para trabalhar com somas de tempos (hora:min:seg.SSS). Exemplo:
     * Somar 03:00:00.000 em 22:00:00.000. Se usarmos apenas um SimpleDateFormat
     * teríamos apenas: 01:00:00.000, pois passou das 24h. Com essa classe
     * teremos o resultado de: 25:00:00.000
     *
     * Inspirado em:
     * http://stackoverflow.com/questions/11354756/summing-time-past-24-hours
     *
     */
    public static class TimeSum {

        private short milliSeconds;
        private short seconds;
        private short minutes;
        private int hours;

        /**
         * Cria o objeto da classe
         *
         * @param dateObj Date - Objeto do tempo a ser extraído as informações:
         * HH:mm:ss.SSS
         */
        public TimeSum(Date dateObj) {
            this(new SimpleDateFormat("HH:mm:ss.SSS").format(dateObj));
        }

        /**
         * Cria o objeto da classe
         *
         * @param dateString String - Tempo informado no formato: HH:mm:ss.SSS
         */
        public TimeSum(String dateString) {

            Pattern pattern = Pattern.compile("(\\d+):(\\d\\d):(\\d\\d)\\.(\\d\\d\\d)");
            Matcher matcher = pattern.matcher(dateString);
            if (matcher.find()) {
                this.hours = Integer.parseInt(dateString.substring(matcher.start(1), matcher.end(1)));
                this.minutes = Short.parseShort(dateString.substring(matcher.start(2), matcher.end(2)));
                this.seconds = Short.parseShort(dateString.substring(matcher.start(3), matcher.end(3)));
                this.milliSeconds = Short.parseShort(dateString.substring(matcher.start(4), matcher.end(4)));
            }
        }

        /**
         * Adiciona o intervalo de tempo informado ao tempo da classe
         *
         * @param interval TimeSum
         */
        public void add(TimeSum interval) {

            int milliSecondsT = (short) (interval.milliSeconds + this.milliSeconds) % 1000;
            int carry = (interval.milliSeconds + this.milliSeconds) / 1000;
            int secondsT = (short) ((interval.seconds + this.seconds) % 60 + carry);
            carry = (interval.seconds + this.seconds) / 60;
            int minutesT = (short) ((interval.minutes + this.minutes) % 60 + carry);
            carry = (interval.minutes + this.minutes) / 60;
            int hoursT = (short) (interval.hours + this.hours + carry);

            this.milliSeconds = (short) milliSecondsT;
            this.seconds = (short) secondsT;
            this.minutes = (short) minutesT;
            this.hours = hoursT;
        }

        /**
         * Retorna o novo horário
         *
         * @return String - Formato: HH:mm:ss.SSS
         */
        public String format() {
            return String.format("%d:%02d:%02d.%03d", this.hours, this.minutes, this.seconds, this.milliSeconds);
        }

        /**
         * Retorna o novo horário
         *
         * @return String - Formato: HH:mm:ss.SSS
         * @throws java.text.ParseException
         */
        public Date parse() throws ParseException {
            return new SimpleDateFormat("HH:mm:ss.SSS").parse(this.format());
        }
    }

}
