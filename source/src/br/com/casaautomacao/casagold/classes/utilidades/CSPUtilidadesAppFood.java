/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe de utilidades relacionadas ao APP FOOD (e-GULA)
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 14/09/2016 - 08:55:02
 */
public abstract class CSPUtilidadesAppFood extends CSPUtilidadesApplication {

    /**
     * Para uso de controle no método {@link CSPUtilidadesAppFood#atualizaLojaSaldosPedido(CSPInstrucoesSQLBase, String, String)
     * }
     */
    private static boolean forceDataPrescricaoToNull;

    /**
     * Converte o APP_CODIGO para o PRODUTO_CODIGO
     *
     * @param conn CSPInstrucoesSQLBase - Conexão válida com o
     * dados_contratantes
     * @param id String - Código a ser convertido
     * @return
     * @throws java.sql.SQLException
     */
    public static String idAppToCodProd(CSPInstrucoesSQLBase conn, String id) throws SQLException {
        if (id.trim().endsWith("-a")) {
            ResultSet selc = conn.select("SELECT r.PRODUTO_CODIGO FROM PRODUTOS r WHERE r.APP_CODIGO||'-a' = ?", id);
            if (selc.next()) {
                return selc.getString("PRODUTO_CODIGO");
            }
            return null;
        } else {
            return id;
        }
    }

    /**
     * Realiza a conversão de APP_CODIGO para o PRODUTO_CODIGO de uma lista de
     * ids.
     *
     * @param conn CSPInstrucoesSQLBase - Conexão válida com o
     * dados_contratantes
     * @param src String - Códigos a serem convertidos
     * @return
     * @throws java.sql.SQLException
     */
    public static String trataListIds(CSPInstrucoesSQLBase conn, String src) throws SQLException {
        String ret = "" + src;
        /**
         * Explodimos por tudo: [ ] , . ; ( )
         */
        src = src.replaceAll("\\[|\\]|\\,|\\.|\\;|\\(|\\)|\\-|\\+", " ").replace(" a", "-a");
        for (String s : src.split(" ")) {
            if (!s.replaceAll("[^0-9]", "").trim().isEmpty() && s.trim().endsWith("-a")) {
                String t = idAppToCodProd(conn, s);
                if (t != null) {
                    ret = ret.replace(s, t);
                }
            }
        }
        return ret;
    }

    /**
     * Retorna as descrições dos opcionais já totalmente tratadas
     *
     * @param conn CSPInstrucoesSQLBase - Conexão válida com o
     * dados_contratantes
     * @param opcsCods String - Códigos dos opcionais
     * @param codGr
     * @param codProd
     * @return
     * @throws java.lang.Exception
     */
    public static String[] getDescricoesOpcionaisFromItemPedido(CSPInstrucoesSQLBase conn, String opcsCods, String codGr, String codProd) throws Exception {

        if (opcsCods != null) {
            final String[] spl = opcsCods.split(";");
            final String[] r = new String[spl.length];
            ResultSet sle;
            for (int i = 0; i < spl.length; i++) {
                final String o = spl[i];
                r[i] = "";
                if (o.endsWith("[]")) {
                    continue;
                }
                final String codOpc = o.split("\\[")[0];

                sle = conn.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     o.DESCRICAO ");
                    sb.append("FROM ");
                    sb.append("     OPCIONAIS o ");
                    sb.append("WHERE ");
                    sb.append("     o.OPCIONAIS_CODIGO = ? ");
                }, codOpc);

                if (sle.next()) {
                    r[i] = sle.getString("DESCRICAO") + ";";
                }

//   final String codsOpcItens = "," + o.split("\\[")[1].split("\\]")[0] + ",";
                String codsOpcItens = o.contains("[") ? "," + o.split("\\[")[1].replace("]", "") + "," : ",,";

                if (codsOpcItens.contains("-")) {
                    r[i] = "Sem " + r[i];
                } else {
                    sle = conn.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("	LIST(oi.DESCRICAO,',') AS D ");
                        sb.append("FROM ");
                        sb.append("	OPCIONAIS o, ");
                        sb.append("	OPCIONAIS_ITEM oi, ");
                        sb.append("	PRODUTOS_OPCIONAIS po ");
                        sb.append("WHERE '");
                        //Manter os appends para evitar problema com o prepare....
                        sb.append(codsOpcItens);
                        sb.append("'	CONTAINING ',' || po.ITEM_CODIGO || ','  AND ");
                        sb.append("	oi.OPCIONAIS_CODIGO = po.OPCIONAIS_CODIGO AND ");
                        sb.append("	oi.ITEM_CODIGO = po.ITEM_CODIGO AND ");
                        sb.append("	o.OPCIONAIS_CODIGO = po.OPCIONAIS_CODIGO AND ");
                        sb.append("	COALESCE(( ");
                        sb.append("		select count(1) FROM ");
                        sb.append("			PRODUTOS_GRUPOS_TAMANHOS pgt, ");
                        sb.append("			PRODUTOS p ");
                        sb.append("		WHERE ");
                        sb.append("			pgt.GRUPOS_CODIGO = p.GRUPOS_CODIGO AND ");
                        sb.append("			p.PRODUTO_CODIGO = po.PRODUTO_CODIGO ");
                        sb.append("	),0) = 0 ");
                        sb.append("	AND po.PRODUTO_CODIGO = ? ");
                    }, codProd);

