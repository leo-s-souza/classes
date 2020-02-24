package br.com.casaautomacao.casagold.classes.impressoras;

import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPObjectReturnEcf;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPDataRZ;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaAliquotas;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaFormasPgto;
import br.com.casaautomacao.casagold.classes.impressoras.retorno.CSPListaRelatorioGerencial;
import java.util.LinkedHashSet;

/**
 *
 * @author cautomacao
 */
public abstract class CSPImpressorasFiscaisBase extends CSPImpressorasBase {

    /**
     * Guarda o md5 usado para impressão fiscal.
     */
    private String md5Paf;

    /*
     * -------------------------------------------------------------------------
     * Métodos abstratos.
     * -------------------------------------------------------------------------
     */
    /**
     * Retorna a classe para montar o texto a ser impresso já com as
     * diretrizes/carateristícas da impressora em questão.
     *
     * @return
     */
    public abstract CSPImpressorasTextBuilder newTextBuilder();

    /**
     * Retorna o estado da impressora.
     *
     * @return Retorno
     */
    public abstract Retorno getEstadoImpressora();

    /**
     * Retorna o status da ECF referente a Redução Z.
     *
     * @return Retorno
     * @throws java.lang.Exception
     */
    public abstract Retorno getStatusReducaoZ() throws Exception;

    /**
     * Emite a Redução Z na impressora.
     *
     * @return
     */
    public abstract Retorno emiteReducaoZ();

    /**
     * Emite a Leitura X na impressora.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno leituraX(boolean pdvInoperante);

    /**
     * Abre o cupom fiscal na impressora.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno abreCupomFiscal(boolean pdvInoperante);

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
    public abstract Retorno fechaCupomFiscal(String formaPagamento, String acrescimoDesconto, String valorAcrescimoDesconto, String valorPago, LinkedHashSet<String> mensagem, boolean pdvInoperante);

    /**
     * Cancela o último cupom emitido.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno cancelaCupomFiscal(boolean pdvInoperante);

    /**
     * Inicia o fechamento do cupom com o uso das formas de pagamento.
     *
     * @param acrescimoDesconto Indica se haverá acréscimo ou desconto no cupom.
     * @param valorAcrescimoDesconto Valor de acréscimo ou desconto.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno iniciaFechamentoCupomFiscal(String acrescimoDesconto, String valorAcrescimoDesconto, boolean pdvInoperante);

    /**
     * Imprime a(s) forma(s) de pagamento e o(s) valor(es) pago(s).
     *
     * @param formaPagamento Forma de pagamento.
     * @param valorPagamento Valor da forma.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno efetuaFormaPagamento(String formaPagamento, String valorPagamento, boolean pdvInoperante);

    /**
     * Termina o fechamento do cupom.
     *
     * @param mensagem Mensagem promocional.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno terminaFechamentoCupomFiscal(LinkedHashSet<String> mensagem, boolean pdvInoperante);

    /**
     * Vende um item na impressora fiscal.
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
    public abstract Retorno vendeItemCupomFiscal(String codigoProduto, String descricao, String aliquota, String unidade, String quantidade, String valorUnitario, boolean arredonda, boolean pdvInoperante);

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
    public abstract Retorno acrescimoDescontoItem(String item, String acrescimoDesconto, String valorAcrescimoDesconto, String quantidade, boolean pdvInoperante);

    /**
     * Emite a leitura da memória fiscal da impressora por intervalo de datas.
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
    public abstract Retorno leituraMemoriaFiscalData(String dataInicial, String dataFinal, String tipo, boolean pdvInoperante);

    /**
     * Emite a leitura da memória fiscal da impressora por intervalo de
     * reduções.
     *
     * @param cReducaoInicial STRING com o Número da redução inicial.
     * @param cReducaoFinal STRING com o Número da redução final.
     * @param tipo
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno leituraMemoriaFiscalReducao(String cReducaoInicial, String cReducaoFinal, String tipo, boolean pdvInoperante);

    /**
     * Função para cancelamento de item.
     *
     * @param item Número do item.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno cancelaItem(String item, boolean pdvInoperante);

    /**
     * Função para cancelar o último item vendido.
     *
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     */
    public abstract Retorno cancelaItemAnterior(boolean pdvInoperante);

    /**
     * Função para download do arquivo binário da Memória Fiscal.
     *
     * @param mf Caminho do arquivo
     * @return Retorno
     */
    public abstract Retorno downloadMF(String mf);

