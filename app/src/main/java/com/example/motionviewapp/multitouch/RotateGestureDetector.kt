package com.example.motionviewapp.multitouch

import android.content.Context
import android.view.MotionEvent
import kotlin.math.atan2

class RotateGestureDetector(
        context: Context,
        private val mListener: OnRotateGestureListener
) : TwoFingerGestureDetector(context) {

    private var mSloppyGesture = false

    override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_DOWN -> {
                resetState()
                mPrevEvent = MotionEvent.obtain(event)
                mTimeDelta = 0

                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) {
                    mGestureInProgress = mListener.onRotateBegin(this)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (!mSloppyGesture) {
                    return
                }
                mSloppyGesture = isSloppyGesture(event)
                if (!mSloppyGesture) { // No, start normal gesture now
                    mGestureInProgress = mListener.onRotateBegin(this)
                }
            }
            MotionEvent.ACTION_POINTER_UP -> if (!mSloppyGesture) {
                return
            }
        }
    }

    override fun handleInProgressEvent(actionCode: Int, event: MotionEvent) {
        when (actionCode) {
            MotionEvent.ACTION_POINTER_UP -> {
                updateStateByEvent(event)
                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                if (!mSloppyGesture) {
                    mListener.onRotateEnd(this)
                }
                resetState()
            }
            MotionEvent.ACTION_MOVE -> {
                updateStateByEvent(event)
                if (mCurrPressure / mPrevPressure > PRESSURE_THRESHOLD) {
                    val updatePrevious = mListener.onRotate(this)
                    if (updatePrevious) {
                        mPrevEvent!!.recycle()
                        mPrevEvent = MotionEvent.obtain(event)
                    }
                }
            }
        }
    }

    override fun resetState() {
        super.resetState()
        mSloppyGesture = false
    }

    val rotationDegreesDelta: Float
        get() {
            val diffRadians =
                    atan2(mPrevFingerDiffY.toDouble(), mPrevFingerDiffX.toDouble()) - atan2(
                            mCurrFingerDiffY.toDouble(),
                            mCurrFingerDiffX.toDouble()
                    )
            return (diffRadians * 180 / Math.PI).toFloat()
        }

    interface OnRotateGestureListener {
        fun onRotate(detector: RotateGestureDetector): Boolean
        fun onRotateBegin(detector: RotateGestureDetector): Boolean
        fun onRotateEnd(detector: RotateGestureDetector)
    }

    open class SimpleOnRotateGestureListener : OnRotateGestureListener {
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            return false
        }

        override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
            return true
        }

        override fun onRotateEnd(detector: RotateGestureDetector) { // Do nothing, overridden implementation may be used
        }
    }

}
