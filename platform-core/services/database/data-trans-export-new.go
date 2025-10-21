package database

import (
	"encoding/json"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

// calculateCmdbAndArtifactIncrementalData 计算CMDB和物料包的增量数据
func calculateCmdbAndArtifactIncrementalData(transExportId string, param models.CreateExportParam) (err error) {
	incrementData := &models.TransDetailCommon{}
	// 序列化为JSON（直接序列化增量数据详情）
	_, err = json.Marshal(incrementData)
	if err != nil {
		return err
	}
	return nil
}
