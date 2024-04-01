package model

const (
	StatusOK    = "OK"
	StatusError = "ERROR"

	OkMessage = "success"
)

type ResponseWrap struct {

	// Processing result code (return 0 if process successfully)
	Status string `json:"status"`

	// Processing result description message.
	Message string `json:"message"`

	Data interface{} `json:"data"`
}
