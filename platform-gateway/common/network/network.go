package network

import "net"

func IsNetworkTimeout(err error) bool {
	if err == nil {
		return false
	}
	netErr, ok := err.(net.Error)
	if !ok {
		return false
	}

	if netErr.Timeout() {
		return true
	}
	return false
}
