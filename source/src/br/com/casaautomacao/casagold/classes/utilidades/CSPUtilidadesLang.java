/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidades;

import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

/**
 * Métodos de auxilio para linguagem em um contexto geral
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 04/04/2016 - 14:48:17
 */
public abstract class CSPUtilidadesLang {

    private static Pattern VALID_EMAIL = null;

    static {
        VALID_EMAIL = Pattern.compile("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    }

    /**
     * Completa com determinado caracter a String
     *
     * @param text String - Texto a ser completado
     * @param qtde int - Quantidade de caracteres necessários
     * @param caracter String - Caracter que preencherá
     * @return
     */
    public static String pad(String text, int qtde, String caracter) {
        return pad(text, qtde, caracter, true);
    }

    /**
     * Completa com determinado caracter a String
     *
     * @param text String - Texto a ser completado
     * @param qtde int - Quantidade de caracteres necessários
     * @param caracter String - Caracter que preencherá
     * @param inLeft boolean - Determina se será da esquerda para direita o
     * preenchimento
     * @return
     */
    public static String pad(String text, int qtde, String caracter, boolean inLeft) {
        String vTemp = "";

        for (int x = 0; x < qtde - text.length(); x++) {
            vTemp = vTemp + caracter;
        }

        if (inLeft) {
            return vTemp + text;
        } else {
            return text + vTemp;
        }
    }

    /**
     * Retorna o MD5 da String passada.
     *
     * @param md5
     * @return
     */
    public static String getMd5(String md5) {
        return DigestUtils.md5Hex(md5);
    }

    /**
     * Retorna a String com a primeira letra maiúscula
     *
     * @param subject String - String a ser tratada
     * @return String Tratada
     */
    final public static String ucfirst(String subject) {
        subject = subject.trim();
        if (subject.length() > 1) {
            return Character.toUpperCase(subject.charAt(0)) + subject.substring(1);
        } else {
            return subject.toUpperCase();
        }
    }

    /**
     * Retorna a String com a primeira letra minúscula
     *
     * @param subject String - String a ser tratada
     * @return String Tratada
     */
    final public static String lcfirst(String subject) {
        subject = subject.trim();
        if (subject.length() > 1) {
            return Character.toLowerCase(subject.charAt(0)) + subject.substring(1);
        } else {
            return subject.toLowerCase();
        }
    }

    /**
     * "Normaliza" a String. Substitui todos os caracteres especiais por
     * caracteres no padrão ASCII, além de remover caracteres indesejados. <br/>
     * Ao final das contas só são aceitos os caractes: <br/>
     * A-Z<br/>a-z<br/>0-9<br/>-<br/>/<br/>_<br/>.<br/>(espaço em branco)
     *
     * @param input String - Texto a ser "normalizado"
     * @return String - Texto normalizado
     */
    final public static String superNormalizeString(String input) {
        return superNormalizeString(input, "");
    }

    /**
     * "Normaliza" a String. Substitui todos os caracteres especiais por
     * caracteres no padrão ASCII, além de remover caracteres indesejados. <br/>
     * Ao final das contas só são aceitos os caractes: <br/>
     * A-Z<br/>a-z<br/>0-9<br/>-<br/>/<br/>_<br/>.<br/>(espaço em branco)
     *
     * @param input String - Texto a ser "normalizado"
     * @param accept String - Caracteres do padrão ASCII que podem ser aceitos
     * além do defautl
     * @return String - Texto normalizado
     */
    final public static String superNormalizeString(String input, String accept) {
        if (input != null) {
            input = input.replace("\\", "/").trim();
            String[] s = Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").split("(?!^)");
            String listaBranca = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789-/_. " + accept;
            StringBuilder end = new StringBuilder();
            for (String a : s) {
                if (listaBranca.contains(a) || listaBranca.toLowerCase().contains(a)) {
                    end.append(a);
                }
            }
            return end.toString().trim();
        } else {
            return "";
        }
    }

    /**
     * Remove os caracteres especiais da String.
     *
     * @param string String a ser formatada.
     * @return String
     */
    public static String removerCaracteresEspeciais(String string) {
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        string = string.replaceAll("[^\\p{ASCII}]", "");
        return string;
    }

    /**
     * Retorna se a string é um Double
     *
     * @param valor String - String a ser verificada
     * @return
     */
    public static boolean isDouble(String valor) {
        if (valor != null && !valor.trim().isEmpty()) {

            return valor.trim().matches("^[0-9]*\\.?[0-9]*$");
        }
        return false;
    }

    /**
     * Retorna se a string é um int
     *
     * @param valor String - String a ser verificada
     * @return
     */
    public static boolean isInt(String valor) {
        if (valor != null && !valor.trim().isEmpty()) {
            try {
                Integer.parseInt(valor);
                return true;
            } catch (NumberFormatException ex) {
            }
        }
        return false;
    }

    /**
     * Retorna se a string é um Boolean
     *
     * @param valor String - String a ser verificada
     * @return
     */
    public static boolean isBoolean(String valor) {

        if (valor != null && !valor.trim().isEmpty()) {

            valor = valor.trim().toLowerCase();

            return (valor.equals("true") || valor.equals("false"));
        }

        return false;
    }

    /**
     * Realiza um substring de forma segura
     *
     * @param valor String - String a ser verificada
     * @param start
     * @param stop
     * @return
     */
    public static String substring(String valor, int start, int stop) {
        if (valor != null) {
            valor = valor.trim();
            if (stop > valor.length()) {
                stop = valor.length();
            }
            if (start > valor.length()) {
                start = valor.length();
            }
            if (start < 0) {
                start = 0;
            }
            return valor.substring(start, stop);
        }
        return valor;
    }

    /**
     * Realiza a formatação padrão das casas decimais do valor
     *
     * @param val double - Valor a ser formatado
     * @return
     */
    public static String defaultDecimalFormat(double val) {
        return defaultDecimalFormat(val, 2);
    }

    /**
     * Realiza a formatação padrão das casas decimais do valor
     *
     * @param val int - Valor a ser formatado
     * @return
     */
    public static String defaultDecimalFormat(int val) {
        return defaultDecimalFormat(val, 2);
    }

    /**
     * Realiza a formatação padrão das casas decimais do valor
     *
     * @param val long - Valor a ser formatado
     * @return
     */
    public static String defaultDecimalFormat(long val) {
        return defaultDecimalFormat(val, 2);
    }

    /**
     * Realiza a formatação padrão das casas decimais do valor
     *
     * @param val double - Valor a ser formatado
     * @param numDecimal int - Número de casas decimais
     * @return
     */
    public static String defaultDecimalFormat(double val, int numDecimal) {
        return defaultDecimalFormatMaster(val, numDecimal);
    }

    /**
     * Realiza a formatação padrão das casas decimais do valor
     *
     * @param val int - Valor a ser formatado
     * @param numDecimal int - Número de casas decimais
     * @return
     */
    public static String defaultDecimalFormat(int val, int numDecimal) {
        return defaultDecimalFormatMaster(val, numDecimal);
    }

    /**
     * Realiza a formatação padrão das casas decimais do valor
     *
     * @param val long - Valor a ser formatado
     * @param numDecimal int - Número de casas decimais
     * @return
     */
    public static String defaultDecimalFormat(long val, int numDecimal) {
        return defaultDecimalFormatMaster(val, numDecimal);
    }

    private static String defaultDecimalFormatMaster(Object val, int numDecimal) {
        if (val == null) {
            return "0,00";
        }
        String r = new DecimalFormat("#." + pad("0", numDecimal, "0", true)).format(val);
        if (r.startsWith(",")) {
            r = "0" + r;
        }
        return r;
    }

    private static NumberFormat defaultCurrencyConf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    /**
     * Define a configuração padrão para o tratamento monetário do sistema
     *
     * @param def NumberFormat
     */
    public static void setDefaultCurrencyConf(NumberFormat def) {
        if (def != null) {

            CSPUtilidadesLang.defaultCurrencyConf = def;
        }
    }

    /**
     * Realiza a formatação padrão do Real (R$)
     *
     *
     *
     * @param val double - valor a ser formatado
     * @return Valor já formatado
     */
    public static String currencyRealFormat(double val) {
        return currencyRealFormat(val, CSPUtilidadesLang.defaultCurrencyConf);
    }

    /**
     * Realiza a formatação padrão do Real (R$)
     *
     *
     *
     * @param val double - valor a ser formatado
     * @param fmt NumberFormat - Formato exigido
     * @return Valor já formatado
     */
    public static String currencyRealFormat(double val, NumberFormat fmt) {
        return fmt.format(val);
    }

    /**
     * Transforma um valor monerário (em reais) para double. Exemplo: R$
     * 1.503,00 se torna 1503.00
     *
     * @param txt String - String a ser formatada
     * @return
     */
    public static double currencyRealToDouble(String txt) {
        if (txt == null || txt.trim().isEmpty()) {
            return 0;
        }
        if (txt instanceof String) {
            if (txt.equals("null")) {
                return 0;
            }
        }

        boolean negativo = false;

        if (txt.contains("-")) {
            negativo = true;
        }

        txt = txt.replace(",", ".").replaceAll("[^\\d.]", "");

        int countDot = contaChar('.', txt);
        if (countDot > 1) {
            String newTxt = "";
            for (char c : txt.toCharArray()) {
                if (c == '.') {
                    if (countDot <= 1) {
                        newTxt += c;
                    }
                    countDot--;
                } else {
                    newTxt += c;
                }
            }
            txt = newTxt;
        }

        if (negativo) {
            return Double.parseDouble("-" + txt);
        }
        return Double.parseDouble(txt);
    }

    /**
     * Metodo para verificar se no texto existe caracteres especiais. São
     * considerados caracteres especiais, todos os caracteres que forem
     * diferentes de : a->z, A->Z e 0->9
     *
     * @param text String - Texto a ser verificado
     * @return boolean - true se existir caracteres especiais
     */
    public static boolean hasCaracterEspecial(String text) {
        Pattern padrao = Pattern.compile("[a-z A-Z 0-9]*"); // A-Z a-z separados permitem "" (espaço)
        Matcher pesquisa = padrao.matcher(text);
        return !pesquisa.matches();
    }

    /**
     * Remove palavras duplicadas da String
     *
     * @param str String - String a ser tratada
     * @param divisor String - O que separa um palavra da outra
     * @return
     */
    public static String removeDuplicates(String str, String divisor) {

        if (str != null && divisor != null) {
            String[] spl = str.split(divisor);
            LinkedHashSet<String> ret = new LinkedHashSet<>();
            ret.addAll(Arrays.asList(spl));
            return String.join(divisor, ret);
        }

        return null;
    }

    /**
     * Retorna a quantidade de determinado caracter em um array de char....
     *
     * @param caractere char
     * @param text char[]
     * @return
     */
    public static int contaChar(char caractere, char[] text) {
        return contaChar(caractere, Arrays.toString(text));
        /*int count = 0;
         for (char c : text) {
         if (c == caractere) {
         count++;
         }
         }

         return count;*/
    }

    /**
     * Retorna a quantidade de determinado caracter em uma string
     *
     * @param caractere char
     * @param text String
     * @return
     */
    public static int contaChar(char caractere, String text) {
        return StringUtils.countMatches(text, caractere);

    }

    /**
     * Ordena o mapa pelo value;
     *
     * @param map
     * @return
     */
    public static Map<String, String> sortByValue(Map<String, String> map) {
        LinkedList<Map.Entry<String, String>> lista = new LinkedList<>(map.entrySet());

        Collections.sort(lista, (Map.Entry<String, String> m1, Map.Entry<String, String> m2) -> (m1.getValue()).compareTo(m2.getValue()));

        Map<String, String> result = new LinkedHashMap<>();
        lista.stream().forEach((entry) -> {
            result.put(entry.getKey(), entry.getValue());
        });

        return result;
    }

    /**
     * Capitaliza a string.
     *
     * @param str String inserida no CP
     * @return String capitalizada
     */
    public static String captalizaString(String str) {
        if (!str.trim().isEmpty()) {
            str = WordUtils.capitalizeFully(str);
            String[] word = str.trim().split(" ");
            StringBuilder strCap = new StringBuilder();

            for (String x : word) {

                switch (x.length()) {
                    case 1:
                        x = x.replace("A", "a")
                                .replace("E", "e")
                                .replace("C", "c")
                                .replace("P", "p");
                        break;

                    case 2:
                        x = x.replace("De", "de")
                                .replace("Da", "da")
                                .replace("Do", "do")
                                .replace("Ao", "ao")
                                .replace("C/", "c/")
                                .replace("P/", "p/")
                                .replace("Ml", "ml")
                                .replace("Kg", "kg")
                                .replace("Pç", "pç");
                        break;

                    case 3:
                        x = x.replace("Das", "das")
                                .replace("Dos", "dos")
                                .replace("Com", "com");
                        break;

                    case 4:
                        x = x.replace("Para", "para");
                }
                strCap.append(x);
                strCap.append(" ");
            }

            return ucfirst(strCap.toString().trim());

        } else {
            return "";
        }
    }

    /**
     * Repete uma string pela quantidade de vezes informada usando um separador
     *
     * @param str
     * @param separator
     * @param numOccurrences
     * @return
     */
    public static String repeatStringWithSeparator(String str, String separator, int numOccurrences) {
        final String[] r = new String[numOccurrences];
        for (int i = 0; i < numOccurrences; i++) {
            r[i] = str;
        }
        return StringUtils.join(r, separator);
    }

    /**
     * Aliais to {@link CSPUtilidadesLang#multipleComparationString(java.lang.String[], java.lang.String[])
     * }
     *
     * Retornará true caso o valor de aVal estiver em bList
     *
     * @param aVal String - Valores A
     * @param bList String[] - Valores B
     * @return
     */
    public static boolean multipleComparationString(String aVal, String[] bList) {
        return ArrayUtils.contains(bList, aVal);
    }

    /**
     * Auxilia na comparação de multiplos valores.
     *
     * Retornará true caso ao menos um item de aList estiver em bList
     *
     * @param aList String[] - Valores A
     * @param bList String[] - Valores B
     * @return
     */
    public static boolean multipleComparationString(String[] aList, String[] bList) {
        for (String a : aList) {
            if (ArrayUtils.contains(bList, a)) {
                return true;
            }

        }
        return false;
    }

    /**
     * Verifica o valor passado é nulo. Se for nulo, retornamos o valor
     * default.<br/>
     *
     * Tanto o valor a ser verificado, como o valor default, precisam ser do
     * mesmo tipo. <br/><br/>
     *
     * <pre>
     * <b>Exemplo:</b>
     * String base = null;
     * <b>coalesce</b>(base, "Valor Default");
     *
     * Nesse caso, como a variável "base" está nula o valor retornado seria
     * "Valor Default".
     *
     * <b>Exemplo 2:</b>
     * String base = "abc";
     * <b>coalesce</b>(base, "Valor Default");
     *
     * Nesse caso, como a variável "base" contém "abc" o valor retornado seria
     * "abc".
     * </pre>
     *
     * @param valorVerificar - Valor a ser verificado
     * @param valorDefault - Valor a ser definido caso o valorVerificar seja
     * nulo.
     * @return
     */
    public static <T> T coalesce(T valorVerificar, T valorDefault) {
        return valorVerificar != null ? valorVerificar : valorDefault;
    }

    /**
     * Interface para ajudar na implementação de 'atalhos' para o StringBuilder
     */
    public static interface StringBuilderShortcut {

        public void run(StringBuilder sb);
    }

    /**
     * Interface alternativa a Runneable
     */
    public interface SuperRunneable {

        public void run() throws Exception;
    }

    /**
     * Interface alternativa a Runneable
     */
    public interface RunneableExecutor {

        public void run(ScheduledExecutorService executor) throws Exception;
    }

    /**
     * Valida e-mail
     *
     * @param email String
     * @return
     * @throws Exception
     */
    public final static boolean isEmailValid(String email) throws Exception {
        return VALID_EMAIL.matcher(email).matches();
    }

    /**
     * Valida CPF
     *
     * @param cpf String
     * @return
     * @throws Exception
     */
    public final static boolean isCpfValid(String cpf) throws Exception {
        /*
         * Dígitos Verificadores(DV) são os dois últimos dígitos do CPF, indicados pelo "-";

         * Calculando o Primeiro Dígito Verificador
         * O primeiro dígito é calculado utilizando-se o seguinte algoritmo.
         *   1) Distribua os 9 primeiros dígitos colocando os pesos 10, 9, 8, 7, 6, 5, 4, 3, 2 da esquerda para a direita;
         *   2) Multiplique os digitos por seus relativos pesos;
         *   3) Calcule o somatório dos resultados;
         *   4) O resultado obtido será divido por 11. Considere como quociente apenas o valor inteiro, 
         o resto da divisão será responsável pelo cálculo do primeiro dígito verificador.
         *   Caso o resto da divisão seja menor que 2, o primeiro dígito verificador se torna 0, 
         caso contrário subtrai-se o valor obtido de 11, ou seja(11-"resto da divisão");
         */

        if (cpf == null) {
            return false;
        }

        cpf = cpf.replaceAll("[^0-9]", "").trim();

        if (cpf.length() != 11) {
            return false;
        }

        //Etapas 1,2 e 3
        int primeiroDigito = 0;
        int peso = 10;

        //primeiroDigito += Dígito * peso
        for (int i = 0; i < 9; i++) {
            primeiroDigito += Integer.parseInt(cpf.substring(i, i + 1)) * peso--;
        }

        //Etapa 4
        primeiroDigito = primeiroDigito % 11;

        if (primeiroDigito < 2) {
            primeiroDigito = 0;
        } else {
            primeiroDigito = 11 - primeiroDigito;
        }

        /*
         *   Cálculo do Segundo Dígito
         *   1)Para o cálculo do segundo dígito será usado o primeiro dígito verificador já calculado. 
         Faça uma distribuição semelhante a anterior só que desta vez usando os pesos 11,10,9,8,7,6,5,4,3,2;
         *   2) Multiplique os valores de cada dígito com seu relativo peso e efetue o somatório dos resultados obtidos;
         *   3) Realize novamente o cálculo do módulo 11;
         *   4)Caso o valor do resto da divisão seja menor que 2, esse valor passa a ser zero, 
         caso contrário é necessário subtrair o valor obtido de 11(11 - "resto da divisão");
         */
        //Etapas 1,2 e 3
        int segundoDigito = 0;
        peso = 11;

        //segundoDigito += Dígito * peso
        for (int i = 0; i < 9; i++) {
            segundoDigito += Integer.parseInt(cpf.substring(i, i + 1)) * peso--;
        }

        segundoDigito += primeiroDigito * 2;

        //Etapa 4
        segundoDigito = segundoDigito % 11;

        if (segundoDigito < 2) {
            segundoDigito = 0;
        } else {
            segundoDigito = 11 - segundoDigito;
        }

        String dv = String.valueOf(primeiroDigito) + String.valueOf(segundoDigito);

        return cpf.substring(9, 11).equals(dv);
    }

    /**
     * Função para formatar cnpj.
     *
     * @param value
     * @return
     * @throws Exception
     */
    public final static String formatCnpj(String value) throws Exception {
        if (value == null || value.isEmpty() || value.length() < 14 || value.length() > 14) {
            throw new Exception("Conteúdo inválido");
        }

        value = value.replaceFirst("(\\d{2})(\\d)", "$1.$2");
        value = value.replaceFirst("(\\d{3})(\\d)", "$1.$2");
        value = value.replaceFirst("(\\d{3})(\\d)", "$1/$2");
        return value.replaceFirst("(\\d{4})(\\d)", "$1-$2");
    }

    /**
     * Valida CNPJ
     *
     * @param cnpj String
     * @return
     * @throws Exception
     */
    public final static boolean isCnpjValid(String cnpj) throws Exception {
        /*
         * Dígitos Verificadores(DV) são os dois últimos dígitos do CNPJ, indicados pelo "-";

         * Calculando o Primeiro Dígito Verificador
         * O primeiro dígito é calculado utilizando-se o seguinte algoritmo.
         *   1) Distribua os 12 primeiros dígitos colocando os pesos 5,4,3,2,9,8,7,6,5,4,3,2 da esquerda para a direita;
         *   2) Multiplique os digitos por seus relativos pesos;
         *   3) Calcule o somatório dos resultados;
         *   4) O resultado obtido será divido por 11. Considere como quociente apenas o valor inteiro, 
         o resto da divisão será responsável pelo cálculo do primeiro dígito verificador.
         *   Caso o resto da divisão seja menor que 2, o primeiro dígito verificador se torna 0, 
         caso contrário subtrai-se o valor obtido de 11, ou seja(11-"resto da divisão");
         */
        if (cnpj == null) {
            return false;
        }

        cnpj = cnpj.replaceAll("[^0-9]", "").trim();

        if (cnpj.length() != 14) {
            return false;
        }

        //Etapas 1,2 e 3
        int primeiroDigito = 0;

        //primeiroDigito += Dígito * peso
        primeiroDigito += Integer.parseInt(cnpj.substring(0, 1)) * 5;
        primeiroDigito += Integer.parseInt(cnpj.substring(1, 2)) * 4;
        primeiroDigito += Integer.parseInt(cnpj.substring(2, 3)) * 3;
        primeiroDigito += Integer.parseInt(cnpj.substring(3, 4)) * 2;
        primeiroDigito += Integer.parseInt(cnpj.substring(4, 5)) * 9;
        primeiroDigito += Integer.parseInt(cnpj.substring(5, 6)) * 8;
        primeiroDigito += Integer.parseInt(cnpj.substring(6, 7)) * 7;
        primeiroDigito += Integer.parseInt(cnpj.substring(7, 8)) * 6;
        primeiroDigito += Integer.parseInt(cnpj.substring(8, 9)) * 5;
        primeiroDigito += Integer.parseInt(cnpj.substring(9, 10)) * 4;
        primeiroDigito += Integer.parseInt(cnpj.substring(10, 11)) * 3;
        primeiroDigito += Integer.parseInt(cnpj.substring(11, 12)) * 2;

        //Etapa 4
        primeiroDigito = primeiroDigito % 11;
        if (primeiroDigito < 2) {
            primeiroDigito = 0;
        } else {
            primeiroDigito = 11 - primeiroDigito;
        }

        /*
         *   Cálculo do Segundo Dígito
         *   1)Para o cálculo do segundo dígito será usado o primeiro dígito verificador já calculado. 
         Faça uma distribuição semelhante a anterior só que desta vez usando os pesos 6,5,4,3,2,9,8,7,6,5,4,3,2;
         *   2) Multiplique os valores de cada dígito com seu relativo peso e efetue o somatório dos resultados obtidos;
         *   3) Realize novamente o cálculo do módulo 11;
         *   4)Caso o valor do resto da divisão seja menor que 2, esse valor passa a ser zero, 
         caso contrário é necessário subtrair o valor obtido de 11(11 - "resto da divisão");
         */
        //Etapas 1,2 e 3
        int segundoDigito = 0;

        //segundoDigito += Dígito * peso
        segundoDigito += Integer.parseInt(cnpj.substring(0, 1)) * 6;
        segundoDigito += Integer.parseInt(cnpj.substring(1, 2)) * 5;
        segundoDigito += Integer.parseInt(cnpj.substring(2, 3)) * 4;
        segundoDigito += Integer.parseInt(cnpj.substring(3, 4)) * 3;
        segundoDigito += Integer.parseInt(cnpj.substring(4, 5)) * 2;
        segundoDigito += Integer.parseInt(cnpj.substring(5, 6)) * 9;
        segundoDigito += Integer.parseInt(cnpj.substring(6, 7)) * 8;
        segundoDigito += Integer.parseInt(cnpj.substring(7, 8)) * 7;
        segundoDigito += Integer.parseInt(cnpj.substring(8, 9)) * 6;
        segundoDigito += Integer.parseInt(cnpj.substring(9, 10)) * 5;
        segundoDigito += Integer.parseInt(cnpj.substring(10, 11)) * 4;
        segundoDigito += Integer.parseInt(cnpj.substring(11, 12)) * 3;
        segundoDigito += primeiroDigito * 2;

        //Etapa 4
        segundoDigito = segundoDigito % 11;
        if (segundoDigito < 2) {
            segundoDigito = 0;
        } else {
            segundoDigito = 11 - segundoDigito;
        }

        String dv = String.valueOf(primeiroDigito) + String.valueOf(segundoDigito);

        return cnpj.substring(12, 14).equals(dv);
    }

    /**
     * Alias para {@link CSPUtilidadesLang#extraiHostAndPath(java.lang.String) }
     *
     * @param path String - Caminho com o host
     *
     * @return
     */
    public static String[] getHostAndPathFromString(String path) {
        return extraiHostAndPath(path);
    }

    /**
     * Extrai de dentro da String um host e um caminho válido. Formatos
     * suportados atualmente(indiferente ao caracte "DIR SEPARATOR"):
     *
     * //<b>host</b>/path/to/file.txt <br/>
     * //<b>host</b>/c:/path/to/file.txt <br/>
     * /<b>host</b>/path/to/file.txt <br/>
     * /<b>host</b>/c:/path/to/file.txt <br/>
     * <b>host</b>/path/to/file.txt <br/>
     * <b>host</b>/c:/path/to/file.txt <br/>
     * /path/to/file.txt <br/>
     * c:/path/to/file.txt
     *
     *
     *
     * @param path String - Caminho com o host
     * @return [0] => host | [1] => path ([0] retornará localhost caso for um
     * endereço local ou não existir no path)
     */
    public static String[] extraiHostAndPath(String path) {
        final String[] r = new String[2];
        String h, originalPath;

        path = path
                .replace("\\", "/")
                .replace("smb:", "")
                .trim();
        originalPath = path;

        if (path.startsWith("//")) {
            path = CSPUtilidadesLang.substring(path, 2, path.length());
        }

        if (path.startsWith("/")) {
            path = CSPUtilidadesLang.substring(path, 1, path.length());
        }

        h = path.split("/")[0].trim();

        if (h.endsWith(":")) {
            h = substring(path, 0, h.length() - 1);
        }

        if (CSPUtilidadesLangRede.isHostAddress(h)) {
            r[0] = h;
            r[1] = path.replaceFirst(h, "");
        } else {
            r[0] = "localhost";
            r[1] = originalPath;
        }

        if (r[1].startsWith(":")) {
            r[1] = substring(r[1], 1, r[1].length());
        }

        {//Windows
            for (String unidade : new String[]{"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "y", "z"}) {
                if (r[1].toLowerCase().startsWith("/" + unidade + ":/")) {
                    r[1] = CSPUtilidadesLang.substring(r[1], 1, r[1].length());
                    break;
                } else if (r[1].toLowerCase().startsWith("/" + unidade + "/")) {
                    r[1] = unidade + ":" + CSPUtilidadesLang.substring(r[1], 2, r[1].length());
                    break;
                }
            }
        }
//        System.out.println(r[0]);
//        System.out.println(r[1]);

        return r;
    }

    /**
     * Retorna um inteiro randomicamente entre o range determinado
     *
     * @param min int - Minimo
     * @param max int - Maximo
     * @return
     */
    public static int getRandomNumber(int min, int max) {

        return new Random().nextInt((max - min) + 1) + min;
    }

    /**
     * Método para auxiliar na montagem de um template
     *
     * @param template String - Template com marcações no formato '{title}',
     * '{a}', '{b}'...
     * @param data HashMap<String, String> - Dados, tendo em mente que a key do
     * map é uma marcação existente no template
     * @return
     */
    public static String buildHtmlWithTemplate(String template, HashMap<String, String> data) {

        if (template == null || data == null) {
            return null;
        }

        for (Map.Entry<String, String> e : data.entrySet()) {
            if (e.getValue() == null) {
                template = template.replace("{" + e.getKey() + "}", e.getValue());
            } else {
                template = template.replace("{" + e.getKey() + "}", e.getValue());
            }
        }

        return template;

    }

    /**
     * * Método para auxiliar na montagem de um template
     *
     * @param backgroundRgb String - Cor de fundo da página
     * @param contentTemplate String - Template do coteúdo com marcações no
     * formato '{title}', '{a}', '{b}'...
     * @param data HashMap<String, String> - Dados, tendo em mente que a key do
     * map é uma marcação existente no template
     * @return
     */
    public static String buildHtmlWithTemplateSimple(String backgroundRgb, String contentTemplate, HashMap<String, String> data) {

        final StringBuilder aux = new StringBuilder();

        aux.append("<!DOCTYPE html> <html> <head>");
        aux.append("<meta charset=\"utf-8\">");
        aux.append("</head> <body style=\"background: rgb({background})\">");
        aux.append(contentTemplate);
        aux.append("</body></html>");

        data.put("background", backgroundRgb);

        return buildHtmlWithTemplate(aux.toString(), data);

    }

    /**
     * converte metros em quilômetros
     *
     * @param metros int - metros
     * @return double - quilômetros
     */
    public static Double converteMetrosEmKm(int metros) {
        if (metros != 0) {
            return (double) metros / 1000;
        }
        return 0.0;
    }

    /**
     * Executa thread com try catch
     *
     * @param th SimpleTh - Código a ser executado
     */
    public static void simpleThread(CSPUtilidadesLang.SuperRunneable th) {
        simpleThread(th, 0);
    }

    /**
     * Executa thread com try catch
     *
     * @param th CSPUtilidadesLang.SuperRunneable - Código a ser executado
     * @param interval int - Intervalo de execução (Em milissegundos)
     */
    public static void simpleThread(CSPUtilidadesLang.SuperRunneable th, int interval) {
        new Thread(() -> {
            try {
                th.run();
            } catch (Exception ex) {
                CSPException.register(ex);
            }
            if (interval > 0) {
                try {
                    Thread.sleep(interval);
                    simpleThread(th, interval);
                } catch (InterruptedException ex) {
                    CSPException.register(ex);
                }
            }
        }).start();
    }

    /**
     * Divide o array em outros menores
     *
     * @param arr Object[] - Array a ser dividido
     * @param maxByArray int - Máximo de posições por array dividido
     * @return Object[]
     */
    public static Object[][] splitArrayIntoOtherArrays(Object[] arr, int maxByArray) {

        final int numParts = arr.length / maxByArray;
        final Object[][] r = new Object[numParts + 1][maxByArray];

        int last = 0;

        for (int i = 0; i <= numParts; i++) {

            if (last >= arr.length) {
                break;
            }

            if (r[i].length <= arr.length - last) {
                System.arraycopy(arr, last, r[i], 0, r[i].length);
            } else {
                System.arraycopy(arr, last, r[i], 0, arr.length - last);

            }

            last += maxByArray;
        }

        return r;
    }

    /**
     * Formata o número de CPF para o formato com pontuação. (123.456.789-00)
     *
     * @param cpfLimpo String - Número do CPF.
     * @return String - CPF formatado.◊
     */
    public static String formataCpf(String cpfLimpo) {
        if (cpfLimpo != null) {
            String cpf = cpfLimpo.replaceAll("[^0-9]", "");

            if (!cpf.trim().isEmpty()) {
                StringBuilder cpfFinal = new StringBuilder(cpf);
                cpfFinal.insert(3, ".");
                cpfFinal.insert(7, ".");
                cpfFinal.insert(11, "-");

                return cpfFinal.toString();
            }
        }

        return null;
    }
}
