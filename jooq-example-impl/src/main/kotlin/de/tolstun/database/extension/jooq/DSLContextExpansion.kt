package de.tolstun.database.extension.jooq

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.TableField
import org.jooq.exception.DataAccessException
import org.jooq.impl.CustomTable
import org.jooq.impl.DSL
import org.jooq.impl.DSL.count
import java.math.BigInteger
import java.util.*


object DSLContextExpansion {


    val random = Random()


    fun DSLContext.nextPrimaryKey(table: CustomTable<*>,
                                  idField: TableField<out Record, out Number>): BigInteger {

        val primaryKeyName = table.getName() + "_" + idField.name + "_SEQ"

        val newPrimaryKey: BigInteger =
                try {
                    this.nextval(primaryKeyName)
                } catch (e: DataAccessException) {
                    this.createPrimaryKeySequenceIfNotExists(table, idField)
                    this.nextval(primaryKeyName)
                }

        return validateAndAdjustNewIntPrimaryKey(this, table, idField, primaryKeyName, newPrimaryKey)
    }


    private fun DSLContext.refreshPrimaryKeySequence(table: CustomTable<*>,
                                                     idField: TableField<out Record, out Number>): Int {

        val primaryKeyName = table.getName() + "_" + idField.name + "_SEQ"

        val maxId = this
            .select(DSL.max(idField).add(1))
            .from(table)
            .fetchOne()
            ?.let { it.getValue(DSL.max(idField).add(1)) }
            ?: 1

        try {
            val queryAsString = "ALTER SEQUENCE $primaryKeyName RESTART WITH ${maxId.toLong() + 1}"
            this.fetchSingle(queryAsString).getValue(0, Int::class.java)
        } catch (e: Exception) {
            // Or renaming the sequence
            this.alterSequence(primaryKeyName).renameTo("${primaryKeyName}_deprecated_${random.nextInt(101)}").execute()
        }

        // delete the old sequence
        // this.dropSequenceIfExists(primaryKeyName).execute()

        return this.createPrimaryKeySequenceIfNotExists(table, idField)
    }


    private fun DSLContext.createPrimaryKeySequenceIfNotExists(table: CustomTable<*>,
                                                               idField: TableField<out Record, out Number>, ): Int {

        val primaryKeyName = table.getName() + "_" + idField.name + "_SEQ"

        val maxId = this
                .select(DSL.max(idField).add(1))
                .from(table)
                .fetchOne()
                ?.let { it.getValue(DSL.max(idField).add(1)) }
                ?: 1

        return this
                .createSequenceIfNotExists(primaryKeyName)
                .minvalue(Int.MIN_VALUE + 1)
                .maxvalue(Int.MAX_VALUE)
                .startWith(maxId)
                .execute()
    }


    private fun validateAndAdjustNewIntPrimaryKey(dslContext: DSLContext,
                                                  table: CustomTable<*>,
                                                  idField: TableField<out Record, out Number>,
                                                  primaryKeyName: String,
                                                  newPrimaryKey: BigInteger, ): BigInteger {

        val isExists: Boolean = dslContext
                .select(count(idField))
                .from(table)
                .where(idField.cast(BigInteger::class.java).eq(newPrimaryKey))
                .fetchOne()
                ?.let { it.getValue(count(idField)) > 0 }
                ?: false

        return if (isExists) {
            dslContext.refreshPrimaryKeySequence(table, idField)
            dslContext.nextval(primaryKeyName)
        } else newPrimaryKey

    }


}