package com.shanming.mypicturebackend.manager.sharding;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

///**
// * 分表算法类
// */
//public class PictureShardingAlgorithm implements StandardShardingAlgorithm<Long> {
//
//    @Override
//    //Collection<String> availableTargetNames 所有支持的分表
//    //PreciseShardingValue<Long> preciseShardingValue 这个就是在配置文件中添加的分库分表的值 spaceId
//    public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Long> preciseShardingValue) {
//        Long spaceId = preciseShardingValue.getValue();
//        //getLogicTableName()拿到逻辑表，即没有分表的表
//        String logicTableName = preciseShardingValue.getLogicTableName();
//        // spaceId 为 null 表示查询所有图片
//        if (spaceId == null) {
//            return logicTableName;
//        }
//        // 根据 spaceId 动态生成分表名
//        //如果有空间id，则可以拼接真实的表名
//        String realTableName = "picture_" + spaceId;
//        //如果分表中存在该表名，则返回该表，否则依然返回逻辑表
//        if (availableTargetNames.contains(realTableName)) {
//            return realTableName;
//        } else {
//            return logicTableName;
//        }
//    }
//
//    @Override
//    public Collection<String> doSharding(Collection<String> collection, RangeShardingValue<Long> rangeShardingValue) {
//        return new ArrayList<>();
//    }
//
//    @Override
//    public Properties getProps() {
//        return null;
//    }
//
//    @Override
//    public void init(Properties properties) {
//
//    }
//}
