DROP PROCEDURE IF EXISTS add_column;

DELIMITER $$
$$
CREATE PROCEDURE add_column(IN _tablename VARCHAR(100),IN _columnname VARCHAR(100),IN _datatype VARCHAR(100))
BEGIN
  DECLARE _dbname VARCHAR(100);
  DECLARE _count INT;
  SET _dbname = DATABASE();
  
  SET _count = (SELECT COUNT(1)  FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = _tablename AND COLUMN_NAME = _columnname AND TABLE_SCHEMA = _dbname);
  IF _count = 0 THEN
    SET @ddl = CONCAT('alter table ', _tablename, ' add column (', _columnname, ' ', _datatype, ')');
    PREPARE STMT FROM @ddl;
    EXECUTE STMT;
  END IF;
END$$
DELIMITER ;
