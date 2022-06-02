package de.tolstun.database.extension.jooq

import org.jooq.Field
import org.jooq.Record
import org.jooq.TableField


object TableFieldExpansion {


    fun TableField<out Record, *>.nameWithTablePrefix(): String {
        return this.qualifiedName.name.joinToString("__")
    }


    fun Field<*>.nameWithTablePrefix(): String {
        return this.qualifiedName.name.joinToString("__")
    }


}