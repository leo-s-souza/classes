/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;

/**
 * Classe de funções de impressora não fical epson.
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
public class CSPImpressoraNaoFiscalSOLinux extends CSPImpressorasNaoFiscaisBase {

    /*
     * -------------------------------------------------------------------------
     * Métodos sobrepostos da classe pai.
     * -------------------------------------------------------------------------
     */
    /**
     * Classe para a inicialização da impressora carregando a library.
     *
     * @param conf
     * @return 
     */
    @Override
    public Retorno startImpresora(Confs conf) {
        CSPLog.info(getClass(), "startImpresora");
        
        return Retorno.OK;
    }

    /**
     * Abre a porta serial para comunicação entre a impressora e o computador.
     *
     * @return Retorno
     */
    @Override
    public Retorno openSerial() {

        CSPLog.info(getClass(), "openSerial");

        setRetornoBruto(1);

        return Retorno.OK;
    }

    /**
     * Fecha a porta serial de comunicação entre a impressora e o computador.
     *
     * @return
     */
    @Override
    public Retorno closeSerial() {

        CSPLog.info(getClass(), "closeSerial");

        setRetornoBruto(1);

        return Retorno.OK;
    }

    /**
     * Esta função aciona a guilhotina, contando o papel em modo parcial ou
     * total.
     *
     * @param modo Definir o corte total(true) ou parcial(false).
     * @return Retorno
     */
    @Override
    public Retorno acionarGuilhotina(boolean modo) {

        CSPLog.info(getClass(), "acionarGuilhotina:" + modo);

        setRetornoBruto(1);

        return Retorno.OK;
    }

    /**
     * Esta função é utilizada na impressão de textos, enviando um conjunto com
     * várias linhas.
     *
     * @param texto
     * @return Retorno
     */
    @Override
    public Retorno imprimeTexto(String texto) {

        texto = CSPUtilidadesLang.superNormalizeString(texto, "<>\n;:.,+-/*$&()=");

        CSPLog.info(getClass(), "imprimeTexto:\n" + texto);

        setRetornoBruto(1);

        return Retorno.OK;
    }

    /**
     * Esta função faz a impressão do código de barras CODABAR.
     *
     * @param codigo De barras.
     * @return Retorno
     */
    @Override
    public Retorno imprimeCodigoBarrasCODABAR(String codigo) {

        CSPLog.info(getClass(), "imprimeCodigoBarrasCODABAR:\n" + codigo);

        setRetornoBruto(1);

        return Retorno.OK;
    }

    @Override
    public Retorno getImpressoraLigada() {
        return Retorno.OK;
    }

    @Override
    public int getTamanhoMaxLinha() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isImpressoraValida() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Retorno imprimeTextoFormatado(String bufTras, int tipoLetra, int italic, int sublin, int expand, int enfat) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
