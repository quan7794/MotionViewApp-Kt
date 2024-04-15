package com.example.motionviewapp.motionviews.model

open class Layer(
        var rotationInDegrees: Float = 0f,
        var scale: Float = 0f,
        var x: Float = 0f,
        var y: Float = 0f,
        var isFlipped: Boolean = false
) {

    open var initialScale = DEFAULT_INITIAL_SCALE
    open var minScale = DEFAULT_MIN_SCALE
    open var maxScale = DEFAULT_MAX_SCALE

    open fun reset() {
        rotationInDegrees = 0.0f
        scale = 1.0f
        isFlipped = false
        x = 0.0f
        y = 0.0f
    }

    open fun resetRotationInDegrees() {
        rotationInDegrees = 0.0f
    }

    open fun postScale(scaleDiff: Float) {
        val newVal = scale + scaleDiff
        val isOutOfMaxRange = newVal > maxScale && scale > newVal
        val isOutOfMinRange = newVal < minScale && scale < newVal
        val isInNormalRange = newVal in minScale..maxScale
        if (isOutOfMinRange
                || isOutOfMaxRange
                || isInNormalRange) {
            scale = newVal
        }
    }

    fun postRotate(rotationInDegreesDiff: Float) {
        rotationInDegrees += rotationInDegreesDiff
        rotationInDegrees %= 360.0f
    }

    fun postTranslate(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    fun flip() {
        isFlipped = !isFlipped
    }

    companion object {
        const val DEFAULT_MIN_SCALE = 0.1f
        const val DEFAULT_MAX_SCALE = 2f
        const val DEFAULT_INITIAL_SCALE = 0.4f
        const val MAX_SCALE_RATIO_TIMES = 20
    }

    open fun clone(): Layer {
        return Layer(rotationInDegrees, scale, x, y, isFlipped)
    }
}