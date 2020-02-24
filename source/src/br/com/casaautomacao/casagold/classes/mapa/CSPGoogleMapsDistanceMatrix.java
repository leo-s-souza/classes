/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.mapa;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.modelos.ModelEndereco;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.superNormalizeString;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson.getFromJson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

/**
 * Classe destinada a comunicação e requisição de dados da API Maps da Google.
 *
 * @author Matheus Felipe Amelco <producao5@casaautomacao.com.br>
 * @date 12/01/2017 - 14:56:58
 */
public class CSPGoogleMapsDistanceMatrix {

    /**
     * JSON com o resultado do request.
     */
    private final JSONObject resultado;

    /**
     * Chave de utilização da API Google Maps Distance Matrix
     */
    private final String googleAPIKey = "AIzaSyBtU25nbrTfAIeSv-OnAbtUfgBFpaBWF_E";

    /**
     * Classe destinada a comunicação e requisição de dados da API Maps da
     * Google.
     *
     * @param enderecoOrigem ModelEndereco - ModelEndereco de origem.
     * @param enderecoDestino ModelEndereco - ModelEndereco de destino.
     * @throws Exception
     */
    public CSPGoogleMapsDistanceMatrix(ModelEndereco enderecoOrigem, ModelEndereco enderecoDestino) throws Exception {
        StringBuilder request = new StringBuilder();
        StringBuilder result = new StringBuilder();

        //Monta o link da requisição HTTP.
        request.append("https://maps.googleapis.com/maps/api/distancematrix/json?");
        request.append("origins=");
        request.append(superNormalizeString(enderecoOrigem.getLogradouro()).replace(" ", "%20"));
        request.append("+");
        request.append(enderecoOrigem.getNumero());
        request.append("+");
        request.append(superNormalizeString(enderecoOrigem.getBairro().getNome()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoOrigem.getBairro().getCidade().getNome()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoOrigem.getBairro().getCidade().getUf().getNome()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoOrigem.getCep()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoOrigem.getBairro().getCidade().getUf().getPais().getNome()).replace(" ", "%20"));
        request.append("&destinations=");
        request.append(superNormalizeString(enderecoDestino.getLogradouro()).replace(" ", "%20"));
        request.append("+");
        request.append(enderecoDestino.getNumero());
        request.append("+");
        request.append(superNormalizeString(enderecoDestino.getBairro().getNome()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoDestino.getBairro().getCidade().getNome()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoDestino.getBairro().getCidade().getUf().getNome()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoDestino.getCep()).replace(" ", "%20"));
        request.append("+");
        request.append(superNormalizeString(enderecoDestino.getBairro().getCidade().getUf().getPais().getNome()).replace(" ", "%20"));
        request.append("&mode=driving");
        request.append("&language=pt-BR");
        request.append("&key=");
        request.append(this.googleAPIKey);

        //Envia a requisição HTTP.
        HttpURLConnection conn = (HttpURLConnection) new URL(request.toString()).openConnection();

        CSPLog.info(CSPGoogleMapsDistanceMatrix.class, "requisicao-distance-matrix-google-maps: " + request.toString());

        //Lê a resposta e escreve no StringBuilder
        if (conn != null) {
            try (BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;

                while ((line = rd.readLine()) != null) {
                    result.append(line);
                }

                rd.close();

            } catch (Exception e) {
                CSPLog.error(CSPGoogleMapsDistanceMatrix.class, "falha-requisicao-distance-matrix-google-maps");
                CSPException.register(e);
            }

        }
        //Define o JSONOBject da resposta.
        if (!result.toString().trim().isEmpty()) {
            this.resultado = new JSONObject(result.toString());

        } else {
            this.resultado = new JSONObject();
        }
    }

    /**
     * Retorna se todos os destinos foram encontrados com sucesso.
     *
     * @return String - "OK" -> indica que a resposta é válida.
     * "INVALID_REQUEST" -> indica que a solicitação fornecida é inválida.
     */
    public String getStatus() {
        if ("OK".equals(getFromJson(this.resultado, "status", "INVALID_REQUEST").toUpperCase())
                && "OK".equals(getFromJson(this.getElements(), "status", "NOT_FOUND").toUpperCase())) {

            return "OK";
        }

        return "INVALID_REQUEST";
    }

    /**
     * Retorna a distância em kilometros.
     *
     * @return String - Valor já formatado em kilometros (Ex.: "2,5 Km")
     */
    public String getDistancia() {
        if ("OK".equals(getStatus())) {
            return this.getElements().getJSONObject("distance").getString("text");
        }

        return "";
    }

    /**
     * Retorna a distância em metros.
     *
     * @return int - Valor absoluto em metros. (Ex.: 1234)
     */
    public int getDistanciaEmMetros() {
        if ("OK".equals(getStatus())) {
            return this.getElements().getJSONObject("distance").getInt("value");
        }

        return 0;
    }

    /**
     * Retorna a duracao da viagem em minutos ou horas.
     *
     * @return String - Valor já formatado em minutos ou horas (Ex.: "5 minutos"
     * ou "2 horas")
     */
    public String getDuracao() {
        if ("OK".equals(getStatus())) {
            return this.getElements().getJSONObject("duration").getString("text");
        }

        return "";
    }

    /**
     * Retorna a duração da viagem em segundos.
     *
     * @return int - Valor absoluto em segundos. (Ex.: 589)
     */
    public int getDuracaoEmSegundos() {
        if ("OK".equals(getStatus())) {
            return this.getElements().getJSONObject("duration").getInt("value");
        }

        return 0;
    }

    /**
     * Retorna o JSONObject de resultado na requisição.
     *
     * @return JSONObject - Resultado da Requisição.
     */
    private JSONObject getElements() {
        return this.resultado.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").getJSONObject(0);
    }

}
