package de.tolstun.rest.controller

import de.tolstun.database.EmployeeDataAccessService
import de.tolstun.database.table.EmployeeTable
import de.tolstun.rest.dto.behavior.EmployeeDtoBehavior.toDto
import de.tolstun.testcontainer.rest.controller.EmployeeController
import de.tolstun.testcontainer.rest.dto.EmployeeDto
import de.tolstun.testcontainer.rest.dto.SortDto
import io.reactivex.rxjava3.core.Single
import org.jooq.SortOrder
import java.util.*


class EmployeeControllerImpl(val employeeDataAccessService: EmployeeDataAccessService) : EmployeeController {


    override fun findEmployee(employeeEmail: String,
                              selectFields: List<String>?,
                              notSelectFields: List<String>?): Single<Optional<EmployeeDto>> =

        employeeDataAccessService
            .findEmployee(
                employeeEmail = employeeEmail,
                selectFields = selectEmployeeField(selectFields, notSelectFields))
            .map { optionalResult ->

                optionalResult.map { it.toDto() }
            }


    override fun findEmployees(employeeEmails: List<String>?,
                               notEmployeeEmails: List<String>?,
                               employeeFirstNames: List<String>?,
                               notEmployeeFirstNames: List<String>?,
                               employeeLastNames: List<String>?,
                               notEmployeeLastNames: List<String>?,
                               sorting: List<SortDto>?,
                               offset: Int?,
                               limit: Int?,
                               selectFields: List<String>?,
                               notSelectFields: List<String>?): Single<List<EmployeeDto>> =

        employeeDataAccessService
            .findEmployees(
                employeeEmails = employeeEmails,
                notEmployeeEmails = notEmployeeEmails,
                employeeFirstNames = employeeFirstNames,
                notEmployeeFirstNames = notEmployeeFirstNames,
                employeeLastNames = employeeLastNames,
                notEmployeeLastNames = notEmployeeLastNames,
                offset = offset,
                limit = limit,
                sorting = sortEmployees(sorting),
                selectFields = selectEmployeeField(selectFields, notSelectFields)
            )
            .map { optionalResult ->

                optionalResult.map { it.toDto() }
            }


    override fun countEmployees(employeeEmails: List<String>?,
                               notEmployeeEmails: List<String>?,
                               employeeFirstNames: List<String>?,
                               notEmployeeFirstNames: List<String>?,
                               employeeLastNames: List<String>?,
                               notEmployeeLastNames: List<String>?): Optional<Int> =

        employeeDataAccessService
            .countEmployees(
                employeeEmails = employeeEmails,
                notEmployeeEmails = notEmployeeEmails,
                employeeFirstNames = employeeFirstNames,
                notEmployeeFirstNames = notEmployeeFirstNames,
                employeeLastNames = employeeLastNames,
                notEmployeeLastNames = notEmployeeLastNames,
            )
            .blockingGet()


    private fun selectEmployeeField(selectFields: List<String>? = emptyList(),
                                    notSelectFields: List<String>? = emptyList()) = { employeeTable: EmployeeTable ->

        val fieldMapper = mapOf(
            "email" to employeeTable.EMPLOYEE_EMAIL,
            "firstName" to employeeTable.EMPLOYEE_FIRST_NAME,
            "lastName" to employeeTable.EMPLOYEE_LAST_NAME)

        fieldMapper
            .filterNot { (dtoName, _) -> notSelectFields?.contains(dtoName.lowercase()) ?: false }
            .filter { (dtoName, _) -> selectFields?.contains(dtoName.lowercase()) ?: false }
            .map { it.value }
    }


    private fun sortEmployees(sortConfDTOs: List<SortDto>?) = { employeeTable: EmployeeTable ->

        val fieldMapper = mapOf(
            "email" to employeeTable.EMPLOYEE_EMAIL,
            "firstName" to employeeTable.EMPLOYEE_FIRST_NAME,
            "lastName" to employeeTable.EMPLOYEE_LAST_NAME)

        sortConfDTOs
            ?.mapNotNull { sortConfDTO ->

                val originField = fieldMapper[sortConfDTO.field]
                val originOrder = SortOrder.values().find { it.name == sortConfDTO.order.uppercase() }

                if(originField != null && originOrder != null) originField to originOrder
                else null
            }
            ?: emptyList()
    }


}