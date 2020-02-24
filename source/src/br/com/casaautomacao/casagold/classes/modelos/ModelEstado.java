/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para estado
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/01/2017 - 13:32:43
 */
public class ModelEstado {

    private final int id;
    private final String nome;
    private final String uf;
    private final ModelPais pais;

    public ModelEstado(int id, String nome, String uf, ModelPais pais) {
        this.id = id;

        if (nome != null && nome.trim().isEmpty()) {
            nome = null;
        }
        this.nome = nome;

        if (uf != null && uf.trim().isEmpty()) {
            uf = null;
        }
        this.uf = uf;

        this.pais = pais;
    }

    public String getNome() {
        return nome;
    }

    public String getUf() {
        return uf;
    }

    public int getId() {
        return id;
    }


    public ModelPais getPais() {
        return pais;
    }

}
