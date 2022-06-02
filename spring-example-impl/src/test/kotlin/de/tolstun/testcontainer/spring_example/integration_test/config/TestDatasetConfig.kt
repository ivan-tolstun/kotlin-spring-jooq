package de.tolstun.testcontainer.spring_example.integration_test.config


enum class DbDatasetDirectory(val paths: List<String>) {

    DATASET_V1(listOf(
        "db/dataset/01/V1__insert__employee.sql",
    ))

}


enum class DbMigrationDirectory(val paths: List<String>) {

    MIGRATION_1(listOf(
        "db/migration/01/V1__create__employee.sql",
    )),

    ROLL_BACK_MIGRATION(listOf(
        "db/migration/roll_back/R__drop__all.sql"
    ))
}