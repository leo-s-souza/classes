/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para país
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/01/2017 - 11:45:10
 */
public class ModelPais {

    private final int id;
    private final String nome;

    public ModelPais(int id, String nome) {
        this.id = id;
        if (nome != null && nome.trim().isEmpty()) {
            nome = null;
        }
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }


}
