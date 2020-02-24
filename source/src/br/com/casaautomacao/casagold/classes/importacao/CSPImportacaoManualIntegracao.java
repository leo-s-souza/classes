/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * Classe de importação manual de informações de integração
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 25/08/2016 - 08:07:00
 */
public abstract class CSPImportacaoManualIntegracao extends CSPImportacaoManual {

    private final CSPInstrucoesSQLBase connDest;
    private final CSPInstrucoesSQLBase connOrigem;

    public CSPImportacaoManualIntegracao(CSPInstrucoesSQLBase connDest, CSPInstrucoesSQLBase connOrigem) {
        this.connDest = connDest;
        this.connOrigem = connOrigem;
    }

    /**
     * Realiza a importação de agentes
     *
     * @param select CSPUtilidadesLang.StringBuilderShortcut - Select que
     * retornará as informações de origem
     * @param selectTelefones CSPUtilidadesLang.StringBuilderShortcut - Select
     * que retornará as informações sobre os telefones do registro de origem
     * @param importOnlyWithCnpjOrCpf boolean - Determina que somente registro
     * com cnpj ou cpf serão importados
     * @param importOnlyWithMunicipio boolean - Determina que somente registros
     * com valores válidos para o campo MUNICIPIO serão importados
     * @param forceFieldNascimentoToNullOnCnpj boolean - Determina que o campo
     * NASCIMENTO será setado como null em registros com cnpj
     * @throws Exception
     */
    protected void importaAgentes(
            CSPUtilidadesLang.StringBuilderShortcut select,
            CSPUtilidadesLang.StringBuilderShortcut selectTelefones,
            boolean importOnlyWithCnpjOrCpf,
            boolean importOnlyWithMunicipio,
            boolean forceFieldNascimentoToNullOnCnpj
    ) throws Exception {
        final LinkedHashSet<String> toNotAllowCnpj = new LinkedHashSet<>();
        final LinkedHashSet<String> toNotAllowCpf = new LinkedHashSet<>();
        final LinkedHashSet<String> toNotAllowRg = new LinkedHashSet<>();
        final LinkedHashSet<String> toNotAllowIe = new LinkedHashSet<>();
        {
            final ResultSet slCurrents = this.connDest.select((StringBuilder sb) -> {
                sb.append("SELECT ");
                sb.append("    r.CNPJ, ");
                sb.append("    r.CPF, ");
                sb.append("    r.IDENTIDADE, ");
                sb.append("    r.IE ");
                sb.append("FROM ");
                sb.append("    INTEGRACAO_AGENTES r ");
//                sb.append("WHERE ");
//                sb.append("    (r.CNPJ is not null) OR ");
//                sb.append("    (r.CPF is not null) OR ");
//                sb.append("    (r.IDENTIDADE is not null) OR ");
//                sb.append("    (r.IE is not null) ");
            });

            while (slCurrents.next()) {
                toNotAllowCnpj.add(this.sanitizeText(slCurrents, "CNPJ", null, null));
                toNotAllowCpf.add(this.sanitizeText(slCurrents, "CPF", null, null));
                toNotAllowRg.add(this.sanitizeText(slCurrents, "IDENTIDADE", null, null));
                toNotAllowIe.add(this.sanitizeText(slCurrents, "IE", null, null));
            }
//            System.out.println(toNotAllow);
        }

        final HashMap<String, Object> tmp = new HashMap<>();

        for (HashMap<String, Object> sl : this.connOrigem.selectInMap(select)) {
            tmp.clear();

            final String cpf = this.sanitizeText(sl, "CPF", "[^0-9]", null);
            final String cnpj = this.sanitizeText(sl, "CNPJ", "[^0-9]", null);
            final String municipioCod = this.sanitizeText(sl, "MUNICIPIO", "[^0-9]", null);

            if (importOnlyWithCnpjOrCpf) {
                if (cpf == null && cnpj == null) {
                    continue;
                }
            }
            if (importOnlyWithMunicipio) {
                if (municipioCod == null) {
                    continue;
                }
            }

            final String rg = this.sanitizeText(sl, "IDENTIDADE", "[^0-9]", null);
            final String ie = this.sanitizeText(sl, "IE", "[^0-9]", null);

//            if (cpf == null && cnpj == null && rg == null && ie == null) {
//                continue;
//            }
//            System.out.println(cpf + " " + cnpj + " " + rg + " " + ie);
//            System.out.println(toNotAllow.contains(cpf) );
//            System.out.println(toNotAllow.contains(cnpj) );
//            System.out.println(toNotAllow.contains(rg) );
//            System.out.println(toNotAllow.contains(ie) );
            if ((cnpj != null && toNotAllowCnpj.contains(cnpj))
                    || (cpf != null && toNotAllowCpf.contains(cpf))
                    || (rg != null && toNotAllowRg.contains(rg))
                    || (ie != null && toNotAllowIe.contains(ie))) {
//                System.out.println("?");
                continue;
            }

            toNotAllowCpf.add(cpf);
            toNotAllowCnpj.add(cnpj);
            toNotAllowRg.add(rg);
            toNotAllowIe.add(ie);

            final String oldCod = this.sanitizeText(sl, "AGENTE_CODIGO", null, null);
            final String newCod = this.buildNewId(this.connDest, "INTEGRACAO_AGENTES", "AGENTE_CODIGO", 6);

            tmp.put("AGENTE_CODIGO", newCod);
            tmp.put("CPF", cpf);
            tmp.put("CNPJ", cnpj);
            tmp.put("IDENTIDADE", rg);
            tmp.put("IE", ie);
            this.sanitizeTextAndPut(sl, tmp, "AGENTE_DESCRICAO", null, null);
            this.sanitizeTextAndPut(sl, tmp, "FANTASIA", null, null);
            if (forceFieldNascimentoToNullOnCnpj) {
                if (cnpj == null) {
                    this.sanitizeTextAndPut(sl, tmp, "NASCIMENTO", null, null);
                }
            } else {
                this.sanitizeTextAndPut(sl, tmp, "NASCIMENTO", null, null);
            }
            this.sanitizeTextAndPut(sl, tmp, "PAI", null, null);
            this.sanitizeTextAndPut(sl, tmp, "MAE", null, null);
            this.sanitizeTextAndPut(sl, tmp, "IM", "[^0-9]", null);
            this.sanitizeTextAndPut(sl, tmp, "PAIS", "[^0-9]", null);
            this.sanitizeTextAndPut(sl, tmp, "ESTADO", "[^0-9]", null);
            tmp.put("MUNICIPIO", municipioCod);
            this.sanitizeTextAndPut(sl, tmp, "BAIRRO", null, null);
            this.sanitizeTextAndPut(sl, tmp, "CEP", "[^0-9]", null);
            this.sanitizeTextAndPut(sl, tmp, "LOGRADOURO", null, null);
            this.sanitizeTextAndPut(sl, tmp, "NUMERO", "[^0-9]", null);
            this.sanitizeTextAndPut(sl, tmp, "COMPLEMENTO", null, null);

            this.connDest.insertComposto("INTEGRACAO_AGENTES", tmp);

            for (HashMap<String, Object> slTel : this.connOrigem.selectInMap(selectTelefones, oldCod)) {

                tmp.clear();

                tmp.put("AGENTE_CODIGO", newCod);
                tmp.put("FONE_CODIGO", this.buildNewId(this.connDest, "AGENTE_FONE", "FONE_CODIGO", 6));
                this.sanitizeTextAndPut(slTel, tmp, "NUMERO", "[^0-9]", null);

                this.connDest.insertComposto("AGENTE_FONE", tmp);
            }

        }

    }

}
