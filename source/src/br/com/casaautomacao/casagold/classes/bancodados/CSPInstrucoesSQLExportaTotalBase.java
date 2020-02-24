/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

/**
 * Base das classes de sql totais
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 14:20:57
 */
public abstract class CSPInstrucoesSQLExportaTotalBase extends CSPInstrucoesSQLExportaBase{

    private final CSPInstrucoesSQLBase conn;

    public CSPInstrucoesSQLExportaTotalBase(CSPInstrucoesSQLBase conn) {
        this.conn = conn;
    }

    protected CSPInstrucoesSQLBase getConn() {
        return conn;
    }

    
}

