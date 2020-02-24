/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.retorno;

/**
 * Classe que guarda os dados da última RZ emitida.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPDataRZ {

    private int modoRz;
    private int contadorReinicioOperacao;
    private int contadorRz;
    private int contadorOrdemOperacao;
    private int contadorGeralOperacaoNFiscal;
    private int contadorCupomFiscal;
    private int contadorGeralRelatorioGerencial;
    private int contadorFitaDetalheEmitida;
    private int contadorOperacaoNFiscalCancelada;
    private int contadorCupomFiscalCancelado;
    private int contadoresEspecificosOperacoesNFiscais;
    private double totalizadorGeral;
    private double vendaBruta;
    private double totalizadoresParciaisTributados;
    private double totalizadorIsencaoICMS;
    private double totalizadorNIncidenciaICMS;
    private double totalizadorSubstituicaoTributariaICMS;
    private double totalizadorIsencaoISSQN;
    private double totalizadorNIncidenciaISSQN;
    private double totalizadorSubstituicaoTributariaISSQN;
    private double totalizadorDescontoICMS;
    private double totalizadorDescontosISSQN;
    private double totalizadorAcrescimosICMS;
    private double totalizadorAcrescimosISSQN;
    private double totalizadorCancelamentosICMS;
    private double totalizadorCancelamentosISSQN;
    private double totalizadoresParciaisNSujeitosICMS;
    private double totalizadorDescontosNFiscais;
    private double totalizadorAcrescimosNFiscais;
    private double totalizadorCancelamentosNFiscais;
    private String dataMovimento;
    private String dataEmissao;
    private final CSPListaAliquotas listaAliquotasTributarias = new CSPListaAliquotas();
    private final CSPListaAliquotas listaAliquotasNTributadas = new CSPListaAliquotas();

    public void setModoRz(int modoRz) {
        this.modoRz = modoRz;
    }

    public void setContadorReinicioOperacao(int contadorReinicioOperacao) {
        this.contadorReinicioOperacao = contadorReinicioOperacao;
    }

    public void setContadorRz(int contadorRz) {
        this.contadorRz = contadorRz;
    }

    public void setContadorOrdemOperacao(int contadorOrdemOperacao) {
        this.contadorOrdemOperacao = contadorOrdemOperacao;
    }

    public void setContadorGeralOperacaoNFiscal(int contadorGeralOperacaoNFiscal) {
        this.contadorGeralOperacaoNFiscal = contadorGeralOperacaoNFiscal;
    }

    public void setContadorCupomFiscal(int contadorCupomFiscal) {
        this.contadorCupomFiscal = contadorCupomFiscal;
    }

    public void setContadorGeralRelatorioGerencial(int contadorGeralRelatorioGerencial) {
        this.contadorGeralRelatorioGerencial = contadorGeralRelatorioGerencial;
    }

    public void setContadorFitaDetalheEmitida(int contadorFitaDetalheEmitida) {
        this.contadorFitaDetalheEmitida = contadorFitaDetalheEmitida;
    }

    public void setContadorOperacaoNFiscalCancelada(int contadorOperacaoNFiscalCancelada) {
        this.contadorOperacaoNFiscalCancelada = contadorOperacaoNFiscalCancelada;
    }

    public void setContadorCupomFiscalCancelado(int contadorCupomFiscalCancelado) {
        this.contadorCupomFiscalCancelado = contadorCupomFiscalCancelado;
    }

    public void setContadoresEspecificosOperacoesNFiscais(int contadoresEspecificosOperacoesNFiscais) {
        this.contadoresEspecificosOperacoesNFiscais = contadoresEspecificosOperacoesNFiscais;
    }

    public void setTotalizadorGeral(double totalizadorGeral) {
        this.totalizadorGeral = totalizadorGeral;
    }

    public void setVendaBruta(double vendaBruta) {
        this.vendaBruta = vendaBruta;
    }

    public void setTotalizadoresParciaisTributados(double totalizadoresParciaisTributados) {
        this.totalizadoresParciaisTributados = totalizadoresParciaisTributados;
    }

    public void setTotalizadorIsencaoICMS(double totalizadorIsencaoICMS) {
        this.totalizadorIsencaoICMS = totalizadorIsencaoICMS;
    }

    public void setTotalizadorNIncidenciaICMS(double totalizadorNIncidenciaICMS) {
        this.totalizadorNIncidenciaICMS = totalizadorNIncidenciaICMS;
    }

    public void setTotalizadorSubstituicaoTributariaICMS(double totalizadorSubstituicaoTributariaICMS) {
        this.totalizadorSubstituicaoTributariaICMS = totalizadorSubstituicaoTributariaICMS;
    }

    public void setTotalizadorIsencaoISSQN(double totalizadorIsencaoISSQN) {
        this.totalizadorIsencaoISSQN = totalizadorIsencaoISSQN;
    }

    public void setTotalizadorNIncidenciaISSQN(double totalizadorNIncidenciaISSQN) {
        this.totalizadorNIncidenciaISSQN = totalizadorNIncidenciaISSQN;
    }

    public void setTotalizadorSubstituicaoTributariaISSQN(double totalizadorSubstituicaoTributariaISSQN) {
        this.totalizadorSubstituicaoTributariaISSQN = totalizadorSubstituicaoTributariaISSQN;
    }

    public void setTotalizadorDescontoICMS(double totalizadorDescontoICMS) {
        this.totalizadorDescontoICMS = totalizadorDescontoICMS;
    }

    public void setTotalizadorDescontosISSQN(double totalizadorDescontosISSQN) {
        this.totalizadorDescontosISSQN = totalizadorDescontosISSQN;
    }

    public void setTotalizadorAcrescimosICMS(double totalizadorAcrescimosICMS) {
        this.totalizadorAcrescimosICMS = totalizadorAcrescimosICMS;
    }

    public void setTotalizadorAcrescimosISSQN(double totalizadorAcrescimosISSQN) {
        this.totalizadorAcrescimosISSQN = totalizadorAcrescimosISSQN;
    }

    public void setTotalizadorCancelamentosICMS(double totalizadorCancelamentosICMS) {
        this.totalizadorCancelamentosICMS = totalizadorCancelamentosICMS;
    }

    public void setTotalizadorCancelamentosISSQN(double totalizadorCancelamentosISSQN) {
        this.totalizadorCancelamentosISSQN = totalizadorCancelamentosISSQN;
    }

    public void setTotalizadoresParciaisNSujeitosICMS(double totalizadoresParciaisNSujeitosICMS) {
        this.totalizadoresParciaisNSujeitosICMS = totalizadoresParciaisNSujeitosICMS;
    }

    public void setTotalizadorDescontosNFiscais(double totalizadorDescontosNFiscais) {
        this.totalizadorDescontosNFiscais = totalizadorDescontosNFiscais;
    }

    public void setTotalizadorAcrescimosNFiscais(double totalizadorAcrescimosNFiscais) {
        this.totalizadorAcrescimosNFiscais = totalizadorAcrescimosNFiscais;
    }

    public void setTotalizadorCancelamentosNFiscais(double totalizadorCancelamentosNFiscais) {
        this.totalizadorCancelamentosNFiscais = totalizadorCancelamentosNFiscais;
    }

    public void setDataMovimento(String dataMovimento) {
        this.dataMovimento = dataMovimento;
    }

    public void setDataEmissao(String dataEmissao) {
        this.dataEmissao = dataEmissao;
    }

    public int getModoRz() {
        return modoRz;
    }

    public int getContadorReinicioOperacao() {
        return contadorReinicioOperacao;
    }

    public int getContadorRz() {
        return contadorRz;
    }

    public int getContadorOrdemOperacao() {
        return contadorOrdemOperacao;
    }

    public int getContadorGeralOperacaoNFiscal() {
        return contadorGeralOperacaoNFiscal;
    }

    public int getContadorCupomFiscal() {
        return contadorCupomFiscal;
    }

    public int getContadorGeralRelatorioGerencial() {
        return contadorGeralRelatorioGerencial;
    }

    public int getContadorFitaDetalheEmitida() {
        return contadorFitaDetalheEmitida;
    }

    public int getContadorOperacaoNFiscalCancelada() {
        return contadorOperacaoNFiscalCancelada;
    }

    public int getContadorCupomFiscalCancelado() {
        return contadorCupomFiscalCancelado;
    }

    public int getContadoresEspecificosOperacoesNFiscais() {
        return contadoresEspecificosOperacoesNFiscais;
    }

    public double getTotalizadorGeral() {
        return totalizadorGeral;
    }

    public double getVendaBruta() {
        return vendaBruta;
    }

    public double getTotalizadoresParciaisTributados() {
        return totalizadoresParciaisTributados;
    }

    public double getTotalizadorIsencaoICMS() {
        return totalizadorIsencaoICMS;
    }

    public double getTotalizadorNIncidenciaICMS() {
        return totalizadorNIncidenciaICMS;
    }

    public double getTotalizadorSubstituicaoTributariaICMS() {
        return totalizadorSubstituicaoTributariaICMS;
    }

    public double getTotalizadorIsencaoISSQN() {
        return totalizadorIsencaoISSQN;
    }

    public double getTotalizadorNIncidenciaISSQN() {
        return totalizadorNIncidenciaISSQN;
    }

    public double getTotalizadorSubstituicaoTributariaISSQN() {
        return totalizadorSubstituicaoTributariaISSQN;
    }

    public double getTotalizadorDescontoICMS() {
        return totalizadorDescontoICMS;
    }

    public double getTotalizadorDescontosISSQN() {
        return totalizadorDescontosISSQN;
    }

    public double getTotalizadorAcrescimosICMS() {
        return totalizadorAcrescimosICMS;
    }

    public double getTotalizadorAcrescimosISSQN() {
        return totalizadorAcrescimosISSQN;
    }

    public double getTotalizadorCancelamentosICMS() {
        return totalizadorCancelamentosICMS;
    }

    public double getTotalizadorCancelamentosISSQN() {
        return totalizadorCancelamentosISSQN;
    }

    public double getTotalizadoresParciaisNSujeitosICMS() {
        return totalizadoresParciaisNSujeitosICMS;
    }

    public double getTotalizadorDescontosNFiscais() {
        return totalizadorDescontosNFiscais;
    }

    public double getTotalizadorAcrescimosNFiscais() {
        return totalizadorAcrescimosNFiscais;
    }

    public double getTotalizadorCancelamentosNFiscais() {
        return totalizadorCancelamentosNFiscais;
    }

    public String getDataMovimento() {
        return dataMovimento;
    }

    public String getDataEmissao() {
        return dataEmissao;
    }

    public CSPListaAliquotas getListaAliquotasTributarias() {
        return listaAliquotasTributarias;
    }

    public CSPListaAliquotas getListaAliquotasNTributadas() {
        return listaAliquotasNTributadas;
    }
}
