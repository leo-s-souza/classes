/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 *
 *
 * @author Matheus Felipe Amelco <producao5@casaautomacao.com.br>
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 12/01/2017 - 15:47:23
 */
public class ModelEndereco {

    private final int id;
    private final String logradouro;
    private final String complemento;
    private final String referencia;
    private final String numero;
    private final String cep;
    private double latitude;
    private double longitude;
    private final ModelBairro bairro;

    public ModelEndereco(int id, String logradouro, String complemento, String referencia, String numero, String cep, double latitude, double longitude, ModelBairro bairro) {
        this.id = id;

        if (logradouro != null && logradouro.trim().isEmpty()) {
            logradouro = null;
        }
        this.logradouro = logradouro;

        if (complemento != null && complemento.trim().isEmpty()) {
            complemento = null;
        }

        this.complemento = complemento;

        this.referencia = referencia;

        if (cep != null && cep.trim().isEmpty()) {
            cep = null;
        }
        this.cep = cep;

        this.numero = numero;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bairro = bairro;
    }

    public ModelEndereco(int id, String logradouro, String complemento, String referencia, String numero, String cep, double latitude, double longitude,
            int bairroId,
            String bairroNome,
            int cidadeId,
            String cidadeNome,
            int estadoId,
            String estadoNome,
            String estadoSigla,
            int paisId,
            String paisNome
    ) {
        this(id, logradouro, complemento, referencia, numero, cep, latitude, longitude,
                new ModelBairro(
                        bairroId,
                        bairroNome,
                        new ModelCidade(
                                cidadeId,
                                cidadeNome,
                                null,
                                new ModelEstado(
                                        estadoId,
                                        estadoNome,
                                        estadoSigla,
                                        new ModelPais(
                                                paisId,
                                                paisNome
                                        )
                                )
                        )
                )
        );
    }

    public int getId() {
        return id;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getCep() {
        return cep;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getReferencia() {
        return referencia;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public ModelBairro getBairro() {
        return bairro;
    }

    public ModelCidade getCidade() {
        return getBairro().getCidade();
    }

    public ModelEstado getEstado() {
        return getCidade().getUf();
    }

    public ModelPais getPais() {
        return getEstado().getPais();
    }

    /**
     *
     * @param latitude
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     *
     * @param longitude
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    /**
     *
     * @return
     */
    public String getEnderecoFormatado() {
        StringBuilder sb = new StringBuilder();

        sb.append(this.logradouro);
        sb.append(", ");
        sb.append(this.numero);
        sb.append(" ");
        sb.append(this.bairro.getNome());
        sb.append(" ");
        sb.append(this.bairro.getCidade().getNome());
        sb.append(" ");
        sb.append(this.bairro.getCidade().getUf().getNome());
        sb.append(" ");
        sb.append(this.bairro.getCidade().getUf().getPais().getNome());

        return sb.toString();
    }

}
