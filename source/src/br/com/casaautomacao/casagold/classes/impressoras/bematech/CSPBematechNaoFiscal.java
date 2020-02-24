/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.bematech;

import bemajava.BematechNaoFiscal;
import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasNaoFiscaisBase;
import com.sun.jna.Native;

/**
 * Classe de funções de impressora não fical bematech.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPBematechNaoFiscal extends CSPImpressorasNaoFiscaisBase {

    /*
     * -------------------------------------------------------------------------
     * Variáveis.
     * -------------------------------------------------------------------------
     */
    /**
     * Guarda a instancia da dll para a chamada de funções.
     */
    private BematechNaoFiscal dllInstance;

    /**
     * Guarda o modelo da impressora usado para as funções da DLL.
     */
    private int modeloImpressora;

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
        this.dllInstance = (BematechNaoFiscal) Native.loadLibrary("mp2064", BematechNaoFiscal.class);
        return setPorta();
    }

    /**
     * Abre a porta serial para comunicação entre a impressora e o computador.
     *
     * @return Retorno
     */
    @Override
    public Retorno openSerial() {
        Retorno rtrn;
        setRetornoBruto(getDllInstance().IniciaPorta(getPorta()));

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
        setRetornoBruto(getDllInstance().FechaPorta());

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            default:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
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

        if (modo) {
            setRetornoBruto(getDllInstance().AcionaGuilhotina(1));
        } else {
            setRetornoBruto(getDllInstance().AcionaGuilhotina(0));
        }

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
        setRetornoBruto(getDllInstance().BematechTX(texto));

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            default:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
        }

        return rtrn;
    }

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
    @Override
    public Retorno imprimeTextoFormatado(String bufTras, int tipoLetra, int italic, int sublin, int expand, int enfat) {
                Retorno rtrn;
        setRetornoBruto(getDllInstance().FormataTX(bufTras, tipoLetra, italic, sublin, expand, enfat));

        switch ((int) getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            default:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
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
        setRetornoBruto(getDllInstance().ImprimeCodigoBarrasCODABAR(codigo));

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
     * @return BematechNaoFiscal
     * @see #dllInstance
     */
    public BematechNaoFiscal getDllInstance() {
        return dllInstance;
    }

    /**
     * Retorna o modelo da impressora.
     *
     * @return int
     * @see #modeloImpressora
     */
    public int getModeloImpressora() {
        return modeloImpressora;
    }

    /**
     * Seta o modelo da impressora.
     *
     * @param modeloImpressora
     * @see #modeloImpressora
     */
    public void setModeloImpressora(int modeloImpressora) {
        this.modeloImpressora = modeloImpressora;
    }

    /*
     * -------------------------------------------------------------------------
     * Métodos gerais.
     * -------------------------------------------------------------------------
     */
    /**
     * Seta dinamicamente procurando a porta de comunicação em que está a
     * impressora.
     * @return 
     */
    public Retorno setPorta() {
        try {
            for (String serial : getSeriaisDisponiveis()) {
                if (getDllInstance().IniciaPorta(serial) == 1) {
                    this.setPorta(serial);
                    getDllInstance().FechaPorta();
                }
            }

            if (this.getPorta() == null) {
                return Retorno.ERRO_ABRIR_PORTA; 
            }
        } catch (Exception ex) {
            CSPLog.error(ex.getMessage());
            return Retorno.ERRO_ABRIR_PORTA; 
        }
        
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

}
