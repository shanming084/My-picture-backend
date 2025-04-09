package com.shanming.mypicturebackend.manager.sharding;

import com.baomidou.mybatisplus.extension.toolkit.SqlRunner;
import com.shanming.mypicturebackend.model.entity.Space;
import com.shanming.mypicturebackend.model.enums.SpaceLevelEnum;
import com.shanming.mypicturebackend.model.enums.SpaceTypeEnum;
import com.shanming.mypicturebackend.service.SpaceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//@Slf4j
//public class DynamicShardingManager {
//
//    @Resource
//    private DataSource dataSource;
//
//    @Resource
//    private SpaceService spaceService;
//
//    private static final String LOGIC_TABLE_NAME = "picture";
//
//    private static final String DATABASE_NAME = "logic_db"; // 配置文件中的数据库名称
//
//    @PostConstruct
//    //这个注解将在这个类被加载后，直接执行注释方法
//    public void initialize() {
//        log.info("初始化动态分表配置...");
//        updateShardingTableNodes();
//    }
//
//    /**
//     * 获取所有动态表名，包括初始表 picture 和分表 picture_{spaceId}
//     */
//    private Set<String> fetchAllPictureTableNames() {
//        // 为了测试方便，直接对所有团队空间分表（实际上线改为仅对旗舰版生效）
//        Set<Long> spaceIds = spaceService.lambdaQuery()
//                .eq(Space::getSpaceType, SpaceTypeEnum.TEAM.getValue())
//                .list()
//                .stream()
//                .map(Space::getId)
//                .collect(Collectors.toSet());
//        Set<String> tableNames = spaceIds.stream()
//                .map(spaceId -> LOGIC_TABLE_NAME + "_" + spaceId)
//                .collect(Collectors.toSet());
//        tableNames.add(LOGIC_TABLE_NAME); // 添加初始逻辑表
//        return tableNames;
//    }
//
//    /**
//     * 更新 ShardingSphere 的 actual-data-nodes 动态表名配置
//     */
//    private void updateShardingTableNodes() {
//        //获取到可用的分表数据
//        Set<String> tableNames = fetchAllPictureTableNames();
//        String newActualDataNodes = tableNames.stream()
//                .map(tableName -> "cqm_picture." + tableName) // 确保前缀合法
//                .collect(Collectors.joining(","));
//        log.info("动态分表 actual-data-nodes 配置: {}", newActualDataNodes);
//
//        ContextManager contextManager = getContextManager();
//        ShardingSphereRuleMetaData ruleMetaData = contextManager.getMetaDataContexts()
//                .getMetaData()
//                .getDatabases()
//                .get(DATABASE_NAME)
//                .getRuleMetaData();
//
//        Optional<ShardingRule> shardingRule = ruleMetaData.findSingleRule(ShardingRule.class);
//        if (shardingRule.isPresent()) {
//            ShardingRuleConfiguration ruleConfig = (ShardingRuleConfiguration) shardingRule.get().getConfiguration();
//            List<ShardingTableRuleConfiguration> updatedRules = ruleConfig.getTables()
//                    .stream()
//                    .map(oldTableRule -> {
//                        if (LOGIC_TABLE_NAME.equals(oldTableRule.getLogicTable())) {
//                            ShardingTableRuleConfiguration newTableRuleConfig = new ShardingTableRuleConfiguration(LOGIC_TABLE_NAME, newActualDataNodes);
//                            newTableRuleConfig.setDatabaseShardingStrategy(oldTableRule.getDatabaseShardingStrategy());
//                            newTableRuleConfig.setTableShardingStrategy(oldTableRule.getTableShardingStrategy());
//                            newTableRuleConfig.setKeyGenerateStrategy(oldTableRule.getKeyGenerateStrategy());
//                            newTableRuleConfig.setAuditStrategy(oldTableRule.getAuditStrategy());
//                            return newTableRuleConfig;
//                        }
//                        return oldTableRule;
//                    })
//                    .collect(Collectors.toList());
//            ruleConfig.setTables(updatedRules);
//            contextManager.alterRuleConfiguration(DATABASE_NAME, Collections.singleton(ruleConfig));
//            contextManager.reloadDatabase(DATABASE_NAME);
//            log.info("动态分表规则更新成功！");
//        } else {
//            log.error("未找到 ShardingSphere 的分片规则配置，动态分表更新失败。");
//        }
//    }
//
//    /**
//     * 动态创建分表
//     * 在分表管理器中新增动态创建分表的方法，通过拼接 SQL 的方式创建出和 picture 表结构一样的分表，创建新的分表后记得更新分表节点
//     * @param space
//     */
//    public void createSpacePictureTable(Space space) {
//        // 动态创建分表
//        // 仅为旗舰版团队空间创建分表
//        if (space.getSpaceType() == SpaceTypeEnum.TEAM.getValue() && space.getSpaceLevel() == SpaceLevelEnum.FLAGSHIP.getValue()) {
//            Long spaceId = space.getId();
//            String tableName = "picture_" + spaceId;
//            // 创建新表
//            String createTableSql = "CREATE TABLE " + tableName + " LIKE picture";
//            try {
//                SqlRunner.db().update(createTableSql);
//                // 更新分表
//                updateShardingTableNodes();
//            } catch (Exception e) {
//                log.error("创建图片空间分表失败，空间 id = {}", space.getId());
//            }
//        }
//    }
//
//
//
//
//    /**
//     * 获取 ShardingSphere ContextManager
//     */
//    private ContextManager getContextManager() {
//        try (ShardingSphereConnection connection = dataSource.getConnection().unwrap(ShardingSphereConnection.class)) {
//            return connection.getContextManager();
//        } catch (SQLException e) {
//            throw new RuntimeException("获取 ShardingSphere ContextManager 失败", e);
//        }
//    }
//}
