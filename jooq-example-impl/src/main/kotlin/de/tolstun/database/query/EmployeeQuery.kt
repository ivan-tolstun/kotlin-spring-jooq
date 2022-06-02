package de.tolstun.database.query

import de.tolstun.database.extension.jooq.SelectOnConditionStepExpansion.orderBy
import de.tolstun.database.record.EmployeeRecord
import de.tolstun.database.table.EmployeeTable.Companion.EMPLOYEE
import org.jooq.*


class EmployeeQuery(val dslContext: DSLContext) : QueryHelper {


    fun buildQueryToGetEmployees(employeeEmails: List<String> = emptyList(),
                                 notEmployeeEmails: List<String> = emptyList(),
                                 employeeLastNames: List<String> = emptyList(),
                                 notEmployeeLastNames: List<String> = emptyList(),
                                 employeeFirstNames: List<String> = emptyList(),
                                 notEmployeeFirstNames: List<String> = emptyList(),
                                 sorting: List<Pair<TableField<*, *>, SortOrder>> = emptyList(),
                                 offset: Int? = null,
                                 limit: Int? = null,
                                 selectFields: List<TableField<*, *>> = emptyList(),
                                 notSelectFields: List<TableField<*, *>> = emptyList()): SelectLimitPercentAfterOffsetStep<out Record> {

        val fieldsToSelect = this
            .findFieldsOrAsterisk(selectFields, EMPLOYEE)
            .filter { field -> !notSelectFields.contains(field) }

        return dslContext

            .select(fieldsToSelect)
            .from(EMPLOYEE)

            .where(inOrIgnoreEmpty(employeeEmails, EMPLOYEE.EMPLOYEE_EMAIL))
            .and(notInOrIgnoreEmpty(notEmployeeEmails, EMPLOYEE.EMPLOYEE_EMAIL))
            .and(inOrIgnoreEmpty(employeeLastNames, EMPLOYEE.EMPLOYEE_LAST_NAME))
            .and(notInOrIgnoreEmpty(notEmployeeLastNames, EMPLOYEE.EMPLOYEE_LAST_NAME))
            .and(inOrIgnoreEmpty(employeeFirstNames, EMPLOYEE.EMPLOYEE_FIRST_NAME))
            .and(notInOrIgnoreEmpty(notEmployeeFirstNames, EMPLOYEE.EMPLOYEE_FIRST_NAME))

            .orderBy(sorting, EMPLOYEE)
            .offset(offset ?: 0)
            .limit(limit ?: Int.MAX_VALUE)
    }



    fun buildQueryToCountEmployees(employeeEmails: List<String> = emptyList(),
                                   notEmployeeEmails: List<String> = emptyList(),
                                   employeeLastNames: List<String> = emptyList(),
                                   notEmployeeLastNames: List<String> = emptyList(),
                                   employeeFirstNames: List<String> = emptyList(),
                                   notEmployeeFirstNames: List<String> = emptyList()): SelectConditionStep<out Record> {

        return dslContext

            .selectCount()
            .from(EMPLOYEE)

            .where(inOrIgnoreEmpty(employeeEmails, EMPLOYEE.EMPLOYEE_EMAIL))
            .and(notInOrIgnoreEmpty(notEmployeeEmails, EMPLOYEE.EMPLOYEE_EMAIL))

            .and(inOrIgnoreEmpty(employeeLastNames, EMPLOYEE.EMPLOYEE_LAST_NAME))
            .and(notInOrIgnoreEmpty(notEmployeeLastNames, EMPLOYEE.EMPLOYEE_LAST_NAME))

            .and(inOrIgnoreEmpty(employeeFirstNames, EMPLOYEE.EMPLOYEE_FIRST_NAME))
            .and(notInOrIgnoreEmpty(notEmployeeFirstNames, EMPLOYEE.EMPLOYEE_FIRST_NAME))
    }


    fun buildQueryToInsertEmployee(newEmployeeEmail: String,
                                   newEmployeeLastName: String? = null,
                                   newEmployeeFirstName: String? = null): InsertReturningStep<EmployeeRecord> {

        val setMap = mapOf(
            EMPLOYEE.EMPLOYEE_EMAIL to newEmployeeEmail,
            EMPLOYEE.EMPLOYEE_FIRST_NAME to newEmployeeLastName,
            EMPLOYEE.EMPLOYEE_LAST_NAME to newEmployeeFirstName
        ).filter { it.value != null }

        return dslContext
            .insertInto(EMPLOYEE)
            .set(setMap)
            .onDuplicateKeyIgnore()
    }


    fun buildQueryToUpdateEmployee(employeeEmail: String,
                                   newEmployeeLastName: String? = null,
                                   newEmployeeFirstName: String? = null): UpdateConditionStep<EmployeeRecord> {

        val setMap = mapOf(
            EMPLOYEE.EMPLOYEE_LAST_NAME to newEmployeeLastName,
            EMPLOYEE.EMPLOYEE_FIRST_NAME to newEmployeeFirstName
        ).filter { it.value != null }

        return dslContext
            .update(EMPLOYEE)
            .set(setMap)
            .where(EMPLOYEE.EMPLOYEE_EMAIL.eq(employeeEmail))
    }


}