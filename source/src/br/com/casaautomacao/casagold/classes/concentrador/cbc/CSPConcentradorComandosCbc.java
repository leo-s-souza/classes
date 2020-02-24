/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.concentrador.cbc;

import static br.com.casaautomacao.casagold.classes.CSPLog.info;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.concentrador.CSPConcentradorComandosBase;
import br.com.casaautomacao.casagold.classes.concentrador.CSPConcentradorComunicacao;
import br.com.casaautomacao.casagold.classes.concentrador.CSPObjectReturnConcentrador;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Clase utilizada para tratar comandos e respostas dos concentradores CBC.
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 */
public class CSPConcentradorComandosCbc extends CSPConcentradorComandosBase {

    public CSPConcentradorComandosCbc() throws Exception {
        this.setCom(new CSPConcentradorComunicacao(this));
    }

    @Override
    public Object enviaComandoConcentrador(Comandos tipo, CSPInstrucoesSQLBase conn) throws Exception {
        if (tipo.isNeedStart() && getPorta() == null) {
            this.start(this);
        }

        CSPObjectReturnConcentrador retorno = new CSPObjectReturnConcentrador();

        switch (tipo.getValor()) {
            case 1:
                retorno.setConcentradorBuffer(enviaComandoAbastecimento(conn));
                break;
            case 2:
                retorno.setConcentradorBuffer(geraRelatorioEncerrantes(tipo));
                break;
            case 100:
                retorno.setConcentradorBuffer(verificaCom());
                break;
        }

        return retorno.getConcentradorBuffer();
    }

    /**
     * Envia o comando para o concentrador, recebe a resposta e separa os dados.
     *
     * @return
     */
    private boolean enviaComandoAbastecimento(CSPInstrucoesSQLBase conn) throws Exception {

        if (getPorta() == null) {
            info("Concentrador não encontrado");
            return false;
        }

        /**
         * Comando: (&A)
         *
         * Resposta: (TTTTTTLLLLLLPPPPVVCCCCBBDDHHMMNNRRRREEEEEEEEEESSKK) ou
         * “(0)” se nenhum abastecimento na memória.
         */
        String resposta = getCom().enviaComando("(&A)", 1000);

        if (resposta.equals("(0)")) {
            return false;
        }

        resposta = resposta.replace("(", "");
        resposta = resposta.replace(")", "");

        LinkedHashMap<String, Object> dados = new LinkedHashMap<>();

        /**
         * TTTTTT: Total a Pagar; (bombas mecânicas retornam “000000”);
         */
        dados.put("TOTAL_PAGAR", CSPUtilidadesLang.substring(resposta, 0, 6));
        /**
         * LLLLLL: Volume abastecido (Litros);
         */
        dados.put("VOLUME_ABASTECIDO", CSPUtilidadesLang.substring(resposta, 6, 12));
        /**
         * PPPP: Preço unitário;
         */
        dados.put("PRECO_UNITARIO", CSPUtilidadesLang.substring(resposta, 12, 16));
        /**
         * VV: Código de vírgula (aplicável aos campos T, L e P);
         */
        dados.put("CODIGO_VIRGULA", CSPUtilidadesLang.substring(resposta, 16, 18));
        /**
         * CCCC: Tempo de abastecimento (Hexadecimal);
         */
        dados.put("TEMPO_ABASTECIMENTO", CSPUtilidadesLang.substring(resposta, 18, 22));
        /**
         * BB: Código de bico;
         */
        dados.put("CODIGO_BICO", CSPUtilidadesLang.substring(resposta, 22, 24));
        /**
         * DD: Dia;
         */
        dados.put("DIA", CSPUtilidadesLang.substring(resposta, 24, 26));
        /**
         * HH: Hora;
         */
        dados.put("HORA", CSPUtilidadesLang.substring(resposta, 26, 28));
        /**
         * MM: Minuto;
         */
        dados.put("MINUTO", CSPUtilidadesLang.substring(resposta, 28, 30));
        /**
         * NN: Mês;
         */
        dados.put("MES", CSPUtilidadesLang.substring(resposta, 30, 32));
        /**
         * RRRR: Número do abastecimento;
         */
        dados.put("NUMERO_ABASTECIMENTO", CSPUtilidadesLang.substring(resposta, 32, 36));
        /**
         * EEEEEEEEEE: Encerrante do bico (com duas casas decimais);
         */
        dados.put("ENCERRANTE_BICO", CSPUtilidadesLang.substring(resposta, 36, 46));
        /**
         * SS: Status de integridade de memória se diferente de zero existe erro
         * (00=Ok);
         */
        dados.put("STATUS_INTEGRIDADE", CSPUtilidadesLang.substring(resposta, 46, 48));
        /**
         * KK: Checksum
         */
        dados.put("CHECKSUM", CSPUtilidadesLang.substring(resposta, 48, 50));

        trataComandoAbastecimento(dados, conn);

        return true;
    }

