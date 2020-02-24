/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras;

import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import gnu.io.CommPortIdentifier;
import java.util.Enumeration;
import java.util.StringJoiner;

/**
 * Classe base para impressoras Bematech.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 * @date 17/11/2016
 */
public abstract class CSPImpressorasBase {

    /*
     * -------------------------------------------------------------------------
     * Variáveis.
     * -------------------------------------------------------------------------
     */
    /**
     * Responsável por guardar o retorno da função da impressora. O retorno dita
     * o que aconteceu na função.
     */
    private int retorno;

    /*
     * -------------------------------------------------------------------------
     * Métodos abstratos.
     * -------------------------------------------------------------------------
     */
    /**
     * Verifica se a impressora está ligada ou conectada no computador.
     *
     * @return Retorno
     */
    public abstract Retorno getImpressoraLigada();

    /**
     * Abre a porta serial para comunicação entre a impressora e o computador.
     *
     * @return Retorno
     */
    public abstract Retorno openSerial();

    /**
     * Fecha a porta serial de comunicação entre a impressora e o computador.
     *
     * @return Retorno
     */
    public abstract Retorno closeSerial();

    /**
     * Método onde cada impressora irá configurar a inicialização da impressora.
     *
     * @param conf
     * @return
     * @throws java.lang.Exception
     */
    public abstract Retorno startImpresora(Confs conf) throws Exception;

    /*
     * -------------------------------------------------------------------------
     * Métodos gerais.
     * -------------------------------------------------------------------------
     */
    /**
     * Lista as portas seriais do sistema.
     *
     * @return String[]
     * @throws Exception
     */
    public final String[] getSeriaisDisponiveis() throws Exception {
        Enumeration listaDePortas = CommPortIdentifier.getPortIdentifiers();
        StringJoiner portas = new StringJoiner(";");

        while (listaDePortas.hasMoreElements()) {
            CommPortIdentifier ips = (CommPortIdentifier) listaDePortas.nextElement();
            if (ips.getName().toUpperCase().contains("COM")) {
                portas.add(ips.getName());
            }
        }

        return portas.toString().split(";");
    }

    /**
     * Método para a inicialização da impressora.
     *
     * @return
     * @throws java.lang.Exception
     */
    public Retorno start() throws Exception {
        this.loadLibCommunicationPort();
        return this.startImpresora(new Confs());
    }

