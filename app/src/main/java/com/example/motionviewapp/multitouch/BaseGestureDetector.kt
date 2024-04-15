package com.example.motionviewapp.multitouch

import android.content.Context
import android.view.MotionEvent

abstract class BaseGestureDetector(protected val mContext: Context) {

    var mGestureInProgress = false
        protected set
    var mPrevEvent: MotionEvent? = null
        protected set
    private var mCurrEvent: MotionEvent? = null
    protected var mCurrPressure = 0f
    protected var mPrevPressure = 0f
    var mTimeDelta: Long = 0
        protected set

    fun onTouchEvent(event: MotionEvent): Boolean {
        val actionCode = event.action and MotionEvent.ACTION_MASK
        if (!mGestureInProgress) {
            handleStartProgressEvent(actionCode, event)
        } else {
            handleInProgressEvent(actionCode, event)
        }
        return true
    }

    protected abstract fun handleStartProgressEvent(actionCode: Int, event: MotionEvent)

    protected abstract fun handleInProgressEvent(actionCode: Int, event: MotionEvent)

    open fun updateStateByEvent(curr: MotionEvent) {
        val prev = mPrevEvent
        if (mCurrEvent != null) {
            mCurrEvent!!.recycle()
            mCurrEvent = null
        }
        mCurrEvent = MotionEvent.obtain(curr)
        mTimeDelta = curr.eventTime - prev!!.eventTime
        mCurrPressure = curr.getPressure(curr.actionIndex)
        mPrevPressure = prev.getPressure(prev.actionIndex)
    }

    open fun resetState() {
        if (mPrevEvent != null) {
            mPrevEvent!!.recycle()
            mPrevEvent = null
        }
        if (mCurrEvent != null) {
            mCurrEvent!!.recycle()
            mCurrEvent = null
        }
        mGestureInProgress = false
    }

    val eventTime: Long
        get() = mCurrEvent!!.eventTime

    companion object {
        const val PRESSURE_THRESHOLD = 0.99f
    }

}