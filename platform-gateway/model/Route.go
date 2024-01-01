package model

import "time"

type HttpDestination struct {
	Scheme           string
	Port             int
	Host             string
	Weight           int
	CreatedTime      int64
	LastModifiedTime int64
	Version          int
	Disabled         bool
}

func (h *HttpDestination) Equals(other *HttpDestination) bool {
	return h.Scheme == other.Scheme &&
		h.Port == other.Port &&
		h.Host == other.Host
}

func (h *HttpDestination) SetVersion(version int) {
	h.Version = version
	h.LastModifiedTime = time.Now().UTC().Unix()
}

func (h *HttpDestination) SetWeight(weight int) {
	h.Weight = weight
	h.LastModifiedTime = time.Now().UTC().Unix()
}