    /**
     * Gera os dados da MF (Memória Fiscal) em formato TXT ou RTF.
     *
     * @param origem String com o caminho+nome do arquivo MF contendo o download
     * dos dados da memória fiscal.
     * @param destino String com o caminho+nome do arquivo que será gerado.
     * @param formato String com o tipo do formato de dados, onde: "0" (zero)
     * para formato .TXT (é criado um arquivo TXT com a imagem dos cupons
     * referentes ao movimento) e "1" (um) para formato .RTF (é criado um
     * arquivo RTF com a imagem dos cupons referentes ao movimento).
     * @param tipoLeitura String com o tipo da leitura da MF, onde: "C" =
     * completa e "S" = simplificada.
     * @param tipoParametro String com o tipo de download, onde: "D" = download
     * por data e "C" = download por CRZ (Contador de Redução Z).
     * @param dadoInicial String com a data ou o CRZ inicial (data no formato
     * DDMMAA ou DDMMAAAA, CRZ com no máximo 6 dígitos).
     * @param dadoFinal String com a data ou o CRZ final (data no formato DDMMAA
     * ou DDMMAAAA, CRZ com no máximo 6 dígitos).
     * @return Retorno
     */
    //public abstract Retorno formataDadosMF(String origem, String destino, String formato, String tipoLeitura, String tipoParametro, String dadoInicial, String dadoFinal);
    /**
     * Função para download do arquivo binário da Memória de Fita Detalhe.
     *
     * @param nomeArquivo nome do arquivo que será gerado, exemplo:
     * "DOWNLOAD.MFD".
     * @param tipoDownload String para o tipo de downlod do mfd.
     * @param dadoInicial Data ou COO inicial.
     * @param dadoFinal Data ou COO final.
     * @param usuario com o número de ordem do proprietário do ECF.
     * @return
     */
    public abstract Retorno downloadMFD(String nomeArquivo, String tipoDownload, String dadoInicial, String dadoFinal, String usuario);

    /**
     * Gera os dados da MFD (Memória de Fita Detalhe) em formato TXT, RTF ou
     * MDB.
     *
     * @param arquivoMFD String com o caminho+nome do arquivo MFD contendo o
     * download dos dados da MFD.
     * @param destino String com o caminho+nome do arquivo que será gerado.
     * @param formato String com o tipo do formato de dados, onde: "0" (zero)
     * para formato .TXT (é criado um arquivo TXT com a imagem dos cupons
     * referentes ao movimento), "1" (um) para formato .RTF (é criado um arquivo
     * RTF com a imagem dos cupons referentes ao movimento) E "2" (dois) para
     * formato .MDB (é criado um arquivo MDB com tabelas referentes ao
     * movimento).
     * @param tipoDownload String com o tipo de download, onde: "0" (zero):
     * download total, "1" (um): download por data e "2" (dois): download por
     * COO.
     * @param dadoInicial String com a data ou o CRZ inicial (data no formato
     * DDMMAA ou DDMMAAAA, CRZ com no máximo 6 dígitos).
     * @param dadoFinal String com a data ou o CRZ final (data no formato DDMMAA
     * ou DDMMAAAA, CRZ com no máximo 6 dígitos).
     * @param usuario String com o número de ordem do proprietário do ECF,
     * exemplo: primeiro proprietário "cUsuario = 1". Pois para cada
     * proprietário o COO dos cupons serão diferentes.
     *
     * @return Retorno
     */
    //public abstract Retorno formataDadosMFD(String arquivoMFD, String destino, String formato, String tipoDownload, String dadoInicial, String dadoFinal, String usuario);
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
    public abstract Retorno espelhoMFD(String arquivoDestino, String tipoDownload, String dadoInicial, String dadoFinal, String usuario, String chavePublica, String chavePrivada);

    /**
     * Gera o arquivo no formato do Ato Cotepe 17/04.
     *
     * @param nomeArquivoOrigem Path + nome do arquivo de origem.
     * @param cNomeArquivoDestino
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
    public abstract Retorno arquivosTextoMFD(String nomeArquivoOrigem, String cNomeArquivoDestino, String tipoDownload, String dadoInicial, String dadoFinal, String usuario, int tipoGeracao, String chavePublica, String chavePrivada);

    /**
     * Retorna os flags fiscais III das impressoras fiscais térmicas.
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
    public abstract Retorno getFlagFiscalImpressoraMFD(CSPObjectReturnEcf poucoPapel, CSPObjectReturnEcf sensorPoucoPapel, CSPObjectReturnEcf cancAutoCFDuasHoras, CSPObjectReturnEcf descontoIssqn, CSPObjectReturnEcf rzAutomatica, CSPObjectReturnEcf onlineOffline);

    /**
     * Retorna se o desconto em issqn esta hábilitado.
     *
     * @param descontoIssqn Retorna se o desconto em issqn esta hábilitado.
     *
     * @return Retorno
     */
    public abstract Retorno getDescontoISSQNHabilitado(CSPObjectReturnEcf descontoIssqn);

