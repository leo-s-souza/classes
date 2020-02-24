/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

/**
 * Base das classes de sql parciais
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 14:20:57
 */
public abstract class CSPInstrucoesSQLExportaParcialBase extends CSPInstrucoesSQLExportaBase{

    private final CSPInstrucoesSQLBase connBaseOrigem;
    private final CSPInstrucoesSQLBase connBaseDestino;

    public CSPInstrucoesSQLExportaParcialBase(CSPInstrucoesSQLBase connBaseOrigem, CSPInstrucoesSQLBase connBaseDestino) {
        this.connBaseOrigem = connBaseOrigem;
        this.connBaseDestino = connBaseDestino;
    }

    protected CSPInstrucoesSQLBase getConnBaseDestino() {
        return connBaseDestino;
    }

    protected CSPInstrucoesSQLBase getConnBaseOrigem() {
        return connBaseOrigem;
    }

    
}
