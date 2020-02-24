/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDML;
import java.util.LinkedHashSet;

/**
 * Efetua o 'dump' do DML total da base
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 14:19:34
 */
public class CSPInstrucoesSQLExportaDMLTotal extends CSPInstrucoesSQLExportaTotalBase {

    public CSPInstrucoesSQLExportaDMLTotal(CSPInstrucoesSQLBase conn) {
        super(conn);
    }

    @Override
    public boolean export() throws Exception {
        final LinkedHashSet<String> r = new LinkedHashSet<>();

        
        for (String tabela : CSPUtilidadesLangInstrucoesSQLExportaDDL.getTabelasNomeOrdemInsert(getConn())) {
            CSPUtilidadesLangInstrucoesSQLExportaDML.putDMLFullTable(getConn(), tabela, r);
        }
        
        for (String gen : CSPUtilidadesLangInstrucoesSQLExportaDDL.getGeneratorsNomes(getConn())) {
            CSPUtilidadesLangInstrucoesSQLExportaDML.putDMLGeneratorValue(getConn(), gen, r);
        }
        
        r.forEach((d) -> {
            putToSave(d);
        });

        return true;
    }

}
