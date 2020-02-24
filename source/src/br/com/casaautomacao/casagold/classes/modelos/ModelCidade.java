/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para cidade
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/01/2017 - 13:34:29
 */
public class ModelCidade {

    private final int id;
    private final String nome;
    private final String codigoIbge;
    private final ModelEstado uf;

    public ModelCidade(int id, String nome, String codigoIbge, ModelEstado uf) {
        this.id = id;
      
        if (nome != null && nome.trim().isEmpty()) {
            nome = null;
        }
        this.nome = nome;
        
        if (codigoIbge != null && codigoIbge.trim().isEmpty()) {
            codigoIbge = null;
        }
        this.codigoIbge = codigoIbge;

        this.uf = uf;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }


    public String getCodigoIbge() {
        return codigoIbge;
    }


    public ModelEstado getUf() {
        return uf;
    }


}
