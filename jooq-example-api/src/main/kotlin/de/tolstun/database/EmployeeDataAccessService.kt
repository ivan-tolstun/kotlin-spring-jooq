package de.tolstun.database

import de.tolstun.database.model.Employee
import de.tolstun.database.table.EmployeeTable
import io.reactivex.rxjava3.core.Single
import org.jooq.SortOrder
import org.jooq.TableField
import java.util.*


interface EmployeeDataAccessService {


    fun findEmployee(employeeEmail: String,
                     selectFields: (EmployeeTable) -> List<TableField<*, *>>? = { emptyList() },
                     notSelectFields: (EmployeeTable) -> List<TableField<*, *>>? = { emptyList() }): Single<Optional<Employee>>


    fun findEmployees(employeeEmails: List<String>? = emptyList(),
                      notEmployeeEmails: List<String>? = emptyList(),
                      employeeLastNames: List<String>? = emptyList(),
                      notEmployeeLastNames: List<String>? = emptyList(),
                      employeeFirstNames: List<String>? = emptyList(),
                      notEmployeeFirstNames: List<String>? = emptyList(),
                      sorting: (EmployeeTable) -> List<Pair<TableField<*, *>, SortOrder>>? = { emptyList() },
                      offset: Int? = null,
                      limit: Int? = null,
                      selectFields: (EmployeeTable) -> List<TableField<*, *>>? = { emptyList() },
                      notSelectFields: (EmployeeTable) -> List<TableField<*, *>>? = { emptyList() }): Single<List<Employee>>


    fun countEmployees(employeeEmails: List<String>? = emptyList(),
                       notEmployeeEmails: List<String>? = emptyList(),
                       employeeLastNames: List<String>? = emptyList(),
                       notEmployeeLastNames: List<String>? = emptyList(),
                       employeeFirstNames: List<String>? = emptyList(),
                       notEmployeeFirstNames: List<String>? = emptyList()): Single<Optional<Int>>


    fun createEmployee(newEmployeeEmail: String,
                       newEmployeeLastName: String? = null,
                       newEmployeeFirstName: String? = null,
                       selectFields: (EmployeeTable) -> List<TableField<*, *>>?= { emptyList() },
                       notSelectFields: (EmployeeTable) -> List<TableField<*, *>>?= { emptyList() }): Single<Optional<Employee>>


    fun updateEmployee(employeeEmail: String,
                       newEmployeeLastName: String? = null,
                       newEmployeeFirstName: String? = null,
                       selectFields: (EmployeeTable) -> List<TableField<*, *>>?= { emptyList() },
                       notSelectFields: (EmployeeTable) -> List<TableField<*, *>>?= { emptyList() }): Single<Optional<Employee>>


}