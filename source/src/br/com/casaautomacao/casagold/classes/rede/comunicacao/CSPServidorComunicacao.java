/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.rede.comunicacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorComunicacao.Comunicando;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorComunicacao.Conectando;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorComunicacao.Desconectado;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorComunicacaoObjetoEstendido.ComunicandoObjetoEstendido;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorComunicacaoObjetoEstendido.ConexaoObjetoEstendido;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.InterfaceServidorComunicacaoObjetoEstendido.DesconectadoObjetoEstendido;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

/**
 * Classe para receber as comunicações do monitor gold
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 12/10/2015 - 14:29:36
 */
public class CSPServidorComunicacao {

    /**
     * Server socket
     *
     * @param servico CSPComunicacao.Servico - Serviço que será atendido
     * @param comunicando InterfaceServidorComunicacao.Comunicando - Quando a
     * resposta é preparada
     * @throws Exception
     */
    public void recebeClient(CSPComunicacao.Servico servico, Comunicando comunicando) throws Exception {
        this.recebeClient(servico, CSPServidorComunicacao.defaultConectando, comunicando, CSPServidorComunicacao.defaultDesconectado);
    }

    /**
     * Server socket
     *
     * @param servico CSPComunicacao.Servico - Serviço que será atendido
     * @param conectado InterfaceServidorComunicacao.Conectando - Quando o
     * client está conectando
     * @param comunicando InterfaceServidorComunicacao.Comunicando - Quando a
     * resposta é preparada
     * @throws Exception
     */
    public void recebeClient(CSPComunicacao.Servico servico, Conectando conectado, Comunicando comunicando) throws Exception {
        this.recebeClient(servico, conectado, comunicando, CSPServidorComunicacao.defaultDesconectado);
    }

    /**
     * Server socket
     *
     * @param servico CSPComunicacao.Servico - Serviço que será atendido
     *
     * @param comunicando InterfaceServidorComunicacao.Comunicando - Quando a
     * resposta é preparada
     * @param desconectado InterfaceServidorComunicacao.Desconetado - Quando o
     * cliente foi desconectado com sucesso
     * @throws Exception
     */
    public void recebeClient(CSPComunicacao.Servico servico, Comunicando comunicando, Desconectado desconectado) throws Exception {
        this.recebeClient(servico, CSPServidorComunicacao.defaultConectando, comunicando, desconectado);
    }

    /**
     * Server socket
     *
     * @param servico CSPComunicacao.Servico - Serviço que será atendido
     * @param conectado InterfaceServidorComunicacao.Conectando - Quando o
     * client está conectando
     * @param comunicando InterfaceServidorComunicacao.Comunicando - Quando a
     * resposta é preparada
     * @param desconectado InterfaceServidorComunicacao.Desconetado - Quando o
     * cliente foi desconectado com sucesso
     *
     * @throws Exception
     */
    public void recebeClient(CSPComunicacao.Servico servico, Conectando conectado, Comunicando comunicando, Desconectado desconectado) throws Exception {
        recebeClient(servico, conectado,null,
                (Socket sc, JSONObject input, InterfaceServidorComunicacaoObjetoEstendido.ConexaoObjetoEstendido coe)
                -> comunicando.run(sc, input),
                (Socket sc, JSONObject input, JSONObject output, InterfaceServidorComunicacaoObjetoEstendido.ConexaoObjetoEstendido coe) -> {
                    desconectado.run(sc, input, output);
                });
    }

