/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package it.cnr.ilc.lc.omegalex.manager;

import it.cnr.ilc.lc.omegalex.OmegaLexProperties;
import it.cnr.ilc.lc.omegalex.domain.Account;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import javax.faces.context.FacesContext;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.jsoup.Jsoup;

/**
 *
 * @author oakgen
 */
public class MailManager implements Serializable {

    private static final String MAIL_SMTP_HOST = OmegaLexProperties.getProperty("smtpHost", "mail.ilc.cnr.it");
    private static final String MAIL_FROM = OmegaLexProperties.getProperty("mailFrom");
    private static final Session SESSION;

    static {
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", MAIL_SMTP_HOST);
        SESSION = Session.getDefaultInstance(properties);
    }

    private static final String RESET_PASSWORD_HTML = "/mail/resetPassword_en.html";

    public void sendResetPasswordMessage(Account account, String password) throws MessagingException {
        try {
            Message message = new MimeMessage(SESSION);
            message.setFrom(new InternetAddress(MAIL_FROM));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(account.getEmail()));
            String text = loadLocalizedText(RESET_PASSWORD_HTML);
            message.setSubject(Jsoup.parse(text).head().getElementsByTag("title").text());
            text = text.replaceFirst(":name", account.getName());
            text = text.replaceFirst(":username", account.getUsername());
            text = text.replaceFirst(":password", password);
            message.setContent(text, "text/html");
            Transport.send(message);
        } catch (IOException ex) {
            throw new MessagingException("loading text " + RESET_PASSWORD_HTML, ex);
        }
    }

    private String loadLocalizedText(String resource) throws IOException {
        String language = FacesContext.getCurrentInstance().getExternalContext().getRequestLocale().getLanguage();
        resource = resource.replaceFirst("_en", "_" + language);
        try {
            return new String(Files.readAllBytes(Paths.get(getClass().getResource(resource).toURI())));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

}
