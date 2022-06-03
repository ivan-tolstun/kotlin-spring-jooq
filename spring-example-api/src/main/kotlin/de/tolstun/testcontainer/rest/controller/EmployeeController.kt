package de.tolstun.testcontainer.rest.controller

import de.tolstun.testcontainer.rest.dto.EmployeeDto
import de.tolstun.testcontainer.rest.dto.SortDto
import io.reactivex.rxjava3.core.Single
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.Optional
import javax.websocket.server.PathParam


@RestController
@RequestMapping(path = ["/api"])
interface EmployeeController {


    @GetMapping(path = ["/employees/{employeeEmail}"], produces = ["application/json"])
    fun findEmployee(@PathParam("employeeEmail") employeeEmail: String,
                     @RequestParam(name = "selectFields", required = false) selectFields: List<String>? = emptyList(),
                     @RequestParam(name = "notSelectFields", required = false) notSelectFields: List<String>? = emptyList()): Single<Optional<EmployeeDto>>


    @GetMapping(path = ["/employees"], produces = ["application/json"])
    fun findEmployees(
        @RequestParam(name = "employeeEmails", required = false) employeeEmails: List<String>? = emptyList(),
        @RequestParam(name = "notEmployeeEmails", required = false) notEmployeeEmails: List<String>? = emptyList(),
        @RequestParam(name = "employeeFirstNames", required = false) employeeFirstNames: List<String>? = emptyList(),
        @RequestParam(name = "notEmployeeFirstNames", required = false) notEmployeeFirstNames: List<String>? = emptyList(),
        @RequestParam(name = "employeeLastNames", required = false) employeeLastNames: List<String>? = emptyList(),
        @RequestParam(name = "notEmployeeLastNames", required = false) notEmployeeLastNames: List<String>? = emptyList(),
        @RequestParam(name = "sorting", required = false) sorting: String? = null,
        @RequestParam(name = "offset", required = false) offset: Int? = null,
        @RequestParam(name = "limit", required = false) limit: Int? = null,
        @RequestParam(name = "selectFields", required = false) selectFields: List<String>? = emptyList(),
        @RequestParam(name = "notSelectFields", required = false) notSelectFields: List<String>? = emptyList()): Single<List<EmployeeDto>>


    @GetMapping(path = ["/employees/length"], produces = ["application/json"])
    fun countEmployees(
        @RequestParam(name = "employeeEmails", required = false) employeeEmails: List<String>? = emptyList(),
        @RequestParam(name = "notEmployeeEmails", required = false) notEmployeeEmails: List<String>? = emptyList(),
        @RequestParam(name = "employeeFirstNames", required = false) employeeFirstNames: List<String>? = emptyList(),
        @RequestParam(name = "notEmployeeFirstNames", required = false) notEmployeeFirstNames: List<String>? = emptyList(),
        @RequestParam(name = "employeeLastNames", required = false) employeeLastNames: List<String>? = emptyList(),
        @RequestParam(name = "notEmployeeLastNames", required = false) notEmployeeLastNames: List<String>? = emptyList()): Optional<Int>

}