    /**
     * Carrega a DLL para a comunicação com as postas (COM. USB etc.).
     */
    public void loadLibCommunicationPort() {
        if (CSPUtilidadesSO.getSO() == CSPUtilidadesSO.SO.SO_WINDOWS) {
            try {
                System.loadLibrary("rxtxParallel");
                System.loadLibrary("rxtxSerial");
            } catch (UnsatisfiedLinkError ex) {
                try {
                    throw new Exception(ex.getMessage());
                } catch (Exception e) {
                    CSPException.register(e, 166);
                }
            }
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Getters e Setters.
     * -------------------------------------------------------------------------
     */
    /**
     * Pega o valor que guarda o retorno da função da impressora.
     *
     * @return Object
     * @see #retorno
     */
    public int getRetornoBruto() {
        return retorno;
    }

    /**
     * Seta o valor da variável que guarda o retorno da função da impressora.
     *
     * @param retorno
     * @see #retorno
     */
    public void setRetornoBruto(int retorno) {
        this.retorno = retorno;
    }

    /**
     * -------------------------------------------------------------------------
     * Classe para a configuração das impressoras fiscais.
     * -------------------------------------------------------------------------
     */
    protected class Confs {

        public void loadLib(String library) {
            try {
                System.loadLibrary(library);
            } catch (UnsatisfiedLinkError ex) {
                try {
                    throw new Exception(ex.getMessage());
                } catch (Exception e) {
                    CSPException.register(e, 166);
                }
            }
        }
    }

    /**
     * Enum referente ao retorno da ECF.
     */
    public enum Retorno {

        /**
         * Sucesso. A função foi executada sem problemas.
         */
        OK(-1),
        /**
         * Alíquota não programada na ECF.
         */
        ERRO_ALIQUOTA(221),
        /**
         * ECF bloqueada por Redução Z.
         */
        BLOQUEIO_POR_RZ(224),
        /**
         * ECF bloqueada por Redução Z.
         */
        RZ_JA_EMITIDA(225),
        /**
         * PDV Bloqueado.
         */
        PDV_BLOQUEADO(230),
        /**
         * Erro de comunicação. Pode acontecer por erro de comunicação na porta
         * COM ou ou erro na própria impressora.
         */
        ERRO_DE_COMUNICACAO(300),
        /**
         * Erro de execução da função.
         */
        ERRO_DE_EXECUCAO(301),
        /**
         * Parâmetro passado na função é inválido.
         */
        PARAMETRO_INVALIDO(302),
        /**
         * O arquivo de inicialização não foi encontrado no diretório de
         * sistema.
         */
        CONFIGURACOES_NAO_ENCONTRADO(303),
        /**
         * Erro ao abrir porta de comunicação, seja USB ou porta COM.
         */
        ERRO_ABRIR_PORTA(304),
        /**
         * Impressora desligada ou cabo de comunicação desconectado.
         */
        ECF_DESLIGADA_OU_CABO_DESCONECTADO(305),
        /**
         * Erro ao gravar o arquivo STATUS.TXT.
         */
        ERRO_STATUS_TXT(306),
        /**
         * Status padrão da impressora está incorreto.
         */
        STATUS_IMPRESSORA_INCORRETO(307),
        /**
         * Impressora sem papel/Pouco papel.
         */
        FIM_POUCO_PAPEL(308),
        /**
         * Erro no relógio.
         */
        ERRO_RELOGIO(309),
        /**
         * Impressora em erro.
         */
        IMPRESSORA_EM_ERRO(310),
        /**
         * Comando inexistente.
         */
        COMANDO_INEXISTENTE(311),
        /**
         * Cumpom fiscal aberto.
         */
        CUPOM_ABERTO(312),
        /**
         * Memória fical lotada.
         */
        MFISCAL_LOTADA(313),
        /**
         * Cancelamento não permitido.
         */
        CANCELAMENTO_N_PERMITIDO(314),
        /**
         * Comando não executado.
         */
        COMANDO_N_EXECUTADO(315),
        /**
         * Comando inválido.
         */
        COMANDO_INVALIDO(316),
        /**
         * Erro desconhecido.
         */
        ERRO_DESCONHECIDO(317),
        /**
         * Cupom fiscal fechado.
         */
        CUPOM_FECHADO(322),
        /**
         * Ecf ocupado.
         */
        ECF_OCUPADO(323),
        /**
         * Impressora com cabeça levantada.
         */
        TAMPA_LEVANTADA(324),
        /**
         * Acréscimo ou desconto maior que o total do cupom fiscal.
         */
        ACRES_DESC_MAIOR_TOTAL_VENDA(327),
        /**
         * Forma de pagamento não programada.
         */
        FORMA_PAGAMENTO_NAO_PROGRAMADA(331),
        /**
         * Cancelamento não imediatamente após.
         */
        CANC_N_IMEDIATAMENTE_APOS(337),
        /**
         * Cancelamento já efetuado.
         */
        CANC_JA_EFETUADO(338),
        /**
         * Contador de redução z inicial inválido.
         */
        CONTADOR_RZ_INICIAL_INVALIDO(365),
        /**
         * Contador de redução z final inválido.
         */
        CONTADOR_RZ_FINAL_INVALIDO(366),
        /**
         * Erro de gravação na mf.
         */
        ERRO_GRAVACAO_MF(382),
        /**
         * Erro de gravação na mfd.
         */
        ERRO_GRAVACAO_MFD(383),
        /**
         * Aguardando acerto de relógio.
         */
        AGUARDANDO_ACERTO_RELOGIO(454);

        /**
         * Guarda o valor do Enum para as mensagens de erro.
         */
        private final int valor;

        /**
         * Retorna o valor do Enum referente as mensagens de erro.
         *
         * @return int
         * @see #valor
         */
        public int getValor() {
            return this.valor;
        }

        /**
         * Construtor do Enum.
         *
         * @param valor
         */
        private Retorno(int valor) {
            this.valor = valor;
        }
    }
}
