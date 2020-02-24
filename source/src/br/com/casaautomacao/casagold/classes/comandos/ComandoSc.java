/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.comandos;

/**
 * Enums utilizado para a chamada das classes de comandos via reflection.
 *
 * @author cautomacao
 */
public enum ComandoSc {
    
    TESTE("teste", null),
    DMBA("DMBA", null),
    AAMG("AAMG", null),
    GSCV("GSCV", null),
    AVSC("AVSC", null);
    
    final public String idName;
    final private Class classe;

    ComandoSc(String idName, Class classe) {
        this.idName = idName;
        this.classe = classe;
    }

    public Class getClasse() {
        return classe;
    }
    
    public static ComandoSc identificaEnum(String nameId) {

        for (ComandoSc s : ComandoSc.values()) {
            if (s.idName.equals(nameId)) {
                return s;
            }
        }

        return null;
    }
}
