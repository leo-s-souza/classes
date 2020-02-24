/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.bematech;

import bemajava.BemaInteger;
import bemajava.BemaString;
import bemajava.Bematech;
import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPDataRZ;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasFiscaisBase;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasTextBuilder;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPAliquotas;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaAliquotas;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaFormasPgto;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaRelatorioGerencial;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPObjectReturnEcf;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.pad;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.ini4j.Wini;

/**
 * Classe de funções da impressora fical Bematech.
 *
 * Para os status da impressora fiscal bematech diferente de 6,0,0 (ACK, ST1 e
 * ST2) temos as situações:
 * <p>
 * - "ACK" indica que o comando enviado à impressora foi recebido com sucesso e
 * que irá processá-lo. O seu valor é 6, tanto decimal quanto hexadecimal.
 * <p>
 * - "NACK" indica que o comando enviado à impressora não foi recebido com
 * sucesso, que seu protocolo (sequência de bytes do comando) não está correto.
 * O seu valor é 21 em decimal ou 15 em hexadecimal.
 * <p>
 * - "ST1" informa o primeiro quadro de status da impressora, onde cada bit
 * setado possui uma situação, como:
 * <ul>
 * <li> bit 7 - "Fim de Papel" (128 é o valor deste bit).</li>
 * <li> bit 6 - "Pouco Papel" (64 é o valor deste bit). </li>
 * <li> bit 5 - "Erro no Relógio" (32 é o valor deste bit). </li>
 * <li> bit 4 - "Impressora em Erro" (16 é o valor deste bit). </li>
 * <li> bit 3 - "Comando não iniciado com ESC" (8 é o valor deste bit).
 * </li>
 * <li> bit 2 - "Comando Inexistente" (4 é o valor deste bit). </li>
 * <li> bit 1 - "Cupom Aberto" (2 é o valor deste bit). </li>
 * <li> bit 0 - "Número de Parâmetro(s) Inválido(s)" (1 é o valor deste
 * bit).</li>
 * </ul>
 *
 * - "ST2" informa o segundo quadro de status da impressora, onde cada bit
 * setado possui uma situação, como:
 * <ul>
 * <li> bit 7 - "Tipo de Parâmetro de Comando Inválido" (128 é o valor deste
 * bit). </li>
 * <li> bit 6 - "Memória Fiscal Lotada" (64 é o valor deste bit). </li>
 * <li> bit 5 - "Erro na Memória RAM" (32 é o valor deste bit). </li>
 * <li> bit 4 - "Alíquota Não Programada" (16 é o valor deste bit).
 * </li>
 * <li> bit 3 - "Capacidade de Alíquotas Lotada" (8 é o valor deste bit).
 * </li>
 * <li> bit 2 - "Cancelamento Não Permitido" (4 é o valor deste bit).
 * </li>
 * <li> bit 1 - "CNPJ/IE do Proprietário Não Programado" (2 é o valor deste
 * bit). </li>
 * <li> bit 0 - "Comando Não Executado" (1 é o valor deste bit). </li>
 * </ul>
 *
 * - "ST3" informa o terceiro quadro de status da impressora. Disponível apenas
 * nas impressoras do Convênio ICMS 85/01 , este byte define com maior precisão
 * o status da impressora.
 * <ul>
 * <li> 0 - "Comando OK". </li>
 * <li> 1 - "Comando Inválido". </li>
 * <li> 3 - "Número de Parâmetro Inválido". </li>
 * <li> 4 - "Tipo de Parâmetro Inválido". </li>
 * <li> 5 - "Todas as Alíquotas já Programadas". </li>
 * <li> 6 - "Totalizador Não Fiscal já Programado". </li>
 * <li> 7 - "Cupom Fiscal Aberto". </li>
 * <li> 8 - "Cupom Fiscal Fechado". </li>
 * <li> 9 - "ECF Ocupado". </li>
 * <li> 10 - Impressora em Erro". </li>
 * <li> 11 - "Impressora sem Papel". </li>
 * <li> 12 - "Impressora com Cabeça Levantada". </li>
 * <li> 13 - "Impressora OFF LINE". </li>
 * <li> 14 - "Alíquota não Programada". </li>
 * <li> 15 - "Terminador de String Faltando". </li>
 * <li> 16 - "Acréscimo ou Desconto maior que o total do Cupom Fiscal".
 * </li>
 * <li> 17 - "Cupom Fiscal sem Item Vendido". </li>
 * <li> 18 - "Comando não Efetivado". </li>
 * <li> 19 - "Sem espaço para novas Formas de Pagamento". </li>
 * <li> 20 - "Forma de Pagamento não Programada".</li>
 * </ul>
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 * @date 17/11/2016
 */
public final class CSPBematechFiscal extends CSPImpressorasFiscaisBase {

    /**
     * Caminho do arquivo de configuração.
     */
    private final static String PATH_INI = PATH + "/BemaFI64.ini";

    /**
     * -------------------------------------------------------------------------
     * Funções Privadas.
     * -------------------------------------------------------------------------
     */
    /**
     * Analisa os retornos da impressora.
     *
     * @param ack Variável inteira do primeiro byte.
     * @param st1 Variável inteira do segundo byte.
     * @param st2 Variável inteira do terceiro byte.
     * @param st3 Variável inteira do quarto byte
     * @param rtrn Retorno atual da função.
     * @return Retorno
     */
    private Retorno analisaStatus(int ack, int st1, int st2, int st3) {
        Retorno rtrn = Retorno.OK;

        if (ack != 6 || st1 != 0 || st2 != 0 || st3 != 0) {
            rtrn = Retorno.STATUS_IMPRESSORA_INCORRETO;

            if (st3 == 66) {
                rtrn = Retorno.BLOQUEIO_POR_RZ;
            } else if (st3 == 63) {
                rtrn = Retorno.RZ_JA_EMITIDA;
            } else if (st3 == 1) {
                rtrn = Retorno.COMANDO_INVALIDO;
            } else if (st3 == 2) {
                rtrn = Retorno.ERRO_DESCONHECIDO;
            } else if (st3 == 8) {
                rtrn = Retorno.CUPOM_FECHADO;
            } else if (st3 == 9) {
                rtrn = Retorno.ECF_OCUPADO;
            } else if (st3 == 12) {
                rtrn = Retorno.TAMPA_LEVANTADA;
            } else if (st3 == 16) {
                rtrn = Retorno.ACRES_DESC_MAIOR_TOTAL_VENDA;
            } else if (st3 == 20) {
                rtrn = Retorno.FORMA_PAGAMENTO_NAO_PROGRAMADA;
            } else if (st3 == 26) {
                rtrn = Retorno.CANC_N_IMEDIATAMENTE_APOS;
            } else if (st3 == 27) {
                rtrn = Retorno.CANC_JA_EFETUADO;
            } else if (st3 == 54) {
                rtrn = Retorno.CONTADOR_RZ_INICIAL_INVALIDO;
            } else if (st3 == 55) {
                rtrn = Retorno.CONTADOR_RZ_FINAL_INVALIDO;
            } else if (st3 == 73) {
                rtrn = Retorno.ERRO_GRAVACAO_MF;
            } else if (st3 == 74) {
                rtrn = Retorno.ERRO_GRAVACAO_MFD;
            } else if (st3 == 145) {
                rtrn = Retorno.AGUARDANDO_ACERTO_RELOGIO;
            } else if (st3 == 7 || st1 == 2) {
                rtrn = Retorno.CUPOM_ABERTO;
            } else if (st3 == 11 || st1 == 128 || st1 == 64) {
                rtrn = Retorno.FIM_POUCO_PAPEL;
            } else if (st3 == 10 || st1 == 16) {
                rtrn = Retorno.IMPRESSORA_EM_ERRO;
            } else if (st2 == 64) {
                rtrn = Retorno.MFISCAL_LOTADA;
            } else if (st2 == 4) {
                rtrn = Retorno.CANCELAMENTO_N_PERMITIDO;
            } else if (st2 == 1) {
                rtrn = Retorno.COMANDO_N_EXECUTADO;
            } else if (st1 == 4) {
                rtrn = Retorno.COMANDO_INEXISTENTE;
            } else if (st1 == 32) {
                rtrn = Retorno.ERRO_RELOGIO;
            }
        }

        return rtrn;
    }

    /**
     * Faz análise do retorno da impressora, pegando o enum referente ao mesmo.
     *
     * @return Retorno Enum do retorno.
     */
    private Retorno analisaRetorno() {
        Retorno rtrn;
        switch (getRetornoBruto()) {
            case 1:
                rtrn = Retorno.OK;
                break;
            case -1:
                rtrn = Retorno.ERRO_DE_EXECUCAO;
                break;
            case -2:
                rtrn = Retorno.PARAMETRO_INVALIDO;
                break;
            case -3:
                rtrn = Retorno.ERRO_ALIQUOTA;
                break;
            case -4:
                rtrn = Retorno.CONFIGURACOES_NAO_ENCONTRADO;
                break;
            case -5:
                rtrn = Retorno.ERRO_ABRIR_PORTA;
                break;
            case -6:
                rtrn = Retorno.ECF_DESLIGADA_OU_CABO_DESCONECTADO;
                break;
            case -8:
                rtrn = Retorno.ERRO_STATUS_TXT;
                break;
            case -24:
                rtrn = Retorno.FORMA_PAGAMENTO_NAO_PROGRAMADA;
                break;
            case -27:
                rtrn = Retorno.STATUS_IMPRESSORA_INCORRETO;
                break;
            default:
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
                break;
        }

        return rtrn;
    }

