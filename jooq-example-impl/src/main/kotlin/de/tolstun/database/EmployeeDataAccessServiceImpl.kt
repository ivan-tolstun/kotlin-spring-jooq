package de.tolstun.database

import de.tolstun.database.model.Employee
import de.tolstun.database.table.EmployeeTable
import de.tolstun.database.table.EmployeeTable.Companion.EMPLOYEE
import de.tolstun.database.query.EmployeeQuery
import de.tolstun.database.record.CommonRecordBehavior.firstToInteger
import de.tolstun.database.record.EmployeeRecordBehavior.toEmployee
import io.reactivex.rxjava3.core.Single
import org.jooq.DSLContext
import org.jooq.SortOrder
import org.jooq.TableField
import java.util.*


open class EmployeeDataAccessServiceImpl(val dslContext: DSLContext) : EmployeeDataAccessService {


    private val employeeQuery: EmployeeQuery = EmployeeQuery(dslContext)


    override fun findEmployee(employeeEmail: String,
                              selectFields: (EmployeeTable) -> List<TableField<*, *>>?,
                              notSelectFields: (EmployeeTable) -> List<TableField<*, *>>?): Single<Optional<Employee>> {

        val query = employeeQuery.buildQueryToGetEmployees(
            employeeEmails = listOfNotNull(employeeEmail),
            limit = 1,
            selectFields = selectFields(EMPLOYEE) ?: emptyList(),
            notSelectFields = notSelectFields(EMPLOYEE) ?: emptyList())

        return Single.fromCallable {

            query
                .fetchOptional()
                .map { it.toEmployee() }
        }
    }


    override fun findEmployees(employeeEmails: List<String>?,
                               notEmployeeEmails: List<String>?,
                               employeeLastNames: List<String>?,
                               notEmployeeLastNames: List<String>?,
                               employeeFirstNames: List<String>?,
                               notEmployeeFirstNames: List<String>?,
                               sorting: (EmployeeTable) ->  List<Pair<TableField<*, *>, SortOrder>>?,
                               offset: Int?,
                               limit: Int?,
                               selectFields: (EmployeeTable) -> List<TableField<*, *>>?,
                               notSelectFields: (EmployeeTable) -> List<TableField<*, *>>?): Single<List<Employee>> {

        val query = employeeQuery.buildQueryToGetEmployees(
            employeeEmails = employeeEmails ?: emptyList(),
            notEmployeeEmails = notEmployeeEmails ?: emptyList(),
            employeeLastNames = employeeLastNames ?: emptyList(),
            notEmployeeLastNames = notEmployeeLastNames ?: emptyList(),
            employeeFirstNames = employeeFirstNames ?: emptyList(),
            notEmployeeFirstNames = notEmployeeFirstNames ?: emptyList(),
            sorting = sorting(EMPLOYEE) ?: emptyList(),
            offset = offset,
            limit = limit,
            selectFields = selectFields(EMPLOYEE) ?: emptyList(),
            notSelectFields = notSelectFields(EMPLOYEE) ?: emptyList())

        return Single.fromCallable {

            query
                .fetch()
                .map { it.toEmployee() }
                .filterNotNull()
        }
    }


    override fun countEmployees(employeeEmails: List<String>?,
                                notEmployeeEmails: List<String>?,
                                employeeLastNames: List<String>?,
                                notEmployeeLastNames: List<String>?,
                                employeeFirstNames: List<String>?,
                                notEmployeeFirstNames: List<String>?): Single<Optional<Int>> {

        val query = employeeQuery.buildQueryToCountEmployees(
            employeeEmails = employeeEmails ?: emptyList(),
            notEmployeeEmails = notEmployeeEmails ?: emptyList(),
            employeeLastNames = employeeLastNames ?: emptyList(),
            notEmployeeLastNames = notEmployeeLastNames ?: emptyList(),
            employeeFirstNames = employeeFirstNames ?: emptyList(),
            notEmployeeFirstNames = notEmployeeFirstNames ?: emptyList())

        return Single.fromCallable {

            query
                .fetchOptional()
                .map { it.firstToInteger() }
        }
    }


    override fun createEmployee(newEmployeeEmail: String,
                                newEmployeeLastName: String?,
                                newEmployeeFirstName: String?,
                                selectFields: (EmployeeTable) -> List<TableField<*, *>>?,
                                notSelectFields: (EmployeeTable) -> List<TableField<*, *>>?): Single<Optional<Employee>> {

        val query = employeeQuery.buildQueryToInsertEmployee(
            newEmployeeEmail = newEmployeeEmail,
            newEmployeeLastName = newEmployeeLastName,
            newEmployeeFirstName = newEmployeeFirstName)

        return Single
            .fromCallable { query.execute() }
            .flatMap { result ->

                when {

                    result > 0 -> findEmployee(
                        employeeEmail = newEmployeeEmail,
                        selectFields = selectFields,
                        notSelectFields = notSelectFields)

                    else -> Single.just(Optional.empty<Employee>())
                }
            }
    }


    override fun updateEmployee(employeeEmail: String,
                                newEmployeeLastName: String?,
                                newEmployeeFirstName: String?,
                                selectFields: (EmployeeTable) -> List<TableField<*, *>>?,
                                notSelectFields: (EmployeeTable) -> List<TableField<*, *>>?): Single<Optional<Employee>> {

        val query = employeeQuery.buildQueryToUpdateEmployee(
            employeeEmail = employeeEmail,
            newEmployeeLastName = newEmployeeLastName,
            newEmployeeFirstName = newEmployeeFirstName)

        return Single
            .fromCallable { query.execute() }
            .flatMap { result ->

                when {

                    result > 0 -> findEmployee(
                        employeeEmail = employeeEmail,
                        selectFields = selectFields,
                        notSelectFields = notSelectFields)

                    else -> Single.just(Optional.empty<Employee>())
                }
            }
    }


}