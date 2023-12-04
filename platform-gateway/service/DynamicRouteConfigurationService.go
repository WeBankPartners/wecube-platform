package service

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/service/remote_route_config"
	"strconv"
	"sync"
	"time"
)

const ROUTE_ID_SUFFIX = "#1"

var dynamicRouteHolderInstance DynamicRouteItemInfoHolder
var dynamicRouteConfigServiceInstance DynamicRouteConfigurationService

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
	loadedContexts        sync.Map
}

func RefreshRoutes() {
	if !dynamicRouteConfigServiceInstance.isDynamicRouteLoaded {
		return
	}

	dynamicRouteConfigServiceInstance.refreshLock.Lock()
	defer dynamicRouteConfigServiceInstance.refreshLock.Unlock()

}

func doRefreshRoutes() {
	log.Logger.Info("About to fetch route item")
	routeItems, err := remote_route_config.FetchAllRouteItemsWithRestClient()
	if err != nil {
		log.Logger.Error("failed to fetch all route items", log.Error(err))
		return
	}
	handleRefreshRouteConfigInfoResponse(routeItems)
}

func refreshAllLoadedContexts() {

}

func initContextRouteConfigs() {
	count := 0
	//TODO
	//refreshAllLoadedContexts();

	contextRouteConfigs := dynamicRouteHolderInstance.routeConfigs()

	for _, contextRouteConfig := range contextRouteConfigs {
		_, ok := dynamicRouteConfigServiceInstance.loadedContexts.Load(contextRouteConfig.context + ROUTE_ID_SUFFIX)
		if ok {
			log.Logger.Debug("context route is already loaded ", log.String("context", contextRouteConfig.context))
			continue
		}

		if initContextRouteConfig(contextRouteConfig) {
			count++
		}
	}
	log.Logger.Debug(fmt.Sprint("add %v route definitions", count))
}

func initContextRouteConfig(contextRouteConfig *MvcContextRouteConfig) bool {
	defaultHttpDestinations := contextRouteConfig.defaultHttpDestinations
	if len(defaultHttpDestinations) == 0 {
		log.Logger.Warn("Cannot find default http destination for " + contextRouteConfig.context)
		return false
	}

	targetHttpDestination := defaultHttpDestinations[0]

	itemInfo := DynamicRouteItemInfo{
		Context:    contextRouteConfig.context,
		Host:       targetHttpDestination.Host,
		Port:       targetHttpDestination.Port,
		HttpScheme: targetHttpDestination.Scheme,
	}
	buildRouteDefinition(contextRouteConfig.context, &itemInfo)
	return true
}

func buildRouteDefinition(context string, itemInfo *DynamicRouteItemInfo) {
	urlStr := fmt.Sprintf("%s://%s:%d", itemInfo.HttpScheme, itemInfo.Host, itemInfo.Port)
	redirectRule := middleware.RedirectRule{
		Context:    itemInfo.Context,
		TargetPath: urlStr,
		HttpScheme: itemInfo.HttpScheme,
		Host:       itemInfo.Host,
		Port:       strconv.Itoa(itemInfo.Port),
	}
	middleware.AddRedirectRule(redirectRule)
}

func handleRefreshRouteConfigInfoResponse(routeItemDtos []*model.RouteItemInfoDto) {
	routeItems := make([]*DynamicRouteItemInfo, len(routeItemDtos))
	for i, remoteItem := range routeItemDtos {
		routeItems[i] = ConvertRouteItem(remoteItem)
	}

	dynamicRouteHolderInstance.RefreshRoutes(routeItems)
	initContextRouteConfigs()

	outdatedMvcContextRouteConfigs := dynamicRouteHolderInstance.outdatedMvcContextRouteConfigs

	for _, config := range outdatedMvcContextRouteConfigs {
		contextRouteId := config.context + ROUTE_ID_SUFFIX
		if _, ok := dynamicRouteConfigServiceInstance.loadedContexts.Load(contextRouteId); ok {
			delete(contextRouteId)
			log.Logger.Debug("outdated context route:" + contextRouteId)

			dynamicRouteConfigServiceInstance.loadedContexts.Delete(contextRouteId)
		}
	}
}

func delete(id string) {

}

func ListAllContextRouteItems() []model.RouteItemInfoDto {
	routeItems := make([]model.RouteItemInfoDto, 0)
	redirectRules := middleware.GetAllRedirectRules()
	for _, rrule := range redirectRules {
		routeItem := model.RouteItemInfoDto{
			Context:    rrule.Context,
			HttpScheme: rrule.HttpScheme,
			Host:       rrule.Host,
			Port:       rrule.Port,
		}
		routeItems = append(routeItems, routeItem)
	}
	return routeItems
}
