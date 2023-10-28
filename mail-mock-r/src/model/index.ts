export interface EmailHeader {
    messageID: string;
    subject: string;
    from: string;
    to: string[];
    cc: string[];
    sent: number;
    hasAttachments: boolean;
    seen: boolean;
}

export interface Email {
    emailHeader: EmailHeader;
    emailBody: EmailBody;
}

export interface EmailBody {
    contentType: string;
    attachmentFilenames: string[];
    content: string;
}