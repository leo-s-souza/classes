/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

import java.util.LinkedHashSet;

/**
 * Classe para a manipulação da lista de Relatórios Gerenciais retornados da
 * impressora.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPListaRelatorioGerencial {

    /**
     * Lista que guarda os Relatórios Gerenciais.
     */
    private final LinkedHashSet<CSPRelatorioGerencial> lista = new LinkedHashSet<>();

    /**
     * Retorna a lista que guarda os Relatórios Gerenciais.
     *
     * @return LinkedHashSet(CSPRelatorioGerencial).
     */
    public LinkedHashSet<CSPRelatorioGerencial> getLista() {
        return lista;
    }

    /**
     * Adiciona um Relatório Gerencial na lista.
     *
     * @param id Número da forma de pagamento.
     * @param cer Contador específico de relatórios gerenciais (CER).
     * @param nome Descrição do Relatório Gerencial.
     */
    public void addRelatorioGerencial(int id, int cer, String nome) {
        getLista().add(new CSPRelatorioGerencial(id, cer, nome));
    }

    /**
     * Retorna a Relatório Gerencial de ID igual ao informado.
     *
     * @param id
     * @return CSPFormaPgto
     */
    public CSPRelatorioGerencial getRelatorioGerencial(int id) {

        for (CSPRelatorioGerencial pgto : getLista()) {
            if (pgto.getId() == id) {
                return pgto;
            }
        }

        return null;
    }
}
