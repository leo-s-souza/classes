/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.sped;


import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.PadraoClasses;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocaisJson;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.isInt;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.substring;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangDateTime.formataDataHora;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.LINE_SEPARATOR;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Classe responsável por gerar os Speds Fiscais
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 04/01/2016 - 09:55:03
 */
public class CSPSpedFiscal {

    private final CSPInstrucoesSQLBase conn;
    private final CSPArquivosLocais arquivo;
    private final Perfil perfil;

    public enum Perfil {

        A, B, C
    }

    private final String versao;
    private final String finalidade;
    private final String atividade;
    private final CSPArquivosLocais dadosComuns;
    private final Date de;
    private final Date ate;
    /**
     * Informações do contratante
     */
    private final HashMap<String, String> contr = new HashMap<>();
    /**
     * Informações da contabilidade do contratante
     */
    private final HashMap<String, String> contb = new HashMap<>();

    /**
     * Construtor da classe
     *
     * @param conn CSPInstrucoesSQLBase - Base de origem dos dados
     * @param pasta String - Pasta onde gerar o arquivo
     * @param perfil Perfil - Perfil a ser gerado
     * @param versao String - Códio da versão a ser gerado
     * @param finalidade String - Código da finalidade a ser gerada
     * @param de Date - Data inicial das informações
     * @param ate Date - Data final das informações
     * @param atividade String - Codigo da atividade do contrante
     * @throws java.lang.Exception
     */
    public CSPSpedFiscal(CSPInstrucoesSQLBase conn, String pasta, Perfil perfil, String versao, String finalidade, String atividade, Date de, Date ate) throws Exception {
        this.conn = conn;

        this.perfil = perfil;
        this.versao = versao;
        this.finalidade = finalidade;
        this.de = de;
        this.ate = ate;
        this.dadosComuns = new CSPArquivosLocais(PadraoClasses.getPastaArquivosDadosComuns());
        this.atividade = atividade;
        this.arquivo = new CSPArquivosLocais(pasta);

//        getRegistroDbContratante().entrySet().forEach(e -> {
//            this.contr.put(e.getKey(), e.getValue());
//        });
//        getInfosRegistroAgente(PadraoClasses.getCodigoContabilidadeContratante()).entrySet().forEach(e -> {
//            this.contb.put(e.getKey(), e.getValue());
//        });
        this.makeNameFile();
    }

    /**
     * Define o nome do arquivo
     */
    private void makeNameFile() throws IOException {
        String pName = "";
        JSONArray pers = this.registrosDadosComuns("jdc_speds_perfil.json");
        for (int i = 0; i < pers.length(); i++) {
            JSONObject t = pers.getJSONObject(i);
            if (t.getString("CODIGO").equals(this.perfil.toString())) {
                pName = t.getString("DESCRICAO");
            }
        }
        this.arquivo.setName(pName + "_" + formataDataHora(de, "ddMMyyyy") + "_" + formataDataHora(ate, "ddMMyyyy") + "_" + this.contr.get("AGENTE_NOME") + "_.txt");
    }

    /**
     * Armazena todo o conteudo do arquivo a ser gerado
     */
    private final HashMap<String, ArrayList<ArrayList<String>>> conteudo = new HashMap<>();

