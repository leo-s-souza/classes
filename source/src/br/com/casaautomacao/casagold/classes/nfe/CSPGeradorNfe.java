/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.nfe;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosXml;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import br.com.casaautomacao.casagold.classes.utilidadexml.CSPAssinarXML;
import br.com.casaautomacao.casagold.classes.utilidadexml.CSPManipXml;
import java.sql.ResultSet;
import java.text.Normalizer;
import java.util.HashMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.jdom2.Element;

/**
 * Classe utilizada para gerar o arquivo xml de uma NF-e.
 *
 * Isto aqui foi feito exclusivamente para passar no PAF-ECF, então sim, GAMBIA.
 * Teoricamente, isto aqui irá para o Retaguarda Web, pois o Retaguarda em java
 * seria descartado, ficando só com o PDV no java, então não se teve uma
 * preocupação em planejar uma API de NFe nos conformes. Porém, se isto aqui
 * ainda for usado, precisará ser inteiramente reajustado, 100%.
 *
 * @author @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public class CSPGeradorNfe {

    /**
     *
     * @param idVenda
     * @param indPres
     * @param dataFechamento
     * @param horaFechamento
     * @param cfop
     * @param frete
     * @param modPag
     * @param qtdVol
     * @return
     * @throws Exception
     */
    public static CSPArquivosXml geraNfe(int idVenda, int indPres, String dataFechamento,
            String horaFechamento, String cfop, Object frete, Object modPag, Object qtdVol) throws Exception {
        final CSPInstrucoesSQLBase BASE_CONTRATANTE = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE);
        CSPArquivosXml arq = new CSPArquivosXml(PATH + "/NFe/" + "nfe_" + CSPUtilidadesLang.pad(String.valueOf(idVenda), 8, "0") + ".xml");
        CSPManipXml xml = CSPManipXml.getInstance(arq, "NFe");
        double totalIcms = 0, totalBC = 0, totalNfe = 0;
        int ambiente = 1, idDest = 1;
        int cliente = 0, indFinal = 0;
        String serie = "", idNfe = "";
        String chaveNfe = "{UF}{AAMM}{CNPJ}{MOD}{SERIE}{nNF}{tpEmis}{cNF}";

        /**
         * Configurações iniciais.
         */
        {
            try (ResultSet rs = BASE_CONTRATANTE.select((sb) -> {
                sb.append("SELECT V.CLIENTE_AGENTE_ID, NFE.ID_NFE, NFE.OPERACAO, NFE.SERIE ");
                sb.append("FROM VENDA_NFE NFE ");
                sb.append("JOIN VENDA V ON V.ID = NFE.VENDA_ID ");
                sb.append("WHERE V.ID = ? ");
            }, idVenda)) {
                if (rs.next()) {
                    cliente = rs.getInt("CLIENTE_AGENTE_ID");
                    idNfe = rs.getString("ID_NFE");
                    indFinal = rs.getInt("OPERACAO");
                    serie = rs.getString("SERIE");
                }
            }

            try (ResultSet rs = BASE_CONTRATANTE.select("SELECT AMBIENTE_ENVIO FROM NFE_CONFIG")) {
                if (rs.next()) {
                    ambiente = rs.getInt("AMBIENTE_ENVIO");
                }
            }

            try (ResultSet rs = BASE_CONTRATANTE.select("SELECT DESTINO_INFO FROM PR_CALCULA_DESTINO_NFE(?, ?)",
                    CSPUtilidadesApplication.getContratanteAtivo().getAgente().getId(), cliente)) {
                if (rs.next()) {
                    if (rs.getString("DESTINO_INFO") != null) {
                        idDest = rs.getInt("DESTINO_INFO");
                    }
                }
            }
        }

        /**
         * Tags iniciais.
         */
        {
            xml.addNamespace("http://www.portalfiscal.inf.br/nfe");

            {//RootElement children
                xml.addSubTag("NFe", "infNFe");

                {//infNFe children
                    xml.addSubTag("infNFe", "ide");
                }
            }
        }

        /**
         * Informações de Identificação da NF-e.
         */
        {
            try (ResultSet rs = BASE_CONTRATANTE.select((sb) -> {
                sb.append("SELECT A.NOME, E.CODIGO_IBGE AS ESTADO_IBGE, C.CODIGO_IBGE AS CIDADE_IBGE,");
                sb.append(" CF.OPERACAO, CF.NATUREZA_OPERACAO, IE.ESTADO_FUSO_HORARIO ");
                sb.append("FROM CONTRATANTE_ATIVO V, CFOP CF ");
                sb.append("JOIN AGENTE A ON V.AGENTE_ID = A.ID ");
                sb.append("JOIN AGENTE_ENDERECO AE ON AE.AGENTE_ID = A.ID ");
                sb.append("JOIN VW_INFOS_ENDERECO IE ON  IE.ENDERECO_ID = AE.ENDERECO_ID ");
                sb.append("JOIN CIDADE C ON C.ID = IE.CIDADE_ID ");
                sb.append("JOIN ESTADO E ON E.ID = IE.ESTADO_ID ");
                sb.append("AND CF.CODIGO = ?");
            }, cfop)) {
                if (rs.next()) {
                    Element ide = xml.getTag("ide");

                    xml.addSubTag("cUF", rs.getString("ESTADO_IBGE"), ide);
                    xml.addSubTag("cNF", CSPUtilidadesLang.pad(Integer.toString(idVenda), 8, "0", true), ide);
                    xml.addSubTag("natOp", rs.getString("OPERACAO"), ide);
                    xml.addSubTag("mod", "55", ide);
                    xml.addSubTag("serie", serie, ide);
                    xml.addSubTag("nNF", idNfe, ide);

                    xml.addSubTag(
                            "dhEmi",
                            CSPUtilidadesLangDateTime.getDataHora("yyyy-MM-dd HH:mm:ss", 0).replace(" ", "T")
                            + rs.getString("ESTADO_FUSO_HORARIO").replace(".", ":").replace("-", "-0"),
                            ide);
                    xml.addSubTag("tpNF", "1", ide);
                    xml.addSubTag("idDest", idDest, ide);
                    xml.addSubTag("cMunFG", rs.getString("CIDADE_IBGE"), ide);
                    xml.addSubTag("tpImp", 1, ide);
                    xml.addSubTag("tpEmis", 1, ide);
                    xml.addSubTag("cDV", "", ide);
                    xml.addSubTag("tpAmb", ambiente, ide);
                    xml.addSubTag("finNFe", 1, ide);
                    xml.addSubTag("indFinal", indFinal, ide);
                    xml.addSubTag("indPres", indPres, ide);
                    xml.addSubTag("procEmi", "0", ide);
                    xml.addSubTag("verProc", "1.0", ide);

                    chaveNfe = chaveNfe
                            .replace("{UF}", rs.getString("ESTADO_IBGE"))
                            .replace("{AAMM}", CSPUtilidadesLangDateTime.getDataHora("yyMM", 0))
                            .replace("{MOD}", "55")
                            .replace("{SERIE}", CSPUtilidadesLang.pad(serie, 3, "0", true))
                            .replace("{nNF}", CSPUtilidadesLang.pad(idNfe, 9, "0", true))
                            .replace("{tpEmis}", "1")
                            .replace("{cNF}", CSPUtilidadesLang.pad(Integer.toString(idVenda), 8, "0", true));
                }
            }
        }

        /**
         * Identificação do Emitente da Nota Fiscal eletrônica.
         */
        {
            try (ResultSet rs = BASE_CONTRATANTE.select((sb) -> {
                sb.append("SELECT AGENTE_ID, AGENTE_CNPJ, AGENTE_NOME, AGENTE_NOME_FANTASIA,");
                sb.append(" COALESCE (INSC_ESTADUAL, '') AS INSC_ESTADUAL,");
                sb.append(" COALESCE (ENDERECO_LOGRADOURO, '') AS LOGRADOURO,");
                sb.append(" COALESCE (ENDERECO_NUMERO, '') AS NUMERO,");
                sb.append(" COALESCE (ENDERECO_COMPLEMENTO, '') AS COMPLEMENTO,");
                sb.append(" COALESCE (ENDERECO_BAIRRO_NOME, '') AS BAIRRO_NOME,");
                sb.append(" COALESCE (ENDERECO_IBGE_CIDADE, '') AS CIDADE_IBGE,");
                sb.append(" COALESCE (ENDERECO_CIDADE_NOME, '') AS CIDADE_NOME,");
                sb.append(" COALESCE (ENDERECO_ESTADO_UF, '') AS UF,");
                sb.append(" COALESCE (ENDERECO_CEP, '')AS CEP,");
                sb.append(" COALESCE (ENDERECO_PAIS_NOME, '') AS PAIS_NOME,");
                sb.append(" COALESCE (ENDERECO_PAIS_IBGE, '') AS PAIS_IBGE,");
                sb.append(" COALESCE (AGENTE_CRT, '') AS CRT ");
                sb.append("FROM VW_INFOS_CONTRATANTE_ATIVO");
            })) {
                if (rs.next()) {
                    Element emit = xml.addSubTag("infNFe", "emit");

                    xml.addSubTag("CNPJ", rs.getString("AGENTE_CNPJ"), emit);
                    xml.addSubTag("xNome", rs.getString("AGENTE_NOME"), emit);
                    xml.addSubTag("xFant", rs.getString("AGENTE_NOME_FANTASIA"), emit);
                    Element enderEmit = xml.addSubTag("enderEmit", "", emit);

                    xml.addSubTag("IE", rs.getString("INSC_ESTADUAL"), emit);
                    xml.addSubTag("CRT", rs.getString("CRT"), emit);

                    {//Endereço do emitente.
                        xml.addSubTag("xLgr", rs.getString("LOGRADOURO"), enderEmit);
                        xml.addSubTag("nro", rs.getString("NUMERO"), enderEmit);

                        if (!rs.getString("COMPLEMENTO").trim().isEmpty()) {
                            xml.addSubTag("xCpl", rs.getString("COMPLEMENTO"), enderEmit);
                        }

                        xml.addSubTag("xBairro", rs.getString("BAIRRO_NOME"), enderEmit);

                        xml.addSubTag("cMun", rs.getString("CIDADE_IBGE"), enderEmit);
                        xml.addSubTag("xMun", rs.getString("CIDADE_NOME"), enderEmit);
                        xml.addSubTag("UF", rs.getString("UF"), enderEmit);
                        xml.addSubTag("CEP", rs.getString("CEP"), enderEmit);
                        xml.addSubTag("cPais", "1058", enderEmit);
                        xml.addSubTag("xPais", "Brasil", enderEmit);
                    }

                    chaveNfe = chaveNfe.replace("{CNPJ}", rs.getString("AGENTE_CNPJ"));
                }
            }
        }

        /**
         * Identificação do Destinatário da Nota Fiscal eletrônica.
         */
        {
            Element enderDest;

            try (ResultSet rs = BASE_CONTRATANTE.selectOneRow("SELECT * FROM VW_INFOS_BLOCO_C WHERE ID_AGENTE = ?", cliente)) {
                Element el = xml.addSubTag("infNFe", "dest");

                if (!rs.getString("CNPJ").trim().isEmpty()) {
                    xml.addSubTag("CNPJ", rs.getString("CNPJ"), el);
                } else {
                    xml.addSubTag("CPF", rs.getString("CPF"), el);
                }

                xml.addSubTag("xNome", "NF-E EMITIDA EM AMBIENTE DE HOMOLOGACAO - SEM VALOR FISCAL", el);
                enderDest = xml.addSubTag("enderDest", "", el);

                if (!rs.getString("SUFRAMA").trim().isEmpty()) {
                    xml.addSubTag("ISUF", rs.getString("SUFRAMA"), el);
                }

                xml.addSubTag("indIEDest", "9", el);
            }

            /**
             * Grupo do endereço do destinatário da NF-e.
             */
            try (ResultSet rs = BASE_CONTRATANTE.select("SELECT * FROM VW_INFOS_BLOCO_CA WHERE ID_AGENTE = ?", cliente)) {
                if (rs.next()) {
                    xml.addSubTag("xLgr", rs.getString("ENDERECO_LOGRADOURO"), enderDest);
                    xml.addSubTag("nro", rs.getString("ENDERECO_NUMERO"), enderDest);

                    if (!rs.getString("ENDERECO_COMPLEMENTO").trim().isEmpty()) {
                        xml.addSubTag("xCpl", rs.getString("ENDERECO_COMPLEMENTO"), enderDest);
                    }

                    xml.addSubTag("xBairro", rs.getString("BAIRRO_NOME"), enderDest);

                    if (!rs.getString("EMAIL").trim().isEmpty()) {
                        xml.addSubTag("xEmail", rs.getString("EMAIL"), enderDest);
                    }

                    xml.addSubTag("cMun", idDest == 3 ? "9999999" : rs.getString("CIDADE_IBGE"), enderDest);
                    xml.addSubTag("xMun", idDest == 3 ? "EXTERIOR" : rs.getString("CIDADE_NOME"), enderDest);
                    xml.addSubTag("UF", idDest == 3 ? "EX" : rs.getString("ESTADO_SIGLA"), enderDest);
                    xml.addSubTag("CEP", rs.getString("ENDERECO_CEP"), enderDest);
                    xml.addSubTag("cPais", "1058", enderDest);
                    xml.addSubTag("xPais", "Brasil", enderDest);

                    if (!rs.getString("TELEFONE").trim().isEmpty()) {
                        xml.addSubTag("fone", rs.getString("TELEFONE"), enderDest);
                    }

                    Element autXML = xml.addSubTag("infNFe", "autXML");
                    xml.addSubTag("CPF", rs.getString("CPF"), autXML);
                }
            }
        }

        /**
         * Detalhamento de Produtos e Serviços da NF-e.
         */
        {
            try (ResultSet rs = BASE_CONTRATANTE.select("SELECT * FROM VW_INFOS_PROD_NFE WHERE VENDA_ID = ?", idVenda)) {
                int cont = 1;
                while (rs.next()) {
                    Element det = xml.addSubTag("infNFe", "det");
                    xml.addAttribute("nItem", String.valueOf(cont), det);
                    Element prod = xml.addSubTag("prod", det);

                    xml.addSubTag("cProd", rs.getString("PRODUTO_ID"), prod);
                    xml.addSubTag("cEAN", rs.getString("CODIGO_BARRAS"), prod);
                    xml.addSubTag("xProd", rs.getString("PRODUTO_DESCRICAO"), prod);
                    xml.addSubTag("NCM", CSPUtilidadesLang.pad(rs.getString("NCM"), 8, "0", true), prod);
                    xml.addSubTag("CFOP", rs.getString("CFOP").replaceAll("[^\\d]", ""), prod);
                    xml.addSubTag("uCom", rs.getString("SIGLA_UNIDADE_MEDIDA_PRODUTO"), prod);
                    xml.addSubTag("qCom", rs.getString("QUANTIDADE_COMPRADA"), prod);
                    xml.addSubTag("vUnCom", rs.getString("VALOR_COBRADO_POR_UNIDADE"), prod);
                    xml.addSubTag("vProd", rs.getString("VALOR_TOTAL"), prod);
                    xml.addSubTag("cEANTrib", rs.getString("CODIGO_BARRAS"), prod);
                    xml.addSubTag("uTrib", rs.getString("SIGLA_UNIDADE_MEDIDA_PRODUTO"), prod);
                    xml.addSubTag("qTrib", rs.getString("QUANTIDADE_COMPRADA"), prod);
                    xml.addSubTag("vUnTrib", rs.getString("VALOR_COBRADO_POR_UNIDADE"), prod);
                    xml.addSubTag("indTot", "1", prod);

                    {
                        Element imposto = xml.addSubTag("imposto", det);
                        {//ICMS
                            String codTrib = rs.getString("CODIGO_TRIBUTO");

                            Element icms = xml.addSubTag("ICMS" + codTrib, xml.addSubTag("ICMS", imposto));
                            xml.addSubTag("orig", rs.getString("CODIGO_ORIGEM"), icms);
                            xml.addSubTag("CST", codTrib, icms);

                            if (!codTrib.equals("60") && !codTrib.equals("40")) {
                                xml.addSubTag("modBC", "3", icms);
                                xml.addSubTag("vBC", rs.getString("VALOR_TOTAL"), icms);
                                xml.addSubTag("pICMS", rs.getString("ALIQUOTA"), icms);
                                xml.addSubTag("vICMS", rs.getString("VALOR_ICMS"), icms);
                            }

                            if (rs.getDouble("VALOR_ICMS") > 0) {
                                totalBC += rs.getDouble("VALOR_TOTAL");
                                totalIcms += rs.getDouble("VALOR_ICMS");
                            }
                            totalNfe += rs.getDouble("VALOR_TOTAL");
                        }

                        //IPI
                        Element ipi = xml.addSubTag("IPI", imposto);
                        xml.addSubTag("cEnq", "999", ipi);
                        Element ipiTrib = xml.addSubTag("IPITrib", ipi);
                        xml.addSubTag("CST", "49", ipiTrib);
                        xml.addSubTag("vBC", "0.00", ipiTrib);
                        xml.addSubTag("pIPI", "0.00", ipiTrib);
                        xml.addSubTag("vIPI", "0.00", ipiTrib);

                        //PIS
                        Element pis = xml.addSubTag("PISAliq", xml.addSubTag("PIS", imposto));
                        xml.addSubTag("CST", "01", pis);
                        xml.addSubTag("vBC", "0.00", pis);
                        xml.addSubTag("pPIS", "0.00", pis);
                        xml.addSubTag("vPIS", "0.00", pis);

                        //COFINS
                        Element cofins = xml.addSubTag("COFINSAliq", xml.addSubTag("COFINS", imposto));
                        xml.addSubTag("CST", "01", cofins);
                        xml.addSubTag("vBC", "0.00", cofins);
                        xml.addSubTag("pCOFINS", "0.00", cofins);
                        xml.addSubTag("vCOFINS", "0.00", cofins);
                    }

                    cont++;
                }
            }
        }

        String formatTotal = CSPUtilidadesLang.defaultDecimalFormat(totalNfe).replace(",", ".");

        /**
         * Valores Totais da NF-e.
         */
        {

            Element total = xml.addSubTag("ICMSTot", xml.addSubTag("infNFe", "total"));
            xml.addSubTag("vBC", CSPUtilidadesLang.defaultDecimalFormat(totalBC).replace(",", "."), total);
            xml.addSubTag("vICMS", CSPUtilidadesLang.defaultDecimalFormat(totalIcms).replace(",", "."), total);
            xml.addSubTag("vICMSDeson", "0.00", total);
            xml.addSubTag("vFCP", "0.00", total);
            xml.addSubTag("vBCST", "0.00", total);
            xml.addSubTag("vST", "0.00", total);
            xml.addSubTag("vFCPST", "0.00", total);
            xml.addSubTag("vFCPSTRet", "0.00", total);
            xml.addSubTag("vProd", formatTotal, total);
            xml.addSubTag("vFrete", "0.00", total);
            xml.addSubTag("vSeg", "0.00", total);
            xml.addSubTag("vDesc", "0.00", total);
            xml.addSubTag("vII", "0.00", total);
            xml.addSubTag("vIPI", "0.00", total);
            xml.addSubTag("vIPIDevol", "0.00", total);
            xml.addSubTag("vPIS", "0.00", total);
            xml.addSubTag("vCOFINS", "0.00", total);
            xml.addSubTag("vOutro", "0.00", total);
            xml.addSubTag("vNF", formatTotal, total);
        }

        /**
         * Informações do Transporte da NF-e.
         */
        {
            Element transp = xml.addSubTag("infNFe", "transp");
            xml.addSubTag("modFrete", frete, transp);
            xml.addSubTag("qVol", qtdVol, xml.addSubTag("vol", transp));
        }

        /**
         * Formas de Pagamento.
         */
        {
            Element pagItem = xml.addSubTag("detPag", xml.addSubTag("infNFe", "pag"));
            xml.addSubTag("tPag", modPag, pagItem);
            xml.addSubTag("vPag", formatTotal, pagItem);
        }

        /**
         * MD5 PDV.
         */
        {
            Element infAdic = xml.addSubTag("infNFe", "infAdic");
            xml.addSubTag("infAdFisco", "Nota Fiscal Eletronica gerada a partir do sistema Casa Gold", infAdic);
            xml.addSubTag("infCpl", "MD5: " + new CSPArquivos(PATH + "/paf-lista-md5.txt").getMd5(), infAdic);
        }

        HashMap<String, Object> dados = new HashMap<>();

        /**
         * Código verificador.
         */
        {
            String[] split = chaveNfe.split("");
            int x = 4;
            int cDV = 0;

            for (String string : split) {
                cDV += Integer.valueOf(string) * x;

                if (x != 2) {
                    x--;
                } else {
                    x = 9;
                }
            }

            cDV = 11 - (cDV % 11);
            if (cDV > 9) {
                cDV = 0;
            }

            xml.addSubTagAttribute("versao", "4.00", "infNFe");
            xml.addSubTagAttribute("Id", "NFe" + chaveNfe + cDV, "infNFe");
            xml.getTag("cDV").addContent(String.valueOf(cDV));
            dados.put("CHAVE", chaveNfe + cDV);
        }

        arq.setContent(removerCaracteresEspeciais(xml.toString().replace(" xmlns=\"\"", "")));
        xml.parseFileContentWithouFormat();

        /**
         * Assinando o XML de Lote da NF-e.
         */
        try (ResultSet rs = BASE_CONTRATANTE.select("SELECT * FROM CERTIFICADO_DIGITAL")) {
            if (rs.next()) {
                CSPAssinarXML.assinaXML(arq.getAbsolutePath(), PATH + "/" + rs.getString("NOME") + ".pfx", rs.getString("SENHA"), CSPAssinarXML.Mode.NFE);
            }
        }

        BASE_CONTRATANTE.insertComposto(false, "VENDA_NFE", dados, "VENDA_ID = ?", idVenda);
        double total = 0;
        try (ResultSet rs = BASE_CONTRATANTE.select("SELECT * FROM VW_INFOS_NFE_REGISTRO_J1 WHERE ID = ?", idVenda)) {
            if (rs.next()) {
                total = rs.getDouble("VALOR_TOTAL_LIQUIDO");
                String cript = DigestUtils.sha256Hex(
                        new StringBuilder()
                                .append(rs.getString("AUTOR_CNPJ"))
                                .append(rs.getString("DATA_ABERTURA"))
                                .append(rs.getString("VALOR_TOTAL_BRUTO"))
                                .append(rs.getString("VALOR_DESCONTO"))
                                .append(rs.getString("VALOR_ACRESCIMO"))
                                .append(rs.getString("VALOR_TOTAL_LIQUIDO"))
                                .append(rs.getString("IS_CANCELADA"))
                                .append(rs.getString("ACRESCIMO_DESCONTO"))
                                .append(rs.getString("CLIENTE_NOME"))
                                .append(rs.getString("CLIENTE_CNPJ"))
                                .append(rs.getString("ID_NFE"))
                                .append(rs.getString("SERIE_NF"))
                                .append(rs.getString("CHAVE_NF"))
                                .append(rs.getString("TIPO_NF")).toString());

                BASE_CONTRATANTE.insertComposto(false, "VENDA_NFE", new HashMap() {
                    {
                        put("CRIPT_NFE", cript);
                    }
                }, "VENDA_ID = ?", idVenda);
            }
        }

        try (ResultSet sql = BASE_CONTRATANTE.select((sb) -> {
            sb.append("SELECT TOTAL");
            sb.append(" FROM TOTAL_DIARIO_PGTO");
            sb.append(" WHERE DATA_MOVIMENTO = ?");
            sb.append(" AND FORMA_PAGAMENTO_ID = ?");
            sb.append(" AND ID_OFICIAL = ?");
        }, dataFechamento, modPag, 3)) {
            if (sql.next()) {
                double temp = total + sql.getDouble("TOTAL");

                BASE_CONTRATANTE.insertComposto(false, "TOTAL_DIARIO_PGTO", new HashMap() {
                    {
                        put("TOTAL", temp);
                    }
                }, "DATA_MOVIMENTO = ? AND FORMA_PAGAMENTO_ID = ? AND ID_OFICIAL = ?", dataFechamento, modPag, 3);

            } else {
                double temp = total;
                BASE_CONTRATANTE.insertComposto("TOTAL_DIARIO_PGTO", new HashMap() {
                    {
                        put("TOTAL", temp);
                        put("DATA_MOVIMENTO", dataFechamento);
                        put("FORMA_PAGAMENTO_ID", modPag);
                        put("ID_OFICIAL", 3);
                    }
                });
            }
        }

        try (ResultSet rs = BASE_CONTRATANTE.select((sb) -> {
            sb.append("SELECT *");
            sb.append(" FROM TOTAL_DIARIO_PGTO");
            sb.append(" WHERE DATA_MOVIMENTO = ?");
            sb.append(" AND FORMA_PAGAMENTO_ID = ?");
            sb.append(" AND ID_OFICIAL = ?");
        }, dataFechamento, modPag, 3)) {
            if (rs.next()) {
                String temp = DigestUtils.sha256Hex(
                        new StringBuilder()
                                .append(rs.getString("DATA_MOVIMENTO"))
                                .append(Integer.parseInt(modPag.toString()))
                                .append(3)
                                .append(rs.getString("TOTAL"))
                                .toString()
                );

                BASE_CONTRATANTE.insertComposto(false, "TOTAL_DIARIO_PGTO", new HashMap() {
                    {
                        put("CRIPT_INFORMACAO", temp);
                    }
                }, "DATA_MOVIMENTO = ? AND FORMA_PAGAMENTO_ID = ? AND ID_OFICIAL = ?",
                        dataFechamento,
                        modPag,
                        3);
            }
        }

        try (ResultSet rs = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE)
                .select("SELECT * FROM VW_INFOS_NFE_REGISTRO_J2 WHERE VENDA_ID = ?", idVenda)) {
            while (rs.next()) {
                String cript = DigestUtils.sha256Hex(
                        new StringBuilder()
                                .append(rs.getString("ITEM_ID"))
                                .append(rs.getString("AUTOR_CNPJ"))
                                .append(rs.getString("ABERTURA_HORARIO"))
                                .append(rs.getString("COD_PRODUTO"))
                                .append(rs.getString("PRODUTO_DESCRICAO"))
                                .append(rs.getString("QUANTIDADE_COMPRADA"))
                                .append(rs.getString("SIGLA_UNIDADE_MEDIDA_PRODUTO"))
                                .append(rs.getString("VALOR_COBRADO_POR_UNIDADE"))
                                .append(rs.getString("VALOR_DESCONTO"))
                                .append(rs.getString("VALOR_ACRESCIMO"))
                                .append(rs.getString("VALOR_TOTAL"))
                                .append(rs.getString("DECIMAIS_QUANTIDADE"))
                                .append(rs.getString("DECIMAIS_PRECO"))
                                .append(rs.getString("ID_NFE"))
                                .append(rs.getString("SERIE_NF"))
                                .append(rs.getString("CHAVE_NF"))
                                .append(rs.getString("TIPO_NF")).toString());

                BASE_CONTRATANTE.insertComposto(false, "VENDA_ITEM_NFE", new HashMap() {
                    {
                        put("CRIPT_ITEM", cript);
                    }
                }, "VENDA_ITEM_ID = ?", rs.getInt("ITEM_ID"));
            }
        }

        return arq;
    }

    /**
     * Retorna o proximo id da NF-e.
     *
     * @return
     * @throws Exception
     */
    public static int getProximoIdNfe() throws Exception {
        CSPInstrucoesSQLBase conn = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE);
        ResultSet rs = conn.select("SELECT NNF_INICIO FROM NFE_CONFIG");
        if (rs.next()) {
            return rs.getInt("NNF_INICIO");
        }

        return 0;
    }

    public static String removerCaracteresEspeciais(String string) {
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", "");
        return string;
    }
}
