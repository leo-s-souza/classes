/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

/**
 * Classe para os Relatórios Gerenciais da impressora.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPRelatorioGerencial {

    /**
     * Número/ID do Relatório Gerencial.
     */
    private final int id;

    /**
     * Número de vezes que o relatório foi utilizado. Contador específico de
     * relatórios gerenciais (CER).
     */
    private final int cer;

    /**
     * Descrição do Relatório Gerencial.
     */
    private final String nome;

    /**
     * Construtor.
     *
     * @param id Número da forma de pagamento.
     * @param cer Contador específico de relatórios gerenciais (CER).
     * @param nome Descrição do Relatório Gerencial.
     */
    public CSPRelatorioGerencial(int id, int cer, String nome) {
        this.id = id;
        this.cer = cer;
        this.nome = nome;
    }

    /**
     * Retorna o número/ID do Relatório Gerencial.
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Retorna o contador específico de relatórios gerenciais (CER).
     *
     * @return int
     */
    public int getCer() {
        return cer;
    }

    /**
     * Retorna a descrição do Relatório Gerencial.
     *
     * @return String
     */
    public String getNome() {
        return nome;
    }
}
