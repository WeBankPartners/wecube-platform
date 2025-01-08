package database

import (
	"context"
	"github.com/WeBankPartners/wecube-platform/platform-core/common/db"
	"github.com/WeBankPartners/wecube-platform/platform-core/models"
	"time"
)

func GetTransExportCustomerList(ctx context.Context) (result []*models.DataTransExportCustomerTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_customer order by created_time desc").Find(&result)
	return
}

func QueryTransExportCustomerByName(ctx context.Context, name string) (result []*models.DataTransExportCustomerTable, err error) {
	err = db.MysqlEngine.Context(ctx).SQL("select * from trans_export_customer where name=?", name).Find(&result)
	return
}

func AddTransExportCustomer(ctx context.Context, exportCustomer *models.DataTransExportCustomerTable) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("insert into trans_export_customer(id,name,nexus_addr,nexus_account,nexus_pwd,nexus_repo,"+
		"created_user,created_time) values(?,?,?,?,?,?,?)", exportCustomer.Id, exportCustomer.Name, exportCustomer.NexusAddr,
		exportCustomer.NexusAccount, exportCustomer.NexusPwd, exportCustomer.NexusRepo, exportCustomer.CreatedUser, time.Now().Format(models.DateTimeFormat))
	return
}

func DeleteTransExportCustomer(ctx context.Context, id string) (err error) {
	_, err = db.MysqlEngine.Context(ctx).Exec("delete from trans_export_customer where id=?", id)
	return
}
