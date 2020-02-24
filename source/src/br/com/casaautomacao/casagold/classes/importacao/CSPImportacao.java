/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.modelos.ModelColunaTabela;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.isDouble;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang.pad;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangInstrucoesSQLExportaDDL.getColunas;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Classe base para a importação de registros
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/12/2015 - 10:22:03
 */
@Deprecated
abstract public class CSPImportacao {

    protected final CSPInstrucoesSQLBase conn;
    protected final String numImportacao;
    protected final String historicoImportacao;
    protected final CSPInstrucoesSQLBase destino;
    private final HashMap<String, CSPInstrucoesSQLBase> estrangeiros;

    protected String arquivoOrigem = "";
    private final String arquivoDestino;

    /**
     * Importação de registros
     *
     * @param contratante CSPInstrucoesSQLBase - Conexão com a base contratante
     * @param importacao String - Número da importação
     * @param historicoImportacao - Código do histórico da importação.
     * @param destino CSPInstrucoesSQLBase - Conexão com a base de destino
     * @param estrangeiros HashMap<String, CSPInstrucoesSQLBase> - Bases
     * estrangeiras
     */
    public CSPImportacao(CSPInstrucoesSQLBase contratante, String importacao, String historicoImportacao, CSPInstrucoesSQLBase destino, HashMap<String, CSPInstrucoesSQLBase> estrangeiros) {
        this.conn = contratante;
        this.numImportacao = importacao;
        this.historicoImportacao = historicoImportacao;
        this.destino = destino;
        this.estrangeiros = estrangeiros;
        if (this.destino != null) {
            this.arquivoDestino = new File(this.destino.getConfs().getPath()).getName();
        } else {
            this.arquivoDestino = "";
        }
    }

    private String makeKey(String tabela, String tabelaOrigem) {
        String key = tabela;
        if (tabelaOrigem != null) {
            key += "-" + tabelaOrigem;
        }
        return key;
    }

    private HashMap<String, String> tratamentoDefault(String key, String tabela, String tabelaOrigem, HashMap<String, String> dados) throws SQLException, Exception {

        this.confValoresCondicionais(tabela, tabelaOrigem);
        //Trata os valroes condicionais
        if (this.valoresCondicionais.get(key) != null) {
            for (Map.Entry<String, String> e : this.getRelacaoTabelasColunasDestinoOrigem(tabela, tabelaOrigem).entrySet()) {
                HashMap<String, String> f = this.valoresCondicionais.get(key).get(e.getKey() + "-" + e.getValue());
                if (f != null && dados.get(e.getKey()) != null) {
                    f.entrySet().stream().filter((d) -> (d.getValue().trim().equals(dados.get(e.getKey()).trim()))).forEach((d) -> {
                        dados.put(e.getKey(), d.getKey());
                    });
                }
            }
        }

        //Verifica se existe algum valor nulo no HashMap de gravação. 
        //Caso exista ele substitui o valor nulo pelo valor dafault gravado na tabela.
        for (Map.Entry<String, String> e : dados.entrySet()) {
            boolean def = false;
            if (e.getValue() == null) {
                def = true;
            } else if (e.getValue().trim().isEmpty()) {
                def = true;
            }
            if (def) {
                dados.put(e.getKey(), this.valoresDefault.get(key).get(e.getKey()));
            }
        }
        HashMap<String, String> r = new HashMap<>();
        for (Map.Entry<String, String> e : dados.entrySet()) {
            String st = e.getValue();
            if (st == null && this.gerarCodigos.get(key).get(e.getKey()) != null && this.gerarCodigos.get(key).get(e.getKey()).toLowerCase().equals("s")) {
                st = geraMaiorCodigo(tabela, e.getKey());
            }
            if (st != null) {
                //Aplica o processo do filtro
                String rm = this.filtroCaracteres.get(key).get(e.getKey());
                st = st.trim();

                st = st.replaceAll("\uFFFD", "");//http://www.fileformat.info/info/unicode/char/fffd/index.htm

                if (rm != null) {
                    String[] chars = rm.split("(?!^)");
                    for (String c : chars) {
                        st = st.replace(c, "");
                    }
                }
                //Faz um trim maroto no que vai ser inserido
                st = st.trim();
                //Para não deixar gravar na base com um tamanho maior do que é possível
                int max = 0;
                if (this.tamanhoMaximoAceito.get(tabela).get(e.getKey()) != null) {
                    max = this.tamanhoMaximoAceito.get(tabela).get(e.getKey());
                }

                if (st.length() > max && max > 0) {
                    st = st.substring(0, max);

                } else if ((this.aplicarZeroEsquerda.get(key).get(e.getKey()) != null && this.aplicarZeroEsquerda.get(key).get(e.getKey()).toLowerCase().equals("s"))
                        || (this.gerarCodigos.get(key).get(e.getKey()) != null && this.gerarCodigos.get(key).get(e.getKey()).toLowerCase().equals("s"))) {
                    st = pad(st, max, "0", true);
                }

                //Faz mais um trim maroto no que vai ser inserido
                st = st.trim();

                //Todo o espaço em branco é removido do hashmap, ou seja, transforma um valor em branco para um null da vida
                if (!st.isEmpty()) {
                    r.put(e.getKey(), st);
                } else {
                    r.put(e.getKey(), null);
                }
            } else {
                r.put(e.getKey(), null);
            }
        }
        return r;
    }

