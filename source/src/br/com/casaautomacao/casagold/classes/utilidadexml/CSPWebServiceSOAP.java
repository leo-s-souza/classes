/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidadexml;

import br.com.casaautomacao.casagold.classes.CSPLog;
import static br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesApplication.PATH;
import br.com.casaautomacao.casagold.classes.utilidades.CSPUtilidadesLang;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.net.URL;
import java.security.Security;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * Comunicação WebService via SOAP.
 *
 * @author Jean Regis <producao1@casaautomacao.com.br>
 */
public class CSPWebServiceSOAP {

    /**
     * Url do webservice onde sera feita a requisição.
     */
    private Object url;

    /**
     * Requisição/request no formato xml.
     */
    private String requestSoap;

    /**
     * Envia a mensagem.
     *
     * @return String
     * @throws Exception
     */
    public String envioArquivo() throws Exception {
        /**
         * Configurações da mensagem.
         */
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "application/soap+xml");

        /**
         * Transforma o xml da requisição em mensagem.
         */
        SOAPMessage msg = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL).createMessage(headers, (new ByteArrayInputStream(this.requestSoap.getBytes("UTF-8"))));

        /**
         * Envia a requisição
         */
        SOAPMessage soapResponse = null;
        try {
            soapResponse = SOAPConnectionFactory.newInstance().createConnection().call(msg, this.url);
        } catch (UnsupportedOperationException | SOAPException e) {
            CSPLog.error(e.getMessage());
            return "-1";
        }

        /**
         * Xml de retorno.
         */
        Document xmlRespostaARequisicao = soapResponse.getSOAPBody().getOwnerDocument();
        /**
         * Configurações para converter o documento em string.
         */
        TransformerFactory transfac = TransformerFactory.newInstance();
        transfac.setAttribute("indent-number", 4);
        Transformer trans = transfac.newTransformer();
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");
        trans.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        /**
         * Cria a string da árvore do xml.
         */
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        DOMSource source = new DOMSource(xmlRespostaARequisicao);
        trans.transform(source, result);
        String xmlString = sw.toString();
        return xmlString;
    }

    public String envioNfe(String keyFile, String keyPassword, String trustStore) throws Exception {
        System.clearProperty("javax.net.ssl.keyStore");
        System.clearProperty("javax.net.ssl.keyStorePassword");
        System.clearProperty("javax.net.ssl.trustStore");
        System.clearProperty("javax.net.ssl.trustStorePassword");

        System.setProperty("java.protocol.handler.pkgs", "com.sun.net.ssl.internal.www.protocol");
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
        System.setProperty("javax.net.ssl.keyStore", keyFile);
        System.setProperty("javax.net.ssl.keyStorePassword", keyPassword);

        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "changeit");

        url = new URL(url.toString());

        return envioArquivo();
    }

    /**
     * Seta a Url do webservice onde sera feita a requisição.
     *
     * @param url
     */
    private void setUrl(String url) {
        this.url = url;
    }

    /**
     * Seta a requisição/request no formato xml.
     *
     * @param requestSoap
     */
    private void setRequestSoap(String requestSoap) {
        this.requestSoap = requestSoap;
    }

    /**
     * Construtor.
     *
     * @param url Url do webservice onde sera feita a requisição.
     * @param requestSoap Requisição/request no formato xml.
     * @return CSPWebServiceSOAP
     * @throws Exception
     */
    public static CSPWebServiceSOAP getInstance(String url, String requestSoap) throws Exception {
        CSPWebServiceSOAP soap = new CSPWebServiceSOAP();
        soap.setUrl(url);

        soap.setRequestSoap(requestSoap);

        return soap;
    }

    /**
     * Construtor.
     *
     * @param url Url do webservice onde sera feita a requisição.
     * @param requestSoap Requisição/request no formato xml.
     * @return CSPWebServiceSOAP
     * @throws Exception
     */
    public static CSPWebServiceSOAP getInstance(String url, CSPUtilidadesLang.StringBuilderShortcut requestSoap) throws Exception {
        CSPWebServiceSOAP soap = new CSPWebServiceSOAP();
        soap.setUrl(url);

        StringBuilder sb = new StringBuilder();
        requestSoap.run(sb);
        soap.setRequestSoap(sb.toString());

        return soap;
    }
}
