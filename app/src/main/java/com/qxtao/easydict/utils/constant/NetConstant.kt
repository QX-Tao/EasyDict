package com.qxtao.easydict.utils.constant

object NetConstant {
    // 网络请求基地址
    const val baseService = "https://dict.youdao.com"

    // 每日一句
    const val imgApi = "https://api.ee123.net/img/jump.php?day="
    const val dailySentenceApi = "https://sentence.iciba.com/?c=dailysentence&m=getdetail&title="

    // 语法检查
    const val grammarCheck = "https://inter.youdao.com/grammarCheck"

    // 在线词典
    const val dictBase = "https://inter.youdao.com/intersearch"
    const val dictMore = "https://dict.youdao.com/jsonapi"
    const val dictVoice = "https://inter.youdao.com/dictvoice"

    // 单词发音
    const val voiceUk = "https://dict.youdao.com/dictvoice?type=1&audio="
    const val voiceUs = "https://dict.youdao.com/dictvoice?type=0&audio="
}