    /**
     * Efetua a importação de determinado registro
     *
     * @param tabela String - Tabela de destino
     * @param tabelaOrigem String - Tabela de origem dos dados
     * @param data String - Dados a serem importados
     * @param fksOrigem
     * @return boolean - Resultado da importação
     */
    protected boolean efetuaImportacao(String tabela, String tabelaOrigem, HashMap<String, String> data, ArrayList<String[]> fksOrigem) throws Exception {
        HashMap<String, String> relacao = this.getRelacaoTabelasColunasDestinoOrigem(tabela, tabelaOrigem);
        String key = this.makeKey(tabela, tabelaOrigem);

        this.alimentaHashsComuns(tabela, tabelaOrigem);
        HashMap<String, String> t1 = new HashMap<>();
        for (String c : camposDestino.get(key)) {
            t1.put(c, null);
            String st = data.get(c);
            if (st != null) {
                st = st.trim();
                if (!st.isEmpty()) {
                    t1.put(c, st);
                }
            }
        }
        this.confValoresCondicionais(tabela, tabelaOrigem);
        this.loadInCacheValoresAlterados();

        HashMap<String, String> r = this.tratamentoDefault(key, tabela, tabelaOrigem, this.trataDadosEstrangeiros(tabela, tabelaOrigem, t1, data));

        //Procura se temos uma FK de origem/destino
        if (fksOrigem != null && chavesEstrangeiras.get(tabela) != null) {
            if (fksOrigem.size() > 0 && chavesEstrangeiras.get(tabela).size() > 0) {
                //Repassa todos os campos e descobre as relações das FKs
                for (Map.Entry<String, String> e : relacao.entrySet()) {
                    String dest = e.getKey();
                    String ori = e.getValue();
                    /*Por causa da origem estrangeira não temos um FK na origem.
                     Se tiver por causa da origem estrangeira vamos apenas adicionar manualmente a origem
                     */
                    HashMap<String, String[]> est = this.origemEstrangeira.get(key);
                    if (est != null) {
                        String[] c = est.get(this.makeKey(dest, ori));
                        if (c != null) {
                            ori = c[3];
                        }
                    }
                    if (ori != null) {

                        for (String[] o : fksOrigem) {
                            if (o[0].equals(ori)) {
                                String[] fk = this.chavesEstrangeiras.get(tabela).get(dest);
                                if (fk == null) {
                                    continue;
                                }
                                // String t = fk[0];
                                String c = fk[1];
                                String tO = o[1];
                                //String cO = o[2];
                                String dataC = r.get(dest);

                                HashMap<String, String> get = this.getInCacheValoresChaveAlterados(tO, c);
                                if (get != null && dataC != null) {

                                    if (get.get(dataC) != null) {
                                        r.put(c, get.get(dataC));
                                    }

                                }
                            }

                        }

                    }
                }
            }
        }
        String where = this.needUpdate(tabela, r);
        String foreign = this.allowForeignKey(tabela, r);
        if (foreign == null) {
            String unique = this.allowNewUnique(tabela, r, where == null);
            if (unique == null) {

                boolean insert = where == null;
                if (insert) {
                    //Para nao deixar gravar registos duplicados (PKs)
                    r = this.trataRegistrosDuplicados(tabela, tabelaOrigem, r);
                    r = this.tratamentoDefault(key, tabela, tabelaOrigem, r);
                }
                if (this.destino.insertComposto(insert, tabela, new HashMap<>(r), where)) {
                    this.addInCacheValoresChaveAlterados(tabela, tabelaOrigem, r, data);
                    this.addInCacheValoresUnique(tabela, r);
                    if (this.chavesPrimarias.get(tabela) != null) {
                        for (String pk : this.chavesPrimarias.get(tabela)) {
                            ArrayList<String> fks = new ArrayList<>();
                            if ((this.chavesEstrangeiras.get(tabela) != null) && (!this.chavesEstrangeiras.get(tabela).isEmpty())) {
                                this.chavesEstrangeiras.get(tabela).entrySet().stream().forEach((f) -> {
                                    fks.add(f.getKey());
                                });
                            }
                            if (!fks.contains(pk)) {
                                if ((r.get(pk) != null) && (data.get(pk) != null) && (!r.get(pk).equals(data.get(pk)))) {
                                    HashMap<String, String> d = new HashMap();
                                    d.put("ORIGEM", "[" + this.arquivoOrigem + "],<<" + tabelaOrigem + ">>,<" + this.getRelacaoTabelasColunasDestinoOrigem(tabela, tabelaOrigem).get(pk) + ">");
                                    d.put("ORIGEM_RESULTADO", data.get(pk));
                                    d.put("DESTINO", "[" + this.arquivoDestino + "],<<" + tabela + ">>,<" + pk + ">");
                                    d.put("DESTINO_RESULTADO", r.get(pk));
                                    this.conn.insertComposto(true, "FRR_IMPORTACAO_ALTERADOS", new HashMap<>(d));
                                    break;
                                }
                            }
                        }
                    }
                    return true;
                }
            } else {

                montaRegistroNaoMigrado(r, tabelaOrigem, "UNIQUE", unique);

                return false;
            }

        }

        montaRegistroNaoMigrado(r, tabelaOrigem, "FOREIGN KEY", foreign);

        return false;
    }
    /**
     * Cacheia, por relacao, de dados estrangeiros
     */
    private final HashMap<String, HashMap<String, HashMap<String, String>>> cacheDadosEstrangeiros = new HashMap<>();