    /**
     * Retorna a data da última Redução Z.
     *
     * @param data Objeto que guarda a data da RZ impressora.
     * @param hora Objeto que guarda a hora da RZ impressora.
     * @return Date
     * @throws java.text.ParseException
     */
    //public abstract Retorno getDataHoraReducao(CSPObjectReturnEcf data, CSPObjectReturnEcf hora) throws Exception;
    /**
     * Retorna a data e a hora atual da impressora.
     *
     * @param data Objeto que guarda a data da impressora.
     * @param hora Objeto que guarda a hora da impressora.
     * @return Retorno
     * @throws java.lang.Exception
     */
    public abstract Retorno getDataHoraImpressora(CSPObjectReturnEcf data, CSPObjectReturnEcf hora) throws Exception;

    /**
     * Retorna a data do último movimento.
     *
     * @param data Objeto que guarda a data do movimento.
     * @return Retorno
     * @throws Exception
     */
    public abstract Retorno getDataMovimento(CSPObjectReturnEcf data) throws Exception;

    /**
     * Retorna a data do movimento da última redução Z.
     *
     * @param data Objeto que guarda a data do movimento da RZ.
     * @return Retorno
     * @throws Exception
     */
    public abstract Retorno getDataMovimentoUltimaReducao(CSPObjectReturnEcf data) throws Exception;

    /**
     * Faz uma sangria na impressora (retirada de dinheiro).
     *
     * @param sangria STRING com o Valor da sangria.
     * @param pdvInoperante Boolean informando se o pdv esta inoperante, caso
     * esteja, a função não deve ser executada.
     * @return Retorno
     * @throws Exception
     */
    public abstract Retorno fazSangria(String sangria, boolean pdvInoperante) throws Exception;

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
    public abstract Retorno fazSuprimento(String suprimento, String forma, boolean pdvInoperante) throws Exception;

    /**
     * Retorna o número de série da impressora.
     *
     * @param num Objeto que guarda o número de série da ECF.
     * @return Retorno
     * @throws Exception
     */
    public abstract Retorno getNumeroSerieECF(CSPObjectReturnEcf num) throws Exception;

    /**
     * Obtém o grande total (GT) do ECF.
     *
     * @param gt String para receber o GT.
     * @return
     * @throws Exception
     */
    public abstract Retorno getGrandeTotal(CSPObjectReturnEcf gt) throws Exception;

    /**
     * Retorna os dados da impressora no momento da última redução Z.
     *
     * @param dados Classe que irá guardar os dados da última RZ. RZ.
     * @return Retorno
     * @throws Exception
     */
    public abstract Retorno getDadosUltimaReducaoZ(CSPDataRZ dados) throws Exception;

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
    public abstract Retorno abreRelatorioGerencial(String indice, boolean pdvInoperante) throws Exception;

    /**
     * Imprime as informações do Relatório Gerencial. O Relatório Gerencial
     * precisa estar aberto.
     *
     * @param msg String com o Texto a ser impresso no relatório. Bematech: até
     * 618 caracteres.
     * @return
     * @throws Exception
     */
    public abstract Retorno usaRelatorioGerencial(String msg) throws Exception;

    /**
     * Encerra o relatório gerencial.
     *
     * @return Retorno
     * @throws Exception
     */
    public abstract Retorno fechaRelatorioGerencial() throws Exception;

    /**
     * Retorna o número de intervenções técnicas realizadas na impressora.
     *
     * @param numeroIntervencoes Variável string para receber o número de
     * intervenções.
     * @return Retorno
     */
    public abstract Retorno getNumeroIntervencoes(CSPObjectReturnEcf numeroIntervencoes);

    /**
     * Retorna o número de reduções Z realizadas na impressora.
     *
     * @param numeroReducoes Variável string para receber o número de Reduções
     * Z.
     * @return Retorno
     */
    public abstract Retorno getNumeroReducoes(CSPObjectReturnEcf numeroReducoes);

    /**
     * Retorna o grande total (GT) da última redução Z.
     *
     * @param grandeTotalUltimaRz Variável string para receber a informação.
     * @return Retorno
     */
    public abstract Retorno getGrandeTotalUltimaReducao(CSPObjectReturnEcf grandeTotalUltimaRz);

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
    public abstract Retorno ProgramaFormaPagamento(String formaPgto, String permiteTef);

    /**
     * Retorna o valor acumulado em uma determinada forma de pagamento.
     *
     * @param formaPgto Variável String com a descrição da Forma de Pagamento
     * que deseja retornar o seu valor.
     * @param valor Objeto para receber a informação do valor acumulado.
     * @return Retorno
     */
    //public abstract Retorno getValorFormaPagamentoMFD(String formaPgto, CSPObjectReturnEcf valor);
    /**
     * Retorna as formas de pagamento e seus valores acumulados.
     *
     * @param lista Classe para a manipulação da lista de formas de pagamento
     * retornada da impressora.
     * @return Retorno
     */
    public abstract Retorno getFormasPagamento(CSPListaFormasPgto lista);

