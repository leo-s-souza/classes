/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.importacao;

import br.com.casaautomacao.casagold.classes.bancodados.CSPInstrucoesSQLBase;

/**
 * Classe de importação manual de informações de integração - Com origem no
 * projeto Casa Gold
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 31/08/2016 - 09:35:00
 */
public class CSPImportacaoManualIntegracaoCasaGold extends CSPImportacaoManualIntegracao {

    public CSPImportacaoManualIntegracaoCasaGold(CSPInstrucoesSQLBase connDest, CSPInstrucoesSQLBase connOrigem) {
        super(connDest, connOrigem);
    }

}
