/**
 * Copyright(c) 2016 CASA Automação LTDA. Todos os direitos reservados. Este
 * software faz parte da propriedade da CASA Automação LTDA.
 */
package br.com.casaautomacao.casagold.classes.rede;

import br.com.casaautomacao.casagold.classes.CSPLog;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

/**
 * Cliente de envio de email do sistema
 *
 *
 * @author Fernando Batels <luisfbatels@gmail.com>
 * @date 08/06/2016 - 11:05:30
 */
public class CSPEmailClientSender {

    private final HtmlEmail email;

    public CSPEmailClientSender(String hostServer, int portServer, String userServer, String nameUserServer, String passUserServer) throws EmailException {
        this.email = new HtmlEmail();
        this.email.setCharset("UTF8");
        this.email.setHostName(hostServer);
        this.email.setSmtpPort(portServer);
        this.email.setFrom(userServer, nameUserServer);
        this.email.setAuthentication(userServer, passUserServer);
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    /**
     * Adiciona um novo endereço na lista de emails
     *
     * @param email String - E-mail válido
     * @throws org.apache.commons.mail.EmailException
     */
    public void addAddress(String email) throws EmailException {
        this.email.addTo(email);
    }

    /**
     * Define o assunto da mensagem
     *
     * @param text String
     */
    public void setSubject(String text) {
        this.email.setSubject(text);
    }

    /**
     * Define o conteúdo da mensagem
     *
     * @param htmlContent String - Conteúdo HTML
     * @param plainContent String - Conteudo TXT
     * @throws org.apache.commons.mail.EmailException
     */
    public void setContent(String htmlContent, String plainContent) throws EmailException {
        this.email.setHtmlMsg(htmlContent);

        this.email.setTextMsg(plainContent);
    }

    /**
     * Envia a mensagem para os destinários configurados
     *
     * @throws java.lang.Exception
     */
    public void send() throws Exception {

        CSPLog.info(this.getClass(), "sending email...");

        CSPLog.info(this.getClass(), "infos-server:" + this.email.getHostName() + ":" + this.email.getSmtpPort() + "");
        CSPLog.info(this.getClass(), "infos-message:" + this.email.getSubject());
        this.email.send();
        CSPLog.info(this.getClass(), "sending email...OK");
    }

}
