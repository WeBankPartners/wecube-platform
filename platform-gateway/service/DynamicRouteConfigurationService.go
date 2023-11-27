package service

import (
	"time"
)

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

func (h *HttpDestination) Equals(other *HttpDestination) bool {
	return h.Scheme == other.Scheme &&
		h.Port == other.Port &&
		h.Host == other.Host
}

func (h *HttpDestination) SetVersion(version int) {
	h.Version = version
	h.LastModifiedTime = time.Now()
}

func (h *HttpDestination) SetWeight(weight int) {
	h.Weight = weight
	h.LastModifiedTime = time.Now()
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
	HttpDestinations     []*HttpDestination
	MvcHttpMethodAndPath MvcHttpMethodAndPath
}

func (m *MvcHttpMethodAndPathConfig) FindHttpDestination(criteria *HttpDestination) *HttpDestination {
	if criteria == nil {
		return nil
	}

	for _, h := range m.HttpDestinations {
		if h.Equals(criteria) {
			return h
		}
	}
	return nil
}

func (m *MvcHttpMethodAndPathConfig) TryAddHttpDestination(httpDestination *HttpDestination) bool {
	if httpDestination == nil {
		return false
	}

	exist := m.FindHttpDestination(httpDestination)
	if exist == nil {
		m.HttpDestinations = append(m.HttpDestinations, &HttpDestination{
			Scheme:  httpDestination.Scheme,
			Host:    httpDestination.Host,
			Port:    httpDestination.Port,
			Weight:  httpDestination.Weight,
			Version: m.Version,
		})
		return true
	}

	exist.SetVersion(m.Version)
	exist.SetWeight(httpDestination.Weight)
	return true
}

func ArrayRemove[T *HttpDestination](arr1 []T, arr2 []T) []T {
	// Create a map to store the elements of arr2
	elements := make(map[T]bool)
	for _, val := range arr2 {
		elements[val] = true
	}

	// Create a slice to store the elements present in arr1 but not in arr2
	result := []T{}
	for _, val := range arr1 {
		if !elements[val] {
			result = append(result, val)
		}
	}

	return result
}

func (m *MvcHttpMethodAndPathConfig) CleanOutdatedHttpDestination() {
	toRemove := make([]*HttpDestination, 0)
	for _, d := range m.HttpDestinations {
		if d.Version < m.Version {
			toRemove = append(toRemove, d)
		}
	}

	m.HttpDestinations = ArrayRemove(m.HttpDestinations, toRemove)
}

type MvcContextRouteConfig struct {
	context string
}
