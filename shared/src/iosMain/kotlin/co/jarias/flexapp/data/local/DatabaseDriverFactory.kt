package co.jarias.flexapp.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import co.jarias.flexapp.shared.database.BingoDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(BingoDatabase.Schema, "bingo.db")
    }
}
