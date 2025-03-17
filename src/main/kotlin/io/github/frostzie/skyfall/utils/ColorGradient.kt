
object GradientCreator

//TODO: add support for this later on (prob with art who knows color codes)


fun createGradientText(text: String, startColor: Int, endColor: Int): String {
    val startRed = (startColor shr 16) and 0xFF
    val startGreen = (startColor shr 8) and 0xFF
    val startBlue = startColor and 0xFF

    val endRed = (endColor shr 16) and 0xFF
    val endGreen = (endColor shr 8) and 0xFF
    val endBlue = endColor and 0xFF

    val result = StringBuilder()
    for (i in text.indices) {
        val ratio = i.toFloat() / (text.length - 1)
        val red = (startRed + (endRed - startRed) * ratio).toInt()
        val green = (startGreen + (endGreen - startGreen) * ratio).toInt()
        val blue = (startBlue + (endBlue - startBlue) * ratio).toInt()

        val colorCode = findClosestColorCode(red, green, blue)
        result.append("§$colorCode${text[i]}")
    }
    return result.toString()
}



private fun findClosestColorCode(red: Int, green: Int, blue: Int): Char {
    val colors = mapOf(
        '0' to intArrayOf(0, 0, 0),         // Black
        '1' to intArrayOf(0, 0, 170),       // Dark Blue
        '2' to intArrayOf(0, 170, 0),       // Dark Green
        '3' to intArrayOf(0, 170, 170),     // Dark Aqua
        '4' to intArrayOf(170, 0, 0),       // Dark Red
        '5' to intArrayOf(170, 0, 170),     // Dark Purple
        '6' to intArrayOf(255, 170, 0),     // Gold
        '7' to intArrayOf(170, 170, 170),   // Gray
        '8' to intArrayOf(85, 85, 85),      // Dark Gray
        '9' to intArrayOf(85, 85, 255),     // Blue
        'a' to intArrayOf(85, 255, 85),     // Green
        'b' to intArrayOf(85, 255, 255),    // Aqua
        'c' to intArrayOf(255, 85, 85),     // Red
        'd' to intArrayOf(255, 85, 255),    // Light Purple
        'e' to intArrayOf(255, 255, 85),    // Yellow
        'f' to intArrayOf(255, 255, 255)    // White
    )

    var closestCode = 'f'
    var closestDistance = Int.MAX_VALUE

    for ((code, rgb) in colors) {
        val distance = (red - rgb[0]) * (red - rgb[0]) +
                       (green - rgb[1]) * (green - rgb[1]) +
                       (blue - rgb[2]) * (blue - rgb[2])
        if (distance < closestDistance) {
            closestDistance = distance
            closestCode = code
        }
    }
    return closestCode
}

