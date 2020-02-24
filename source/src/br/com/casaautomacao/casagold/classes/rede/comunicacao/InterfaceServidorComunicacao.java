/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.rede.comunicacao;

import java.net.Socket;
import org.json.JSONObject;

/**
 * Interface para auxiliar a recepção do monitor gold
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 12/10/2015 - 14:29:36
 */
public interface InterfaceServidorComunicacao {

    /**
     * Quando está iniciando a conexão
     */
    public interface Conectando {

        /**
         * Quando está sendo iniciada a conexão
         *
         * @param sc Socket - Objeto Socket da conexão do cliente
         * @param input JSONObject - Requisição do monitor
         * @return boolean - Retorna se comunicação está liberada
         *
         * @throws java.lang.Exception
         */
        public boolean run(Socket sc, JSONObject input) throws Exception;

    }

    /**
     * Quando está sendo comunicado algo
     */
    public interface Comunicando {

        /**
         * Quando está sendo comunicado algo
         *
         * @param sc Socket - Objeto Socket da conexão do cliente
         * @param input JSONObject - Requisição do monitor
         * @return JSONObject
         * @throws java.lang.Exception
         */
        public JSONObject run(Socket sc, JSONObject input) throws Exception;

    }

    /**
     * Quando o client desconetou
     */
    public interface Desconectado {

        /**
         * Quando a conexão terminou com sucesso
         *
         * @param sc Socket - Objeto Socket da conexão do cliente
         * @param input JSONObject - Requisição do cliente
         * @param output JSONObject - Resposta para a requisição
         * @throws java.lang.Exception
         */
        public void run(Socket sc, JSONObject input, JSONObject output) throws Exception;

    }
}