    /**
     * Executa o processo de geraçao do SPED
     *
     * @return boolean
     * @throws java.lang.Exception
     */
    public boolean gerar() throws Exception {
        this.conteudo.clear();

        CSPLog.info(this.getClass(),  "iniciando...");
        /**
         * ********************************** LEIA
         * *********************************
         *
         * - Cada bloco deverá ser agrupado pelas "{ }" e devidamente nomeado!!
         *
         * - Mantenha a legibilidade do fonte!!
         *
         * - A classe possui método para encurtar o tamanho do fonte, use!!!
         *
         * - Sempre ao adicionar um novo bloco não esqueça de nomear o mesmo.
         * Idem para os registros!
         *
         * - Numere cada campo!
         *
         * - Use o caractere "?" para marcar qualquer campo que ainda não está
         * concluído. O mesmo não será apresentado no arquivo final, somente no
         * log
         *
         */
        {//Bloco 0 - Abertura, Identificação e Referências
            CSPLog.info(this.getClass(),  "bloco 0....");
            //0000: ABERTURA DO ARQUIVO DIGITAL E IDENTIFICAÇÃO DA ENTIDADE
            this.addInfo(Registros.REG_0000,//1
                    versao,//2
                    finalidade,//3
                    formataDataHora(de, "ddMMyyyy"),//4
                    formataDataHora(ate, "ddMMyyyy"),//5
                    this.contr.get("AGENTE_NOME"),//6
                    this.contr.get("CNPJ"),//7
                    this.contr.get("CPF"),//8
                    this.auxCode(() -> {//9
                        JSONArray muni = this.registrosDadosComuns("jdc_municipios.json");
                        for (int i = 0; i < muni.length(); i++) {
                            JSONObject t = muni.getJSONObject(i);
                            if (t.getString("CODIGO_CIDADE").equals(this.contr.get("MUNICIPIO"))) {
                                return t.getString("SIGLA");
                            }
                        }
                        return "";
                    }),
                    this.contr.get("IE"),//10
                    this.contr.get("MUNICIPIO"),//11
                    this.contr.get("IM"),//12
                    "",//13
                    this.perfil.toString(),//14
                    this.atividade//15
            );
            //0001: ABERTURA DO BLOCO 0
            this.addInfo(Registros.REG_0001, "0");
            //0005: DADOS COMPLEMENTARES DA ENTIDADE
            this.addInfo(Registros.REG_0005,//1
                    this.contr.get("FANTASIA"),//2
                    this.contr.get("CEP"),//3
                    this.contr.get("LOGRADOURO"),//4
                    this.contr.get("NUMERO"),//5
                    this.contr.get("COMPLEMENTO"),//6
                    this.contr.get("BAIRRO"),//7
                    this.contr.get("FONE"),//8
                    "",//9
                    this.contr.get("EMAIL")//10
            );
            //0100: DADOS DO CONTABILISTA
            this.addInfo(Registros.REG_0100,//1
                    this.contb.get("AGENTE_NOME"),//2
                    this.contb.get("CPF"),//3
                    this.contb.get("CREG_C"),//4
                    this.contb.get("CNPJ"),//5
                    this.contb.get("CEP"),//6
                    this.contb.get("LOGRADOURO"),//7
                    this.contb.get("NUMERO"),//8
                    this.contb.get("COMPLEMENTO"),//9
                    this.contb.get("BAIRRO"),//10
                    this.contb.get("FONE"),//11
                    this.contb.get("FAX"),//12
                    this.contb.get("EMAIL"),//13
                    this.contb.get("MUNICIPIO")//14
            );
            //0190: IDENTIFICAÇÃO DAS UNIDADES DE MEDIDA
            this.whileResult("SELECT  "
                    + "    DISTINCT u.UNIDADE_CODIGO,"
                    + "    u.DESCRICAO  "
                    + "FROM  "
                    + "    PRODUTOS_UNIDADES u "
                    + "    INNER JOIN PRODUTOS p ON p.UNIDADE_CODIGO = u.UNIDADE_CODIGO "
                    + "GROUP BY  "
                    + "    u.UNIDADE_CODIGO, u.DESCRICAO "
                    + "ORDER BY  "
                    + "    u.UNIDADE_CODIGO ASC", (r) -> {
                        this.addInfo(Registros.REG_0190,//1
                                r.getString("UNIDADE_CODIGO"),//2
                                r.getString("DESCRICAO")//3
                        );
                    });
            //0200: TABELA DE IDENTIFICAÇÃO DO ITEM (PRODUTO E SERVIÇOS)
            this.whileResult("SELECT  "
                    + "    DISTINCT r.PRODUTO_CODIGO,  "
                    + "    r.UNIDADE_CODIGO,  "
                    + "    r.DESCRICAO,  "
                    + "    r.SPED_CODIGO, "
                    + "    r.TIPO_ITEM, "
                    + "    r.NCM, "
                    + "    r.ICMS "
                    + "FROM  "
                    + "    PRODUTOS r "
                    + "ORDER BY "
                    + "    r.PRODUTO_CODIGO ASC", (r) -> {
                        this.addInfo(Registros.REG_0200,//1
                                r.getString("PRODUTO_CODIGO"),//2
                                r.getString("DESCRICAO"),//3
                                "",//4
                                "",//5
                                "",//6
                                r.getString("TIPO_ITEM"),//7
                                r.getString("NCM"),//8
                                "",//9
                                this.auxCode(r, (s) -> {//10
                                    if (s.getString("NCM") != null) {
                                        return substring(s.getString("NCM"), 0, 2);
                                    }
                                    return "";
                                }),
                                "",//11
                                this.auxCode(r, (s) -> {//12
                                    if (s.getString("ICMS") != null && isInt(s.getString("ICMS"))) {
                                        return CSPUtilidadesLang.defaultDecimalFormat(Double.parseDouble(s.getString("ICMS")) / 100);
                                    }
                                    return "";
                                })
                        );
                    });

        }
        {//Bloco C - Documentos Fiscais I - Mercadorias (ICMS/IPI)
            CSPLog.info(this.getClass(),  "bloco C....");
            //C001: ABERTURA DO BLOCO C
            this.addInfo(Registros.REG_C001, "0");
            //C170: ITENS DO DOCUMENTO (CÓDIGO 01, 1B, 04 e 55)
            this.whileResult("SELECT "
                    + "         r.* "
                    + "FROM "
                    + "     EST_LANCAMENTO_ENTRADAS_ITENS r "
                    + "ORDER "
                    + "     BY r.DOCUMENTO ASC", (r) -> {
                        this.sumTempVal(Registros.REG_C170, "vl-merc-" + r.getString("DOCUMENTO"), r.getDouble("TOTAL_ITEM"));
                        this.sumTempVal(Registros.REG_C170, "vl-desc-" + r.getString("DOCUMENTO"), r.getDouble("DESCONTO_ITEM"));
                        //Agrupamos e cacheamos os dados para uso no C190

                        String keyC190 = "CST_ICMS-" + r.getString("CFOP") + "-" + r.getString("ICMS_ALIQUOTA");

                        this.addTempVal(Registros.REG_C170, "to-c190-" + keyC190, new String[]{"CST_ICMS", r.getString("CFOP"), r.getString("ICMS_ALIQUOTA")});
                        this.sumTempVal(Registros.REG_C170, keyC190 + "-vl-bc-icms", r.getDouble("ICMS_BASE"));

                        this.addInfo(Registros.REG_C170, //1
                                r.getString("ITEM_NUMERO"),//2
                                r.getString("PRODUTO_CODIGO"),//3
                                "",//4
                                r.getString("QUANTIDADE"),//5
                                r.getString("UNIDADE_CODIGO"),//6
                                CSPUtilidadesLang.defaultDecimalFormat(r.getDouble("TOTAL_ITEM")),//7
                           CSPUtilidadesLang.     defaultDecimalFormat(r.getDouble("DESCONTO_ITEM")),//8
                                "0",//9
                                "000",//10
                                r.getString("CFOP"),//11
                                "?",//12
                              CSPUtilidadesLang.  defaultDecimalFormat(r.getDouble("ICMS_BASE"),5),//13
                                r.getString("ICMS_ALIQUOTA"),//14
                                "?",//15
                                "?",//16
                                "?",//17
                                "?",//18
                                "?",//19
                                "?",//20
                                "?",//21
                                "?",//22
                                "?",//23
                                "?",//24
                                "?",//25
                                "?",//26
                                "?",//27
                                "?",//28
                                "?",//29
                                "?",//30
                                "?",//31
                                "?",//32
                                "?",//33
                                "?",//34
                                "?",//35
                                "?",//36
                                "?"//37
                        );
                    });
            //C190: REGISTRO ANALÍTICO DO DOCUMENTO (CÓDIGO 01, 1B, 04, 55 e 65)
            this.getTempVal(Registros.REG_C170).entrySet().forEach(e -> {
                if (e.getKey().startsWith("to-c190-")) {
                    String key = ((String[]) e.getValue())[0] + "-" + ((String[]) e.getValue())[1] + "-" + ((String[]) e.getValue())[2];
                    this.addInfo(Registros.REG_C190, //1
                            "?",//2
                            this.getSumTempValFormatted(Registros.REG_C170, key + "-vl-bc-icms"),//3
                            "?",//4
                            "?",//5
                            "?",//6
                            "?",//7
                            "?",//8
                            "?",//9
                            "?",//10
                            "?",//11
                            "?"//12
                    );
                }
            });

            //C100: NOTA FISCAL (CÓDIGO 01), NOTA FISCAL AVULSA (CÓDIGO 1B), NOTA FISCAL DE PRODUTOR (CÓDIGO 04), NF-e (CÓDIGO 55) e NFC-e (CÓDIGO 65)
            this.whileResult("SELECT "
                    + "         r.* "
                    + "FROM "
                    + "     EST_LANCAMENTO_ENTRADAS r "
                    + "ORDER "
                    + "     BY r.DOCUMENTO ASC", (r) -> {
                        this.addInfo(Registros.REG_C100, //1
                                "0",//2
                                "1",//3
                                r.getString("AGENTE_CODIGO"),//4
                                r.getString("DOCUMENTO_MODELO_FISCAL"),//5
                                "?",//6
                                r.getString("DOCUMENTO_SERIE"),//7
                                r.getString("DOCUMENTO"),//8
                                r.getString("CHAVE_NFE"),//9
                                formataDataHora(r.getDate("DOCUMENTO_DATA"), "ddMMyyyy"),//10
                                formataDataHora(r.getDate("DATA_ENTRADA"), "ddMMyyyy"),//11
                                "?",//12
                                r.getString("PAGAMENTO_TIPO"),//13
                                this.getSumTempValFormatted(Registros.REG_C170, "vl-desc-" + r.getString("DOCUMENTO")),//14
                                "?",//15
                                this.getSumTempValFormatted(Registros.REG_C170, "vl-merc-" + r.getString("DOCUMENTO")),//16
                                r.getString("FRETE_TIPO"),//17
                             CSPUtilidadesLang.   defaultDecimalFormat(r.getDouble("FRETE_TOTAL")),//18
                              CSPUtilidadesLang.  defaultDecimalFormat(r.getDouble("SEGURO_VALOR")),//19
                              CSPUtilidadesLang.  defaultDecimalFormat(r.getDouble("DESPESAS_VALOR")),//20
                                "?",//21
                                "?",//22
                                "?",//23
                                "?",//24
                                "?",//25
                                "?",//26
                                "?",//27
                                "?",//28
                                "?"//29
                        );
                    });
        }
        {//Bloco D - Documentos Fiscais II - Serviços (ICMS)
            CSPLog.info(this.getClass(),  "bloco D....");
        }
        {//Bloco E - Apuração ICMS e do IPI
            CSPLog.info(this.getClass(),  "bloco E....");
        }
        {//Bloco G - Controle de Crédito de ICMS do Ativo Permanente - CIAP
            CSPLog.info(this.getClass(),  "bloco G....");
        }
        {//Bloco H - Inventário Físico
            CSPLog.info(this.getClass(),  "bloco H....");
        }
        {//Bloco K - Controle de Produção e do Estoque
            CSPLog.info(this.getClass(),  "bloco K....");
        }
        {//Bloco 1 - Outras Informações
            CSPLog.info(this.getClass(),  "bloco 1....");
        }
        {//Bloco 9 - Controle e Encerramento do Arquivo Digital
            CSPLog.info(this.getClass(),  "bloco 9....");
        }

        return this.makeFile();
    }

