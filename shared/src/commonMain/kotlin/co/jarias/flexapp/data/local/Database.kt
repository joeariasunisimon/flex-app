package co.jarias.flexapp.data.local

import co.jarias.flexapp.shared.database.BingoDatabase

class Database(databaseDriverFactory: DatabaseDriverFactory) {
    val database = BingoDatabase(databaseDriverFactory.createDriver())
}
