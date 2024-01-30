package com.qxtao.easydict.database

import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class SimpleDictData (
    private val context: Context
) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {
    private val db: SQLiteDatabase

    companion object {
        private const val DATABASE_NAME = "simple_dict.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PASSWORD = "easyenglish"

        // 定义表名和列名
        private const val TABLE_NAME = "dict"
        private const val COLUMN_ORIGIN = "origin"
        private const val COLUMN_TRANSLATION = "translation"
    }

    init {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        db = SQLiteDatabase.openOrCreateDatabase(databaseFile,
            DATABASE_PASSWORD, null, null)
        initTable(db)
    }

    private fun initTable(db: SQLiteDatabase) {
        // 创建数据库表
        val createTableQuery = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                "$COLUMN_ORIGIN TEXT PRIMARY KEY," +
                "$COLUMN_TRANSLATION TEXT" +
                ")"
        db.execSQL(createTableQuery)
    }

    override fun onCreate(db: SQLiteDatabase?) {  }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {  }

    override val writableDatabase: SQLiteDatabase
        get() = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME),
            DATABASE_PASSWORD, null, null)

    override val readableDatabase: SQLiteDatabase
        get() = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME),
            DATABASE_PASSWORD, null, null)

    fun searchDicts(query: String): List<SimpleDict>? {
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ORIGIN, COLUMN_TRANSLATION),
            "$COLUMN_ORIGIN LIKE ? OR $COLUMN_TRANSLATION LIKE ?",
            arrayOf("$query%", "%$query%"),
            null,
            null,
            null
        )
        val simpleDicts = mutableListOf<SimpleDict>()
        while (cursor.moveToNext()) {
            val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORIGIN))
            val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
            simpleDicts.add(SimpleDict(origin, translation))
        }
        cursor.close()
        return simpleDicts.ifEmpty { null }
    }

    fun getDictListByOriginList(originList: List<String>): List<SimpleDict>? {
        val dictList = mutableListOf<SimpleDict>()
        val selection = "$COLUMN_ORIGIN IN (${originList.joinToString { "? " }})"
        val selectionArgs = originList.toTypedArray()
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
        while (cursor.moveToNext()) {
            val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORIGIN))
            val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
            val simpleDict = SimpleDict(origin, translation)
            dictList.add(simpleDict)
        }
        cursor.close()
        return dictList.ifEmpty { null }
    }

    fun getTranslationByOrigin(origin: String): String? {
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_TRANSLATION),
            "$COLUMN_ORIGIN = ?",
            arrayOf(origin),
            null,
            null,
            null
        )
        var translation: String? = null
        if (cursor.moveToFirst()) {
            translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
        }
        cursor.close()
        return translation
    }

    fun getRandomSimpleDictList(): List<SimpleDict> {
        val cursor = db.query(
            TABLE_NAME,
            arrayOf(COLUMN_ORIGIN, COLUMN_TRANSLATION),
            null,
            null,
            null,
            null,
            "RANDOM()",
            "8"
        )
        val simpleDicts = mutableListOf<SimpleDict>()
        while (cursor.moveToNext()) {
            val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ORIGIN))
            val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
            simpleDicts.add(SimpleDict(origin, translation))
        }
        cursor.close()
        return simpleDicts
    }

    override fun close() {
        db.close()
        super.close()
    }

    data class SimpleDict(
        val origin: String,
        val translation: String
    )

}