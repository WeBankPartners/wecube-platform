//go:build linux
// +build linux

package network

import (
	"golang.org/x/sys/unix"
	"net"
	"os"
	"syscall"
)

func IsConnReset(err error) bool {
	if err == nil {
		return false
	}
	opErr, ok := err.(*net.OpError)
	if !ok {
		return false
	}

	syscallErr, ok := opErr.Err.(*os.SyscallError)
	if !ok {
		return false
	}

	connresetErr, ok := syscallErr.Err.(syscall.Errno)
	if !ok {
		return false
	}

	if connresetErr == unix.ECONNRESET {
		return true
	}
	return false
}
