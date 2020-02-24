/**
 * Copyright(c) 2015 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.rede.comunicacao;

import br.com.casaautomacao.casagold.classes.CSPLog;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.exceptions.CSPException;
import static br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao.getHostByHostBuilder;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangRede;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

/**
 * Classe para a transferencia de arquivos com as centrais
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 02/11/2015 - 08:56:41
 */
public class CSPTransferenciaArquivos extends CSPComunicacao {

    public enum Servico {

        /**
         * CMBOCV - Servico Módulo Backup Online Casa Visual - Transferência de
         * Arquivos
         */
        CMBOCV_ARQUIVOS(ServicoAuxHost.CMG, 11234),
        /**
         * CMA - Central/Serviço Módulo Atualizador - Transferência de Arquivos
         */
        CMA_ARQUIVOS(ServicoAuxHost.CMG, 11235),
        /**
         * CMAICAF - Servico Módulo Atualizações de Informações dos Contratantes
         * App Food (App Food) - Transferência de Arquivos
         */
        CMAICAF_ARQUIVOS(ServicoAuxHost.CAPP, 11236),
        /**
         * CINFOS - Servico de Inforações Gerais (categorias, restaurantes....)
         * (App Food) - Transferência de Arquivos
         */
        CINFOS_ARQUIVOS(ServicoAuxHost.CAPP, 22231),
        /**
         * CMBOB - Servico Módulo Backup Online Bases - Transferência de
         * Arquivos
         */
        CMBOB_ARQUIVOS(ServicoAuxHost.CMG, 11237),
        /**
         * CCASRI - Central/Serviço Compartilhamento Arquivos Rede Interna -
         * Transferência de Arquivos (upload)
         */
        CCASRI_ARQUIVOS_UPLOAD(ServicoAuxHost.LOCAL, 33111),
        /**
         * CCASRI - Central/Serviço Compartilhamento Arquivos Rede Interna -
         * Transferência de Arquivos (download)
         */
        CCASRI_ARQUIVOS_DOWNLOAD(ServicoAuxHost.LOCAL, 33112),
        /**
         * CCAS - Central/Serviço Compartilhamento Arquivos - Transferência de
         * Arquivos (upload)
         */
        CCAS_ARQUIVOS_UPLOAD(ServicoAuxHost.CMG, 33115),
        /**
         * CCAS - Central/Serviço Compartilhamento Arquivos - Transferência de
         * Arquivos (download)
         */
        CCAS_ARQUIVOS_DOWNLOAD(ServicoAuxHost.CMG, 33116),
        /**
         * CCAS - Central/Serviço Compartilhamento Arquivos - Transferência de
         * Arquivos (upload) - Capp
         */
        CCAS_ARQUIVOS_UPLOAD_CAPP(ServicoAuxHost.CAPP, 43115),
        /**
         * CCAS - Central/Serviço Compartilhamento Arquivos - Transferência de
         * Arquivos (download) - Capp
         */
        CCAS_ARQUIVOS_DOWNLOAD_CAPP(ServicoAuxHost.CAPP, 43116),
        /**
         * Testes - Testes com o socket, se já não estiver claro pra você
         */
        _TESTES_ARQUIVOS(ServicoAuxHost.LOCAL, 9097);

        public final ServicoAuxHost hostType;
        public final int port;
        public final String sigla;

