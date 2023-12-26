package database

func GetAllModels() {
	// select * from plugin_package_attributes where entity_id in (select id from plugin_package_entities where data_model_id in (select id from plugin_package_data_model where concat(package_name,'_',`version`) in (select concat(package_name,'_',max(`version`)) from plugin_package_data_model group by package_name)));
}
