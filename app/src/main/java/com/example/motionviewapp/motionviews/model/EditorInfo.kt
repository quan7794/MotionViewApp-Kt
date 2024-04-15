package com.example.motionviewapp.motionviews.model

import com.example.motionviewapp.motionviews.widget.MotionView

data class EditorInfo (
        var ratio: Float = 1.0f,
        var left: Float = 0f,
        var top: Float = 0f,
        var editMode: MotionView.EditMode = MotionView.EditMode.DECOR
)