package org.dandoy.mm;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record EmailBody(String contentType, List<String> attachmentFilenames, String content) {}
