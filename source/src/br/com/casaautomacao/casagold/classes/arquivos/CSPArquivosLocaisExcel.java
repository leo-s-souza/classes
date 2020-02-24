/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Classe para manipular arquivos do MS Excel (.xls, .xlsx)
 *
 *
 * @author Leonardo Schwarz de Souza <producao4@casaautomacao.com.br>
 * @date 28/09/2015 - 10:24:31
 */
@Deprecated
public class CSPArquivosLocaisExcel extends CSPArquivosLocais {

    /**
     * Classe para manipular arquivos do MS Excel (.xls, .xlsx)
     *
     *
     * @param caminho String - Caminho absoluto
     */
    @Deprecated
    public CSPArquivosLocaisExcel(String caminho) {
        super(caminho);
    }

    /**
     * Pega as informações do arquivo (xls ou xlsx) setado no caminho e volta
     * uma String[][]
     *
     * @return
     */
    @Deprecated
    public Object[][] getInformacoes() {
        return this.getInformacoes(0);
    }

    /**
     * Retorna o objeto do workbook do arquivo
     *
     * @return Workbook
     */
    @Deprecated
    public Workbook getWorkbook() {
        Workbook workbook;
        try {
            if (this.getAbsolutePath().toLowerCase().contains(".xlsx")) {
                workbook = new XSSFWorkbook(this.objFileInputStream());
            } else {
                workbook = new HSSFWorkbook(this.objFileInputStream());
            }
        } catch (IOException ex) {
            CSPException.register(ex);
            return null;
        }
        return workbook;
    }

    /**
     * Retorna as informações do arquivo
     *
     * @param indexSheet int - Indice da planilha dentro do arquivo
     * @return
     */
    @Deprecated
    public Object[][] getInformacoes(int indexSheet) {

        int quantColunas = 0;

        //Verifica se o arquivo escolhido e xls ou xlsx
        Workbook workbook = this.getWorkbook();

        Sheet sheet = workbook.getSheetAt(indexSheet);

        ArrayList<ArrayList<Object>> linhas = new ArrayList<>();
        for (Row row : sheet) {
            ArrayList<Object> colunas = new ArrayList<>();
            for (int cn = 0; cn < row.getLastCellNum(); cn++) {
                Cell cell = row.getCell(cn);
                if (cell != null) {
                    switch (cell.getCellType()) {
                        case Cell.CELL_TYPE_BOOLEAN:
                            colunas.add(cell.getBooleanCellValue());
                            break;
                        case Cell.CELL_TYPE_NUMERIC:
                            colunas.add(cell.getNumericCellValue());
                            break;
                        case Cell.CELL_TYPE_STRING:
                            colunas.add(cell.getStringCellValue());
                            break;
                        case Cell.CELL_TYPE_ERROR:
                            colunas.add(cell.getErrorCellValue());
                            break;
                        case Cell.CELL_TYPE_BLANK:
                            colunas.add("");
                            break;
                        default:
                            colunas.add(null);
                            break;
                    }
                } else {
                    colunas.add(null);
                }
            }
            if (colunas.size() > quantColunas) {
                quantColunas = colunas.size();
            }

            linhas.add(colunas);
        }
        /**
         * Joga os dados do arquivo selecionado em uma variavel e envia para o
         * destino;
         */
        Object[][] tmp = new Object[linhas.size()][quantColunas];
        int i = 0;
        for (ArrayList<Object> ln : linhas) {
            Object[] aux = new Object[quantColunas];

            int k = 0;
            for (Object cl : ln) {
                aux[k] = cl;
                ++k;
            }

            tmp[i] = aux;
            ++i;
        }

        return tmp;
    }

    @Deprecated
    public boolean setInformacoes(Workbook workbook) {
        try {
            FileOutputStream fos = new FileOutputStream(this.getAbsolutePath());
            workbook.write(fos);
            fos.close();
        } catch (IOException e) {
            CSPException.register(e);
            return false;
        }

        return true;
    }

    @Deprecated
    public boolean setInformacoes(Object[][] dados) {
        try {
            Workbook workbook = new HSSFWorkbook();
            Sheet sheet = workbook.createSheet();

            int rowIndex = 0;
            for (Object[] ln : dados) {
                Row row = sheet.createRow(rowIndex++);

                int cellIndex = 0;
                for (Object cl : ln) {
                    if (cl != null) {

                        row.createCell(cellIndex++).setCellValue(cl.toString());
                    } else {
                        row.createCell(cellIndex++).setCellValue("");
                    }
                }
            }

            setInformacoes(workbook);
        } catch (Exception e) {
            CSPException.register(e);
            return false;
        }

        return true;
    }

}
