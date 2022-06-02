package de.tolstun.testcontainer.extension.impl.auth.extension.common

import java.time.Instant
import java.time.ZoneId
import java.util.*


object DataExtension {


    fun Date.toLocalDateTime() =

        Instant
            .ofEpochMilli(this.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime()


    fun Date.toLocalDate() =

        Instant
            .ofEpochMilli(this.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()


}