package exterror

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-auth-server/model"
	"io/ioutil"
	"reflect"
	"strings"
)

type BadCredentialsError struct {
	s string
}

func NewBadCredentialsError(text string) error {
	return &BadCredentialsError{text}
}

func (e *BadCredentialsError) Error() string {
	return e.s
}

type AuthServerError struct {
	s string
}

func NewAuthServerError(text string) error {
	return &AuthServerError{text}
}

func (e *AuthServerError) Error() string {
	return e.s
}

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
	AuthServer3000Error CustomError `json:"auth_server_3000_error"`
	AuthServer3001Error CustomError `json:"auth_server_3001_error"`
	AuthServer3002Error CustomError `json:"auth_server_3002_error"`
	AuthServer3003Error CustomError `json:"auth_server_3003_error"`
	AuthServer3004Error CustomError `json:"auth_server_3004_error"`
	AuthServer3005Error CustomError `json:"auth_server_3005_error"`
	AuthServer3006Error CustomError `json:"auth_server_3006_error"`
	AuthServer3007Error CustomError `json:"auth_server_3007_error"`
	AuthServer3008Error CustomError `json:"auth_server_3008_error"`
	AuthServer3009Error CustomError `json:"auth_server_3009_error"`
	AuthServer3010Error CustomError `json:"auth_server_3010_error"`
	AuthServer3011Error CustomError `json:"auth_server_3011_error"`
	AuthServer3012Error CustomError `json:"auth_server_3012_error"`
	AuthServer3013Error CustomError `json:"auth_server_3013_error"`
	AuthServer3014Error CustomError `json:"auth_server_3014_error"`
	AuthServer3015Error CustomError `json:"auth_server_3015_error"`
	AuthServer3016Error CustomError `json:"auth_server_3016_error"`
	AuthServer3017Error CustomError `json:"auth_server_3017_error"`
	AuthServer3018Error CustomError `json:"auth_server_3018_error"`
	AuthServer3019Error CustomError `json:"auth_server_3019_error"`
	AuthServer3020Error CustomError `json:"auth_server_3020_error"`
	AuthServer3021Error CustomError `json:"auth_server_3021_error"`
	AuthServer3022Error CustomError `json:"auth_server_3022_error"`
	AuthServer3023Error CustomError `json:"auth_server_3023_error"`
	AuthServer3024Error CustomError `json:"auth_server_3024_error"`
	AuthServer3025Error CustomError `json:"auth_server_3025_error"`
	AuthServer3026Error CustomError `json:"auth_server_3026_error"`
	AuthServer3027Error CustomError `json:"auth_server_3027_error"`
	AuthServer3028Error CustomError `json:"auth_server_3028_error"`
	AuthServer3029Error CustomError `json:"auth_server_3029_error"`
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
	var errorMessage string
	customErr, b := err.(CustomError)
	if !b {
		customErr = Catch(New().ServerHandleError, err)
	}
	if customErr.Code == 0 {
		customErr = Catch(New().ServerHandleError, err)
	}
	errorResponse = model.ResponseWrap{
		ErrorCode: customErr.Code,
		Status:    model.ResponseStatusError,
	}
	if headerLanguage == "" || customErr.PassEnable {
		errorMessage = buildErrMessage(customErr.Message, customErr.MessageParams)
		if ErrorDetailReturn && customErr.DetailErr != nil {
			errorMessage = fmt.Sprintf("%s (%s)", errorMessage, customErr.DetailErr.Error())
		}
		errorResponse.Message = errorMessage
		return errorResponse
	}
	errorResponse.Message = getTranslatedErrorMsg(headerLanguage, customErr)
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
