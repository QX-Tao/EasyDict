package com.qxtao.easydict.database

import android.content.ContentValues
import android.content.Context
import com.qxtao.easydict.adapter.wordlist.WordListItem
import com.qxtao.easydict.utils.common.FileIOUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper

class WordListData (
    private val context: Context
) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {
    private val db: SQLiteDatabase

    companion object {
        private const val DATABASE_NAME = "word_list.db"
        private const val DATABASE_VERSION = 1
        private const val DATABASE_PASSWORD = "easyenglish"

        // 定义表名和列名
        private const val TABLE_DB_CONFIG = "config"
        private const val COLUMN_CONFIG_ID = "id"
        private const val COLUMN_SELECTED = "selected"
        private const val COLUMN_IS_CONFIRMED = "is_confirmed"
        private const val LINE_CONFIG_ID = 1
        private val TABLE_LIST = listOf("cet4", "cet6", "kaoyan", "ielts", "toefl", "xiaoxue", "chuzhong", "gaokao", "tem4", "tem8")
        private const val COLUMN_WORD = "word"
        private const val COLUMN_TRANSLATION = "translation"
        private const val COLUMN_IS_COLLECTED =  "is_collected"
        private const val COLUMN_IS_LEARNED = "is_learned"
    }

    init {
        val databaseFile = context.getDatabasePath(DATABASE_NAME)
        db = SQLiteDatabase.openOrCreateDatabase(databaseFile,
            DATABASE_PASSWORD, null, null)
        initTable(db)
    }

    private fun initTable(db: SQLiteDatabase) {
        val createConfigTableQuery = "CREATE TABLE IF NOT EXISTS $TABLE_DB_CONFIG (" +
                "$COLUMN_CONFIG_ID INTEGER PRIMARY KEY," +
                "$COLUMN_IS_CONFIRMED INTEGER," +
                "$COLUMN_SELECTED TEXT" +
                ")"
        db.execSQL(createConfigTableQuery)
        for (item in TABLE_LIST) {
            val createTableQuery = "CREATE TABLE IF NOT EXISTS $item (" +
                    "$COLUMN_WORD TEXT PRIMARY KEY," +
                    "$COLUMN_TRANSLATION TEXT," +
                    "$COLUMN_IS_COLLECTED INTEGER," +
                    "$COLUMN_IS_LEARNED INTEGER" +
                    ")"
            db.execSQL(createTableQuery)
        }
    }

    suspend fun updateConfig(selected: String = "cet4", isConfig: Boolean = true): Boolean {
        return withContext(Dispatchers.IO){
            try {
                val values = ContentValues().apply {
                    put(COLUMN_SELECTED, selected)
                    put(COLUMN_IS_CONFIRMED, if (isConfig) 1 else 0)
                }
                val whereClause = "$COLUMN_CONFIG_ID = ?"
                val whereArgs = arrayOf(LINE_CONFIG_ID.toString())
                val rowsUpdated = db.update(TABLE_DB_CONFIG, values, whereClause, whereArgs)
                if (rowsUpdated == 0) {
                    values.put(COLUMN_CONFIG_ID, LINE_CONFIG_ID)
                    db.insert(TABLE_DB_CONFIG, null, values)
                }
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    suspend fun getSelectBookInfo(): Pair<Boolean, Triple<String, Int, Int>?>{
        return withContext(Dispatchers.IO){
            if (!isConfirmed()){
                Pair(false, null)
            } else {
                Pair(true, Triple(selected(), getAllWordListNum(), getLeanedWordListNum()))
            }
        }
    }

    fun setIsCollected(word: String, tablePosition: Int, isCollected: Boolean){
        val values = ContentValues().apply {
            put(COLUMN_IS_COLLECTED, if (isCollected) System.currentTimeMillis() else 0)
        }
        val whereClause = "$COLUMN_WORD = ?"
        val whereArgs = arrayOf(word)
        db.update(TABLE_LIST[tablePosition], values, whereClause, whereArgs)
    }

    fun setIsLearned(word: String, tablePosition: Int, isLearned: Boolean){
        val values = ContentValues().apply {
            put(COLUMN_IS_LEARNED, if (isLearned) System.currentTimeMillis() else 0)
        }
        val whereClause = "$COLUMN_WORD = ?"
        val whereArgs = arrayOf(word)
        db.update(TABLE_LIST[tablePosition], values, whereClause, whereArgs)
    }

    private suspend fun configAllWord(): Boolean{
        return withContext(Dispatchers.IO){
            if (isConfirmed()) return@withContext true
            try {
                clearAllTable()
                for (item in TABLE_LIST){
                    val wordList = FileIOUtils.readFile2String(context.assets.open("txt/$item.txt")).
                    split("\r\n")
                    val list = SimpleDictData(context).getDictListByOriginList(wordList)!!
                    db.beginTransaction()
                    try {
                        for (word in list) addWord(word, item)
                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                }
                updateConfig()
                true
            } catch (e: Exception){
                false
            }
        }
    }

    suspend fun getAllWordListItems(): Pair<List<WordListItem>, String>{
        return withContext(Dispatchers.IO){
            val isReadyConfigAllWord = async { configAllWord() }.await()
            if (!isReadyConfigAllWord){
                getAllWordListItems()
            } else {
                val selected = selected()
                val list = mutableListOf<WordListItem>()
                val cursor = db.rawQuery("SELECT * FROM $selected WHERE $COLUMN_IS_LEARNED = 0", null)
                while (cursor.moveToNext()){
                    val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD))
                    val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
                    list.add(WordListItem(origin, translation, false))
                }
                Pair(list, selected)
            }
        }
    }

    suspend fun getAllWordListItem(): List<WordListItem> {
        return withContext(Dispatchers.IO){
            val selected = selected()
            val list = mutableListOf<WordListItem>()
            val cursor = db.rawQuery("SELECT * FROM $selected", null)
            while (cursor.moveToNext()){
                val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
                list.add(WordListItem(origin, translation, false))
            }
            list
        }
    }

    suspend fun getLearnedWordListItem(): List<WordListItem>{
        return withContext(Dispatchers.IO){
            val selected = selected()
            val list = mutableListOf<WordListItem>()
            val cursor = db.rawQuery("SELECT * FROM $selected WHERE $COLUMN_IS_LEARNED != 0 ORDER BY $COLUMN_IS_LEARNED DESC", null)
            while (cursor.moveToNext()){
                val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
                list.add(WordListItem(origin, translation, false))
            }
            list
        }
    }

    suspend fun getCollectedWordListItem(): List<WordListItem>{
        return withContext(Dispatchers.IO){
            val selected = selected()
            val list = mutableListOf<WordListItem>()
            val cursor = db.rawQuery("SELECT * FROM $selected WHERE $COLUMN_IS_COLLECTED != 0 ORDER BY $COLUMN_IS_COLLECTED DESC", null)
            while (cursor.moveToNext()){
                val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
                list.add(WordListItem(origin, translation, false))
            }
            list
        }
    }

    suspend fun getLearningWordListItem(): List<WordListItem>{
        return withContext(Dispatchers.IO){
            val selected = selected()
            val list = mutableListOf<WordListItem>()
            val cursor = db.rawQuery("SELECT * FROM $selected WHERE $COLUMN_IS_LEARNED = 0", null)
            while (cursor.moveToNext()){
                val origin = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_WORD))
                val translation = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TRANSLATION))
                list.add(WordListItem(origin, translation, false))
            }
            list
        }
    }

    fun getLeanedWordListNum(): Int {
        val selected = selected()
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $selected WHERE $COLUMN_IS_LEARNED != 0", null)
        return cursor.moveToFirst().let { cursor.getInt(0) }
    }
    fun getAllWordListNum(): Int {
        val selected = selected()
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $selected", null)
        return cursor.moveToFirst().let { cursor.getInt(0) }
    }
    fun getCollectedWordListNum(): Int {
        val selected = selected()
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $selected WHERE $COLUMN_IS_COLLECTED != 0", null)
        return cursor.moveToFirst().let { cursor.getInt(0) }
    }


    private fun clearAllTable(){
        db.execSQL("DELETE FROM $TABLE_DB_CONFIG")
        for (item in TABLE_LIST) {
            db.execSQL("DELETE FROM $item")
        }
    }

    private fun isConfirmed(id : Int = LINE_CONFIG_ID): Boolean {
        val cursor = db.rawQuery("SELECT * FROM $TABLE_DB_CONFIG WHERE $COLUMN_CONFIG_ID = $id", null)
        return cursor.moveToFirst() && cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_CONFIRMED)) == 1
    }

    private fun selected(id : Int = LINE_CONFIG_ID): String {
        val cursor = db.rawQuery("SELECT * FROM $TABLE_DB_CONFIG WHERE $COLUMN_CONFIG_ID = $id", null)
        return cursor.moveToFirst().let { cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SELECTED)) }
    }

    private fun addWord(word: SimpleDictData.SimpleDict, tableName: String){
        val values = ContentValues().apply {
            put(COLUMN_WORD, word.origin)
            put(COLUMN_TRANSLATION, word.translation)
            put(COLUMN_IS_COLLECTED, 0)
            put(COLUMN_IS_LEARNED, 0)
        }
        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE)
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
}