        private Servico(ServicoAuxHost hostType, int port) {
            this.hostType = hostType;
            this.port = port;
            this.sigla = this.name();
        }

    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Servico a ser conectada
     * @param arquivo String - Nome do arquivo na servico
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivo(Servico servico, String arquivo, String destino) throws Exception {
        return this.baixaArquivo(servico, CSPComunicacao.validadeDefault, arquivo, destino);
    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Servicoa ser conectada
     * @param arquivo String - Nome do arquivo na servico
     * @param infos JSONObject - Informações adicionais
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivo(Servico servico, String arquivo, JSONObject infos, String destino) throws Exception {
        return baixaArquivoByHostBuilder(servico, validadeDefault, arquivo, infos, destino);
//        return baixaArquivoOtherHost(servico, CSPComunicacao.validadeDefault, getHostByHostBuilder(servico.hostType, servico.port, null, 0), arquivo, infos, destino);
    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     *
     * @param host String - Host do servidor
     * @param arquivo String - Nome do arquivo na servico
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivoOtherHost(Servico servico, String host, String arquivo, String destino) throws Exception {
        return baixaArquivoOtherHost(servico, CSPComunicacao.validadeDefault, host, arquivo, new JSONObject(), destino);
    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     * @param host String - Host do servidor
     * @param arquivo String - Nome do arquivo na servico
     * @param infos JSONObject - Informações adicionais
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivoOtherHost(Servico servico, String host, String arquivo, JSONObject infos, String destino) throws Exception {
        return baixaArquivoOtherHost(servico, CSPComunicacao.validadeDefault, host, arquivo, infos, destino);
    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Servico a ser conectada
     * @param arquivo String - Nome do arquivo na servico
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivo(Servico servico, ValidationLocal validade, String arquivo, String destino) throws Exception {
        return baixaArquivo(servico, validade, arquivo, new JSONObject(), destino);
    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Servicoa ser conectada
     * @param arquivo String - Nome do arquivo na servico
     * @param infos JSONObject - Informações adicionais
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivo(Servico servico, ValidationLocal validade, String arquivo, JSONObject infos, String destino) throws Exception {
        return baixaArquivoByHostBuilder(servico, validade, arquivo, infos, destino);
//        return baixaArquivoOtherHost(servico, validade, getHostByHostBuilder(servico.hostType, servico.port, null, 0), arquivo, infos, destino);
    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     *
     * @param host String - Host do servidor
     * @param arquivo String - Nome do arquivo na servico
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivoOtherHost(Servico servico, ValidationLocal validade, String host, String arquivo, String destino) throws Exception {
        return baixaArquivoOtherHost(servico, validade, host, arquivo, new JSONObject(), destino);
    }

    /**
     * Auxilia no tratamento de conexões com mais de uma tentativa possível
     *
     * @param servico
     * @param validade
     * @return
     */
    public CSPArquivosLocais baixaArquivoByHostBuilder(Servico servico, ValidationLocal validade, String arquivo, JSONObject infos, String destino) {
        CSPArquivosLocais r = null;

        for (int i = 0; i < 5; i++) {

            try {

                r = baixaArquivoOtherHost(servico, validade, getHostByHostBuilder(servico.hostType, servico.port, null, i), arquivo, infos, destino);

            } catch (Exception ex) {
                CSPException.register(ex);
                continue;
            }

            if (r != null) {
                break;
            }

        }

        return r;
    }

    /**
     * Efetua o download do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     * @param host String - Host do servidor
     * @param arquivo String - Nome do arquivo na servico
     * @param infos JSONObject - Informações adicionais
     * @param destino String - Destino do arquivo localmente
     * @return
     * @throws Exception
     */
    public CSPArquivosLocais baixaArquivoOtherHost(Servico servico, ValidationLocal validade, String host, String arquivo, JSONObject infos, String destino) throws Exception {

        final int porta = servico.port;
        final String prefix = "S(" + servico.sigla + ")<=file<=(" + host + ":" + porta + ")";

        CSPLog.info(prefix + "...");

        if (!validade.run(host, servico.port)) {
            CSPLog.error(prefix + "...ERROR(local-validation)");
            return null;
        }

        if (CSPUtilidadesLangRede.ping(host)) {
            final Socket rec = new Socket();
            try {
//                rec.connect(new InetSocketAddress(host, porta));
                rec.connect(new InetSocketAddress(host, porta), 5000);

            } catch (Exception e) {
                CSPException.register(e, "exception-no-port");
                CSPLog.error(prefix + "...ERROR(no-port)");//Porta não acessível
                return null;
            }
            CSPArquivosLocais fileDest = new CSPArquivosLocais(destino);
            if (fileDest.exists() && fileDest.isDir()) {
                fileDest.setName(FilenameUtils.getName(arquivo));
            }
            if (fileDest.exists()) {
                fileDest.delete();
            }
            try (ObjectOutputStream out = new ObjectOutputStream(rec.getOutputStream())) {
                infos.put("FILE_NAME", arquivo);
                out.writeObject(infos.toString());

                final DataInputStream in = new DataInputStream(rec.getInputStream());
                final FileOutputStream fileOut = fileDest.objFileOutputStream();

                final byte[] br = new byte[512];

                int leitura = in.read(br);

                while (leitura != -1) {
                    if (leitura != -2) {
                        fileOut.write(br, 0, leitura);
                    }
                    leitura = in.read(br);
                }

                in.close();
                fileOut.close();
            }
            rec.close();
            CSPLog.info(prefix + "...OK");
            return fileDest;
        }
        CSPLog.error(prefix + "...ERROR(no-ping)");//Servidor não responde ao PING
        return null;
    }

    /**
     * Efetua o upload do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     * @param arquivo String - Nome do arquivo localmente
     * @return
     * @throws Exception
     */
    public boolean enviaArquivo(Servico servico, String arquivo) throws Exception {
        return this.enviaArquivo(servico, CSPComunicacao.validadeDefault, arquivo);
    }

    /**
     * Efetua o upload do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     * @param host String - Host do servidor
     * @param arquivo String - Nome do arquivo localmente
     * @return
     * @throws Exception
     */
    public boolean enviaArquivoOtherHost(Servico servico, String host, String arquivo) throws Exception {
        return this.enviaArquivoOtherHost(servico, CSPComunicacao.validadeDefault, host, arquivo);
    }

    /**
     * Efetua o upload do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     * @param arquivo String - Nome do arquivo localmente
     * @return
     * @throws Exception
     */
    public boolean enviaArquivo(Servico servico, ValidationLocal validade, String arquivo) throws Exception {
        return this.enviaArquivoByHostBuilder(servico, validade, arquivo);
//        return this.enviaArquivoOtherHost(servico, validade, getHostByHostBuilder(servico.hostType, servico.port, null, 0), arquivo);
    }

    /**
     * Auxilia no tratamento de conexões com mais de uma tentativa possível
     *
     * @param servico
     * @param validade
     * @return
     */
    public boolean enviaArquivoByHostBuilder(Servico servico, ValidationLocal validade, String arquivo) {
        boolean r = false;

        for (int i = 0; i < 5; i++) {

            try {

                r = enviaArquivoOtherHost(servico, validade, getHostByHostBuilder(servico.hostType, servico.port, null, i), arquivo);

            } catch (Exception ex) {
                CSPException.register(ex);
                continue;
            }

            if (r) {
                break;
            }

        }

        return r;
    }

    /**
     * Efetua o upload do arquivo
     *
     * @param servico Servico - Serviço a ser conectado
     * @param host String - Host do servidor
     * @param arquivo String - Nome do arquivo localmente
     * @return
     * @throws Exception
     */
    public boolean enviaArquivoOtherHost(Servico servico, ValidationLocal validade, String host, String arquivo) throws Exception {

        final int porta = servico.port;
        final String prefix = "S(" + servico.sigla + ")=>file=>(" + host + ":" + porta + ")";
        CSPLog.info(prefix + "...");

        if (!validade.run(host, servico.port)) {
            CSPLog.error(prefix + "...ERROR(local-validation)");
            return false;
        }

        if (CSPUtilidadesLangRede.ping(host)) {
            Socket rec = new Socket();
            try {
                rec.connect(new InetSocketAddress(host, porta), 5000);
//                rec.connect(new InetSocketAddress(host, porta));
            } catch (Exception e) {
                CSPLog.error(prefix + "...ERROR(no-port)");//Porta não acessível
                CSPException.register(e);
                return false;
            }
            DataInputStream arq = new DataInputStream(new FileInputStream(arquivo));
            byte buffer[] = new byte[512];
            DataOutputStream saida = new DataOutputStream(rec.getOutputStream());
            saida.flush();
            int leitura = arq.read(buffer);
            while (leitura != - 1) {
                if (leitura != - 2) {
                    saida.write(buffer, 0, leitura);
                }
                leitura = arq.read(buffer);
            }

            saida.close();
            arq.close();
            rec.close();
            CSPLog.info(prefix + "...OK");
            return true;
        }
        CSPLog.error(prefix + "...ERROR(no-ping)");//Servidor não responde ao PING
        return false;
    }

}
