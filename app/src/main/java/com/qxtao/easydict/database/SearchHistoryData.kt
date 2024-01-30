package com.qxtao.easydict.database

import android.content.ContentValues
import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class SearchHistoryData (
    private val context: Context
) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {
    private val db: SQLiteDatabase

    companion object {
        private const val DATABASE_NAME = "search_history.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PASSWORD = "easyenglish"

        // 定义表名和列名
        private const val DICT_TABLE_NAME = "dict"
        private const val COLUMN_ORIGIN = "origin"
        private const val COLUMN_TRANSLATION = "translation"
        private const val COLUMN_TIMESTAMP = "timestamp"
    }

    init {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        db = SQLiteDatabase.openOrCreateDatabase(databaseFile,
            DATABASE_PASSWORD, null, null)
        initTable(db)
    }

    private fun initTable(db: SQLiteDatabase) {
        val createDictTableQuery = "CREATE TABLE IF NOT EXISTS $DICT_TABLE_NAME (" +
                "$COLUMN_ORIGIN TEXT PRIMARY KEY," +
                "$COLUMN_TRANSLATION TEXT," +
                "$COLUMN_TIMESTAMP INTEGER" +
                ")"
        db.execSQL(createDictTableQuery)
    }

    fun getSearchHistory(): List<SearchHistory>? {
        val query = "SELECT * FROM $DICT_TABLE_NAME ORDER BY $COLUMN_TIMESTAMP DESC"
        val cursor = db.rawQuery(query, null)
        val searchHistory = mutableListOf<SearchHistory>()
        while (cursor.moveToNext()){
            val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORIGIN))
            val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
            val timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP))
            val searchSearchHistory = SearchHistory(origin, translation, timestamp)
            searchHistory.add(searchSearchHistory)
        }
        cursor.close()
        return searchHistory.ifEmpty { null }
    }

    fun deleteSearchRecord(origin: String) {
        val whereClause = "$COLUMN_ORIGIN = ?"
        val whereArgs = arrayOf(origin)
        db.delete(DICT_TABLE_NAME, whereClause, whereArgs)
    }

    fun setSearchRecord(origin: String, translation: String) {
        val query = "SELECT * FROM $DICT_TABLE_NAME WHERE $COLUMN_ORIGIN = ?"
        val cursor = db.rawQuery(query, arrayOf(origin))
        val values = ContentValues().apply {
            put(COLUMN_ORIGIN, origin)
            put(COLUMN_TRANSLATION, translation)
            put(COLUMN_TIMESTAMP, System.currentTimeMillis())
        }
        if (cursor.count > 0) { // 如果记录存在，则修改
            db.update(DICT_TABLE_NAME, values, "$COLUMN_ORIGIN = ?", arrayOf(origin))
        } else { // 如果不存在，则插入
            db.insert(DICT_TABLE_NAME, null, values)
        }
    }

    fun updateTranslation(origin: String, newTranslation: String, updateTimestamp: Boolean) {
        val contentValues = ContentValues().apply {
            put(COLUMN_TRANSLATION, newTranslation)
            if (updateTimestamp) {
                put(COLUMN_TIMESTAMP, System.currentTimeMillis())
            }
        }
        val whereClause = "$COLUMN_ORIGIN = ?"
        val whereArgs = arrayOf(origin)
        writableDatabase.update(DICT_TABLE_NAME, contentValues, whereClause, whereArgs)
    }


    override fun onCreate(db: SQLiteDatabase?) { }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) { }

    override val writableDatabase: SQLiteDatabase
        get() = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME),
            DATABASE_PASSWORD, null, null)

    override val readableDatabase: SQLiteDatabase
        get() = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME),
            DATABASE_PASSWORD, null, null)

    override fun close() {
        db.close()
        super.close()
    }

    data class SearchHistory(
        val origin: String,
        val translation: String,
        val timestamp: Long
    )
}