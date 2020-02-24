/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.concentrador;

import static br.com.casaautomacao.casagold.classes.CSPLog.info;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
import java.util.StringJoiner;

/**
 * Classe reponsavel por encontrar as portas coms disponiveis e fazer o
 * carregamento das bibliotecas rxtx que são utilizadas para a comunicação com
 * as portas com.
 *
 * Isso aqui foi feito pelo Leléo. Basicamente tudo tem que ser refeito, pois de
 * dinânico não tem nada.
 */
public abstract class CSPConcentradorBase {

    /**
     * Guarda a porta de comunicação do concentrador.
     */
    private String porta = null;

    private CSPConcentradorComunicacao com;

    /*
     * -------------------------------------------------------------------------
     * Métodos gerais.
     * -------------------------------------------------------------------------
     */
    /**
     * Lista as portas seriais do sistema.
     *
     * @return String[]
     * @throws Exception
     */
    public final String[] getSeriaisDisponiveis() throws Exception {
        Enumeration listaDePortas = CommPortIdentifier.getPortIdentifiers();
        StringJoiner portas = new StringJoiner(";");

        while (listaDePortas.hasMoreElements()) {
            CommPortIdentifier ips = (CommPortIdentifier) listaDePortas.nextElement();
            if (ips.getName().toUpperCase().contains("COM")) {
                portas.add(ips.getName());
            }
        }

        return portas.toString().split(";");
    }

    /**
     * Método para a inicialização da impressora.
     *
     * @throws java.lang.Exception
     */
    public void start(CSPConcentradorComandosBase comand) throws Exception {
        this.loadLibCommunicationPort();

        info("Verificando porta de comunicação com concentrador...OK");
        for (String s : this.getSeriaisDisponiveis()) {
            setPorta(s);
            if ((Boolean) comand.enviaComandoConcentrador(CSPConcentradorComandosBase.Comandos.VERIFICA_COM, null)) {
                break;
            }
            setPorta(null);
        }

        info("Verificando porta de comunicação com concentrador...OK");
        if (getPorta() != null) {
            info("Porta de comunicação - " + getPorta());
        } else {
            info("Porta não encontrada");
        }
    }

    /**
     * Carrega a DLL para a comunicação com as postas (COM. USB etc.).
     */
    public void loadLibCommunicationPort() {
        if (CSPUtilidadesSO.getSO() == CSPUtilidadesSO.SO.SO_WINDOWS) {
            try {
                System.loadLibrary("rxtxParallel");
                System.loadLibrary("rxtxSerial");
            } catch (UnsatisfiedLinkError ex) {
                try {
                    throw new Exception(ex.getMessage());
                } catch (Exception e) {
                    CSPException.register(e);
                }
            }
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Getters e setters.
     * -------------------------------------------------------------------------
     */
    public String getPorta() {
        return porta;
    }

    public void setPorta(String porta) {
        this.porta = porta;
    }

    public CSPConcentradorComunicacao getCom() {
        return com;
    }

    public void setCom(CSPConcentradorComunicacao com) {
        this.com = com;
    }
}
