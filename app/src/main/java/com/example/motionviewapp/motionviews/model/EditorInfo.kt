package com.example.motionviewapp.motionviews.model

import com.example.motionviewapp.motionviews.widget.MotionView

//this class use for edit board zoom in/out
data class EditorInfo (
        var ratio: Float = 1.0f,
        var left: Float = 0f,
        var top: Float = 0f,
        var editMode: MotionView.EditMode = MotionView.EditMode.CONTENT_EDIT
)