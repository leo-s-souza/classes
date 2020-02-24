/*
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.interpretadores;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Classe interpretadora de LOGs Brutos.
 *
 * @author Matheus Felipe Amelco <producao5@casaautomacao.com.br>
 * @date 28/06/2016 11:30:09
 */
public class CSPInterpretadorLogsBrutos {

    private InterfaceConditionStart itfConditionStart;

    private final ArrayList<InterfaceOnLineAction> itfOnLineActionBefore = new ArrayList<>();
    private final ArrayList<InterfaceOnLineAction> itfOnLineActionAfter = new ArrayList<>();

    private InterfaceOnAction itfOnActionDone;
    private InterfaceOnAction itfOnActionNotDone;

    /**
     * Define a condição para o inicio do processamento
     *
     * @param conditionStart
     */
    public void setConditionStart(InterfaceConditionStart conditionStart) {
        this.itfConditionStart = conditionStart;
    }

    /**
     * Define a condição e ação que deve ser feita ao percorrer as linhas antes
     * do "start"
     *
     * @param onLineAction
     */
    public void addOnLineActionBeforeStart(InterfaceOnLineAction onLineAction) {
        this.itfOnLineActionBefore.add(onLineAction);

    }

    /**
     * Define a condição e ação que deve ser feita ao percorrer as linhas depois
     * do "start"
     *
     * @param onLineAction
     */
    public void addOnLineActionAfterStart(InterfaceOnLineAction onLineAction) {
        this.itfOnLineActionAfter.add(onLineAction);
    }

    /**
     * Ações se o processamento obtiver sucesso.
     *
     * @param onAction
     */
    public void setOnDoneActions(InterfaceOnAction onAction) {
        this.itfOnActionDone = onAction;
    }

    /**
     * Ações se o processamento não obtiver sucesso.
     *
     * @param onAction
     */
    public void setOnNotDoneActions(InterfaceOnAction onAction) {
        this.itfOnActionNotDone = onAction;
    }

    /**
     * Executa o processo
     *
     * @param file
     * @throws Exception
     */
    public void run(CSPArquivos file) throws Exception {
        final ArrayList<String> set = new ArrayList();
        FileReader fr = null;
        LineNumberReader lnr = null;
        String linha;

        try {
            //Cria o reader
            fr = new FileReader(file.getAbsolutePath());
            lnr = new LineNumberReader(fr);
            
            //Lê todas as linhas até o final do documento
            while ((linha = lnr.readLine()) != null) {
                final int currentLine = lnr.getLineNumber();
//                System.out.println(currentLine);
                StringBuilder sb = new StringBuilder();

                if (set.size() == 50) {
                    set.remove(1);
                }

                if (set.size() < 50) {
                    set.add(linha);
                }
                
                //Verificação definida para inciar o processamento
                if (this.itfConditionStart.check(linha)) {

                    //O que deve ser feito antes do 'start'
                    for (int l = set.size() - 1; l >= 0; l--) {
                        boolean sai = false;
                        for (InterfaceOnLineAction t : this.itfOnLineActionBefore) {
                            if (t.run(set.get(l), sb)) {
                                sai = true;
                                break;
                            }
                        }
                        if (sai) {
                            break;
                        }
                    }

                    //O que deve ser feito após do 'start'
                    while ((linha = lnr.readLine()) != null) {
                        boolean sai = false;
                        for (InterfaceOnLineAction t : this.itfOnLineActionAfter) {
                            if (t.run(linha, sb)) {
                                sai = true;
                                break;
                            }
                        }
                        if (sai) {
                            break;
                        }
                    }
                    //Assim que for processado
                    this.itfOnActionDone.run(sb, file);
                    lnr.setLineNumber(currentLine);
                }
            }

        } catch (Exception e) {
            CSPException.register(e);

        } finally {
            //Fecha o reader (IMPORTANTE)
            if (fr != null) {
                fr.close();
            }
            if (lnr != null) {
                lnr.close();
            }
        }
    }

    public interface InterfaceConditionStart {

        /**
         * Condição para inicio do processamento
         *
         * @param line String- Linha atual
         * @return boolean
         * @throws Exception
         */
        public boolean check(String line) throws Exception;
    }

    public interface InterfaceOnLineAction {

        /**
         * Ação durante a interação com as linhas
         *
         * @param line String- Linha atual
         * @param toResult StringBuilder - StringBuilder de resultado
         * @return boolean - Retornando "true", para de percorrer as linhas
         * @throws Exception
         */
        public boolean run(String line, StringBuilder toResult) throws Exception;
    }

    public interface InterfaceOnAction {

        /**
         * Ação ao final do processo
         *
         * @param result StringBuilder - StringBuilder de resultado
         * @param file CSPArquivos - Arquivo que está sendo processado
         * @return boolean
         * @throws Exception
         */
        public boolean run(StringBuilder result, CSPArquivos file) throws Exception;
    }

    /**
     * Auxiliar para extrair uma informação de uma string. Se possível
     *
     * @param data HashMap<String, Object> - Hash para aplicar o valor
     * @param keyToPut String - Key para aplicar o valor no hash
     * @param line String - Linha
     * @param keyStart String - String demarcadora do inicio da informação
     * @param keyEnd String - String demarcadora do fim da informação
     * @param valDefault String - Valor para caso não encontre-se a informação,
     * ou a mesma se apresente vazia ou mesmo nula
     * @param clearRegex String - Expressão regular para limpar a string.
     * Exemplo: [^0123456789] (deixa a string somente com números)
     */
    public void extractInfoAndPut(HashMap<String, Object> data, String keyToPut, String line, String keyStart, String keyEnd, String valDefault, String clearRegex) {
        
        final String r = this.extractInfo(line, keyStart, keyEnd, valDefault, clearRegex);
        
        if (r != null && !r.trim().isEmpty()) {
        
            data.put(keyToPut, r);
        }
    }

    /**
     * Auxiliar para extrair uma informação de uma string
     *
     * @param line String - Linha
     * @param keyStart String - String demarcadora do inicio da informação
     * @param keyEnd String - String demarcadora do fim da informação
     * @param valDefault String - Valor para caso não encontre-se a informação,
     * ou a mesma se apresente vazia ou mesmo nula
     * @param clearRegex String - Expressão regular para limpar a string.
     * Exemplo: [^0123456789] (deixa a string somente com números)
     * @return
     */
    public String extractInfo(String line, String keyStart, String keyEnd, String valDefault, String clearRegex) {

        if (line != null) {

            if (line.contains(keyStart)) {

                String[] start = line.split(keyStart);

                if (start.length > 0) {

                    String r = start[1].split(keyEnd)[0];

                    if (r != null && !r.trim().isEmpty()) {

                        if (clearRegex != null) {

                            r = r.replaceAll(clearRegex, "");

                            if (r != null && !r.trim().isEmpty()) {
                                return r;
                            }
                        } else {
                            return r;
                        }
                    }
                }
            }
        }
        return valDefault;
    }

}