    /**
     * Atributo destinado para troca de valores temporários
     */
    private HashMap<Registros, HashMap<String, Object>> auxTemp = new HashMap<>();

    /**
     * Armazena determinado valor para uso temporário
     *
     * @param reg Registros - Relacionado a qual registro
     * @param key String - Key de identificação
     * @param val Object - Valor
     */
    private void addTempVal(Registros reg, String key, Object val) {
        HashMap<String, Object> hash = this.auxTemp.get(reg);
        if (hash == null) {
            hash = new HashMap<>();
        }
        hash.put(key, val);
        this.auxTemp.put(reg, hash);
    }

    /**
     * *
     * Realiza a somatória automática do valor temporário
     *
     * @param reg
     * @param key
     * @param val
     */
    private void sumTempVal(Registros reg, String key, Double val) {
        Object t = this.getTempVal(reg, key);
        double sum = 0;
        if (t != null) {
            sum = (double) t;
        }

        if (val == null) {
            val = (double) 0;
        }
        sum += val;
        this.addTempVal(reg, key, sum);
    }

    /**
     * Pega o valor temporário
     *
     * @param reg Registros - Relacionado a qual registro
     * @param key String - Key de identificação
     * @return
     */
    private Object getTempVal(Registros reg, String key) {
        return this.getTempVal(reg).get(key);
    }

