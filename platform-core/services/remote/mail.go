package remote

import (
	"bytes"
	"crypto/tls"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"github.com/WeBankPartners/wecube-platform/platform-core/services/database"
	"net/smtp"
	"strings"
)

func getMailSource() (authObj smtp.Auth, senderObj *models.SendMailSource, err error) {
	senderObj, err = database.GetPlatformMailVar()
	if err != nil {
		return
	}
	if senderObj.Sender == "" || senderObj.Server == "" {
		err = fmt.Errorf("mail source sender and server can not empty")
		return
	}
	authObj = smtp.PlainAuth("", senderObj.Sender, senderObj.Password, senderObj.Server)
	return
}

func SendSmtpMail(smo models.SendMailTarget) {
	authObj, senderObj, err := getMailSource()
	if err != nil {
		log.Logger.Error("send smtp mail fail with get config", log.Error(err))
		return
	}
	if senderObj.SSL {
		sendSMTPMailTLS(smo, authObj, senderObj)
		return
	}
	if !strings.Contains(senderObj.Server, ":") {
		senderObj.Server = fmt.Sprintf("%s:25", senderObj.Server)
	}
	err = smtp.SendMail(senderObj.Server, authObj, senderObj.Sender, smo.Accept, mailQQMessage(smo.Accept, smo.Subject, smo.Content, senderObj.Sender))
	if err != nil {
		log.Logger.Error("Send mail error", log.Error(err))
	}
}

func sendSMTPMailTLS(smo models.SendMailTarget, authObj smtp.Auth, senderObj *models.SendMailSource) {
	tlsConfig := &tls.Config{
		InsecureSkipVerify: true,
		ServerName:         senderObj.Server,
	}
	address := senderObj.Server
	if !strings.Contains(senderObj.Server, ":") {
		address = fmt.Sprintf("%s:465", senderObj.Server)
	}
	conn, err := tls.Dial("tcp", address, tlsConfig)
	if err != nil {
		log.Logger.Error("Tls dial error", log.Error(err))
		return
	}
	client, err := smtp.NewClient(conn, senderObj.Server)
	if err != nil {
		log.Logger.Error("Smtp new client error", log.Error(err))
		return
	}
	defer client.Close()
	if b, _ := client.Extension("AUTH"); b {
		err = client.Auth(authObj)
		if err != nil {
			log.Logger.Error("Client auth error", log.Error(err))
			return
		}
	}
	err = client.Mail(senderObj.Sender)
	if err != nil {
		log.Logger.Error("Client mail set from error", log.Error(err))
		return
	}
	for _, to := range smo.Accept {
		if err = client.Rcpt(to); err != nil {
			log.Logger.Error(fmt.Sprintf("Client rcpt %s error", to), log.Error(err))
			return
		}
	}
	w, err := client.Data()
	if err != nil {
		log.Logger.Error("Client data init error", log.Error(err))
		return
	}
	_, err = w.Write(mailQQMessage(smo.Accept, smo.Subject, smo.Content, senderObj.Sender))
	if err != nil {
		log.Logger.Error("Write message error", log.Error(err))
		return
	}
	w.Close()
	err = client.Quit()
	if err != nil {
		log.Logger.Error("Client quit error", log.Error(err))
		return
	}
}

func mailQQMessage(to []string, subject, content, sender string) []byte {
	var buff bytes.Buffer
	buff.WriteString("To:")
	buff.WriteString(strings.Join(to, ","))
	buff.WriteString("\r\nFrom:")
	buff.WriteString(sender + "<" + sender + ">")
	buff.WriteString("\r\nSubject:")
	buff.WriteString(subject)
	buff.WriteString("\r\nContent-Type:text/plain;charset=UTF-8\r\n\r\n")
	buff.WriteString(content)
	return buff.Bytes()
}
