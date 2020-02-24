/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Classe que guarda o valor retornado nas impressoras fiscais.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPObjectReturnEcf {

    /**
     * Buffer que guarda o valor retornado pela função do ECF.
     */
    private Object ecfBuffer;

    /**
     * Retorna o Buffer que possui o valor retornado pela função do ECF.
     *
     * @return Object
     * @see #ecfBuffer
     */
    public Object getEcfBuffer() {
        return ecfBuffer;
    }

    /**
     * Retorna o Buffer, que possui o valor retornado pela função do ECF,
     * convertido para inteiro.
     *
     * @return int
     * @see #ecfBuffer
     */
    public int getEcfBufferToInt() {
        if (ecfBuffer != null) {
            return Integer.parseInt(ecfBuffer.toString());
        } else {
            return 0;
        }
    }

    /**
     * Retorna o Buffer, que possui o valor retornado pela função do ECF,
     * convertido para double.
     *
     * @return double
     * @see #ecfBuffer
     */
    public double getEcfBufferToDouble() {
        if (ecfBuffer != null) {
            String temp = ecfBuffer.toString();
            temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
            return Double.parseDouble(temp);
        } else {
            return 0;
        }
    }

    /**
     * Retorna o Buffer, que possui o valor retornado pela função do ECF,
     * convertido para Date.
     *
     * @return Date
     * @throws java.lang.Exception
     * @see #ecfBuffer
     */
    public Date getEcfBufferToDate() throws Exception {
        if (ecfBuffer != null) {
            return new SimpleDateFormat("dd.MM.yyyy").parse(ecfBuffer.toString());
        } else {
            return null;
        }
    }

    /**
     * Retorna o Buffer, que possui o valor retornado pela função do ECF,
     * convertendo a hora para o tipo Date.
     *
     * @return Date
     * @throws java.lang.Exception
     * @see #ecfBuffer
     */
    public Date getEcfBufferToTime() throws Exception {
        if (ecfBuffer != null) {
            return new SimpleDateFormat("HH:mm:ss").parse(ecfBuffer.toString());
        } else {
            return null;
        }
    }

    /**
     * Retorna o Buffer, que possui o valor retornado pela função do ECF,
     * convertido para String.
     *
     * @return String
     * @see #ecfBuffer
     */
    public String getEcfBufferToString() {
        if (ecfBuffer != null) {
            return ecfBuffer.toString().trim();
        } else {
            return "";
        }
    }

    /**
     * Seta o valor do Buffer.
     *
     * @param ecfBuffer
     * @see #ecfBuffer
     */
    public void setEcfBuffer(Object ecfBuffer) {
        this.ecfBuffer = ecfBuffer;
    }
}
