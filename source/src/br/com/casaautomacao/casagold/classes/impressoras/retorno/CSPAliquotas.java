/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

/**
 * Classe das alíquotas do ECF.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPAliquotas {

    /**
     * Número da alíquota.
     */
    private final int id;

    /**
     * Percentual da alíquota.
     */
    private final double percentual;

    /**
     * Valor acumulado (2 casas decimais).
     */
    private final double valorAcumulado;

    /**
     * Tipo da alíquota. 0 = ICMS, 1= ISSQN.
     */
    private int tipo;

    /**
     * Código do PAF.
     */
    private String codigo;

    /**
     * Contrutor.
     *
     * @param id Número da alíquota.
     * @param percentual Percentual da alíquota.
     * @param valorAcumulado Valor acumulado (2 casas decimais).
     * @param tipo Tipo da alíquota. 0 = ICMS, 1= ISSQN.
     * @param codigo Código do PAF.
     */
    public CSPAliquotas(int id, double percentual, double valorAcumulado, int tipo, String codigo) {
        this.id = id;
        this.percentual = percentual;
        this.valorAcumulado = valorAcumulado;
        this.tipo = tipo;
        this.codigo = codigo;
    }

    /**
     * Retorna o número da alíquota.
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna o valor percentual da alíquota (2 casas decimais).
     *
     * @return
     */
    public double getPercentual() {
        return percentual;
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
     * Retorna o código do PAF.
     *
     * @return String
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * Retorna o tipo da alíquota. 0 = ICMS, 1= ISSQN.
     *
     * @return int
     */
    public int getTipo() {
        return tipo;
    }

    /**
     * Seta o tipo da alíquota. 0 = ICMS, 1= ISSQN.
     *
     * @param tipo
     */
    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    /**
     * Sets o código do PAF.
     *
     * @param codigo
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
