package de.tolstun.database.extension.jooq

import de.tolstun.database.extension.jooq.TableFieldExpansion.nameWithTablePrefix
import org.jooq.Field
import org.jooq.impl.CustomTable


object CustomTableExpansion {


    fun CustomTable<*>.asteriskWithTablePrefix(): List<Field<out Any>> {

        return this.fields().toList().map { it.`as`(it.nameWithTablePrefix()) }
    }


}