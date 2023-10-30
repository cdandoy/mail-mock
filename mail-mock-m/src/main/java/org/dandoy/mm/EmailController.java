package org.dandoy.mm;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.http.multipart.CompletedFileUpload;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.serde.annotation.Serdeable;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;

@Controller("/emails")
@Slf4j
public class EmailController {
    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @Get()
    public List<EmailHeader> getEmailHeaders() {
        return emailService.getEmailHeaders();
    }

    @Get("{id}")
    public Email getEmail(@PathVariable String id) {
        return emailService.getMessage(id);
    }

    @Put("read/{id}")
    public void read(@PathVariable String id, @QueryValue("read") @Nullable Boolean read) {
        emailService.read(id, read == null || read);
    }

    @Serdeable
    public record UnseenResponse(int unseen) {}

    @Get("unseen")
    public UnseenResponse unseen() {
        int unseen = emailService.getUnseenCount();
        return new UnseenResponse(unseen);
    }

    @Delete
    public void deleteAll() {
        emailService.deleteAllMessages();
    }

    @Delete("{id}")
    public void deleteMessage(@PathVariable String id) {
        emailService.deleteMessage(id);
    }

    @Get("attachment/{id}/{filename}")
    public StreamedFile attachment(@PathVariable String id, @PathVariable String filename) {
        return emailService.attachment(id, filename);
    }

    @Get("content/{id}")
    public StreamedFile content(@PathVariable String id) {
        return emailService.content(id);
    }

    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Post("upload")
    public void upload(CompletedFileUpload file) {
        try (BufferedInputStream inputStream = new BufferedInputStream(file.getInputStream())) {
            emailService.upload(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
