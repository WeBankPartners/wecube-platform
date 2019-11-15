delete from menu_items;
insert into menu_items (id,parent_id,code,description) values
(1,null,'JOBS','')
,(2,null,'DESIGNING','')
,(3,null,'IMPLEMENTATION','')
,(4,null,'MONITORING','')
,(5,null,'ADJUSTMENT','')
,(6,null,'INTELLIGENCE_OPS','')
,(7,null,'COLLABORATION','')
,(8,null,'ADMIN','')
,(305,3,'IMPLEMENTATION_WORKFLOW_EXECUTION','')
,(701,7,'COLLABORATION_PLUGIN_MANAGEMENT','')
,(702,7,'COLLABORATION_WORKFLOW_ORCHESTRATION','');