    /**
     * Server socket
     *
     * @param servico CSPComunicacao.Servico - Serviço que será atendido
     * @param conectado InterfaceServidorComunicacao.Conectando - Quando o
     * client está conectando
     * @param classeObjetoEstendido Class - Objeto estendido
     * @param comunicando InterfaceServidorComunicacao.Comunicando - Quando a
     * resposta é preparada
     * @param desconectado InterfaceServidorComunicacao.Desconetado - Quando o
     * cliente foi desconectado com sucesso
     * @throws Exception
     */
    public void recebeClient(CSPComunicacao.Servico servico, Conectando conectado, Class classeObjetoEstendido, ComunicandoObjetoEstendido comunicando, DesconectadoObjetoEstendido desconectado) throws Exception {

        ServerSocket server = new ServerSocket(servico.port);

        //  this.backups.mkdirs();
        while (true) {

            Socket cliente = server.accept();
            new Thread(() -> {
                final String prefix = "SS(" + servico.sigla + "):" + servico.port + "<=>" + cliente.getRemoteSocketAddress();
                ConexaoObjetoEstendido objetoEstendido = null;
                CSPLog.info(prefix + "...");
                try {

                    final StringBuilder inputJson = new StringBuilder();
                    //  final DataInputStream in = new DataInputStream(cliente.getInputStream());

                    final BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream(), StandardCharsets.UTF_8));
                    final boolean isInNewMode = trataInputJson(inputJson, in, prefix);

                    final String strInput = inputJson.toString();
                    final long sizeInput = strInput.getBytes("UTF-8").length;
                    CSPLog.info(prefix + "(in: " + strInput + ";size:" + sizeInput + ")...");

                    ObjectOutputStream outOld = null;
                    BufferedWriter outNew = null;
                    JSONObject infos = new JSONObject(strInput);

                    if (isInNewMode) {
                        outNew = new BufferedWriter(new OutputStreamWriter(cliente.getOutputStream()));
                    } else {
                        outOld = new ObjectOutputStream(cliente.getOutputStream());
                    }

                    if (!conectado.run(cliente, infos)) {
                        throw new RefusedClient(prefix, strInput, sizeInput);
                    }
                    if (classeObjetoEstendido != null) {
                        objetoEstendido = (ConexaoObjetoEstendido) classeObjetoEstendido.newInstance();
                        objetoEstendido.start(cliente, infos);
                    }

                    JSONObject r = comunicando.run(cliente, infos, objetoEstendido);

                    if (r == null) {
                        r = new JSONObject();
                    }

                    if (isInNewMode && outNew != null) {
                        outNew.write(r.toString());
                        outNew.flush();
                    } else if (outOld != null) {
                        outOld.writeObject(r.toString());
                        outOld.flush();
                    }

                    CSPLog.info(prefix + "(out: " + r.toString() + ";size:" + r.toString().getBytes("UTF-8").length + ";new_mode:" + isInNewMode + ")...");

                    if (isInNewMode && outNew != null) {
                        outNew.close();
                    } else if (outOld != null) {
                        outOld.close();
                    }

                    cliente.close();
                    in.close();

                    desconectado.run(cliente, infos, r, objetoEstendido);

                    CSPLog.info(prefix + "...OK");
                } catch (Exception ex) {
                    CSPLog.error(prefix + "...ERROR");
                    CSPException.register(ex);
                    try {
                        cliente.close();
                    } catch (IOException ex1) {
                        CSPException.register(ex);
                    }
                } finally {
                    try {

                        if (objetoEstendido != null) {
                            objetoEstendido.free(cliente);
                        }
                    } catch (Exception ex) {
                        CSPException.register(ex);
                    }
                }

            }).start();

        }
    }

    /**
     * Realiza o tratamento para o input de informações e já retorna se o input
     * foi realizado no novo padrão
     *
     * @param inputJson StringBuilder - Destino das informações coletadas
     * @param in BufferedReader - Entrada da comunicação
     * @return
     * @throws java.io.Exception
     */
    protected boolean trataInputJson(final StringBuilder inputJson, final BufferedReader in, String prefix) throws Exception {

        final StringBuilder checkIsNewMode = new StringBuilder();

        boolean allowStartRead = false;

        if (in.markSupported()) {
            int count = 0;
            /**
             * Ficamos esperando até encontrarmos um valor válido para iniciar a
             * leitura
             */
            while (true) {
                in.mark(2000);
                char[] t = new char[1024];
                in.read(t);
                boolean bk = false;
                for (char u : t) {
                    if (u != 0x00) {
                        final String to = Character.toString(u);
                        if (to.equals("{")) {
                            in.reset();
                            bk = true;
                            break;
                        }
                    }
                }
                if (bk) {
                    in.reset();
                    break;
                }

                if (count >= 100) {
                    /**
                     * Porra, se depois de 100 ainda não tiver mandado nada, vai
                     * tomar banho
                     */
                    throw new ClientNoSendValidData(prefix, 0);
                }
                ++count;
            }

        }
        int countStartStr = 0;//{
        int countEndStr = 0;//}
        int countAspas = 0;//"

        //Variaveis de controle para inputs "arriscados"
        int countReads = -1;
        int posUltimaFechaChave = 0;

        /**
         * Vamos ficar lendo a entrada até detectarmos que o último character
         * recebido tenha sido um nullo
         */
        while (true) {
//            System.out.println();
            char[] t = new char[1024];
//            System.out.println("??????????");
            if (in.read(t) < 0) {
                break;
            }
//            System.out.println("------------");
            int countArriscaInput = -1;

            boolean eliminaChave = false;
            char previous = 0x00;
            for (char u : t) {
                countReads++;
                if (u != 0x00) {
//                    System.out.print(Character.toString(u));
                    //Antes de iniciar a leitura do json
                    if (!allowStartRead) {
                        final String to = Character.toString(u);
                        //Vamos tentar encontrar antes do json a string "new_mode:"
                        if (CSPUtilidadesLang.multipleComparationString(to, new String[]{
                            "n", "e", "w", "_", "m", "o", "d",/*"e",*/ ":"
                        })) {
                            checkIsNewMode.append(to);
                        }

                        //Só liberamos a leitura no { do padrão do objeto de json
                        //http://www.fileformat.info/info/unicode/char/fffd/index.htm
                        if (to.equals("{")) {
                            allowStartRead = true;
                        }
                    }
                    //Aqui sim iniciamos a leitura
                    //Tem que ficar fora do else fdp, pois se liberarmos acima vai cair aqui
                    if (allowStartRead) {
                        final String to = Character.toString(u);

                        inputJson.append(to);

                        countStartStr += to.equals("{") ? 1 : 0;

                        if (to.equals("}")) {
                            countEndStr++;
                            countArriscaInput = 0;
                            posUltimaFechaChave = countReads;
                        }

                        if (to.equals("\"")) {
                            if ('\\' != previous) {
                                countAspas++;
                            }
                        }
                    }
                } else if (countEndStr > 0) {

                    if (countAspas % 2 == 0) {
                        countArriscaInput++;
                    }
                    if (countArriscaInput >= 2) {
                        eliminaChave = true;
                    }
                }

                previous = u;
            }

//            System.out.println();
//            System.out.println(inputJson.toString());
            if (t.length == 0 || countStartStr <= countEndStr) {
                break;

            } else if (eliminaChave) {
                String bk = inputJson.toString().trim().replace("\r", "").replace("\n", "").trim();
                bk = bk.substring(1, bk.length());
                if (!bk.startsWith("{")) {
                    continue;
                }
                inputJson.setLength(0);
                inputJson.append(bk);

                /**
                 * Legenda:
                 *
                 * countStartStr => quantas { foram encontradas
                 *
                 * countEndStr => quantas } foram encontradas
                 *
                 * countReads => quantos char's foram lidos
                 *
                 * posUltimaFechaChave => qual a posição do último } dentro dos
                 * char's lidos
                 */
                CSPLog.error(prefix + "...ERROR(input-arriscado:" + countStartStr + ";" + countEndStr + ";" + countReads + ";" + posUltimaFechaChave + ";" + countAspas + ")");
                break;
            }

        }

        {
            String bk = inputJson.toString().trim().replace("\r", "").replace("\n", "").trim();
            inputJson.setLength(0);
            inputJson.append(bk);
            //bk = null;
        }

//        System.out.println("\n\n---------------------------------------------------------------------------");
        return checkIsNewMode.toString().equals("new_mode:");
    }

    protected static InterfaceServidorComunicacao.Conectando defaultConectando = null;

    protected static InterfaceServidorComunicacao.Desconectado defaultDesconectado = null;

    /**
     * Ação padrão a ser realizada para autenticar um client
     *
     * @param defaultConectando Conectando
     */
    public static void setDefaultConectando(Conectando defaultConectando) {
        CSPServidorComunicacao.defaultConectando = defaultConectando;
    }

    /**
     * Ação padrão a ser realizada ao desconectar um cliente
     *
     * @param defaultDesconectado Desconectado
     */
    public static void setDefaultDesconectado(Desconectado defaultDesconectado) {
        CSPServidorComunicacao.defaultDesconectado = defaultDesconectado;
    }

    public static class RefusedClient extends Exception {

        public RefusedClient(String prefix, String data, long size) {
            super(prefix + "(error:client-refused;in:" + data + ";size:" + size + ")");
        }

    }

    public static class ClientNoSendValidData extends Exception {

        public ClientNoSendValidData(String prefix, long size) {
            super(prefix + "(error:client-not-send-valid-data;size:" + size + ")");
        }

    }

}
