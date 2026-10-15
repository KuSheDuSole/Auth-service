package ru.kubsu.dekanat.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer("postgres:17.5")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("db/init/01-auth-schema.sh", 0755),
                        "/docker-entrypoint-initdb.d/01-auth-schema.sh")
                .withEnv("AUTH_SVC_PASSWORD", "auth_svc");
    }

}
