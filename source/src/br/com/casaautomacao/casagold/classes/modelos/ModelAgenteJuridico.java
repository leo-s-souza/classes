/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.modelos;

/**
 * Modelo para agentes jurídicos
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public class ModelAgenteJuridico extends ModelAgente {

    private final String cnpj;
    private final String nomeFantasia;
    private final int inscricaoEstadual;
    private final String inscricaoMunicipal;

    /**
     *
     * @param id -id do agente
     * @param nome - nome do agente
     * @param cnpj - cnpj do agente jurídicos
     * @param nomeFantasia - nome fantasia do agente jurídicos
     */
    public ModelAgenteJuridico(int id, String nome, String cnpj, String nomeFantasia, int inscricaoEstadual, String inscricaoMunicipal) {
        super(id, nome);
        this.cnpj = cnpj;
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
        this.inscricaoMunicipal = inscricaoMunicipal;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public int getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public String getInscricaoMunicipal() {
        return inscricaoMunicipal;
    }

}
