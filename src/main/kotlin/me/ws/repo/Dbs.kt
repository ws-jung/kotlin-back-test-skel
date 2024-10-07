package me.ws.repo

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotliquery.Session
import kotliquery.sessionOf

interface BaseDao {
    companion object {
        fun getSession(): Session {
            val hikariConfig = HikariConfig()
            hikariConfig.driverClassName = "org.postgresql.Driver"
            hikariConfig.jdbcUrl = "jdbc:postgresql://localhost:15432/stock"
            hikariConfig.username = "admin"
            hikariConfig.password = "admin"
            hikariConfig.maximumPoolSize = 5
            hikariConfig.connectionTestQuery = "SELECT 1"
            hikariConfig.poolName = "springHikariCP"

            hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true")
            hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250")
            hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048")
            hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true")

            return sessionOf(HikariDataSource(hikariConfig));
        }
    }
}