    /**
     * Lê o retorno estendido da impressora (ACK, ST1, ST2 e ST3) referente ao
     * último comando enviado.
     * <ul>
     * <li> Essa função deve ser usada após a execução de qualquer outra função
     * da dll para ler o retorno da impressora referente à função executada.
     * Essa função devolve o status da impressora (pouco papel, comando não
     * executado, tipo de parâmetro de CMD inválido etc.).
     * </li>
     * <li> O ST3 só será retornado, caso seja habilita o retorno estendido na
     * impressora.
     * </li>
     * </ul>
     *
     * @return Retorno
     */
    private Retorno statusImpressora() {
        Retorno rtrn;
        BemaInteger iACK = new BemaInteger();
        BemaInteger iST1 = new BemaInteger();
        BemaInteger iST2 = new BemaInteger();
        BemaInteger iST3 = new BemaInteger();

        setRetornoBruto(Bematech.RetornoImpressoraMFD(iACK, iST1, iST2, iST3));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = analisaStatus(iACK.getNumber(), iST1.getNumber(), iST2.getNumber(), iST3.getNumber());
        } else {
            try {
                throw new Exception();
            } catch (Exception e) {
                CSPLog.error(e.getMessage());
            }
        }

        return rtrn;
    }

    /**
     * Retorna a data da última Redução Z.
     *
     * @param data Objeto que guarda a data da RZ impressora.
     * @param hora Objeto que guarda a hora da RZ impressora.
     * @return Date
     * @throws java.text.ParseException
     */
    private Retorno getDataHoraReducao(CSPObjectReturnEcf data, CSPObjectReturnEcf hora) throws Exception {
        Retorno rtrn;

        BemaString bemaData = new BemaString();
        bemaData.buffer = "       ";
        BemaString bemaHora = new BemaString();
        bemaHora.buffer = "       ";
        setRetornoBruto(Bematech.DataHoraReducao(bemaData, bemaHora));

        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();

            if (!bemaData.getBuffer().equals("000000")) {
                DateFormat f = new SimpleDateFormat("ddMMyy");
                DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
                data.setEcfBuffer(f2.format(f.parse(bemaData.getBuffer())));

                f = new SimpleDateFormat("HHmmss");
                f2 = new SimpleDateFormat("HH:mm:ss");
                hora.setEcfBuffer(f2.format(f.parse(bemaHora.getBuffer())));
            }
        }

        return rtrn;
    }

    /**
     * Retorna o valor acumulado em uma determinada forma de pagamento.
     *
     * @param formaPgto Variável String com a descrição da Forma de Pagamento
     * que deseja retornar o seu valor.
     * @param valor Objeto para receber a informação do valor acumulado.
     * @return Retorno
     */
    private Retorno getValorFormaPagamentoMFD(String formaPgto, CSPObjectReturnEcf valor) {
        Retorno rtrn;

        BemaString forma = new BemaString();
        forma.buffer = pad("", 15, " ");
        setRetornoBruto(Bematech.ValorFormaPagamento(formaPgto, forma));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            valor.setEcfBuffer(forma.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o modelo da impressora.
     *
     * @param modeloImpressora Variável para receber a informação.
     * @return Retorno
     */
    private Retorno getModeloImpressora(CSPObjectReturnEcf modeloImpressora) {
        Retorno rtrn;

        BemaString ecfModeloImpressora = new BemaString();
        ecfModeloImpressora.buffer = pad("", 11, " ");
        setRetornoBruto(Bematech.VersaoFirmwareMFD(ecfModeloImpressora));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            modeloImpressora.setEcfBuffer(ecfModeloImpressora.getBuffer());
        }

        return rtrn;
    }

    /*
     * -------------------------------------------------------------------------
     * Funções da Interface.
     * -------------------------------------------------------------------------
     */
    /**
     * Retorna a classe para montar o texto a ser impresso já com as
     * diretrizes/carateristícas da impressora em questão.
     *
     * @return
     */
    @Override
    public CSPImpressorasTextBuilder newTextBuilder() {
        return new CSPImpressorasTextBuilder(48);
    }

    /**
     * Classe para a inicialização da impressora carregando a library.
     *
     * @return
     * @throws java.io.IOException
     */
    @Override
    public Retorno startImpresora(Confs conf) throws Exception {
        Wini ini = new Wini(new File(PATH_INI));
        ini.put("Sistema", "Porta", "DEFAULT");
        ini.put("Sistema", "Path", PATH);
        ini.store();

        conf.loadLib("BemaFI64");
        /**
         * Habilita o retorno estendido da ECF para a verificação do status.
         */
        Bematech.HabilitaDesabilitaRetornoEstendidoMFD("1");

        return Retorno.OK;
    }

    /**
     * Retorna o status da ECF referente a Redução Z.
     *
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getStatusReducaoZ() throws Exception {
        Retorno rtrn;

        /**
         * Variáveis.
         */
        DateFormat f = new SimpleDateFormat("dd.MM.yyyy");
        CSPObjectReturnEcf dataImpressora = new CSPObjectReturnEcf();
        CSPObjectReturnEcf dataReducao = new CSPObjectReturnEcf();
        CSPObjectReturnEcf dataUltimaReducao = new CSPObjectReturnEcf();
        CSPObjectReturnEcf dataMovimento = new CSPObjectReturnEcf();

        /**
         * Primeiro caso. Aqui temos a RZ já emitida na impressora fiscal, ou
         * seja, a RZ do dia atual já foi impressa não permitindo abrir o
         * movimento, sendo assim, o ECF ficará bloqueado até a mudança do dia
         * (00:00h).
         */
        rtrn = getDataHoraImpressora(dataImpressora, new CSPObjectReturnEcf());

        if (rtrn == Retorno.OK) {
            rtrn = getDataMovimentoUltimaReducao(dataUltimaReducao);
        }
        Date dataAtual = f.parse(dataImpressora.getEcfBuffer().toString());
        Date dataUltimaRz = dataUltimaReducao.getEcfBuffer() == null ? null : f.parse(dataUltimaReducao.getEcfBuffer().toString());

        if (dataUltimaRz != null && dataUltimaRz.equals(dataAtual)) {
            return Retorno.RZ_JA_EMITIDA;
        }

        /**
         * Segundo caso. A RZ está pendente na impressora (Neste caso o ECF
         * imprime a mensagem “Aguardando Redução Z” logo que é ligado).
         */
        if (rtrn == Retorno.OK) {
            rtrn = getDataMovimento(dataMovimento);
        }
        Date dataMv = dataMovimento.getEcfBuffer() == null ? null : f.parse(dataMovimento.getEcfBuffer().toString());

        if (dataMv != null && dataMv.before(dataAtual)) {
            return Retorno.BLOQUEIO_POR_RZ;
        }

        /**
         * Terceiro caso. Aqui, se a data retornada pela função
         * getDataHoraReducao() for igual a data atual, temos o dia fechado, sem
         * movimento de vendas, bloqueando a impressora até a 00:00h.
         */
        if (dataUltimaRz == null && dataMv == null) {
            if (rtrn == Retorno.OK) {
                rtrn = getDataHoraReducao(dataReducao, new CSPObjectReturnEcf());
            }
            Date dataRz = dataReducao.getEcfBuffer() == null ? null : f.parse(dataReducao.getEcfBuffer().toString());

            if (dataRz != null && dataAtual.equals(dataRz)) {
                return Retorno.RZ_JA_EMITIDA;
            }
        }

        return rtrn;
    }

    /**
     * Essa função devolve o status da impressora.
     *
     * @return Retorno
     */
    @Override
    public Retorno getEstadoImpressora() {
        Retorno rtrn;
        BemaInteger iACK = new BemaInteger();
        BemaInteger iST1 = new BemaInteger();
        BemaInteger iST2 = new BemaInteger();
        BemaInteger iST3 = new BemaInteger();

        setRetornoBruto(Bematech.VerificaEstadoImpressoraMFD(iACK, iST1, iST2, iST3));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = analisaStatus(iACK.getNumber(), iST1.getNumber(), iST2.getNumber(), iST3.getNumber());
        }

        return rtrn;
    }

    /**
     * Emite a Leitura X na impressora.
     *
     * @return Retorno
     */
    @Override
    public Retorno leituraX(boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.LeituraX());

            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Verifica se a impressora está ligada ou conectada no computador.
     *
     * @return Retorno
     */
    @Override
    public Retorno getImpressoraLigada() {
        Retorno rtrn;
        setRetornoBruto(Bematech.VerificaImpressoraLigada());

        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Abre a porta serial para comunicação entre a impressora e o computador.
     * <p>
     * A função lê o nome da porta a ser aberta no arquivo BemaFI64.ini. Se o
     * parâmetro "porta" estiver configurado com a palavra "Default" a função
     * localiza onde a impressora está conectada e configura o arquivo INI.
     *
     * @return Retorno
     */
    @Override
    public Retorno openSerial() {
        Retorno rtrn;
        setRetornoBruto(Bematech.AbrePortaSerial());

        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Fecha a porta serial de comunicação entre a impressora e o computador.
     *
     * @return RetornoS
     */
    @Override
    public Retorno closeSerial() {
        Retorno rtrn;
        setRetornoBruto(Bematech.FechaPortaSerial());

        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Emite a Redução Z na impressora. Permite ajustar o relógio interno da
     * impressora em até 5 minutos.
     * <p>
     * Somente será aceito um ajuste de +/- 5 minutos. Se os valores estiverem
     * fora dessa faixa serão limitados a 5 minutos.
     * <p>
     * O cupom fiscal deve estar fechado.
     *
     * @return Retorno
     */
    @Override
    public Retorno emiteReducaoZ() {
        Retorno rtrn;

        setRetornoBruto(Bematech.ReducaoZ("", ""));

        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Abre o cupom fiscal na impressora. O cupom fiscal deve estar fechado.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return
     */
    @Override
    public Retorno abreCupomFiscal(boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.AbreCupom(""));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Fecha o cupom fiscal com a impressão da mensagem promocional.
     * <p>
     * Observações:
     * <ul>
     * <li> O cupom deve estar aberto. </li>
     * <li> Pelo menos 1 (um) item deve ter sido vendido e não pode ter sido
     * cancelado. </li>
     * <li> A utilização essa função elimina a obrigatoriedade de uso das
     * funções {@link #iniciaFechamentoCupomFiscal(String, String, String)},
     * {@link #efetuaFormaPagamento(String, String)} e
     * {@link #terminaFechamentoCupomFiscal(String)} que estão implementadas
     * internamente na função. </li>
     * </ul>
     *
     * @param formaPagamento STRING com o nome da forma de pagamento com no
     * máximo 16 caracteres.
     * @param acrescimoDesconto Indica se haverá acréscimo ou desconto no cupom.
     * 'A' para acréscimo e 'D' para desconto.
     * @param valorAcrescimoDesconto STRING com no máximo 14 dígitos para
     * acréscimo ou desconto por valor e 4 dígitos para acréscimo ou desconto
     * por percentual.
     * @param valorPago STRING com o valor pago com no máximo 14 dígitos.
     * @param mensagem STRING com a mensagem promocional com até 384 caracteres
     * (8 linhas X 48 colunas), para a impressora fiscal MP-20 FI II, e 320
     * caracteres (8 linhas X 40 colunas), para a impressora fiscal MP-40 FI II.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return
     */
    @Override
    public Retorno fechaCupomFiscal(String formaPagamento, String acrescimoDesconto, String valorAcrescimoDesconto, String valorPago, LinkedHashSet<String> mensagem, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            {
                formaPagamento = CSPUtilidadesLang.superNormalizeString(formaPagamento);
                if (formaPagamento.length() > 16) {
                    formaPagamento = formaPagamento.substring(0, 15);
                }
            }

            /**
             * Tratamentos da mensagem promocional.
             */
            StringBuilder mensagemPromocional = new StringBuilder();
            Iterator<String> itr = mensagem.iterator();
            int x = 0;
            while (itr.hasNext() && x < 8) {
                String temp = itr.next();

                if (!temp.trim().isEmpty()) {
                    if (temp.length() <= 48) {
                        mensagemPromocional.append(temp);
                        x++;
                    } else {
                        int idx = 48;

                        do {
                            mensagemPromocional.append(temp.substring(0, idx));
                            x++;

                            temp = temp.substring(idx);
                            if (x < 8 && !temp.trim().isEmpty()) {
                                mensagemPromocional.append("\n");
                            }

                            idx = 48 > temp.length() ? temp.length() : 48;
                        } while (!temp.trim().isEmpty() && x < 8);
                    }

                    if (x < 8 && itr.hasNext()) {
                        mensagemPromocional.append("\n");
                    }
                }
            }

            valorAcrescimoDesconto = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valorAcrescimoDesconto), 2).trim();

            setRetornoBruto(Bematech.FechaCupom(formaPagamento, acrescimoDesconto, "$", valorAcrescimoDesconto, valorPago, mensagemPromocional.toString()));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Cancela o último cupom emitido.
     * <p>
     * Observações:
     * <p>
     * Somente é permitido o cancelamento do cupom fiscal aberto ou
     * imediatamente após o seu encerramento. Caso algum outro documento seja
     * emitido, o cupom fiscal não será cancelado. Salvo se o documento for um
     * Comprovante de Crédito/Débito e este tenha sido estornado.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno cancelaCupomFiscal(boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.CancelaCupom());
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Inicia o fechamento do cupom com o uso das formas de pagamento.
     * <p>
     * Observações:
     * <ul>
     * <li> O cupom deve estar aberto. </li>
     * <li> Pelo menos 1 (um) item deve ter sido vendido e não pode ter sido
     * cancelado. </li>
     * <li> O valor do acréscimo ou desconto deve ser menor que o subtotal do
     * cupom. </li>
     * </ul>
     *
     * @param acrescimoDesconto Indica se haverá acréscimo ou desconto no cupom.
     * 'A' para acréscimo e 'D' para desconto.
     * @param valorAcrescimoDesconto STRING com no máximo 14 dígitos para
     * acréscimo ou desconto por valor e 4 dígitos para acréscimo ou desconto
     * por percentual.
     * @return
     */
    @Override
    public Retorno iniciaFechamentoCupomFiscal(String acrescimoDesconto, String valorAcrescimoDesconto, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.IniciaFechamentoCupom(acrescimoDesconto, "$", valorAcrescimoDesconto));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Imprime a(s) forma(s) de pagamento e o(s) valor(es) pago(s) nessa forma.
     * <p>
     * Observação: O fechamento do cupom com formas de pagamento deve ter sido
     * iniciado.
     *
     * @param formaPagamento STRING com a forma de pagamento com no máximo 16
     * caracteres.
     * @param valorPagamento STRING com o valor da forma de pagamento com até 14
     * dígitos.
     * @return
     */
    @Override
    public Retorno efetuaFormaPagamento(String formaPagamento, String valorPagamento, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.EfetuaFormaPagamento(formaPagamento, valorPagamento));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Termina o fechamento do cupom com mensagem promocional.
     * <p>
     * Importante:o campo referente a mensagem promocional também pode ser usado
     * para imprimir a mensagem suplementar, conforme exigência do Convênio
     * PAF-ECF.
     * <p>
     * Observações:
     * <ul>
     * <li> A forma de pagamento deve ter sido efetuada. </li>
     * <li> Serão impressas 8 linhas de texto por 48 colunas. </li>
     * </ul>
     *
     * @param mensagem STRING com a mensagem promocional com até 384 caracteres
     * (8 linhas X 48 colunas), para a impressora fiscal MP-20 FI II, e 320
     * caracteres (8 linhas X 40 colunas), para a impressora fiscal MP-40 FI II.
     *
     * @return Retorno
     */
    @Override
    public Retorno terminaFechamentoCupomFiscal(LinkedHashSet<String> mensagem, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            /**
             * Tratamentos da mensagem promocional.
             */
            StringBuilder mensagemPromocional = new StringBuilder();
            Iterator<String> itr = mensagem.iterator();
            int x = 0;
            while (itr.hasNext() && x < 8) {
                String temp = itr.next();

                if (!temp.trim().isEmpty()) {
                    if (temp.length() <= 48) {
                        mensagemPromocional.append(temp);
                        x++;
                    } else {
                        int idx = 48;

                        do {
                            mensagemPromocional.append(temp.substring(0, idx));
                            x++;

                            temp = temp.substring(idx);
                            if (x < 8 && !temp.trim().isEmpty()) {
                                mensagemPromocional.append("\n");
                            }

                            idx = 48 > temp.length() ? temp.length() : 48;
                        } while (!temp.trim().isEmpty() && x < 8);
                    }

                    if (x < 8 && itr.hasNext()) {
                        mensagemPromocional.append("\n");
                    }
                }
            }

            setRetornoBruto(Bematech.TerminaFechamentoCupom(mensagemPromocional.toString()));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Vende item após a abertura do cupom fiscal. Essa função permite também a
     * venda de itens com 3 casas decimais no valor unitário.
     * <p>
     * Observações:
     * <ul>
     * <li> Imagine que há duas alíquotas com o valor 12% cadastradas na
     * impressora. A primeira cadastrada na posição 01 como ICMS e a outra na
     * posição 05 como ISS. Se você informar o valor 1200 ou 12,00 no parâmetro
     * "alíquota" a função irá imprimir o item usando a alíquota 01 de ICMS. A
     * função lê as alíquotas da impressora e usa o índice da primeira
     * ocorrência. Para usar a alíquota de ISS você deverá passar o índice 05 e
     * não o valor 1200 ou 12,00. Se você não tiver duas alíquotas com o mesmo
     * valor cadastradas na impressora use sempre o valor no parâmetro alíquota.
     * </li>
     * <li> O cupom fiscal deve estar aberto. </li>
     * </ul>
     *
     * @param codigoProduto STRING com o código do produto com até 14
     * caracteres.
     * @param descricao STRING com a descrição do produto com até 200
     * caracteres.
     * @param aliquota STRING com o índice (99) ou valor (99,99) da alíquota
     * tributária.
     * @param unidade STRING com a unidade de medida com até 2 caracteres.
     * @param quantidade STRING com a quantidade fracionaria com até 7
     * caracteres (9.999,999).
     * @param valorUnitario STRING com o valor unitário com 3 casas decimais com
     * até 8 caracteres (99.999,999).
     * @param arredonda STRING com o desconto percentual (99,99) ou por valor
     * com 2 casas decimais (999.999,99).
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return
     */
    @Override
    public Retorno vendeItemCupomFiscal(String codigoProduto, String descricao, String aliquota, String unidade, String quantidade, String valorUnitario, boolean arredonda, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            /**
             * Formata a descrição.
             */
            {
                descricao = CSPUtilidadesLang.superNormalizeString(descricao);
                if (descricao.length() > 29) {
                    descricao = descricao.substring(0, 28);
                }
            }

            /**
             * Formata a unidade.
             */
            unidade = CSPUtilidadesLang.superNormalizeString(unidade);

            /**
             * Formata o valor unitário.
             */
            valorUnitario = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valorUnitario), 3).trim();

            /**
             * Formatação da quantidade do item na venda.
             */
            quantidade = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(quantidade), 3).trim();

            /**
             * Configuração de alíquotas não tributadas.
             */
            switch (aliquota) {
                case "1":
                    aliquota = "FF";
                    break;
                case "2":
                    aliquota = "II";
                    break;
                case "3":
                    aliquota = "NN";
                    break;
                default:
                    aliquota = CSPUtilidadesLang.pad(aliquota, 5, "0", true);
                    break;
            }

            setRetornoBruto(
                    Bematech.VendeItemArredondamentoMFD(
                            codigoProduto,
                            descricao,
                            aliquota,
                            unidade,
                            quantidade,
                            valorUnitario,
                            "000.000,00",
                            "000.000,00",
                            arredonda)
            );
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Efetua acréscimo ou desconto em qualquer item enquanto o cupom fiscal não
     * estiver totalizado.
     *
     * @param item STRING numérica até 3 dígitos com o número do item.
     * @param acrescimoDesconto Indica se é acréscimo ou desconto. 'A' para
     * acréscimo ou 'D' para desconto.
     * @param valorAcrescimoDesconto STRING com no máximo 14 dígitos para
     * acréscimo ou desconto por valor e 4 dígitos para acréscimo ou desconto
     * percentual.
     * @param quantidade STRING com a quantidade fracionaria.
     * @return Retorno
     */
    @Override
    public Retorno acrescimoDescontoItem(String item, String acrescimoDesconto, String valorAcrescimoDesconto, String quantidade, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            valorAcrescimoDesconto = String.valueOf(Double.valueOf(quantidade) * Double.valueOf(valorAcrescimoDesconto));
            valorAcrescimoDesconto = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valorAcrescimoDesconto), 2).trim().replace(",", "");

            setRetornoBruto(Bematech.AcrescimoDescontoItemMFD(item, acrescimoDesconto, "$", valorAcrescimoDesconto));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Emite a leitura da memória fiscal da impressora por intervalo de datas. O
     * cupom fiscal deve estar fechado.
     *
     * @param dataInicial STRING com a Data inicial no formato ddmmaa, dd/mm/aa,
     * ddmmaaaa ou dd/mm/aaaa.
     * @param dataFinal STRING com a Data final no formato ddmmaa, dd/mm/aa,
     * ddmmaaaa ou dd/mm/aaaa.
     * @param tipo
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno leituraMemoriaFiscalData(String dataInicial, String dataFinal, String tipo, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.LeituraMemoriaFiscalDataMFD(dataInicial, dataFinal, tipo));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Emite a leitura da memória fiscal da impressora por intervalo de
     * reduções. O cupom fiscal deve estar fechado.
     *
     * @param cReducaoInicial STRING com o Número da redução inicial com até 4
     * dígitos.
     * @param cReducaoFinal STRING com o Número da redução final com até 4
     * dígitos.
     * @param tipo
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno leituraMemoriaFiscalReducao(String cReducaoInicial, String cReducaoFinal, String tipo, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.LeituraMemoriaFiscalReducaoMFD(cReducaoInicial, cReducaoFinal, tipo));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Cancela qualquer item dentre os cem (100) últimos itens vendidos.
     * <p>
     * O cupom fiscal deve estar aberto.
     * <p>
     * Ao menos um item deve ter sido vendido.
     * <p>
     * O item não pode ter sido cancelado anteriormente e nem fora da faixa dos
     * últimos itens vendidos.
     *
     * @param item STRING com o número do item a ser cancelado com no máximo 3
     * dígitos.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno cancelaItem(String item, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.CancelaItemGenerico(item));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Função para cancelar o último item vendido.
     *
     * @return Retorno
     */
    @Override
    public Retorno cancelaItemAnterior(boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.CancelaItemAnterior());
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Realiza o download da MF (Memória Fiscal) das impressoras fiscais do
     * convênio ICMS 85.
     * <p>
     * Esta função é utilizada somente nas impressoras fiscais térmicas.
     * <p>
     * No emulador da impressora, para que a função tenha efeito, é obrigatório
     * que a comunicação seja realizada através de um cabo serial fisicamente
     * conectado ao micro (COM1 <-> COM2, por exemplo). A pinagem deste cabo
     * está descrito no arquivo de ajuda do emulador.
     *
     * @param mf STRING com o nome do arquivo que será gerado, exemplo:
     * "MFISCAL.MF".
     * @return Retorno
     */
    @Override
    public Retorno downloadMF(String mf) {
        Retorno rtrn;

        setRetornoBruto(Bematech.DownloadMF(mf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Realiza o download da MFD (Memória de Fita Detalhe) das impressoras
     * fiscais térmicas Bematech.
     * <ul>
     * <li> Esta função é utilizada somente nas impressoras fiscais térmicas.
     * </li>
     * <li> Os parâmetros dadoInicial e dadoFinal são obrigatórios se o tipo de
     * download for por data ou por COO.
     * </li>
     * <li> O parâmetro usuario é obrigatório se o download for por COO.
     * </li>
     * <li> Esta função não retornará os status ACK, ST1 e ST2 da impressora,
     * pois ela é utiliza, apenas, para o download da MFD.
     * </li>
     * <li> Não esqueça de copiar a BemaMFD.dll e BemaMFD2.dll para o diretório
     * de sistema de seu Windows.
     * </li>
     * <li> No emulador da impressora, para que a função tenha efeito, é
     * obrigatório que a comunicação seja realizada através de um cabo serial
     * fisicamente conectado ao micro (COM1 <-> COM2, por exemplo). A pinagem
     * deste cabo está descrito no arquivo de ajuda do emulador.
     * </li>
     * </ul>
     *
     * @param nomeArquivo STRING com o nome do arquivo que será gerado, exemplo:
     * "DOWNLOAD.MFD".
     * @param tipoDownload STRING com o tipo de download, onde: "0" (zero):
     * download total; "1" (um): download por data; "2" (dois): download por
     * COO.
     * @param dadoInicial STRING com a data ou o COO inicial (data no formato
     * DDMMAA ou DDMMAAAA, COO com no máximo 6 dígitos).
     * @param dadoFinal STRING com a data ou o COO final (data no formato DDMMAA
     * ou DDMMAAAA, COO com no máximo 6 dígitos).
     * @param usuario STRING com o número de ordem do proprietário do ECF,
     * exemplo: primeiro proprietário "cUsuario = 1". Pois para cada
     * proprietário o COO dos cupons serão diferentes.
     * @return Retorno
     */
    @Override
    public Retorno downloadMFD(String nomeArquivo, String tipoDownload, String dadoInicial, String dadoFinal, String usuario) {
        Retorno rtrn;

        if (tipoDownload.equals("1")) {
            dadoInicial = dadoInicial.replaceAll("\\.", "");
            dadoFinal = dadoFinal.replaceAll("\\.", "");
        } else if (tipoDownload.equals("2")) {
            dadoInicial = pad(dadoInicial, 6, "0", true);
            dadoFinal = pad(dadoFinal, 6, "0", true);
        }

        setRetornoBruto(Bematech.DownloadMFD(nomeArquivo, tipoDownload, dadoInicial, dadoFinal, usuario));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Gera o arquivo no formato do Ato Cotepe 17/04.
     *
     * @param nomeArquivoOrigem Path + nome do arquivo de origem.
     * @param cNomeArquivoDestino Path + nome do arquivo txt a ser gerado.
     * @param tipoDownload tipo de download, onde: "D": download por data; "C":
     * download por COO.
     * @param dadoInicial Dado inicial para o download da MFD, data ou coo.
     * @param dadoFinal dado final para o download da MFD, data ou coo.
     * @param usuario Identificacao do usuario.
     * @param tipoGeracao Indicando a parametrização a ser feita no arquivo,
     * onde: 0 = MF; 1 = MFD.
     * @param chavePublica Chave pública para assinatura do arquivo, com 256
     * caracteres.
     * @param chavePrivada Chave privada para assinatura do arquivo, com 256
     * caracteres.
     *
     * @return Retorno
     */
    @Override
    public Retorno arquivosTextoMFD(String nomeArquivoOrigem, String cNomeArquivoDestino, String tipoDownload, String dadoInicial, String dadoFinal, String usuario, int tipoGeracao, String chavePublica, String chavePrivada) {
        Retorno rtrn;

        switch (tipoDownload) {
            case "D":
                dadoInicial = dadoInicial.replaceAll("\\.", "");
                dadoFinal = dadoFinal.replaceAll("\\.", "");
                break;
            case "C":
                dadoInicial = pad(dadoInicial, 6, "0", true);
                dadoFinal = pad(dadoFinal, 6, "0", true);
                break;
            case "R":
                dadoInicial = pad(dadoInicial, 4, "0", true);
                dadoFinal = pad(dadoFinal, 4, "0", true);
                break;
        }

        setRetornoBruto(Bematech.ArquivoMFDPath(nomeArquivoOrigem, cNomeArquivoDestino, dadoInicial, dadoFinal, tipoDownload, usuario, tipoGeracao, chavePublica, chavePrivada, 1));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Gera o espelho MFD.
     *
     * @param nomeArquivo STRING com o path + nome do arquivo txt a ser gerado,
     * com o tamanho de até 512 caracteres.
     * @param tipoDownload STRING indicando o tipo de download, onde: "D":
     * download por data; "C": download por COO.
     * @param dadoInicial STRING com o dado inicial para o download da MFD, com
     * o formato DD/MM/AAAA para data ou "999999" para COO.
     * @param dadoFinal STRING com o dado final para o download da MFD, com o
     * formato DD/MM/AAAA para data ou "999999" para COO.
     * @param usuario STRING contendo a identificacao do usuario, com o tamanho
     * de até 1 caracter.
     * @param chavePublica STRING com a chave pública para assinatura do
     * arquivo, com 256 caracteres.
     * @param chavePrivada STRING com a chave privada para assinatura do
     * arquivo, com 256 caracteres.
     * @return
     */
    @Override
    public Retorno espelhoMFD(String nomeArquivo, String tipoDownload, String dadoInicial, String dadoFinal, String usuario, String chavePublica, String chavePrivada) {
        Retorno rtrn;

        if (tipoDownload.equals("D")) {
            dadoInicial = dadoInicial.replaceAll("\\.", "");
            dadoFinal = dadoFinal.replaceAll("\\.", "");
        } else {
            dadoInicial = pad(dadoInicial, 6, "0", true);
            dadoFinal = pad(dadoFinal, 6, "0", true);
        }

        setRetornoBruto(Bematech.EspelhoMFD(nomeArquivo, dadoInicial, dadoFinal, tipoDownload, usuario, chavePublica, chavePrivada));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna um número referente ao flag fiscal da impressora. Veja
     * discriminação abaixo.
     *
     * @param poucoPapel 0: Impressora com pouco papel.
     * @param sensorPoucoPapel 1: Sensor de pouco papel desabilitado, 0: Sensor
     * de pouco papel habilitado.
     * @param cancAutoCFDuasHoras 1: Cancelamento automático de cupom às duas
     * horas desabilitado, 0: Cancelamento automático de cupom às duas horas
     * habilitado.
     * @param descontoIssqn 1: desconto em issqn desabilitado, 0: desconto em
     * issqn habilitado.
     * @param rzAutomatica 1: RZ automática desabilita, 0: RZ automática
     * habilitada.
     * @param onlineOffline 1: impressora OFF-LINE, 0: impressora ON-LINE.
     *
     * @return Retorno
     */
    @Override
    public Retorno getFlagFiscalImpressoraMFD(CSPObjectReturnEcf poucoPapel, CSPObjectReturnEcf sensorPoucoPapel, CSPObjectReturnEcf cancAutoCFDuasHoras, CSPObjectReturnEcf descontoIssqn, CSPObjectReturnEcf rzAutomatica, CSPObjectReturnEcf onlineOffline) {
        Retorno rtrn;

        BemaInteger flag = new BemaInteger();

        setRetornoBruto(Bematech.FlagsFiscais3MFD(flag));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            int rFlag = flag.getNumber();

            if (onlineOffline != null) {
                if (rFlag >= 32) {
                    onlineOffline.setEcfBuffer(1);
                    rFlag -= 32;
                } else {
                    onlineOffline.setEcfBuffer(0);
                }
            } else {
                if (rFlag >= 32) {
                    rFlag -= 32;
                }
            }

            if (rzAutomatica != null) {
                if (rFlag >= 16) {
                    rzAutomatica.setEcfBuffer(0);
                    rFlag -= 16;
                } else {
                    rzAutomatica.setEcfBuffer(1);
                }
            } else {
                if (rFlag >= 16) {
                    rFlag -= 16;
                }
            }

            if (descontoIssqn != null) {
                if (rFlag >= 8) {
                    descontoIssqn.setEcfBuffer(0);
                    rFlag -= 8;
                } else {
                    descontoIssqn.setEcfBuffer(1);
                }
            } else {
                if (rFlag >= 8) {
                    rFlag -= 8;
                }
            }

            if (cancAutoCFDuasHoras != null) {
                if (rFlag >= 4) {
                    cancAutoCFDuasHoras.setEcfBuffer(0);
                    rFlag -= 4;
                } else {
                    cancAutoCFDuasHoras.setEcfBuffer(1);
                }
            } else {
                if (rFlag >= 4) {
                    rFlag -= 4;
                }
            }

            if (sensorPoucoPapel != null) {
                if (rFlag >= 2) {
                    sensorPoucoPapel.setEcfBuffer(0);
                    rFlag -= 2;
                } else {
                    sensorPoucoPapel.setEcfBuffer(1);
                }
            } else {
                if (rFlag >= 2) {
                    rFlag -= 2;
                }
            }

            if (poucoPapel != null) {
                if (rFlag >= 1) {
                    poucoPapel.setEcfBuffer(0);
                } else {
                    poucoPapel.setEcfBuffer(1);
                }
            }
        }

        return rtrn;
    }

    /**
     * Retorna se o desconto em issqn esta hábilitado.
     *
     * @param descontoIssqn 1: desconto em issqn desabilitado, 0: desconto em
     * issqn habilitado.
     *
     * @return Retorno
     */
    @Override
    public Retorno getDescontoISSQNHabilitado(CSPObjectReturnEcf descontoIssqn) {
        return getFlagFiscalImpressoraMFD(null, null, null, descontoIssqn, null, null);
    }

    /**
     * Retorna a data e a hora atual da impressora.
     *
     * @param data Objeto que guarda a data da impressora.
     * @param hora Objeto que guarda a hora da impressora.
     * @return Retorno
     * @throws java.lang.Exception
     */
    @Override
    public Retorno getDataHoraImpressora(CSPObjectReturnEcf data, CSPObjectReturnEcf hora) throws Exception {
        Retorno rtrn;

        BemaString dataB = new BemaString();
        dataB.buffer = "       ";
        BemaString horaB = new BemaString();
        horaB.buffer = "       ";

        setRetornoBruto(Bematech.DataHoraImpressora(dataB, horaB));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            DateFormat f = new SimpleDateFormat("ddMMyy");
            DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
            data.setEcfBuffer(f2.format(f.parse(dataB.getBuffer())));

            DateFormat d1 = new SimpleDateFormat("HHmmss");
            DateFormat d2 = new SimpleDateFormat("HH:mm:ss");
            hora.setEcfBuffer(d2.format(d1.parse(horaB.getBuffer())));
        }

        return rtrn;
    }

    /**
     * Retorna a data do último movimento.
     *
     * @param data Objeto que guarda a data do movimento.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getDataMovimento(CSPObjectReturnEcf data) throws Exception {
        Retorno rtrn;

        BemaString bemaData = new BemaString();
        bemaData.buffer = "       ";

        setRetornoBruto(Bematech.DataMovimento(bemaData));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();

            if (!bemaData.getBuffer().equals("000000")) {
                DateFormat f = new SimpleDateFormat("ddMMyy");
                DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");

                data.setEcfBuffer(f2.format(f.parse(bemaData.getBuffer())));
            }
        }

        return rtrn;
    }

    /**
     * Retorna a data do movimento da última redução Z.
     *
     * @param data Objeto que guarda a data da RZ impressora.
     * @return Date
     * @throws java.text.ParseException
     */
    @Override
    public Retorno getDataMovimentoUltimaReducao(CSPObjectReturnEcf data) throws Exception {
        Retorno rtrn;

        BemaString bemaData = new BemaString();
        bemaData.buffer = "       ";
        setRetornoBruto(Bematech.DataMovimentoUltimaReducaoMFD(bemaData));

        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();

            if (!bemaData.getBuffer().equals("000000")) {
                DateFormat f = new SimpleDateFormat("ddMMyy");
                DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");

                data.setEcfBuffer(f2.format(f.parse(bemaData.getBuffer())));
            }
        }

        return rtrn;
    }

    /**
     * Faz uma sangria na impressora (retirada de dinheiro).
     *
     * @param sangria STRING com o Valor da sangria.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno fazSangria(String sangria, boolean pdvInoperante) throws Exception {
        Retorno rtrn;

        if (!pdvInoperante) {
            sangria = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(sangria)).trim();

            setRetornoBruto(Bematech.Sangria(sangria));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Faz um suprimento na impressora (entrada de dinheiro).
     *
     * @param suprimento STRING com o Valor do suprimento.
     * @param forma STRING com a Forma.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno fazSuprimento(String suprimento, String forma, boolean pdvInoperante) throws Exception {
        Retorno rtrn;

        if (!pdvInoperante) {
            suprimento = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(suprimento)).trim();

            setRetornoBruto(Bematech.Suprimento(suprimento, forma));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Retorna o número de série da impressora MFD.
     *
     * @param num Variável STRING com o tamanho de 20 posições para receber a
     * informação + 1 posição para o NULL.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getNumeroSerieECF(CSPObjectReturnEcf num) throws Exception {
        Retorno rtrn;

        BemaString numSerie = new BemaString();
        numSerie.buffer = "                     ";
        setRetornoBruto(Bematech.NumeroSerieMFD(numSerie));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            num.setEcfBuffer(numSerie.getBuffer());
        }

        return rtrn;
    }

    /**
     * Obtém o grande total (GT) do ECF.
     *
     * @param gt String para receber o GT.
     *
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getGrandeTotal(CSPObjectReturnEcf gt) throws Exception {
        Retorno rtrn;

        /**
         * A variável deve ser inicializada com 21 espaços, sendo 20 para os
         * dados + 1 para o NULL.
         */
        BemaString gtCrip = new BemaString();
        gtCrip.buffer = pad("", 21, " ");
        setRetornoBruto(Bematech.GrandeTotalCriptografado(gtCrip));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();

            if (rtrn == Retorno.OK) {
                /**
                 * A variável deve ser inicializada com 21 espaços, sendo 20
                 * para os dados + 1 para o NULL.
                 */
                BemaString gtDescrip = new BemaString();
                gtDescrip.buffer = pad("", 21, " ");
                setRetornoBruto(Bematech.GrandeTotalDescriptografado(gtCrip.getBuffer(), gtDescrip));
                rtrn = analisaRetorno();

                if (rtrn == Retorno.OK) {
                    rtrn = statusImpressora();
                    gt.setEcfBuffer(gtDescrip.getBuffer());
                }
            }
        }

        return rtrn;
    }

    /**
     * Retorna os dados da impressora no momento da última redução Z.
     *
     * @param dados Classe que irá guardar os dados da última RZ.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getDadosUltimaReducaoZ(CSPDataRZ dados) throws Exception {
        Retorno rtrn;

        BemaString dadosRZ = new BemaString();
        dadosRZ.buffer = pad("", 1279, " ");
        BemaString indiceIss = new BemaString();
        indiceIss.buffer = pad("", 49, " ");

        setRetornoBruto(Bematech.VerificaIndiceAliquotasIss(indiceIss));
        setRetornoBruto(Bematech.DadosUltimaReducaoMFD(dadosRZ));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();

            String[] retornoDadosRZ = dadosRZ.getBuffer().split(",");
            dados.getListaAliquotasTributarias().getLista().clear();

            dados.setModoRz(Integer.parseInt(retornoDadosRZ[0]));
            dados.setContadorReinicioOperacao(Integer.parseInt(retornoDadosRZ[1]));
            dados.setContadorRz(Integer.parseInt(retornoDadosRZ[2]));
            dados.setContadorOrdemOperacao(Integer.parseInt(retornoDadosRZ[3]));
            dados.setContadorGeralOperacaoNFiscal(Integer.parseInt(retornoDadosRZ[4]));
            dados.setContadorCupomFiscal(Integer.parseInt(retornoDadosRZ[5]));
            dados.setContadorGeralRelatorioGerencial(Integer.parseInt(retornoDadosRZ[6]));
            dados.setContadorFitaDetalheEmitida(Integer.parseInt(retornoDadosRZ[7]));
            dados.setContadorOperacaoNFiscalCancelada(Integer.parseInt(retornoDadosRZ[8]));
            dados.setContadorCupomFiscalCancelado(Integer.parseInt(retornoDadosRZ[9]));

            {
                String temp = retornoDadosRZ[10];
                int tempInt = 0;
                if (!temp.replaceAll("0", "").isEmpty()) {
                    for (int x = 0; x < temp.length(); x += 4) {
                        tempInt += Integer.parseInt(temp.substring(x, x + 4));
                    }
                }
                dados.setContadoresEspecificosOperacoesNFiscais(tempInt);
            }
            {
                String temp = retornoDadosRZ[15];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorGeral(Double.parseDouble(temp));
            }
            {
                String acumulado = retornoDadosRZ[16];
                String percentual = retornoDadosRZ[35];
                String acumuladoAliquota = "0";

                if (!acumulado.replaceAll("0", "").isEmpty() && !percentual.replaceAll("0", "").isEmpty()) {
                    String percentualAliquota;

                    int totalizadorAliquota = 0;
                    int forAliquota = 0;

                    int idAliquota = 1;

                    for (int x = 0; x < percentual.length(); x += 4) {
                        int tempInt = Integer.parseInt(acumulado.substring(forAliquota, forAliquota + 14));
                        totalizadorAliquota += tempInt;
                        acumuladoAliquota = String.valueOf(tempInt);

                        if (!percentual.substring(x, x + 4).replaceAll("0", "").isEmpty()) {
                            percentualAliquota = percentual.substring(x, x + 4);
                            percentualAliquota = percentualAliquota.substring(0, 2) + "." + percentualAliquota.substring(2, 4);

                            if (tempInt > 0) {
                                acumuladoAliquota = (tempInt < 100 ? "0" : acumuladoAliquota.substring(0, acumuladoAliquota.length() - 2))
                                        + "." + acumuladoAliquota.substring(acumuladoAliquota.length() - 2, acumuladoAliquota.length());
                            } else {
                                acumuladoAliquota = "0";
                            }

                            dados.getListaAliquotasTributarias()
                                    .addAliquota(
                                            idAliquota,
                                            Double.parseDouble(percentualAliquota),
                                            Double.parseDouble(acumuladoAliquota),
                                            0, pad(idAliquota + "T", 3, "0", true));
                        }

                        forAliquota += 14;
                        idAliquota++;
                    }

                    acumuladoAliquota = String.valueOf(totalizadorAliquota);
                    acumuladoAliquota = (totalizadorAliquota < 100 ? "0" : acumuladoAliquota.substring(0, acumuladoAliquota.length() - 2))
                            + "." + acumuladoAliquota.substring(acumuladoAliquota.length() - 2, acumuladoAliquota.length());
                }

                dados.setTotalizadoresParciaisTributados(Double.parseDouble(acumuladoAliquota));

                if (indiceIss.getBuffer() != null && !indiceIss.getBuffer().trim().isEmpty()) {
                    for (String idIs : indiceIss.getBuffer().split(",")) {
                        CSPAliquotas al = dados.getListaAliquotasTributarias().getAliquota(Integer.parseInt(idIs));

                        if (al != null) {
                            al.setTipo(1);
                            al.setCodigo(pad(al.getId() + "S", 3, "0", true));
                            dados.getListaAliquotasTributarias().getLista().add(al);
                        }
                    }
                }
            }
            {
                String temp = retornoDadosRZ[17];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorIsencaoICMS(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[18];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorNIncidenciaICMS(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[19];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorSubstituicaoTributariaICMS(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[20];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorIsencaoISSQN(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[21];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorNIncidenciaISSQN(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[22];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorSubstituicaoTributariaISSQN(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[23];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorDescontoICMS(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[24];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorDescontosISSQN(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[25];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorAcrescimosICMS(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[26];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorAcrescimosISSQN(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[27];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorCancelamentosICMS(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[28];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorCancelamentosISSQN(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[29];
                int tempInt = 0;
                if (!temp.replaceAll("0", "").isEmpty()) {
                    for (int x = 0; x < temp.length(); x += 14) {
                        tempInt += Integer.parseInt(temp.substring(x, x + 14));
                    }
                    temp = String.valueOf(tempInt);
                    temp = (tempInt < 100 ? "0" : temp.substring(0, temp.length() - 2))
                            + "." + temp.substring(temp.length() - 2, temp.length());
                } else {
                    temp = "0";
                }
                dados.setTotalizadoresParciaisNSujeitosICMS(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[32];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorDescontosNFiscais(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[33];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorAcrescimosNFiscais(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[34];
                temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                dados.setTotalizadorCancelamentosNFiscais(Double.parseDouble(temp));
            }
            {
                String temp = retornoDadosRZ[36];
                if (!temp.subSequence(4, 6).equals("99")) {
                    DateFormat f = new SimpleDateFormat("ddMMyy");
                    DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
                    dados.setDataMovimento(f2.format(f.parse(retornoDadosRZ[36])));
                } else {
                    CSPObjectReturnEcf dataImpressora = new CSPObjectReturnEcf();
                    rtrn = getDataHoraImpressora(dataImpressora, new CSPObjectReturnEcf());

                    if (rtrn == Retorno.OK) {
                        dados.setDataMovimento(dataImpressora.getEcfBuffer().toString());
                    }
                }
            }
            {
                CSPObjectReturnEcf data = new CSPObjectReturnEcf();
                CSPObjectReturnEcf hora = new CSPObjectReturnEcf();
                rtrn = getDataHoraImpressora(data, hora);
                if (rtrn == Retorno.OK) {
                    dados.setDataEmissao(data.getEcfBuffer() + " " + hora.getEcfBuffer());
                }
            }

            dados.getListaAliquotasNTributadas().addAliquota(1, 0, dados.getTotalizadorSubstituicaoTributariaICMS(), 2, "F1");
            dados.getListaAliquotasNTributadas().addAliquota(2, 0, dados.getTotalizadorIsencaoICMS(), 2, "I1");
            dados.getListaAliquotasNTributadas().addAliquota(3, 0, dados.getTotalizadorNIncidenciaICMS(), 2, "N1");
            dados.getListaAliquotasNTributadas().addAliquota(4, 0, dados.getTotalizadorSubstituicaoTributariaISSQN(), 2, "FS1");
            dados.getListaAliquotasNTributadas().addAliquota(5, 0, dados.getTotalizadorIsencaoISSQN(), 2, "IS1");
            dados.getListaAliquotasNTributadas().addAliquota(6, 0, dados.getTotalizadorNIncidenciaISSQN(), 2, "NS1");
            dados.getListaAliquotasNTributadas().addAliquota(7, 0,
                    dados.getTotalizadorDescontosNFiscais()
                    + dados.getTotalizadorAcrescimosNFiscais()
                    + dados.getTotalizadorCancelamentosNFiscais(), 2, "OPNF");
            dados.getListaAliquotasNTributadas().addAliquota(8, 0, dados.getTotalizadorDescontoICMS(), 2, "DT");
            dados.getListaAliquotasNTributadas().addAliquota(9, 0, dados.getTotalizadorDescontosISSQN(), 2, "DS");
            dados.getListaAliquotasNTributadas().addAliquota(10, 0, dados.getTotalizadorAcrescimosICMS(), 2, "AT");
            dados.getListaAliquotasNTributadas().addAliquota(11, 0, dados.getTotalizadorAcrescimosISSQN(), 2, "AS");
            dados.getListaAliquotasNTributadas().addAliquota(12, 0, dados.getTotalizadorCancelamentosICMS(), 2, "Can-T");
            dados.getListaAliquotasNTributadas().addAliquota(13, 0, dados.getTotalizadorCancelamentosISSQN(), 2, "Can-S");
        }

        return rtrn;
    }

    /**
     * Abre Relatório Gerencial na impressora fiscal.
     *
     * @param indice String numérica com o valor entre 1 e 30, com o índice do
     * relatório.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno abreRelatorioGerencial(String indice, boolean pdvInoperante) throws Exception {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.AbreRelatorioGerencialMFD(indice));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Imprime as informações do Relatório Gerencial. O Relatório Gerencial
     * precisa estar aberto.
     *
     * @param msg String com o Texto a ser impresso no relatório.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno usaRelatorioGerencial(String msg) throws Exception {
        Retorno rtrn;

        if (msg.length() > 618) {
            do {
                String temp = msg.substring(0, 618);
                if (temp.contains("\n")) {
                    temp = temp.substring(0, temp.lastIndexOf("\n"));
                }

                setRetornoBruto(Bematech.UsaRelatorioGerencialMFD(temp));
                rtrn = analisaRetorno();

                if (rtrn == Retorno.OK) {
                    msg = msg.substring(temp.length(), msg.length());
                } else {
                    break;
                }
            } while (msg.length() > 618);
        }

        setRetornoBruto(Bematech.UsaRelatorioGerencialMFD(msg));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Encerra o relatório gerencial.
     *
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno fechaRelatorioGerencial() throws Exception {
        Retorno rtrn;

        setRetornoBruto(Bematech.FechaRelatorioGerencial());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna o número de intervenções técnicas realizadas na impressora.
     *
     * @param numeroIntervencoes Variável string para receber o número de
     * intervenções.
     * @return Retorno
     */
    @Override
    public Retorno getNumeroIntervencoes(CSPObjectReturnEcf numeroIntervencoes) {
        Retorno rtrn;

        BemaString numInte = new BemaString();
        numInte.buffer = pad("", 5, " ");
        setRetornoBruto(Bematech.NumeroIntervencoes(numInte));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            numeroIntervencoes.setEcfBuffer(numInte.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o número de reduções Z realizadas na impressora.
     *
     * @param numeroReducoes Variável string para receber o número de Reduções
     * Z.
     * @return Retorno
     */
    @Override
    public Retorno getNumeroReducoes(CSPObjectReturnEcf numeroReducoes) {
        Retorno rtrn;

        BemaString numRed = new BemaString();
        numRed.buffer = pad("", 5, " ");
        setRetornoBruto(Bematech.NumeroReducoes(numRed));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            numeroReducoes.setEcfBuffer(numRed.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o grande total (GT) da última redução Z.
     *
     * @param grandeTotalUltimaRz Variável string para receber a informação.
     * @return Retorno
     */
    @Override
    public Retorno getGrandeTotalUltimaReducao(CSPObjectReturnEcf grandeTotalUltimaRz) {
        Retorno rtrn;

        BemaString gtRed = new BemaString();
        gtRed.buffer = pad("", 19, " ");
        setRetornoBruto(Bematech.GrandeTotalUltimaReducaoMFD(gtRed));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            grandeTotalUltimaRz.setEcfBuffer(gtRed.getBuffer());
        }

        return rtrn;
    }

    /**
     * Programa as formas de pagamento.
     *
     * @param formaPgto String com a forma de pagamento.
     * @param permiteTef String com 0 (zero) ou 1 (um) indicando se a forma de
     * pagamento permite operação TEF ou não, onde: 1 - permite operação TEF; 0
     * - não permite operação TEF.
     * @return Retorno
     */
    @Override
    public Retorno ProgramaFormaPagamento(String formaPgto, String permiteTef) {
        Retorno rtrn;

        setRetornoBruto(Bematech.ProgramaFormaPagamentoMFD(formaPgto, permiteTef));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna as formas de pagamento e seus valores acumulados.
     *
     * @param lista Classe para a manipulação da lista de formas de pagamento
     * retornada da impressora.
     * @return Retorno
     */
    @Override
    public Retorno getFormasPagamento(CSPListaFormasPgto lista) {
        Retorno rtrn;

        BemaString formasPgto = new BemaString();
        formasPgto.buffer = pad("", 3017, " ");
        setRetornoBruto(Bematech.VerificaFormasPagamento(formasPgto));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            lista.getLista().clear();

            String[] retornoPgto = formasPgto.getBuffer().split(",");

            for (int x = 0; x < retornoPgto.length; x++) {

                String pgto = retornoPgto[x];

                String valCum = pgto.substring(pgto.length() - 41, pgto.length() - 21);
                valCum = valCum.substring(0, valCum.length() - 2) + "." + valCum.substring(valCum.length() - 2, valCum.length());

                String valUltCup = pgto.substring(pgto.length() - 21, pgto.length() - 1);
                valUltCup = valUltCup.substring(0, valUltCup.length() - 2) + "." + valUltCup.substring(valUltCup.length() - 2, valUltCup.length());

                int emiteCupomNFicalVinculado = Integer.parseInt(pgto.substring(pgto.length() - 1, pgto.length()));

                lista.addFormaPgto(x + 1, pgto.substring(0, pgto.length() - 41).trim(),
                        Double.parseDouble(valCum),
                        Double.parseDouble(valUltCup), (emiteCupomNFicalVinculado == 1));
            }
        }

        return rtrn;
    }

    /**
     * Retorna o número do cupom.
     *
     * @param coo Variável para receber o número do último cupom.
     * @return Retorno
     */
    @Override
    public Retorno getCOO(CSPObjectReturnEcf coo) {
        Retorno rtrn;

        BemaString cupom = new BemaString();
        cupom.buffer = pad("", 7, " ");
        setRetornoBruto(Bematech.NumeroCupom(cupom));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            coo.setEcfBuffer(cupom.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o número do caixa cadastrado na impressora.
     *
     * @param caixa Variável para receber o número do caixa.
     * @return Retorno
     */
    @Override
    public Retorno getNumeroCaixa(CSPObjectReturnEcf caixa) {
        Retorno rtrn;

        BemaString numCaixa = new BemaString();
        numCaixa.buffer = pad("", 5, " ");
        setRetornoBruto(Bematech.NumeroCaixa(numCaixa));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            caixa.setEcfBuffer(numCaixa.getBuffer());
        }

        return rtrn;
    }

    @Override
    public Retorno getRelatoriosGerenciais(CSPListaRelatorioGerencial rgs) {
        Retorno rtrn;

        BemaString rGerenciais = new BemaString();
        rGerenciais.buffer = pad("", 660, " ");
        setRetornoBruto(Bematech.VerificaRelatorioGerencialMFD(rGerenciais));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            rgs.getLista().clear();

            String[] retornoRGs = rGerenciais.getBuffer().split(",");
            for (int x = 0; x < retornoRGs.length; x++) {
                rgs.addRelatorioGerencial(x + 1, Integer.parseInt(retornoRGs[x].substring(0, 4)), retornoRGs[x].substring(4, 21).trim());
            }
        }

        return rtrn;
    }

    /**
     * Retorna a marca, o modelo e o tipo da impressora.
     *
     * @param marca Variável para receber a marca da impressora.
     * @param modelo Variável para receber o modelo
     * @param tipo Variável para receber o tipo da impressora
     * @return Retorno
     */
    @Override
    public Retorno getMarcaModeloTipoImpressora(CSPObjectReturnEcf marca, CSPObjectReturnEcf modelo, CSPObjectReturnEcf tipo) {
        Retorno rtrn;

        BemaString ecfMarca = new BemaString();
        BemaString ecfModelo = new BemaString();
        BemaString ecfTipo = new BemaString();
        ecfMarca.buffer = pad("", 16, " ");
        ecfModelo.buffer = pad("", 21, " ");
        ecfTipo.buffer = pad("", 8, " ");
        setRetornoBruto(Bematech.MarcaModeloTipoImpressoraMFD(ecfMarca, ecfModelo, ecfTipo));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            marca.setEcfBuffer(ecfMarca.getBuffer().trim());
            modelo.setEcfBuffer(ecfModelo.getBuffer().trim());
            tipo.setEcfBuffer(ecfTipo.getBuffer().trim());
        }

        return rtrn;
    }

    /**
     * Retorna a versão do firmware da impressora.
     *
     * @param versaoFirmware Variável para receber a informação.
     * @return Retorno
     */
    @Override
    public Retorno getVersaoFirmware(CSPObjectReturnEcf versaoFirmware) {
        Retorno rtrn;

        BemaString ecfVersaoFirmware = new BemaString();
        ecfVersaoFirmware.buffer = pad("", 7, " ");
        setRetornoBruto(Bematech.VersaoFirmwareMFD(ecfVersaoFirmware));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            versaoFirmware.setEcfBuffer(ecfVersaoFirmware.getBuffer());
        }

        return rtrn;
    }

    /**
     * Esta função retorna a sigla da MF adicional, caso exista.
     *
     * @param dataSw Variável para receber a informação da data de gravação do
     * software básico da impressora.
     * @param horaSw Variável para receber a informação da hora de gravação do
     * software básico da impressora.
     * @param mfAdicional Variável para receber a informação.
     * @return Retorno
     * @throws java.lang.Exception
     */
    @Override
    public Retorno getDataHoraSWMFAdicional(CSPObjectReturnEcf dataSw, CSPObjectReturnEcf horaSw, CSPObjectReturnEcf mfAdicional) throws Exception {
        Retorno rtrn;

        BemaString ecfMfAdicional = new BemaString();
        ecfMfAdicional.buffer = "  ";
        BemaString ecfDataHoraSw = new BemaString();
        ecfDataHoraSw.buffer = pad("", 21, " ");
        setRetornoBruto(Bematech.DataHoraGravacaoUsuarioSWBasicoMFAdicional(new BemaString(), ecfDataHoraSw, ecfMfAdicional));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            mfAdicional.setEcfBuffer(ecfMfAdicional.getBuffer().trim().isEmpty() ? "N" : ecfMfAdicional.getBuffer());
            if (!"00/00/0000 00:00:00".equals(ecfDataHoraSw.buffer)) {
                DateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                ecfDataHoraSw.buffer = f2.format(f.parse(ecfDataHoraSw.buffer));
                dataSw.setEcfBuffer(ecfDataHoraSw.buffer.substring(0, 10));
                horaSw.setEcfBuffer(ecfDataHoraSw.buffer.substring(11, 19));
            }
        }

        return rtrn;
    }

    /**
     * Retorna o número de operações não fiscais executadas na impressora.
     *
     * @param gnf Variável para receber o número do gnf.
     * @return Retorno
     */
    @Override
    public Retorno getGNF(CSPObjectReturnEcf gnf) {
        Retorno rtrn;

        BemaString gnfEcf = new BemaString();
        gnfEcf.buffer = pad("", 7, " ");
        setRetornoBruto(Bematech.NumeroOperacoesNaoFiscais(gnfEcf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            gnf.setEcfBuffer(gnfEcf.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o número de relatórios gerenciais emitidos.
     *
     * @param grg Variável para receber o número do grg.
     * @return Retorno
     */
    @Override
    public Retorno getGRG(CSPObjectReturnEcf grg) {
        Retorno rtrn;

        BemaString grgEcf = new BemaString();
        grgEcf.buffer = pad("", 7, " ");
        setRetornoBruto(Bematech.ContadorRelatoriosGerenciaisMFD(grgEcf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            grg.setEcfBuffer(grgEcf.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o número de comprovantes de crédito emitidos.
     *
     * @param cdc Variável para receber o número do cdc.
     * @return Retorno
     */
    @Override
    public Retorno getCDC(CSPObjectReturnEcf cdc) {
        Retorno rtrn;

        BemaString cdcEcf = new BemaString();
        cdcEcf.buffer = pad("", 5, " ");
        setRetornoBruto(Bematech.ContadorComprovantesCreditoMFD(cdcEcf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            cdc.setEcfBuffer(cdcEcf.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o número de substituições de proprietário (ordem do usuário).
     *
     * @param numOrdemUsuario Variável para receber o número de ordem do usuário
     * do ECF.
     * @return Retorno
     */
    @Override
    public Retorno getNumOrdemUsuario(CSPObjectReturnEcf numOrdemUsuario) {
        Retorno rtrn;

        BemaString numOrdemEcf = new BemaString();
        numOrdemEcf.buffer = pad("", 5, " ");
        setRetornoBruto(Bematech.NumeroSubstituicoesProprietario(numOrdemEcf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            numOrdemUsuario.setEcfBuffer(numOrdemEcf.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna o número de cupons fiscais emitidos.
     *
     * @param ccf Variável para receber o número do ccf.
     * @return Retorno
     */
    @Override
    public Retorno getCCF(CSPObjectReturnEcf ccf) {
        Retorno rtrn;

        BemaString ccfEcf = new BemaString();
        ccfEcf.buffer = pad("", 7, " ");
        setRetornoBruto(Bematech.ContadorCupomFiscalMFD(ccfEcf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
            ccf.setEcfBuffer(ccfEcf.getBuffer());
        }

        return rtrn;
    }

    /**
     * Retorna as alíquotas cadastradas na impressora.
     *
     * @param aliquotas
     * @return Retorno
     */
    @Override
    public Retorno getAliquotas(CSPListaAliquotas aliquotas) {
        Retorno rtrn;

        BemaString aliEcf = new BemaString();
        aliEcf.buffer = pad("", 80, " ");
        BemaString indiceIss = new BemaString();
        indiceIss.buffer = pad("", 49, " ");

        setRetornoBruto(Bematech.VerificaIndiceAliquotasIss(indiceIss));
        setRetornoBruto(Bematech.RetornoAliquotas(aliEcf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            String[] retornoDados = aliEcf.getBuffer().split(",");

            for (int i = 0; i < retornoDados.length; i++) {
                int idAliquota = i + 1;
                String aliquota = retornoDados[i];
                if (Integer.parseInt(aliquota) > 0) {
                    aliquota = aliquota.substring(0, 2) + "." + aliquota.substring(2, 4);
                    aliquotas.addAliquota(
                            idAliquota,
                            Double.parseDouble(aliquota),
                            0,
                            0, pad(idAliquota + "T", 3, "0", true) + retornoDados[i]);
                }
            }

            if (indiceIss.getBuffer() != null && !indiceIss.getBuffer().trim().isEmpty()) {
                for (String idIs : indiceIss.getBuffer().split(",")) {
                    CSPAliquotas al = aliquotas.getAliquota(Integer.parseInt(idIs));

                    if (al != null) {
                        al.setTipo(1);
                        al.setCodigo(al.getCodigo().replace("T", "S"));
                        aliquotas.getLista().add(al);
                    }
                }
            }

            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Abre o comprovante não fiscal vinculado.
     *
     * @param formaPagamento Forma de pagamento com até 16 caracteres.
     * @param valor Valor pago na forma de pagamento com até 14 dígitos (2 casas
     * decimais).
     * @param ccf Número do cupom a que se refere o comprovante com até 6
     * dígitos.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno abreComprovanteNaoFiscalVinculado(String formaPagamento, String valor, String ccf, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(Bematech.AbreComprovanteNaoFiscalVinculado(formaPagamento, "", ccf));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Imprime o comprovante não fiscal vinculado.
     *
     * @param msg Texto a ser impresso no comprovante não fiscal vinculado com
     * até 618 caracteres.
     * @return Retorno
     */
    @Override
    public Retorno usaComprovanteNaoFiscalVinculado(String msg) {
        Retorno rtrn;

        msg = CSPUtilidadesLang.superNormalizeString(msg);

        setRetornoBruto(Bematech.UsaComprovanteNaoFiscalVinculado(msg));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Encerrar o comprovante não fiscal vinculado.
     *
     * @return Retorno
     */
    @Override
    public Retorno fechaComprovanteNaoFiscalVinculado() {
        Retorno rtrn;

        setRetornoBruto(Bematech.FechaComprovanteNaoFiscalVinculado());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }
}
