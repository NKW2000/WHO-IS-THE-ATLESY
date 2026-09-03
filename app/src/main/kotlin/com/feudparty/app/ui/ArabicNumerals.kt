package com.feudparty.app.ui

private val ARABIC_DIGITS = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')

/** الأرقام بتنعرض بالشكل العربي (٠١٢٣) زي التصميم. */
fun Int.ar(): String = toString().ar()

fun String.ar(): String = map { char ->
    if (char in '0'..'9') ARABIC_DIGITS[char - '0'] else char
}.joinToString("")
