/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.rede.comunicacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.json.JSONObject;

/**
 * Classe para comunicação com as centrais
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 12/10/2015 - 15:06:53
 */
public class CSPComunicacao {

    protected static ValidationLocal validadeDefault = null;
    protected static HostBuilder hostBuilderDefault = null;

    private static HashMap<String, String> dataDefault = new HashMap<>();

    public static HashMap<String, String> getDataDefault() {
        return dataDefault;
    }

    public enum Servico {

        /**
         * CMBOCV - Central/Serviço Módulo Backup Online Casa Visual
         */
        CMBOCV(ServicoAuxHost.CMG, 11224),
        /**
         * CMA - Central/Serviço Módulo Atualizador
         */
        CMA(ServicoAuxHost.CMG, 11225),
        /**
         * CMAICAF - Central/Serviço Módulo Atualizações de Informações dos
         * Contratantes App Food (App Food)
         */
        CMAICAF(ServicoAuxHost.CAPP, 11226),
        /**
         * CINFOS - Central/Serviço de Informações Gerais
         * (notificações,categorias, restaurantes....) (App Food)
         */
        CINFOS(ServicoAuxHost.CAPP, 22221),
        /**
         * CRPS - Central/Serviço de Registro de Pedidos (App Food)
         */
        CRPS(ServicoAuxHost.CAPP, 22222),
        /**
         * CTU - Central/Serviço de Tratamento de Usuários (App Food)
         */
        CTU(ServicoAuxHost.CAPP, 22223),
        /**
         * CMBOB - Central/Serviço Módulo Backup Online Bases
         */
        CMBOB(ServicoAuxHost.CMG, 11227),
        /**
         * CTASK_CMG - Central/Serviço Servidor Tarefas CMG
         */
        CTASK_CMG(ServicoAuxHost.CMG_TASK, 12974),
        /**
         * CTASK_CAPP - Central/Serviço Servidor Tarefas CAPP
         */
        CTASK_CAPP(ServicoAuxHost.CAPP_TASK, 12975),
        /**
         * CTASK_PDV - Central/Serviço Servidor Tarefas PDV
         */
        CTASK_PDV(ServicoAuxHost.LOCAL, 12976),
        /**
         * CTASK_RET - Central/Serviço Servidor Tarefas Retaguarda
         */
        CTASK_RET(ServicoAuxHost.LOCAL, 12977),
        /**
         * CTASK_MG - Central/Serviço Servidor Tarefas MG
         */
        CTASK_MG(ServicoAuxHost.LOCAL, 12978),
        /**
         * CCASRI - Central/Serviço Compartilhamento Arquivos Rede Interna
         */
        CCASRI(ServicoAuxHost.LOCAL, 33113),
        /**
         * CCAS - Central/Serviço Compartilhamento Arquivos
         */
        CCAS(ServicoAuxHost.CMG, 33114),
        /**
         * CCAS - Central/Serviço Compartilhamento Arquivos
         */
        CCAS_CAPP(ServicoAuxHost.CAPP, 43114),
        /**
         * CMGCAPI - Central/Serviço Monigor Gold Comunicação API 
         */ 
        CMGCAPI(ServicoAuxHost.CMG, 33134),
        /**
         * Testes - Testes com o socket, se já não estiver claro pra você
         */
        _TESTES(ServicoAuxHost.LOCAL, 9096);
        public final ServicoAuxHost hostType;
        public final int port;
        public final String sigla;

        private Servico(ServicoAuxHost host, int port) {
            this.hostType = host;
            this.port = port;
            this.sigla = this.name();
        }

    }

    /**
     * Comunica e retorna o que foi respondido
     *
     * @param servico Servico - Qual a servico a se comunicar
     * @param requsicao JSONObject - Informações para a requisição
     * @return JSONObject Resposta da requisição
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public JSONObject comunica(Servico servico, JSONObject requsicao) throws Exception {
        return this.comunicaByHostBuilder(servico, validadeDefault, requsicao);
//        return this.comunicaOtherHost(servico, getHostByHostBuilder(servico.hostType, servico.port, null, 0), requsicao);
    }

    /**
     * Comunica e retorna o que foi respondido
     *
     * @param servico Servico - Qual a servico a se comunica
     * @param validade
     * @param requsicao JSONObject - Informações para a requisição
     * @return JSONObject Resposta da requisição
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    public JSONObject comunica(Servico servico, ValidationLocal validade, JSONObject requsicao) throws Exception {
        return this.comunicaByHostBuilder(servico, validade, requsicao);
//        return this.comunicaOtherHost(servico, validade, getHostByHostBuilder(servico.hostType, servico.port, null, 0), requsicao);
    }

    /**
     * Comunica e retorna o que foi respondido
     *
     * @param servico
     * @param host
     * @param requsicao
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public JSONObject comunicaOtherHost(Servico servico, String host, JSONObject requsicao) throws Exception {
        return this.comunicaOtherHost(servico, CSPComunicacao.validadeDefault, host, requsicao);
    }

    /**
     * Auxilia no tratamento de conexões com mais de uma tentativa possível
     *
     * @param servico
     * @param validade
     * @param requsicao
     * @return
     */
    public JSONObject comunicaByHostBuilder(Servico servico, ValidationLocal validade, JSONObject requsicao) {
        JSONObject r = null;

        for (int i = 0; i < 5; i++) {

            try {

                r = comunicaOtherHost(servico, validade, getHostByHostBuilder(servico.hostType, servico.port, null, i), requsicao);

            } catch (Exception ex) {
                CSPException.register(ex);
                continue;
            }

            if (r != null) {
                break;
            }

        }

        return r;
    }

