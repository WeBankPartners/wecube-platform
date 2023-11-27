package service

import "time"

type HttpDestination struct {
	Scheme           string
	Port             int
	Host             string
	Weight           int
	CreatedTime      time.Time
	LastModifiedTime time.Time
	Version          int
	Disabled         bool
}

type MvcHttpMethodAndPath struct {
	HttpMethod string
	Path       string
}

type MvcHttpMethodAndPathConfig struct {
	CreatedTime          time.Time
	LastModifiedTime     time.Time
	Disabled             bool
	Version              int
	HttpDestinations     []HttpDestination
	MvcHttpMethodAndPath MvcHttpMethodAndPath
}

type MvcContextRouteConfig struct {
	context string
}
