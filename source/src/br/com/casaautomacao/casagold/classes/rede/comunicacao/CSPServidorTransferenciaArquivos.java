/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.rede.comunicacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorTransferenciaArquivos.Conectando;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorTransferenciaArquivos.Desconectado;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.getMd5;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime.getTempoCompletoLimpo;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Classe para transferir/servir arquivos ao cliente
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 12/10/2015 - 14:29:36
 */
public class CSPServidorTransferenciaArquivos extends CSPServidorComunicacao {

    /**
     * Prepara o arquivo para o download pelo cliente
     *
     * @param servico CSPComunicacao.Servico - Servico a ser executada
     * @param pastaBase String - Pasta base dos arquivos
     * @param conectando InterfaceServidorTransferenciaArquivos.Conectando -
     * Quando o client está conectando
     * @throws java.io.IOException
     */
    public void serveArquivo(CSPTransferenciaArquivos.Servico servico, String pastaBase, Conectando conectando) throws Exception {
        this.serveArquivo(servico, pastaBase, conectando, CSPServidorTransferenciaArquivos.defaultDesconectadoFile);
    }

    /**
     * Prepara o arquivo para o download pelo cliente
     *
     * @param servico CSPComunicacao.Servico - Servico a ser executada
     * @param pastaBase String - Pasta base dos arquivos
     * @param desconectado InterfaceServidorTransferenciaArquivos.Desconetado -
     * Quando o cliente foi desconectado com sucesso
     * @throws java.io.IOException
     */
    public void serveArquivo(CSPTransferenciaArquivos.Servico servico, String pastaBase, Desconectado desconectado) throws Exception {
        this.serveArquivo(servico, pastaBase, CSPServidorTransferenciaArquivos.defaultConectandoFile, desconectado);
    }

    /**
     * Prepara o arquivo para o download pelo cliente
     *
     * @param servico CSPComunicacao.Servico - Servico a ser executada
     * @param pastaBase String - Pasta base dos arquivos
     * @param conectando InterfaceServidorTransferenciaArquivos.Conectando -
     * Quando o client está conectando
     * @param desconectado InterfaceServidorTransferenciaArquivos.Desconetado -
     * Quando o cliente foi desconectado com sucesso
     * @throws java.io.IOException
     */
    public void serveArquivo(CSPTransferenciaArquivos.Servico servico, String pastaBase, Conectando conectando, Desconectado desconectado) throws Exception {
      
        final int porta = servico.port;
        final String sigla = servico.sigla;
        final ServerSocket serv = new ServerSocket(porta);
        //  this.backups.mkdirs();
        while (true) {
            Socket cliente = serv.accept();
            new Thread(() -> {
                final String prefix = "SS(" + sigla + "):" + porta + "=>file=>" + cliente.getRemoteSocketAddress();
              
                CSPLog.info(prefix + "...");
                try {

                    final StringBuilder inputJson = new StringBuilder();

                    final BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
                    final boolean isInNewMode = trataInputJson(inputJson, in, prefix);

                    final String inputStr = inputJson.toString();
                    CSPArquivos ar = new CSPArquivos(pastaBase);

                    JSONObject infos = new JSONObject();
                    try {
                        infos = new JSONObject(inputStr);

                        ar.setPath(pastaBase + "/" + infos.getString("FILE_NAME"));//Padrao
                    } catch (JSONException e) {
                        /**
                         * Tentamos ler em JSON, mas como nem todos os clients
                         * rodam o padrão novo precisamos manter a
                         * compatibilidade aceitando o input da informa na forma
                         * antiga
                         */
                        ar.setPath(pastaBase + "/" + inputStr);
                        infos.put("FILE_NAME", inputStr);
                    }

                    ///infos.remove("FILE_NAME");
                    CSPLog.info(prefix + "(file:" + ar.getAbsolutePath() + ";data:" + inputJson + ";size:" + inputJson.toString().getBytes("UTF-8").length + ")...");
                    if (!conectando.run(cliente, ar, infos)) {

                        throw new CSPServidorComunicacao.RefusedClient(prefix, inputStr, inputStr.getBytes("UTF-8").length);
                    }

                    FileInputStream inFile = ar.objFileInputStream();
                    if (inFile != null) {
                        DataInputStream arq = new DataInputStream(inFile);
                        byte buffer[] = new byte[512];
                        DataOutputStream outOld = new DataOutputStream(cliente.getOutputStream());
                        outOld.flush();
                        int leitura = arq.read(buffer);
                        while (leitura != - 1) {
                            if (leitura != - 2) {
                                outOld.write(buffer, 0, leitura);
                            }
                            leitura = arq.read(buffer);
                        }
                        outOld.close();
                        inFile.close();
                        CSPLog.info(prefix + "(size:" + ar.length() + ";file:" + ar.getAbsolutePath() + ")...OK");
                    } else {
                        CSPLog.error(prefix + "(file:" + ar.getAbsolutePath() + ")...NO(file-input-stream-null)");
                    }

                    cliente.close();

                    desconectado.run(cliente, ar, infos);

                    CSPLog.info(prefix + "...OK");
                } catch (Exception ex) {
                    CSPLog.error(prefix + "...ERROR");
                    CSPException.register(ex);
                    try {
                        cliente.close();
                    } catch (IOException ex1) {
                        CSPException.register(ex);
                    }
                }
            }).start();

        }
    }

