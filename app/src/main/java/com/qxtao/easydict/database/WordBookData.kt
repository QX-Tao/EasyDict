package com.qxtao.easydict.database

import android.content.ContentValues
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import java.sql.SQLException
import java.sql.Timestamp

class WordBookData (
    private val context: Context
) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {
    private val db: SQLiteDatabase

    companion object {
        private const val DATABASE_NAME = "word_book.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PASSWORD = "easyenglish"

        // 定义表名和列名
        private const val WORD_BOOK_TABLE_NAME = "word_book"
        private const val COLUMN_WORD_BOOK_ID= "book_id"
        private const val COLUMN_WORD_BOOK_NAME = "book_name"
        private const val COLUMN_WORD_ID = "word_id"
        private const val WORD_TABLE_NAME = "word"
        private const val COLUMN_WORD = "word"
        private const val COLUMN_WORD_TRANS = "word_translation"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val LINES_WORD_BOOK_MY_COLLECT = "我的收藏"
    }

    init {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        db = SQLiteDatabase.openOrCreateDatabase(databaseFile,
            DATABASE_PASSWORD, null, null)
        initTable(db)
    }

    private fun initTable(db: SQLiteDatabase) {
        val createWordBookTableQuery = "CREATE TABLE IF NOT EXISTS $WORD_BOOK_TABLE_NAME (" +
                "$COLUMN_WORD_BOOK_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_WORD_BOOK_NAME TEXT UNIQUE," +
                "$COLUMN_TIMESTAMP INTEGER" +
                ")"

        val createWordTableQuery = "CREATE TABLE IF NOT EXISTS $WORD_TABLE_NAME (" +
                "$COLUMN_WORD_ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "$COLUMN_WORD TEXT UNIQUE," +
                "$COLUMN_WORD_TRANS TEXT," +
                "$COLUMN_TIMESTAMP INTEGER," +
                "$COLUMN_WORD_BOOK_ID INTEGER," +
                "FOREIGN KEY($COLUMN_WORD_BOOK_ID) REFERENCES $WORD_BOOK_TABLE_NAME($COLUMN_WORD_BOOK_ID)" +
                ")"

        db.execSQL(createWordBookTableQuery)
        db.execSQL(createWordTableQuery)

        addWordBook(LINES_WORD_BOOK_MY_COLLECT, Long.MAX_VALUE)
    }

    // 添加单词本
    fun addWordBook(bookName: String, timestamp: Long? = null): Pair<Boolean, String> {
        val values = ContentValues().apply {
            put(COLUMN_WORD_BOOK_NAME, bookName)
            put(COLUMN_TIMESTAMP, timestamp ?: System.currentTimeMillis())
        }
        return try {
            db.insertOrThrow(WORD_BOOK_TABLE_NAME, null, values)
            Pair(true, bookName)
        } catch (e: Exception) {
            // 处理单词本已存在的情况
            if (e.message?.contains("UNIQUE constraint failed") == true) {
                // 单词本已存在，不执行任何操作
                Pair(false, "单词本已存在")
            } else {
                Pair(false, "添加单词本失败")
            }
        }
    }


    // 修改指定单词本名称
    fun renameWordBook(oldName: String, newName: String): Pair<Boolean, String> {
        val values = ContentValues().apply {
            put(COLUMN_WORD_BOOK_NAME, newName)
        }
        return try {
            db.update(WORD_BOOK_TABLE_NAME, values, "$COLUMN_WORD_BOOK_NAME = ?", arrayOf(oldName))
            Pair(true, newName)
        } catch (e: Exception) {
            if (e.message?.contains("UNIQUE constraint failed") == true) {
                // 单词本已存在，不执行任何操作
                Pair(false, "单词本已存在")
            } else {
                Pair(false, "修改单词本名称失败")
            }
        }
    }

    // 添加单词到指定单词本（默认添加到“我的收藏”）
    fun addWordToBook(word: String, translation: String, bookName: String = LINES_WORD_BOOK_MY_COLLECT) {
        val bookId = getWordBookId(bookName)
        if (bookId != -1 && !isWordInWordBooks(word)) {
            val values = ContentValues().apply {
                put(COLUMN_WORD, word)
                put(COLUMN_WORD_TRANS, translation)
                put(COLUMN_TIMESTAMP, System.currentTimeMillis())
                put(COLUMN_WORD_BOOK_ID, bookId)
            }
            db.insert(WORD_TABLE_NAME, null, values)
        }
    }

    // 删除单词本
    fun deleteWordBook(bookName: String) {
        val bookId = getWordBookId(bookName)
        if (bookId != -1) {
            db.delete(WORD_BOOK_TABLE_NAME, "$COLUMN_WORD_BOOK_ID = ?", arrayOf(bookId.toString()))
            db.delete(WORD_TABLE_NAME, "$COLUMN_WORD_BOOK_ID = ?", arrayOf(bookId.toString()))
        }
    }

    // 删除单词本中的指定单词
    fun deleteWordFromBook(word: String) {
        db.delete(WORD_TABLE_NAME, "$COLUMN_WORD = ?", arrayOf(word))
    }

    // 批量从单词本中删除单词
    fun deleteWordsFromBook(words: List<String>) {
        db.beginTransaction()
        try {
            for (word in words) {
                db.delete(
                    COLUMN_WORD,
                    "$COLUMN_WORD = ?",
                    arrayOf(word)
                )
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // 清空指定单词本中的所有单词
    fun clearWordBook(bookName: String) {
        val bookId = getWordBookId(bookName)
        if (bookId != -1) {
            db.delete(WORD_TABLE_NAME, "$COLUMN_WORD_BOOK_ID = ?", arrayOf(bookId.toString()))
        }
    }

    // 将单词从一个单词本移动到另一个单词本
    fun moveWordToBook(word: String, sourceBookName: String, destinationBookName: String) {
        val sourceBookId = getWordBookId(sourceBookName)
        val destinationBookId = getWordBookId(destinationBookName)

        if (sourceBookId != -1 && destinationBookId != -1) {
            val values = ContentValues().apply {
                put(COLUMN_WORD_BOOK_ID, destinationBookId)
            }

            db.update(
                WORD_TABLE_NAME,
                values,
                "$COLUMN_WORD = ? AND $COLUMN_WORD_BOOK_ID = ?",
                arrayOf(word, sourceBookId.toString())
            )
        }
    }

    // 批量移动单词到指定单词本
    fun moveWordsToBook(words: List<String>, sourceBookName: String, destinationBookName: String) : Pair<Boolean, String> {
        val sourceBookId = getWordBookId(sourceBookName)
        val destinationBookId = getWordBookId(destinationBookName)
        if (sourceBookId != -1 && destinationBookId != -1) {
            db.beginTransaction()
            try {
                val values = ContentValues().apply {
                    put(COLUMN_WORD_BOOK_ID, destinationBookId)
                }
                for (word in words) {
                    db.update(
                        WORD_TABLE_NAME,
                        values,
                        "$COLUMN_WORD = ? AND $COLUMN_WORD_BOOK_ID = ?",
                        arrayOf(word, sourceBookId.toString())
                    )
                }

                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
            }
            return Pair(true, destinationBookName)
        } else return Pair(false, "操作失败")
    }

    // 获取指定单词本中的单词列表
    suspend fun getWordList(bookName: String): List<Word> {
        return withContext(Dispatchers.IO) {
            val bookId = getWordBookId(bookName)
            val query = "SELECT * FROM $WORD_TABLE_NAME WHERE $COLUMN_WORD_BOOK_ID = ? ORDER BY $COLUMN_TIMESTAMP DESC"
            val cursor = db.rawQuery(query, arrayOf(bookId.toString()))
            val wordList = mutableListOf<Word>()
            while (cursor.moveToNext()){
                val word = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD_TRANS))
                wordList.add(Word(word, translation))
            }
            wordList
        }
    }

    // 获取单词本列表
    fun getWordBookList(): List<String> {
        val query = "SELECT $COLUMN_WORD_BOOK_NAME FROM $WORD_BOOK_TABLE_NAME ORDER BY $COLUMN_TIMESTAMP DESC"
        val cursor = db.rawQuery(query, null)
        val bookList = mutableListOf<String>()

        if (cursor.moveToFirst()) {
            do {
                bookList.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return bookList
    }

    // 获取单词本ID
    private fun getWordBookId(bookName: String): Int {
        val query = "SELECT $COLUMN_WORD_BOOK_ID FROM $WORD_BOOK_TABLE_NAME WHERE $COLUMN_WORD_BOOK_NAME = ?"
        val cursor = db.rawQuery(query, arrayOf(bookName))
        return if (cursor.moveToFirst()) {
            cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_WORD_BOOK_ID))
        } else {
            -1
        }
    }

    // 判断单词是否已经存在于任何一个单词本中
    fun isWordInWordBooks(word: String): Boolean {
        val query = "SELECT COUNT(*) FROM $COLUMN_WORD WHERE $COLUMN_WORD = ?"
        val cursor = db.rawQuery(query, arrayOf(word))
        return if (cursor.moveToFirst()) {
            cursor.getInt(0) > 0
        } else {
            false
        }
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

    data class Word(
        val word: String,
        val translation: String?
    )

}