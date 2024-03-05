package remote

import (
	"bytes"
	"crypto/tls"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/exterror"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"net/smtp"
	"strings"
)

func getMailSource() (authObj smtp.Auth, senderObj *models.SendMailSource, err error) {
	senderObj, err = GetPlatformMailVar()
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

func SendSmtpMail(smo models.SendMailTarget) (err error) {
	authObj, senderObj, getSourceErr := getMailSource()
	if getSourceErr != nil {
		err = fmt.Errorf("send smtp mail fail with get config,%s ", getSourceErr.Error())
		return
	}
	if senderObj.SSL {
		err = sendSMTPMailTLS(smo, authObj, senderObj)
		return
	}
	if !strings.Contains(senderObj.Server, ":") {
		senderObj.Server = fmt.Sprintf("%s:25", senderObj.Server)
	}
	err = smtp.SendMail(senderObj.Server, authObj, senderObj.Sender, smo.Accept, mailQQMessage(smo.Accept, smo.Subject, smo.Content, senderObj.Sender))
	if err != nil {
		err = fmt.Errorf("Send mail error,%s ", err.Error())
	}
	return
}

func sendSMTPMailTLS(smo models.SendMailTarget, authObj smtp.Auth, senderObj *models.SendMailSource) (err error) {
	tlsConfig := &tls.Config{
		InsecureSkipVerify: true,
		ServerName:         senderObj.Server,
	}
	address := senderObj.Server
	if !strings.Contains(senderObj.Server, ":") {
		address = fmt.Sprintf("%s:465", senderObj.Server)
	}
	conn, dialErr := tls.Dial("tcp", address, tlsConfig)
	if dialErr != nil {
		err = fmt.Errorf("tls dial address:%s fail,%s", address, dialErr.Error())
		return
	}
	client, newClientErr := smtp.NewClient(conn, senderObj.Server)
	if newClientErr != nil {
		err = fmt.Errorf("new smtp client fail,%s ", newClientErr.Error())
		return
	}
	defer client.Close()
	if b, _ := client.Extension("AUTH"); b {
		err = client.Auth(authObj)
		if err != nil {
			err = fmt.Errorf("Client auth fail,%s ", err.Error())
			return
		}
	}
	err = client.Mail(senderObj.Sender)
	if err != nil {
		err = fmt.Errorf("Client mail set from fail:%s ", err.Error())
		return
	}
	for _, to := range smo.Accept {
		if err = client.Rcpt(to); err != nil {
			err = fmt.Errorf("Client rcpt %s error,%s ", to, err.Error())
			return
		}
	}
	w, dataInitErr := client.Data()
	if dataInitErr != nil {
		err = fmt.Errorf("Client data init error,%s ", dataInitErr.Error())
		return
	}
	_, err = w.Write(mailQQMessage(smo.Accept, smo.Subject, smo.Content, senderObj.Sender))
	if err != nil {
		err = fmt.Errorf("Write message error,%s ", err.Error())
		return
	}
	w.Close()
	err = client.Quit()
	if err != nil {
		err = fmt.Errorf("Client quit error,%s ", err.Error())
		return
	}
	return
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

func GetPlatformMailVar() (senderObj *models.SendMailSource, err error) {
	var sysRows []*models.SystemVariables
	err = db.MysqlEngine.SQL("select name,`value`,default_value from system_variables where name in (?,?,?,?) and status='active' and source=?", models.SysVarMailSender, models.SysVarMailServer, models.SysVarMailPassword, models.SysVarMailSSL, models.SysVarSystemSource).Find(&sysRows)
	if err != nil {
		err = exterror.Catch(exterror.New().DatabaseQueryError, err)
		return
	}
	senderObj = &models.SendMailSource{}
	for _, row := range sysRows {
		tmpV := row.DefaultValue
		if row.Value != "" {
			tmpV = row.Value
		}
		switch row.Name {
		case models.SysVarMailSender:
			senderObj.Sender = tmpV
		case models.SysVarMailServer:
			senderObj.Server = tmpV
		case models.SysVarMailPassword:
			senderObj.Password = tmpV
		case models.SysVarMailSSL:
			tmpV = strings.ToLower(tmpV)
			if tmpV == "y" || tmpV == "yes" || tmpV == "true" {
				senderObj.SSL = true
			}
		}
	}
	return
}
