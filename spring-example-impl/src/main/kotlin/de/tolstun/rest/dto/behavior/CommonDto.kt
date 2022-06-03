package de.tolstun.rest.dto.behavior

import com.fasterxml.jackson.databind.ObjectMapper
import de.tolstun.testcontainer.rest.dto.SortDto


object CommonDto {


    private val mapper = ObjectMapper()


    fun String.toSortDto(): SortDto =

        mapper.readValue(this, SortDto::class.java)


    fun Map<String, String>.toSortDto() =

        SortDto(
            field = this["field"]!!,
            order = this["order"]!!,
        )




}