/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.*;
import java.util.LinkedHashSet;

/**
 * Efetua o 'dump' do DDL total da base
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 14:15:15
 */
public class CSPInstrucoesSQLExportaDDLTotal extends CSPInstrucoesSQLExportaTotalBase {

    public CSPInstrucoesSQLExportaDDLTotal(CSPInstrucoesSQLBase conn) {
        super(conn);
    }

    @Override
    public boolean export() throws Exception {
        final String exportIsql = exportDDLWithIsqlFb(getConn());
        final LinkedHashSet<String> r = new LinkedHashSet<>();
//
//        putToSave(exportIsql);
//        putToSave("=================================================================================================================================");
//        putToSave("=================================================================================================================================");
//        putToSave("=================================================================================================================================");

        putDDLCreateDomains(r, exportIsql);
        putDDLCreateGenerators(r, exportIsql);
        putDDLCreateExceptions(r, exportIsql);
        putDDLCreateTables(r, exportIsql);
        putDDLCreateIndices(r, exportIsql);
        putDDLCreateFks(r, exportIsql);
        putDDLCreateProceduresPart1(r, exportIsql);
        putDDLCreateComputedsBy(r, exportIsql);
        putDDLCreateViews(r, exportIsql);
        putDDLCreateProceduresPart2(r, exportIsql);
        putDDLCreateTriggers(r, exportIsql);
        putDDLCreateComments(r, exportIsql);

        r.forEach((d) -> {
            putToSave(d);
        });

        return true;
    }

}
