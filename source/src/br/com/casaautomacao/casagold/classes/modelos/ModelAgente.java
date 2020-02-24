/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para agentes
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public class ModelAgente {
    
    private final int id;
    private final String nome;

    /**
     * 
     * @param id -id do agente
     * @param nome - nome do agente
     */
    public ModelAgente(int id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }
}
