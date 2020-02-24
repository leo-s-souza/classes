/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

import java.util.LinkedHashSet;

/**
 * Classe para a manipulação da lista de alíquotas retornadas da
 * impressora.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPListaAliquotas {

    /**
     * Lista que guarda as alíquotas.
     */
    private final LinkedHashSet<CSPAliquotas> lista = new LinkedHashSet<>();

    /**
     * Retorna a lista que guarda as alíquotas.
     *
     * @return LinkedHashSet(CSPFormaPgto).
     */
    public LinkedHashSet<CSPAliquotas> getLista() {
        return lista;
    }

    /**
     * Adiciona uma alíquota na lista.
     *
     * @param id Número da alíquota.
     * @param percentual Percentual da alíquota.
     * @param valorAcumulado Valor acumulado (2 casas decimais).
     * @param tipo Tipo da alíquota. 0 = ICMS, 1= ISSQN.
     * @param codigo Código do PAF.
     */
    public void addAliquota(int id, double percentual, double valorAcumulado, int tipo, String codigo) {
        getLista().add(new CSPAliquotas(id, percentual, valorAcumulado, tipo, codigo));
    }

    /**
     * Retorna a alíquota de percentual igual ao informado.
     *
     * @param percentual
     * @return CSPFormaPgto
     */
    public CSPAliquotas getAliquota(double percentual) {
        for (CSPAliquotas pgto : getLista()) {
            if (pgto.getPercentual() == percentual) {
                return pgto;
            }
        }

        return null;
    }

    /**
     * Retorna a alíquota de id igual ao informado.
     *
     * @param id
     * @return CSPFormaPgto
     */
    public CSPAliquotas getAliquota(int id) {

        for (CSPAliquotas pgto : getLista()) {
            if (pgto.getId() == id) {
                return pgto;
            }
        }

        return null;
    }
}