                    if (sle.next()) {
                        if (sle.getString("D") != null) {
                            r[i] += CSPUtilidadesLang.removeDuplicates(sle.getString("D"), ",");

                            continue;
                        }
                    }

                    sle = conn.select((StringBuilder sb) -> {
                        sb.append("SELECT ");
                        sb.append("     LIST(opci.DESCRICAO,',') AS D ");
                        sb.append("FROM ");
                        sb.append("     OPCIONAIS_ITEM opci, ");
                        sb.append("     PRODUTOS_GRUPOS_OPCIONAIS pgo ");
                        sb.append("WHERE '");
                        //Manter os appends para evitar problema com o prepare....
                        sb.append(codsOpcItens);
                        sb.append("' CONTAINING ',' || opci.ITEM_CODIGO || ',' ");
                        sb.append("     AND pgo.OPCIONAIS_CODIGO = opci.OPCIONAIS_CODIGO ");
                        sb.append("     AND pgo.GRUPOS_CODIGO = ? ");
                        sb.append("     AND pgo.OPCIONAIS_CODIGO = ? ");
                    }, codGr, codOpc);

                    if (sle.next()) {
                        r[i] += CSPUtilidadesLang.removeDuplicates(sle.getString("D"), ",");
                    }
                }
            }

            for (int i = 0; i < r.length; i++) {
                r[i] = r[i].replace(";", "=").replace("null", "");

            }

            return r;
        }
        return new String[0];
    }

    /**
     *
     * @param conn
     * @param sbsCods
     * @param codGr
     * @param codProd
     * @return
     * @throws Exception
     */
    public static String[] getDescricoesSaboresFromItemPedido(CSPInstrucoesSQLBase conn, String sbsCods, String codGr, String codProd) throws Exception {

        if (sbsCods != null) {

            final ArrayList<String> arraySbs = new ArrayList<>();
            final HashMap<String, String> sabor = new HashMap<>();

            ResultSet sle = conn.select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("     sb.DESCRICAO AS D, sb.SABORES_CODIGO AS SCODIGO ");
                sb.append("FROM ");
                sb.append("     SABORES sb ");
                sb.append("WHERE ");
                sb.append("     '").append(sbsCods).append("' CONTAINING sb.SABORES_CODIGO || '['");
            });

            String[] grousSbs = sbsCods.split(";");
            while (sle.next()) {
                sabor.put(sle.getString("SCODIGO"), sle.getString("D") + "=");
            }

            /**
             * Geralmente quando temos sabores os ingredientes adicionais e
             * removidos estarão diretamente ligados aos sabores
             */
            for (String s : grousSbs) {
                String array = "";

                sle = conn.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     LIST(prod.DESCRICAO,'+;') AS D ");
                    sb.append("FROM ");
                    sb.append("     PRODUTOS_GRUPOS_SABORES_INGREDI pgsi,");
                    sb.append("     PRODUTOS prod ");
                    sb.append("WHERE ");
                    sb.append("     ('");
                    sb.append(s);
                    sb.append("' CONTAINING '+'|| pgsi.PRODUTO_CODIGO || ',' OR ");
                    sb.append("     '");
                    sb.append(s);
                    sb.append("' CONTAINING '+'|| pgsi.PRODUTO_CODIGO || ']') AND ");
                    sb.append("     prod.PRODUTO_CODIGO = pgsi.PRODUTO_CODIGO");
                });

                if (sle.next()) {
                    array = (sabor.get(s.substring(0, 3)));
                    if (sle.getString("D") != null) {
                        String sab = sle.getString("D");
                        if (sab.length() > 0 && !sab.endsWith(";")) {
                            sab += "+;";
                        }
                        array += sab;
                    }
                }

                sle = conn.select((StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("     LIST(prod.DESCRICAO,';') AS D ");
                    sb.append("FROM ");
                    sb.append("     PRODUTOS_GRUPOS_SABORES_INGREDI pgsi,");
                    sb.append("     PRODUTOS prod ");
                    sb.append("WHERE ");
                    sb.append("     ('");
                    sb.append(s);
                    sb.append("' CONTAINING '-' || pgsi.PRODUTO_CODIGO || ',' OR ");
                    sb.append("     '");
                    sb.append(s);
                    sb.append("' CONTAINING '-'|| pgsi.PRODUTO_CODIGO || ']') AND ");
                    sb.append("     prod.PRODUTO_CODIGO = pgsi.PRODUTO_CODIGO");
                });

                if (sle.next()) {
                    if (sle.getString("D") != null) {
                        String sab = sle.getString("D");
                        if (array.isEmpty()) {
                            array = (sabor.get(s.substring(0, 3)) + sab);
                        } else {
                            array += sab;
                        }
                    }
                }
                arraySbs.add(CSPUtilidadesLang.removeDuplicates(array, ";"));
            }

            return arraySbs.toArray(new String[arraySbs.size()]);
        }

        return new String[0];
    }

    /**
     *
     * @param conn
     * @param addsCods
     * @param codGr
     * @param codProd
     * @return
     * @throws Exception
     */
    public static String[] getDescricoesAdicionaisRemovidosFromItemPedido(CSPInstrucoesSQLBase conn, String addsCods, String codGr, String codProd) throws Exception {
        if (addsCods != null) {

            final ArrayList<HashMap<String, Object>> sle = conn.selectInMap((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("     sb.DESCRICAO ");
                sb.append("FROM ");
                sb.append("     PRODUTOS sb ");
                sb.append("WHERE ");
                sb.append("     ';").append(addsCods).append(";' CONTAINING ';'|| sb.PRODUTO_CODIGO || ';' ");
                sb.append("     AND sb.DESCRICAO is not null ");
            });

            final String[] r = new String[sle.size()];

            for (int i = 0; i < sle.size(); i++) {
                r[i] = sle.get(i).get("DESCRICAO").toString();
            }

            return r;
        }
        return new String[0];
    }

    /**
     * Atualiza o <<BONUS_PERCENTUAL>> e <<PEDIDOS_PERCENTUAL>> na base local do
     * contratante.
     *
     * @param get JSONOBject - Dados.
     * @param conn CSPInstrucoesSQL - Conexão com a base.
     * @throws Exception
     */
    public static void atualizaPercentuaisLoja(JSONObject get, CSPInstrucoesSQLBase conn) throws Exception {
        JSONArray arr = get.getJSONArray("PERCENTUAIS");

        if (arr != null && arr.length() > 0) {

            for (int i = 0; i < arr.length(); i++) {

                final JSONObject percentual = arr.getJSONObject(i);
                final HashMap<String, Object> data = new HashMap<>();

                data.put("BONUS_PERCENTUAL", percentual.getDouble("BONUS_PERCENTUAL"));
                data.put("PEDIDOS_PERCENTUAL", percentual.getDouble("PEDIDOS_PERCENTUAL"));

                conn.gravaComposto("EGULA_LOJA_SALDOS", data, null);
            }
        }
    }

    /**
     * Calcula e atualiza os saldos de bonus e afins na base local. Confira as
     * regras!:
     *
     * Regra 1 -----------------------------------------------------------------
     *
     * [DADOS_CONTRATANTE], <<EGULA_LOJA_SALDOS>>, <DATA_LIMITE> menor que data
     * limite da nova inserção, [DADOS_CONTRATANTE], <<EGULA_LOJA_SALDOS>>,
     * <DATA_LIMITE> deve assumir data limite estipulado na nova inserção.
     *
     * Nota: Lembrando que dentre as informações enviadas pelo CAPP referente a
     * liberação, deve constar: DATA DA LIBERAÇÃO + DIAS LIMITES DA LOJA, NO
     * INSTANTE DA INSERÇÃO
     *
     * Regra 2 -----------------------------------------------------------------
     *
     * Sempre que [DADOS_CONTRATANTE],<<EGULA_LOJA_SALDOS>>,<DATA_PRESCRICAO>
     * possuir conteúdo, o sistema deve calcular em pedidos APP confirmados:
     * Diminuir valor confirmado de <PEDIDOS_LIMITE> enquanto
     * <LIMITE_PEDIDOS_PRESCRITO> possui valor acima de zero
     *
     * Nota: A ideia do limite prescrito é que, até a data prescrita a Loja
     * possuirá possibilidade ilimitada de emissão de pedidos, e para casos onde
     * a loja tenha adquirido antecipadamente créditos para datas posteriores o
     * sistema poderá e deverá debitar de <PEDIDOS_LIMITE> apenas o valor
     * remanescente de créditos adquiridos anteriormente.
     *
     * Regra 3 -----------------------------------------------------------------
     *
     * Sempre que [DADOS_CONTRATANTE], <<EGULA_LOJA_SALDOS>>,
     * <LIMITE_PEDIDOS_PRESCRITO> for alterado pra “null”, deve-se alterar para
     * o mesmo resultado (“null” também) <DATA_PRESCRICAO> caso a data do SO for
     * maior que <DATA_PRESCRICAO>
     *
     * Regra 4 -----------------------------------------------------------------
     *
     * Sempre que o crédito adquirido pela loja gerar um valor de
     * <PEDIDOS_LIMITE> menor que <Limite Pedidos> do cadastro da loja em
     * [4EAA], não deve gerar <DATA_LIMITE>, exceto : A Loja possua
     * <DATA_LIMITE> maior que data SO
     *
     * Regra 5 -----------------------------------------------------------------
     *
     * Sempre que <DATA_LIMITE>=”null”, <LIMITE_PEDIDOS_PRESCRITO> e
     * <DATA_PRESCRICAO> devem também estar “null”
     *
     * Regra 6 -----------------------------------------------------------------
     *
     * Sempre que <PEDIDOS_LIMITE> for maior que limite de pedidos cadastrado
     * para a loja (em [4EAA – Lojas]) o sistema deve calcular o numero de dias
     * para <DATA_LIMITE> a partir da parte inteira da divisão de
     * <PEDIDOS_LIMITE> por <LIMITE_PEDIDOS> do contratante.
     *
     * Regra 7 -----------------------------------------------------------------
     *
     * Sempre que [DADOS_CONTRATANTE],<<EGULA_LOJA_SALDOS>>,<DATA_LIMITE>
     * alternar de “null” para uma data válida, <LIMITE_PEDIDOS_PRESCRITO> e
     * <DATA_PRESCRICAO> devem receber valor mediante a seguinte condição :
     *
     * Se parte inteira da divisão de {<<EGULA_LOJA_SALDOS>>,<PEDIDOS_LIMITE>
     * / <<EGULA_LIBERACOES>>,<LIMITE_PEDIDOS> (do ultimo pedido registrado)}
     * for maior que 1
     *
     * <LIMITE_PEDIDOS_PRESCRITO>=<<EGULA_LIBERACOES>>,<PEDIDOS_LIMITE> (do
     * ultimo registro da tabela)}
     *
     * <DATA_PRESCRICAO>=<data SO>+{<<EGULA_LIBERACOES>>,<LIMITE_DIAS> (do
     * ultimo registro da tabela)}
     *
     * · Caso contrario
     *
     * <LIMITE_PEDIDOS_PRESCRITO>=”null”
     * <DATA_PRESCRICAO>=”null”
     *
     * Nota
     *
     * Essa regra ainda vai sofrer alterações para tratar possibilidades de
     * campanhas diferenciadas, onde créditos poderão ser adquiridos pela Loja
     * em condições distintas de configuração de Dias Limite e Taxa egula .
     *
     * Exemplo
     *
     * Uma loja, pode estar usufruindo de um crédito adquirido em configuração
     * de R$ 5.000,00 de taxa limite para 30 dias, e adquirir um crédito
     * promocional de R$ 7.500,00 para 60 dias
     *
     * O sistema deverá tratar a manutenção do primeiro crédito de forma a não
     * causar prejuízos pra loja.
     *
     * O que vai mudar é que o sistema não mais passará a se basear no ultimo
     * registro de <<EGULA-LIBERACOES>>. Será implementado a possibilidade de
     * ser utilizado valores de registros anteriores, dependendo a condição.
     *
     * Regra 8 -----------------------------------------------------------------
     * Regra 9 -----------------------------------------------------------------
     *
     * @param dados JSONObject -
     * @param conn CSPInstrucoesSQL - Conexão com a base.
     * @param dataLiberacao - Data da Liberação.
     * @throws java.lang.Exception
     */
    public static void updateSaldosLoja(JSONObject dados, CSPInstrucoesSQLBase conn, Date dataLiberacao) throws Exception {

        final JSONArray saldos = dados.getJSONArray("UP");

        if (saldos != null && saldos.length() > 0) {
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            for (int i = 0; i < saldos.length(); i++) {

                final JSONObject saldo = saldos.getJSONObject(i);
                final double limitePedidos = saldo.getDouble("LIMITE_PEDIDOS");
                final HashMap<String, Object> data = new HashMap<>();

                data.put("LIBERACAO_NUMERO", saldo.getString("LIBERACAO_NUMERO"));
                data.put("DATA", sdf.parse(saldo.getString("DATA_LIBERACAO")));
                data.put("PEDIDOS_CREDITO", saldo.getDouble("VALOR_PEDIDO"));
                data.put("PEDIDOS_BONUS_CREDITO", saldo.getDouble("VALOR_BONUS"));
                data.put("PERCENTUAL_BONUS", saldo.getDouble("BONUS_PERCENTUAL"));
                data.put("PERCENTUAL_PEDIDO", saldo.getDouble("PEDIDOS_PERCENTUAL"));
                data.put("LIMITE_DIAS", saldo.getInt("LIMITE_DIAS"));
                data.put("LIMITE_PEDIDOS", limitePedidos);

                conn.insertComposto(true, "EGULA_LIBERACOES", data);

                data.clear();

                data.put("PEDIDOS_LIMITE", saldo.getDouble("VALOR_PEDIDO"));
                data.put("BONUS_LIMITE", saldo.getDouble("VALOR_BONUS"));

                Calendar cal = Calendar.getInstance();

                if (dataLiberacao != null) {
                    cal.setTime(dataLiberacao);
                }

                boolean next = false;

                final ResultSet sl = conn.select("SELECT * FROM EGULA_LOJA_SALDOS");
                if (sl.next()) {
                    next = true;
                    data.put("PEDIDOS_LIMITE", Double.valueOf(data.get("PEDIDOS_LIMITE").toString()) + sl.getDouble("PEDIDOS_LIMITE"));
                    data.put("BONUS_LIMITE", Double.valueOf(data.get("BONUS_LIMITE").toString()) + sl.getDouble("BONUS_LIMITE"));
                    /**
                     * A antiga data limite se torna a prescrição
                     */
                    if (sl.getString("DATA_LIMITE") != null) {

                        if (sdf.parse(sl.getString("DATA_LIMITE")).getTime() < sdf.parse(saldo.getString("DATA_LIBERACAO")).getTime()) {
                            cal.setTime(sdf.parse(saldo.getString("DATA_LIBERACAO")));
                        } else {
                            long dataHoje;
                            if (dataLiberacao != null) {
                                dataHoje = sdf.parse(sdf.format(dataLiberacao)).getTime();
                            } else {
                                dataHoje = sdf.parse(sdf.format(new Date())).getTime();
                            }

                            if (sdf.parse(sl.getString("DATA_LIMITE")).getTime() >= dataHoje) {
                                data.put("DATA_PRESCRICAO", sl.getDate("DATA_LIMITE"));
                                data.put("LIMITE_PEDIDOS_PRESCRITO", sl.getDouble("PEDIDOS_LIMITE"));
                                cal.setTime(sl.getDate("DATA_LIMITE"));

                            }
                        }
                    }

                } else {
                    cal.setTime(sdf.parse(saldo.getString("DATA_LIBERACAO")));
                }

                //Regra 6
                if (next) {
                    if (((double) data.get("PEDIDOS_LIMITE")) > limitePedidos
                            && (sl.getString("DATA_LIMITE") == null
                            || (sdf.parse(sl.getString("DATA_LIMITE")).getTime() < sdf.parse(saldo.getString("DATA_LIBERACAO")).getTime()))) {

                        cal.add(Calendar.DATE, (int) ((double) data.get("PEDIDOS_LIMITE") / limitePedidos));
                    } else {
                        cal.add(Calendar.DATE, (int) (saldo.getInt("LIMITE_DIAS") * (saldo.getDouble("VALOR_PEDIDO") / saldo.getInt("LIMITE_PEDIDOS"))));
                    }

                    data.put("DATA_LIMITE", cal.getTime());

                }

                //Regra 4
                if (next) {
                    if (((double) data.get("PEDIDOS_LIMITE")) < limitePedidos) {
                        if (sl.getString("DATA_LIMITE") != null) {
                            long dataHoje;
                            if (dataLiberacao != null) {
                                dataHoje = sdf.parse(sdf.format(dataLiberacao)).getTime();
                            } else {
                                dataHoje = sdf.parse(sdf.format(new Date())).getTime();
                            }
                            if (sdf.parse(sl.getString("DATA_LIMITE")).getTime() <= dataHoje) {
                                data.put("DATA_LIMITE", null);
                            }
                        } else {
                            data.put("DATA_LIMITE", null);
                        }
                    }
                }

                //Regra 5
                if (data.get("DATA_LIMITE") == null) {
                    data.put("DATA_LIMITE", null);
                    data.put("LIMITE_PEDIDOS_PRESCRITO", null);
                    data.put("DATA_PRESCRICAO", null);
                }

                //Regra 7
                if (next) {
                    if (sl.getString("DATA_LIMITE") == null && data.get("DATA_LIMITE") != null) {
                        data.put("LIMITE_PEDIDOS_PRESCRITO", null);
                        data.put("DATA_PRESCRICAO", null);

                        final int days = (int) ((double) data.get("PEDIDOS_LIMITE") / limitePedidos);

                        if (days > 1) {

                            final ResultSet rs = conn.selectOneRow((StringBuilder sb) -> {
                                sb.append("SELECT ");
                                sb.append("    first 1 skip 1 ");
                                sb.append("    r.LIMITE_DIAS, ");
                                sb.append("    r.LIMITE_PEDIDOS ");
                                sb.append("FROM ");
                                sb.append("    EGULA_LIBERACOES r ");
                            });

                            if (rs != null) {

                                cal = Calendar.getInstance();

                                if (dataLiberacao != null) {
                                    cal.setTime(dataLiberacao);
                                }

                                cal.add(Calendar.DATE, rs.getInt("LIMITE_DIAS"));
                                
                               // data.put("LIMITE_PEDIDOS_PRESCRITO", rs.getDouble("LIMITE_PEDIDOS"));
                                data.put("DATA_PRESCRICAO", cal.getTime());
                            }
                        }
                    }
                };
                data.put("BONUS_PERCENTUAL", saldo.getDouble("BONUS_PERCENTUAL"));
                data.put("PEDIDOS_PERCENTUAL", saldo.getDouble("PEDIDOS_PERCENTUAL"));

                conn.gravaComposto("EGULA_LOJA_SALDOS", data, null);
            }

        }
    }

    /**
     * Alias para {@link CSPUtilidadesAppFood#updateSaldosLoja(JSONObject, CSPInstrucoesSQLBase)
     * }
     *
     * @param dados JSONObject -
     * @param conn CSPInstrucoesSQL - Conexão com a base.
     * @throws Exception
     */
    public static void updateSaldosLoja(JSONObject dados, CSPInstrucoesSQLBase conn) throws Exception {
        CSPUtilidadesAppFood.updateSaldosLoja(dados, conn, null);
    }

    /**
     * Confirma o pedido Egula e atualiza os saldos da loja.
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com a base.
     * @param data String - Data da entrega.
     * @param dataPedido String - Data do pedido.
     * @param hora String - Hora do pedido.
     * @param numPedido String - Número do pedido.
     * @param totalPedido String - Valor total do pedido.
     * @param pagamento String - Forma de pagamento.
     * @return
     * @throws Exception
     */
    public static boolean confirmaPedido(CSPInstrucoesSQLBase conn, String data, String dataPedido, String hora, String numPedido, String totalPedido, String pagamento) throws Exception {
        HashMap<String, Object> dados = new HashMap<>();
        dados.put("DATA_ENTREGA", new SimpleDateFormat("dd.MM.yyyy").parse(data));
        dados.put("HORA_ENTREGA", new SimpleDateFormat("HH:mm").parse(hora));
        dados.put("STATUS", "1");
        conn.insertComposto(false, "PEDIDOS", dados, "PEDIDO_NUMERO = ?", numPedido);

        return CSPUtilidadesAppFood.atualizaLojaSaldosPedido(conn, dataPedido, totalPedido, pagamento);
    }

    /**
     * Atualiza as informações de saldos da loja.
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com a base.
     * @param dataPedido String - Data do pedido.
     * @param totalPedido String - Valor total do pedido.
     * @throws Exception
     */
    private static boolean atualizaLojaSaldosPedido(CSPInstrucoesSQLBase conn, String dataPedido, String totalPedido, String pagamento) throws Exception {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final long timePedido = sdf.parse(dataPedido).getTime();

        ResultSet slLastLiberacao = conn.selectOneRow((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("	FIRST 1 ");
            sb.append("	r.LIMITE_PEDIDOS ");
            sb.append("FROM ");
            sb.append("	EGULA_LIBERACOES r ");
            sb.append("ORDER BY ");
            sb.append("	r.LIBERACAO_NUMERO DESC ");

        });
        final double lastLiberacaoLimite = (slLastLiberacao != null) ? slLastLiberacao.getDouble(1) : 0;

        conn.gravaCompostoSuper(
                "EGULA_LOJA_SALDOS", new LinkedHashMap<String, CSPInstrucoesSQLBase.GravaCompostoColumn>() {
            {
                forceDataPrescricaoToNull = false;
                put("PEDIDOS_LIMITE", (boolean isInsert, ResultSet oldData) -> {

                    double oldVal = 0;
                    double totalPed = Double.valueOf(totalPedido);
                    double newVal = oldVal - totalPed;

                    if (!isInsert) {
                        oldVal = oldData.getDouble("PEDIDOS_LIMITE");
                        newVal = oldVal - totalPed;

                        if (oldVal > 0) {//Só para evitar cagadas

                            if (oldData.getString("DATA_PRESCRICAO") == null || timePedido > sdf.parse(oldData.getString("DATA_PRESCRICAO")).getTime()) {

                                if (oldData.getString("DATA_LIMITE") != null) {
                                    if (newVal < 0 && sdf.parse(oldData.getString("DATA_LIMITE")).getTime() >= timePedido) {
                                        return 0;
                                    }
                                }

                            } else if (oldData.getString("DATA_PRESCRICAO") != null) {
                                String limitePrescrito = oldData.getString("LIMITE_PEDIDOS_PRESCRITO");

                                if (limitePrescrito != null && Double.parseDouble(limitePrescrito) >= 0) {
                                    //Regra 02 (Procure-as no método updateSaldosLoja())
                                    if (totalPed > Double.parseDouble(limitePrescrito)) {
                                        return oldVal - Double.parseDouble(limitePrescrito);
                                    }

                                }
                            }

                        }
                    }
                    System.out.println("FORA!!");
                    return newVal;

                });

                put("LIMITE_PEDIDOS_PRESCRITO", (boolean isInsert, ResultSet oldData) -> {

                    if (!isInsert && oldData.getString("LIMITE_PEDIDOS_PRESCRITO") != null) {

                        final double oldVal = oldData.getDouble("LIMITE_PEDIDOS_PRESCRITO");
                        final double newVal = oldVal - Double.valueOf(totalPedido);

                        if (oldVal > 0) {//Só para evitar cagadas

                            if (oldData.getString("DATA_PRESCRICAO") != null && timePedido <= sdf.parse(oldData.getString("DATA_PRESCRICAO")).getTime()) {

                                if (newVal < 0) {
                                    forceDataPrescricaoToNull = true;
                                    return null;
                                }

                                return newVal;
                            }

                        } else if (oldVal == 0) {
                            forceDataPrescricaoToNull = true;
                            return null;
                        }

                        return oldVal;
                    }

                    forceDataPrescricaoToNull = true;
                    return null;
                });

                put("BONUS_LIMITE", (boolean isInsert, ResultSet oldData) -> {
                    if (!isInsert) {
                        return oldData.getDouble("BONUS_LIMITE") - Double.valueOf(totalPedido);
                    }
                    return 0 - Double.valueOf(totalPedido);
                });

                put("BONUS_CREDITO", (boolean isInsert, ResultSet oldData) -> {

                    if (pagamento != null && pagamento.equals("eb")) {

                        if (!isInsert) {
                            return oldData.getDouble("BONUS_CREDITO") + Double.valueOf(totalPedido);
                        }

                        return Double.valueOf(totalPedido);
                    }

                    return 0;
                });

                put("DATA_PRESCRICAO", (boolean isInsert, ResultSet oldData) -> {
                    if (isInsert) {
                        return null;
                    }

                    final Date dtPres = oldData.getString("DATA_PRESCRICAO") != null ? oldData.getDate("DATA_PRESCRICAO") : null;
                    final double pdLimit = oldData.getDouble("PEDIDOS_LIMITE");

                    /**
                     * Regra 03 (Procure-as no updateSaldosLoja())
                     */
                    if (forceDataPrescricaoToNull) {
                        if (dtPres != null) {
                            if (sdf.parse(sdf.format(CSPUtilidadesLangDateTime.getDataObj())).getTime() > sdf.parse(sdf.format(dtPres)).getTime()) {

                                return null;
                            }
                        }
                    }

                    if (dtPres == null || timePedido > sdf.parse(sdf.format(dtPres)).getTime()) {
                        return null;
                    }

                    if (lastLiberacaoLimite > pdLimit) {
                        return null;
                    }

                    return dtPres;
                });

                put("DATA_LIMITE", (boolean isInsert, ResultSet oldData) -> {

                    if (!isInsert) {
                        final Date dtLi = oldData.getString("DATA_LIMITE") != null ? oldData.getDate("DATA_LIMITE") : null;

                        if (dtLi != null) {
                            final long hj = sdf.parse(sdf.format(CSPUtilidadesLangDateTime.getDataObj())).getTime();

                            if (hj > dtLi.getTime()) {
                                if (oldData.getString("DATA_PRESCRICAO") == null) {
                                    return null;
                                }
                            }
                        }

                        return dtLi;
                    }

                    return null;
                });

            }
        },
                null);

        return true;
    }

    /**
     * Faz o Recalculo Financeiro da loja. Passa registro a registro
     * recalculando os valores disponíveis à loja.
     *
     * @param conn CSPInstrucoesSQLBase - Conexão com a base.
     * @throws Exception
     */
    public static void recalculoFinanceiroLoja(CSPInstrucoesSQLBase conn) throws Exception {
        ArrayList<Date> datas = new ArrayList<>();
        ResultSet rs;

        rs = conn.select("SELECT DATA FROM EGULA_LIBERACOES WHERE DATA IS NOT NULL");
        while (rs.next()) {
            if (!datas.contains(rs.getDate("DATA"))) {
                datas.add(rs.getDate("DATA"));
            }
        }

        rs = conn.select("SELECT DATA_RESTAURANTE FROM PEDIDOS WHERE STATUS = 1 AND DATA_RESTAURANTE IS NOT NULL");
        while (rs.next()) {
            if (!datas.contains(rs.getDate("DATA_RESTAURANTE"))) {
                datas.add(rs.getDate("DATA_RESTAURANTE"));
            }
        }

        //Ordena as datas
        Collections.sort(datas);

        //Deleta a linha única contida na EGULA_LOJA_SALDOS para começar o recalculo do 0.
        conn.deleteComposto("EGULA_LOJA_SALDOS", "1 = 1");

        for (Date data : datas) {
            ArrayList<HashMap<String, Object>> infos = conn.selectInMap((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("     '0' AS IS_PEDIDO, ");
                sb.append("     el.LIBERACAO_NUMERO AS LIBERACAO_NUMERO, ");
                sb.append("     el.LIMITE_PEDIDOS AS LIMITE_PEDIDOS, ");
                sb.append("     el.PEDIDOS_CREDITO AS PEDIDOS_CREDITO, ");
                sb.append("     el.PEDIDOS_BONUS_CREDITO AS PEDIDOS_BONUS_CREDITO, ");
                sb.append("     el.PERCENTUAL_BONUS AS PERCENTUAL_BONUS, ");
                sb.append("     el.PERCENTUAL_PEDIDO AS PERCENTUAL_PEDIDO, ");
                sb.append("     el.LIMITE_DIAS AS LIMITE_DIAS, ");
                sb.append("     el.LIMITE_PEDIDOS AS LIMITE_PEDIDOS, ");
                sb.append("     NULL AS PEDIDO_NUMERO, ");
                sb.append("     NULL AS PEDIDO_TOTAL, ");
                sb.append("     NULL AS PAGAMENTO ");
                sb.append("FROM ");
                sb.append("     EGULA_LIBERACOES el ");
                sb.append("WHERE ");
                sb.append("     el.DATA = ? ");
                sb.append("UNION ");
                sb.append("     SELECT ");
                sb.append("          '1' AS IS_PEDIDO, ");
                sb.append("          NULL AS LIBERACAO_NUMERO, ");
                sb.append("          NULL AS LIMITE_PEDIDOS, ");
                sb.append("          NULL AS PEDIDOS_CREDITO, ");
                sb.append("          NULL AS PEDIDOS_BONUS_CREDITO, ");
                sb.append("          NULL AS PERCENTUAL_BONUS, ");
                sb.append("          NULL AS PERCENTUAL_PEDIDO, ");
                sb.append("          NULL AS LIMITE_DIAS, ");
                sb.append("          NULL AS LIMITE_PEDIDOS, ");
                sb.append("          p.PEDIDO_NUMERO AS PEDIDO_NUMERO, ");
                sb.append("          p.PEDIDO_TOTAL AS PEDIDO_TOTAL, ");
                sb.append("          p.PAGAMENTO AS PAGAMENTO ");
                sb.append("     FROM ");
                sb.append("          PEDIDOS p ");
                sb.append("     WHERE ");
                sb.append("          p.DATA_RESTAURANTE = ? AND p.STATUS = 1 AND MESA_CODIGO IS NULL");
                /**
                 * Fazemos essa conversão nas datas porque ao consultar no banco
                 * com um formato diferente, o JDBC não consegue pegar todos os
                 * registros.
                 */
            }, CSPUtilidadesLangDateTime.formataDataHora(data, "dd.MM.yyyy"), CSPUtilidadesLangDateTime.formataDataHora(data, "dd.MM.yyyy"));

            {
                double bonusConv = 0;
                ResultSet sel = conn.select("SELECT VALOR_BONUS FROM EGULA_BONUS_CONVERSAO WHERE STATUS = 0");

                while (sel.next()) {
                    bonusConv += sel.getDouble("VALOR_BONUS");
                }

                HashMap<String, Object> dados = new HashMap<>();
                dados.put("CONVERSAO_PENDENTE", bonusConv);

                conn.gravaComposto("EGULA_LOJA_SALDOS", dados, null);
            }

            for (HashMap<String, Object> info : infos) {
                boolean isPedido = info.get("IS_PEDIDO").equals("1");

                if (isPedido) {
                    CSPUtilidadesAppFood.atualizaLojaSaldosPedido(conn, CSPUtilidadesLangDateTime.formataDataHora(data, "yyyy-MM-dd"), String.valueOf(info.get("PEDIDO_TOTAL")), String.valueOf(info.get("PAGAMENTO")));

                } else {
                    conn.deleteComposto("EGULA_LIBERACOES", "LIBERACAO_NUMERO = ?", String.valueOf(info.get("LIBERACAO_NUMERO")));

                    JSONObject j = new JSONObject();
                    j.put("LIBERACAO_NUMERO", String.valueOf(info.get("LIBERACAO_NUMERO")));
                    j.put("DATA_LIBERACAO", CSPUtilidadesLangDateTime.formataDataHora(data, "yyyy-MM-dd"));
                    j.put("VALOR_PEDIDO", Double.parseDouble(CSPUtilidadesLang.coalesce(String.valueOf(info.get("PEDIDOS_CREDITO")), "0")));
                    j.put("VALOR_BONUS", Double.parseDouble(CSPUtilidadesLang.coalesce(String.valueOf(info.get("PEDIDOS_BONUS_CREDITO")), "0")));
                    j.put("BONUS_PERCENTUAL", Double.parseDouble(CSPUtilidadesLang.coalesce(String.valueOf(info.get("PERCENTUAL_BONUS")), "0")));
                    j.put("PEDIDOS_PERCENTUAL", Double.parseDouble(CSPUtilidadesLang.coalesce(String.valueOf(info.get("PERCENTUAL_PEDIDO")), "0")));
                    j.put("LIMITE_DIAS", Integer.parseInt(CSPUtilidadesLang.coalesce(String.valueOf(info.get("LIMITE_DIAS")), "0")));
                    j.put("LIMITE_PEDIDOS", Double.parseDouble(CSPUtilidadesLang.coalesce(String.valueOf(info.get("LIMITE_PEDIDOS")), "0")));

                    JSONArray jArr = new JSONArray();
                    jArr.put(j);

                    CSPUtilidadesAppFood.updateSaldosLoja(new JSONObject() {
                        {
                            put("UP", jArr);
                        }
                    }, conn, data);
                }
            }
        }
    }

}
