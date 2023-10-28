# MailMock - A Fake Email server and client.

MailMock is an email server and a Web based email client running in a Docker image.

This is typically used by developers and QA engineers to visualize the emails sent by their application.

## Running MailMock
* Run MailMock: 'docker run -p 6509:8080 -p 25:25 cdandoy/mail-mock`
* Open the email client: [http://localhost:6509/](http://localhost:6509/)
* Configure your application to send emails to the SMTP port:
```
mail.smtp.host=localhost
mail.smtp.port=25
```

