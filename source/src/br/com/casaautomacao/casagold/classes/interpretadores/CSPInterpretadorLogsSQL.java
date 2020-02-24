/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.interpretadores;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import org.json.JSONArray;

/**
 * Classe responsavel de interpretar e executar logs SQLs do nosso sistema
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 29/04/2016 - 14:28:58
 */
public class CSPInterpretadorLogsSQL {

    /**
     * Executa um grupo(lote) de sqls. Quando ocorrer um exception o método irá
     * apagar o arquivo orignal e escrever nele as linhas restantes a serem
     * executadas
     *
     * @param sqls CSPArquivos - Arquivo onde está armazenado os SQLs
     * @param con CSPInstrucoesSQLBase - Conexão com a base de destino
     * @param filtroBase String - Nome da base para ser utilizado nos filtros
     * @return ArrayList<String> - Linhas executadas
     * @throws Exception
     */
    public static ArrayList<String> runSqGroup(CSPArquivos sqls, CSPInstrucoesSQLBase con, String filtroBase) throws Exception {
        ArrayList<String> split = new ArrayList<>(Arrays.asList(sqls.getContent().split(LINE_SEPARATOR)));
        ArrayList<String> toError = (ArrayList<String>) split.clone();

        try {
            ArrayList<String> executadas = new ArrayList<>();
            for (String sql : split) {
                String sqlOri = sql;
                if (sql == null || sql.trim().isEmpty()) {
                    if (sql != null) {
                        toError.remove(sqlOri);//Vamos removendo as linhas que já foram executadas com sucesso
                    }
                    continue;
                }
                sql = sql.trim();
                //String def = sql.trim();
                if (sql.contains(";--//")) {
                    String[] spl = sql.split(";--//");
                    sql = spl[0];
                    //Somente os comandos para a base DADOS_CONTRATANTE no cliente
                    if (!spl[1].toLowerCase().contains(filtroBase)) {
                        toError.remove(sqlOri);//Vamos removendo as linhas que já foram executadas com sucesso
                        continue;
                    }
                } else {
                    //Somente os comandos para a base DADOS_CONTRATANTE no cliente
                    if (!sql.toLowerCase().contains(filtroBase)) {
                        toError.remove(sqlOri);//Vamos removendo as linhas que já foram executadas com sucesso
                        continue;
                    }
                }
                Object[] valores = new Object[0];
                if ((sql.contains("?")
                        && (sql.contains("\\. Valores: \\[")//Padrão Velho
                        || sql.contains(";--v:"))//Padrão Novo
                        ) && !sql.contains("\\[\\]")) {
                    String[] spl2 = sql.split(sql.contains(";--v:") ? ";--v:\\[" : "\\. Valores: \\[");
                    sql = spl2[0];
                    String data = spl2[1].split("];--//")[0];
                    if (CSPUtilidadesLangJson.isJson("[" + data + "]")) {
                        JSONArray arr = new JSONArray("[" + data + "]");

                        valores = new Object[arr.length()];

                        for (int i = 0; i < valores.length; i++) {

                            if (arr.get(i) == org.json.JSONObject.NULL) {
                                valores[i] = null;
                            } else {
                                valores[i] = arr.get(i);
                            }
                        }

                    } else {
                        valores = data.split(", ");
                    }
                }
                sql = sql.replace(". Valores: []", "");
                sql = sql.replace(";--v:", "");
                con.execute(sql, valores);
                toError.remove(sqlOri);//Vamos removendo as linhas que já foram executadas com sucesso
                executadas.add(sqlOri);
            }
            sqls.delete();//Se chegou até aqui é possível então excluir o arquivo :)
            return executadas;
        } catch (SQLException | IOException ex) {
            CSPException.register(ex, false);
            StringBuilder toBackup = new StringBuilder();
            for (int i = 0; i < toError.size(); i++) {
                String sql = toError.get(i);
                toBackup.append(sql);
                if (i == 0 && !sql.contains("<-- LINHA PROBLEMÁTICA!")) {
                    toBackup.append("<-- LINHA PROBLEMÁTICA!");
                }
                toBackup.append(LINE_SEPARATOR);
            }
            sqls.setContent(toBackup.toString());

        }
        return null;
    }
}
