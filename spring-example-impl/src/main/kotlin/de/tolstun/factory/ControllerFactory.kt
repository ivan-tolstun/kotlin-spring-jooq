package de.tolstun.factory

import de.tolstun.database.EmployeeDataAccessService
import de.tolstun.rest.controller.EmployeeControllerImpl
import de.tolstun.testcontainer.rest.controller.EmployeeController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
open class ControllerFactory {


    @Bean
    open fun employeeController(employeeDataAccessService: EmployeeDataAccessService): EmployeeController {

        return EmployeeControllerImpl(employeeDataAccessService)
    }


}