    /**
     * Realiza o tratamento dos dados estrangeiros
     *
     * @param tabela
     * @param tabelaOrigem
     * @param dados
     * @return
     */
    private HashMap<String, String> trataDadosEstrangeiros(String tabela, String tabelaOrigem, HashMap<String, String> dados, HashMap<String, String> originais) throws SQLException {

        String key = this.makeKey(tabela, tabelaOrigem);
        if (this.origemEstrangeira.get(key) != null) {
            HashMap<String, String> t = this.getRelacaoTabelasColunasDestinoOrigem(tabela, tabelaOrigem);
            if (this.cacheDadosEstrangeiros.get(key) == null) {
                HashMap<String, HashMap<String, String>> tmp = new HashMap<>();

                for (Map.Entry<String, String> e : t.entrySet()) {
                    String[] f = this.origemEstrangeira.get(key).get(e.getKey() + "-" + e.getValue());
                    if (f != null) {
                        CSPInstrucoesSQLBase con = this.estrangeiros.get(f[0]);
                        HashMap<String, String> tmp2 = new HashMap<>();
                        ResultSet s = con.select("SELECT DISTINCT " + f[2] + " AS K, " + f[3] + " AS V FROM " + f[1]);
                        while (s.next()) {
                            tmp2.put(s.getString("K"), s.getString("V"));
                        }
                        tmp.put(e.getKey(), tmp2);
                    }

                }
                this.cacheDadosEstrangeiros.put(key, tmp);
            }

            for (Map.Entry<String, String> e : t.entrySet()) {
                HashMap<String, String> get = this.cacheDadosEstrangeiros.get(key).get(e.getKey());
                if (get == null) {
                    continue;
                }
                /**
                 * Exemplificando:
                 *
                 * Preciso do NCM(NCM), já tenho o CODIGO_PRODUTO(origem)
                 *
                 * Com o for abaixo o sistema buscará qual é a key que possui o
                 * CODIGO_PRODUTO. Nesse caso será a PRODUTO_CODIGO
                 *
                 */
                String o = "?";
                for (Map.Entry<String, String> d : t.entrySet()) {
                    if (d.getValue() != null && e.getValue() != null && !d.getKey().equals(e.getKey())) {
                        if (d.getValue().trim().equals(e.getValue().trim())) {
                            o = d.getKey();
                            break;
                        }
                    }
                }

                String val = originais.get(o);

                if (val != null) {

                    dados.put(e.getKey(), get.get(val));
                }

            }
        }
        return dados;
    }

    private final HashMap<String, Boolean> relacaoTabelasUpdate = new HashMap<>();

    /**
     * Identifica e trata quando existe a necessidade de realizar um update no
     * registro
     *
     * @param tabela String - Tabela de destino
     * @param dados
     * @return String - Where do update e null em caso de inserts
     */
    private String needUpdate(String tabela, HashMap<String, String> dados) throws Exception {
        if (this.relacaoTabelasUpdate.get(tabela) == null) {
            this.relacaoTabelasUpdate.put(tabela, Boolean.FALSE);
            ResultSet s = this.destino.select("SELECT COUNT(*) AS S FROM " + tabela);

            if (s.next()) {

                this.relacaoTabelasUpdate.put(tabela, s.getInt("S") > 0);
            }

        }
        if (this.relacaoTabelasUpdate.get(tabela)) {

            ArrayList<String> pks = this.chavesPrimarias.get(tabela);
            if (pks != null) {
                ArrayList<String> where = new ArrayList<>();
                pks.stream().filter((a) -> (dados.get(a) != null && !dados.get(a).trim().isEmpty())).forEach((a) -> {
                    where.add(" " + a + " = '" + dados.get(a) + "'");
                });
                if (where.size() > 0) {
                    return String.join(" AND ", where);
                }
            }
        }
        return null;
    }

