package com.diettracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mysql-test")
@EnabledIfEnvironmentVariable(named = "MYSQL_INTEGRATION_TEST", matches = "true")
class MySqlMigrationTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayMigrationsBuildAValidMySql8Schema() {
        Integer migrationVersion = jdbcTemplate.queryForObject(
                "SELECT MAX(CAST(version AS UNSIGNED)) FROM flyway_schema_history WHERE success = 1",
                Integer.class);
        Integer categoryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM food_category", Integer.class);
        Integer systemFoodCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM food_item WHERE user_id IS NULL", Integer.class);
        Integer chickenCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM food_item WHERE name = '鸡胸肉' AND user_id IS NULL",
                Integer.class);

        assertThat(migrationVersion).isEqualTo(7);
        assertThat(categoryCount).isGreaterThanOrEqualTo(7);
        assertThat(systemFoodCount).isGreaterThanOrEqualTo(48);
        assertThat(chickenCount).isGreaterThanOrEqualTo(1);
    }
}
