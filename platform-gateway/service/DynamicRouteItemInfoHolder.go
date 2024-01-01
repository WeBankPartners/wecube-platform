package service

import (
	"github.com/WeBankPartners/wecube-platform/platform-gateway/model"
	"strconv"
	"sync"
)

type DynamicRouteItemInfo struct {
	ItemId     string
	Context    string
	HttpMethod string
	Path       string
	HttpScheme string
	Host       string
	Port       int
	Disabled   bool
	Weight     int
}

func ConvertRouteItem(dto *model.RouteItemInfoDto) *DynamicRouteItemInfo {
	d := DynamicRouteItemInfo{}
	d.Host = dto.Host
	d.Path = dto.Path
	d.HttpMethod = dto.HttpMethod
	d.HttpScheme = dto.HttpScheme
	d.Context = dto.Context
	port, _ := strconv.Atoi(dto.Port)
	d.Port = port
	weight, _ := strconv.Atoi(dto.Weight)
	d.Weight = weight
	return &d
}

type DynamicRouteItemInfoHolder struct {
	mvcContextRouteConfigs         sync.Map
	lastVersion                    int
	currentVersion                 int
	unreachableHttpDestinations    []*model.HttpDestination
	outdatedMvcContextRouteConfigs []*MvcContextRouteConfig
}

func (h DynamicRouteItemInfoHolder) Init() {
	h.unreachableHttpDestinations = make([]*model.HttpDestination, 0)
	h.outdatedMvcContextRouteConfigs = make([]*MvcContextRouteConfig, 0)
}

func (h *DynamicRouteItemInfoHolder) RefreshRoutes(fullyDynamicRouteItemInfos []*DynamicRouteItemInfo) {
	if len(fullyDynamicRouteItemInfos) == 0 {
		return
	}

	h.increaseVersion()

	for _, item := range fullyDynamicRouteItemInfos {
		if item == nil {
			continue
		}
		if len(item.Context) == 0 {
			continue
		}
		h.tryAddDynamicRouteItemInfo(item)
	}

	h.cleanOutdated()
}

func (h *DynamicRouteItemInfoHolder) cleanOutdated() {
	h.clearUnreachableHttpDestinations()
	h.cleanOutdatedMvcContextRouteConfigs()
	h.cleanMvcContextRouteConfigs()
}

func (h *DynamicRouteItemInfoHolder) increaseVersion() {
	h.lastVersion = h.currentVersion
	h.currentVersion = h.currentVersion + 1
}

func (h *DynamicRouteItemInfoHolder) tryAddDynamicRouteItemInfo(item *DynamicRouteItemInfo) {
	var existConfig *MvcContextRouteConfig
	val, ok := h.mvcContextRouteConfigs.Load(item.Context)
	existConfig = val.(*MvcContextRouteConfig)
	if !ok {
		existConfig = &MvcContextRouteConfig{
			Context: item.Context,
		}
		h.mvcContextRouteConfigs.Store(item.Context, existConfig)
	}

	existConfig.SetVersion(h.currentVersion)
	if len(item.HttpMethod) == 0 && len(item.Path) == 0 {
		existConfig.tryAddMvcHttpMethodAndPathConfig2(item.Path, &model.HttpDestination{
			Scheme: item.HttpScheme,
			Host:   item.Host,
			Port:   item.Port,
			Weight: item.Weight,
		})
		return
	}

	if len(item.HttpMethod) == 0 {
		return
	}

	existConfig.tryAddMvcHttpMethodAndPathConfig(item.Path, item.HttpMethod,
		&model.HttpDestination{
			Scheme: item.HttpScheme,
			Host:   item.Host,
			Port:   item.Port,
			Weight: item.Weight,
		})
}

func (h *DynamicRouteItemInfoHolder) clearUnreachableHttpDestinations() {
	h.unreachableHttpDestinations = make([]*model.HttpDestination, 0)
}

func (h *DynamicRouteItemInfoHolder) cleanOutdatedMvcContextRouteConfigs() {
	h.outdatedMvcContextRouteConfigs = make([]*MvcContextRouteConfig, 0)
}

func (h *DynamicRouteItemInfoHolder) cleanMvcContextRouteConfigs() {
	h.mvcContextRouteConfigs.Range(func(key, value interface{}) bool {
		config := value.(*MvcContextRouteConfig)
		config.cleanOutdated()

		if config.Version < h.currentVersion {
			h.outdatedMvcContextRouteConfigs = append(h.outdatedMvcContextRouteConfigs, config)
		}
		return true // Return true to continue iterating, or false to stop
	})

	for _, config := range h.outdatedMvcContextRouteConfigs {
		h.mvcContextRouteConfigs.Delete(config.Context)
	}
}

func (h *DynamicRouteItemInfoHolder) findContextConfig(context, path, httpMethod string) *MvcContextRouteConfig {
	if len(context) == 0 || len(path) == 0 || len(httpMethod) == 0 {
		return nil
	}
	val, _ := h.mvcContextRouteConfigs.Load(context)
	if val != nil {
		return val.(*MvcContextRouteConfig)
	}
	return nil
}

func (h *DynamicRouteItemInfoHolder) findRoute(context, path, httpMethod string) *MvcHttpMethodAndPathConfig {
	ctxConfig := h.findContextConfig(context, path, httpMethod)
	if ctxConfig == nil {
		return nil
	}

	return ctxConfig.findByMvcHttpMethodAndPath(httpMethod, path)
}

func (h *DynamicRouteItemInfoHolder) findDefaultRoute(context, path, httpMethod string) []*model.HttpDestination {
	ctxConfig := h.findContextConfig(context, path, httpMethod)
	if ctxConfig == nil {
		return make([]*model.HttpDestination, 0)
	}

	return ctxConfig.DefaultHttpDestinations

}

func (h *DynamicRouteItemInfoHolder) getMvcContextRouteConfig(context string) *MvcContextRouteConfig {
	if len(context) == 0 {
		return nil
	}
	val, _ := h.mvcContextRouteConfigs.Load(context)
	if val != nil {
		return val.(*MvcContextRouteConfig)
	}
	return nil
}

func (h *DynamicRouteItemInfoHolder) RouteConfigs() []*MvcContextRouteConfig {
	values := make([]*MvcContextRouteConfig, 0)
	h.mvcContextRouteConfigs.Range(func(key, value interface{}) bool {
		values = append(values, value.(*MvcContextRouteConfig))
		return true // Return true to continue iterating, or false to stop
	})
	return values
}
