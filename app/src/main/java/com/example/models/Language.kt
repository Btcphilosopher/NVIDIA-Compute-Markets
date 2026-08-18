package com.example.models

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    TAIWANESE_HOKKIEN("nan", "Taiwanese Hokkien", "臺灣話 / 台語"),
    CHINESE_MANDARIN("zh", "Chinese (Mandarin)", "中文 (國語/普通話)")
}
