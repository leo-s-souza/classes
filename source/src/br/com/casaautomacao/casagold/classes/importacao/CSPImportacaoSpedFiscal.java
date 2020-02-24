/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.InterfaceCSPArquivos;
import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Classe responsável por importar os registros do sped fiscal para o sistema
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/12/2015 - 10:17:20
 */
@Deprecated
public class CSPImportacaoSpedFiscal extends CSPImportacao implements InterfaceImportacao {

    // Key = Bloco.
    // Value = Todas as linhas do bloco.
    private HashMap<String, String[]> blocosLinhas = new HashMap<>();

    private final InterfaceCSPArquivos origem;

    /**
     * Importação de SPED fiscal
     * 
     * @param contratante CSPInstrucoesSQLBase - Conexão com a base contratante
     * @param importacao String - Número da importação
     * @param historicoImportacao - Código do histórico da importação.
     * @param destino CSPInstrucoesSQLBase - Conexão com a base de destino
     * @param origem InterfaceCSPArquivos - SPED de origem
      * @param estrangeiros HashMap<String, CSPInstrucoesSQLBase> - Bases
     * estrangeiras
     */
    public CSPImportacaoSpedFiscal(CSPInstrucoesSQLBase contratante, String importacao, String historicoImportacao, CSPInstrucoesSQLBase destino, InterfaceCSPArquivos origem, HashMap<String, CSPInstrucoesSQLBase> estrangeiros) {
        super(contratante, importacao, historicoImportacao, destino, estrangeiros);
        this.origem = origem;
        this.preencheHashMapBlocosLinhas();
    }

    private ArrayList<String[]> relacaoTabelasDestinoOrigem = new ArrayList<>();

    /**
     * Realiza a configuraçao da relação tabela origem X tabela destino
     */
    private boolean confTabelaDestinoOrigem() throws SQLException {

        ResultSet result = conn.select("SELECT ORIGEM_TABELA, DESTINO_TABELA FROM FRR_IMPORTACAO_RELACAO WHERE IMPORTACAO_CODIGO = ? GROUP BY DESTINO_TABELA, ORIGEM_TABELA", numImportacao);

        this.relacaoTabelasDestinoOrigem = new ArrayList<>();

        while (result.next()) {
            this.relacaoTabelasDestinoOrigem.add(new String[]{result.getString("DESTINO_TABELA"), result.getString("ORIGEM_TABELA")});
        }

        return true;

    }

    @Override
    public boolean run() throws Exception {
        this.arquivoOrigem = new File(this.origem.getAbsolutePath()).getName();
        
        this.destino.setAutoCommit(false);
        CSPLog.info(this.getClass(), "importação " + this.numImportacao+ " inicinado " + this.origem.getAbsolutePath() + "=>" + this.destino.getConfs().getPath()+ "...");

        //Recebe as tabelas que tem importações gravadas no codigo
        this.confTabelaDestinoOrigem();
        for (String[] i : relacaoTabelasDestinoOrigem) {

            String out = i[0];
            String in = i[1];

            //Recebe as colunas da tabelae iguala as colunas do FDB origem
            //e define em que coluna do FDB destino
            //os valores do FDB origem vão ser gravados.
            this.confRelacaoDestinoOrigemDados(out, in);

            //Variavel utilizada para receber os HashMaps para a inserção no banco de dados.
            ArrayList<HashMap<String, String>> tmp = new ArrayList<>();
            CSPLog.info(this.getClass(), "importação " + this.numImportacao+ " importando " + this.origem.getAbsolutePath() + "(" + in + ")=>" + this.destino.getConfs().getPath() + "(" + out + ")...");
            //Variavel recebe todas as linhas do bloco.
            String[] linhasBloco = this.getBloco(in);

            if (linhasBloco != null) {

                //Percorre todas as linhas do bloco.
                for (String linha : linhasBloco) {

                    //Variavel recebe todos os registros da linha.
                    String[] registrosLinhas = getRegistros(linha);

                    //HashMap usado para enviar os dados para serem gravados na tabela.
                    HashMap<String, String> dados = new HashMap<>();
                    //Percorre as colunas que vão receber os dados.
                    for (Map.Entry<String, String> e : this.getRelacaoTabelasColunasDestinoOrigem(out, in).entrySet()) {

                        //Verifica se a coluna de destino está relacionada a uma coluna de origem.
                        if (e.getValue() != null) {

                            //Variavel utilizada para pegar o numero  do campo da linha do bloco.
                            String valor = e.getValue();
                            valor = valor.substring(valor.lastIndexOf("O") + 1, valor.length());

                            //Variavel recebe o campo gravado no banco de dados.
                            //EX: CAMPO2
                            valor = registrosLinhas[Integer.parseInt(valor) - 1];
                            //Variavel pega apenas o numero gravado no final do valor.
                            //EX: CAMPO2 = 2
                            dados.put(e.getKey(), valor);
                        } else {
                            dados.put(e.getKey(), null);
                        }
                    }
                    tmp.add(dados);
                }
            }
            for (HashMap<String, String> t : tmp) {

                //envia os dados ja prontos para a gravação na tabela.
                this.efetuaImportacao(out, in, t,null);
            }
            this.destino.commit();//Commitamos por tabela
        }
        this.destino.setAutoCommit(true);
        this.registraHistorico();
        CSPLog.info(this.getClass(), "importação " + this.numImportacao+ " finalizada " + this.origem.getAbsolutePath() + "=>" + this.destino.getConfs().getPath());
        return true;
    }

