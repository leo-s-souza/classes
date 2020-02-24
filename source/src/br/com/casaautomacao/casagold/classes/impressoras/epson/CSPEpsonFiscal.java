/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.epson;

import Epson.InterfaceEpson.InterfaceEpson;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasFiscaisBase;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasTextBuilder;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPDataRZ;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaAliquotas;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaFormasPgto;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaRelatorioGerencial;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPObjectReturnEcf;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.pad;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Classe de funções da impressora fical Epson.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public final class CSPEpsonFiscal extends CSPImpressorasFiscaisBase {

    /**
     * Guarda a instancia da dll para a chamada de funções.
     */
    private final InterfaceEpson EPSON = new InterfaceEpson();

    /**
     * -------------------------------------------------------------------------
     * Funções Privadas.
     * -------------------------------------------------------------------------
     */
    /**
     * Faz análise do retorno da impressora, pegando o enum referente ao mesmo.
     *
     * @return Retorno Enum do retorno.
     */
    private Retorno analisaRetorno() {
        Retorno rtrn;
        switch (getRetornoBruto()) {
            case 0:
                rtrn = Retorno.OK;
                break;
            default:
                rtrn = Retorno.ERRO_DE_EXECUCAO;
                break;
        }

        return rtrn;
    }

    /**
     * Lê o retorno estendido da impressora referente ao último comando enviado.
     * <ul>
     * <li> Essa função deve ser usada após a execução de qualquer outra função
     * da dll para ler o retorno da impressora referente à função executada.
     * Essa função devolve o status da impressora (pouco papel, comando não
     * executado, tipo de parâmetro de CMD inválido etc.).
     * </li>
     * </ul>
     *
     * @return Retorno
     */
    private Retorno statusImpressora() {
        Retorno rtrn;

        setRetornoBruto(EPSON.EPSON_Obter_Estado_ImpressoraEX());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            rtrn = analisaStatus(EPSON.retornos.sArg3);
        } else {
            rtrn = Retorno.ERRO_DE_EXECUCAO;
        }

        return rtrn;
    }

    /**
     * Analisa os retornos da impressora.
     *
     * @param estadoImpressora
     * @return
     */
    private Retorno analisaStatus(String estadoImpressora) {
        Retorno rtrn = Retorno.OK;

        switch (estadoImpressora) {
            case "0804":
            case "080D":
            case "0802":
                rtrn = Retorno.BLOQUEIO_POR_RZ;
                break;
            case "080F":
            case "0801":
                rtrn = Retorno.RZ_JA_EMITIDA;
                break;
            case "0304":
            case "0305":
                rtrn = Retorno.FIM_POUCO_PAPEL;
                break;
            default:
                break;
        }

        return rtrn;
    }

    /**
     * Verifica se o período de vendas esta aberto ou fechado.
     *
     * @return
     */
    private boolean isPeriodoFechado() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Estado_Impressora());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            String estadoImpressora = EPSON.retornos.sArg1.substring(12, 16);
            estadoImpressora = Integer.toBinaryString(Integer.parseInt(estadoImpressora, 16));
            estadoImpressora = new StringBuilder(estadoImpressora).reverse().toString();

            if (estadoImpressora.length() >= 8) {
                return estadoImpressora.charAt(7) == '0';
            }
        }

        return false;
    }

    /**
     * Abre a jornada fiscal do ECF.
     *
     * @return
     */
    public Retorno abreJornadaFiscal() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_RelatorioFiscal_Abrir_Jornada());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
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
        return new CSPImpressorasTextBuilder(56);
    }

    /**
     * Verifica se a impressora está ligada ou conectada no computador.
     *
     * @return Retorno
     */
    @Override
    public Retorno getImpressoraLigada() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Estado_Impressora());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            String estadoImpressora = EPSON.retornos.sArg1.substring(8, 12);
            estadoImpressora = Integer.toBinaryString(Integer.parseInt(estadoImpressora, 16));
            estadoImpressora = new StringBuilder(estadoImpressora).reverse().toString();

            if (estadoImpressora.length() >= 16 && estadoImpressora.charAt(15) == '1') {
                rtrn = Retorno.ERRO_DE_COMUNICACAO;
            }
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Abre a porta serial para comunicação entre a impressora e o computador.
     *
     * @return Retorno
     */
    @Override
    public Retorno openSerial() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Serial_Abrir_PortaEX());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Fecha a porta serial de comunicação entre a impressora e o computador.
     *
     * @return Retorno
     */
    @Override
    public Retorno closeSerial() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Serial_Fechar_Porta());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Classe para a inicialização da impressora. Carrega a library e abre a
     * porta de comunicação.
     *
     * @param conf
     * @return Retorno
     * @throws java.lang.Exception
     */
    @Override
    public Retorno startImpresora(Confs conf) throws Exception {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Serial_Abrir_PortaAD());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            setRetornoBruto(EPSON.EPSON_Serial_Abrir_Fechar_Porta_CMD(Integer.valueOf(EPSON.retornos.sArg1),
                    Integer.valueOf(EPSON.retornos.sArg2)));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.OK) {
                if (isPeriodoFechado()) {
                    if (getStatusReducaoZ() == Retorno.OK) {
                        rtrn = abreJornadaFiscal();
                    }
                }
            }
        }

        return rtrn;
    }

    /**
     * Retorna o estado da impressora.
     *
     * @return Retorno
     */
    @Override
    public Retorno getEstadoImpressora() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Estado_Impressora());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            String estadoImpressora = EPSON.retornos.sArg1.substring(8, 12);
            estadoImpressora = Integer.toBinaryString(Integer.parseInt(estadoImpressora, 16));
            estadoImpressora = new StringBuilder(estadoImpressora).reverse().toString();

            if (!estadoImpressora.equals("0")) {
                if (estadoImpressora.charAt(0) == '1') {
                    rtrn = Retorno.FIM_POUCO_PAPEL;
                } else {
                    if ((estadoImpressora.length() >= 2 && estadoImpressora.charAt(1) == '1')
                            || (estadoImpressora.length() >= 3 && estadoImpressora.charAt(2) == '1')
                            || (estadoImpressora.length() >= 4 && estadoImpressora.charAt(3) == '1')) {
                        rtrn = Retorno.FIM_POUCO_PAPEL;
                    } else if (estadoImpressora.length() >= 14 && estadoImpressora.charAt(13) == '1') {
                        rtrn = Retorno.TAMPA_LEVANTADA;
                    } else if (estadoImpressora.length() >= 15 && estadoImpressora.charAt(14) == '1') {
                        rtrn = Retorno.ERRO_DE_EXECUCAO;
                    } else if (estadoImpressora.length() >= 16 && estadoImpressora.charAt(15) == '1') {
                        rtrn = Retorno.ERRO_DE_COMUNICACAO;
                    }
                }
            } else {
                estadoImpressora = EPSON.retornos.sArg1.substring(12, 16);
                estadoImpressora = Integer.toBinaryString(Integer.parseInt(estadoImpressora, 16));
                estadoImpressora = new StringBuilder(estadoImpressora).reverse().toString();

                if (!estadoImpressora.equals("0")) {
                    if (estadoImpressora.length() == 1 && estadoImpressora.charAt(0) == '1') {
                        rtrn = Retorno.CUPOM_ABERTO;
                    } else if (estadoImpressora.length() >= 4 && estadoImpressora.charAt(0) == '1' && estadoImpressora.charAt(1) == '0'
                            && estadoImpressora.charAt(2) == '0' && estadoImpressora.charAt(3) == '0') {
                        rtrn = Retorno.CUPOM_ABERTO;
                    }
                }
            }
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna o status da ECF referente a Redução Z.
     *
     * @return Retorno
     * @throws java.lang.Exception
     */
    @Override
    public Retorno getStatusReducaoZ() throws Exception {
        Retorno rtrn;
        CSPObjectReturnEcf dataImpressora = new CSPObjectReturnEcf();
        CSPObjectReturnEcf horaImpressora = new CSPObjectReturnEcf();
        CSPObjectReturnEcf dataJornada = new CSPObjectReturnEcf();
        CSPObjectReturnEcf dataRz = new CSPObjectReturnEcf();

        rtrn = getDataHoraImpressora(dataImpressora, horaImpressora);
        if (rtrn == Retorno.OK) {
            rtrn = getDataMovimentoUltimaReducao(dataRz);

            if (rtrn == Retorno.OK) {
                Date horaLimite = new SimpleDateFormat("HH:mm:ss").parse("02:00:00");
                Date dataEcf = dataImpressora.getEcfBufferToDate();
                Date horaEcf = horaImpressora.getEcfBufferToTime();
                Date dataRzEcf = dataRz.getEcfBufferToDate();

                if (dataEcf.equals(dataRzEcf)) {
                    rtrn = Retorno.RZ_JA_EMITIDA;
                } else {
                    rtrn = getDataMovimento(dataJornada);
                    if (rtrn == Retorno.OK) {
                        Date dataJornadaEcf = dataJornada.getEcfBufferToDate();

                        if (dataRzEcf != null && dataJornadaEcf != null) {
                            if (dataJornadaEcf.equals(dataRzEcf) && dataJornadaEcf.before(dataEcf)) {
                                if (horaEcf.before(horaLimite)) {
                                    rtrn = Retorno.RZ_JA_EMITIDA;
                                }
                            } else if (!dataEcf.equals(dataJornadaEcf) && !dataJornadaEcf.equals(dataRzEcf)) {
                                if (horaEcf.after(horaLimite)) {
                                    rtrn = Retorno.BLOQUEIO_POR_RZ;
                                }
                            }
                        }
                    }
                }
            }
        }

        return rtrn;
    }

    /**
     * Esta função imprime um Relatório Z e fecha a Jornada Fiscal corrente.
     *
     * @return
     */
    @Override
    public Retorno emiteReducaoZ() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_RelatorioFiscal_RZEX(2));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
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
            setRetornoBruto(EPSON.EPSON_RelatorioFiscal_LeituraX());
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Abre o cupom fiscal na impressora.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno abreCupomFiscal(boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(EPSON.EPSON_Fiscal_Abrir_Cupom("", "", "", "", 1));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Fecha o cupom fiscal.
     *
     * @param formaPagamento Nome da forma de pagamento.
     * @param acrescimoDesconto Indica se haverá acréscimo ou desconto no cupom.
     * @param valorAcrescimoDesconto Valor de acréscimo ou desconto.
     * @param valorPago Valor pago.
     * @param mensagem Mensagem promocional.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno fechaCupomFiscal(String formaPagamento, String acrescimoDesconto, String valorAcrescimoDesconto, String valorPago, LinkedHashSet<String> mensagem, boolean pdvInoperante) {
        Retorno rtrn = Retorno.OK;

        if (!pdvInoperante) {
            /**
             * Efetua o desconto/acréscimo no subtotal do cupom.
             */
            if (Double.parseDouble(valorAcrescimoDesconto) > 0) {
                rtrn = iniciaFechamentoCupomFiscal(acrescimoDesconto, valorAcrescimoDesconto, pdvInoperante);
            }

            if (rtrn == Retorno.OK) {
                /**
                 * Processa o pagamento do Cupom Fiscal.
                 */
                rtrn = efetuaFormaPagamento(formaPagamento, valorPago, pdvInoperante);

                /**
                 * Fecha o cupom fiscal e corta o papel.
                 */
                if (rtrn == Retorno.OK) {
                    rtrn = terminaFechamentoCupomFiscal(mensagem, pdvInoperante);
                }
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Esta função cancela o último cupom fiscal ou o cupom aberto. Caso existam
     * Comprovantes de Crédito ou Débito relacionados a este cupom, os mesmos
     * também serão cancelados.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno cancelaCupomFiscal(boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(EPSON.EPSON_Fiscal_Cancelar_CupomEX());
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Inicia o fechamento do cupom efetuado um desconto ou acréscimo no
     * subtotal do cupom, caso existam.
     *
     * @param acrescimoDesconto Indica se haverá acréscimo ou desconto no cupom.
     * @param valorAcrescimoDesconto Valor de acréscimo ou desconto.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno iniciaFechamentoCupomFiscal(String acrescimoDesconto, String valorAcrescimoDesconto, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            /**
             * Formata o valor de Acrescimo/Desconto.
             */
            valorAcrescimoDesconto = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valorAcrescimoDesconto), 2).trim().replace(",", "");

            /**
             * Efetua um desconto ou acréscimo no subtotal do cupom.
             */
            setRetornoBruto(EPSON.EPSON_Fiscal_Desconto_Acrescimo_Subtotal(valorAcrescimoDesconto, 2, acrescimoDesconto.equals("D"), false));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Esta função processa o pagamento do Cupom Fiscal, imprimindo a(s)
     * forma(s) de pagamento e o(s) valor(es) pago(s).
     *
     * @param formaPagamento Forma de pagamento.
     * @param valorPagamento Valor da forma.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno efetuaFormaPagamento(String formaPagamento, String valorPagamento, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            /**
             * Formata o valor pago.
             */
            valorPagamento = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valorPagamento.replace(",", ".")), 2).trim().replace(",", "");

            /**
             * Tira os caracteres especiais, caso tenha.
             */
            formaPagamento = CSPUtilidadesLang.superNormalizeString(formaPagamento);

            /**
             * Imprime a forma de pagamento.
             */
            setRetornoBruto(EPSON.EPSON_Fiscal_Pagamento(formaPagamento.trim(), valorPagamento, 2, "", ""));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Termina o fechamento do cupom, imprimindo a mensagem promocional.
     *
     * @param mensagem Mensagem promocional.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
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
                    if (temp.length() <= 56) {
                        mensagemPromocional.append(temp);
                        x++;
                    } else {
                        int idx = 56;

                        do {
                            mensagemPromocional.append(temp.substring(0, idx));
                            x++;

                            temp = temp.substring(idx);
                            if (x < 8 && !temp.trim().isEmpty()) {
                                mensagemPromocional.append("\r\n");
                            }

                            idx = 56 > temp.length() ? temp.length() : 56;
                        } while (!temp.trim().isEmpty() && x < 8);
                    }

                    if (x < 8 && itr.hasNext()) {
                        mensagemPromocional.append("\r\n");
                    }
                }
            }

            /**
             * Imprime a mensagem promocional.
             */
            setRetornoBruto(EPSON.EPSON_Fiscal_Imprimir_MensagemEX(mensagemPromocional.toString()));
            rtrn = analisaRetorno();

            /**
             * Fecha o cupom.
             */
            if (rtrn == Retorno.OK) {
                setRetornoBruto(EPSON.EPSON_Fiscal_Fechar_Cupom(true, false));
                rtrn = analisaRetorno();

                if (rtrn == Retorno.OK) {
                    return rtrn;
                }
            }

            rtrn = statusImpressora();
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Esta função vende um item no Cupom Fiscal, permitindo selecionar se o
     * resultado da operação de multiplicação do preço pela quantidade deverá
     * ser truncado ou arredondado.
     *
     * @param codigoProduto Código do produto.
     * @param descricao Descrição do produto.
     * @param aliquota Valor ou o índice na ECF da alíquota tributária do
     * produto.
     * @param unidade Sigla da unidade de medida usada.
     * @param quantidade Valor para a quantidade vendida do produto.
     * @param valorUnitario Valor unitário do produto.
     * @param arredonda Indica se o valor do produto terá arredondamento ou
     * truncamento.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
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
                if (descricao.length() > 233) {
                    descricao = descricao.substring(0, 232);
                }
            }

            /**
             * Formata a unidade.
             */
            unidade = CSPUtilidadesLang.superNormalizeString(unidade);

            /**
             * Formata o valor unitário.
             */
            valorUnitario = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valorUnitario), 3).trim().replace(",", "");

            /**
             * Formatação da quantidade do item na venda.
             */
            quantidade = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(quantidade), 3).trim().replace(",", "");

            /**
             * Configuração de alíquotas não tributadas.
             */
            switch (aliquota) {
                case "1":
                    aliquota = "F";
                    break;
                case "2":
                    aliquota = "I";
                    break;
                case "3":
                    aliquota = "N";
                    break;
                default:
                    aliquota = CSPUtilidadesLang.pad(aliquota.replace(",", ""), 4, "0", true);
                    break;
            }

            setRetornoBruto(EPSON.EPSON_Fiscal_Vender_Item_AD(codigoProduto, descricao, quantidade, 3, unidade, valorUnitario, 3, aliquota, 1, arredonda ? 2 : 1, 1));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
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
     * @param item STRING numérica com o número do item.
     * @param acrescimoDesconto Indica se é acréscimo ou desconto. 'A' para
     * acréscimo ou 'D' para desconto.
     * @param valorAcrescimoDesconto STRING para acréscimo ou desconto por valor
     * ou percentual.
     * @param quantidade STRING com a quantidade fracionaria.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno acrescimoDescontoItem(String item, String acrescimoDesconto, String valorAcrescimoDesconto, String quantidade, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            /**
             * Formata o valor de Acrescimo/Desconto.
             */
            valorAcrescimoDesconto = String.valueOf(Double.valueOf(quantidade) * Double.valueOf(valorAcrescimoDesconto));
            valorAcrescimoDesconto = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valorAcrescimoDesconto), 2).trim().replace(",", "");

            setRetornoBruto(EPSON.EPSON_Fiscal_Desconto_Acrescimo_ItemEX(item, valorAcrescimoDesconto, 2, acrescimoDesconto.equals("D"), false));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
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
     * @param dataInicial String com a Data inicial no formato ddmmaaaa.
     * @param dataFinal String com a Data final no formato ddmmaaaa.
     * @param tipo
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno leituraMemoriaFiscalData(String dataInicial, String dataFinal, String tipo, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(EPSON.EPSON_RelatorioFiscal_Leitura_MF(dataInicial, dataFinal, 5, null, 1024));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
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
     * @param reducaoInicial String com o Número da redução inicial com até 4
     * dígitos.
     * @param reducaoFinal String com o Número da redução final com até 4
     * dígitos.
     * @param tipo
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno leituraMemoriaFiscalReducao(String reducaoInicial, String reducaoFinal, String tipo, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(EPSON.EPSON_RelatorioFiscal_Leitura_MF(reducaoInicial, reducaoFinal, 4, null, 1024));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Função para cancelamento de item.
     *
     * @param item Número do item.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno cancelaItem(String item, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(EPSON.EPSON_Fiscal_Cancelar_Item(item));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Esta função cancela o último item vendido no cupom fiscal atual.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno cancelaItemAnterior(boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(EPSON.EPSON_Fiscal_Cancelar_Ultimo_Item());
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
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
     *
     * @param mf String com o nome do arquivo que será gerado, exemplo:
     * "MFISCAL.MF".
     * @return Retorno
     */
    @Override
    public Retorno downloadMF(String mf) {
        Retorno rtrn;

        setRetornoBruto(EPSON.EPSON_Obter_Arquivo_Binario_MF(mf));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
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
     * </ul>
     *
     * @param nomeArquivo String com o nome do arquivo que será gerado, exemplo:
     * "DOWNLOAD.MFD".
     * @param tipoDownload String com o tipo de download, onde: "0" (zero):
     * download total; "1" (um): download por data; "2" (dois): download por
     * COO.
     * @param dadoInicial String com a data ou o COO final (data no formato
     * DDMMAAAA e COO com no máximo 8 dígitos).
     * @param dadoFinal String com a data ou o COO final (data no formato
     * DDMMAAAA e COO com no máximo 8 dígitos).
     * @param usuario Não é usado na epson.
     * @return
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

        setRetornoBruto(EPSON.EPSON_Obter_Arquivos_Binarios(
                dadoInicial,
                dadoFinal,
                tipoDownload.equals("0") ? 3 : tipoDownload.equals("1") ? 0 : 2,
                null, nomeArquivo));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return Retorno.OK;
    }

    /**
     * Gera o espelho MFD.
     *
     * @param arquivoDestino Path + nome do arquivo txt a ser gerado.
     * @param tipoDownload tipo de download, onde: "D": download por data; "C":
     * download por COO.
     * @param dadoInicial Dado inicial para o download da MFD, data ou coo.
     * @param dadoFinal dado final para o download da MFD, data ou coo.
     * @param usuario Identificacao do usuario.
     * @param chavePublica Chave pública para assinatura do arquivo, com 256
     * caracteres.
     * @param chavePrivada Chave privada para assinatura do arquivo, com 256
     * caracteres.
     *
     * @return Retorno
     */
    @Override
    public Retorno espelhoMFD(String arquivoDestino, String tipoDownload, String dadoInicial, String dadoFinal, String usuario, String chavePublica, String chavePrivada) {
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

        int download = tipoDownload.equals("D") ? 0 : tipoDownload.equals("R") ? 1 : tipoDownload.equals("C") ? 2 : 3;

        arquivoDestino = arquivoDestino.replace(".txt", "");

        setRetornoBruto(EPSON.EPSON_Obter_Dados_MF_MFD(
                dadoInicial, dadoFinal,
                download,
                255, 0, 0,
                arquivoDestino));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Gera o arquivo no formato do Ato Cotepe 17/04.
     *
     * @param nomeArquivoOrigem Path + nome do arquivo de origem.
     * @param nomeArquivoDestino Path + nome do arquivo txt a ser gerado.
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
    public Retorno arquivosTextoMFD(String nomeArquivoOrigem, String nomeArquivoDestino, String tipoDownload, String dadoInicial, String dadoFinal, String usuario, int tipoGeracao, String chavePublica, String chavePrivada) {
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

        int download = tipoDownload.equals("D") ? 0 : tipoDownload.equals("R") ? 1 : tipoDownload.equals("C") ? 2 : 3;
        String mf = null, mfd = null;
        int geracao = 0;

        if (tipoGeracao == 0) {
            mf = nomeArquivoOrigem;
            geracao = 21;
        } else if (tipoGeracao == 1) {
            mfd = nomeArquivoOrigem;
            geracao = 22;
        }

        nomeArquivoDestino = nomeArquivoDestino.replace(".txt", "");

        setRetornoBruto(EPSON.EPSON_Obter_Dados_Arquivos_MF_MFD(
                dadoInicial, dadoFinal,
                download,
                0, geracao, 0,
                nomeArquivoDestino,
                mf, mfd));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    @Override
    public Retorno getFlagFiscalImpressoraMFD(CSPObjectReturnEcf poucoPapel, CSPObjectReturnEcf sensorPoucoPapel, CSPObjectReturnEcf cancAutoCFDuasHoras, CSPObjectReturnEcf descontoIssqn, CSPObjectReturnEcf rzAutomatica, CSPObjectReturnEcf onlineOffline) {
        return Retorno.OK;
    }

    /**
     * Retorna se o desconto em issqn esta hábilitado.
     *
     * @param descontoIssqn Retorna se o desconto em issqn esta hábilitado.
     *
     * @return Retorno
     */
    @Override
    public Retorno getDescontoISSQNHabilitado(CSPObjectReturnEcf descontoIssqn) {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Desconto_Iss());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            descontoIssqn.setEcfBuffer(EPSON.retornos.sArg1.trim().equals("S") ? 0 : 1);
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
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
        setRetornoBruto(EPSON.EPSON_Obter_Hora_Relogio());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            DateFormat f = new SimpleDateFormat("ddMMyyyy");
            DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
            data.setEcfBuffer(f2.format(f.parse(EPSON.retornos.sArg1.substring(0, 8).trim())));

            f = new SimpleDateFormat("HHmmss");
            f2 = new SimpleDateFormat("HH:mm:ss");
            hora.setEcfBuffer(f2.format(f.parse(EPSON.retornos.sArg1.substring(8, 14).trim())));
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna a data da jornada fiscal do ECF.
     *
     * @param data Objeto que guarda a data do movimento.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getDataMovimento(CSPObjectReturnEcf data) throws Exception {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Data_Hora_Jornada());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            DateFormat f = new SimpleDateFormat("ddMMyyyy");
            DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
            data.setEcfBuffer(f2.format(f.parse(EPSON.retornos.sArg1.substring(0, 8).trim())));
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna a data do movimento da última redução Z emitida.
     *
     * @param data Objeto que guarda a data do movimento da RZ.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getDataMovimentoUltimaReducao(CSPObjectReturnEcf data) throws Exception {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Dados_Ultima_RZ());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            if (EPSON.retornos.sArg1.length() == 1167) {
                DateFormat f = new SimpleDateFormat("ddMMyyyy");
                DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
                data.setEcfBuffer(f2.format(f.parse(EPSON.retornos.sArg1.substring(1159, 1167).trim())));
            }
        } else {
            rtrn = statusImpressora();
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
        sangria = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(sangria), 2).trim().replace(",", "");

        setRetornoBruto(EPSON.EPSON_NaoFiscal_Sangria(sangria, 2));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
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
        suprimento = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(suprimento), 2).trim().replace(",", "");

        setRetornoBruto(EPSON.EPSON_NaoFiscal_Fundo_Troco(suprimento, 2));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna o número de série da impressora.
     *
     * @param num Objeto que guarda o número de série da ECF.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getNumeroSerieECF(CSPObjectReturnEcf num) throws Exception {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Dados_Impressora());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            if (EPSON.retornos.sArg1.length() > 20) {
                num.setEcfBuffer(EPSON.retornos.sArg1.substring(0, 20).trim());
            }
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Obtém o grande total (GT) do ECF.
     *
     * @param gt String para receber o GT.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getGrandeTotal(CSPObjectReturnEcf gt) throws Exception {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_GT());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            gt.setEcfBuffer(EPSON.retornos.sArg1);
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna os dados da impressora no momento da última redução Z.
     *
     * @param dados Classe que irá guardar os dados da última RZ. RZ.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno getDadosUltimaReducaoZ(CSPDataRZ dados) throws Exception {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Dados_Ultima_RZ());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            if (EPSON.retornos.sArg1.length() == 1167) {
                String dadosRz = EPSON.retornos.sArg1;

                setRetornoBruto(EPSON.EPSON_Obter_Estado_ReducaoZ_Automatica());
                rtrn = analisaRetorno();

                if (rtrn == Retorno.OK) {
                    dados.setModoRz(EPSON.retornos.bFlag1 ? 1 : 0);
                    dados.setContadorReinicioOperacao(Integer.parseInt(dadosRz.substring(32, 38)));
                    dados.setContadorRz(Integer.parseInt(dadosRz.substring(26, 32)));
                    dados.setContadorOrdemOperacao(Integer.parseInt(dadosRz.substring(20, 26)));
                    dados.setContadorGeralOperacaoNFiscal(Integer.parseInt(dadosRz.substring(38, 44)));
                    dados.setContadorCupomFiscal(Integer.parseInt(dadosRz.substring(62, 68)));
                    dados.setContadorGeralRelatorioGerencial(Integer.parseInt(dadosRz.substring(56, 62)));
                    dados.setContadorFitaDetalheEmitida(Integer.parseInt(dadosRz.substring(74, 80)));
                    dados.setContadorOperacaoNFiscalCancelada(Integer.parseInt(dadosRz.substring(80, 86)));
                    dados.setContadorCupomFiscalCancelado(Integer.parseInt(dadosRz.substring(68, 74)));

                    {
                        String temp = dadosRz.substring(86, 104);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorGeral(Double.parseDouble(temp));
                    }
                    {
                        String acumulado = dadosRz.substring(383, 791);
                        String percentual = dadosRz.substring(257, 377);
                        String acumuladoAliquota = "0";

                        if (!acumulado.replaceAll("0", "").isEmpty() && !percentual.trim().isEmpty()) {
                            String percentualAliquota;
                            int totalizadorAliquota = 0;
                            int forAliquota = 0;
                            int idAliquota = 1;

                            for (int x = 0; x < percentual.length(); x += 5) {
                                int tempInt = Integer.parseInt(acumulado.substring(forAliquota, forAliquota + 17));
                                acumuladoAliquota = String.valueOf(tempInt);
                                percentualAliquota = percentual.substring(x, x + 5).trim();

                                if (!percentualAliquota.trim().isEmpty()) {
                                    if (tempInt > 0) {
                                        acumuladoAliquota = (tempInt < 100 ? "0" : acumuladoAliquota.substring(0, acumuladoAliquota.length() - 2))
                                                + "." + acumuladoAliquota.substring(acumuladoAliquota.length() - 2, acumuladoAliquota.length());
                                    } else {
                                        acumuladoAliquota = "0";
                                    }

                                    if (percentualAliquota.startsWith("T")) {
                                        percentualAliquota = percentualAliquota.replaceAll("[^\\d]", "");
                                        percentualAliquota = percentualAliquota.substring(0, 2) + "." + percentualAliquota.substring(2, 4);
                                        dados.getListaAliquotasTributarias()
                                                .addAliquota(
                                                        idAliquota,
                                                        Double.parseDouble(percentualAliquota),
                                                        Double.parseDouble(acumuladoAliquota),
                                                        0, pad(idAliquota + "T", 3, "0", true));
                                    } else if (percentualAliquota.startsWith("S")) {
                                        percentualAliquota = percentualAliquota.replaceAll("[^\\d]", "");
                                        percentualAliquota = percentualAliquota.substring(0, 2) + "." + percentualAliquota.substring(2, 4);
                                        dados.getListaAliquotasTributarias()
                                                .addAliquota(
                                                        idAliquota,
                                                        Double.parseDouble(percentualAliquota),
                                                        Double.parseDouble(acumuladoAliquota),
                                                        1, pad(idAliquota + "S", 3, "0", true));
                                    } else {
                                        tempInt = 0;
                                        switch (percentualAliquota) {
                                            case "F":
                                                dados.setTotalizadorSubstituicaoTributariaICMS(Double.parseDouble(acumuladoAliquota));
                                                break;
                                            case "I":
                                                dados.setTotalizadorIsencaoICMS(Double.parseDouble(acumuladoAliquota));
                                                break;
                                            case "N":
                                                dados.setTotalizadorNIncidenciaICMS(Double.parseDouble(acumuladoAliquota));
                                                break;
                                            case "FS":
                                                dados.setTotalizadorSubstituicaoTributariaISSQN(Double.parseDouble(acumuladoAliquota));
                                                break;
                                            case "IS":
                                                dados.setTotalizadorIsencaoISSQN(Double.parseDouble(acumuladoAliquota));
                                                break;
                                            case "NS":
                                                dados.setTotalizadorNIncidenciaISSQN(Double.parseDouble(acumuladoAliquota));
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                }

                                forAliquota += 17;
                                idAliquota++;
                                totalizadorAliquota += tempInt;
                            }

                            acumuladoAliquota = String.valueOf(totalizadorAliquota);
                            acumuladoAliquota = (totalizadorAliquota < 100 ? "0" : acumuladoAliquota.substring(0, acumuladoAliquota.length() - 2))
                                    + "." + acumuladoAliquota.substring(acumuladoAliquota.length() - 2, acumuladoAliquota.length());
                        }

                        dados.setTotalizadoresParciaisTributados(Double.parseDouble(acumuladoAliquota));
                    }
                    {
                        String temp = dadosRz.substring(155, 172);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorDescontoICMS(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(172, 189);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorDescontosISSQN(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(206, 223);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorAcrescimosICMS(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(223, 240);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorAcrescimosISSQN(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(104, 121);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorCancelamentosICMS(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(121, 138);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorCancelamentosISSQN(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(817, 1157);
                        int tempInt = 0;
                        int count = 0;
                        if (!temp.replaceAll("0", "").isEmpty()) {
                            for (int x = 0; x < temp.length(); x += 17) {
                                tempInt += Integer.parseInt(temp.substring(x, x + 13));
                                count += Integer.parseInt(temp.substring(x + 13, x + 17));
                            }
                            temp = String.valueOf(tempInt);
                            temp = (tempInt < 100 ? "0" : temp.substring(0, temp.length() - 2))
                                    + "." + temp.substring(temp.length() - 2, temp.length());
                        } else {
                            temp = "0";
                        }

                        dados.setContadoresEspecificosOperacoesNFiscais(count);
                        dados.setTotalizadoresParciaisNSujeitosICMS(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(189, 206);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorDescontosNFiscais(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(240, 257);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorAcrescimosNFiscais(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(138, 155);
                        temp = temp.substring(0, temp.length() - 2) + "." + temp.substring(temp.length() - 2, temp.length());
                        dados.setTotalizadorCancelamentosNFiscais(Double.parseDouble(temp));
                    }
                    {
                        String temp = dadosRz.substring(1159, 1167);
                        DateFormat f = new SimpleDateFormat("ddMMyyyy");
                        DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
                        dados.setDataMovimento(f2.format(f.parse(temp)));
                    }
                    {
                        String data = dadosRz.substring(0, 8);
                        DateFormat f = new SimpleDateFormat("ddMMyyyy");
                        DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
                        data = f2.format(f.parse(data)) + " ";

                        String hora = dadosRz.substring(8, 14);
                        f = new SimpleDateFormat("HHmmss");
                        f2 = new SimpleDateFormat("HH:mm:ss");
                        dados.setDataEmissao(data + f2.format(f.parse(hora)));
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
            }
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Abre Relatório Gerencial na impressora fiscal.
     *
     * @param indice String numérica com o com o índice do relatório. Tamanho 2.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     * @throws Exception
     */
    @Override
    public Retorno abreRelatorioGerencial(String indice, boolean pdvInoperante) throws Exception {
        Retorno rtrn;

        if (!pdvInoperante) {
            setRetornoBruto(EPSON.EPSON_NaoFiscal_Abrir_Relatorio_Gerencial(indice));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Imprime as informações do Relatório Gerencial. O Relatório Gerencial
     * precisa estar aberto. Esse comando efetua a quebra automática de linha ao
     * final de 56 caracteres da linha, e também permite a quebra de linha
     * utilizando-se os caracteres terminadores de linha 0x0D e 0x0A.
     *
     * @param msg String com o Texto a ser impresso no relatório.
     * @return
     * @throws Exception
     */
    @Override
    public Retorno usaRelatorioGerencial(String msg) throws Exception {
        Retorno rtrn = Retorno.OK;

        String[] linha = msg.split("\n");

        for (String string : linha) {
            setRetornoBruto(EPSON.EPSON_NaoFiscal_Imprimir_LinhaEX(string != null ? string : ""));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
                break;
            }
        }

//        setRetornoBruto(EPSON.EPSON_NaoFiscal_Imprimir_LinhaEX(msg));
//        rtrn = analisaRetorno();
//
//        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
//            rtrn = statusImpressora();
//        }
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
        setRetornoBruto(EPSON.EPSON_NaoFiscal_Fechar_Relatorio_Gerencial(true));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
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
        setRetornoBruto(EPSON.EPSON_Obter_Contadores());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            numeroIntervencoes.setEcfBuffer(EPSON.retornos.sArg1.substring(12, 18).trim());
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna o número de Reduções Z realizadas na impressora.
     *
     * @param numeroReducoes Variável string para receber o número de Reduções
     * Z.
     * @return Retorno
     */
    @Override
    public Retorno getNumeroReducoes(CSPObjectReturnEcf numeroReducoes) {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Contadores());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            numeroReducoes.setEcfBuffer(EPSON.retornos.sArg1.substring(6, 12).trim());
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Dados_Ultima_RZ());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            if (!EPSON.retornos.sArg1.trim().isEmpty()) {
                grandeTotalUltimaRz.setEcfBuffer(EPSON.retornos.sArg1.substring(86, 104).trim());
            }
        } else {
            rtrn = statusImpressora();
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
     *
     * @return Retorno
     */
    @Override
    public Retorno ProgramaFormaPagamento(String formaPgto, String permiteTef) {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Config_Forma_PagamentoEX(permiteTef.equals("0"), formaPgto));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Esta função retorna todos os meios de pagamentos cadastrados na
     * impressora fiscal.
     *
     * @param lista Classe para a manipulação da lista de formas de pagamento
     * retornada da impressora.
     * @return Retorno
     */
    @Override
    public Retorno getFormasPagamento(CSPListaFormasPgto lista) {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Tabela_Pagamentos());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            for (int i = 0; i < EPSON.retornos.sArg1.length(); i += 44) {
                String pgto = EPSON.retornos.sArg1.substring(i, i + 44);
                String desc = pgto.substring(2, 17).trim();
                if (!desc.isEmpty()) {
                    int id = Integer.parseInt(pgto.substring(0, 2));
                    String valCum = pgto.substring(17, 30);
                    valCum = valCum.substring(0, valCum.length() - 2) + "." + valCum.substring(valCum.length() - 2);

                    String valUltCup = pgto.substring(30, 43);
                    valUltCup = valUltCup.substring(0, valUltCup.length() - 2) + "." + valUltCup.substring(valUltCup.length() - 2);

                    lista.addFormaPgto(id, desc,
                            Double.parseDouble(valCum),
                            Double.parseDouble(valUltCup),
                            pgto.charAt(43) == 'S');
                }
            }
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Contadores());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            coo.setEcfBuffer(EPSON.retornos.sArg1.substring(0, 6).trim());
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Numero_ECF_Loja());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            caixa.setEcfBuffer(EPSON.retornos.sArg1.substring(3, 6).trim().replaceAll("[^\\d]", ""));
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Esta função retorna todos os relatórios gerenciais cadastrados na
     * impressora fiscal.
     *
     * @param rgs Classe para a manipulação da lista dos Relatórios Gerenciais
     * retornados da impressora.
     * @return Retorno
     */
    @Override
    public Retorno getRelatoriosGerenciais(CSPListaRelatorioGerencial rgs) {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Tabela_Relatorios_Gerenciais());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            for (int i = 0; i < EPSON.retornos.sArg1.length(); i += 21) {
                String rg = EPSON.retornos.sArg1.substring(i, i + 21);
                String desc = rg.substring(2, 17).trim();
                if (!desc.isEmpty()) {
                    int id = Integer.parseInt(rg.substring(0, 2));

                    rgs.addRelatorioGerencial(id, Integer.parseInt(rg.substring(17, 21)), desc);
                }
            }
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Retorna a marca, o modelo e o tipo da impressora.
     *
     * @param marca Variável para receber a marca da impressora.
     * @param modelo Variável para receber o modelo.
     * @param tipo Variável para receber o tipo da impressora.
     * @return Retorno
     */
    @Override
    public Retorno getMarcaModeloTipoImpressora(CSPObjectReturnEcf marca, CSPObjectReturnEcf modelo, CSPObjectReturnEcf tipo) {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_Obter_Dados_Impressora());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            if (EPSON.retornos.sArg1.length() > 100) {
                marca.setEcfBuffer(EPSON.retornos.sArg1.substring(40, 60).trim());
                modelo.setEcfBuffer(EPSON.retornos.sArg1.substring(60, 80).trim());
                tipo.setEcfBuffer(EPSON.retornos.sArg1.substring(80, 100).trim());
            }
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Dados_Impressora());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            if (EPSON.retornos.sArg1.length() >= 108) {
                versaoFirmware.setEcfBuffer(EPSON.retornos.sArg1.substring(100, 108).trim().replaceAll("\\.", ""));
            }
        } else {
            rtrn = statusImpressora();
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

        setRetornoBruto(EPSON.EPSON_Obter_Versao_SWBasicoEX());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            mfAdicional.setEcfBuffer("N");

            DateFormat f = new SimpleDateFormat("ddMMyyyy");
            DateFormat f2 = new SimpleDateFormat("dd.MM.yyyy");
            dataSw.setEcfBuffer(f2.format(f.parse(EPSON.retornos.sArg2)));

            f = new SimpleDateFormat("HHmmss");
            f2 = new SimpleDateFormat("HH:mm:ss");
            horaSw.setEcfBuffer(f2.format(f.parse(EPSON.retornos.sArg3)));
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Contadores());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            gnf.setEcfBuffer(EPSON.retornos.sArg1.substring(18, 24).trim());
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Contadores());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            grg.setEcfBuffer(EPSON.retornos.sArg1.substring(36, 42).trim());
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Contadores());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            cdc.setEcfBuffer(EPSON.retornos.sArg1.substring(24, 30).trim());
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Numero_Usuario());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            numOrdemUsuario.setEcfBuffer(EPSON.retornos.sArg1.trim());
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Contadores());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            ccf.setEcfBuffer(EPSON.retornos.sArg1.substring(42, 48).trim());
        } else {
            rtrn = statusImpressora();
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
        setRetornoBruto(EPSON.EPSON_Obter_Tabela_Aliquotas());
        rtrn = analisaRetorno();

        if (rtrn == Retorno.OK) {
            int x = 0;
            for (int i = 0; i < EPSON.retornos.sArg1.length(); i += 23) {
                x++;
                String aliquota = EPSON.retornos.sArg1.substring(i, i + 23);
                String desc = aliquota.substring(0, 2).trim();

                if (desc.startsWith("T")) {
                    String val = aliquota.substring(2, 6);
                    String codTot = pad(x + "T", 3, "0", true) + val;
                    val = val.substring(0, val.length() - 2) + "." + val.substring(val.length() - 2);

                    String valCum = aliquota.substring(6, 23);
                    valCum = valCum.substring(0, valCum.length() - 2) + "." + valCum.substring(valCum.length() - 2);

                    aliquotas.addAliquota(x, Double.parseDouble(val), Double.parseDouble(valCum), 0, codTot);
                } else if (desc.startsWith("S")) {
                    String val = aliquota.substring(2, 6);
                    String codTot = pad(x + "S", 3, "0", true) + val;
                    val = val.substring(0, val.length() - 2) + "." + val.substring(val.length() - 2);

                    String valCum = aliquota.substring(6, 23);
                    valCum = valCum.substring(0, valCum.length() - 2) + "." + valCum.substring(valCum.length() - 2);

                    aliquotas.addAliquota(x, Double.parseDouble(val), Double.parseDouble(valCum), 1, codTot);
                }
            }
        } else {
            rtrn = statusImpressora();
        }

        return rtrn;
    }

    /**
     * Abre o comprovante não fiscal vinculado.
     *
     * @param formaPagamento Forma de pagamento com até 15 caracteres.
     * @param valor Valor pago na forma de pagamento com até 13 dígitos (2 casas
     * decimais).
     * @param ccf Não é usado na epson.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    @Override
    public Retorno abreComprovanteNaoFiscalVinculado(String formaPagamento, String valor, String ccf, boolean pdvInoperante) {
        Retorno rtrn;

        if (!pdvInoperante) {
            /**
             * Formata o valor pago.
             */
            valor = CSPUtilidadesLang.defaultDecimalFormat(Double.valueOf(valor.replace(",", ".")), 3).trim().replace(",", "");

            setRetornoBruto(EPSON.EPSON_NaoFiscal_Abrir_CCD(formaPagamento, valor, 3, "1"));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
            }
        } else {
            rtrn = Retorno.PDV_BLOQUEADO;
        }

        return rtrn;
    }

    /**
     * Imprime o comprovante não fiscal vinculado. Esse comando efetua a quebra
     * automática de linha ao final de 56 caracteres da linha, e também permite
     * a quebra de linha utilizando-se os caracteres terminadores de linha 0x0D
     * e 0x0A.
     *
     * @param msg Texto a ser impresso no comprovante não fiscal vinculado.
     * @return Retorno
     */
    @Override
    public Retorno usaComprovanteNaoFiscalVinculado(String msg) {
        Retorno rtrn = Retorno.OK;

        String[] linha = msg.split("\n");

        for (String string : linha) {
            setRetornoBruto(EPSON.EPSON_NaoFiscal_Imprimir_LinhaEX(string));
            rtrn = analisaRetorno();

            if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
                rtrn = statusImpressora();
                break;
            }
        }

//        setRetornoBruto(EPSON.EPSON_NaoFiscal_Imprimir_LinhaEX(msg));
//        rtrn = analisaRetorno();
//
//        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
//            rtrn = statusImpressora();
//        }
        return rtrn;
    }

    /**
     * Encerra o comprovante não fiscal vinculado.
     *
     * @return Retorno
     */
    @Override
    public Retorno fechaComprovanteNaoFiscalVinculado() {
        Retorno rtrn;
        setRetornoBruto(EPSON.EPSON_NaoFiscal_Fechar_CCD(true));
        rtrn = analisaRetorno();

        if (rtrn == Retorno.ERRO_DE_EXECUCAO) {
            rtrn = statusImpressora();
        }

        return rtrn;
    }
}
