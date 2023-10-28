package org.dandoy.mm;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Properties;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SendMailTests {
    private static final String[] first = {"Edie", "Barney", "Charlotte", "Richard", "Fred", "Zach", "Maxim", "Connie", "Cassandra", "Jakub"};
    private static final String[] last = {"Rowland", "Wilcox", "Morse", "Fuentes", "Sherman", "Sloan", "Harrington", "Cain", "Mccann", "Flynn"};
    private static final String[] hosts = {"takefreetrial.com", "wejars.com", "silvapets.com", "tunneldrive.com", "wayclue.com", "silvanshop.com", "noodledash.com", "slimspirit.com", "spacevocal.com", "spynara.com"};
    private static final String[] words = {"ad", "adipiscing", "aliqua", "aliquip", "amet", "anim", "aute", "cillum", "commodo", "consectetur", "consequat", "culpa", "cupidatat", "deserunt", "do", "dolor", "dolore", "duis", "ea", "eiusmod", "elit", "enim", "esse", "est", "et", "eu", "ex", "excepteur", "exercitation", "fugiat", "id", "in", "incididunt", "ipsum", "irure", "labore", "laboris", "laborum", "lorem", "magna", "minim", "mollit", "nisi", "non", "nostrud", "nulla", "occaecat", "officia", "pariatur", "proident", "qui", "quis", "reprehenderit", "sed", "sint", "sit", "sunt", "tempor", "ullamco", "ut", "velit", "veniam", "voluptate",};
    private static final Random random = new Random();

    @Disabled("For manual tests only")
    @Test
    void testMany() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            testOne();
            Thread.sleep(2000);
        }
    }

    @Disabled("For manual tests only")
    @Test
    void testOne() {
        doit(
                capitalize(randomWords(5)),
                randomEmail(),
                randomEmail(),
                randomEmail()
        );
    }

    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < 10; i++) {
            doit(
                    capitalize(randomWords(5)),
                    randomEmail(),
                    randomEmail(),
                    randomEmail()
            );
            Thread.sleep(2000);
        }
    }

    private static String randomEmail() {
        return "%s.%s@%s".formatted(
                randomString(first),
                randomString(last),
                randomString(hosts)
        );
    }

    private static String capitalize(String s) {
        if (s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    private static String randomWords(int n) {
        return IntStream.range(0, n)
                .mapToObj(it -> randomString(words))
                .collect(Collectors.joining(" "));
    }

    private static String randomHtml() {
        return "<html><body>%s</body></html>".formatted(randomHtmlParagraphs(random.nextInt(5, 15)));
    }

    private static String randomHtmlParagraphs(int n) {
        return IntStream.range(0, n)
                .mapToObj(it -> "<div %s>%s.</div>".formatted(
                        randomStyle(),
                        capitalize(randomWords(25))
                ))
                .collect(Collectors.joining(" "));
    }

    private static String randomStyle() {
        return "style=\"%s%s\"".formatted(
                random.nextInt(3) == 0 ? "font-weight: bold;" : "",
                random.nextInt(3) == 0 ? "font-style: italic;" : ""
        );
    }

    private static String randomString(String[] strings) {
        return strings[random.nextInt(0, strings.length)];
    }

    @SneakyThrows
    static void doit(String someSubject, String from, String to, String cc) {
        Session session = getSession();

        MimeMessage mimeMessage = new MimeMessage(session);
        mimeMessage.setFrom(new InternetAddress(from));
        mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        mimeMessage.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));

        mimeMessage.setSubject(someSubject, "UTF-8");

        Multipart mp = new MimeMultipart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent(randomWords(100), "text/plain");
        mp.addBodyPart(textPart);

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(randomHtml(), "text/html");
        mp.addBodyPart(htmlPart);

        if (random.nextInt(3) == 0) {
            MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            DataSource source = new ByteArrayDataSource(randomWords(200), "text/plain");
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName("attachment.txt");

            mp.addBodyPart(attachmentBodyPart);
        }

        mimeMessage.setContent(mp);
        Transport.send(mimeMessage);
    }

    private static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "localhost");
        props.put("mail.smtp.port", "25");
//        props.put("mail.debug", "true");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication("username", "password");
            }
        });
    }
}
