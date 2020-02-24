/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo que representa as informações de uma tabela no banco
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 18/03/2017 - 11:44:17
 */
public class ModelTabela {

    /**
     * Nome da tabela.
     */
    final public String nome;

    /**
     * Numero de campos da tabela.
     */
    final public int numCampos;

    /**
     * Classe de Model de uma tabela do banco de dados.
     *
     * @param nome String - Nome da tabela.
     * @param numCampos int - Numero de campos da tabela.
     */
    public ModelTabela(String nome, int numCampos) {
        this.nome = nome;
        this.numCampos = numCampos;
    }

}