    private void trataComandoAbastecimento(LinkedHashMap<String, Object> dados, CSPInstrucoesSQLBase conn) throws Exception {
        String time = CSPUtilidadesLangDateTime.getTimeSamp();
        String data = CSPUtilidadesLang.substring(time, 0, 8) + dados.get("DIA");
        StringBuilder insert = new StringBuilder("INSERT INTO ABASTECIDA (ID_BICO_CONCENTRADOR,QUANTIDADE,VALOR_TOTAL,INSTANTE_ABASTECIDA, ENCERRANTE_FINAL ,BICO_ID) VALUES (");

        insert.append("'").append(dados.get("CODIGO_BICO")).append("',");
        insert.append("'").append(new StringBuilder(dados.get("VOLUME_ABASTECIDO").toString()).insert(dados.get("VOLUME_ABASTECIDO").toString().length() - 2, '.')).append("',");
        insert.append("'").append(new StringBuilder(dados.get("TOTAL_PAGAR").toString()).insert(dados.get("TOTAL_PAGAR").toString().length() - 2, '.')).append("',");
        insert.append("'").append(data).append(", ").append(dados.get("HORA")).append(":").append(dados.get("MINUTO")).append(":00:000").append("',");
        insert.append("'").append(new StringBuilder(dados.get("ENCERRANTE_BICO").toString()).insert(dados.get("ENCERRANTE_BICO").toString().length() - 1, '.')).append("',");

        ResultSet select = conn.select((sb) -> {
            sb.append("SELECT ");
            sb.append("    b.ID ");
            sb.append("FROM ");
            sb.append("    BICO b ");
            sb.append("WHERE ");
            sb.append("    b.BICO_CONCENTRADOR_ID = ? ");
        }, dados.get("CODIGO_BICO"));

        if (select.next()) {
            insert.append("'").append(select.getString("ID")).append("')");
        } else {
            insert.append("null").append(")");
        }
        conn.execute(insert.toString());

        try (ResultSet rs = conn.select("SELECT * FROM VW_INFOS_REGISTRO_C2 ORDER BY ID_ABASTECIDA DESC")) {
            if (rs.next()) {
                String cript = DigestUtils.sha256Hex(
                        new StringBuilder()
                                .append(rs.getString("ID_ABASTECIDA"))
                                .append(rs.getString("ID_TANQUE"))
                                .append(rs.getString("ID_BOMBA"))
                                .append(rs.getString("ID_BICO"))
                                .append(rs.getString("COMBUSTIVEL"))
                                .append(rs.getString("INSTANTE_ABASTECIDA"))
                                .append(rs.getString("ENCERRANTE_INICIAL"))
                                .append(rs.getString("ENCERRANTE_FINAL"))
                                .append(rs.getString("STATUS"))
                                .append(rs.getString("NUMERO_SERIE"))
                                .append(rs.getString("ABERTURA_HORARIO"))
                                .append(rs.getString("COO"))
                                .append(rs.getString("QUANTIDADE")).toString()
                );

                conn.insertComposto(false, "ABASTECIDA", new HashMap() {
                    {
                        put("CRIPT_ABASTECIDA", cript);
                    }
                }, "ID = ?", rs.getString("ID_ABASTECIDA"));
            }
        }

        /**
         * Envia comando de incremento para o concentrador.
         */
        enviaComandoIncremento();
    }

    /**
     * Comando: (&I)
     *
     * Comando utilizado para mover o ponteiro de leitura para o próximo
     * Abastecimento. O comando de incremento não retorna resposta.
     */
    private void enviaComandoIncremento() throws Exception {
        getCom().enviaComando("(&I)", 500);
    }

    /**
     * Gera as informações dos relatórios de encerrantes dos bicos cadastrados
     * no sistema.
     *
     * @return
     * @throws Exception
     */
    private LinkedHashMap<String, Object> geraRelatorioEncerrantes(Comandos tipo) throws Exception {

        String tipoRelEncerrante = (tipo.name().contains("ALINEA_B")) ? CSPUtilidadesLang.substring(tipo.name(), tipo.name().length() - 2, tipo.name().length()) : String.valueOf(tipo.name().charAt(tipo.name().length() - 1));

        ResultSet select = new CSPInstrucoesSQLBase(CSPInstrucoesSQLBase.Bases.BASE_CONTRATANTE).select((sb) -> {
            sb.append("SELECT");
            sb.append("    ID_BICO,");
            sb.append("    ENCERRANTE_INI,");
            sb.append("    ENCERRANTE_FIN,");
            sb.append("    IIF(ENCERRANTE_TOTAL IS NULL, '0.000', ENCERRANTE_TOTAL) AS ENCERRANTE_TOTAL ");
            sb.append("FROM");
            sb.append("    PR_RETORNA_REL_ENCERRANTES (?)");
        }, tipoRelEncerrante);

        LinkedHashMap<String, Object> dados = new LinkedHashMap<>();

        while (select.next()) {

            String ei = " EI" + CSPUtilidadesLang.pad(select.getString("ENCERRANTE_INI"), 11, "0");
            String ef = " EF" + CSPUtilidadesLang.pad(select.getString("ENCERRANTE_FIN"), 11, "0");
            String vl = " V" + CSPUtilidadesLang.pad(select.getString("ENCERRANTE_TOTAL"), 11, "0");
            String bico = CSPUtilidadesLang.pad(select.getString("ID_BICO"), 2, "0");

            String infoEncerrantes = "#CE:B" + bico + ei + ef + vl;

            dados.put("BICO_" + select.getString("ID_BICO"), infoEncerrantes);
        }

        return dados;
    }

    /**
     * Envia comando para verificar se existe respota na porta com.
     */
    private Object verificaCom() throws Exception {
        return getCom().enviaComando("(&A)", 1000) != null;
    }
}
