package de.tolstun.database.table

import de.tolstun.database.record.EmployeeRecord
import org.jooq.TableField
import org.jooq.impl.CustomTable
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType


class EmployeeTable : CustomTable<EmployeeRecord>(DSL.name("EMPLOYEE")) {


    val EMPLOYEE_EMAIL: TableField<EmployeeRecord, String> =
        createField<String>(DSL.name("EMAIL"), SQLDataType.VARCHAR(255).identity(true))

    val EMPLOYEE_FIRST_NAME: TableField<EmployeeRecord, String> =
        createField<String>(DSL.name("FIRST_NAME"), SQLDataType.VARCHAR(255))

    val EMPLOYEE_LAST_NAME: TableField<EmployeeRecord, String> =
        createField<String>(DSL.name("LAST_NAME"), SQLDataType.VARCHAR(255))


    override fun getRecordType(): Class<out EmployeeRecord> {
        return EmployeeRecord::class.java
    }


    companion object {
        val EMPLOYEE: EmployeeTable = EmployeeTable()
    }


}

