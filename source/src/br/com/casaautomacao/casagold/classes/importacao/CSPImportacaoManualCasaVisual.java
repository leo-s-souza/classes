/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

/**
 * Classe de importação manual de informações CasaVisual => CasaGold
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 22/08/2016 - 18:47:01
 */
public class CSPImportacaoManualCasaVisual extends CSPImportacaoManual {

    private final CSPInstrucoesSQLBase connDadosGold;
    private final CSPInstrucoesSQLBase connConfigGold;
    private final CSPInstrucoesSQLBase connPadraoVisual;

    public CSPImportacaoManualCasaVisual(CSPInstrucoesSQLBase connDadosGold, CSPInstrucoesSQLBase connPadraoVisual) {
        this.connDadosGold = connDadosGold;
        this.connConfigGold = null;
        this.connPadraoVisual = connPadraoVisual;
    }

    /**
     * Analisa, encontra e replica o cadastro de grupos de produtos para a nova
     * base.
     *
     * @param modeApp ModeApp - ALL:todos os novos grupos serão transformados em
     * app automaticamente ou NONE:nenhum dos novos grupos serão transformados
     * em app
     * @return HashMap<String, String> - Relaçao de codigo CasaVisual=CasaGold
     * @throws java.lang.Exception
     */
    public LinkedHashMap<String, String> importaGruposProdutos(ModeApp modeApp) throws Exception {
        final LinkedHashSet<String> codsExists = new LinkedHashSet<>();
        final ResultSet selectGrs = this.connDadosGold.select("select GRUPOS_CODIGO from PRODUTOS_GRUPOS");
        final LinkedHashMap<String, String> r = new LinkedHashMap<>();

        while (selectGrs.next()) {
            codsExists.add(selectGrs.getString("GRUPOS_CODIGO"));
        }

        for (HashMap<String, Object> sl : this.connPadraoVisual.selectInMap((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("     r.CODIGO, ");
            sb.append("     r.DESCRICAO ");
            sb.append("FROM ");
            sb.append("    GRUPO r ");
            sb.append("ORDER BY ");
            sb.append("    r.CODIGO ASC, ");
            sb.append("    r.DESCRICAO ASC ");
        })) {
            if (!codsExists.contains((String) sl.get("CODIGO"))) {

                this.connDadosGold.gravaCompostoSomenteInsert("PRODUTOS_GRUPOS", new HashMap() {
                    {
                        put("GRUPOS_CODIGO", sl.get("CODIGO"));
                        put("DESCRICAO", sl.get("DESCRICAO"));
                        put("APP", modeApp.equals(ModeApp.ALL) ? "X" : null);
                    }
                }, " GRUPOS_CODIGO = ?", sl.get("CODIGO"));
            }

            r.put(sl.get("CODIGO") + "", sl.get("CODIGO") + "");//Será usada no futuro!

        }

        return r;
    }

    /**
     * Analisa, encontra e replica o cadastro de produtos para a nova base.
     *
     * @param relacaoGruposVisualxGold HashMap<String, String> - Relaçao de
     * codigo CasaVisual=CasaGold
     * @param modeApp ModeApp - CONDITION_A:somente produtos com estoque > 0
     * serão transformados em app ou ALL:todos os novos produtos serão
     * transformados em app automaticamente ou NONE:nenhum dos novos produtos
     * serão transformados em app
     * @param imgsInput CSPArquivos - Pasta contendo imagens para a auto relação
     * com o cadastro
     * @param imgsOutput CSPArquivos - Pasta de destino das imagens de produtos
     * importados
     * @throws Exception
     */
    public void importaProdutos(LinkedHashMap<String, String> relacaoGruposVisualxGold, ModeApp modeApp, CSPArquivos imgsInput, CSPArquivos imgsOutput) throws Exception {
        final ArrayList<CSPArquivos> map = new ArrayList<>();
        final LinkedHashSet<String> codsExists = new LinkedHashSet<>();

        final ResultSet selectPrds = this.connDadosGold.select("select PRODUTO_CODIGO from PRODUTOS");
        while (selectPrds.next()) {
            codsExists.add(selectPrds.getString("PRODUTO_CODIGO"));
        }

        this.mapeiaPastas(map, imgsInput);
        this.connDadosGold.setAutoCommit(false);

        for (HashMap<String, Object> sl : this.connPadraoVisual.selectInMap((StringBuilder sb) -> {
            sb.append("select ");
            sb.append("    p.CODIGO, ");
            sb.append("    p.DESCRICAO, ");
            sb.append("    ps.CODIGO_NCM AS NCM, ");
            sb.append("    p.CODIGO_ALIQUOTA AS ICMS, ");
            sb.append("    COALESCE(p.PRECO_DE_VENDA, 0) AS PRECO_DE_VENDA, ");
            sb.append("    gr.CODIGO AS GR_CODIGO, ");
            sb.append("    COALESCE(p.QUANTIDADE,0) AS QUANTIDADE, ");
            sb.append("    p.CODIGO_UNIDADE ");
            sb.append("from ");
            sb.append("    PRODUTOS p, ");
            sb.append("    PRODUTOS_SPED ps, ");
            sb.append("    GRUPO gr, ");
            sb.append("    PRODUTO_GRUPO pgr ");
            sb.append("WHERE ");
            sb.append("    ps.CODIGO_PRODUTO = p.CODIGO AND ");
            sb.append("    gr.CHAVE = pgr.CHAVE_GRUPO AND ");
            sb.append("    pgr.CODIGO_PRODUTO = p.CODIGO ");

            sb.append("ORDER BY ");
            sb.append("    p.CODIGO ASC ");
        })) {

            if (codsExists.contains((String) sl.get("CODIGO"))) {
                continue;
            }
            this.connDadosGold.insertComposto(true, "PRODUTOS", new HashMap() {
                {
                    final String desc = (String) sl.get("DESCRICAO");
                    put("PRODUTO_CODIGO", CSPUtilidadesLang.pad((String) sl.get("CODIGO"), 6, "0"));
                    put("DESCRICAO", desc);
                    put("NCM", sl.get("NCM"));
                    put("TIPO_ITEM", "00");
                    put("ICMS", sl.get("ICMS"));
                    put("PRECO_VENDA", sl.get("PRECO_DE_VENDA"));
                    put("UNIDADE_CODIGO", sl.get("CODIGO_UNIDADE"));
                    put("GRUPOS_CODIGO", relacaoGruposVisualxGold.get((String) sl.get("GR_CODIGO")));

                    if (modeApp.equals(ModeApp.CONDITION_A)) {
                        put("APP", ((int) sl.get("QUANTIDADE")) > 0 ? "X" : null);
                    } else if (modeApp.equals(ModeApp.ALL)) {
                        put("APP", "X");
                    }

                    if (desc != null && desc.contains("(") && desc.contains(")")) {
                        String find = desc.split("\\(")[1].split("\\)")[0];
                        put("APP_IMAGEM", copyAux(map, imgsOutput, find));
                    }
                }
            });
        }

        this.connDadosGold.commit();
        this.connDadosGold.setAutoCommit(true);
    }

    /**
     * Enum para facilitar a parametrização de configurações a respeito do app
     */
    public enum ModeApp {

        ALL,
        CONDITION_A,
        CONDITION_B,
        CONDITION_C,
        NONE
    }
}
