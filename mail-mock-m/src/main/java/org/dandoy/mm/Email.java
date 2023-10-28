package org.dandoy.mm;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Email(EmailHeader emailHeader, EmailBody emailBody) {}