    /**
     * Procura na base de dados o maior codigo cadastrado e cria o proximo
     * codigo com 0 a esquerda.
     *
     * @param origemDestino - Recebe tabeladestino-origem.
     * @param campoDestino - recebe o campo de destino.
     * @return
     */
    private String geraMaiorCodigo(String tabela, String campoDestino) throws Exception {

        //Pega maior codigo gravado na tabela.
        ResultSet result = destino.select("SELECT MAX(" + campoDestino + ") AS " + campoDestino + " FROM " + tabela);

        String retorna = "";

        if (result.next()) {
            // Recebe maior codigo.
            retorna = result.getString(campoDestino);

            // Variavel utilizada para o acréscimo do código.
            int acrescentaCodigo;
            //Verifica se existe algum codigo gravado. Caso não tenha o codigo é 1, 
            //caso tenha outro codigo gravado acrescenta mais um ao codigo.
            if (retorna == null || (retorna.equals(""))) {
                acrescentaCodigo = 1;
            } else {
                acrescentaCodigo = Integer.parseInt(retorna);
                acrescentaCodigo++;
            }

            //Recebe o valor do proximo código que será gravado.
            retorna = String.valueOf(acrescentaCodigo);

        }

        return retorna;
    }

    private final HashMap<String, HashMap<String, String>> relacaoTabelasColunasDestinoOrigem = new HashMap<>();
    /**
     * Valores default
     */
    private final HashMap<String, HashMap<String, String>> valoresDefault = new HashMap<>();
    /**
     * Relaçao dos filtros de caracteres
     */
    private final HashMap<String, HashMap<String, String>> filtroCaracteres = new HashMap<>();
    /**
     * Tamanho maximo aceito por campo
     */
    private final HashMap<String, HashMap<String, Integer>> tamanhoMaximoAceito = new HashMap<>();
    /**
     * Quais campos devem ser completados com zero a esquerda
     */
    private final HashMap<String, HashMap<String, String>> aplicarZeroEsquerda = new HashMap<>();
    /**
     * Quais campos devem gerar codigos.
     */
    private final HashMap<String, HashMap<String, String>> gerarCodigos = new HashMap<>();

    private final HashMap<String, ArrayList<String>> camposDestino = new HashMap<>();

    /**
     * Armazena a relação das tabelas de destinos que estão limpas
     */
    private final HashMap<String, Boolean> tabelasDestinoLimpas = new HashMap<>();
    /*
     * Quais campos deve buscar suas informações em uma origem estrangeira
     */
    private final HashMap<String, HashMap<String, String[]>> origemEstrangeira = new HashMap<>();

    /**
     * Valores condicionais
     */
    private final HashMap<String, HashMap<String, HashMap<String, String>>> valoresCondicionais = new HashMap<>();
    /**
     * PKs da tabela
     */
    private final HashMap<String, ArrayList<String>> chavesPrimarias = new HashMap<>();
    /**
     * FKs da tabela
     */
    private final HashMap<String, HashMap<String, String[]>> chavesEstrangeiras = new HashMap<>();

    /**
     * UNIQUEs da tabela
     */
    private final HashMap<String, ArrayList<String>> uniques = new HashMap<>();

    /**
     * Registros não mogrados.
     */
    private final ArrayList<HashMap<String, String>> naoMigrados = new ArrayList<>();

    /**
     * Hashmap com os valores dos campos uniques da tabela utilizada.
     */
    private final HashMap<String, HashMap<String, ArrayList<String>>> cacheValoresUnique = new HashMap<>();

    /**
     * Retonar o hashmap das delaçoes
     *
     * @param tabelaDestino
     * @param tabelaOrigem
     * @return
     */
    public HashMap<String, String> getRelacaoTabelasColunasDestinoOrigem(String tabelaDestino, String tabelaOrigem) {
        String key = this.makeKey(tabelaDestino, tabelaOrigem);
        return relacaoTabelasColunasDestinoOrigem.get(key);
    }

