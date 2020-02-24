/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.epson;

import Epson.InterfaceEpsonNF.*;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasNaoFiscaisBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;

/**
 * Classe de funções de impressora não fical epson.
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
public class CSPEpsonNaoFiscal extends CSPImpressorasNaoFiscaisBase {

    /*
     * -------------------------------------------------------------------------
     * Variáveis.
     * -------------------------------------------------------------------------
     */
    /**
     * Guarda a instancia da dll para a chamada de funções.
     */
    private InterfaceEpsonNF epson;


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
        this.epson = new InterfaceEpsonNF();
        
        return Retorno.OK;
    }

    /**
     * Abre a porta serial para comunicação entre a impressora e o computador.
     *
     * @return Retorno
     */
    @Override
    public Retorno openSerial() {
        Retorno rtrn;
        setRetornoBruto(this.epson.IniciaPorta(this.getPorta()));

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            default:
                rtrn = Retorno.ERRO_ABRIR_PORTA;
                break;
        }

        if (rtrn != Retorno.OK) {
            try {
                throw new Exception();
            } catch (Exception e) {
                CSPException.register(e, rtrn.getValor());
            }
        }

        return rtrn;
    }

    /**
     * Fecha a porta serial de comunicação entre a impressora e o computador.
     *
     * @return
     */
    @Override
    public Retorno closeSerial() {
        Retorno rtrn;
        setRetornoBruto(getEpson().FechaPorta());

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            default:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
        }

        if (rtrn != Retorno.OK) {
            try {
                throw new Exception();
            } catch (Exception e) {
                CSPException.register(e, rtrn.getValor());
            }
        }

        return rtrn;
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
        Retorno rtrn;

        setRetornoBruto(getEpson().AcionaGuilhotina(1));

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            case 0:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
            default:
                rtrn = Retorno.PARAMETRO_INVALIDO;
                break;
        }

        if (rtrn != Retorno.OK) {
            try {
                throw new Exception();
            } catch (Exception e) {
                CSPException.register(e, rtrn.getValor());
            }
        }

        return rtrn;
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
        Retorno rtrn;

        texto = CSPUtilidadesLang.superNormalizeString(texto, "<>\n;:.,+-/*$&()=");

        setRetornoBruto(getEpson().ImprimeTextoTag(texto));

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            default:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
        }

        if (rtrn != Retorno.OK) {
            try {
                throw new Exception();
            } catch (Exception e) {
                CSPException.register(e, rtrn.getValor());
            }
        }

        return rtrn;
    }

    /**
     * Esta função faz a impressão do código de barras CODABAR.
     *
     * @param codigo De barras.
     * @return Retorno
     */
    @Override
    public Retorno imprimeCodigoBarrasCODABAR(String codigo) {
        Retorno rtrn;
        setRetornoBruto(getEpson().ImprimeCodigoBarrasCODABAR(codigo));

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            case 0:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
            case -1:
                rtrn = Retorno.ERRO_DE_EXECUCAO;
                break;
            default:
                rtrn = Retorno.PARAMETRO_INVALIDO;
                break;
        }

        if (rtrn != Retorno.OK) {
            try {
                throw new Exception();
            } catch (Exception e) {
                CSPException.register(e, rtrn.getValor());
            }
        }

        return rtrn;
    }

    @Override
    public Retorno getImpressoraLigada() {
        return Retorno.OK;
    }

    /*
     * -------------------------------------------------------------------------
     * Getters e setters.
     * -------------------------------------------------------------------------
     */
    /**
     * Retorna a instancia da dll para a chamada de funções.
     *
     * @return InterfaceEpsonNF
     * @see #epson
     */
    private InterfaceEpsonNF getEpson() {
        return epson;
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
