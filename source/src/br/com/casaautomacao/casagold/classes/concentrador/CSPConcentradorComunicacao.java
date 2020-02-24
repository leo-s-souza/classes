/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.concentrador;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Classe responsavel por fazer a comunicação com o concentrador conectado na
 * porta com.
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public class CSPConcentradorComunicacao extends CSPConcentradorBase implements Runnable, SerialPortEventListener {

    /*
     * -------------------------------------------------------------------------
     * Variáveis.
     * -------------------------------------------------------------------------
     */
    private CommPortIdentifier cp;

    private SerialPort serialPort;

    private OutputStream saida;

    private InputStream entrada;

    private Thread threadLeitura;

    private boolean escrita;

    protected String resposta;

    private CSPConcentradorComandosBase comand;

    public CSPConcentradorComunicacao(CSPConcentradorComandosBase com) throws Exception {
        setComand(com);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            CSPException.register(e);
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Métodos gerais.
     * -------------------------------------------------------------------------
     */
    @Override
    public void serialEvent(SerialPortEvent ev) {

        StringBuffer bufferLeitura = new StringBuffer();

        int novoDado = 0;

        switch (ev.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:

                break;

            case SerialPortEvent.DATA_AVAILABLE:

                //Novo algoritmo de leitura.
                while (novoDado != -1) {

                    try {

                        novoDado = getEntrada().read();

                        if (novoDado == -1) {
                            break;
                        }

                        bufferLeitura.append((char) novoDado);

                    } catch (IOException ioe) {

                        CSPException.register(ioe);

                    }
                }

                setResposta(new String(bufferLeitura));

                break;
        }
    }

    private void habilitarEscrita() {
        setEscrita(true);
    }

    private void habilitarLeitura() {
        setEscrita(false);
    }

    /**
     * Abre uma conexão com a porta com.
     *
     * @throws Exception
     */
    private void abrirPorta() throws Exception {

        CSPLog.info(this.getClass(), "Abrindo conexão com porta " + getComand().getPorta() + "...");

        setCp(CommPortIdentifier.getPortIdentifier(getComand().getPorta()));

        setSerialPort((SerialPort) getCp().open("SerialComLeitura", 0));

        //configurar parâmetros
        getSerialPort().setSerialPortParams(getSerialPort().getBaudRate(),
                SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1,
                SerialPort.PARITY_NONE);

        getSerialPort().setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

        CSPLog.info(this.getClass(), "Abrindo conexão com porta " + getComand().getPorta() + "...OK");
    }

    /**
     * Método habilita a leitura dos dados da porta com.
     *
     * @throws Exception
     */
    private void lerDados() throws Exception {

        if (!isEscrita()) {

            setEntrada(getSerialPort().getInputStream());

            getSerialPort().addEventListener(this);
            getSerialPort().notifyOnDataAvailable(true);

            setThreadLeitura(new Thread(this));
            getThreadLeitura().start();

            run();
        }
    }

    /**
     * Envia um comando para a porta com.
     *
     * @param cmd - comando a ser enviado para a porta com.
     */
    private void enviarComando(String cmd) {

        if (isEscrita()) {

            try {
                setSaida(getSerialPort().getOutputStream());

                CSPLog.info(this.getClass(), "Enviando comando " + cmd + " para porta " + getComand().getPorta() + "...");

                getSaida().write(cmd.getBytes());

                Thread.sleep(100);

                getSaida().flush();
                CSPLog.info(this.getClass(), "Enviando comando " + cmd + " para porta " + getComand().getPorta() + "...OK");

            } catch (Exception e) {
                CSPLog.info(this.getClass(), "Houve um erro durante o envio do comando " + cmd + " para porta " + getComand().getPorta() + "...");

                CSPException.register(e);
            }
        }
    }

    /**
     * Fecha a comunicação com a porta com.
     */
    public void fecharCom() {
        getSerialPort().close();
    }

    /**
     * Envia um comando para o concentrador e retorna a resposta.
     *
     * @param cmd - comando que vai ser enviado para o concentrador.
     * @param sleepTime int - utilizado para saber quanto tempo é necessário
     * esperar até que o comando tenha recebido a resposta(milisegundos).
     *
     * Tempos de resposta cbc em:
     * http://ptdocz.com/doc/188403/protocolo-de-comunica%C3%A7%C3%A3o-companytec
     *
     * @return
     * @throws Exception
     */
    public String enviaComando(String cmd, int sleepTime) throws Exception {

        habilitarEscrita();

        abrirPorta();

        enviarComando(cmd);

        habilitarLeitura();
        lerDados();

        //Controle de tempo da leitura aberta na serial
        Thread.sleep(sleepTime);

        String resp = getResposta();

        fecharCom();

        return resp;
    }

    /*
     * -------------------------------------------------------------------------
     * Getters e Setters.
     * -------------------------------------------------------------------------
     */
    public CommPortIdentifier getCp() {
        return cp;
    }

    public void setCp(CommPortIdentifier cp) {
        this.cp = cp;
    }

    public SerialPort getSerialPort() {
        return serialPort;
    }

    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    public OutputStream getSaida() {
        return saida;
    }

    public void setSaida(OutputStream saida) {
        this.saida = saida;
    }

    public InputStream getEntrada() {
        return entrada;
    }

    public void setEntrada(InputStream entrada) {
        this.entrada = entrada;
    }

    public Thread getThreadLeitura() {
        return threadLeitura;
    }

    public void setThreadLeitura(Thread threadLeitura) {
        this.threadLeitura = threadLeitura;
    }

    public boolean isEscrita() {
        return escrita;
    }

    public void setEscrita(boolean escrita) {
        this.escrita = escrita;
    }

    public void setResposta(String resposta) {
        this.resposta = resposta;
    }

    public String getResposta() {
        return resposta;
    }

    private CSPConcentradorComandosBase getComand() {
        return comand;
    }

    private void setComand(CSPConcentradorComandosBase comand) {
        this.comand = comand;
    }

}
