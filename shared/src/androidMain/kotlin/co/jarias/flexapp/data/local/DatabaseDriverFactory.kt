package co.jarias.flexapp.data.local

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import app.cash.sqldelight.db.SqlDriver
import co.jarias.flexapp.shared.database.BingoDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(BingoDatabase.Schema, context, "bingo.db")
    }
}
