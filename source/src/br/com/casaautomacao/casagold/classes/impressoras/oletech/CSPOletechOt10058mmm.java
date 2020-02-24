/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.impressoras.oletech;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import br.com.casaautomacao.casagold.classes.impressoras.CSPImpressorasNaoFiscaisBase;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Copies;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;

/**
 * Suporte a impressora não fiscal Oletech OT-100 58mm
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
public class CSPOletechOt10058mmm extends CSPImpressorasNaoFiscaisBase {

    private PrintService impressora = null;

    /**
     * Classe para a inicialização da impressora carregando a library.
     *
     * @param conf
     * @return
     */
    @Override
    public Retorno startImpresora(Confs conf) {
        try {
            for (PrintService printService : PrinterJob.lookupPrintServices()) {
                if (printService.getName().equals(this.getPorta())) {
                    impressora = printService;
                    return Retorno.OK;
                }
            }
        } catch (Exception e) {
            CSPLog.error(e.getMessage());
        }

        return Retorno.ERRO_DE_COMUNICACAO;
    }

    @Override
    public Retorno imprimeTexto(String texto) {

        try {
            texto = CSPUtilidadesLang.superNormalizeString(texto.replaceAll("\\<.*?\\>", ""), "<>\n\f;:.,+-/*$&()=");

            if (!texto.endsWith("\f")) {
                texto = texto + "\n";
            }

            try (InputStream is = new ByteArrayInputStream(texto.getBytes("UTF8"))) {
                final PrintRequestAttributeSet pras = new HashPrintRequestAttributeSet();
                final DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
                final Doc doc = new SimpleDoc(is, flavor, null);
                final DocPrintJob job = this.impressora.createPrintJob();
                final PrintJobWatcher pjw = new PrintJobWatcher(job);

                pras.add(new Copies(1));
                job.print(doc, pras);
                pjw.waitForDone();
            }

        } catch (IOException | PrintException e) {
            CSPException.register(e);
            return Retorno.ERRO_DE_COMUNICACAO;
        }

        return Retorno.OK;
    }

    @Override
    public int getTamanhoMaxLinha() {
        return 53;
    }

    @Override
    public boolean isImpressoraValida() {
        try {
            for (PrintService printService : PrinterJob.lookupPrintServices()) {
                if (printService.getName().equals(this.getPorta())) {
                    return true;
                }
            }
        } catch (Exception e) {
            CSPException.register(e);
        }
        return false;
    }

    @Override
    public Retorno acionarGuilhotina(boolean modo) {
        return Retorno.OK;
    }

    @Override
    public Retorno imprimeCodigoBarrasCODABAR(String codigo) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Retorno getImpressoraLigada() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Retorno openSerial() {
        return Retorno.OK;
    }

    @Override
    public Retorno closeSerial() {
        return Retorno.OK;
    }

    @Override
    public Retorno imprimeTextoFormatado(String bufTras, int tipoLetra, int italic, int sublin, int expand, int enfat) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class PrintJobWatcher {

        boolean done = false;

        PrintJobWatcher(DocPrintJob job) {
            job.addPrintJobListener(new PrintJobAdapter() {

                @Override
                public void printJobCanceled(PrintJobEvent pje) {
                    allDone();
                }

                @Override
                public void printJobCompleted(PrintJobEvent pje) {
                    allDone();
                }

                @Override
                public void printJobFailed(PrintJobEvent pje) {
                    allDone();
                }

                @Override
                public void printJobNoMoreEvents(PrintJobEvent pje) {
                    allDone();
                }

                void allDone() {
                    synchronized (PrintJobWatcher.this) {
                        done = true;
                        PrintJobWatcher.this.notify();
                    }
                }
            });
        }

        public synchronized void waitForDone() {
            try {
                while (!done) {
                    wait();
                }
            } catch (InterruptedException e) {
            }
        }
    }

}
