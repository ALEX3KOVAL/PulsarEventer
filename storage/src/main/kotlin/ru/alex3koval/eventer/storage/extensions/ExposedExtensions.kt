package ru.alex3koval.eventer.storage.extensions

import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateStatement
import org.jetbrains.exposed.sql.update
import ru.alex3koval.eventer.app.exception.DatabaseOperationException

fun<T : Table> T.singleUpdate(
    where: SqlExpressionBuilder.() -> Op<Boolean>,
    body: T.(UpdateStatement) -> Unit
): Result<Unit> = runCatching {
    val numberOfRecords = update(where = where, body = body)

    if (numberOfRecords == 0) throw DatabaseOperationException("Ни одна запись не была обновлена")
    if (numberOfRecords > 1) throw DatabaseOperationException("Было обновлено $numberOfRecords записей, вместо одной")
}