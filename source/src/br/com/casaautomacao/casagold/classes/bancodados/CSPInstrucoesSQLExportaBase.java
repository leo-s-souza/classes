/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.bancodados;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import java.util.LinkedHashSet;

/**
 * Base das classes exportação de sql
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/03/2017 - 14:20:57
 */
public abstract class CSPInstrucoesSQLExportaBase {

    private final LinkedHashSet<String> toSave = new LinkedHashSet<>();

    /**
     * Exporta os sqls necessários
     *
     * @return
     * @throws Exception
     */
    public abstract boolean export() throws Exception;

    /**
     * Salva o sql gerado em um arquivo sql gerado automaticamente
     *
     * @return
     * @throws java.lang.Exception
     */
    public CSPArquivos saveToFile() throws Exception {
        final CSPArquivos r = new CSPArquivos(CSPUtilidadesSO.PATH_TEMP + "/ddl_gold_" + CSPUtilidadesLangDateTime.getTempoCompletoLimpo() + ".sql");
        saveToFile(r);
        return r;
    }

    /**
     * Salva o sql gerado em no arquivo sql
     *
     * @param to CSPArquivos
     *
     * @throws java.lang.Exception
     */
    public void saveToFile(CSPArquivos to) throws Exception {

        to.setContent("");

        int restando = this.toSave.size();

        for (String s : this.toSave) {
            to.appendContent(s + (restando > 1 ? "\n" : ""));
            restando--;
        }

    }

    /**
     * Adiciona o comando sql para ser salvo
     *
     * @param sql String
     */
    protected void putToSave(String sql) {
        if (sql != null && !sql.trim().isEmpty()) {

            sql = sql.trim();

            if (!sql.endsWith(";")) {
                sql = sql + ";";
            }

            this.toSave.add(sql);
        }
    }

}
