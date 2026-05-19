package com.nhb.common.id.service;

import com.nhb.common.core.utils.SpringContextUtil;
import com.nhb.common.id.core.WorkerNodeRepository;
import com.nhb.common.id.entity.WorkerNode;
import com.nhb.common.id.exception.IdGeneratorException;
import com.nhb.common.id.properties.IdGeneratorConfigProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;

/**
 * @author luck_nhb
 * @version 1.0
 * @date 2026/5/19 13:50
 * @description: 使用数据库分配workerId
 */
@Slf4j
@RequiredArgsConstructor
public class DbWorkerNodeRepository implements WorkerNodeRepository {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    /**
     * 检查表是否存在的 SQL（兼容 MySQL 5.7+ 和 8.0+）
     */
    private static final String CHECK_TABLE_EXISTS_SQL =
            "SELECT COUNT(*) FROM information_schema.TABLES " +
                    "WHERE TABLE_SCHEMA = ? AND TABLE_NAME = 'WORKER_NODE'";

    /**
     * 建表 SQL
     */
    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE IF NOT EXISTS `WORKER_NODE` (" +
                    "  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '自增主键，同时也是 WorkerId'," +
                    "  `host_name` VARCHAR(64) NOT NULL COMMENT '主机名（K8S 下为 Pod 名称）'," +
                    "  `port` VARCHAR(64) NOT NULL COMMENT '端口号'," +
                    "  `launch_time` DATETIME NOT NULL COMMENT '启动日期'," +
                    "  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间（心跳时间）'," +
                    "  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'," +
                    "  PRIMARY KEY (`ID`)," +
                    "  UNIQUE KEY `UK_HOST_PORT` (`HOST_NAME`, `PORT`)," +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci " +
                    "  COMMENT='WorkerNode 分配表 - 用于 IdGenerator 的 WorkerId 分配'";

    /**
     * 根据主机和端口查询 WorkerNode
     */
    private static final String GET_WORKER_NODE_BY_HOST_PORT_SQL =
            "SELECT id, host_name, port, launch_time, update_time, create_time " +
                    "FROM WORKER_NODE " +
                    "WHERE HOST_NAME = ? AND PORT = ?";

    /**
     * 插入新的 WorkerNode 记录
     */
    private static final String ADD_WORKER_NODE_SQL =
            "INSERT INTO WORKER_NODE (host_name, port, launch_time, update_time, create_time) " +
                    "VALUES (?, ?, ?, NOW(), NOW())";

    /**
     * 更新 WorkerNode 的 MODIFIED 时间（心跳续期）
     */
    private static final String UPDATE_WORKER_NODE_HEARTBEAT_SQL =
            "UPDATE WORKER_NODE SET update_time = NOW() WHERE ID = ?";

    /**
     * 最大重试次数
     */
    private static final int MAX_RETRY_TIMES = 3;

    /**
     * 重试间隔（毫秒）
     */
    private static final long RETRY_INTERVAL_MS = 100;
    /**
     * 数据库名
     */
    private String databaseName;

    public void init() {
        try {
            databaseName = extractDatabaseName(dataSource);
            if (!isTableExists()) {
                log.warn("WORKER_NODE The table does not exist and is being created automatically...");
                createTable();
                log.info("WORKER_NODE The table was successfully created");
            } else {
                log.info("WORKER_NODE the table already exists skip creating the table");
            }
        } catch (Exception e) {
            log.error("Initialization of the WORKER_NODE table failed", e);
            throw new IdGeneratorException("Initialization of the WORKER_NODE table failed", e);
        }
    }

    /**
     * 检查 WORKER_NODE 表是否存在
     *
     * @return true=存在，false=不存在
     */
    private boolean isTableExists() {
        try {
            Integer count = this.jdbcTemplate.queryForObject(
                    CHECK_TABLE_EXISTS_SQL,
                    Integer.class,
                    databaseName
            );
            return count != null && count > 0;
        } catch (DataAccessException e) {
            log.error("An exception occurs when the checklist is present", e);
            // 如果查询 information_schema 失败，尝试直接查询表
            return checkTableByQuery();
        }
    }

    /**
     * 通过直接查询表来检查表是否存在（降级方案）
     */
    private boolean checkTableByQuery() {
        try {
            this.jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM WORKER_NODE WHERE 1=0",
                    Integer.class
            );
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    /**
     * 创建 WORKER_NODE 表
     */
    private void createTable() {
        try {
            this.jdbcTemplate.execute(CREATE_TABLE_SQL);
            log.info("WORKER_NODE the table has been created");
        } catch (DataAccessException e) {
            log.error("Failed to create WORKER_NODE table", e);
            throw new IdGeneratorException("Failed to create WORKER_NODE table", e);
        }
    }

    /**
     * 从数据源中提取数据库名称
     */
    private String extractDatabaseName(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            String url = conn.getMetaData().getURL();
            // 从 JDBC URL 中提取数据库名
            // 例如：jdbc:mysql://localhost:3306/id_generator
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash > 0) {
                String dbName = url.substring(lastSlash + 1);
                // 去掉可能的参数部分
                int questionMark = dbName.indexOf('?');
                if (questionMark > 0) {
                    dbName = dbName.substring(0, questionMark);
                }
                return dbName;
            }
        } catch (SQLException e) {
            log.warn("I can't extract the database name from the data source, use the default value", e);
            throw new IdGeneratorException("I can't extract the database name from the data source", e);
        }
        return null;
    }