    private String getSumTempValFormatted(Registros reg, String key) {
        return this.getSumTempValFormatted(reg, key, 2);
    }
    private String getSumTempValFormatted(Registros reg, String key,int decimal) {
        return CSPUtilidadesLang.defaultDecimalFormat(this.getSumTempVal(reg, key),decimal);
    }

    private double getSumTempVal(Registros reg, String key) {
        Object t = this.getTempVal(reg).get(key);
        if (t == null) {
            return 0;
        }
        return (double) t;
    }

    /**
     * Pega o valor temporário
     *
     * @param reg Registros - Relacionado a qual registro
     * @return
     */
    private HashMap<String, Object> getTempVal(Registros reg) {
        HashMap<String, Object> hash = this.auxTemp.get(reg);
        if (hash == null) {
            hash = new HashMap<>();
        }
        return hash;
    }

    /**
     * Determina quais bloco já são gerados.
     *
     * A ordem dos enums aqui definida é a mesma que será 'impressa' no arquivo!
     *
     */
    private enum Registros {

        REG_0000,
        REG_0001,
        REG_0005,
        REG_0100,
        REG_0190,
        REG_0200,
        REG_C001,
        REG_C100,
        REG_C170,
        REG_C190;

    }

    /**
     * Retorna o JSONArray dos registros do arquivo
     *
     * @return
     */
    private JSONArray registrosDadosComuns(String arquivo) throws IOException {
        CSPArquivosLocaisJson a = new CSPArquivosLocaisJson(this.dadosComuns.getAbsolutePath() + "/" + arquivo);
        if (a.exists()) {
            return a.getObject().getJSONArray("REGISTROS");
        } else {
            return null;
        }
    }

