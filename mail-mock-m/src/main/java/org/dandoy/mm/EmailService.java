package org.dandoy.mm;

import com.icegreen.greenmail.imap.ImapHostManager;
import com.icegreen.greenmail.store.StoredMessage;
import com.icegreen.greenmail.util.GreenMail;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.scheduling.annotation.Scheduled;
import jakarta.inject.Singleton;
import jakarta.mail.Flags;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.micronaut.http.MediaType.APPLICATION_OCTET_STREAM_TYPE;
import static io.micronaut.http.MediaType.TEXT_PLAIN_TYPE;
import static org.dandoy.mm.ParsedMimeMessage.getMessageID;

@Singleton
@Slf4j
public class EmailService {
    private final GreenMailService greenMailService;
    private final EmailWebsocket emailWebsocket;
    private long lastEmailReceived = -1;

    public EmailService(GreenMailService greenMailService, @SuppressWarnings("MnInjectionPoints") EmailWebsocket emailWebsocket) {
        this.greenMailService = greenMailService;
        this.emailWebsocket = emailWebsocket;
    }

    @Scheduled(fixedDelay = "1s")
    void checkEmails() {
        List<StoredMessage> newMessages = storedMessageStream()
                .filter(it -> it.getReceivedDate().getTime() > lastEmailReceived)
                .toList();
        if (!newMessages.isEmpty()) {
            lastEmailReceived = newMessages.stream()
                    .map(storedMessage -> storedMessage.getReceivedDate().getTime())
                    .max(Long::compareTo)
                    .get();

            Map<String, EmailWebsocket.EmailEvent> messageIdToEvent = new HashMap<>();
            for (StoredMessage newMessage : newMessages) {
                MimeMessage mimeMessage = newMessage.getMimeMessage();
                String messageID = getMessageID(mimeMessage);
                ParsedMimeMessage parsed = ParsedMimeMessage.build(mimeMessage);
                EmailWebsocket.EmailEvent emailEvent = messageIdToEvent.computeIfAbsent(messageID, s -> new EmailWebsocket.EmailEvent(parsed.getFrom(), parsed.getSubject(), new ArrayList<>()));
                emailEvent.tos().addAll(parsed.getTos());
            }
            emailWebsocket.newEmails(messageIdToEvent.values());

            sendUnseenCount();
        }
    }

    @SneakyThrows
    public List<EmailHeader> getEmailHeaders() {
        Map<String, MimeMessage> messagesById = new HashMap<>();

        storedMessageStream()
                .filter(it -> !it.isSet(Flags.Flag.DELETED))
                .forEach(it -> {
                    MimeMessage mimeMessage = it.getMimeMessage();
                    messagesById.putIfAbsent(getMessageID(mimeMessage), mimeMessage);
                });
        return messagesById.values()
                .stream()
                .map(EmailService::toEmailHeader)
                .sorted(Comparator.comparing(EmailHeader::sent).reversed())
                .toList();
    }

    private static EmailHeader toEmailHeader(MimeMessage mimeMessage) {
        ParsedMimeMessage message = ParsedMimeMessage.build(mimeMessage);
        return toEmailHeader(message);
    }

    private static EmailHeader toEmailHeader(ParsedMimeMessage message) {
        return new EmailHeader(
                message.getMessageID(),
                message.getSubject(),
                message.getFrom(),
                message.getTos(),
                message.getCcs(),
                message.getSentDate(),
                !message.getAttachments().isEmpty(),
                message.isSeen()
        );
    }

    Email getMessage(String messageID) {
        return storedMessageStream(messageID)
                .map(storedMessage -> ParsedMimeMessage.build(storedMessage.getMimeMessage()))
                .map(message -> new Email(toEmailHeader(message), toEmailBody(message)))
                .findFirst()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, null));
    }

    private static EmailBody toEmailBody(ParsedMimeMessage message) {
        return new EmailBody(
                message.getBestContentType(),
                message.getAttachmentFilenames(),
                message.getBestContent()
        );
    }

    private Stream<StoredMessage> allStoredMessageStream() {
        GreenMail greenMail = greenMailService.getGreenMail();
        ImapHostManager imapHostManager = greenMail.getManagers().getImapHostManager();
        return imapHostManager.getAllMessages()
                .stream();
    }

    private Stream<StoredMessage> storedMessageStream() {
        return allStoredMessageStream()
                .filter(it -> !it.isSet(Flags.Flag.DELETED));
    }

    private Stream<StoredMessage> storedMessageStream(String messageId) {
        return allStoredMessageStream()
                .filter(it -> messageId.equals(getMessageID(it.getMimeMessage())));
    }

    private void sendUnseenCount() {
        emailWebsocket.unseenChanged(getUnseenCount());
    }

    public int getUnseenCount() {
        return (int) storedMessageStream()
                .filter(it -> !it.isSet(Flags.Flag.SEEN))
                .map(it -> getMessageID(it.getMimeMessage()))
                .distinct()
                .count();
    }

    private void setFlag(String messageId, Flags.Flag flag, boolean val) {
        storedMessageStream(messageId).forEach(it -> it.setFlag(flag, val));
    }

    public void deleteAllMessages() {
        storedMessageStream().forEach(it -> it.setFlag(Flags.Flag.DELETED, true));
        sendUnseenCount();
    }

    public void deleteMessage(String messageId) {
        setFlag(messageId, Flags.Flag.DELETED, true);
        sendUnseenCount();
    }

    public void read(String messageId, boolean read) {
        setFlag(messageId, Flags.Flag.SEEN, read);
        sendUnseenCount();
    }

    public StreamedFile attachment(String messageId, String filename) {
        return storedMessageStream(messageId)
                .map(StoredMessage::getMimeMessage)
                .map(ParsedMimeMessage::build)
                .flatMap(message -> message.getAttachments().stream())
                .filter(attachment -> filename.equals(attachment.filename()))
                .map(attachment -> new StreamedFile(
                                new ByteArrayInputStream(attachment.content()),
                                APPLICATION_OCTET_STREAM_TYPE
                        ).attach(attachment.filename())
                )
                .findFirst()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "File not found: %s:%s".formatted(messageId, filename)));
    }

    public StreamedFile original(String messageId) {
        return storedMessageStream(messageId)
                .map(storedMessage -> {
                    MimeMessage mimeMessage = storedMessage.getMimeMessage();
                    return new StreamedFile(toInputStream(mimeMessage), TEXT_PLAIN_TYPE);
                })
                .findFirst()
                .orElseThrow(() -> new HttpStatusException(HttpStatus.NOT_FOUND, "Message not found: %s" + messageId));
    }

    @SneakyThrows
    private static InputStream toInputStream(MimeMessage mimeMessage) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            mimeMessage.writeTo(byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return new ByteArrayInputStream(byteArray);
        }
    }

    @SneakyThrows
    public String upload(InputStream elmInputStream) {
        Session session = greenMailService.createSession();
        MimeMessage mimeMessage = new MimeMessage(session, elmInputStream);
        // Get the known message IDs before we ingest the email.
        Set<String> messageIdsBefore = storedMessageStream()
                .map(it -> getMessageID(it.getMimeMessage()))
                .collect(Collectors.toSet());
        Transport.send(mimeMessage);
        // Find the first message that we didn't know
        return storedMessageStream()
                .map(it -> getMessageID(it.getMimeMessage()))
                .filter(it -> !messageIdsBefore.contains(it))
                .findFirst()
                .orElse(null);

    }
}