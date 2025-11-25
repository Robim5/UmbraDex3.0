package com.umbra.umbradex.util

import android.content.Context
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

// Toast extensions
fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

// String extensions
fun String.capitalizeFirst(): String {
    return this.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(Locale.getDefault())
        else it.toString()
    }
}

fun String.toPokemonId(): Int {
    return this.removePrefix("#").toIntOrNull() ?: 0
}

fun Int.toPokemonIdString(): String {
    return "#${this.toString().padStart(4, '0')}"
}

// Date extensions
fun Date.toFormattedString(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(this)
}

fun String.toDate(): Date? {
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.parse(this)
    } catch (e: Exception) {
        null
    }
}

// Color extensions
fun String.toColor(): Color {
    return try {
        Color(android.graphics.Color.parseColor(this))
    } catch (e: Exception) {
        Color.White
    }
}

// List extensions
fun <T> List<T>.second(): T? {
    return if (this.size >= 2) this[1] else null
}

// Generation helper
fun Int.getGeneration(): Int {
    return when (this) {
        in 1..151 -> 1
        in 152..251 -> 2
        in 252..386 -> 3
        in 387..493 -> 4
        in 494..649 -> 5
        in 650..721 -> 6
        in 722..809 -> 7
        in 810..905 -> 8
        in 906..1025 -> 9
        else -> 1
    }
}

// XP calculation (simple leveling system)
fun calculateXpForLevel(level: Int): Int {
    return level * 100 // 100 XP per level
}

fun calculateLevelFromXp(xp: Int): Int {
    return (xp / 100).coerceAtLeast(1)
}