    /**
     * Comunica e retorna o que foi respondido
     *
     * @param servico
     * @param validade
     * @param host
     * @param requsicao
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public JSONObject comunicaOtherHost(Servico servico, ValidationLocal validade, String host, JSONObject requsicao) throws Exception {
        final String prefix = "S(" + servico.sigla + ")<=>(" + host + ":" + servico.port + ")";

        CSPLog.info(prefix + "...");

        if (validade != null && !validade.run(host, servico.port)) {
            CSPLog.error(prefix + "...ERROR(local-validation)");
            return null;
        }

        if (CSPUtilidadesLangRede.ping(host)) {

            Socket s = new Socket();

            try {
                s.connect(new InetSocketAddress(host, servico.port), 5000);
            } catch (Exception e) {

                CSPException.register(e, "exception-no-port");

                CSPLog.error(prefix + "...ERROR(no-port)");//Porta não acessível

                return null;

            }
            
            ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());

            for (Map.Entry<String, String> def : dataDefault.entrySet()) {
                if (!requsicao.has(def.getKey())) {
                    requsicao.put(def.getKey(), def.getValue());
                }
            }

            out.writeObject(requsicao.toString());
           
            out.flush();

            ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        

            ExecutorService es = Executors.newCachedThreadPool();
            Object[] rdO = new Object[]{null};

            es.execute(() -> {
                try {
                    rdO[0] = in.readObject();
                } catch (IOException | ClassNotFoundException ex) {
                    CSPException.register(ex);
                }
            });

            es.shutdown();

            if (!es.awaitTermination(45, TimeUnit.SECONDS)) {

                CSPLog.error(prefix + "...ERROR(out-timout)");//Conexão/servidor demorou a responder

                return null;
            }

            if (rdO[0] != null) {

                JSONObject resposta = new JSONObject((String) rdO[0]);

                s.close();

                CSPLog.info(prefix + "...OK");

                return resposta;

            } else {

                CSPLog.error(prefix + "...ERROR(out-null)");//Resposta nulla vinda do servidor

                return null;

            }

        }

        CSPLog.error(prefix + "...ERROR(no-ping)");//Servidor não responde ao PING

        return null;

    }

    /**
     * Configura as informações padrões que o socket vai enviar nas requisições
     *
     * @param dataDefault HashMap<String, String> - Infomrações (CNPJ, MAC.....)
     */
    public static void setDataDefault(HashMap<String, String> dataDefault) {
        CSPComunicacao.dataDefault = dataDefault;
    }

    /**
     * Seta a validação default que será efetuada por padrão para as requisições
     *
     * @param validadeDefault
     */
    public static void setValidadeDefault(ValidationLocal validadeDefault) {
        CSPComunicacao.validadeDefault = validadeDefault;
    }

    /**
     * Seta o controle de hosts default
     *
     * @param hostBuilderDefault
     */
    public static void setHostBuilderDefault(HostBuilder hostBuilderDefault) {
        CSPComunicacao.hostBuilderDefault = hostBuilderDefault;
    }

    /**
     * Realiza o devido tratamento e retorna o host indicado
     *
     * @param serType ServicoAuxHost
     * @param port int
     * @param hostCustom String
     * @return
     * @throws Exception
     */
    public static String getHostByHostBuilder(ServicoAuxHost serType, int port, String hostCustom, int tentativas) throws Exception {
        if (hostBuilderDefault != null) {
            String host = hostBuilderDefault.host(tentativas, serType, port);
            if (host != null) {
                return host;
            }
        }

        return hostCustom;
    }

    /**
     * Interface para validação das comunicações
     */
    public interface ValidationLocal {

        /**
         * Validação
         *
         * @param host String - Host que esta sendo tentado conectar
         * @param port int - Porta do host que esta sendo tentado conectar
         * @return
         * @throws Exception
         */
        public boolean run(String host, int port) throws Exception;
    }

    /**
     * Interface para tratamento de hosts externos
     */
    public interface HostBuilder {

        /**
         * Retorna qual o host deve ser conectado
         *
         * @param numTentativa int - Número de tentativas
         * @param servType ServicoAuxHost
         * @param port int - Porta do host que esta sendo tentado conectar
         * @return String - Host ou "null" em caso de hosts personalizados
         * @throws Exception
         */
        public String host(int numTentativa, ServicoAuxHost servType, int port) throws Exception;
    }

    public enum ServicoAuxHost {

        CMG,
        CAPP,
        CMG_TASK,
        CAPP_TASK,
        LOCAL
    }
}
