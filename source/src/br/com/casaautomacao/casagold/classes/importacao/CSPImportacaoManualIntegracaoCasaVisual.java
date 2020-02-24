/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;

/**
 * Classe de importação manual de informações de integração - Com origem no
 * projeto Casa Gold
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/08/2016 - 09:35:00
 */
public class CSPImportacaoManualIntegracaoCasaVisual extends CSPImportacaoManualIntegracao {

    public CSPImportacaoManualIntegracaoCasaVisual(CSPInstrucoesSQLBase connDest, CSPInstrucoesSQLBase connOrigem) {
        super(connDest, connOrigem);
    }

    /**
     * Realiza a importação de agentes originados da tabela de clientes
     *
     * @param importOnlyWithCnpjOrCpf boolean - Determina que somente registro
     * com cnpj ou cpf serão importados
     * @param importOnlyWithMunicipio boolean - Determina que somente registros
     * com valores válidos para o campo MUNICIPIO serão importados
     * @param forceFieldNascimentoToNullOnCnpj boolean - Determina que o campo
     * NASCIMENTO será setado como null em registros com cnpj
     * @throws Exception
     */
    public void importaAgentesFromClientes(
            boolean importOnlyWithCnpjOrCpf,
            boolean importOnlyWithMunicipio,
            boolean forceFieldNascimentoToNullOnCnpj) throws Exception {
        this.importaAgentes((StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("    r.CODIGO AS AGENTE_CODIGO, ");
            sb.append("    r.CLIENTE AS AGENTE_DESCRICAO, ");
            sb.append("    r.ENDERECO AS LOGRADOURO, ");
            sb.append("    r.CNPJ, ");
            sb.append("    r.IDENTIDADE, ");
            sb.append("    r.INSCR_ESTADUAL AS IE, ");
            sb.append("	COALESCE(( ");
            sb.append("        SELECT ");
            sb.append("            c.CODIGO_PAIS ");
            sb.append("        FROM ");
            sb.append("            CIDADE c ");
            sb.append("        WHERE ");
            sb.append("            c.CODIGO_IBGE = r.CODIGO_IBGE ");
            sb.append("    ),null) AS PAIS, ");
            sb.append("       COALESCE(( ");
            sb.append("        SELECT ");
            sb.append("            e.CODIGO_IBGE ");
            sb.append("        FROM ");
            sb.append("            ESTADO e, ");
            sb.append("            CIDADE c ");
            sb.append("        WHERE ");
            sb.append("            e.CODIGO = c.CODIGO_ESTADO AND ");
            sb.append("            c.CODIGO_IBGE = r.CODIGO_IBGE ");
            sb.append("    ),null) AS ESTADO, ");
            sb.append("    r.NASCIMENTO, ");
            sb.append("    r.PAI, ");
            sb.append("    r.MAE, ");
            sb.append("    r.CEP, ");
            sb.append("    r.CPF, ");
            sb.append("    r.NUMERO, ");
            sb.append("    r.COMPLEMENTO, ");
            sb.append("    r.NOME_BAIRRO AS BAIRRO, ");
            sb.append("    r.CODIGO_IBGE AS MUNICIPIO, ");
            sb.append("    r.NOME_FANTASIA AS FANTASIA, ");
            sb.append("    null as IM ");
            sb.append("FROM ");
            sb.append("    CLIENTE r ");

        }, (StringBuilder sb) -> {
            sb.append("SELECT ");
            sb.append("    lt.TELEFONE AS NUMERO ");
            sb.append("FROM ");
            sb.append("    LISTA_TELEFONICA lt ");
            sb.append("WHERE ");
            sb.append("    lt.TIPO_TELEFONE = '01' AND ");
            sb.append("    lt.CODIGO = ? ");
            sb.append("ORDER BY ");
            sb.append("    lt.SEQUENCIA ASC ");

        }, importOnlyWithCnpjOrCpf, importOnlyWithMunicipio, forceFieldNascimentoToNullOnCnpj);
    }

    /**
     * Realiza a importação de agentes originados da tabela de fornecedores
     *
     * @param importOnlyWithCnpjOrCpf boolean - Determina que somente registro
     * com cnpj ou cpf serão importados
     *
     * @param importOnlyWithMunicipio boolean - Determina que somente registros
     * com valores válidos para o campo MUNICIPIO serão importados
     * @param forceFieldNascimentoToNullOnCnpj boolean - Determina que o campo
     * NASCIMENTO será setado como null em registros com cnpj
     * @throws Exception
     */
    public void importaAgentesFromFornecedores(
            boolean importOnlyWithCnpjOrCpf,
            boolean importOnlyWithMunicipio,
            boolean forceFieldNascimentoToNullOnCnpj) throws Exception {

        this.importaAgentes(
                (StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("    r.CODIGO AS AGENTE_CODIGO, ");
                    sb.append("    r.FORNECEDOR AS AGENTE_DESCRICAO, ");
                    sb.append("    r.CNPJ, ");
                    sb.append("    null AS IDENTIDADE, ");
                    sb.append("    r.INSCR_ESTADUAL AS IE, ");
                    sb.append("    null AS PAI, ");
                    sb.append("    null AS MAE, ");
                    sb.append("    COALESCE(( ");
                    sb.append("        SELECT ");
                    sb.append("            c.CODIGO_PAIS ");
                    sb.append("        FROM ");
                    sb.append("            CIDADE c ");
                    sb.append("        WHERE ");
                    sb.append("            c.CODIGO_IBGE = r.CODIGO_IBGE ");
                    sb.append("    ),null) AS PAIS, ");
                    sb.append("       COALESCE(( ");
                    sb.append("        SELECT ");
                    sb.append("            e.CODIGO_IBGE ");
                    sb.append("        FROM ");
                    sb.append("            ESTADO e, ");
                    sb.append("            CIDADE c ");
                    sb.append("        WHERE ");
                    sb.append("            e.CODIGO = c.CODIGO_ESTADO AND ");
                    sb.append("            c.CODIGO_IBGE = r.CODIGO_IBGE ");
                    sb.append("    ),null) AS ESTADO, ");
                    sb.append("    r.CODIGO_IBGE AS MUNICIPIO, ");
                    sb.append("    null AS NASCIMENTO, ");
                    sb.append("    r.CEP, ");
                    sb.append("    null AS CPF, ");
                    sb.append("    r.NUMERO, ");
                    sb.append("    r.COMPLEMENTO, ");
                    sb.append("    r.NOME_BAIRRO AS BAIRRO, ");
                    sb.append("    r.ENDERECO AS LOGRADOURO, ");
                    sb.append("    null AS FANTASIA, ");
                    sb.append("    null as IM ");
                    sb.append("FROM ");
                    sb.append("    FORNECEDORES r ");
                },
                (StringBuilder sb) -> {
                    sb.append("SELECT ");
                    sb.append("    lt.TELEFONE AS NUMERO ");
                    sb.append("FROM ");
                    sb.append("    LISTA_TELEFONICA lt ");
                    sb.append("WHERE ");
                    sb.append("    lt.TIPO_TELEFONE = '02' AND ");
                    sb.append("    lt.CODIGO = ? ");
                    sb.append("ORDER BY ");
                    sb.append("    lt.SEQUENCIA ASC ");
                },
                importOnlyWithCnpjOrCpf,
                importOnlyWithMunicipio,
                forceFieldNascimentoToNullOnCnpj
        );
    }

}
