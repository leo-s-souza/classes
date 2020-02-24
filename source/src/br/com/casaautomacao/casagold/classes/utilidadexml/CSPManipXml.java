/**
 * Copyright(c) 2017 CASA Automação LTDA. Todos os
 * direitos reservados. Este software faz parte da propriedade da CASA Automação
 * LTDA.
 */
package br.com.casaautomacao.casagold.classes.utilidadexml;

import br.com.casaautomacao.casagold.classes.arquivos.CSPArquivosXml;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.DOMOutputter;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.transform.JDOMSource;

/**
 * Classe responsavel pela manipulação de arquivos xml.
 *
 * @author Vitor Bellini Federle <producao3@casaautomacao.com.br>
 * @date 20/11/2017 - 17:29:09
 */
public class CSPManipXml {

    /**
     * Elemento principal do arquivo xml.
     */
    private Element rootElement;

    /**
     * Arquivo xml que vai ser manipulado.
     */
    private CSPArquivosXml arq;

    /**
     * Instancia do documento xml.
     */
    private Document doc;

    /*
     * Construtores para trabalhar com um arquivo já setado.
     */
    /**
     * Construtor que recebe o caminho do arquivo a ser setado.
     *
     * @param path Caminho do arquivo a ser setado.
     * @return CSPManipXml
     * @throws Exception
     */
    public static CSPManipXml getInstance(String path) throws Exception {
        return getInstance(new CSPArquivosXml(path));
    }

    /**
     * Construtor que recebe o caminho do arquivo a ser setado e o rootElement
     * usado.
     *
     * @param path Caminho do arquivo a ser setado.
     * @param tagRoot rootElement.
     * @return CSPManipXml
     * @throws Exception
     */
    public static CSPManipXml getInstance(String path, String tagRoot) throws Exception {
        return getInstance(new CSPArquivosXml(path), tagRoot);
    }

    /**
     * Construtor que recebe o arquivo a ser setado.
     *
     * @param arq Arquivo a ser setado.
     * @return CSPManipXml
     * @throws Exception
     */
    public static CSPManipXml getInstance(CSPArquivosXml arq) throws Exception {
        CSPManipXml m = new CSPManipXml();
        File f = new File(arq.getAbsolutePath());
        Document d = new SAXBuilder().build(f);

        m.setDoc(d);
        m.setRootElement(d.getRootElement());
        m.setArq(arq);

        return m;
    }

    /**
     * Construtor que recebe o arquivo a ser setado e o rootElement usado.
     *
     * @param arq Arquivo a ser setado.
     * @param tagRoot rootElement.
     * @return CSPManipXml
     * @throws Exception
     */
    public static CSPManipXml getInstance(CSPArquivosXml arq, String tagRoot) throws Exception {
        CSPManipXml m = new CSPManipXml();
        Document d;

        if (!arq.exists() || arq.getContent().length() == 0) {
            Element el = new Element(tagRoot);
            d = new Document(el);
        } else {
            File f = new File(arq.getAbsolutePath());
            d = new SAXBuilder().build(f);
        }

        m.setDoc(d);
        m.setRootElement(d.getRootElement());
        m.setArq(arq);

        return m;
    }

    /*
     * Construtores para trabalhar sem um arquivo setado.
     */
    /**
     * Construtor que recebe um xml em forma de string para setar como
     * documento.
     *
     * @param doc Xml em forma de string.
     * @return CSPManipXml
     * @throws Exception
     */
    public static CSPManipXml getInstanceWithoutFile(String doc) throws Exception {
        CSPManipXml m = new CSPManipXml();
        Element el = new Element(doc);
        Document d = new Document(el);

        m.setDoc(d);
        m.setRootElement(d.getRootElement());

        return m;
    }

    /**
     * Pega a primeira tag com o nome recebido por parametro.
     *
     * @param nome - nome da tag a ser procurada.
     * @return
     * @throws Exception
     */
    public Element getTag(String nome) throws Exception {
        return getTag(nome, getRootElement());
    }

    /**
     * Procura uma tag dentro de tags especificas.
     *
     * @param nome - Tag a ser procurada
     * @param elmts - List de tags especificas que devem ser percorridas para
     * encontrar a tag recebida no parametro <b><i>"nome"</i></b>
     * @return
     * @throws Exception
     */
    public Element getTag(String nome, String... elmts) throws Exception {
        Element el;

        if (elmts.length > 0) {

            el = getRootElement();

            int quant = 1;

            for (String elmt : elmts) {
                if (quant == elmts.length) {
                    el = getTag(nome, el);
                } else {
                    el = getTag(elmt, el);
                    quant++;
                }
            }
        } else {
            el = getTag(nome);
        }

        return el;
    }

