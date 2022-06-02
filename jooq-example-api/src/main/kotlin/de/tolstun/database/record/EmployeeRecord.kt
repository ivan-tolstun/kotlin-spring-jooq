package de.tolstun.database.record

import de.tolstun.database.table.EmployeeTable.Companion.EMPLOYEE
import org.jooq.impl.CustomRecord


class EmployeeRecord : CustomRecord<EmployeeRecord>(EMPLOYEE)