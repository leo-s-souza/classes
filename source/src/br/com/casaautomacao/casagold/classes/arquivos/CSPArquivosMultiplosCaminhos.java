/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.arquivos;

/**
 * Auxilia na realização um for pelas pelos caminhos, onde se deseja aplicar a
 * mesma ação
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 */
public class CSPArquivosMultiplosCaminhos {

    /**
     * Auxilia na realização um for pelas pelos caminhos, onde se deseja aplicar
     * a mesma ação
     *
     * @param instance
     * @param onList
     * @param pastas
     * @throws Exception
     */
    public static void list(InterfaceCSPArquivos instance, OnList onList, String... pastas) throws Exception {
        for (int i = 0; i < pastas.length; i++) {
            String pasta = pastas[i];
            instance.setPath(pasta);
            instance.setName("");
            if (!onList.run(instance)) {
                break;
            }
        }
        if (onList instanceof OnListWithEnd) {
            ((OnListWithEnd) onList).onEnd();
        }
    }

    public interface OnList {

        /**
         *
         * @param file InterfaceCSPArquivos - Objeto do diretório a ser percorrido
         * @return boolean - Se deve ou não continuar o processo
         * @throws Exception
         */
        public boolean run(InterfaceCSPArquivos file) throws Exception;

    }

    public interface OnListWithEnd extends OnList {

        public void onEnd() throws Exception;

    }
}