    /**
     * Busca o elemento do XML pelo nome da tag passada.
     *
     * @param nome Nome da tag a ser procurada no XML.
     * @param elmt Elemento/Tag principal que contém os filhos a serem
     * percorridos a procura do elemento passado.
     * @return Element
     * @throws Exception
     */
    private Element getTag(String nome, Element elmt) throws Exception {
        if (elmt.getName().equals(nome)) {
            return elmt;
        }

        for (Element element : elmt.getChildren()) {
            if (element.getName().equals(nome)) {
                return element;
            } else {
                Element el = getTag(nome, element);
                if (el != null) {
                    if (el.getName().equals(nome)) {
                        return el;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Retorna uma lista com todos elementos contendo o mesmo nome.
     *
     * @param nome Nome da tag a ser procurada no XML.
     * @param elmt Elemento/Tag principal que contém os filhos a serem
     * percorridos a procura do elemento passado.
     * @return Element
     * @throws Exception
     */
    public List<Element> getMultiTag(String nome, Element elmt) throws Exception {
        List<Element> elementos = new ArrayList<>();

        for (Element element : elmt.getChildren()) {
            if (element.getName().equals(nome)) {
                elementos.add(element);
            } else {
                Element el = getTag(nome, element);
                if (el != null && el.getName().equals(nome)) {
                    elementos.add(element);
                }
            }
        }

        return elementos;
    }

    /**
     * Retorna um Iterator com todos elements do xml.
     *
     * @return
     * @throws Exception
     */
    public Iterator getAllTags() throws Exception {
        Element mural = getDoc().getRootElement();
        List elements = mural.getChildren();
        Iterator i = elements.iterator();

        if (i != null) {
            return i;
        }

        return null;
    }

    /**
     * Adiciona uma tag diretamente dentro do rootElement.
     *
     * @param nome - Nome da tag
     * @return
     * @throws java.lang.Exception
     */
    public Element addTag(String nome) throws Exception {
        return addTag(nome, null);
    }

    /**
     * Adiciona uma tag diretamente dentro do rootElement.
     *
     * @param nome - Nome da tag
     * @param conteudo - Conteúdo da tag.
     * @return Element
     * @throws java.lang.Exception
     */
    public Element addTag(String nome, Object conteudo) throws Exception {
        Element rtrn = new Element(nome);
        if (conteudo == null) {
            getRootElement().addContent(rtrn);
        } else {
            getRootElement().addContent(rtrn.setText(conteudo.toString()));
        }

        return rtrn;
    }

    /**
     * Adiciona as tags definidas no array e o conteudo na ultima tag. Todas
     * dentro da tag inicial
     *
     * @param tagInicial
     * @param elmts
     * @return Element
     * @throws java.lang.Exception
     */
    public Element addSubTag(String tagInicial, String... elmts) throws Exception {
        return addSubTagContent(tagInicial, null, elmts);
    }

    /**
     * Adiciona as tags definidas no array e o conteudo na ultima tag. Todas
     * dentro da tag inicial
     *
     * @param tagInicial
     * @param conteudo - Conteúdo da tag.
     * @param elmts
     * @return Element
     * @throws java.lang.Exception
     */
    public Element addSubTagContent(String tagInicial, String conteudo, String... elmts) throws Exception {
        Element el = getTag(tagInicial);
        int quant = 1;

        for (String elmt : elmts) {
            if (quant == elmts.length) {
                Element rtrn = new Element(elmt);
                if (conteudo == null) {
                    el.addContent(rtrn);
                } else {
                    el.addContent(rtrn.setText((String) conteudo));
                }

                return rtrn;
            } else {
                el = el.addContent(new Element(elmt));
                el = getTag(elmt, el);
                quant++;
            }
        }

        return el;
    }

    /**
     * Adiciona uma tag dentro de outra tag.
     *
     * @param nome - Nome da tag
     * @param element - Elemento principal do arquivo.
     * @return Element
     * @throws java.lang.Exception
     */
    public Element addSubTag(String nome, Element element) throws Exception {
        return addSubTag(nome, null, element);
    }

    /**
     * Adiciona uma tag dentro de outra tag.
     *
     * @param nome - Nome da tag
     * @param conteudo - Conteúdo da tag.
     * @param element - Elemento principal do arquivo.
     * @return Element
     * @throws java.lang.Exception
     */
    public Element addSubTag(String nome, Object conteudo, Element element) throws Exception {
        Element rtrn = new Element(nome);

        if (conteudo == null) {
            element.addContent(rtrn);
        } else {
            element.addContent(rtrn.setText(conteudo.toString()));
        }

        return rtrn;
    }

    /**
     * Adiciona varias tags com conteudos dentro de uma unica tag.
     *
     * @param dados - key = nome tag | value = conteudo da tag
     * @param element - Elemento principal do arquivo.
     * @throws java.lang.Exception
     */
    public void addSubTags(LinkedHashMap<String, Object> dados, Element element) throws Exception {
        dados.entrySet().forEach((entry) -> {
            String key = entry.getKey();
            Object value = entry.getValue();
            element.addContent(new Element(key).setText(value.toString()));
        });
    }

    /**
     * Adiciona um atributo ao rootElement.
     *
     * @param atributo
     * @param conteudo - Conteúdo do atributo.
     * @throws java.lang.Exception
     */
    public void addAttribute(String atributo, String conteudo) throws Exception {
        addAttribute(atributo, conteudo, getRootElement());
    }

    /**
     * Adiciona um atibuto dentro de uma tag em uma lista de tags especificas.
     *
     * @param atributo
     * @param conteudo - Conteúdo do atributo.
     * @param tag - tag especifica que vai receber o atributo.
     * @param elmts - Lista de tags a serem percorridas para encontrar a tag que
     * vai receber o atributo.
     * @throws java.lang.Exception
     */
    public void addSubTagAttribute(String atributo, String conteudo, String tag, String... elmts) throws Exception {
        addAttribute(atributo, conteudo, getTag(tag, elmts));
    }

    /**
     * Adiciona umum atributo em um elemento especifico.
     *
     * @param atributo
     * @param conteudo - Conteúdo da tag.
     * @param element - Elemento principal do arquivo.
     * @throws java.lang.Exception
     */
    public void addAttribute(String atributo, String conteudo, Element element) throws Exception {
        element.setAttribute(new Attribute(atributo, conteudo));
    }

    /**
     * Adiciona um namespace no rootElement.
     *
     * OBS: Uma tag não pode ter o mesmo namespace da sua tag superior. Se for
     * necessário colocar, deve-se colocar após a manipulação do arquivo.
     *
     * @param ns - namespace a ser adicionado.
     * @throws java.lang.Exception
     */
    public void addNamespace(String ns) throws Exception {
        addNamespace(ns, getRootElement());
    }

    /**
     * Adiciona um namespace na ultima tag de uma lista especifica de tags.
     *
     * OBS: Uma tag não pode ter o mesmo namespace da sua tag superior. Se for
     * necessário colocar, deve-se colocar após a manipulação do arquivo.
     *
     * @param ns - namespace a ser adicionado.
     * @param tag - tag que vai receber o namespace
     * @param elmts - lista de elementos a serem percorridos para encontrar a
     * tag que vai receber o namespace.
     * @throws java.lang.Exception
     */
    public void addNamespace(String ns, String tag, String... elmts) throws Exception {
        addNamespace(ns, getTag(tag, elmts));
    }

    /**
     * Adiciona um namespace a uma tag.
     *
     * OBS: Uma tag não pode ter o mesmo namespace da sua tag superior. Se for
     * necessário colocar, deve-se colocar após a manipulação do arquivo.
     *
     * @param ns - namespace a ser adicionado.
     * @param element - Elemento principal do arquivo.
     * @throws java.lang.Exception
     */
    private void addNamespace(String ns, Element element) throws Exception {
        element.setNamespace(Namespace.getNamespace(ns));
    }

    /**
     * Remove uma tag dentro de um element.
     *
     * @param nome - Nome da tag a ser removida.
     * @param element - Elemento em qual a tag se encontra.
     * @throws java.lang.Exception
     */
    public void removeTag(String nome, Element element) throws Exception {
        element.removeChild(nome);
    }

    /**
     * Remove uma tag dentro de um element.
     *
     * @param atributo - Nome do atributo a ser removido.
     * @param element - Elemento em qual o atributo se encontra.
     * @throws java.lang.Exception
     */
    public void removeAttribute(String atributo, Element element) throws Exception {
        element.removeAttribute(atributo);
    }

    /**
     * Remove um namespace dentro de um element.
     *
     * OBS: Uma tag não pode ter o mesmo namespace da sua tag superior. Se for
     * necessário colocar, deve-se colocar após a manipulação do arquivo.
     *
     * @param element - Elemento em qual o namespace se encontra.
     * @throws java.lang.Exception
     */
    public void removeNamespace(Element element) throws Exception {
        element.removeNamespaceDeclaration(Namespace.NO_NAMESPACE);
    }

    /**
     * Transforma o elemento recebido por parametro em string xml já com
     * identação.
     *
     * @param el Element - elemento xml que deve ser passado para string.
     * @return
     * @throws Exception
     */
    public static String transformElementString(Document el) throws Exception {

        TransformerFactory transfac = TransformerFactory.newInstance();
        Transformer trans = transfac.newTransformer();

        /**
         * Retira a declaração do xml e identa o texto.
         */
        trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        trans.setOutputProperty(OutputKeys.INDENT, "yes");

        /**
         * Cria a string baseada na arvore do xml.
         */
        StringWriter sw = new StringWriter();
        StreamResult result = new StreamResult(sw);
        JDOMSource source = new JDOMSource(el);

        trans.transform(source, result);
        String xmlString = sw.toString();

        return xmlString;
    }

    /**
     * Salva o conteudo alterado no arquivo xml.
     *
     * @throws Exception
     */
    public void saveContent() throws Exception {
        if (!getArq().exists() || getArq().getContent().length() == 0) {
            getArq().setContent("");
        }
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        getArq().setContent(xmlOutput.outputString(getDoc()));
    }

    /**
     * Salva o conteudo alterado no arquivo xml.
     *
     * @throws Exception
     */
    public void parseFileContentWithouFormat() throws Exception {
        if (!getArq().exists() || getArq().getContent().length() == 0) {
            return;
        }

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        File fXmlFile = getArq().objFile();
        org.w3c.dom.Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fXmlFile);

        Transformer tfr = TransformerFactory.newInstance().newTransformer();
        tfr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tfr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tfr.setParameter(OutputKeys.INDENT, "no");
        tfr.transform(new DOMSource(document), new StreamResult(s));

        getArq().setContent(s.toString().replace("\n", "").replace("\r", ""));
    }

    /**
     * Salva o conteudo alterado no arquivo xml.
     *
     * @throws Exception
     */
    public void saveContentWithouFormat() throws Exception {
        if (!getArq().exists() || getArq().getContent().length() == 0) {
            getArq().setContent("");
        }

        ByteArrayOutputStream s = new ByteArrayOutputStream();
        org.w3c.dom.Document document = new DOMOutputter().output(getDoc());

        Transformer tfr = TransformerFactory.newInstance().newTransformer();
        tfr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        tfr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        tfr.setParameter(OutputKeys.INDENT, "no");
        tfr.transform(new DOMSource(document), new StreamResult(s));

        getArq().setContent(s.toString().replace("\n", "").replace("\r", ""));
    }

    /**
     * Salva o conteudo em um arquivo no caminho passado.
     *
     * @param path
     * @throws Exception
     */
    public void saveContent(String path) throws Exception {
        if (!getArq().exists() || getArq().getContent().length() == 0) {
            getArq().setContent("");
        }
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        new CSPArquivosXml(path).setContent(xmlOutput.outputString(getDoc()));
    }

    /**
     * Salva o conteudo no arquivo passado.
     *
     * @param arq
     * @throws Exception
     */
    public void saveContent(CSPArquivosXml arq) throws Exception {
        if (!getArq().exists() || getArq().getContent().length() == 0) {
            getArq().setContent("");
        }
        XMLOutputter xmlOutput = new XMLOutputter();
        xmlOutput.setFormat(Format.getPrettyFormat());
        arq.setContent(xmlOutput.outputString(getDoc()));
    }

    @Override
    public String toString() {
        XMLOutputter xmlOutput = new XMLOutputter();
        return xmlOutput.outputString(getDoc());
    }

    public Element getRootElement() {
        return rootElement;
    }

    public final void setRootElement(Element rootElement) {
        this.rootElement = rootElement;
    }

    public CSPArquivosXml getArq() {
        return arq;
    }

    public final void setArq(CSPArquivosXml arq) {
        this.arq = arq;
    }

    public final Document getDoc() {
        return doc;
    }

    public final void setDoc(Document doc) {
        this.doc = doc;
    }
}
