import {Button, Form} from "react-bootstrap";
import React, {useCallback, useMemo, useState} from "react";
import {DropzoneRootProps, useDropzone} from 'react-dropzone'

import "./UploadComponent.scss"
import axios from "axios";
import {useNavigate} from "react-router-dom";
import {TopHeader} from "../topheader/TopHeader";

const baseStyle = {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    padding: '20px',
    borderWidth: 2,
    borderRadius: 2,
    borderColor: '#eeeeee',
    borderStyle: 'dashed',
    backgroundColor: '#fafafa',
    color: '#bdbdbd',
    outline: 'none',
    transition: 'border .24s ease-in-out'
};

const focusedStyle = {
    borderColor: '#2196f3'
};

const acceptStyle = {
    borderColor: '#00e676'
};

const rejectStyle = {
    borderColor: '#ff1744'
};

interface UploadResponse {
    messageId?: string;
}

export function UploadComponent() {
    const navigate = useNavigate();
    const [error, setError] = useState("");
    const [multipart, setMultipart] = useState("");
    const onDrop = useCallback((acceptedFiles: File[]) => {
        handleFile(acceptedFiles[0])
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const {
        getRootProps,
        getInputProps,
        isFocused,
        isDragAccept,
        isDragReject
    } = useDropzone({
        // accept: {'*/*': []},
        onDrop: onDrop,
    });
    const style: DropzoneRootProps = useMemo(() => ({
        ...baseStyle,
        ...(isFocused ? focusedStyle : {}),
        ...(isDragAccept ? acceptStyle : {}),
        ...(isDragReject ? rejectStyle : {})
    }), [
        isFocused,
        isDragAccept,
        isDragReject,
    ]);

    function handleFile(file: File) {
        let formData = new FormData();
        formData.append('file', file);
        setError("")
        axios.post<UploadResponse>('/emails/upload', formData)
            .then(result => {
                let messageId = result.data.messageId;
                if (messageId) navigate(`/email/${messageId}`)
                else navigate('/')
            })
            .catch(() => {
                setError("Failed to parse the uploaded email");
            });
    }

    function handleText() {
        setError("")
        axios.post<UploadResponse>('/emails/upload-text', {multipart})
            .then(result => {
                let messageId = result.data.messageId;
                if (messageId) navigate(`/email/${messageId}`)
                else navigate('/')
            })
            .catch(() => {
                setError("Failed to parse the uploaded email");
            });
    }

    function Toolbar() {
        return <Button href="#/" variant={"primary"} size={"sm"} title={"Inbox"} className={"email-toolbar-back"}>
            <i className={"fa fa-fw fa-arrow-left"}/>
            Back
        </Button>
    }

    return (
        <div id={"upload"}>
            <TopHeader toolbar={<Toolbar/>}/>
            <div className="container">
                {error && <div className={"alert alert-danger"}>{error}</div>}
                <div {...getRootProps({style})}>
                    <input {...getInputProps()} />
                    <ul>
                        <li>Drop your .eml file here</li>
                        <li>or click to select a file on your file system</li>
                        <li>or enter the multi-part text below</li>
                    </ul>
                </div>
                <Form>
                    <Form.Group className="mb-3">
                        <Form.Label>Multi-part</Form.Label>
                        <Form.Control className={"multipart"}
                                      as="textarea"
                                      rows={20}
                                      placeholder={`Date: ${new Date()}\nFrom: Netflix <info@members.netflix.com>\nTo: netflix@example.com\nSubject: John, we just added a docuseries you might like`}
                                      onChange={e=>setMultipart(e.target.value)}
                                      value={multipart}
                        />
                    </Form.Group>
                    <Button variant="primary" onClick={handleText}>
                        <i className={"fa fa-fw fa-upload"}/>&nbsp;
                        Upload
                    </Button>
                </Form>

            </div>
        </div>
    );
}