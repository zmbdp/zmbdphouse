use `zmbdphouse_dev`;

INSERT INTO sys_dictionary_type (type_key, value, remark, status)
VALUES ('device_list', '设备列表', '房屋设备', 1),
       ('position_list', '方向列表', '房屋朝向', 1),
       ('rent_type_list', '租房类型列表', '', 1),
       ('rent_range', '租金', '租金范围', 1),
       ('rent_type', '租房类型', '房屋出租类型', 1),
       ('room_num', '居室', '房源居室数量', 1),
       ('rent_time', '租期', '房屋出租时长', 1);

INSERT INTO sys_dictionary_data (type_key, data_key, value, remark, sort, status)
VALUES ('device_list', 'soft', '沙发', '', 1, 1),
       ('device_list', 'washer', '洗衣机', '', 2, 1),
       ('device_list', 'heater', '热水器', '', 3, 1),
       ('device_list', 'broadband', '宽带', '', 4, 1),
       ('device_list', 'icebox', '冰箱', '', 5, 1),
       ('device_list', 'hood', '油烟机', '', 6, 1),
       ('device_list', 'gas', '燃气灶', '', 7, 1),
       ('device_list', 'cook', '可做饭', '', 8, 1),
       ('device_list', 'tv', '电视', '', 9, 1),
       ('device_list', 'aircondition', '空调', '', 10, 1);
INSERT INTO sys_dictionary_data (type_key, data_key, value, remark, sort, status)
VALUES ('device_list', 'wardrobe', '衣柜', '', 11, 1),
       ('device_list', 'bed', '床', '', 12, 1),
       ('device_list', 'toilet', '卫生间', '', 13, 1),
       ('device_list', 'smartlock', '智能门锁', '', 14, 1),
       ('device_list', 'balcony', '阳台', '', 15, 1),
       ('device_list', 'heating', '暖气', '', 16, 1),
       ('position_list', 'east', '东', '', 1, 1),
       ('position_list', 'south', '南', '', 2, 1),
       ('position_list', 'west', '西', '', 3, 1),
       ('position_list', 'north', '北', '', 4, 1);
INSERT INTO sys_dictionary_data (type_key, data_key, value, remark, sort, status)
VALUES ('rent_type_list', 'whole_rent', '整租', '', 1, 1),
       ('rent_type_list', 'share_rent', '合租', '', 2, 1),
       ('rent_type_list', 'worry_free_rental', '省心租', '', 3, 1),
       ('rent_type_list', 'apartment', '公寓', '', 4, 1),
       ('rent_type_list', 'personal_house', '个人房源', '', 4, 1),
       ('rent_range', 'range_1', '<1000元', '', 1, 1),
       ('rent_range', 'range_2', '1000-1500元', '', 2, 1),
       ('rent_range', 'range_3', '1500-2000元', '', 3, 1),
       ('rent_range', 'range_4', '2000-3000元', '', 4, 1),
       ('rent_range', 'range_5', '3000-5000元', '', 5, 1);
INSERT INTO sys_dictionary_data (type_key, data_key, value, remark, sort, status)
VALUES ('rent_range', 'range_6', '>5000元', '', 6, 1),
       ('rent_type', 'whole', '整租', '', 2, 1),
       ('rent_type', 'shared', '合租', '', 3, 1),
       ('rent_type', 'trouble_free', '省心租', '', 4, 1),
       ('rent_type', 'apartment', '公寓', '', 5, 1),
       ('rent_type', 'individual', '个人房源', '', 6, 1),
       ('room_num', 'one', '1居', '', 2, 1),
       ('room_num', 'two', '2居', '', 3, 1),
       ('room_num', 'three', '3居', '', 4, 1),
       ('room_num', 'four', '4居', '', 5, 1);
INSERT INTO sys_dictionary_data (type_key, data_key, value, remark, sort, status)
VALUES ('room_num', 'more', '4居以上', '', 6, 1),
       ('rent_time', 'thirty_seconds', '30秒', '', 1, 1),
       ('rent_time', 'half_year', '半年', '', 2, 1),
       ('rent_time', 'one_year', '一年', '', 3, 1);
INSERT INTO tag (tag_code, tag_name)
VALUES ('near_subway', '近地铁'),
       ('civil_water_elec', '民水民电'),
       ('lift', '有电梯'),
       ('central_heating', '集中供暖'),
       ('renovation', '精装修'),
       ('cook', '可做饭');