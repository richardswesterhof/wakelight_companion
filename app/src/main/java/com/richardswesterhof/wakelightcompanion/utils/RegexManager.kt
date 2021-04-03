package com.richardswesterhof.wakelightcompanion.utils

val schedule_interval_regex = Regex("([A-Z]+)\\s*(?:([0-1]?[0-9]|2[0-3]):([0-5]?[0-9])\\s*-\\s*([0-1]?[0-9]|2[0-3]):([0-5]?[0-9])|([A-Z]+))", setOf(RegexOption.IGNORE_CASE))
