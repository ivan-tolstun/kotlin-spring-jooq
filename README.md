# Spring-boot with Jooq and Testcontainer


- In this project you will find an example of using the Jooq library together with Spring-boot.
- Also here is an example integration test using docker containers


## Data access with Jooq

```kotlin
dslContext
    .select(EMPLOYEE.EMPLOYEE_EMAIL, EMPLOYEE.EMPLOYEE_LAST_NAME)
    .from(EMPLOYEE)
    .where(EMPLOYEE.EMPLOYEE_EMAIL.`in`(listOf("user1@bla.de", "user2@bla.de", "user3@bla.de")))
    .orderBy(EMPLOYEE.EMPLOYEE_EMAIL.desc())
```


## Test with docker container 

```kotlin
@Rule
val employeeContainer: JavaRestApiContainer = JavaRestApiContainer(
        jarFile = File("""./../spring-example-impl/target/spring-example-impl-0.1.1.jar"""),
        internalContainerName = "employee--itest",
        internalDomain = "employee",
        internalHttpPort = 8080,
        jwtKey = JWT_KEY,
        environmentVariables = mutableMapOf(
            "DB_DATABASE" to postgreSQLContainer.databaseName,
            "DB_USER" to postgreSQLContainer.userName,
            "DB_PASSWORD" to postgreSQLContainer.password,
            "DB_URL" to postgreSQLContainer.internalDomain,
            "DB_PORT" to postgreSQLContainer.internalPort.toString(),
        ),
        internalHttpsPort = null,
        internalDebugPort = 5005,
        _waitStrategy = Wait.forHttp("/actuator/health").withReadTimeout(Duration.ofSeconds(25))
    )


@Test
fun `test GET employees with email field only`() {

    postgreSQLContainer.runMigrationScripts(
        *DbMigrationDirectory.ROLL_BACK_MIGRATION.paths.toTypedArray(),
        *DbMigrationDirectory.MIGRATION_1.paths.toTypedArray(),
        *DbDatasetDirectory.DATASET_V1.paths.toTypedArray()
    )

    employeeContainer.GET(
        path = "/api/employees",
        queries = mapOf(
            "sorting" to SortDto(field = "email", order = "desc"),
            "employeeEmails" to listOf("device.manager@exceet.de", "Roman.Rem@exceet.de"),
            "selectFields" to listOf("email")
        )
    )
        .then()
        .statusCode(200)
        .body(containsString(
            "{\"email\":\"device.manager@exceet.de\"}," +
                    "{\"email\":\"Roman.Rem@exceet.de\"}"
        ))

}
```


## Test with docker container and Yaml test file

```yaml
restApiTests:
  - testName: 'get employees with email field only'
    method: 'GET'
    path: '/api/employees'
    queries:
      selectFields:
        - 'email'
      employeeEmails:
        - 'device.manager@exceet.de'
        - 'Roman.Rem@exceet.de'
      sorting:
        field: 'email'
        order: 'desc'
    responseStatusCode: 200
    responseBody: '[{"email":"device.manager@exceet.de"},{"email":"Roman.Rem@exceet.de"}]'
```


## Maven

* <em style="color:#ffc;">Don't run the test:</em> ``````-Dmaven.test.skip=true``````

#### Build project
```console
ivan@68rus:~$ ./mvnw clean install -Dmaven.test.skip=true
```

#### Test project
```console
ivan@68rus:~$ ./mvnw test
```


#### Versioning

To release version - 1.2.3
``````console
ivan@68rus:~$  mvn build-helper:parse-version versions:set \
-DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.incrementalVersion}
``````

To next release version - 1.2.{next}
``````console
ivan@68rus:~$  mvn build-helper:parse-version versions:set \
-DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion}
``````

To next incremental snapshot version - 1.2.{next}-SNAPSHOT
``````console
ivan@68rus:~$  mvn build-helper:parse-version versions:set \
-DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.minorVersion}.\${parsedVersion.nextIncrementalVersion}-SNAPSHOT
``````

To next minor snapshot version - 1.{next}.0-SNAPSHOT
``````console
ivan@68rus:~$  mvn build-helper:parse-version versions:set \
-DnewVersion=\${parsedVersion.majorVersion}.\${parsedVersion.nextMinorVersion}.0-SNAPSHOT
``````

To next major snapshot version - {next}.0.0-SNAPSHOT
``````console
ivan@68rus:~$  mvn build-helper:parse-version versions:set \
-DnewVersion=\${parsedVersion.nextMajorVersion}.0.0-SNAPSHOT
``````



