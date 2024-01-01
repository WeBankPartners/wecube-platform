package service

import (
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/api/middleware"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/constant"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"github.com/WeBankPartners/wecube-platform/platform-gateway/service/remote_route_config"
	"net/url"
	"strconv"
	"sync"
	"time"
)

//const ROUTE_ID_SUFFIX = "#1"

var DynamicRouteItemInfoHolderInstance = &DynamicRouteItemInfoHolder{}
var DynamicRouteConfigurationServiceInstance = &DynamicRouteConfigurationService{}

type MvcHttpMethodAndPath struct {
	HttpMethod string
	Path       string
}

type MvcHttpMethodAndPathConfig struct {
	CreatedTime          int64
	LastModifiedTime     int64
	Disabled             bool
	Version              int
	HttpDestinations     []*model.HttpDestination
	MvcHttpMethodAndPath *MvcHttpMethodAndPath
}

func CreateMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath *MvcHttpMethodAndPath) *MvcHttpMethodAndPathConfig {
	m := &MvcHttpMethodAndPathConfig{
		MvcHttpMethodAndPath: mvcHttpMethodAndPath,
		CreatedTime:          time.Now().UTC().Unix(),
		LastModifiedTime:     time.Now().UTC().Unix(),
	}
	return m
}

func (m *MvcHttpMethodAndPathConfig) FindHttpDestination(criteria *model.HttpDestination) *model.HttpDestination {
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

func (m *MvcHttpMethodAndPathConfig) TryAddHttpDestination(httpDestination *model.HttpDestination) bool {
	if httpDestination == nil {
		return false
	}

	exist := m.FindHttpDestination(httpDestination)
	if exist == nil {
		m.HttpDestinations = append(m.HttpDestinations, &model.HttpDestination{
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

func ArrayRemove[T *model.HttpDestination](arr1 []T, arr2 []T) []T {
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
	m.LastModifiedTime = time.Now().UTC().Unix()
}

func (m *MvcHttpMethodAndPathConfig) CleanOutdatedHttpDestination() {
	toRemove := make([]*model.HttpDestination, 0)
	for _, d := range m.HttpDestinations {
		if d.Version < m.Version {
			toRemove = append(toRemove, d)
		}
	}

	m.HttpDestinations = ArrayRemove(m.HttpDestinations, toRemove)
}

type MvcContextRouteConfig struct {
	Context                 string
	MvcPathRouteConfigs     sync.Map
	DefaultHttpDestinations []*model.HttpDestination
	CreatedTime             int64
	LastModifiedTime        int64
	Disabled                bool
	Version                 int
}

func (c *MvcContextRouteConfig) SetVersion(version int) {
	c.Version = version
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) SetLastModifiedTime() {
	c.LastModifiedTime = time.Now().UTC().Unix()
}

func (c *MvcContextRouteConfig) FindDefaultHttpDestination(criteria *model.HttpDestination) *model.HttpDestination {
	if criteria == nil {
		return nil
	}

	for _, h := range c.DefaultHttpDestinations {
		if h.Equals(criteria) {
			return h
		}
	}
	return nil
}

func (c *MvcContextRouteConfig) TryAddDefaultHttpDestination(httpDestination *model.HttpDestination) bool {
	if httpDestination == nil {
		return false
	}

	exist := c.FindDefaultHttpDestination(httpDestination)
	if exist == nil {
		exist = &model.HttpDestination{
			Scheme: httpDestination.Scheme,
			Host:   httpDestination.Host,
			Port:   httpDestination.Port,
		}
		c.DefaultHttpDestinations = append(c.DefaultHttpDestinations, exist)
	}

	exist.SetWeight(httpDestination.Weight)
	exist.SetVersion(httpDestination.Version)

	c.SetLastModifiedTime()
	return true
}

func (c *MvcContextRouteConfig) CleanOutdatedHttpDestinations() {
	toRemoves := make([]*model.HttpDestination, 0)
	for _, h := range c.DefaultHttpDestinations {
		if h.Version < c.Version {
			toRemoves = append(toRemoves, h)
		}
	}

	c.DefaultHttpDestinations = ArrayRemove(c.DefaultHttpDestinations, toRemoves)
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) CleanOutdatedMvcPathRouteConfigs() {
	outDatedConfigs := make([]*MvcHttpMethodAndPath, 0)

	c.MvcPathRouteConfigs.Range(func(key, value interface{}) bool {
		path := key.(*MvcHttpMethodAndPath)
		config := value.(*MvcHttpMethodAndPathConfig)
		if config.Version < c.Version {
			outDatedConfigs = append(outDatedConfigs, path)
		}
		return true // Return true to continue iterating, or false to stop
	})

	for _, path := range outDatedConfigs {
		c.MvcPathRouteConfigs.Delete(path)
	}
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) cleanOutdated() {
	c.MvcPathRouteConfigs.Range(func(key, value interface{}) bool {
		config := value.(*MvcHttpMethodAndPathConfig)
		config.CleanOutdatedHttpDestination()
		return true // Return true to continue iterating, or false to stop
	})

	c.CleanOutdatedHttpDestinations()
	c.CleanOutdatedMvcPathRouteConfigs()
}

func (c *MvcContextRouteConfig) doAddMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath *MvcHttpMethodAndPath,
	httpDestination *model.HttpDestination) {
	if mvcHttpMethodAndPath == nil || httpDestination == nil {
		return
	}
	var existConfig *MvcHttpMethodAndPathConfig
	val, _ := c.MvcPathRouteConfigs.Load(mvcHttpMethodAndPath)
	if val == nil {
		existConfig = CreateMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath)
		c.MvcPathRouteConfigs.Store(mvcHttpMethodAndPath, existConfig)
	} else {
		existConfig = val.(*MvcHttpMethodAndPathConfig)
	}
	existConfig.SetVersion(c.Version)
	existConfig.TryAddHttpDestination(httpDestination)
	c.SetLastModifiedTime()
}

func (c *MvcContextRouteConfig) tryAddMvcHttpMethodAndPathConfig(mvcPath string, httpMethod string, httpDestination *model.HttpDestination) {
	if len(mvcPath) == 0 || httpDestination == nil || len(httpMethod) == 0 {
		return
	}
	mvcHttpMethodAndPath := &MvcHttpMethodAndPath{
		HttpMethod: httpMethod,
		Path:       mvcPath,
	}
	c.doAddMvcHttpMethodAndPathConfig(mvcHttpMethodAndPath, httpDestination)
}

func (c *MvcContextRouteConfig) tryAddMvcHttpMethodAndPathConfig2(mvcPath string, httpDestination *model.HttpDestination) {
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

	val, _ := c.MvcPathRouteConfigs.Load(&MvcHttpMethodAndPath{
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
		Context:          context,
		CreatedTime:      time.Now().UTC().Unix(),
		LastModifiedTime: time.Now().UTC().Unix(),
	}
}

type DynamicRouteConfigurationService struct {
	isDynamicRouteLoaded  bool
	isDynamicRouteLoading bool
	loadLock              sync.Mutex
	refreshLock           sync.Mutex
	loadedContexts        sync.Map
}

func (d *DynamicRouteConfigurationService) DeleteRouteItem(routeContext string) {
	log.Logger.Info(fmt.Sprintf("to delete route item:%v", routeContext))
	routeId := routeContext //+ ROUTE_ID_SUFFIX

	if _, found := d.loadedContexts.Load(routeId); !found {
		log.Logger.Debug(fmt.Sprintf("such context route does not exist. context=%v", routeId))
		return
	}

	middleware.RemoveRule(routeContext)
	d.loadedContexts.Delete(routeId)
	log.Logger.Info(fmt.Sprintf("delete result:%v", routeId))

}

func Init() {

}

/*func start() {
	if transactionCompensateTicker != nil {
		return
	}

	transactionCompensateTicker = time.NewTicker(time.Duration(DefaultCompensateScanIntervalMin) * time.Minute)
	go func() {
		for {
			select {
			case t := <-transactionCompensateTicker.C:
				startTime := time.Now()
				log.Logger.Debug("begin of transaction compensation process", log.String("ticker", fmt.Sprintf("%v", t)))
				processSuspendedTransactions()
				log.Logger.Debug("end of transaction compensation process", log.String("ticker", fmt.Sprintf("%v", t)),
					log.Int64("cost_ms", time.Now().Sub(startTime).Milliseconds()))
			}
		}
	}()
	log.Logger.Info(fmt.Sprintf("transactionCompensateTicker is created (interval is %d mins)", DefaultCompensateScanIntervalMin))
}
*/
func (d *DynamicRouteConfigurationService) RefreshRoutes() {
	if !DynamicRouteConfigurationServiceInstance.isDynamicRouteLoaded {
		return
	}

	DynamicRouteConfigurationServiceInstance.refreshLock.Lock()
	defer DynamicRouteConfigurationServiceInstance.refreshLock.Unlock()

	d.doRefreshRoutes()
}

func (d *DynamicRouteConfigurationService) doRefreshRoutes() {
	log.Logger.Info("About to fetch route item")
	routeItems, err := remote_route_config.FetchAllRouteItemsWithRestClient()
	if err != nil {
		log.Logger.Error("failed to fetch all route items", log.Error(err))
		return
	}
	d.handleRefreshRouteConfigInfoResponse(routeItems)
}

func (d *DynamicRouteConfigurationService) refreshAllLoadedContexts() {
	rules := middleware.GetAllRedirectRules()
	for i := range rules {
		context := rules[i].Context
		if _, has := d.loadedContexts.Load(context); !has {
			d.loadedContexts.Store(context, "1")
		}
	}
}

func (d *DynamicRouteConfigurationService) initContextRouteConfigs() {
	count := 0
	d.refreshAllLoadedContexts()

	contextRouteConfigs := DynamicRouteItemInfoHolderInstance.RouteConfigs()

	for _, contextRouteConfig := range contextRouteConfigs {
		_, ok := DynamicRouteConfigurationServiceInstance.loadedContexts.Load(contextRouteConfig.Context) // + ROUTE_ID_SUFFIX
		if ok {
			log.Logger.Debug("context route is already loaded ", log.String("context", contextRouteConfig.Context))
			continue
		}

		if initContextRouteConfig(contextRouteConfig) {
			count++
		}
	}
	log.Logger.Debug(fmt.Sprint("add %v route definitions", count))
}

func initContextRouteConfig(contextRouteConfig *MvcContextRouteConfig) bool {
	defaultHttpDestinations := contextRouteConfig.DefaultHttpDestinations
	if len(defaultHttpDestinations) == 0 {
		log.Logger.Warn("Cannot find default http destination for " + contextRouteConfig.Context)
		return false
	}

	targetHttpDestination := defaultHttpDestinations[0]

	itemInfo := DynamicRouteItemInfo{
		Context:    contextRouteConfig.Context,
		Host:       targetHttpDestination.Host,
		Port:       targetHttpDestination.Port,
		HttpScheme: targetHttpDestination.Scheme,
	}
	buildRouteDefinition(contextRouteConfig.Context, &itemInfo)
	return true
}

func buildRouteDefinition(context string, itemInfo *DynamicRouteItemInfo) error {
	urlStr := fmt.Sprintf("%s://%s:%d", itemInfo.HttpScheme, itemInfo.Host, itemInfo.Port)
	parsedURL, err := url.Parse(urlStr)
	if err != nil {
		log.Logger.Error("Error parsing URL:", log.Error(err))
		return err
	}
	uri := parsedURL.RequestURI()

	redirectRule := middleware.RedirectRule{
		Context:    itemInfo.Context,
		Uri:        uri,
		TargetPath: urlStr,
		HttpScheme: itemInfo.HttpScheme,
		Host:       itemInfo.Host,
		Port:       strconv.Itoa(itemInfo.Port),
	}
	middleware.AddRedirectRule(redirectRule)
	return nil
}

func (d *DynamicRouteConfigurationService) handleRefreshRouteConfigInfoResponse(routeItemDtos []*model.RouteItemInfoDto) {
	routeItems := make([]*DynamicRouteItemInfo, len(routeItemDtos))
	for i, remoteItem := range routeItemDtos {
		routeItems[i] = ConvertRouteItem(remoteItem)
	}

	DynamicRouteItemInfoHolderInstance.RefreshRoutes(routeItems)
	d.initContextRouteConfigs()

	outdatedMvcContextRouteConfigs := DynamicRouteItemInfoHolderInstance.outdatedMvcContextRouteConfigs

	for _, config := range outdatedMvcContextRouteConfigs {
		contextRouteId := config.Context //+ ROUTE_ID_SUFFIX
		if _, ok := DynamicRouteConfigurationServiceInstance.loadedContexts.Load(contextRouteId); ok {
			delete(contextRouteId)
			log.Logger.Debug("outdated context route:" + contextRouteId)

			DynamicRouteConfigurationServiceInstance.loadedContexts.Delete(contextRouteId)
		}
	}
}

func delete(uri string) {

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

func buildHttpDestinationDto(http *model.HttpDestination) *model.HttpDestinationDto {
	return &model.HttpDestinationDto{
		CreatedTime:      http.CreatedTime,
		Disabled:         http.Disabled,
		Host:             http.Host,
		LastModifiedTime: http.LastModifiedTime,
		Port:             http.Port,
		Scheme:           http.Scheme,
		Version:          http.Version,
		Weight:           http.Weight,
	}

}

func buildMvcHttpMethodAndPathConfigDto(c *MvcHttpMethodAndPathConfig) *model.MvcHttpMethodAndPathConfigDto {
	result := &model.MvcHttpMethodAndPathConfigDto{
		CreatedTime:      c.CreatedTime,
		Disabled:         c.Disabled,
		HttpMethod:       c.MvcHttpMethodAndPath.HttpMethod,
		Path:             c.MvcHttpMethodAndPath.Path,
		Version:          c.Version,
		LastModifiedTime: c.LastModifiedTime,
		HttpDestinations: make([]*model.HttpDestinationDto, 0),
	}

	for _, d := range c.HttpDestinations {
		result.HttpDestinations = append(result.HttpDestinations, buildHttpDestinationDto(d))
	}
	return result
}

func BuildMvcContextRouteConfigDto(routeConfig *MvcContextRouteConfig) *model.MvcContextRouteConfigDto {

	dto := &model.MvcContextRouteConfigDto{
		Context:                     routeConfig.Context,
		CreatedTime:                 routeConfig.CreatedTime,
		Disabled:                    routeConfig.Disabled,
		LastModifiedTime:            routeConfig.LastModifiedTime,
		Version:                     routeConfig.Version,
		DefaultHttpDestinations:     make([]*model.HttpDestinationDto, 0),
		MvcHttpMethodAndPathConfigs: make([]*model.MvcHttpMethodAndPathConfigDto, 0),
	}

	for _, d := range routeConfig.DefaultHttpDestinations {
		dto.DefaultHttpDestinations = append(dto.DefaultHttpDestinations, buildHttpDestinationDto(d))
	}

	routeConfig.MvcPathRouteConfigs.Range(func(key, value interface{}) bool {
		config := value.(*MvcHttpMethodAndPathConfig)
		dto.MvcHttpMethodAndPathConfigs = append(dto.MvcHttpMethodAndPathConfigs, buildMvcHttpMethodAndPathConfigDto(config))
		return true // Return true to continue iterating, or false to stop
	})

	return dto
}

func GetAllMvcContextRouteConfigs() []*model.MvcContextRouteConfigDto {
	routeContextConfigs := DynamicRouteItemInfoHolderInstance.RouteConfigs()

	result := make([]*model.MvcContextRouteConfigDto, 0)
	for _, c := range routeContextConfigs {
		result = append(result, BuildMvcContextRouteConfigDto(c))
	}
	return result
}
