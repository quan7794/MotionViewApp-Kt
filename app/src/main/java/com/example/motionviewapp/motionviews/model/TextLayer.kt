package com.example.motionviewapp.motionviews.model

class TextLayer(
    var text: String = DEFAULT_TEXT,
    var font: Font = Font(),
    var position: Float = Font.DEFAULT_COLOR_POSITION
) : Layer() {

    override var initialScale = DEFAULT_INITIAL_SCALE
    override var minScale = DEFAULT_MIN_SCALE
    override var maxScale = DEFAULT_MAX_SCALE
    var isEditing = false

    override fun reset() {
        super.reset()
        text = ""
        font = Font()
        position = 0f
    }

    override fun postScale(scaleDiff: Float) {
        val newVal = scale + scaleDiff

        val isOutOfMaxRange = newVal > maxScale && scale > newVal
        val isOutOfMinRange = newVal < minScale && scale < newVal
        val isInNormalRange = newVal in minScale..maxScale
        if (isOutOfMaxRange || isOutOfMinRange || isInNormalRange) {
            scale = newVal
        }
    }

    fun cloneTextLayer(): TextLayer {
        return TextLayer(text, font.copy(), position)
    }

    companion object {
        const val DEFAULT_MAX_SCALE = Limits.MAX_SCALE
        const val DEFAULT_MIN_SCALE = Limits.MIN_SCALE
        const val DEFAULT_INITIAL_SCALE = Limits.INITIAL_SCALE // set the same to avoid text scaling
        const val DEFAULT_TEXT = "Default text"
        const val MAX_SCALE_RATIO_TIMES = 5
        const val MIN_SCALE_RATIO_TIMES = 2
        const val PADDING_RATIO = 5
    }

    interface Limits {
        companion object {
            /**
             * limit text size to view bounds
             * so that users don't put small font size and scale it 100+ times
             */
            const val MAX_SCALE = 1.0f
            const val MIN_SCALE = 0.2f
            const val MIN_BITMAP_HEIGHT = 0.13f
            const val FONT_SIZE_STEP = 0.008f
            const val INITIAL_FONT_SIZE = 0.075f
            const val INITIAL_FONT_COLOR = -0x1000000
            const val INITIAL_SCALE = 0.8f // set the same to avoid text scaling
        }
    }
}


