/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

import java.util.LinkedHashSet;

/**
 * Classe para a manipulação da lista de formas de pagamento retornadas da
 * impressora.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPListaFormasPgto {

    /**
     * Lista que guarda as formas de Pagamento.
     */
    private final LinkedHashSet<CSPFormaPgto> lista = new LinkedHashSet<>();

    /**
     * Retorna a lista que guarda as formas de Pagamento.
     *
     * @return LinkedHashSet(CSPFormaPgto).
     */
    public LinkedHashSet<CSPFormaPgto> getLista() {
        return lista;
    }

    /**
     * Adiciona uma forma de pagamento na lista.
     *
     * @param id Número da forma de pagamento.
     * @param nome Descrição da forma de pagamento.
     * @param valorAcumulado Valor acumulado (2 casas decimais).
     * @param valorUltimoCupom Valor recebido no último cupom (2 casas
     * decimais).
     * @param emiteCupomNFicalVinculado Valor indicando se a forma foi usada
     * para a emissão do cupom não fiscal vinculado (true usado, false não
     * usado).
     */
    public void addFormaPgto(int id, String nome, double valorAcumulado, double valorUltimoCupom, boolean emiteCupomNFicalVinculado) {
        getLista().add(new CSPFormaPgto(id, nome, valorAcumulado, valorUltimoCupom, emiteCupomNFicalVinculado));
    }

    /**
     * Retorna a forma de pagamento de nome igual ao informado.
     *
     * @param descricaoForma
     * @return CSPFormaPgto
     */
    public CSPFormaPgto getFormaPgto(String descricaoForma) {
        for (CSPFormaPgto pgto : getLista()) {
            if (pgto.getNome().equals(descricaoForma)) {
                return pgto;
            }
        }

        return null;
    }

    /**
     * Retorna a forma de pagamento de nome igual ao informado ignorando letras
     * maiúsculas/minúsculas.
     *
     * @param descricaoForma
     * @return CSPFormaPgto
     */
    public CSPFormaPgto getFormaPgtoIgnoreCase(String descricaoForma) {
        for (CSPFormaPgto pgto : getLista()) {
            if (pgto.getNome().equalsIgnoreCase(descricaoForma)) {
                return pgto;
            }
        }

        return null;
    }

    /**
     * Retorna a forma de pagamento de id igual ao informado.
     *
     * @param id
     * @return CSPFormaPgto
     */
    public CSPFormaPgto getFormaPgto(int id) {

        for (CSPFormaPgto pgto : getLista()) {
            if (pgto.getId() == id) {
                return pgto;
            }
        }

        return null;
    }
}