    /**
     * Realiza a configuraçao da relação coluna origem X coluna destino, valores
     * default e zeros a esquerda
     *
     * @param tabela String - tabela de destino
     * @param tabelaOrigem String - tabela de origem
     * @return
     */
    protected boolean confRelacaoDestinoOrigemDados(String tabela, String tabelaOrigem) throws Exception {
        String key = this.makeKey(tabela, tabelaOrigem);
        String and = "AND ORIGEM_TABELA IS NULL";
        if (tabelaOrigem != null) {
            and = "AND (ORIGEM_TABELA = '" + tabelaOrigem + "' OR ORIGEM_TABELA_CAMPO IS NULL)";

        }
        if (this.relacaoTabelasColunasDestinoOrigem.get(key) == null
                || this.aplicarZeroEsquerda.get(key) == null
                || this.valoresDefault.get(key) == null
                || this.filtroCaracteres.get(key) == null
                || this.gerarCodigos.get(key) == null
                || this.origemEstrangeira.get(key) == null) {

            ResultSet result = this.conn.select("SELECT DADO_ESTRANGEIRO,VALOR_DEFAULT,FILTRO_CARACTERE,ZEROS_ESQUERDA,ORIGEM_TABELA_CAMPO, DESTINO_TABELA_CAMPO, GERAR_CODIGOS FROM FRR_IMPORTACAO_RELACAO WHERE IMPORTACAO_CODIGO = '" + numImportacao + "' AND DESTINO_TABELA = '" + tabela + "' " + and + " GROUP BY DADO_ESTRANGEIRO,VALOR_DEFAULT,FILTRO_CARACTERE,ZEROS_ESQUERDA,ORIGEM_TABELA_CAMPO, DESTINO_TABELA_CAMPO, GERAR_CODIGOS");
            HashMap<String, String> tmp = new HashMap<>();
            HashMap<String, String> tmp2 = new HashMap<>();
            HashMap<String, String> tmp3 = new HashMap<>();
            HashMap<String, String> tmp4 = new HashMap<>();
            HashMap<String, String> tmp5 = new HashMap<>();
            HashMap<String, String[]> tmp6 = new HashMap<>();
            while (result.next()) {
                String d = result.getString("DESTINO_TABELA_CAMPO");
                tmp.put(d, result.getString("ORIGEM_TABELA_CAMPO"));
                tmp2.put(d, result.getString("ZEROS_ESQUERDA"));
                tmp3.put(d, result.getString("VALOR_DEFAULT"));
                tmp4.put(d, result.getString("FILTRO_CARACTERE"));
                tmp5.put(d, result.getString("GERAR_CODIGOS"));
                String est = result.getString("DADO_ESTRANGEIRO");
                if (est != null) {
                    String[] t = est.split("\\|");
                    String[] t2 = t[2].split(",");

                    tmp6.put(d + "-" + result.getString("ORIGEM_TABELA_CAMPO"), new String[]{
                        t[0].replace("\\", "/"),//Base
                        t[1],//Tabela de comparação
                        t2[0],//Coluna de comparação
                        t2[1],//Coluna de resultado
                    });
                }
            }
            this.relacaoTabelasColunasDestinoOrigem.put(key, tmp);
            this.aplicarZeroEsquerda.put(key, tmp2);
            ///this.aplicarZeroEsquerda.put(tabela + "-", tmp2);//Pois em teoria sempre deve ser aplicada para a mesma origem

            this.valoresDefault.put(key, tmp3);
            this.filtroCaracteres.put(key, tmp4);
            this.gerarCodigos.put(key, tmp5);
            this.origemEstrangeira.put(key, tmp6);
        }
        if (this.tabelasDestinoLimpas.get(key) == null) {
            ResultSet result = destino.select("SELECT COUNT(*) AS L FROM " + tabela);
            if (result.next()) {
                this.tabelasDestinoLimpas.put(key, result.getInt("L") > 0);
            } else {
                this.tabelasDestinoLimpas.put(key, false);
            }
        }

        return true;
    }

    private final HashMap<String, HashMap<String, HashMap<String, String>>> cacheValoresChavesAlterados = new HashMap<>();

    private HashMap<String, String> getInCacheValoresChaveAlterados(String tabelaOrigem, String campoDestino) {
        HashMap<String, HashMap<String, String>> get = this.cacheValoresChavesAlterados.get(tabelaOrigem);
        if (get == null) {
            get = new HashMap<>();
        }

        return get.get(campoDestino);
    }
    private boolean cacheValoresAlterados = false;

    private void loadInCacheValoresAlterados() throws SQLException, Exception {
        if (!this.cacheValoresAlterados) {
            ResultSet select = this.conn.select("SELECT r.* FROM FRR_IMPORTACAO_ALTERADOS r");
            while (select.next()) {
                String[] o = select.getString("ORIGEM").replace("]", "").replace("<", "").replace(">", "").split(",");
                String[] d = select.getString("DESTINO").replace("]", "").replace("<", "").replace(">", "").split(",");
                this.alimentaHashsComuns(d[1], o[1]);
                this.addInCacheValoresChaveAlterados(d[1], o[1], new HashMap() {
                    {
                        put(d[2], select.getString("DESTINO_RESULTADO"));
                    }
                }, new HashMap() {
                    {
                        put(d[2], select.getString("ORIGEM_RESULTADO"));
                    }
                });
            }
            this.cacheValoresAlterados = true;
        }
    }

    /**
     * Armazena as Pks em cache para uso posterior. A key de cada registro é o
     * seu valor antigo
     *
     * @param tabela String -
     * @param atual
     * @param original
     */
    private void addInCacheValoresChaveAlterados(String tabela, String tabelaOrigem, HashMap<String, String> atual, HashMap<String, String> original) {
        ArrayList<String> pks = this.chavesPrimarias.get(tabela);
        HashMap<String, HashMap<String, String>> get = this.cacheValoresChavesAlterados.get(tabelaOrigem);
        if (get == null) {
            get = new HashMap<>();
        }
        for (Map.Entry<String, String> c : atual.entrySet()) {
            if (pks.contains(c.getKey())) {
                HashMap<String, String> vals = get.get(c.getKey());
                if (vals == null) {
                    vals = new HashMap<>();
                }
                vals.put(original.get(c.getKey()), c.getValue());

                get.put(c.getKey(), vals);
            }
        }

        this.cacheValoresChavesAlterados.put(tabelaOrigem, get);
    }

