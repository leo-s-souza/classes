package br.com.casaautomacao.casagold.classes.impressoras;

/**
 *
 * @author cautomacao
 */
public abstract class CSPImpressorasNaoFiscaisBase extends CSPImpressorasBase {

    /**
     * Guarda a porta de comunicação da impressora.
     */
    private String porta;

    /*
     * -------------------------------------------------------------------------
     * Métodos abstratos.
     * -------------------------------------------------------------------------
     */
    /**
     * Esta função é utilizada na impressão de textos, enviando um conjunto com
     * várias linhas.
     *
     * @param texto
     * @return Retorno
     */
    public abstract Retorno imprimeTexto(String texto);

    /**
     * Esta função tem por objetivo enviar textos para a impressora, com
     * formatações informadas pelos parâmetros.
     *
     * @param bufTras Texto a ser impresso
     * @param tipoLetra 1 = comprimido, 2 = normal, 3 = elite
     * @param italic 1 = ativa o modo itálico, 0 = desativa o modo itálico
     * @param sublin 1 = ativa o modo sublinhado, 0 = desativa o modo sublinhado
     * @param expand 1 = ativa o modo expandido, 0 = desativa o modo expandido
     * @param enfat 1 = ativa o modo enfatizado, 0 = desativa o modo enfatizado
     * @return Retorno
     */
    public abstract Retorno imprimeTextoFormatado(String bufTras, int tipoLetra, int italic, int sublin, int expand, int enfat);

    /**
     * Esta função aciona a guilhotina, contando o papel em modo parcial ou
     * total.
     *
     * @param modo Definir o corte total(true) ou parcial(false).
     * @return Retorno
     */
    public abstract Retorno acionarGuilhotina(boolean modo);

    /**
     * Esta função faz a impressão do código de barras CODABAR.
     *
     * @param codigo
     * @return Retorno
     */
    public abstract Retorno imprimeCodigoBarrasCODABAR(String codigo);

    /**
     * Retorna o tamanho máximo que a linha terá na impressora
     *
     * @return int
     */
    public abstract int getTamanhoMaxLinha();

    /**
     * Usado em validações de rotinas determina se a impressora é válida
     *
     * @return
     */
    public abstract boolean isImpressoraValida();

    /**
     * Seta a porta de comunicação da impressora.
     *
     * @param porta String
     */
    public void setPorta(String porta) {
        this.porta = porta;
    }

    /**
     * Retorna a porta de comunicação da impressora.
     *
     * @return
     */
    public String getPorta() {
        return porta;
    }

    public CSPImpressorasTextBuilder buildNewText() {
        return new CSPImpressorasTextBuilder(this.getTamanhoMaxLinha());
    }

    /**
     * Esta função é utilizada na impressão de textos, enviando um conjunto com
     * várias linhas.
     *
     * @param texto
     * @return Retorno
     */
    public Retorno imprimeTexto(CSPImpressorasTextBuilder texto) {
        return imprimeTexto(texto.getOutput());
    }

}
