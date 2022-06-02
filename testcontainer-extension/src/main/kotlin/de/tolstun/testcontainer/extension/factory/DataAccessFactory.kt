package de.tolstun.testcontainer.extension.factory

import org.jooq.SQLDialect
import org.jooq.conf.RenderNameCase
import org.jooq.conf.RenderQuotedNames
import org.jooq.conf.Settings
import org.jooq.impl.DefaultDSLContext
import java.sql.Connection
import java.sql.DriverManager


class DataAccessFactory(private val jdbcUrl: String,
                        private val userName: String,
                        private val password: String) {


    val dbConnection: Connection by lazy {
        DriverManager.getConnection(this.jdbcUrl, this.userName, this.password)
    }


    val jooqSettings: Settings by lazy {

        Settings()
            .withRenderQuotedNames(RenderQuotedNames.NEVER)
            .withRenderNameCase(RenderNameCase.LOWER_IF_UNQUOTED)
    }


    val defaultDSLContext: DefaultDSLContext by lazy {
        DefaultDSLContext(dbConnection, SQLDialect.POSTGRES, jooqSettings)
    }


}