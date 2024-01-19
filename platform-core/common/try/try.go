package try

import (
	"bytes"
	"fmt"
	"runtime"
)

func Exception(handle func(e interface{})) {
	if err := recover(); err != nil {
		handle(err)
	}
}

func GetErrorMessage(err interface{}) string {
	buf := new(bytes.Buffer)
	fmt.Fprintf(buf, "%v", err)
	return buf.String()
}

func GetErrorStackTrace(err interface{}) string {
	buf := new(bytes.Buffer)
	fmt.Fprintf(buf, "%v\n", err)
	for i := 1; ; i++ {
		pc, file, line, ok := runtime.Caller(i)
		if !ok {
			break
		}
		fmt.Fprintf(buf, "%s:%d (0x%x)\n", file, line, pc)
	}
	return buf.String()
}

func ExceptionStack(handle func(e interface{}, err interface{})) {
	if err := recover(); err != nil {
		e := printStackTrace(err)
		handle(e, err)
	}
}

func ExceptionStack1(handle func(e interface{})) {
	if err := recover(); err != nil {
		e := printStackTrace(err)
		handle(e)
	}
}

func printStackTrace(err interface{}) string {
	buf := new(bytes.Buffer)
	fmt.Fprintf(buf, "%v\n", err)
	for i := 1; ; i++ {
		pc, file, line, ok := runtime.Caller(i)
		if !ok {
			break
		}
		fmt.Fprintf(buf, "%s:%d (0x%x)\n", file, line, pc)
	}
	return buf.String()
}