    @Override
    public WorkerNode getWorkerNodeByHostPort(String host, String port) {
        try {
            WorkerNode entity = this.jdbcTemplate.queryForObject(
                    GET_WORKER_NODE_BY_HOST_PORT_SQL,
                    new WorkerNodeRowMapper(),
                    host, port
            );
            if (entity != null) {
                log.info("Find WorkerNode: id={}, host={}, port={}", entity.getId(), host, port);
                return entity;
            }
        } catch (EmptyResultDataAccessException e) {
            log.debug("Not find WorkerNode: host={}, port={}", host, port);
        } catch (DataAccessException e) {
            log.error("Query WorkerNode fail: host={}, port={}", host, port, e);
            throw new IdGeneratorException("Query WorkerNode fail", e);
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addWorkerNode(WorkerNode entity) {
        // 先检查是否已存在（幂等性处理）
        WorkerNode workerNode = getWorkerNodeByHostPort(entity.getHostName(), entity.getPort());
        if (null != workerNode) {
            renewHeartbeat(workerNode.getId());
            log.info("Reuse existing ones WorkerNode: id={}, host={}, port={}", workerNode.getId(), entity.getHostName(), entity.getPort());
        }
        IdGeneratorConfigProperties idGeneratorConfigProperties = SpringContextUtil.getBean(IdGeneratorConfigProperties.class);
        // 插入新记录（带重试）
        for (int retry = 0; retry < MAX_RETRY_TIMES; retry++) {
            try {
                KeyHolder keyHolder = new GeneratedKeyHolder();
                int affectedRows = this.jdbcTemplate.update(
                        connection -> {
                            PreparedStatement ps = connection.prepareStatement(ADD_WORKER_NODE_SQL, Statement.RETURN_GENERATED_KEYS);
                            ps.setString(1, entity.getHostName());
                            ps.setString(2, entity.getPort());
                            ps.setObject(3, entity.getLaunchTime());
                            return ps;
                        },
                        keyHolder
                );
                if (affectedRows > 0 && keyHolder.getKey() != null) {
                    long workerId = keyHolder.getKey().longValue();
                    //进行循环使用
                    workerId = workerId << idGeneratorConfigProperties.getWorkerIdBits();
                    entity.setId(workerId);
                    log.info("Successful assignment WorkerNode: id={}, host={}, port={}", workerId, entity.getHostName(), entity.getPort());
                }
            } catch (DataAccessException e) {
                if (isDuplicateKeyException(e)) {
                    log.warn("WorkerNode Insert the conflict and try again {} : host={}, port={}", retry + 1, entity.getHostName(), entity.getPort());
                    if (retry < MAX_RETRY_TIMES - 1) {
                        try {
                            Thread.sleep(RETRY_INTERVAL_MS);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new IdGeneratorException("The retry was interrupted", ie);
                        }
                        continue;
                    }
                }
                log.error("INSERTION WorkerNode failure: host={}, port={}", entity.getHostName(), entity.getPort(), e);
                throw new IdGeneratorException("INSERTION WorkerNode failure", e);
            }
        }
        throw new IdGeneratorException("INSERT WorkerNode failed，retry " + MAX_RETRY_TIMES + " after that it still failed");
    }

    /**
     * 续期 WorkerNode 的心跳
     */
    private void renewHeartbeat(long workerId) {
        try {
            int affected = this.jdbcTemplate.update(UPDATE_WORKER_NODE_HEARTBEAT_SQL, workerId);
            if (affected > 0) {
                log.info("Renewal WorkerNode heartbeat: id={}", workerId);
            }
        } catch (DataAccessException e) {
            log.warn("Renewal WorkerNode heartbeat failure: id={}", workerId, e);
        }
    }

    /**
     * 检查是否是主键/唯一键冲突异常
     */
    private boolean isDuplicateKeyException(DataAccessException e) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException) {
            String sqlState = ((SQLException) cause).getSQLState();
            return "23000".equals(sqlState);
        }
        return false;
    }

    /**
     * WorkerNode 的行映射器
     */
    private static class WorkerNodeRowMapper implements RowMapper<WorkerNode> {
        @Override
        public WorkerNode mapRow(ResultSet rs, int rowNum) throws SQLException {
            WorkerNode entity = new WorkerNode();
            entity.setId(rs.getLong("id"));
            entity.setHostName(rs.getString("host_name"));
            entity.setPort(rs.getString("port"));
            LocalDateTime launchDate = rs.getObject("launch_time", LocalDateTime.class);
            if (launchDate != null) {
                entity.setLaunchTime(launchDate);
            }
            LocalDateTime updateTime = rs.getObject("update_time", LocalDateTime.class);
            if (updateTime != null) {
                entity.setUpdateTime(updateTime);
            }
            LocalDateTime createTime = rs.getObject("create_time", LocalDateTime.class);
            if (createTime != null) {
                entity.setCreateTime(createTime);
            }
            return entity;
        }
    }
}