    /**
     * Realiza o tratamento para registros duplicados. De forma automática o
     * método identifica se o registro é duplicado, e caso sim, já altera as PKs
     * para não gerar problemas
     *
     * @param tabela String - Tabela tratada
     * @param dados HashMap<String, String> - Valores que seriam gravados na
     * base
     * @return HashMap<String, String> - Valores então tratados, quando houve a
     * necessidade
     */
    private HashMap<String, String> trataRegistrosDuplicados(String tabela, String tabelaOrigem, HashMap<String, String> dados) throws SQLException {
        String key = this.makeKey(tabela, tabelaOrigem);
        //Se a tabela está limpa não existe o pq de ter que validar
        if (!this.tabelasDestinoLimpas.get(key)) {

            return dados;
        }
        //Para impedir chaves duplicadas
        ArrayList<String> where = new ArrayList<>();
        String f = "";
        for (String a : this.chavesPrimarias.get(tabela)) {
            if (dados.get(a) != null && !dados.get(a).trim().isEmpty()) {
                where.add(" " + a + " = '" + dados.get(a) + "'");
                f = a;
            }
        }
        String t = "";
        if (where.size() > 0) {
            t = "WHERE" + String.join(" AND ", where);
        }
        ResultSet e = this.destino.select("SELECT COALESCE(COUNT(" + f + "),0) AS E FROM " + tabela + " " + t);
        if (e.next() && e.getInt("E") > 0) {

            //Não aplica-se o processo de re-calculo de chave para campos que não são integers
            e = this.destino.select("SELECT FIRST 1 " + f + " FROM " + tabela + " ORDER BY " + f + " DESC");
            if (e.next()) {
                if (!isDouble(e.getString(f))) {
                    return null;
                }
            }
            ResultSet select = this.destino.select("SELECT COALESCE(MAX( CAST(" + f + " AS INTEGER)), 0) + 1 AS PK FROM " + tabela);
            if (select.next()) {

                dados.put(f, select.getString("PK"));

            }

        }

        return dados;
    }

    /**
     * Impede registros marcados como UNIQUE sejam gravados..
     *
     * @param tabela
     * @param dados
     * @return
     */
    private String allowNewUnique(String tabela, HashMap<String, String> dados, boolean isInsert) throws SQLException {

        //verifica valores que irão ser gravados no banco já existem em um campo unique.
        HashMap<String, ArrayList<String>> camposValores = cacheValoresUnique.get(tabela);
        if (camposValores != null) {
            for (Map.Entry<String, String> entrySet : dados.entrySet()) {
                String key = entrySet.getKey();
                String value = entrySet.getValue();

                if (camposValores.get(key) != null) {
                    ArrayList<String> valores = camposValores.get(key);
                    for (String valor : valores) {
                        if (value != null) {
                        }
                        if (valor.equals(value)) {
                            return key + " = " + value;
                        }
                    }
                }
            }
            return null;
        }

        //Para impedir UNIQUES duplicados
        ArrayList<String> where = new ArrayList<>();
        this.uniques.get(tabela).stream().filter((a) -> (dados.get(a) != null)).forEach((a) -> {
            where.add(" " + a + " = '" + dados.get(a) + "'");
        });
        ArrayList<String> whereMore = new ArrayList<>();
        if (!isInsert) {
            //Em caso de UPDATE não pode existir outra unique que não seja essa PK
            this.chavesPrimarias.get(tabela).stream().filter((a) -> (dados.get(a) != null)).forEach((a) -> {
                whereMore.add(" " + a + " != '" + dados.get(a) + "'");
            });
        }
        if (where.isEmpty()) {
            return null;
        }

        return null;
    }

    /**
     * Armazena as chaves estrangeiras já verificadas
     */
    private final HashMap<String, Boolean> fksVerificadas = new HashMap<>();

    /**
     * Impede que registros com FK inválida sejam gravados na base
     *
     * @param tabela String - Tabela
     * @param dados
     * @return
     */
    private String allowForeignKey(String tabela, HashMap<String, String> dados) throws SQLException {
        for (Map.Entry<String, String[]> c : this.chavesEstrangeiras.get(tabela).entrySet()) {
            String makeKey = c.getValue()[0] + "-" + c.getValue()[1] + "-" + dados.get(c.getKey());
            if (this.fksVerificadas.get(makeKey) != null) {
                if (this.fksVerificadas.get(makeKey)) {
                    continue;
                } else {
                    return c.getValue()[0] + " = " + dados.get(c.getKey());
                }
            }

            ResultSet se = this.destino.select("SELECT COUNT(*) AS F FROM " + c.getValue()[0] + " WHERE " + c.getValue()[1] + " = '" + dados.get(c.getKey()) + "'");
            if (se.next()) {
                if (se.getInt("F") == 0) {
                    this.fksVerificadas.put(makeKey, false);
                    return c.getValue()[0] + " = " + dados.get(c.getKey());
                } else {
                    this.fksVerificadas.put(makeKey, true);
                }

            }
        }

        return null;
    }

