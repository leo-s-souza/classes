/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.casaautomacao.casagold.classes.utilidadexml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class CSPAssinarXML {

    /**
     * Chave privada do arquivo.
     */
    private static PrivateKey privateKey;

    /**
     * Inforçoes da chave para a geraçao da assinatura.
     */
    private static KeyInfo keyInfo;

    public enum Mode {
        DEFAULT,
        NFE;
    }

    /**
     * Assinatura do XML.
     *
     * @param path Caminho xml.
     * @param certificado Caminho certificado.
     * @param senha Senha certificado
     * @param modo
     * @throws Exception
     */
    public static void assinaXML(String path, String certificado, String senha, Mode modo) throws Exception {
        File fXmlFile = new File(path);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        Document document = dbFactory.newDocumentBuilder().parse(fXmlFile);
        
        XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
        ArrayList<Transform> transformList = signatureFactory(signatureFactory);

        /**
         * Dados do certificado para as informaçoes de chave.
         */
        {
            InputStream entrada = new FileInputStream(certificado);
            KeyStore ks = KeyStore.getInstance("pkcs12");
            try {
                ks.load(entrada, senha.toCharArray());
            } catch (IOException e) {
                throw new Exception("Senha do Certificado Digital incorreta ou Certificado inválido.");
            }

            KeyStore.PrivateKeyEntry pkEntry = null;
            Enumeration<String> aliasesEnum = ks.aliases();
            while (aliasesEnum.hasMoreElements()) {
                String alias = (String) aliasesEnum.nextElement();
                if (ks.isKeyEntry(alias)) {
                    pkEntry = (KeyStore.PrivateKeyEntry) ks.getEntry(alias, new KeyStore.PasswordProtection(senha.toCharArray()));
                    privateKey = pkEntry.getPrivateKey();
                    break;
                }
            }
            @SuppressWarnings("null")
            X509Certificate cert = (X509Certificate) pkEntry.getCertificate();
            KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();
            List<X509Certificate> x509Content = new ArrayList<>();
            x509Content.add(cert);
            X509Data x509Data = keyInfoFactory.newX509Data(x509Content);
            keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data));
        }

        /**
         * Faz a assinatura.
         */
        if (modo.equals(Mode.DEFAULT)) {
            assinarXML(signatureFactory, transformList, privateKey, keyInfo, document);
        } else if (modo.equals(Mode.NFE)) {
            assinarNFE(signatureFactory, transformList, privateKey, keyInfo, document);
        }

        /**
         * Grava no arquivo.
         */
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        Transformer tfr = TransformerFactory.newInstance().newTransformer();
        tfr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tfr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tfr.setParameter(OutputKeys.INDENT, "no");
        tfr.transform(new DOMSource(document), new StreamResult(s));

        try (FileOutputStream output = new FileOutputStream(path)) {
            output.write(s.toString().replace("\n", "").replace("\r", "").getBytes());
        }
    }

    /**
     * Metodo privado para a assinatura do documento.
     *
     * @param fac
     * @param transformList
     * @param privateKey
     * @param ki
     * @param document
     * @param elementaryTag
     * @param index
     * @throws Exception
     */
    private static void assinarXML(XMLSignatureFactory fac, ArrayList<Transform> transformList, PrivateKey privateKey, KeyInfo ki, Document document) throws Exception {
        Reference ref = fac.newReference("", fac.newDigestMethod(DigestMethod.SHA1, null), transformList, null, null);
        SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref));

        XMLSignature signature = fac.newXMLSignature(si, ki);
        DOMSignContext dsc = new DOMSignContext(privateKey, document.getDocumentElement());
        signature.sign(dsc);
    }

    /**
     * Metodo privado para a assinatura do documento.
     *
     * @param fac
     * @param transformList
     * @param privateKey
     * @param ki
     * @param document
     * @param elementaryTag
     * @param index
     * @throws Exception
     */
    private static void assinarNFE(XMLSignatureFactory fac, ArrayList<Transform> transformList, PrivateKey privateKey, KeyInfo ki, Document document) throws Exception {
        NodeList elements = document.getElementsByTagName("infNFe");
        org.w3c.dom.Element el = (org.w3c.dom.Element) elements.item(0);
        String id = el.getAttribute("Id");
        el.setIdAttribute("Id", true);

        Reference ref = fac.newReference("#" + id,
                fac.newDigestMethod(DigestMethod.SHA1, null), transformList,
                null, null);
        SignedInfo si = fac.newSignedInfo(fac.newCanonicalizationMethod(
                CanonicalizationMethod.INCLUSIVE,
                (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref));

        XMLSignature signature = fac.newXMLSignature(si, ki);
        DOMSignContext dsc = new DOMSignContext(privateKey, document.getDocumentElement());
        signature.sign(dsc);
    }

    /**
     * Define as configuraçoes da assinatura.
     *
     * @param signatureFactory
     * @return
     * @throws Exception
     */
    private static ArrayList<Transform> signatureFactory(XMLSignatureFactory signatureFactory) throws Exception {
        ArrayList<Transform> transformList = new ArrayList<>();
        TransformParameterSpec tps = null;
        Transform envelopedTransform = signatureFactory.newTransform(Transform.ENVELOPED, tps);
        Transform c14NTransform = signatureFactory.newTransform("http://www.w3.org/TR/2001/REC-xml-c14n-20010315", tps);
        transformList.add(envelopedTransform);
        transformList.add(c14NTransform);
        return transformList;
    }
}
