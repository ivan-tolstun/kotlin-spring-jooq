package de.tolstun.database.extension.jooq

import org.jooq.*
import org.jooq.impl.TableImpl


object SelectOnConditionStepExpansion {


    fun SelectConditionStep<*>.orderBy(sorting: List<Pair<TableField<*, *>, SortOrder>>,
                                       vararg tables: TableImpl<*>): SelectSeekStepN<*> = this.orderBy(

        sorting.flatMap { (orderBy, direction) -> tables.flatMap { table ->

            table.fields()
                .filter { it.name.equals(orderBy.name, true) }
                .map {
                    when (direction) {
                        SortOrder.DESC -> it.desc()
                        SortOrder.ASC  -> it.asc()
                        else           -> it.sortDefault()
                    }
                }

        } })


}