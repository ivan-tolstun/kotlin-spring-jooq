package de.tolstun.testcontainer.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
data class EmployeeDto(@JsonProperty("email") val email: String? = null,
                       @JsonProperty("firstName") val firstName: String? = null,
                       @JsonProperty("lastName") val lastName: String? = null)
