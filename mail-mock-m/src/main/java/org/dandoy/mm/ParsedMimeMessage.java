package org.dandoy.mm;

import jakarta.mail.*;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ParsedMimeMessage {
    @Getter
    private final MimeMessage mimeMessage;
    private String textContent;
    private String textContentType;
    private String htmlContent;
    private String htmlContentType;
    @Getter
    private final List<Attachment> attachments = new ArrayList<>(1);

    private ParsedMimeMessage(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
        parse(mimeMessage);
    }

    public static ParsedMimeMessage build(MimeMessage mimeMessage) {
        return new ParsedMimeMessage(mimeMessage);
    }

    static List<String> toStrings(Address[] addresses) {
        if (addresses == null) return null;
        return Arrays.stream(addresses)
                .map(Address::toString)
                .toList();
    }

    static String toString(Address[] from) {
        if (from == null) return null;
        return Arrays.stream(from).map(Address::toString).collect(Collectors.joining(", "));
    }

    static Long toLong(Date date) {
        if (date == null) return null;
        return date.getTime();
    }

    @SneakyThrows
    static String getMessageID(MimeMessage mimeMessage) {
        return mimeMessage.getMessageID();
    }

    String getBestContent() {
        return htmlContent == null ? textContent : htmlContent;
    }

    String getBestContentType() {
        return htmlContent == null ? textContentType : htmlContentType;
    }

    @SneakyThrows
    void parse(Part part) {
        String contentType = part.getContentType();
        String partDisposition = part.getDisposition();
        if (partDisposition == null) {
            Object content = part.getContent();
            if (contentType.startsWith("text/plain")) {
                if (content instanceof String text) {
                    textContent = text;
                    textContentType = contentType;
                }
            } else if (contentType.startsWith("text/html")) {
                if (content instanceof String text) {
                    htmlContent = text;
                    htmlContentType = contentType;
                }
            } else if (contentType.startsWith("multipart/mixed")) {
                if (content instanceof MimeMultipart multipart) {
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        parse(bodyPart);
                    }
                }
            } else {
                log.warn("Unexpected content type: {} ", contentType);
            }
        } else if ("attachment".equals(partDisposition)) {
            attachments.add(new Attachment(
                    part.getFileName(),
                    contentType,
                    buildContent(part)
            ));
        } else {
            log.warn("Unexpected part disposition: {} ", partDisposition);
        }
    }

    @SneakyThrows
    private static byte[] buildContent(Part part) {
        byte[] buffer = new byte[1024 * 64];
        try (InputStream inputStream = part.getInputStream()) {
            int read = inputStream.read(buffer);
            return Arrays.copyOf(buffer, read);
        }
    }

    @SneakyThrows
    public String getSubject() {
        return mimeMessage.getSubject();
    }

    @SneakyThrows
    public String getFrom() {
        return ParsedMimeMessage.toString(mimeMessage.getFrom());
    }

    public List<String> getTos() {
        return getRecipients(Message.RecipientType.TO);
    }

    public List<String> getCcs() {
        return getRecipients(Message.RecipientType.CC);
    }

    @SneakyThrows
    private List<String> getRecipients(Message.RecipientType recipientType) {
        return ParsedMimeMessage.toStrings(mimeMessage.getRecipients(recipientType));
    }

    @SneakyThrows
    public long getSentDate() {
        return ParsedMimeMessage.toLong(mimeMessage.getSentDate());
    }

    @SneakyThrows
    public boolean isSeen() {
        return mimeMessage.isSet(Flags.Flag.SEEN);
    }

    public String getMessageID() {
        return getMessageID(mimeMessage);
    }

    public List<String> getAttachmentFilenames() {
        return attachments.stream().map(it -> it.filename).toList();
    }

    public record Attachment(String filename, String mimeType, byte[] content) {}
}