    /**
     * Realiza uma consulta simples e já retorna o ResultSet pronto para usar
     *
     * @return
     */
    private ResultSet simpleResult(String sql) throws SQLException {
        return this.simpleResult(sql, null);
    }

    /**
     * Realiza uma consulta simples e já retorna o ResultSet pronto para usar
     *
     * @return
     */
    private ResultSet simpleResult(String sql, Object valor) throws SQLException {
        return this.simpleResult(sql, new ArrayList<>(Arrays.asList(valor)));
    }

    /**
     * Realiza uma consulta simples e já retorna o ResultSet pronto para usar
     *
     * @return
     */
    private ResultSet simpleResult(String sql, ArrayList<Object> valores) throws SQLException {
        ResultSet s = conn.select(sql, valores);

        if (s.next()) {
            return s;
        }

        return null;
    }

    /**
     * * Realiza uma consulta de forma a economizar fonte
     *
     * @return
     */
    private void whileResult(String sql, WilheResult run) throws Exception {
        this.whileResult(sql, null, run);
    }

    /**
     * * Realiza uma consulta de forma a economizar fonte
     *
     * @return
     */
    private void whileResult(String sql, Object valor, WilheResult run) throws Exception {
        this.whileResult(sql, new ArrayList<>(Arrays.asList(valor)), run);
    }

