package database

import (
	"context"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"time"
)

func GetTransExportCustomerList(ctx context.Context) (result []*models.DataTransExportCustomerTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_customer order by updated_time desc").Find(&result)
	return
}

func QueryTransExportCustomerByName(ctx context.Context, name string) (result []*models.DataTransExportCustomerTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_customer where name=?", name).Find(&result)
	return
}

func GetTransExportCustomer(ctx context.Context, id string) (result *models.DataTransExportCustomerTable, err error) {
	var list []*models.DataTransExportCustomerTable
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_customer where id=?", id).Find(&list)
	if len(list) > 0 {
		result = list[0]
	}
	return
}

func AddTransExportCustomer(ctx context.Context, exportCustomer *models.DataTransExportCustomerTable) (err error) {
	now := time.Now().Format(models.DateTimeFormat)
	_, err = db.MysqlEngine.Context(ctx).Exec("insert into trans_export_customer(id,name,nexus_addr,nexus_account,nexus_pwd,nexus_repo,"+
		"created_user,created_time,updated_time,exec_workflow_ids) values(?,?,?,?,?,?,?,?,?,?)", exportCustomer.Id, exportCustomer.Name, exportCustomer.NexusAddr,
		exportCustomer.NexusAccount, exportCustomer.NexusPwd, exportCustomer.NexusRepo, exportCustomer.CreatedUser, now, now, exportCustomer.ExecWorkflowIds)
	return
}

func UpdateTransExportCustomer(ctx context.Context, exportCustomer *models.DataTransExportCustomerTable) (err error) {
	now := time.Now().Format(models.DateTimeFormat)
	_, err = db.MysqlEngine.Context(ctx).Exec("update trans_export_customer set name=?,nexus_addr=?,nexus_account=?,nexus_pwd=?,nexus_repo=?,"+
		"updated_time=?,exec_workflow_ids=? where id=?", exportCustomer.Name, exportCustomer.NexusAddr, exportCustomer.NexusAccount, exportCustomer.NexusPwd,
		exportCustomer.NexusRepo, now, exportCustomer.ExecWorkflowIds, exportCustomer.Id)
	return
}

func DeleteTransExportCustomer(ctx context.Context, id string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("delete from trans_export_customer where id=?", id)
	return
}
