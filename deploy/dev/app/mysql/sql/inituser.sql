-- 1、初始化数据库：创建nacos 外置数据库 zmbdphouse_nacos_dev 和脚手架业务数据库 zmbdphouse_dev
-- 2、创建用户，用户名：zmbdpdev 密码：Hf@173503494
-- 3、授予zmbdpdev用户特定权限
create database if not exists `zmbdphouse_nacos_dev` default character set utf8mb4 collate utf8mb4_general_ci;
create database if not exists `zmbdphouse_dev` default character set utf8mb4 collate utf8mb4_general_ci;

create user 'zmbdpdev'@'%' identified BY 'Hf@173503494';

grant replication slave, replication client on *.* to 'zmbdpdev'@'%';
grant all privileges on zmbdphouse_nacos_dev.* to 'zmbdpdev'@'%';
grant all privileges on zmbdphouse_dev.* to 'zmbdpdev'@'%';
FLUSH PRIVILEGES;

