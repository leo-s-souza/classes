/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.compartilhamento;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivos;
import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosLocais;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPComunicacao;
import br.com.casaautomacao.casagold.classes.rede.comunicacao.CSPTransferenciaArquivos;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLangJson;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesSO.PATH_TEMP;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONObject;

/**
 * Cliente para compartilhamento de arquivos com CMG
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 19/06/2016 - 17:44:13
 */
public class CSPClienteCompartilhamentoBase {

    /**
     * Efetua o upload de um arquivo para o CMG
     *
     * @param sInfo
     * @param sFile
     * @param id String - Id para identificar a utilidade do arquivo no cmg
     * @param file CSPArquivos - Arquivo de origem
     * @param host String - Host especifico
     * @param params String[] - Informações adicionais
     * @return
     * @throws Exception
     */
    public static boolean uploadFileBase(CSPComunicacao.Servico sInfo, CSPTransferenciaArquivos.Servico sFile, final String id, String host, final CSPArquivos file, String... params) throws Exception {

        if (!file.canRead()) {
            return false;
        }
        
        if(file.getAbsolutePath().startsWith("//")) {
            final CSPArquivos tmp = new CSPArquivos(PATH_TEMP+"/"+file.getName());
            
            if(!file.replace(tmp)){
                throw new IOException("Não foi possível copiar o arquivo remoto("+file.getAbsolutePath()+") para a pasta temporária dessa máquina!");
            }
            
            file.setPath(tmp.getAbsolutePath());
        }

        final JSONObject dataSend = new JSONObject() {
            {
                put("ACAO", "on-upload");
                put("FILE_PATH", file.getAbsolutePath());
                put("PARAMS", new JSONObject() {
                    {
                        put("ID", id);
                        put("MD5", file.getMd5());
                        put("SIZE", file.length());
                        put("MORE", params);
                    }
                });
            }
        };

        final CSPTransferenciaArquivos sc = new CSPTransferenciaArquivos();

        final JSONObject response = host == null
                ? sc.comunica(sInfo, dataSend)
                : sc.comunicaOtherHost(sInfo, host, dataSend);
        if (response != null && CSPUtilidadesLangJson.getFromJson(response, "STATUS", "no").equals("ok")) {
            return host == null
                    ? sc.enviaArquivo(sFile, file.getAbsolutePath())
                    : sc.enviaArquivoOtherHost(sFile, host, file.getAbsolutePath());

        }

        return false;
    }

    /**
     * Efetua o download de um arquivo do CMG
     *
     * @param sInfo
     * @param sFile
     * @param name String - Nome/Caminho do arquivo a ser baixado
     * @param host String - Host especifico
     * @return
     * @throws Exception
     */
    protected static CSPArquivos downloadFileBase(CSPComunicacao.Servico sInfo, CSPTransferenciaArquivos.Servico sFile, final String name, String host) throws Exception {
        final CSPTransferenciaArquivos sc = new CSPTransferenciaArquivos();
        final JSONObject dataSend = new JSONObject() {
            {
                put("ACAO", "on-download");
                put("FILE_PATH", name);
                put("PARAMS", new JSONObject());
            }
        };
        final JSONObject response
                = host == null
                        ? sc.comunica(sInfo, dataSend)
                        : sc.comunicaOtherHost(sInfo, host, dataSend);

        if (response != null && CSPUtilidadesLangJson.getFromJson(response, "STATUS", "no").equals("ok")) {
            final String dest = CSPUtilidadesSO.PATH_TEMP + "/" + FilenameUtils.getName(name);
            final CSPArquivosLocais tmp
                    = host == null
                            ? sc.baixaArquivo(sFile, name, new JSONObject(), dest)
                            : sc.baixaArquivoOtherHost(sFile, host, name, new JSONObject(), dest);
            if (tmp != null) {
                return new CSPArquivos(tmp.getAbsolutePath());
            }
        }

        return null;
    }

}
