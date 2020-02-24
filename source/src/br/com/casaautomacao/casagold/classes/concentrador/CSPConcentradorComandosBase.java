/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.concentrador;

import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;

/**
 * Classe utilizada para controle e tratamento dos comandos enviados ao
 * concentrados e suas respostas.
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public abstract class CSPConcentradorComandosBase extends CSPConcentradorBase {

    /**
     * Envia comandos para o concentrador.
     *
     * @param tipo Comandos - comando a ser executado.
     * @param conn CSPInstrucoesSQLBase - caso o comando tenha que gravar ou
     * pegar alguma informação na base.
     * @return
     * @throws Exception
     */
    public abstract Object enviaComandoConcentrador(Comandos tipo, CSPInstrucoesSQLBase conn) throws Exception;

    /**
     * Comandos suportados pelo concentrador.
     */
    public enum Comandos {
        /**
         * Comando utilizado para ler os abastecimentos da memória da placa.
         */
        ABASTECIMENTOS(1, true),
        /**
         * Comando utilizado para montar os relatórios de encerrantes tipo
         * Alinea(a).
         */
        RELATORIO_ENCERRANTES_ALINEA_A(2, false),
        /**
         * Comando utilizado para montar os relatórios de encerrantes tipo
         * Alinea(b). Alinea B1 é utilizado antes de fazer a RZ da Impressora
         * Fiscal.
         */
        RELATORIO_ENCERRANTES_ALINEA_B1(2, false),
        /**
         * Comando utilizado para montar os relatórios de encerrantes tipo
         * Alinea(b). Alinea B2 é utilizado após de fazer a RZ da Impressora
         * Fiscal em caso de RZ pendente.
         */
        RELATORIO_ENCERRANTES_ALINEA_B2(2, false),
        /**
         * Comando utilizado para montar os relatórios de encerrantes tipo
         * Alinea(C).
         */
        RELATORIO_ENCERRANTES_ALINEA_C(2, false),
        /**
         * Comando utilizado para verificar conexão com porta com.
         */
        VERIFICA_COM(100, true);

        /**
         * Guarda o valor do Enum para os comandos enviados ao concentrador.
         */
        private final int valor;

        /**
         * Guarda o valor do Enum para os comandos enviados ao concentrador.
         */
        private final boolean needStart;

        /**
         * Retorna o valor do Enum referente aos comandos enviados ao
         * concentrador.
         *
         * @return String
         * @see #valor
         */
        public int getValor() {
            return this.valor;
        }

        /**
         * Retorna se é necessário usar o método start para enviar comando ao
         * concentrador.
         *
         * @return String
         * @see #valor
         */
        public boolean isNeedStart() {
            return needStart;
        }

        /**
         * Construtor do Enum.
         *
         * @param valor
         */
        private Comandos(int valor, boolean needStart) {
            this.valor = valor;
            this.needStart = needStart;
        }
    }
}
