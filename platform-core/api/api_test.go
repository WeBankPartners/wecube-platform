package api

import "testing"

func TestSkipSensitiveBodyLog(t *testing.T) {
	if !skipSensitiveBodyLog("/platform/v1/tools/password/encrypt") {
		t.Fatalf("password encrypt request body should not be logged")
	}
	if !skipSensitiveBodyLog("/platform/v1/tools/password/encrypt?trace=1") {
		t.Fatalf("password encrypt request body should not be logged when query exists")
	}
	if skipSensitiveBodyLog("/platform/v1/system-variables/update") {
		t.Fatalf("system variable update request body should be logged")
	}
}
