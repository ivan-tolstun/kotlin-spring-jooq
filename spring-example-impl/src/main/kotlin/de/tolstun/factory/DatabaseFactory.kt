package de.tolstun.factory

import de.tolstun.database.EmployeeDataAccessService
import de.tolstun.database.EmployeeDataAccessServiceImpl
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DataSourceConnectionProvider
import org.jooq.impl.DefaultDSLContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import java.util.*
import javax.sql.DataSource


@Configuration
open class DatabaseFactory {


    @Bean
    open fun sqlDialect(environment: Environment): SQLDialect {

        val url = environment.getRequiredProperty("spring.datasource.url")

        return when {

            url.uppercase(Locale.getDefault()).contains(SQLDialect.H2.nameUC) -> SQLDialect.H2
            url.uppercase(Locale.getDefault()).contains(SQLDialect.POSTGRES.nameUC) -> SQLDialect.POSTGRES
            url.uppercase(Locale.getDefault()).contains(SQLDialect.MYSQL.nameUC) -> SQLDialect.MYSQL

            else -> SQLDialect.DEFAULT
        }
    }


    @Bean
    open fun connectionProvider(dataSource: DataSource): DataSourceConnectionProvider =

        DataSourceConnectionProvider(TransactionAwareDataSourceProxy(dataSource))


    @Bean
    open fun dsl(connectionProvider: DataSourceConnectionProvider,
                 sqlDialect: SQLDialect): DefaultDSLContext {

        val settings = Settings()
            .withRenderQuotedNames(RenderQuotedNames.NEVER)
            .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED)

        return DefaultDSLContext(connectionProvider, sqlDialect, settings)
    }


    @Bean
    open fun employeeDataAccessServiceImpl(dslContext: DSLContext): EmployeeDataAccessService? {

        return EmployeeDataAccessServiceImpl(dslContext)
    }


}