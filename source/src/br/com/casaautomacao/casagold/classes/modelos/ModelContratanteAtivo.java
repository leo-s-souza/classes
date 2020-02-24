/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para agente ativo
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public class ModelContratanteAtivo {

    private final ModelAgenteJuridico agente;
    private final ModelEndereco endereco;

    /**
     * 
     * @param agente
     * @param endereco
     */
    public ModelContratanteAtivo(ModelAgenteJuridico agente, ModelEndereco endereco) {
        this.agente = agente;
        this.endereco = endereco;
    }

    public ModelAgenteJuridico getAgente() {
        return agente;
    }

    public ModelEndereco getEndereco() {
        return endereco;
    }
}
