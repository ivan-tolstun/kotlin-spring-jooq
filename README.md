# Docker test containers (extension version)

This is an extension to the "testcontainer" library for use in tests. 
Implemented ready-made containers with advanced functions.

## Use container templates

### PostgreSQL container:
```kotlin
@Rule
val postgreSQLContainer = DmPostgreSQLContainer(
        imageName = "postgres:11-alpine",
        internalContainerName = "db-itest",
        internalHost = "db",
        databaseName = "database",
        userName = "manager",
        password = "manager",
        environmentVariables = mutableMapOf("TZ" to "Europe/Berlin"))
```

### Kafka containers:
```kotlin
@Rule
val kafkaContainer = DmKafkaContainer(
        internalContainerName = "kafka-itest")

@Rule
val zooKeeperContainer = DmZooKeeperContainer(
    internalContainerName = "zooKeeper-itest")
```

### App container from jar with DB and Kafka dependency:
```kotlin
@Rule
val deviceContainer = DmRestApiContainerFromJar(
        jarFileDirectory = """./path_to_jar""",
        internalContainerName = "device-itest",
        internalDomain = "dm-device",
        internalHttpPort = 8080,
        jwtKey = JWT_KEY,
        environmentVariables = mutableMapOf(
            "DATABASE" to postgreSQLContainer.databaseName,
            "DB_USER" to postgreSQLContainer.userName,
            "DB_PASSWORD" to postgreSQLContainer.password,
            "DB_URL" to postgreSQLContainer.internalHost,
            "DB_PORT" to postgreSQLContainer.internalPort.toString(),
            "EXCEET_SECURITY_JWT_KEY" to JWT_KEY,
            "KAFKA_BOOTSTRAP_SERVERS" to "${kafkaContainer.internalHost}:${kafkaContainer.internalPort}"
        ),
        internalHttpsPort = null,
        _waitStrategy = Wait.forHttp("/actuator/health").withReadTimeout(Duration.ofSeconds(25))
    )
```


### App container from image with DB and Kafka dependency:
```kotlin
@Rule
val deviceContainer = DmRestApiContainer(
        imageName = """image_url""",
        internalContainerName = "service-name-itest",
        internalDomain = "dm-service-name",
        internalHttpPort = 8080,
        jwtKey = JWT_KEY,
        environmentVariables = mutableMapOf(
            "DATABASE" to postgreSQLContainer.databaseName,
            "DB_USER" to postgreSQLContainer.userName,
            "DB_PASSWORD" to postgreSQLContainer.password,
            "DB_URL" to postgreSQLContainer.internalHost,
            "DB_PORT" to postgreSQLContainer.internalPort.toString(),
            "EXCEET_SECURITY_JWT_KEY" to JWT_KEY,
            "KAFKA_BOOTSTRAP_SERVERS" to "${kafkaContainer.internalHost}:${kafkaContainer.internalPort}"
        ),
        internalHttpsPort = null,
        _waitStrategy = Wait.forHttp("/actuator/health").withReadTimeout(Duration.ofSeconds(25))
    )
```


## Autotest with rest-api containers
```kotlin
postgreSQLContainer.runMigrationScripts(
    "db/dataset/R__1_insert_customer_data.sql",
    "db/dataset/R__2_insert_hardware_data.sql",
    "db/dataset/R__3_insert_device_data.sql")

deviceContainer.runYamlTests("auto-test/device.yaml")
```
```yaml
restApiTests:
  # --------------------------------------------------------------------------
  - testName: 'test on successful'
    method: 'GET'
    path: '/api/customer/1/device'
    dmJwtToken:
        userCustomerId: 1
        userType: 'EMPLOYEE'
        userPermissions:
          - 'device_create'
          - 'device_read'
          - 'device_update'
          - 'device_delete'
    responseStatusCode: 200
    responseBody: '{"limit":65535,"page":1,"pagecount":1,"total":3,"data":[{"id":1,"serial":"12345678900000000000","description":"description_1","customerId":1,"appBundleName":null,"appBundleVersion":null,"osName":null,"osVersion":null,"hardwareType":{"id":1,"name":"type_1","description":"description_1"},"state":{},"stateDate":null,"active":true},{"id":5,"serial":"12345678900000000004","description":"description_5","customerId":1,"appBundleName":null,"appBundleVersion":null,"osName":null,"osVersion":null,"hardwareType":{"id":1,"name":"type_1","description":"description_1"},"state":{},"stateDate":null,"active":true},{"id":6,"serial":"12345678900000000005","description":"description_6","customerId":1,"appBundleName":null,"appBundleVersion":null,"osName":null,"osVersion":null,"hardwareType":{"id":1,"name":"type_1","description":"description_1"},"state":{},"stateDate":null,"active":true}]}'
    variablesToSave:
      - pathInJson: 'pagecount'
        saveAs: 'numberOfDevices'
      - pathInJson: 'page'
        saveAs: 'pageNumber'
      - pathInJson: 'data[0].customerId'
        saveAs: 'device_0_customerId'
  # --------------------------------------------------------------------------
  - testName: 'test to use the stored value'
    method: 'GET'
    path: '/api/customer/{{device_0_customerId}}/device'
    dmJwtToken:
        userCustomerId: 1
        userType: 'EMPLOYEE'
        userPermissions:
          - 'device_create'
          - 'device_read'
          - 'device_update'
          - 'device_delete'
    responseStatusCode: 200
    responseBody: '{"limit":65535,"page":{{pageNumber}},"pagecount":{{numberOfDevices}},"total":3,"data":[{"id":1,"serial":"12345678900000000000","description":"description_1","customerId":1,"appBundleName":null,"appBundleVersion":null,"osName":null,"osVersion":null,"hardwareType":{"id":1,"name":"type_1","description":"description_1"},"state":{},"stateDate":null,"active":true},{"id":5,"serial":"12345678900000000004","description":"description_5","customerId":1,"appBundleName":null,"appBundleVersion":null,"osName":null,"osVersion":null,"hardwareType":{"id":1,"name":"type_1","description":"description_1"},"state":{},"stateDate":null,"active":true},{"id":6,"serial":"12345678900000000005","description":"description_6","customerId":1,"appBundleName":null,"appBundleVersion":null,"osName":null,"osVersion":null,"hardwareType":{"id":1,"name":"type_1","description":"description_1"},"state":{},"stateDate":null,"active":true}]}'
```




## Maven

* <em style="color:#ffc;">Don't run the test:</em> ``````-Dmaven.test.skip=true``````
* <em style="color:#ffc;">The micronaut framework adds a reflection configuration file:</em> ``````-Dpackaging=docker-native``````

#### Build project
```console
ivan@68rus:~$ ./mvnw clean install -Dpackaging=docker-native -Dmaven.test.skip=true
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