    /**
     * Auxilia os métodos que necessitam do this.chavesPrimarias
     *
     * @param tabela
     */
    private void alimentaHashsComuns(String tabela, String tabelaOrigem) throws Exception {
        String key = this.makeKey(tabela, tabelaOrigem);
        if (this.chavesPrimarias.get(tabela) == null
                || this.uniques.get(tabela) == null
                || this.chavesEstrangeiras.get(tabela) == null
                || this.tamanhoMaximoAceito.get(tabela) == null
                || this.camposDestino.get(key) == null) {
            ArrayList<String> tmp = new ArrayList<>();
            ArrayList<String> tmp2 = new ArrayList<>();
            ArrayList<String> tmp5 = new ArrayList<>();
            HashMap<String, Integer> tmp3 = new HashMap<>();
            HashMap<String, String[]> tmp4 = new HashMap<>();
            for (ModelColunaTabela c : getColunas(tabela, destino)) {

                tmp5.add(c.getNome());
                if (c.isPrimaryKey()) {
                    tmp.add(c.getNome());
                }
                if (c.isUnique()) {
                    tmp2.add(c.getNome());
                }
                tmp3.put(c.getNome(), Integer.parseInt(String.valueOf(c.getTamanho())));
                if (c.isForeignKey()) {
                    if (tmp.size() > 1) {
                        //Como um FK pode ser um PK, isso pode gerar problemas ao 
                        //re-calcular o ID para registros duplicados
                        tmp.remove(c.getNome());
                    }
                    tmp4.put(c.getNome(), new String[]{
                        c.getReferNameTableForeignKey(),//tabela
                        c.getReferNameColumnForeignKey()//coluna
                    });
                    String preMakeKey = c.getReferNameTableForeignKey() + "-" + c.getReferNameColumnForeignKey() + "-";

                    ResultSet se = this.destino.select("SELECT " + c.getReferNameColumnForeignKey() + " AS PRE_FK FROM " + c.getReferNameTableForeignKey());
                    while (se.next()) {

                        this.fksVerificadas.put(preMakeKey + se.getString("PRE_FK"), true);
                    }

                }
            }
            this.camposDestino.put(key, tmp5);
            this.chavesPrimarias.put(tabela, tmp);
            this.uniques.put(tabela, tmp2);
            this.montaHashMapValoresUniques(tabela, tmp2);
            this.tamanhoMaximoAceito.put(tabela, tmp3);
            this.chavesEstrangeiras.put(tabela, tmp4);
        }
    }

    /**
     * Pega os valores de campos uniques já gravados no banco de dados e envia
     * para o método addInCacheValoresUnique para preencher o
     * cacheValoresUnique.
     *
     * @param tabela - Tabela utilizada.
     * @param colunasUnique - Array de colunas unique da tabela
     * @throws Exception
     */
    private void montaHashMapValoresUniques(String tabela, ArrayList<String> colunasUnique) throws Exception {

        for (String coluna : colunasUnique) {
            ResultSet rs = destino.select("SELECT " + coluna + " FROM " + tabela + " WHERE " + coluna + " IS NOT NULL");
            while (rs.next()) {
                this.addInCacheValoresUnique(tabela, new HashMap() {
                    {
                        put(coluna, rs.getString(coluna));
                    }
                });
            }
        }

    }

    /**
     * Método utilizado para preencher o HashMap cacheValoresUnique com os
     * valores ja gravados no banco de campos unique.
     *
     * @param tabela - tabela sendo utilizada.
     * @param valoresGravados - Key = coluna - Value = valor da coluna.
     */
    private void addInCacheValoresUnique(String tabela, HashMap<String, String> valoresGravados) {
        ArrayList<String> colunasUnique = this.uniques.get(tabela);

        HashMap<String, ArrayList<String>> get = this.cacheValoresUnique.get(tabela);
        if (get == null) {
            get = new HashMap<>();
        }

        for (String u : colunasUnique) {
            if (valoresGravados.get(u) != null) {
                ArrayList<String> get1 = get.get(u);
                if (get1 == null) {
                    get1 = new ArrayList<>();
                }
                get1.add(valoresGravados.get(u));
                get.put(u, get1);
            }
        }

        cacheValoresUnique.put(tabela, get);
    }

