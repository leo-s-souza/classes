/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras;

import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;

/**
 * Api para montar o texto a ser impresso já com as diretrizes/carateristícas da
 * impressora em questão
 *
 * @author Fernando Batels<luisfbatels@gmail.com>
 */
public class CSPImpressorasTextBuilder {

    private final StringBuilder sb;
    private final int maxLine;

    public CSPImpressorasTextBuilder(int maxLine) {
        this.sb = new StringBuilder();
        this.maxLine = maxLine;
    }

    /**
     * Adiciona conteúdo e quebra a linha
     *
     * @param texto String...
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addLinha(Object... texto) {
        StringBuilder append = new StringBuilder();

        for (Object s : texto) {
            append.append(s.toString());
        }

        String line = append.toString();
        if (line.length() > maxLine) {
            int pos;

            do {
                if (line.charAt(maxLine - 1) != ' ' && line.charAt(maxLine) != ' ') {
                    pos = line.substring(0, maxLine).lastIndexOf(" ");
                    if (pos == -1 || pos == 0) {
                        pos = maxLine;
                    }
                } else {
                    pos = maxLine;
                }

                this.sb.append(line.substring(0, pos).trim());
                this.quebraLinha();
                line = line.substring(pos, line.length());
            } while (line.length() > maxLine);
        }

        this.sb.append(line.trim());
        this.quebraLinha();

        return this;
    }

    /**
     * Adiciona conteúdo e quebra a linha
     *
     * @param texto String...
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addLinhaSimples(Object... texto) {

        for (Object s : texto) {
            this.sb.append(s.toString());
        }

        this.quebraLinha();

        return this;
    }

    /**
     * Efetua a quebra de linha
     *
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder quebraPag() {
        this.sb.append("\f");

        return this;
    }

    /**
     * Efetua a quebra de página
     *
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder quebraLinha() {
        this.sb.append("\n");

        return this;
    }

    /**
     * Adiciona um tab.
     *
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addTab() {
        //ECF não aceita \t nem (char)9, vai na mão mesmo.
        this.sb.append("    ");

        return this;
    }

    /**
     * Adiciona uma linha no formato 'Total .......... R$ 10,0'
     *
     * @param texto String
     * @param divisor String
     * @param value double
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addLinhaPreco(String texto, String divisor, double value) {
        this.addLinhaComposta(texto, divisor, CSPUtilidadesLang.currencyRealFormat(value));

        return this;
    }

    /**
     * Adiciona uma linha no formato 'CONTEÚDO .......... CONTEÚDO'
     *
     * @param texto String
     * @param divisor String
     * @param value String
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addLinhaComposta(String texto, String divisor, String value) {
        texto = texto.trim();
        value = value.trim();

        int jaOcupado = texto.replaceAll("\\<.*?\\>", "").length() + value.replaceAll("\\<.*?\\>", "").length() + 2;

        String pad = " " + CSPUtilidadesLang.pad("", maxLine - jaOcupado, divisor) + " ";

        this.addLinhaSimples(texto, pad, value);

        return this;
    }

    /**
     * Adiciona uma linha para título.
     *
     * @param texto String
     * @param divisor String
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addLinhaTitulo(String texto, String divisor) {

        String ln = CSPUtilidadesLang.pad("", maxLine, divisor);

        texto = CSPUtilidadesLang.pad(texto, ((maxLine - texto.length()) / 2) + texto.length(), " ", true);

        this.addLinhaSimples(ln, "\n", texto, "\n", ln);

        return this;
    }

    /**
     * Adiciona uma linha centralizada.
     *
     * @param texto String
     * @param divisor String
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addLinhaCentralizada(String texto, String divisor) {
        int temp = (maxLine - (texto.length() + 2)) / 2;

        texto = CSPUtilidadesLang.pad("", temp, divisor) + " " + texto + " ";

        this.addLinhaSimples(texto, CSPUtilidadesLang.pad("", maxLine - texto.length(), divisor));

        return this;
    }

    /**
     * Adiciona uma tag. Basta informar a mesma que o método irá concatenar os
     * '\<' e '\>'
     *
     * @param tag String
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addTag(String tag) {
        this.sb.append("<");
        this.sb.append(tag);
        this.sb.append(">");

        return this;
    }

    /**
     * Adiciona uma linha divisória seguida de um a quebra
     *
     * @param divisor String
     * @return CSPImpressorasTextBuilder
     */
    public CSPImpressorasTextBuilder addDivisor(String divisor) {
        this.sb.append(CSPUtilidadesLang.pad("", maxLine, divisor));
        this.quebraLinha();

        return this;
    }

    /**
     * Adiciona conteúdo.
     *
     * @param text Texto a ser adicionado.
     */
    public void append(String text) {
        sb.append(text);
    }

    /**
     * Retorna o resultado do texto gerado retirando os caracteres especiais.
     *
     * @return String
     */
    public String getOutput() {
        return CSPUtilidadesLang.superNormalizeString(this.sb.toString(), "|<>\n\f;:.,+-/*$&()=");
    }

    /**
     * Retorna o resultado do texto gerado.
     *
     * @return String
     */
    @Override
    public String toString() {
        return CSPUtilidadesLang.removerCaracteresEspeciais(this.sb.toString());
    }
}
