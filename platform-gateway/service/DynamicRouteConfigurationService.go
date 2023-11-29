package service

import (
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/service/remote_route_config"
	"sync"
	"time"
)

var dynamicRouteHolder DynamicRouteItemInfoHolder

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
	MvcHttpMethodAndPath *MvcHttpMethodAndPath
}

func CreateMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath *MvcHttpMethodAndPath) *MvcHttpMethodAndPathConfig {
	m := &MvcHttpMethodAndPathConfig{
		MvcHttpMethodAndPath: mvcHttpMethodAndPath,
		CreatedTime:          time.Now(),
		LastModifiedTime:     time.Now(),
	}
	return m
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

func (m *MvcHttpMethodAndPathConfig) SetVersion(version int) {
	m.Version = version
	m.SetLastModifiedTime()
}

func (m *MvcHttpMethodAndPathConfig) SetLastModifiedTime() {
	m.LastModifiedTime = time.Now()
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
	context                 string
	mvcPathRouteConfigs     sync.Map
	defaultHttpDestinations []*HttpDestination
	createdTime             time.Time
	lastModifiedTime        time.Time
	disabled                bool
	version                 int
}

func (c *MvcContextRouteConfig) SetVersion(version int) {
	c.version = version
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) SetLastModifiedTime() {
	c.lastModifiedTime = time.Now()
}

func (c *MvcContextRouteConfig) FindDefaultHttpDestination(criteria *HttpDestination) *HttpDestination {
	if criteria == nil {
		return nil
	}

	for _, h := range c.defaultHttpDestinations {
		if h.Equals(criteria) {
			return h
		}
	}
	return nil
}

func (c *MvcContextRouteConfig) TryAddDefaultHttpDestination(httpDestination *HttpDestination) bool {
	if httpDestination == nil {
		return false
	}

	exist := c.FindDefaultHttpDestination(httpDestination)
	if exist == nil {
		exist = &HttpDestination{
			Scheme: httpDestination.Scheme,
			Host:   httpDestination.Host,
			Port:   httpDestination.Port,
		}
		c.defaultHttpDestinations = append(c.defaultHttpDestinations, exist)
	}

	exist.SetWeight(httpDestination.Weight)
	exist.SetVersion(httpDestination.Version)

	c.SetLastModifiedTime()
	return true
}

func (c *MvcContextRouteConfig) CleanOutdatedHttpDestinations() {
	toRemoves := make([]*HttpDestination, 0)
	for _, h := range c.defaultHttpDestinations {
		if h.Version < c.version {
			toRemoves = append(toRemoves, h)
		}
	}

	c.defaultHttpDestinations = ArrayRemove(c.defaultHttpDestinations, toRemoves)
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) CleanOutdatedMvcPathRouteConfigs() {
	outDatedConfigs := make([]*MvcHttpMethodAndPath, 0)

	c.mvcPathRouteConfigs.Range(func(key, value interface{}) bool {
		path := key.(*MvcHttpMethodAndPath)
		config := value.(*MvcHttpMethodAndPathConfig)
		if config.Version < c.version {
			outDatedConfigs = append(outDatedConfigs, path)
		}
		return true // Return true to continue iterating, or false to stop
	})

	for _, path := range outDatedConfigs {
		c.mvcPathRouteConfigs.Delete(path)
	}
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) cleanOutdated() {
	c.mvcPathRouteConfigs.Range(func(key, value interface{}) bool {
		config := value.(*MvcHttpMethodAndPathConfig)
		config.CleanOutdatedHttpDestination()
		return true // Return true to continue iterating, or false to stop
	})

	c.CleanOutdatedHttpDestinations()
	c.CleanOutdatedMvcPathRouteConfigs()
}

func (c *MvcContextRouteConfig) doAddMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath *MvcHttpMethodAndPath,
	httpDestination *HttpDestination) {
	if mvcHttpMethodAndPath == nil || httpDestination == nil {
		return
	}
	var existConfig *MvcHttpMethodAndPathConfig
	val, _ := c.mvcPathRouteConfigs.Load(mvcHttpMethodAndPath)
	if val == nil {
		existConfig = CreateMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath)
		c.mvcPathRouteConfigs.Store(mvcHttpMethodAndPath, existConfig)
	} else {
		existConfig = val.(*MvcHttpMethodAndPathConfig)
	}
	existConfig.SetVersion(c.version)
	existConfig.TryAddHttpDestination(httpDestination)
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) tryAddMvcHttpMethodAndPathConfig(mvcPath string, httpMethod string, httpDestination *HttpDestination) {
	if len(mvcPath) == 0 || httpDestination == nil || len(httpMethod) == 0 {
		return
	}
	mvcHttpMethodAndPath := &MvcHttpMethodAndPath{
		HttpMethod: httpMethod,
		Path:       mvcPath,
	}
	c.doAddMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath, httpDestination)
}

func (c *MvcContextRouteConfig) tryAddMvcHttpMethodAndPathConfig2(mvcPath string, httpDestination *HttpDestination) {
	if len(mvcPath) == 0 || httpDestination == nil {
		return
	}

	for _, httpMethod := range constant.HttpMethods {
		mvcHttpMethodAndPath := &MvcHttpMethodAndPath{
			HttpMethod: httpMethod,
			Path:       mvcPath,
		}
		c.doAddMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath, httpDestination)
	}
}

func (c *MvcContextRouteConfig) findByMvcHttpMethodAndPath(httpMethod string, path string) *MvcHttpMethodAndPathConfig {
	if len(httpMethod) == 0 || len(path) == 0 {
		return nil
	}

	val, _ := c.mvcPathRouteConfigs.Load(&MvcHttpMethodAndPath{
		HttpMethod: httpMethod,
		Path:       path,
	})
	if val != nil {
		return val.(*MvcHttpMethodAndPathConfig)
	} else {
		return nil
	}
}

func CreateMvcContextRouteConfig(context string) *MvcContextRouteConfig {
	return &MvcContextRouteConfig{
		context:          context,
		createdTime:      time.Now(),
		lastModifiedTime: time.Now(),
	}
}

type DynamicRouteConfigurationService struct {
	isDynamicRouteLoaded  bool
	isDynamicRouteLoading bool
	loadLock              sync.Mutex
	refreshLock           sync.Mutex
}

func (s DynamicRouteConfigurationService) RefreshRoutes() {
	if !s.isDynamicRouteLoaded {
		return
	}

	s.refreshLock.Lock()
	defer s.refreshLock.Unlock()

}

func (s DynamicRouteConfigurationService) doRefreshRoutes() {
	log.Logger.Info("About to fetch route item")
	routeItems, err := remote_route_config.FetchAllRouteItemsWithRestClient()
	if err != nil {
		log.Logger.Error("failed to fetch all route items", log.Error(err))
		return
	}
}

func (s DynamicRouteConfigurationService) handleRefreshRouteConfigInfoResponse() {

}
