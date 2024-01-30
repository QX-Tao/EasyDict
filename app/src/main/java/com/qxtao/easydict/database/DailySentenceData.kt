package com.qxtao.easydict.database

import android.content.ContentValues
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import android.content.Context

class DailySentenceData(
     private val context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "daily_sentence.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PASSWORD = "easydict"

        // 定义表名和列名
        private const val TABLE_NAME = "daily_sentence"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_EN_SENTENCE = "en_sentence"
        private const val COLUMN_CN_SENTENCE = "cn_sentence"
        private const val COLUMN_IMAGE_URL = "image_url"
        private const val COLUMN_TTS_URL = "tts_url"
    }

    init {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        val db = SQLiteDatabase.openOrCreateDatabase(databaseFile,
            DATABASE_PASSWORD, null, null)
        initTable(db)
        db.close()
    }

    override fun onCreate(db: SQLiteDatabase?) {  }

    private fun initTable(db: SQLiteDatabase) {
        // 创建数据库表
        val createTableQuery = "CREATE TABLE IF NOT EXISTS $TABLE_NAME (" +
                "$COLUMN_DATE TEXT PRIMARY KEY," +
                "$COLUMN_EN_SENTENCE TEXT," +
                "$COLUMN_CN_SENTENCE TEXT," +
                "$COLUMN_IMAGE_URL TEXT," +
                "$COLUMN_TTS_URL TEXT" +
                ")"
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // 在数据库升级时执行操作（如果需要）
        // 这里可以处理不同数据库版本之间的迁移逻辑
    }

    override val writableDatabase: SQLiteDatabase
        get() = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME),
            DATABASE_PASSWORD, null, null)

    override val readableDatabase: SQLiteDatabase
        get() = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(DATABASE_NAME),
            DATABASE_PASSWORD, null, null)

    // 插入数据
    fun insertData(date: String, enSentence: String, cnSentence: String, imageUrl: String, ttsUrl: String) {
        val db = writableDatabase
        // 检查是否已存在具有相同日期的记录
        val existingRecordCursor = db.query(
            TABLE_NAME,
            null,
            "$COLUMN_DATE = ?",
            arrayOf(date),
            null,
            null,
            null
        )
        if (existingRecordCursor.moveToFirst()) {
            // 如果已存在记录，则更新该记录
            val values = ContentValues().apply {
                put(COLUMN_EN_SENTENCE, enSentence)
                put(COLUMN_CN_SENTENCE, cnSentence)
                put(COLUMN_IMAGE_URL, imageUrl)
                put(COLUMN_TTS_URL, ttsUrl)
            }
            db.update(TABLE_NAME, values, "$COLUMN_DATE = ?", arrayOf(date))
        } else {
            // 如果不存在记录，则插入新的记录
            val values = ContentValues().apply {
                put(COLUMN_DATE, date)
                put(COLUMN_EN_SENTENCE, enSentence)
                put(COLUMN_CN_SENTENCE, cnSentence)
                put(COLUMN_IMAGE_URL, imageUrl)
                put(COLUMN_TTS_URL, ttsUrl)
            }
            db.insert(TABLE_NAME, null, values)
        }
        existingRecordCursor.close()
        db.close()
    }

    // 查询数据
    fun getDataByDate(date: String): DailySentence? {
        val db = readableDatabase
        val selection = "$COLUMN_DATE = ?"
        val selectionArgs = arrayOf(date)
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null)
        val dailySentence: DailySentence? = if (cursor.moveToFirst()) {
            DailySentence(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EN_SENTENCE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CN_SENTENCE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TTS_URL))
            )
        } else null
        cursor.close()
        db.close()
        return dailySentence
    }

    // 数据类 DailySentence
    data class DailySentence(
        val date: String,
        val enSentence: String,
        val cnSentence: String,
        val imageUrl: String,
        val ttsUrl: String
    )
}

