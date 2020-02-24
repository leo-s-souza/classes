/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

/**
 * Classe da forma de pagamento.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPFormaPgto {

    /**
     * Número da forma de pagamento.
     */
    private final int id;

    /**
     * Descrição da forma de pagamento.
     */
    private final String nome;

    /**
     * Valor acumulado (2 casas decimais).
     */
    private final double valorAcumulado;

    /**
     * Valor recebido no último cupom (2 casas decimais).
     */
    private final double valorUltimoCupom;

    /**
     * Valor indicando se a forma foi usada para a emissão do cupom não fiscal
     * vinculado (true usado, false não usado).
     */
    private final boolean emiteCupomNFiscalVinculado;

    /**
     * Contrutor.
     *
     * @param id Número da forma de pagamento.
     * @param nome Descrição da forma de pagamento.
     * @param valorAcumulado Valor acumulado (2 casas decimais).
     * @param valorUltimoCupom Valor recebido no último cupom (2 casas
     * decimais).
     * @param emiteCupomNFiscalVinculado Valor indicando se a forma foi usada
     * para a emissão do cupom não fiscal vinculado (true usado, false não
     * usado).
     */
    public CSPFormaPgto(int id, String nome, double valorAcumulado, double valorUltimoCupom, boolean emiteCupomNFiscalVinculado) {
        this.id = id;
        this.nome = nome;
        this.valorAcumulado = valorAcumulado;
        this.valorUltimoCupom = valorUltimoCupom;
        this.emiteCupomNFiscalVinculado = emiteCupomNFiscalVinculado;
    }

    /**
     * Retorna o número da forma de pagamento.
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna a descrição da forma de pagamento.
     *
     * @return String
     */
    public String getNome() {
        return nome;
    }

    /**
     * Retorna o valor acumulado (2 casas decimais).
     *
     * @return double
     */
    public double getValorAcumulado() {
        return valorAcumulado;
    }

    /**
     * Retorna o valor recebido no último cupom (2 casas decimais).
     *
     * @return double
     */
    public double getValorUltimoCupom() {
        return valorUltimoCupom;
    }

    /**
     * Retorna o valor indicando se a forma foi usada para a emissão do cupom
     * não fiscal vinculado (true usado, false não usado).
     *
     * @return boolean
     */
    public boolean isEmiteCupomNFiscalVinculado() {
        return emiteCupomNFiscalVinculado;
    }
}