    /**
     * Método utilizado para gravar no banco de dados o historico de importações
     * efetuadas.
     */
    protected void registraHistorico() throws Exception {

        //HashMap utilizado para enviar os dados para o banco.
        HashMap<String, String> informacoesHistorico = new HashMap<>();

        //Select utilizado para obter os arquivos que estão sendo utilizados na importação.
        ResultSet result1 = conn.select("SELECT ORIGEM_BD, DESTINO_BD FROM FRR_IMPORTACAO WHERE IMPORTACAO_CODIGO = " + numImportacao);

        //Joga as informações da base dentro do HashMap.
        if (result1.next()) {
            informacoesHistorico.put("IMPORTACAO_CODIGO", numImportacao);
            informacoesHistorico.put("ORIGEM_BD", result1.getString("ORIGEM_BD"));
            informacoesHistorico.put("DESTINO_BD", result1.getString("DESTINO_BD"));
        }
//
//        //Select utilizado para obter o maior codigo gravado na base para definir o proximo codigo.
//        ResultSet result = conn.select("SELECT (COALESCE(MAX ( CAST ( HISTORICO_NUMERO AS INTEGER)), 0) + 1) AS HISTORICO_NUMERO FROM FRR_IMPORTACAO_HISTORICO");
//
//        //Variavel utilizada para definir o proximo codigo a ser gravado.
//        int historicoNumero = 0;
//
//        if (result.next()) {
//            //Verifica se o valor da base não volta nula. Caso volte é atribuido o valor 1 a variavel.
//            if (result.getString("HISTORICO_NUMERO") != null) {
//                historicoNumero = Integer.parseInt(result.getString("HISTORICO_NUMERO"));
//            } else {
//                historicoNumero = 1;
//            }
//        } else {
//            historicoNumero = 1;
//        }

        informacoesHistorico.put("HISTORICO_NUMERO", this.historicoImportacao);
        //Variavel utilizada para pegar a data atual do sistema.
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
        Date date = new Date();

        informacoesHistorico.put("DATA_IMPORTACAO", dateFormat.format(date));

        this.conn.insertComposto(true, "FRR_IMPORTACAO_HISTORICO", new HashMap<>(informacoesHistorico));

        if (!naoMigrados.isEmpty()) {
            for (HashMap<String, String> registros : naoMigrados) {
                this.conn.insertComposto(true, "FRR_IMPORTACAO_NAO_MIGRADOS", new HashMap<>(registros));
            }
        }
    }

    private void confValoresCondicionais(String tabela, String tabelaOrigem) throws SQLException {
        String key = this.makeKey(tabela, tabelaOrigem);
        if (this.valoresCondicionais.get(key) == null) {

            HashMap<String, HashMap<String, String>> tmp = new HashMap<>();
            HashMap<String, Integer> tmp2 = new HashMap<>();
            ResultSet result = this.conn.select("SELECT INDICE_RELACAO,ORIGEM_TABELA_CAMPO, DESTINO_TABELA_CAMPO FROM FRR_IMPORTACAO_RELACAO WHERE IMPORTACAO_CODIGO = '" + numImportacao + "' AND DESTINO_TABELA = '" + tabela + "' AND ORIGEM_TABELA = '" + tabelaOrigem + "' GROUP BY ORIGEM_TABELA_CAMPO, DESTINO_TABELA_CAMPO,INDICE_RELACAO");
            while (result.next()) {
                tmp2.put(result.getString("DESTINO_TABELA_CAMPO") + "-" + result.getString("ORIGEM_TABELA_CAMPO"), result.getInt("INDICE_RELACAO"));
            }
            for (Map.Entry<String, Integer> e : tmp2.entrySet()) {

                result = this.conn.select("SELECT r.ORIGEM_DADO, r.DESTINO_DADO FROM FRR_IMPORTACAO_CONDICIONAIS r WHERE r.IMPORTACAO_CODIGO = ? AND r.INDICE_RELACAO = ?", new ArrayList<>(Arrays.asList(numImportacao, e.getValue())));
                HashMap<String, String> tmp3 = new HashMap<>();
                while (result.next()) {
                    tmp3.put(result.getString("DESTINO_DADO"), result.getString("ORIGEM_DADO"));
                }
                tmp.put(e.getKey(), tmp3);
            }
            this.valoresCondicionais.put(key, tmp);
        }
    }

    private void montaRegistroNaoMigrado(HashMap<String, String> registros, String tabelaOrigem, String tipoErro, String colunaValor) throws Exception {

        ArrayList<String> resultado = new ArrayList<>();

        registros.entrySet().stream().forEach((entrySet) -> {
            String key = entrySet.getKey();
            String value = entrySet.getValue();

            resultado.add("{<" + key + ">}" + "\"" + value + "\"");
        });

        colunaValor = "{<" + colunaValor.split("=")[0] + ">}" + colunaValor.split("=")[1];

        HashMap<String, String> d = new HashMap<>();
        d.put("IMPORTACAO_CODIGO", numImportacao);
        d.put("ORIGEM_TABELA", tabelaOrigem);
        d.put("HISTORICO_NUMERO", historicoImportacao);
        d.put("REGISTRO", String.join("", resultado));
        d.put("BLOQUEIO_TIPO", tipoErro);
        d.put("BLOQUEIO_INFORMACAO", colunaValor);

        naoMigrados.add(d);

    }
}
