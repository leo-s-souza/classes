/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

import br.com.casaautomacao.casagold.classes.modelos.ModelColunaTabela;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;

/**
 * Efetua o 'dump' do DDL parcial da base, comparando a estrutura da base A com
 * a B.
 *
 * Ou seja, geral ddl necessário para transformar a base A em B
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 14:18:28
 */
public class CSPInstrucoesSQLExportaDDLParcial extends CSPInstrucoesSQLExportaParcialBase {

    public CSPInstrucoesSQLExportaDDLParcial(CSPInstrucoesSQLBase connBaseOrigem, CSPInstrucoesSQLBase connBaseDestino) {
        super(connBaseOrigem, connBaseDestino);
    }

    @Override
    public boolean export() throws Exception {
        final LinkedHashSet<String> r = new LinkedHashSet<>();
        final String exportIsqlDestino = exportDDLWithIsqlFb(getConnBaseDestino());
        final String exportIsqlOrigem = exportDDLWithIsqlFb(getConnBaseOrigem());
//
//        r.add(exportIsqlOrigem);
//        r.add("+++++++++++++++++++++++++++++++++++++++");
//        r.add("+++++++++++++++++++++++++++++++++++++++");
//        r.add("+++++++++++++++++++++++++++++++++++++++");
//        

        //drop triggers
        for (String tg : getTriggersNomes(getConnBaseDestino())) {
            putDDLDropTrigger(tg, r);
        }

        //drop constrains
        for (String[] c : getConstraintsNomes(getConnBaseDestino(), "RCN.RDB$CONSTRAINT_TYPE = 'FOREIGN KEY'")) {
            putDDLDropConstraint(c[0], c[1], r);
        }

        //drop indices
        for (String in : getIndicesNomes(getConnBaseDestino(), "RDB$INDEX_NAME not like 'PK_AG_%' AND RDB$INDEX_NAME not like 'INTEG''_%' AND RDB$INDEX_NAME not like 'UNI_AG_%' and RDB$INDEX_NAME not like 'RDB$%' and RDB$FOREIGN_KEY is null")) {
            /**
             * indices de pk's e fk's serão removidos ao deletarmos as
             * constrains
             */
            putDDLDropIndex(in, r);
        }

        //alter procedures to ""
        {
            final LinkedHashSet<String> tmp = new LinkedHashSet<>();

            putDDLCreateProceduresPart1(tmp, exportIsqlDestino);

            tmp.stream().filter((pr) -> (pr != null)).forEachOrdered((pr) -> {
                r.add(pr.replace("CREATE PROCEDURE ", "ALTER PROCEDURE "));
            });

            tmp.clear();
        }

        //drop views + procedures
        {
            final LinkedHashSet<String> viewsNomes = getViewsNomes(getConnBaseDestino());

            //Limpa as views
            for (String in : viewsNomes) {
                putDDLEmptyView(in, r, getConnBaseDestino());
            }

            //Limpa as procedures
            {
                final LinkedHashSet<String> tmp = new LinkedHashSet<>();
                putDDLCreateProceduresPart1(tmp, exportIsqlDestino);

                for (String in : tmp) {
                    if (in != null) {
                        r.add(in.replaceAll("(?i)CREATE PROCEDURE", "ALTER PROCEDURE"));
                    }

                }
                tmp.clear();
            }

            //Limpa colunas computedBy
            {
                for (String tab : getTabelasNomes(getConnBaseDestino())) {
                    for (ModelColunaTabela col : getColunas(tab, getConnBaseDestino())) {
                        if (col.isComputedBy()) {
                            putDDLEmptyComputedBy(tab, col.getNome(), r);
                        }
                    }
                }
            }

            //drop procedures
            for (String in : getProceduresNomes(getConnBaseDestino())) {
                putDDLDropProcedure(in, r);
            }

            //Drop views
            for (String in : viewsNomes) {
                putDDLDropView(in, r);
            }

        }

        //drop tables extras
        {
            final ArrayList<String> tabsOri = getTabelasNomes(getConnBaseOrigem());

            for (String tabDest : getTabelasNomes(getConnBaseDestino())) {
                if (!tabsOri.contains(tabDest)) {
                    putDDLDropTable(tabDest, r);
                }
            }

            tabsOri.clear();
        }

        //create domains novas
        {
            final LinkedHashSet<String> dmsDest = getDomainsNomes(getConnBaseDestino());
            final LinkedHashSet<String> tmp = new LinkedHashSet<>();

            putDDLCreateDomains(tmp, exportIsqlOrigem);

            for (String dmOri : getDomainsNomes(getConnBaseOrigem())) {
                if (!dmsDest.contains(dmOri)) {
                    tmp.stream().filter((dm) -> (dm != null && dm.trim().startsWith("CREATE DOMAIN " + dmOri + " "))).forEachOrdered((dm) -> {
                        r.add(dm.trim());
                    });
                }
            }

            dmsDest.clear();
            tmp.clear();
        }

        //create exceptions novas
        {
            final LinkedHashSet<String> excpsDest = getExceptionsNomes(getConnBaseDestino());
            final LinkedHashSet<String> tmp = new LinkedHashSet<>();

            putDDLCreateExceptions(tmp, exportIsqlOrigem);

            for (String excpOri : getExceptionsNomes(getConnBaseOrigem())) {
                if (!excpsDest.contains(excpOri)) {

                    tmp.stream().filter((ex) -> (ex != null
                            && (ex.trim().startsWith("CREATE EXCEPTION " + excpOri.trim() + " ")
                            || ex.trim().startsWith("CREATE EXCEPTION \"" + excpOri.trim() + "\" ")))).forEachOrdered((ex) -> {
                        r.add(ex);
                    });
                }
            }

            excpsDest.clear();
            tmp.clear();
        }

        //create generators novos
        {
            /**
             * Os que já existirem na base não são tocados
             */

            final LinkedHashSet<String> gensDest = getGeneratorsNomes(getConnBaseDestino());
            final LinkedHashSet<String> tmp = new LinkedHashSet<>();

            putDDLCreateGenerators(tmp, exportIsqlOrigem);

            for (String genOri : getGeneratorsNomes(getConnBaseOrigem())) {
                if (!gensDest.contains(genOri)) {

                    tmp.stream()
                            .filter((ge) -> (ge != null && ge.trim().startsWith("CREATE GENERATOR " + genOri.trim())))
                            .forEachOrdered((ge) -> {
                                r.add(ge);
                            });
                }
            }

            gensDest.clear();
            tmp.clear();
        }

        //create tables novas
        {
            final ArrayList<String> tabsDest = getTabelasNomes(getConnBaseDestino());
            final LinkedHashSet<String> tmp = new LinkedHashSet<>();

            putDDLCreateTables(tmp, exportIsqlOrigem);

            for (String tabOri : getTabelasNomes(getConnBaseOrigem())) {
                if (!tabsDest.contains(tabOri)) {
                    tmp.stream()
                            .filter((ge) -> (ge != null && ge.trim().startsWith("CREATE TABLE " + tabOri.trim() + " ")))
                            .forEachOrdered((ge) -> {
                                r.add(ge);
                            });
                }
            }

            tabsDest.clear();
            tmp.clear();
        }

        //alter tables existentes
        {
            final ArrayList<String> tabsOrig = getTabelasNomes(getConnBaseOrigem());

            for (String tabOri : getTabelasNomes(getConnBaseDestino())) {
                if (tabsOrig.contains(tabOri)) {
                    putDDLDiferencaTable(tabOri, getConnBaseOrigem(), getConnBaseDestino(), exportIsqlOrigem, exportIsqlDestino, r);
                }
            }

            tabsOrig.clear();
        }

        //create constrains
        putDDLCreateFks(r, exportIsqlOrigem);

        //create indices
        putDDLCreateIndices(r, exportIsqlOrigem);

        //create procedures ""
        putDDLCreateProceduresPart1(r, exportIsqlOrigem);

        //alter computed by
        putDDLCreateComputedsBy(r, exportIsqlOrigem);

        //create views
        putDDLCreateViews(r, exportIsqlOrigem);

        //alter procedures to "asdasdas"
        putDDLCreateProceduresPart2(r, exportIsqlOrigem);

        //create triggers
        putDDLCreateTriggers(r, exportIsqlOrigem);

        r.forEach((d) -> {
            putToSave(d);
        });

        return true;
    }

}
