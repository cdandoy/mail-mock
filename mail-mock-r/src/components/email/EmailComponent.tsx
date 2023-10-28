import {useNavigate, useParams} from "react-router-dom";
import React, {useEffect, useState} from "react";
import axios from "axios";
import './Email.scss'
import {Email} from "../../model";
import {Button} from "react-bootstrap";

export function EmailComponent() {
    let {messageId} = useParams();
    const navigate = useNavigate();
    const [email, setEmail] = useState<Email | undefined>();
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        setLoading(true);
        axios.get<Email>('/emails/' + messageId)
            .then(result => {
                setEmail(result.data);
            })
            .finally(() => setLoading(false))

        axios.put('/emails/read/' + messageId)
            .then(() => {
            })
            .catch(() => {
                console.error('Failed')
            })
    }, []);

    function emailAddress(address: string) {
        return <span key={address}>{address}</span>;
    }

    function handleTrash() {
        axios.delete('/emails/' + messageId)
            .then(() => navigate('/'))
            .catch(() => setError("Failed to delete the message"));
    }

    function attachmentUrl(messageId: string, filename: string) {
        const prefix = document.URL.startsWith("http://localhost:3000") ? "http://localhost:7015" : "";
        return `${prefix}/emails/attachment/${(encodeURIComponent(messageId))}/${(encodeURIComponent(filename))}`;
    }

    if (loading) return <div><i className={"fa fa-2x fa-spinner fa-spin"}/> Loading </div>
    if (!email) return <div><i className={"fa fa-2x fa-error"}/> Email not found </div>

    return (
        <>
            {error && <div className={"alert alert-danger"}>{error}</div>}
            <div id={"email"}>
                <div className={"email-toolbar"}>
                    <Button href="#/" variant={"secondary"} size={"sm"}>
                        <i className={"fa fa-fw fa-arrow-left"}/>
                    </Button>
                    <Button variant={"danger"} size={"sm"} onClick={handleTrash}>
                        <i className={"fa fa-fw fa-trash"}/>
                    </Button>
                </div>
                <table className={"email-headers"}>
                    <tbody>
                    <tr className={"email-subject"}>
                        <td colSpan={2}>
                            <h3>{email.emailHeader.subject}</h3>
                        </td>
                    </tr>
                    <tr className={"email-from"}>
                        <td>From:</td>
                        <td>{emailAddress(email.emailHeader.from)}</td>
                    </tr>
                    <tr className={"email-to"}>
                        <td>To:</td>
                        <td>{email.emailHeader.to?.map(emailAddress)}</td>
                    </tr>
                    <tr className={"email-cc"}>
                        <td>CC:</td>
                        <td>{email.emailHeader.cc?.map(emailAddress)}</td>
                    </tr>
                    <tr>
                        <td></td>
                        <td className={"email-attachments"}>
                            {email.emailBody.attachmentFilenames?.map(filename =>
                                <a key={filename} className={"email-attachment"} href={attachmentUrl(email.emailHeader.messageID, filename)} download={true}>
                                    <i className={"fa fa-paperclip"}/>&nbsp;
                                    <span>{filename}</span>
                                </a>
                            )}
                        </td>
                    </tr>
                    </tbody>
                </table>

                <div className={"email-content"}>
                    {email.emailBody.contentType.startsWith("text/html") && (
                        <div dangerouslySetInnerHTML={{__html: email.emailBody.content}}></div>
                    )}
                    {email.emailBody.contentType.startsWith("text/plain") && (
                        <div>
                            {email.emailBody.content}
                        </div>
                    )}
                </div>
            </div>
        </>
    );
}