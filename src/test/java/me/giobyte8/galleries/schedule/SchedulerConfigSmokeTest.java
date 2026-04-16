package me.giobyte8.galleries.schedule;

import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.spring.autoconfigure.JobRunrAutoConfiguration;
import org.jobrunr.spring.autoconfigure.storage.JobRunrSqlStorageAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SchedulerConfigSmokeTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner()
                    .withConfiguration(
                            AutoConfigurations.of(
                                    JobRunrSqlStorageAutoConfiguration.class,
                                    JobRunrAutoConfiguration.class
                            )
                    )
                    .withUserConfiguration(SchedulerConfig.class);

    @Test
    void bootsJobRunrWithDedicatedSchedulerDatasource(@TempDir Path tmpDir) {
        String dbPath = tmpDir.resolve("scheduler-smoke.db").toString();

        contextRunner
                .withPropertyValues(
                        "jobrunr.database.type=sql",
                        "jobrunr.database.datasource=schedulerDatasource",
                        "jobrunr.background-job-server.enabled=false",
                        "jobrunr.dashboard.enabled=false",
                        "galleries.scheduler.datasource.jdbc-url="
                                + "jdbc:sqlite:" + dbPath,
                        "galleries.scheduler.datasource.driver-class-name="
                                + "org.sqlite.JDBC"
                )
                .run(context -> {
                    assertThat(context.getStartupFailure()).isNull();
                    assertThat(context.containsBean("schedulerDatasource"))
                            .isTrue();

                    DataSource schedulerDataSource = context.getBean(
                            "schedulerDatasource",
                            DataSource.class
                    );
                    assertThat(schedulerDataSource)
                            .isInstanceOf(SQLiteDataSource.class);
                    assertThat(((SQLiteDataSource) schedulerDataSource).getUrl())
                            .isEqualTo("jdbc:sqlite:" + dbPath);

                    JobScheduler jobScheduler =
                            context.getBean(JobScheduler.class);
                    assertThat(jobScheduler).isNotNull();
                });
    }
}


