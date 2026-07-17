package execution

import (
	"context"
	"testing"

	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func TestHandleInputDataUsesConstantValueByParamNameWhenIdChanged(t *testing.T) {
	inputParamDefs := []*models.PluginConfigInterfaceParameters{
		{
			Id:          "new-param-id",
			Name:        "subSystemRegisterInfos",
			DataType:    models.PluginParamDataTypeString,
			MappingType: models.PluginParamMapTypeConstant,
			Required:    "Y",
		},
	}
	entityInstances := []*models.BatchExecutionPluginExecEntityInstances{
		{Id: "entity-id", BusinessKeyValue: "entity-key"},
	}
	inputConstantMap := map[string]string{
		"subSystemRegisterInfos": `[{"subSystem":"SUBSYS_CODE"}]`,
	}
	rootExpr := &models.ExpressionObj{Package: "wecmdb", Entity: "deploy_environment"}

	inputParamDatas, err := handleInputData(context.Background(), "", "", entityInstances, inputParamDefs, rootExpr, inputConstantMap, nil, &models.ProcInsNodeReq{})
	if err != nil {
		t.Fatalf("handleInputData returned error: %v", err)
	}

	if got := inputParamDatas[0]["subSystemRegisterInfos"]; got != `[{"subSystem":"SUBSYS_CODE"}]` {
		t.Fatalf("subSystemRegisterInfos = %v, want %s", got, `[{"subSystem":"SUBSYS_CODE"}]`)
	}
}
