package de.tolstun.database.record

import org.jooq.Record


object CommonRecordBehavior {

    fun Record.firstToInteger(): Int? {
        return this.get(0, Int::class.java)
    }


    fun Record.firstToString(): String? {
        return this.get(0, String::class.java)
    }

}
