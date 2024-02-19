package cron

import (
	"fmt"
	"time"

	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/log"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
)

func SetupCleanUpBatchExecTicker() {
	ticker := time.NewTicker(24 * time.Hour)
	// ticker := time.NewTicker(90 * time.Second)
	go func() {
		for {
			select {
			case t := <-ticker.C:
				startTime := time.Now()
				log.Logger.Info("start clean up batch exec", log.String("ticker", fmt.Sprintf("%v", t)))
				CleanUpBatchExecRecord()
				log.Logger.Info("finish clean up batch exec", log.String("ticker", fmt.Sprintf("%v", t)),
					log.Int64("cost_ms", time.Now().Sub(startTime).Milliseconds()))
			}
		}
	}()
	log.Logger.Info("setup clean up batch exec ticker")
	return
}

func CleanUpBatchExecRecord() {
	keepBatchExecDays := models.Config.Cron.KeepBatchExecDays
	if keepBatchExecDays == 0 {
		keepBatchExecDays = models.DefaultKeepBatchExecDays
	}
	now := time.Now()
	keepBatchExecTime := now.Add(-time.Duration(keepBatchExecDays) * 24 * time.Hour)
	keepBatchExecTimeStr := keepBatchExecTime.String()

	transId := fmt.Sprintf("clean_up_batch_exec_record_%d", time.Now().Unix())
	var actions []*db.ExecAction
	// delete batchExecution
	action := &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExec, " WHERE created_time<?"),
		Param: []interface{}{keepBatchExecTimeStr},
	}
	actions = append(actions, action)

	// delete batchExecutionJobs
	action = &db.ExecAction{
		Sql:   db.CombineDBSql("DELETE FROM ", models.TableNameBatchExecJobs, " WHERE execute_time<?"),
		Param: []interface{}{keepBatchExecTimeStr},
	}
	actions = append(actions, action)

	err := db.Transaction(actions, db.DBCtx(transId))
	if err != nil {
		log.Logger.Error("clean up batch exec sql failed", log.Error(err))
	}
	return
}
