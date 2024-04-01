package remote

import (
	"bytes"
	"encoding/json"
	"fmt"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"io"
	"net/http"
	"strings"
)

func RegisterPluginRoute(pluginPackageName, host, port string) (err error) {
	if models.Config.Gateway.HostPorts == "" {
		return
	}
	postParam := models.RegisterGatewayRouteParam{Context: pluginPackageName}
	postParam.Items = []*models.RegisterGatewayRouteItem{{Context: pluginPackageName, HttpScheme: "http", Host: host, Port: port}}
	postBytes, _ := json.Marshal(postParam)
	for _, gatewayUrl := range strings.Split(models.Config.Gateway.Url, ",") {
		if err = doGatewayHttpRequest(fmt.Sprintf("http://%s/gateway/v1/route-items", gatewayUrl), postBytes); err != nil {
			err = fmt.Errorf("do http reqeust to gateway register route items %s,%s,%s fail,%s ", pluginPackageName, host, port, err.Error())
			break
		}
	}
	return
}

func doGatewayHttpRequest(httpUrl string, postBytes []byte) (err error) {
	req, reqErr := http.NewRequest(http.MethodPost, httpUrl, bytes.NewReader(postBytes))
	if reqErr != nil {
		err = fmt.Errorf("new http reqeust fail,%s ", reqErr.Error())
		return
	}
	resp, respErr := http.DefaultClient.Do(req)
	if respErr != nil {
		err = fmt.Errorf("do http reqeust fail,%s ", respErr.Error())
		return
	}
	respBytes, _ := io.ReadAll(resp.Body)
	resp.Body.Close()
	var response models.CommonGatewayResp
	if err = json.Unmarshal(respBytes, &response); err != nil {
		err = fmt.Errorf("json unmarhsal response body fail,%s ", err.Error())
		return
	}
	if response.Status != "OK" {
		err = fmt.Errorf(response.Message)
	}
	return
}
