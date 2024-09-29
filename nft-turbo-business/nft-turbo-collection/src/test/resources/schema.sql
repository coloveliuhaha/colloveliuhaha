/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = collection   */
/******************************************/
CREATE TABLE IF NOT EXISTS  `collection` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增主键）',
  `gmt_create` datetime NOT NULL COMMENT '创建时间',
  `gmt_modified` datetime NOT NULL COMMENT '最后更新时间',
  `name` varchar(512) DEFAULT NULL COMMENT '藏品名称',
  `cover` varchar(512) DEFAULT NULL COMMENT '藏品封面',
  `class_id` varchar(128) DEFAULT NULL COMMENT '藏品类目ID',
  `price` decimal(10,0) DEFAULT NULL COMMENT '价格',
  `quantity` bigint DEFAULT NULL COMMENT '藏品数量',
  `detail` text COMMENT '藏品详情',
  `saleable_inventory` bigint DEFAULT NULL COMMENT '可销售库存',
  `occupied_inventory` bigint DEFAULT NULL COMMENT '已占用库存',
  `identifier` varchar(128) DEFAULT NULL COMMENT '幂等号',
   `version` int DEFAULT NULL COMMENT '版本号',
  `state` varchar(128) DEFAULT NULL COMMENT '状态',
  `creator_id` varchar(128) DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '藏品创建时间',
  `sale_time` datetime DEFAULT NULL COMMENT '藏品发售时间',
  `sync_chain_time` datetime DEFAULT NULL COMMENT '藏品上链时间',
  `deleted` int DEFAULT NULL COMMENT '是否逻辑删除，0为未删除，非0为已删除',
  `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
)
;
/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = held_collection   */
/******************************************/
CREATE TABLE IF NOT EXISTS  `held_collection` (
   `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增主键）',
   `gmt_create` datetime NOT NULL COMMENT '创建时间',
   `gmt_modified` datetime NOT NULL COMMENT '最后更新时间',
   `collection_id` bigint unsigned DEFAULT NULL COMMENT '藏品id',
   `serial_no` varchar(256) DEFAULT NULL COMMENT '藏品编号',
   `nft_id` varchar(256) DEFAULT NULL COMMENT 'NFT唯一编号',
   `pre_id` varchar(128) DEFAULT NULL COMMENT '上一个持有人id',
   `user_id` varchar(128) DEFAULT NULL COMMENT '持有人id',
   `state` varchar(128) DEFAULT NULL COMMENT '状态',
   `name` varchar(512) DEFAULT NULL COMMENT '藏品名称',
   `cover` varchar(512) DEFAULT NULL COMMENT '藏品封面',
   `purchase_price` decimal(10,0) DEFAULT NULL COMMENT '购入价格',
   `biz_type` varchar(128) DEFAULT NULL COMMENT ' 业务类型',
   `biz_no` varchar(128) DEFAULT NULL COMMENT '业务单据号',
   `tx_hash` varchar(256) DEFAULT NULL COMMENT '交易hash',
   `hold_time` datetime DEFAULT NULL COMMENT '藏品持有时间',
   `sync_chain_time` datetime DEFAULT NULL COMMENT '藏品同步时间',
   `delete_time` datetime DEFAULT NULL COMMENT '藏品销毁时间',
   `deleted` int DEFAULT NULL COMMENT '是否逻辑删除，0为未删除，非0为已删除',
   `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
   PRIMARY KEY (`id`)
)
;

/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = collection_stream   */
/******************************************/
CREATE TABLE IF NOT EXISTS  `collection_stream` (
     `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增主键）',
     `collection_id` bigint unsigned NOT NULL  COMMENT '藏品ID',
     `gmt_create` datetime NOT NULL COMMENT '创建时间',
     `gmt_modified` datetime NOT NULL COMMENT '最后更新时间',
     `name` varchar(512)  DEFAULT NULL COMMENT '藏品名称',
     `cover` varchar(512)   DEFAULT NULL COMMENT '藏品封面',
     `class_id` varchar(128)  DEFAULT NULL COMMENT '藏品类目ID',
     `stream_type` varchar(128)  DEFAULT NULL COMMENT '流水类型',
     `price` decimal(10,0) DEFAULT NULL COMMENT '价格',
     `quantity` bigint DEFAULT NULL COMMENT '藏品数量',
     `detail` text  COMMENT '详情',
     `saleable_inventory` bigint DEFAULT NULL COMMENT '可销售库存',
     `occupied_inventory` bigint DEFAULT NULL COMMENT '已占用库存',
     `state` varchar(128)  DEFAULT NULL COMMENT '状态',
     `create_time` datetime DEFAULT NULL COMMENT '藏品创建时间',
     `sale_time` datetime DEFAULT NULL COMMENT '藏品发售时间',
     `sync_chain_time` datetime DEFAULT NULL COMMENT '藏品上链时间',
     `identifier` varchar(128) DEFAULT NULL COMMENT '幂等号',
     `deleted` int DEFAULT NULL COMMENT '是否逻辑删除，0为未删除，非0为已删除',
     `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
     PRIMARY KEY (`id`)
) ;

/******************************************/
/*   DatabaseName = nfturbo   */
/*   TableName = collection_snapshot   */
/******************************************/
CREATE TABLE IF NOT EXISTS  `collection_snapshot` (
       `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增主键）',
       `gmt_create` datetime NOT NULL COMMENT '创建时间',
       `gmt_modified` datetime NOT NULL COMMENT '最后更新时间',
       `collection_id` bigint NOT NULL COMMENT '藏品id',
       `name` varchar(512) DEFAULT NULL COMMENT '藏品名称',
       `cover` varchar(512) DEFAULT NULL COMMENT '藏品封面',
       `class_id` varchar(128) DEFAULT NULL COMMENT '藏品类目ID',
       `price` decimal(18,6) DEFAULT NULL COMMENT '价格',
       `quantity` bigint DEFAULT NULL COMMENT '藏品数量',
       `detail` text COMMENT '详情',
       `saleable_inventory` bigint DEFAULT NULL COMMENT '可销售库存',
       `create_time` datetime DEFAULT NULL COMMENT '藏品创建时间',
       `sale_time` datetime DEFAULT NULL COMMENT '藏品发售时间',
       `sync_chain_time` datetime DEFAULT NULL COMMENT '藏品上链时间',
       `deleted` int DEFAULT NULL COMMENT '是否逻辑删除，0为未删除，非0为已删除',
       `lock_version` int DEFAULT NULL COMMENT '乐观锁版本号',
       `creator_id` varchar(128) DEFAULT NULL COMMENT '创建者',
       `version` int DEFAULT NULL COMMENT '修改版本',
       PRIMARY KEY (`id`)
) ;
