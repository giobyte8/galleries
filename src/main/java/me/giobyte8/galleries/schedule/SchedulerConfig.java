package me.giobyte8.galleries.schedule;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.sqlite.SQLiteDataSource;

@Configuration
public class SchedulerConfig {

    @Bean(name = "schedulerDatasource")
    @ConfigurationProperties(prefix = "galleries.scheduler.datasource")
    public SchedulerSQLiteDataSource schedulerDatasource() {
        return new SchedulerSQLiteDataSource();
    }

    @SuppressWarnings("unused")
    public static class SchedulerSQLiteDataSource extends SQLiteDataSource {

        public void setJdbcUrl(String jdbcUrl) {
            setUrl(jdbcUrl);
        }

        public void setDriverClassName(String driverClassName) {
            // Kept for conventional datasource properties compatibility.
        }
    }
}