    /**
     * Retorna o número do cupom.
     *
     * @param coo Variável para receber o número do último cupom.
     * @return Retorno
     */
    public abstract Retorno getCOO(CSPObjectReturnEcf coo);

    /**
     * Retorna o número do caixa cadastrado na impressora.
     *
     * @param caixa Variável para receber o número do caixa.
     * @return Retorno
     */
    public abstract Retorno getNumeroCaixa(CSPObjectReturnEcf caixa);

    /**
     * Retorna os Relatórios Gerenciais da impressora.
     *
     * @param rgs Classe para a manipulação da lista dos Relatórios Gerenciais
     * retornados da impressora.
     * @return Retorno
     */
    public abstract Retorno getRelatoriosGerenciais(CSPListaRelatorioGerencial rgs);

    /**
     * Retorna a marca, o modelo e o tipo da impressora.
     *
     * @param marca Variável para receber a marca da impressora.
     * @param modelo Variável para receber o modelo
     * @param tipo Variável para receber o tipo da impressora
     * @return Retorno
     */
    public abstract Retorno getMarcaModeloTipoImpressora(CSPObjectReturnEcf marca, CSPObjectReturnEcf modelo, CSPObjectReturnEcf tipo);

    /**
     * Retorna a versão do firmware da impressora.
     *
     * @param versaoFirmware Variável para receber a informação.
     * @return Retorno
     */
    public abstract Retorno getVersaoFirmware(CSPObjectReturnEcf versaoFirmware);

    /**
     * Retorna o modelo da impressora.
     *
     * @param modeloImpressora Variável para receber a informação.
     * @return Retorno
     */
    //public abstract Retorno getModeloImpressora(CSPObjectReturnEcf modeloImpressora);
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
    public abstract Retorno getDataHoraSWMFAdicional(CSPObjectReturnEcf dataSw, CSPObjectReturnEcf horaSw, CSPObjectReturnEcf mfAdicional) throws Exception;

    /**
     * Retorna o número de operações não fiscais executadas na impressora.
     *
     * @param gnf Variável para receber o número do gnf.
     * @return Retorno
     */
    public abstract Retorno getGNF(CSPObjectReturnEcf gnf);

    /**
     * Retorna o número de relatórios gerenciais emitidos.
     *
     * @param grg Variável para receber o número do grg.
     * @return Retorno
     */
    public abstract Retorno getGRG(CSPObjectReturnEcf grg);

    /**
     * Retorna o número de comprovantes de crédito emitidos.
     *
     * @param cdc Variável para receber o número do cdc.
     * @return Retorno
     */
    public abstract Retorno getCDC(CSPObjectReturnEcf cdc);

    /**
     * Retorna o número de substituições de proprietário (ordem do usuário).
     *
     * @param numOrdemUsuario Variável para receber o número de ordem do usuário
     * do ECF.
     * @return Retorno
     */
    public abstract Retorno getNumOrdemUsuario(CSPObjectReturnEcf numOrdemUsuario);

    /**
     * Retorna o número de cupons fiscais emitidos.
     *
     * @param ccf Variável para receber o número do ccf.
     * @return Retorno
     */
    public abstract Retorno getCCF(CSPObjectReturnEcf ccf);

    /**
     * Retorna as alíquotas cadastradas na impressora.
     *
     * @param aliquotas
     * @return Retorno
     */
    public abstract Retorno getAliquotas(CSPListaAliquotas aliquotas);

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
    public abstract Retorno abreComprovanteNaoFiscalVinculado(String formaPagamento, String valor, String ccf, boolean pdvInoperante);

    /**
     * Imprime o comprovante não fiscal vinculado.
     *
     * @param msg Texto a ser impresso no comprovante não fiscal vinculado com
     * até 618 caracteres.
     * @return Retorno
     */
    public abstract Retorno usaComprovanteNaoFiscalVinculado(String msg);

    /**
     * Encerra o comprovante não fiscal vinculado.
     *
     * @return Retorno
     */
    public abstract Retorno fechaComprovanteNaoFiscalVinculado();

    /*
     * -------------------------------------------------------------------------
     * Getter e Setters
     * -------------------------------------------------------------------------
     */
    /**
     * Retorna o MD5 do arquivo paf para a impressora fiscal.
     *
     * @return String
     * @see md5Paf
     */
    public String getMd5Paf() {
        return md5Paf;
    }

    /**
     * Seta o MD5 do arquivo paf para a impressora fiscal.
     *
     * @param md5Paf
     * @see md5Paf
     */
    public void setMd5Paf(String md5Paf) {
        this.md5Paf = md5Paf;
    }
}
