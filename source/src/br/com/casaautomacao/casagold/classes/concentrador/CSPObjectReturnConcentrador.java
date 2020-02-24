/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.concentrador;

/**
 * Classe que guarda o valor retornado nos concentradores.
 *
 * @author cautomacao
 */
public class CSPObjectReturnConcentrador {

    /**
     * Buffer que guarda o valor retornado pela função da ECF.
     */
    private Object concentradorBuffer;

    /**
     * Retorna o Buffer que possuí o valor retornado pela função do
     * Concentrador.
     *
     * @return Object
     * @see #concentradorBuffer
     */
    public Object getConcentradorBuffer() {
        return concentradorBuffer;
    }

    /**
     * Seta o valor do Buffer.
     *
     * @param concentradorBuffer
     * @see #concentradorBuffer
     */
    public void setConcentradorBuffer(Object concentradorBuffer) {
        this.concentradorBuffer = concentradorBuffer;
    }
}