    /**
     * Verifica se o origem recebido é um SPED válido.
     *
     * @return
     */
    public boolean isSped() {

        String[] linha = getBloco("0000");

        //verifica se a variavel line recebe a primeira linha do origem.
        if (!linha[0].equals("")) {

            int cont = 0;
            int posInicial = 0;
            int posFinal = 0;

            /*
             *Array utilizado para pegar os conteudos necessarios para a validação do origem;
             *
             * posição 1 = recebe o primeiro valor do origem que deve ser igual a "0000".
             * posição 2 = recebe o segundo valor do origem que deve ser validado como int.
             * posição 3 = recebe o terceiro valor do origem que deve ser validado como int.
             * posição 4 = recebe a data inicial do origem.
             * posição 5 = recebe a data final do origem.
             */
            ArrayList<String> conteudoValidacao = new ArrayList<>();

            //percorre os caracteres da variavel line verificando onde estão os | pegando os registros entre eles.
            for (int i = 0; i < linha[0].length(); i++) {
                //verifica se o caractere é um |.
                if (linha[0].charAt(i) == '|') {
                    cont++;
                    // verifica a quantidade de | que foram encontrados e define a 
                    //posição inicial e final do registro da linha.
                    if (cont == 1) {
                        posInicial = i;
                    } else if (cont != posFinal) {
                        posFinal = i;
                    }

                    // caso a posição final seja diferente da posição inicial e diferente de 0 o conteudo entre essas
                    //posições é adicionado ao array conteudoValidacao.
                    if ((posInicial != posFinal) && (posFinal != 0)) {
                        conteudoValidacao.add(linha[0].substring(posInicial + 1, posFinal));
                        posInicial = posFinal;
                    }

                    // verifica se já foram percorridas as posições necessárias na linha do origem.
                    if (cont == 6) {

                        // verifica se o primeiro registro do array é valido.
                        // obs: O primeiro registro é sempre 0000.
                        if (!conteudoValidacao.get(0).equals("0000")) {
                            return false;
                        }

                        // verifica se o segundo e terceiro registro do array são formados apenas por numeros.
                        try {
                            Integer.parseInt(conteudoValidacao.get(1));
                            Integer.parseInt(conteudoValidacao.get(2));
                        } catch (NumberFormatException ex) {
                            return false;
                        }

                        // verifica se o quarto e quinto registro do array são datas validas.
                        try {
                            new SimpleDateFormat("ddmmyyyy").parse(conteudoValidacao.get(3));
                            new SimpleDateFormat("ddmmyyyy").parse(conteudoValidacao.get(4));
                        } catch (ParseException ex) {
                            return false;
                        }
                        break;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Retorna os blocos existentes sped.
     *
     * @return
     */
    public ArrayList<String> getBlocosSped() {

        //Variavel para receber os blocos existentes no sped.
        ArrayList<String> resultado = new ArrayList<>();

        // Percorre o HashMap blocosLinhas e preenche o ArrayList resultado com as keys.
        for (Map.Entry<String, String[]> entrySet : blocosLinhas.entrySet()) {
            String key = entrySet.getKey();

            resultado.add(key);
        }

        return resultado;

    }

    /**
     * Retorna o bloco, linhas do bloco no SPED e colunas nas linhas do bloco.
     *
     * @param bloco
     * @return
     */
    public String[] getInfoBlocos(String bloco) {

        String[] resultado = new String[3];

        // Recebe as linhas contidas no bloco.
        String[] linhasBloco = getBloco(bloco);

        int campos = 0;

        // percorre a primeira linha de cada registro e preenche a variavel 
        // campos com a quantidade de campos do registro.
        for (int i = 0; i < linhasBloco[0].length(); i++) {
            if (linhasBloco[0].charAt(i) == '|') {
                campos++;
            }
        }

        // Recebe o bloco utilizado;
        resultado[0] = bloco;
        // Recebe a quantidade de campos nas linhas do bloco.
        resultado[1] = String.valueOf(campos - 1);
        // Recebe a quantidade de linhas do bloco.
        resultado[2] = String.valueOf(linhasBloco.length);

        return resultado;
    }

    /**
     * Retorna todos os registros da linha.
     *
     * @param linha
     * @return
     */
    public String[] getRegistros(String linha) {

        int cont = 0;
        int posInicial = 0;
        int posFinal = 0;

        ArrayList<String> registros = new ArrayList<>();

        for (int i = 0; i < linha.length(); i++) {
            //verifica se o caractere é um |.
            if (linha.charAt(i) == '|') {
                cont++;
                // verifica a quantidade de | que foram encontrados e define a 
                //posição inicial e final do registro da linha.
                if (cont == 1) {
                    posInicial = i;
                } else if (cont != posFinal) {
                    posFinal = i;
                }

                // caso a posição final seja diferente da posição inicial e diferente de 0 o conteudo entre essas
                // posições é adicionado ao array conteudoValidacao.
                if ((posInicial != posFinal) && (posFinal != 0)) {
                    //IF utilizado para verificar campos vazios entre |.
                    if ((posInicial + 1) == posFinal) {
                        registros.add("");
                        posInicial = posFinal;
                    } else {
                        registros.add(linha.substring(posInicial + 1, posFinal));
                        posInicial = posFinal;
                    }
                }

            }
        }

        return registros.toArray(new String[registros.size()]);

    }

    /**
     * Recebe os registros da linha com o maior registro e retorna com as
     * informações para o grid.
     *
     * @param registros
     * @param bloco
     * @return
     */
    public Object[][] getInfos(String[] registros, String bloco) {

        // Variavel recebe a linha na qual o registro se encontra.
        int linha = 0;

        // Variavel recebe as informações para serem enviadas no return;
        Object[][] resultado = new Object[registros.length][3];

        //Percorre o Array de String registros.
        for (String registro : registros) {

            // Variavel recebe na posição 0 o registro com maior tamanho.
            // Na posição 1 recebe o numero de caracteres do maior registro.
            String[] caracteres = getMaiorRegistro(this.getBloco(bloco), linha);

            // Recebe o tipo de informação que deve ser utilizada na gravação dos registros no banco.
            String tipoInformacao;

            // Verifica se a variavel registro está vazia ou nula.
            if ((!registro.equals("")) || ((caracteres[0] != null) && (!caracteres[0].equals("")))) {

                //recebe o registro a ser verificado. 
                String verificaRegistro;

                // Verificação necessária para quando a primeira linha do  bloco do SPED possui um registro vazio
                // mas em alguma linha do bloco  um dos registros da coluna a ser trabalhada  não é vazio.
                if (!registro.equals("")) {
                    verificaRegistro = registro;
                } else {
                    verificaRegistro = caracteres[0];
                }

                //verifica se o valora da variavel registro é um integer.
                try {
                    // variavel utilizada para receber o valor char do primeiro caracter da variavel registro;
                    String verificaZero = String.valueOf(verificaRegistro.charAt(0));
                    //Caso o primeiro caracter da variavel registro seja 0 e exista mais do que 
                    //um caracter na variavel é considerado um varchar.
                    if ((verificaZero.equals("0")) && (verificaRegistro.length() > 1)) {
                        tipoInformacao = "Varchar";
                    } else {
                        Integer.valueOf(verificaRegistro);
                        tipoInformacao = "Numeric";
                    }
                } catch (NumberFormatException ex) {
                    //Verificação necessária para quando o valor integer for grande demais para a verificação com um integer normal.
                    try {
                        long valor = Long.valueOf(verificaRegistro);
                        BigInteger.valueOf(valor);
                        tipoInformacao = "Numeric";
                    } catch (NumberFormatException exx) {
                        //verifica se o valor da variavel verificaRegistro é double.
                        try {
                            // Variavel necessária pois os numeros double no Sped vem com virgula, 
                            // mas para a verificação é necessário a troca da virgula or ponto.
                            String verificaDouble = verificaRegistro.replace(',', '.');
                            Double.parseDouble(verificaDouble);
                            tipoInformacao = "Real";
                        } catch (NumberFormatException exxx) {
                            //Caso nenhuma das validações acime funcione a informação é considerada varchar.
                            tipoInformacao = "Varchar";
                        }
                    }
                }

                //Verifica se o comprimento da variavel é igual a 8 para fazer a validação de data na formatação ddmmyyyy.
                if (verificaRegistro.length() == 8) {
                    try {
                        new SimpleDateFormat("ddmmyyyy").parse(verificaRegistro);
                        tipoInformacao = "Varchar";
                    } catch (ParseException e) {
                    }
                }
            } else {
                tipoInformacao = "";
                caracteres[1] = String.valueOf(0);
            }

            resultado[linha][0] = caracteres[1];
            resultado[linha][1] = tipoInformacao;
            resultado[linha][2] = registro;

            linha++;

        }

        return resultado;
    }

    /**
     * Retorna A linha com o maior Registro na posição recebida.
     *
     * @param bloco
     * @param pos
     * @return posição 0 = registro com o maior numero de caracteres | posição 1
     * = tamanho do maior registro.
     */
    public String[] getMaiorRegistro(String[] bloco, int pos) {

        int linhaAtual = 0;
        int maiorRegistro = 0;
        String[] resultado = new String[2];

        //Percorre as linhas do bloco selecionado.
        for (String bloco1 : bloco) {

            //Recebe todos os registros da linha do bloco.
            String[] registros = this.getRegistros(bloco1);

            //Verifica se a quantidade de caracteres é maior que 0.
            //Caso seja 0 ele verifica se é a primeira linha e define o maior registro.
            if (maiorRegistro < registros[pos].length()) {
                resultado[0] = registros[pos];
                maiorRegistro = resultado[0].length();
                resultado[1] = String.valueOf(maiorRegistro);
            }
            linhaAtual++;
        }
        return resultado;
    }

    /**
     * Retorna as linhas do bloco selecionado.
     *
     * @param bloco
     * @return
     */
    public String[] getBloco(String bloco) {

        return blocosLinhas.get(bloco);

    }

    /**
     * Preenche o HashMap com os blocos na key e um String[] com as linhas do
     * bloco como value.
     */
    private void preencheHashMapBlocosLinhas() {
        try {

            //variavel recebe todas as linhas do origem selecionado.
            Scanner in = new Scanner(new FileReader(origem.getAbsolutePath()));

            HashMap<String, ArrayList<String>> blocoLinhas = new HashMap<>();

            //Percorre todas as linhas recebidas do origem.
            while (in.hasNextLine()) {
                ArrayList<String> linhas = new ArrayList<>();
                //variavel recebe a linha atual do origem.
                String line = in.nextLine();

                // variavel recebe o registro da linha sendo percorrida.
                String bloco = line.substring(line.indexOf('|') + 1, line.length());
                bloco = bloco.substring(0, bloco.indexOf('|'));

                // verifica se o HashMap ja tem uma key igual a variavel registro.
                if (blocoLinhas.get(bloco) == null) {
                    linhas.add(line);
                    blocoLinhas.put(bloco, linhas);
                } else {
                    linhas = blocoLinhas.get(bloco);
                    linhas.add(line);
                    blocoLinhas.put(bloco, linhas);
                }
            }

            //Percorre o HashMap temporario e adiciona os valores no HashMap global.
            for (Map.Entry<String, ArrayList<String>> entrySet : blocoLinhas.entrySet()) {
                String key = entrySet.getKey();
                ArrayList<String> value = entrySet.getValue();

                blocosLinhas.put(key, value.toArray(new String[value.size()]));

            }

            blocoLinhas = null;

        } catch (FileNotFoundException ex) {
            CSPException.register(ex);
        }
    }

}