    /**
     * Recebe o arquivo enviado pelo cliente
     *
     * @param servico CSPComunicacao.Servico - Servico a ser executada
     *
     * @param pastaBase String - Pasta base de destino dos arquivos
     *
     * @param conectando InterfaceServidorTransferenciaArquivos.Conectando -
     * Quando o client está conectando
     */
    public void recebeArquivo(CSPTransferenciaArquivos.Servico servico, String pastaBase, Conectando conectando) throws Exception {
        this.recebeArquivo(servico, pastaBase, conectando, CSPServidorTransferenciaArquivos.defaultDesconectadoFile);
    }

    /**
     * Recebe o arquivo enviado pelo cliente
     *
     * @param servico CSPComunicacao.Servico - Servico a ser executada
     *
     * @param pastaBase String - Pasta base de destino dos arquivos
     *
     * @param desconectado InterfaceServidorTransferenciaArquivos.Desconetado -
     * Quando o cliente foi desconectado com sucesso
     */
    public void recebeArquivo(CSPTransferenciaArquivos.Servico servico, String pastaBase, Desconectado desconectado) throws Exception {
        this.recebeArquivo(servico, pastaBase, CSPServidorTransferenciaArquivos.defaultConectandoFile, desconectado);
    }

    /**
     * Recebe o arquivo enviado pelo cliente
     *
     * @param servico CSPComunicacao.Servico - Servico a ser executada
     *
     * @param pastaBase String - Pasta base de destino dos arquivos
     *
     * @param conectando InterfaceServidorTransferenciaArquivos.Conectando -
     * Quando o client está conectando
     * @param desconectado InterfaceServidorTransferenciaArquivos.Desconetado -
     * Quando o cliente foi desconectado com sucesso
     */
    public void recebeArquivo(CSPTransferenciaArquivos.Servico servico, String pastaBase, Conectando conectando, Desconectado desconectado) throws Exception {
        final int porta = servico.port;
        final String sigla = servico.sigla;
        final ServerSocket serv = new ServerSocket(porta);

        while (true) {

            Socket cliente = serv.accept();
            new Thread(() -> {
                final String prefix = "SS(" + sigla + "):" + porta + "<=file<=" + cliente.getRemoteSocketAddress();

                CSPLog.info(prefix + "...");
                try {
                    String name = getMd5(getTempoCompletoLimpo() + new Random().nextInt(100));
                    CSPArquivos ar = new CSPArquivos(pastaBase + "/" + name);
                    ar.setContent(name);
                    if (!conectando.run(cliente, ar, new JSONObject())) {
                        throw new CSPServidorComunicacao.RefusedClient(prefix, null, 0);
                    }
                    CSPLog.info(prefix + "(file:" + ar.getAbsolutePath() + ")...");
                    DataInputStream entrada = new DataInputStream(cliente.getInputStream());
                    FileOutputStream sarq = new FileOutputStream(pastaBase + "/" + name);
                    byte[] br = new byte[512];
                    int leitura
                            = entrada.read(br);
                    while (leitura != -1) {
                        if (leitura != -2) {
                            sarq.write(br, 0, leitura);
                        }
                        leitura = entrada.read(br);
                    }
                    entrada.close();
                    sarq.close();
                    cliente.close();

                    CSPLog.info(prefix + "(size:" + ar.length() + ";file:" + ar.getAbsolutePath() + ")...OK");

                    desconectado.run(cliente, ar, new JSONObject());
                    /* toStore.put("DATA_HORA_FIM", CSPUtilidadesLang.formataDataHora(new Date(), "yyyy/MM/dd HH:mm:ss.SSS"));
                     this.backups.setObject(toStore);*/
                    CSPLog.info(prefix + "...OK");
                } catch (Exception ex) {
                    CSPLog.error(prefix + "...ERROR");
                    CSPException.register(ex);
                    try {
                        cliente.close();
                    } catch (IOException ex1) {
                        CSPException.register(ex);
                    }
                }
            }).start();

        }
    }

    protected static InterfaceServidorTransferenciaArquivos.Conectando defaultConectandoFile = null;

    protected static InterfaceServidorTransferenciaArquivos.Desconectado defaultDesconectadoFile = null;

    /**
     * Ação padrão a ser realizada para autenticar um client
     *
     * @param defaultConectando Conectando
     */
    public static void setDefaultConectando(InterfaceServidorTransferenciaArquivos.Conectando defaultConectando) {
        CSPServidorTransferenciaArquivos.defaultConectandoFile = defaultConectando;
    }

    /**
     * Ação padrão a ser realizada ao desconectar um cliente
     *
     * @param defaultDesconectado Desconectado
     */
    public static void setDefaultDesconectado(InterfaceServidorTransferenciaArquivos.Desconectado defaultDesconectado) {
        CSPServidorTransferenciaArquivos.defaultDesconectadoFile = defaultDesconectado;
    }

}
