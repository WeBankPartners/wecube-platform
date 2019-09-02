
call create_table('properties', '
  id int auto_increment primary key,
  name varchar(255) not null,
  value varchar(2000),
  scope_type varchar(50) not null default "global",
  scope_value varchar(500),
  seq_no int not null default 0,
  status varchar(50) not null default "active",
  index idx_prop_scope_val (scope_value)
  ');