    /**
     * Realiza uma consulta de forma a economizar fonte
     *
     * @return
     */
    private void whileResult(String sql, ArrayList<Object> valores, WilheResult run) throws Exception {
        ResultSet s = conn.select(sql, valores);

        while (s.next()) {
            run.run(s);
        }

    }

    /**
     * Interface para economizar o tamanho do fonte
     */
    private interface WilheResult {

        public void run(ResultSet r) throws Exception;
    }

    private String auxCode(AuxCode run) throws Exception {
        return run.run();
    }

    private String auxCode(ResultSet r, AuxCodeResultSet run) throws Exception {

        return run.run(r);

    }

    /**
     * Interface para economizar o tamanho do fonte
     */
    private interface AuxCode {

        public String run() throws Exception;
    }

    /**
     * Interface para economizar o tamanho do fonte
     */
    private interface AuxCodeResultSet {

        public String run(ResultSet s) throws SQLException;
    }

    /**
     * 'Aliais' do método 'addInfo'
     *
     * @param bloco Registros - Bloco da informação
     * @param infos String - Informações, <b>sem o bloco</b>
     */
    private void addInfo(Registros registro, String... infos) {
        this.addInfo(registro, new ArrayList<>(Arrays.asList((String[]) infos)));
    }

    /**
     * Adiciona uma nova linha de informações no bloco especificado. Caso o
     * mesmo ainda não exista o método o criará.
     *
     * @param bloco Registros - Bloco da informação
     * @param infos ArrayList<String> - Informações, <b>sem o bloco</b>
     */
    private void addInfo(Registros registro, ArrayList<String> infos) {
        String name = registro.toString().replace("REG_", "");
        CSPLog.info(this.getClass(),  "adicionando registro " + name + ":" + Arrays.toString(infos.toArray()) + "...");
        ArrayList<ArrayList<String>> tmp = new ArrayList<>();
        if (this.conteudo.get(name) != null) {
            tmp = this.conteudo.get(name);
        }
        tmp.add(infos);
        this.conteudo.put(name, tmp);
    }

    /**
     * Transforma o HashMap de informações no arquivo final
     */
    private boolean makeFile() throws Exception {
        CSPLog.info(this.getClass(),  "montando arquivo...");
        this.arquivo.setContent("");
        ArrayList<String> reportVerif = new ArrayList<>();
        for (Registros reg : Registros.values()) {
            String name = reg.toString().replace("REG_", "");
            ArrayList<ArrayList<String>> get = this.conteudo.get(name);
            if (get != null) {
                int k = 1;
                for (ArrayList<String> s : get) {
                    String t = "|" + name + "|";
                    int i = 2;
                    for (String in : s) {
                        if (in != null && in.trim().equals("?")) {
                            t += "|";
                            if (!reportVerif.contains(name + "" + i)) {
                                CSPLog.error(this.getClass(),  "Registro " + name + ", campo n°" + i + " precisa ser verificado!");
                                reportVerif.add(name + "" + i);
                            }
                        } else if (in != null) {
                            t += in.trim() + "|";
                        } else {
                            t += "|";
                        }
                        ++i;
                    }
                    this.arquivo.appendContent(t + LINE_SEPARATOR);
                    ++k;
                }
                CSPLog.info(this.getClass(),  "Total registro " + name + ": " + k);
            } else {
                CSPLog.error(this.getClass(),  "Registro " + name + " definido, mas não alimentado!");
            }
        }

        CSPLog.info(this.getClass(),  "arquivo gerado!");
        return true;
    }

}
