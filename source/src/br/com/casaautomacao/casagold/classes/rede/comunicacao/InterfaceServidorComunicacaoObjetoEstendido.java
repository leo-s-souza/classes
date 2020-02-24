/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.rede.comunicacao;

import java.net.Socket;
import org.json.JSONObject;

/**
 * Interface para auxiliar a recepção do client socket com suporte ao objeto
 * estendido
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 12/10/2015 - 14:29:36
 */
public interface InterfaceServidorComunicacaoObjetoEstendido {

    /**
     * Quando está sendo comunicado algo
     */
    public interface ComunicandoObjetoEstendido {

        /**
         * Quando está sendo comunicado algo
         *
         * @param sc Socket - Objeto Socket da conexão do cliente
         * @param input JSONObject - Requisição do monitor
         * @param coe ConexaoObjetoEstendido
         * @return JSONObject
         * @throws java.lang.Exception
         */
        public JSONObject run(Socket sc, JSONObject input, ConexaoObjetoEstendido coe) throws Exception;

    }

    /**
     * Quando o client desconetou
     */
    public interface DesconectadoObjetoEstendido {

        /**
         * Quando a conexão terminou com sucesso
         *
         * @param sc Socket - Objeto Socket da conexão do cliente
         * @param input JSONObject - Requisição do cliente
         * @param output JSONObject - Resposta para a requisição
         * @param coe ConexaoObjetoEstendido
         * @throws java.lang.Exception
         */
        public void run(Socket sc, JSONObject input, JSONObject output, ConexaoObjetoEstendido coe) throws Exception;

    }

    /**
     * Interface para o uso do recurso de objeto estendido
     */
    public interface ConexaoObjetoEstendido {

        /**
         * Chamado para iniciar o obejto
         *
         * @param sc Socket - Objeto Socket da conexão do cliente
         * @throws java.lang.Exception
         */
        public void start(Socket sc, JSONObject input) throws Exception;

        /**
         * Chamado na hora que a api determina que esse objeto precisa ser
         * destruído. O método é chamado sempre que a conexão é fechada, sendo
         * com sucesso ou não.
         *
         * @param sc Socket - Objeto Socket da conexão do cliente
         * @throws java.lang.Exception
         */
        public void free(Socket sc) throws Exception;

    }
}
