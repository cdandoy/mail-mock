package org.dandoy.mm;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record EmailHeader(String messageID, String subject, String from, List<String> to, List<String> cc, long sent, boolean hasAttachments, boolean seen) {}
