package de.tolstun.rest.dto.behavior

import de.tolstun.database.model.Employee
import de.tolstun.testcontainer.rest.dto.EmployeeDto


object EmployeeDtoBehavior {


    fun Employee.toDto(): EmployeeDto =

        EmployeeDto(email = this.email,
            firstName = this.firstName,
            lastName = this.lastName)


}