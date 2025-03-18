package io.github.frostzie.skyfall.utils

import java.text.SimpleDateFormat

object Util {
    fun SimpleDateFormat.formatCurrentTime(): String = this.format(System.currentTimeMillis())
}