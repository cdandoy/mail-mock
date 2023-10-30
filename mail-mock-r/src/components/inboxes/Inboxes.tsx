import React, {useEffect, useReducer, useState} from "react";
import axios from "axios";
import {useNavigate} from "react-router-dom";
import './Inboxes.scss'
import {Button} from "react-bootstrap";
import {EmailHeader} from "../../model";
import {useEmailWebsocket} from "../../EmailWebsocket";
import {NoMails} from "./NoMails";
import Form from 'react-bootstrap/Form';
import {useSessionStorage} from "usehooks-ts";

interface HeaderProps {
    selectedMessageIds: string[],
    deleteAll: () => void,
    deleteSelected: () => void,
    search: string,
    setSearch: (search: string) => void,
}

function Header({selectedMessageIds, deleteAll, deleteSelected, search, setSearch}: HeaderProps) {
    const hasSelection = selectedMessageIds.length > 0;

    return <div className={"inbox-header"}>
        <div className={"inbox-header-left"}>
            <Button className={"inbox-delete"}
                    variant={hasSelection ? "danger" : "secondary"}
                    size={"sm"}
                    title={"Delete"}
                    onClick={deleteSelected}
                    disabled={!hasSelection}>
                Delete
            </Button>

            <Button className={"inbox-purge"}
                    variant={"danger"}
                    size={"sm"}
                    title={"Delete"}
                    onClick={deleteAll}>
                Purge
            </Button>
            <Button className={"inbox-upload"}
                    variant={""}
                    size={"sm"}
                    title={"Upload"}
                    href={"#upload"}>
                <i className={"fa fa-upload"}/>
            </Button>
        </div>
        <div className={"inbox-header-right"}>
            <Form.Control type="search" spellCheck="false" placeholder="Search" value={search} onChange={e => setSearch(e.target.value.toLowerCase())}/>
        </div>
    </div>
}

export function Inboxes() {
    const navigate = useNavigate();
    const [emailHeaders, setEmailHeaders] = useState<EmailHeader[]>([]);
    const [update, forceUpdate] = useReducer(x => x + 1, 0);
    const [selectedMessageIds, setSelectedMessageIds] = useState<string[]>([]);
    const [search, setSearch] = useSessionStorage("search", "");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const emailWebsocketState = useEmailWebsocket();

    useEffect(() => {
        setLoading(true);
        axios.get<EmailHeader[]>('/emails')
            .then(response => {
                setEmailHeaders(response.data);
            })
            .catch(() => setError('Failed to get the emails'))
            .finally(() => setLoading(false));
    }, [update, emailWebsocketState.unseen]);

    function handleEmailClicked(messageID: string) {
        navigate(`email/${messageID}`);
    }

    function handleCheckboxChanged(event: React.ChangeEvent<HTMLInputElement>, header: EmailHeader) {
        const selection = selectedMessageIds.slice();
        const index = selection.indexOf(header.messageID);
        if (index >= 0) selection.splice(index, 1);
        if (event.target.checked) selection.push(header.messageID);
        setSelectedMessageIds(selection);
    }

    function unselect(messageId: string) {
        const index = selectedMessageIds.indexOf(messageId);
        if (index >= 0) {
            const selection = selectedMessageIds.slice();
            selection.splice(index, 1)
            setSelectedMessageIds(selection);
        }
    }

    function deleteAll() {
        axios.delete('/emails')
            .catch(() => setError(`Failed to delete all messages`));
        setSelectedMessageIds([]);
        forceUpdate();
    }

    function deleteSelected() {
        selectedMessageIds.forEach(messageId => {
            axios.delete(`/emails/${messageId}`)
                .catch(() => setError(`Failed to delete ${messageId}`))
        })
        setSelectedMessageIds([]);
        forceUpdate();
    }

    function deleteOne(e: React.MouseEvent, messageId: string) {
        e.stopPropagation();
        unselect(messageId);

        axios.delete(`/emails/${messageId}`)
            .catch(() => setError(`Failed to delete ${messageId}`))
        forceUpdate();
    }

    function isSearchMatch(emailHeader: EmailHeader): boolean {
        function matchesTo(match: string): boolean {
            for (const to of emailHeader.to) {
                if (to.toLowerCase().indexOf(match) >= 0) return true;
            }
            return false;
        }

        function matchesFrom(match: string): boolean {
            return emailHeader.from.toLowerCase().indexOf(match) >= 0;
        }

        function matchesSubject(match: string): boolean {
            return emailHeader.subject.toLowerCase().indexOf(match) >= 0;
        }

        if (search.startsWith("to:")) matchesTo(search.substring(3));
        else if (search.startsWith("from:")) return matchesFrom(search.substring(5));
        else if (search.startsWith("subject:")) return matchesFrom(search.substring(8));
        return matchesTo(search) || matchesSubject(search);
    }

    const today = new Date().setHours(0, 0, 0, 0);

    const filteredEmails = emailHeaders.filter(it => isSearchMatch(it));

    return (
        <div id={"inboxes"}>
            {loading && <div className={"loading-container"}>
                <div className={"loading-content"}>
                    <i className={"fa fa-spinner fa-spin"}/>&nbsp;
                    Loading
                </div>
            </div>}

            <Header selectedMessageIds={selectedMessageIds} deleteAll={deleteAll} deleteSelected={deleteSelected} search={search} setSearch={setSearch}/>

            {filteredEmails.length === 0 ?
                <NoMails search={search}/>
                :
                <div className={"inbox-container"}>
                    {error && <div className={"alert alert-danger"}>{error}</div>}
                    <table className={"table table-hover"}>
                        <tbody>
                        {filteredEmails
                            .filter(emailHeader => isSearchMatch(emailHeader))
                            .map(emailHeader => {
                                const checked = selectedMessageIds.indexOf(emailHeader.messageID) >= 0
                                const sentDate = new Date(emailHeader.sent);
                                const sentString = today === new Date(emailHeader.sent).setHours(0, 0, 0, 0) ? sentDate.toLocaleTimeString() : sentDate.toLocaleDateString();
                                return (
                                    <tr key={emailHeader.messageID}
                                        onClick={() => handleEmailClicked(emailHeader.messageID)}
                                        className={emailHeader.seen ? "email-seen" : ""}>
                                        <td className={"inbox-checkbox"} onClick={(event) => event.stopPropagation()}>
                                            <input type={"checkbox"}
                                                   checked={checked}
                                                   onChange={(event) => handleCheckboxChanged(event, emailHeader)}/>
                                        </td>
                                        <td className={"inbox-to"}>
                                            <div>
                                                {emailHeader.to && emailHeader.to.length > 0 ? emailHeader.to[0] : ''}
                                            </div>
                                        </td>
                                        <td className={"inbox-subject"}>{emailHeader.subject}</td>
                                        <td className={"inbox-attach"}>{emailHeader.hasAttachments ? <i className={'fa fa-paperclip'}/> : ''}</td>
                                        <td className={"inbox-sent"}>
                                            <div className={"email-toolbar"}>
                                                <div className={"email-tools"}>
                                                    <Button className={"inbox-delete"}
                                                            variant={"secondary"}
                                                            size={"sm"}
                                                            title={"Delete"}
                                                            onClick={(e) => deleteOne(e, emailHeader.messageID)}
                                                    >
                                                        <i className={"fa fa-trash"}/>
                                                    </Button>
                                                </div>
                                            </div>
                                            {sentString}
                                        </td>
                                    </tr>
                                );
                            })}
                        </tbody>
                    </table>
                </div>
            }
        </div>
    );
}