package de.tolstun.database.record

import com.fasterxml.jackson.databind.ObjectMapper
import de.tolstun.database.model.Employee
import de.tolstun.database.table.EmployeeTable.Companion.EMPLOYEE
import org.jooq.Record


object EmployeeRecordBehavior {


    private val mapper = ObjectMapper()


    fun Record.toEmployee(): Employee {

        val employeeEmail: String? =
            try { this.getValue(EMPLOYEE.EMPLOYEE_EMAIL) }
            catch (e: Exception) { null }

        val employeeLastName: String? =
            try { this.getValue(EMPLOYEE.EMPLOYEE_LAST_NAME) }
            catch (e: Exception) { null }

        val employeeFirstName: String? =
            try { this.getValue(EMPLOYEE.EMPLOYEE_FIRST_NAME) }
            catch (e: Exception) { null }

        return Employee(
            email = employeeEmail,
            firstName = employeeLastName,
            lastName = employeeFirstName)
    }


}

