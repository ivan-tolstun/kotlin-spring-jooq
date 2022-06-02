package de.tolstun.database.query

import org.jooq.Condition
import org.jooq.Record
import org.jooq.TableField
import org.jooq.TableRecord
import org.jooq.impl.CustomTable
import org.jooq.impl.DSL
import java.util.*


interface QueryHelper {


    fun <T : TableRecord<T>> findFields(fieldName: String,
                                        table: CustomTable<T>): List<TableField<T, *>> {
        return table
                .fields().toList()
                .filter { it.name.equals(fieldName, true) }
                .filterIsInstance<TableField<T, *>>()
    }


    fun <T : TableRecord<T>> findFieldsOrAsterisk(selectOnlyFields: List<TableField<T, *>>,
                                                  table: CustomTable<T>): List<TableField<T, *>> {

        return if (selectOnlyFields != null && selectOnlyFields.isNotEmpty())
            selectOnlyFields
                    .map { it.name }
                    .flatMap { this.findFields<T>(it, table) }
                    .distinct()
        else table.fields().toList().filterIsInstance<TableField<T, *>>()
    }


    fun findFieldsOrAsterisk(selectOnlyFields: List<TableField<*, *>>,
                             vararg tables: CustomTable<*>): List<TableField<*, *>> {

        return tables.flatMap { table ->

            if (selectOnlyFields != null && selectOnlyFields.isNotEmpty())
                selectOnlyFields
                    .map { it.name }
                    .flatMap { this.findFields(it, table) }
                    .distinct()

            else table.fields().toList().filterIsInstance<TableField<*, *>>()
        }
    }


    fun <T> inOrIgnoreEmpty(values: List<T>,
                            field: TableField<out Record?, T>): Condition {
        return if (values.size <= 1) equalOrIgnoreEmpty(values.stream().findFirst(), field)
        else field.`in`(values)
    }


    fun <T> notInOrIgnoreEmpty(values: List<T>,
                               field: TableField<out Record?, T>): Condition {

        return if (values.size <= 1) notEqualOrIgnoreEmpty(values.stream().findFirst(), field)
        else field.notIn(values)
    }


    fun <T> equalOrIgnoreEmpty(value: Optional<T>,
                               field: TableField<out Record?, T>): Condition {
        return value.map { field.eq(it) }.orElse(DSL.noCondition())
    }


    fun <T> equalOrIgnoreEmpty(value: T?,
                               field: TableField<out Record?, T>): Condition {
        return value?.let { field.eq(it) } ?: DSL.noCondition()
    }


    fun <T> notEqualOrIgnoreEmpty(value: Optional<T>,
                                  field: TableField<out Record?, T>): Condition {
        return value.map { field.notEqual(it) }.orElse(DSL.noCondition())
    }


    fun <T> notEqualOrIgnoreEmpty(value: T?,
                                  field: TableField<out Record?, T>): Condition {
        return value?.let { field.notEqual(it) } ?: DSL.noCondition()
    }
}