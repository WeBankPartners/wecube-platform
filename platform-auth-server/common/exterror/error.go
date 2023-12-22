package exterror

import (
	"encoding/json"
	"fmt"
	"io/ioutil"
	"reflect"
	"strings"

	"github.com/WeBankPartners/wecube-platform/platform-auth-server/api/model"
)

type CustomError struct {
	PassEnable    bool          `json:"passEnable"`    // 透传其它服务报错，不用映射
	Code          int           `json:"code"`          // 错误码
	Message       string        `json:"message"`       // 错误信息模版
	DetailErr     error         `json:"detail"`        // 错误信息
	MessageParams []interface{} `json:"messageParams"` // 消息参数列表
}

func (c CustomError) Error() string {
	return c.Message
}

func (c CustomError) WithParam(params ...interface{}) CustomError {
	c.MessageParams = params
	return c
}

type ErrorTemplate struct {
	CodeMessageMap map[int]string `json:"-"`

	Language string `json:"language"`
	Success  string `json:"success"`

	// ------------- system error -------------
	ServerHandleError    CustomError `json:"server_handle_error"`
	NonRegisterFuncError CustomError `json:"non_register_func_error"`

	// ------------- business error -------------
	AuthServer3000Error CustomError `json:"server_handle_error"`
	AuthServer3001Error CustomError `json:"server_handle_error"`
	AuthServer3002Error CustomError `json:"server_handle_error"`
	AuthServer3003Error CustomError `json:"server_handle_error"`
	AuthServer3004Error CustomError `json:"server_handle_error"`
	AuthServer3005Error CustomError `json:"server_handle_error"`
	AuthServer3006Error CustomError `json:"server_handle_error"`
	AuthServer3007Error CustomError `json:"server_handle_error"`
	AuthServer3008Error CustomError `json:"server_handle_error"`
	AuthServer3009Error CustomError `json:"server_handle_error"`
	AuthServer3010Error CustomError `json:"server_handle_error"`
	AuthServer3012Error CustomError `json:"server_handle_error"`
	AuthServer3013Error CustomError `json:"server_handle_error"`
	AuthServer3014Error CustomError `json:"server_handle_error"`
	AuthServer3015Error CustomError `json:"server_handle_error"`
	AuthServer3016Error CustomError `json:"server_handle_error"`
	AuthServer3017Error CustomError `json:"server_handle_error"`
	AuthServer3018Error CustomError `json:"server_handle_error"`
	AuthServer3019Error CustomError `json:"server_handle_error"`
	AuthServer3020Error CustomError `json:"server_handle_error"`
	AuthServer3021Error CustomError `json:"server_handle_error"`
	AuthServer3022Error CustomError `json:"server_handle_error"`
	AuthServer3023Error CustomError `json:"server_handle_error"`
	AuthServer3024Error CustomError `json:"server_handle_error"`
	AuthServer3025Error CustomError `json:"server_handle_error"`
	AuthServer3026Error CustomError `json:"server_handle_error"`
}

var (
	TemplateList      []*ErrorTemplate
	ErrorDetailReturn bool
)

func InitErrorTemplateList(dirPath string, detailReturn bool) (err error) {
	ErrorDetailReturn = detailReturn
	if !strings.HasSuffix(dirPath, "/") {
		dirPath = dirPath + "/"
	}
	fs, readDirErr := ioutil.ReadDir(dirPath)
	if readDirErr != nil {
		return readDirErr
	}
	if len(fs) == 0 {
		return fmt.Errorf("dirPath:%s is empty dir", dirPath)
	}
	for _, v := range fs {
		if !strings.HasSuffix(v.Name(), ".json") {
			continue
		}
		tmpFileBytes, _ := ioutil.ReadFile(dirPath + v.Name())
		tmpErrorTemplate := ErrorTemplate{}
		tmpErr := json.Unmarshal(tmpFileBytes, &tmpErrorTemplate)
		if tmpErr != nil {
			err = fmt.Errorf("unmarshal json file :%s fail,%s ", v.Name(), tmpErr.Error())
			break
		}
		tmpErrorTemplate.Language = strings.Replace(v.Name(), ".json", "", -1)
		tmpErrorTemplate.CodeMessageMap = make(map[int]string)
		tmpRt := reflect.TypeOf(tmpErrorTemplate)
		tmpVt := reflect.ValueOf(tmpErrorTemplate)
		for i := 0; i < tmpRt.NumField(); i++ {
			if tmpRt.Field(i).Type.Name() == "CustomError" {
				tmpC := tmpVt.Field(i).Interface().(CustomError)
				tmpErrorTemplate.CodeMessageMap[tmpC.Code] = tmpC.Message
			}
		}
		TemplateList = append(TemplateList, &tmpErrorTemplate)
	}
	if err == nil && len(TemplateList) == 0 {
		err = fmt.Errorf("i18n error template list empty")
	}
	return err
}

func New() (et ErrorTemplate) {
	et = ErrorTemplate{}
	if len(TemplateList) > 0 {
		et = *TemplateList[0]
	}
	return
}

func Catch(customErr CustomError, err error) CustomError {
	customErr.DetailErr = err
	return customErr
}

func GetErrorResult(headerLanguage string, err error) model.ResponseWrap {
	var errorResponse model.ResponseWrap
	customErr, b := err.(CustomError)
	if !b {
		customErr = Catch(New().ServerHandleError, err)
	}
	if customErr.Code == 0 {
		customErr = Catch(New().ServerHandleError, err)
	}
	errorResponse = model.ResponseWrap{
		ErrorCode:    customErr.Code,
		ErrorMessage: buildErrMessage(customErr.Message, customErr.MessageParams),
	}
	if ErrorDetailReturn && customErr.DetailErr != nil {
		errorResponse.Data = customErr.DetailErr.Error()
	}
	if headerLanguage == "" || customErr.PassEnable {
		return errorResponse
	}
	errorMessage := getTranslatedErrorMsg(headerLanguage, customErr)
	if errorMessage != "" {
		errorResponse.ErrorMessage = errorMessage
	}
	return errorResponse
}

func getTranslatedErrorMsg(headerLanguage string, customErr CustomError) string {
	headerLanguage = strings.Replace(headerLanguage, ";", ",", -1)
	var errorMessage string
	for _, lang := range strings.Split(headerLanguage, ",") {
		if strings.HasPrefix(lang, "q=") {
			continue
		}
		lang = strings.ToLower(lang)
		for _, template := range TemplateList {
			if template.Language == lang {
				if message, exist := template.CodeMessageMap[customErr.Code]; exist {
					errorMessage = buildErrMessage(message, customErr.MessageParams)
				}
				break
			}
		}
		if errorMessage != "" {
			break
		}
	}
	return errorMessage
}

func buildErrMessage(templateMessage string, params []interface{}) (message string) {
	message = templateMessage
	if strings.Count(templateMessage, "%") == 0 {
		return
	}
	message = fmt.Sprintf(message, params...)
	return
}

func IsBusinessErrorCode(errorCode int) bool {
	return strings.HasPrefix(fmt.Sprintf("%d", errorCode), "2")
}

func IsCustomError(err error) bool {
	_, ok := err.(CustomError)
	return ok
}
