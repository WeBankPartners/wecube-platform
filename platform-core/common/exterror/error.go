package exterror

import (
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"io/ioutil"
	"reflect"
	"strings"
)

type CustomError struct {
	Key           string        `json:"key"`           // 错误编码
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
	CodeKeyMap     map[int]string `json:"-"`

	Language string `json:"language"`
	Success  string `json:"success"`
	// request param validate error
	RequestParamValidateError CustomError `json:"request_param_validate_error"`
	RequestReadBodyError      CustomError `json:"request_read_body_error"`
	RequestJsonUnmarshalError CustomError `json:"request_json_unmarshal_error"`
	RequestTokenValidateError CustomError `json:"request_token_validate_error"`
	RequestTokenExpireError   CustomError `json:"request_token_expire_error"`
	// database error
	DatabaseQueryError      CustomError `json:"database_query_error"`
	DatabaseQueryEmptyError CustomError `json:"database_query_empty_error"`
	DatabaseExecuteError    CustomError `json:"database_execute_error"`
	// sever handle error
	ServerHandleError                CustomError `json:"server_handle_error"`
	PluginDependencyIllegal          CustomError `json:"plugin_dependency_illegal"`
	ProcDefNodeNameEmptyError        CustomError `json:"proc_def_node_name_empty_error"`
	ProcDefNodeNameRepeatError       CustomError `json:"proc_def_node_name_repeat_error"`
	ProcDefNodeServiceNameEmptyError CustomError `json:"proc_def_node_service_name_empty_error"`
	ProcDefNodeDeleteError           CustomError `json:"proc_def__node_delete_error"`
	ProcDefNodeDateEmptyError        CustomError `json:"proc_def_node_date_empty_error"`
	ProcDefNode20000004Error         CustomError `json:"proc_def_node_20000004_error"`
	ProcDefNode20000005Error         CustomError `json:"proc_def_node_20000005_error"`
	ProcDefNode20000006Error         CustomError `json:"proc_def_node_20000006_error"`
	ProcDefNode20000007Error         CustomError `json:"proc_def_node_20000007_error"`
	ProcDefNode20000008Error         CustomError `json:"proc_def_node_20000008_error"`
	ProcDefNode20000009Error         CustomError `json:"proc_def_node_20000009_error"`
	ProcDefNode20000010Error         CustomError `json:"proc_def_node_20000010_error"`
	ProcDefNode20000015Error         CustomError `json:"proc_def_import_low_version_error"`
	ProcDefNode20000016Error         CustomError `json:"proc_def_import_draft_conflict_error"`
	ProcDefNode20000017Error         CustomError `json:"proc_def_import_server_error"`
	ProcDefLoopCheckError            CustomError `json:"proc_def_loop_check_error"`
	ProcDefNameRepeatError           CustomError `json:"proc_def_name_repeat_error"`
	ProcDefRootEntityEmptyError      CustomError `json:"proc_def_root_entity_empty_error"`
	DeleteUserError                  CustomError `json:"delete_user_error"`
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
			continue
		}
		tmpErrorTemplate.Language = strings.Replace(v.Name(), ".json", "", -1)
		tmpErrorTemplate.CodeMessageMap = make(map[int]string)
		tmpErrorTemplate.CodeKeyMap = make(map[int]string)
		tmpRt := reflect.TypeOf(tmpErrorTemplate)
		tmpVt := reflect.ValueOf(tmpErrorTemplate)
		for i := 0; i < tmpRt.NumField(); i++ {
			if tmpRt.Field(i).Type.Name() == "CustomError" {
				tmpC := tmpVt.Field(i).Interface().(CustomError)
				tmpErrorTemplate.CodeMessageMap[tmpC.Code] = tmpC.Message
				tmpErrorTemplate.CodeKeyMap[tmpC.Code] = tmpRt.Field(i).Tag.Get("json")
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

func GetErrorResult(headerLanguage string, err error, defaultErrorCode int) (errorCode int, errorKey, errorMessage string) {
	customErr, b := err.(CustomError)
	if !b {
		return defaultErrorCode, models.DefaultHttpErrorCode, err.Error()
	} else {
		errorCode = customErr.Code
		if headerLanguage == "" || customErr.PassEnable {
			errorMessage = buildErrMessage(customErr.Message, customErr.MessageParams)
			if customErr.DetailErr != nil && ErrorDetailReturn {
				errorMessage = fmt.Sprintf("%s (%s)", errorMessage, customErr.DetailErr.Error())
			}
			return
		}
		headerLanguage = strings.Replace(headerLanguage, ";", ",", -1)
		for _, lang := range strings.Split(headerLanguage, ",") {
			if strings.HasPrefix(lang, "q=") {
				continue
			}
			lang = strings.ToLower(lang)
			for _, template := range TemplateList {
				if template.Language == lang {
					if message, exist := template.CodeMessageMap[errorCode]; exist {
						errorMessage = buildErrMessage(message, customErr.MessageParams)
						errorKey = template.CodeKeyMap[errorCode]
					}
					break
				}
			}
			if errorMessage != "" {
				break
			}
		}
		if errorMessage == "" {
			errorMessage = buildErrMessage(customErr.Message, customErr.MessageParams)
		}
	}
	if customErr.DetailErr != nil && ErrorDetailReturn {
		errorMessage = fmt.Sprintf("%s (%s)", errorMessage, customErr.DetailErr.Error())
	}
	return
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
	if strings.HasPrefix(fmt.Sprintf("%d", errorCode), "2") {
		return true
	}
	return false
}
