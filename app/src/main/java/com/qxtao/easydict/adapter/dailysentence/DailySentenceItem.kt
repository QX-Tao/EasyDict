package com.qxtao.easydict.adapter.dailysentence

class DailySentenceItem(
    val type: Int,
    val date: String,
    val enSentence: String,
    val cnSentence: String,
    val imageUrl: String,
    val ttsUrl: String
){
    override fun toString(): String {
        return """
            "date": "$date",
            "sentenceEn": "$enSentence",
            "cnSentence": "$cnSentence",
            "imagePath": "$imageUrl",
            "soundPath": "$ttsUrl"
            """.trimIndent()
    }
}