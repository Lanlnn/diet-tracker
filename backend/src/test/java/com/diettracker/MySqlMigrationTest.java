package com.diettracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("mysql-test")
@EnabledIfEnvironmentVariable(named = "MYSQL_INTEGRATION_TEST", matches = "true")
class MySqlMigrationTest {
    @Test
    void flywayMigrationsBuildAValidMySql8Schema() {
    }
}
