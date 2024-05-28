package com.qxtao.easydict.adapter.settings

class OpenSourceItem(
    val title: String,
    val info: String,
    val link: String,
    val license: String
){
    override fun toString(): String {
        return """
            "title": "$title",
            "info": "$info",
            "link": "$link",
            "license": "$license"
            """.trimIndent()
    }
}