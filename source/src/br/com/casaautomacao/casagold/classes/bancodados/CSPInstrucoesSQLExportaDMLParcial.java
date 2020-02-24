/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDML;
import java.sql.SQLException;
import java.util.LinkedHashSet;

/**
 * Efetua o 'dump' do DML parcial da base, comparando os dados da base A com a
 * B.
 *
 * Ou seja, geral dml necessário para transformar a base A em B
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 14:19:54
 */
public class CSPInstrucoesSQLExportaDMLParcial extends CSPInstrucoesSQLExportaParcialBase {

    private final String[] tablesToWork;
    private final boolean forceDeleteDependentes;

    /**
     *
     * @param connVelhaBase CSPInstrucoesSQLBase
     * @param connNovaBase CSPInstrucoesSQLBase
     * @param forceDeleteDependentes boolean
     * @throws SQLException
     */
    public CSPInstrucoesSQLExportaDMLParcial(CSPInstrucoesSQLBase connVelhaBase, CSPInstrucoesSQLBase connNovaBase, boolean forceDeleteDependentes) throws SQLException {
        super(connVelhaBase, connNovaBase);

        LinkedHashSet<String> t = CSPUtilidadesLangInstrucoesSQLExportaDDL.getTabelasNomeOrdemInsert(connNovaBase);
        tablesToWork = t.toArray(new String[t.size()]);
        this.forceDeleteDependentes = forceDeleteDependentes;
    }

    /**
     *
     * @param connVelhaBase CSPInstrucoesSQLBase
     * @param connNovaBase CSPInstrucoesSQLBase
     * @param tablesToWork String[]
     * @param forceDeleteDependentes boolean
     */
    public CSPInstrucoesSQLExportaDMLParcial(CSPInstrucoesSQLBase connVelhaBase, CSPInstrucoesSQLBase connNovaBase, String[] tablesToWork, boolean forceDeleteDependentes) {
        super(connVelhaBase, connNovaBase);
        
        this.tablesToWork = tablesToWork;
        this.forceDeleteDependentes = forceDeleteDependentes;
    }

    @Override
    public boolean export() throws Exception {
        final LinkedHashSet<String> r = new LinkedHashSet<>();

        for (String tab : this.tablesToWork) {
            
            CSPUtilidadesLangInstrucoesSQLExportaDML.putDMLDiferencaTable(tab, getConnBaseOrigem(), getConnBaseDestino(), this.forceDeleteDependentes, r);
        }

        r.forEach((d) -> {
            putToSave(d);
        });

        return true;
    }

}
