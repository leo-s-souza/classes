/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para bairro
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/01/2017 - 13:55:58
 */
public class ModelBairro {

    private final int id;
    private final String nome;
    private final ModelCidade cidade;

    /**
     *
     * @param id int - Id do bairro.
     * @param nome String - Nome do bairro.
     * @param cidade ModelCidade - Cidade relacionada ao bairro.
     */
    public ModelBairro(int id, String nome, ModelCidade cidade) {
        this.id = id;

        if (nome != null && nome.trim().isEmpty()) {
            nome = null;
        }
        this.nome = nome;

        this.cidade = cidade;
    }

    /**
     * Retorna o nome do bairro.
     *
     * @return String
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o Id do bairro.
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna a cidade relacionada ao bairro.
     *
     * @return ModelCidade
     */
    public ModelCidade getCidade() {
        return cidade;
    }

}
