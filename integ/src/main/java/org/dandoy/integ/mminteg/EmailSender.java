package org.dandoy.integ.mminteg;

import jakarta.activation.DataHandler;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.SneakyThrows;

public class EmailSender {
    private final MimeMessage mimeMessage;
    private final Multipart multipart;
    private boolean hasContent;

    public EmailSender(MimeMessage mimeMessage, Multipart multipart) {
        this.mimeMessage = mimeMessage;
        this.multipart = multipart;
    }

    @SneakyThrows
    public static EmailSender build(Session session) {
        Multipart multipart = new MimeMultipart();
        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setContent(multipart);
        return new EmailSender(mimeMessage, multipart);
    }

    @SneakyThrows
    public EmailSender subject(String subject) {
        mimeMessage.setSubject(subject, "UTF-8");
        return this;
    }

    @SneakyThrows
    public EmailSender from(String from) {
        mimeMessage.setFrom(new InternetAddress(from));
        return this;
    }

    @SneakyThrows
    public EmailSender to(String to) {
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        return this;
    }

    @SneakyThrows
    public EmailSender cc(String cc) {
        mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
        return this;
    }

    @SneakyThrows
    private EmailSender setContent(String text, String contentType) {
        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(text, contentType);
        multipart.addBodyPart(textPart);
        hasContent = true;
        return this;
    }

    public EmailSender text(String text) {
        return setContent(text, "text/plain");
    }

    public EmailSender html(String html) {
        return setContent(html, "text/html");
    }

    private EmailSender attach(ByteArrayDataSource bytes, String filename) throws MessagingException {
        MimeBodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setDataHandler(new DataHandler(bytes));
        attachmentBodyPart.setFileName(filename);
        multipart.addBodyPart(attachmentBodyPart);
        return this;
    }

    @SneakyThrows
    public EmailSender attach(byte[] bytes, String contentType, String filename) {
        return attach(new ByteArrayDataSource(bytes, contentType), filename);
    }

    @SneakyThrows
    public EmailSender attach(String text, String contentType, String filename) {
        return attach(new ByteArrayDataSource(text, contentType), filename);
    }

    @SneakyThrows
    public void send() {
        if (!hasContent) throw new RuntimeException("Content not set");
        if (mimeMessage.getSubject() == null) throw new RuntimeException("Subject not set");
        Transport.send(mimeMessage);
    }
}
