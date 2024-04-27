package com.example.motionviewapp.multitouch

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.sqrt

abstract class TwoFingerGestureDetector(context: Context?) : BaseGestureDetector(context!!) {

    private val mEdgeSlop: Float
    protected var mPrevFingerDiffX = 0f
    protected var mPrevFingerDiffY = 0f
    protected var mCurrFingerDiffX = 0f
    protected var mCurrFingerDiffY = 0f
    private var mRightSlopEdge = 0f
    private var mBottomSlopEdge = 0f
    private var mCurrLen = 0f
    private var mPrevLen = 0f

    var contentCenterPoint: PointF? = null

    abstract override fun handleStartProgressEvent(actionCode: Int, event: MotionEvent)

    abstract override fun handleInProgressEvent(actionCode: Int, event: MotionEvent)

    override fun updateStateByEvent(curr: MotionEvent) {
        super.updateStateByEvent(curr)
        val prev = mPrevEvent!!
        mCurrLen = -1f
        mPrevLen = -1f

        if (prev.pointerCount == 1) {
            contentCenterPoint?.let {
                mPrevFingerDiffX = it.x - prev.getX(0)
                mPrevFingerDiffY = it.y - prev.getY(0)
                mCurrFingerDiffX = it.x - curr.getX(0)
                mCurrFingerDiffY = it.y - curr.getY(0)
            }
        } else {
            mPrevFingerDiffX = prev.getX(1) - prev.getX(0)
            mPrevFingerDiffY = prev.getY(1) - prev.getY(0)
            mCurrFingerDiffX = curr.getX(1) - curr.getX(0)
            mCurrFingerDiffY = curr.getY(1) - curr.getY(0)
        }
    }

    val currentSpan: Float
        get() {
            if (mCurrLen == -1f) {
                val cvx = mCurrFingerDiffX
                val cvy = mCurrFingerDiffY
                mCurrLen = sqrt(cvx * cvx + cvy * cvy.toDouble()).toFloat()
            }
            return mCurrLen
        }

    val previousSpan: Float
        get() {
            if (mPrevLen == -1f) {
                val pvx = mPrevFingerDiffX
                val pvy = mPrevFingerDiffY
                mPrevLen = sqrt(pvx * pvx + pvy * pvy.toDouble()).toFloat()
            }
            return mPrevLen
        }

    open fun isSloppyGesture(event: MotionEvent): Boolean { // As orientation can change, query the metrics in touch down
//        val metrics = mContext.resources.displayMetrics
//        mRightSlopEdge = metrics.widthPixels - mEdgeSlop
//        mBottomSlopEdge = metrics.heightPixels - mEdgeSlop
//        val edgeSlop = mEdgeSlop
//        val rightSlop = mRightSlopEdge
//        val bottomSlop = mBottomSlopEdge
//        val x0 = event.rawX
//        val y0 = event.rawY
//        val x1 = getRawX(event, 1)
//        val y1 = getRawY(event, 1)
//        val p0sloppy = x0 < edgeSlop || y0 < edgeSlop || x0 > rightSlop || y0 > bottomSlop
//        val p1sloppy = x1 < edgeSlop || y1 < edgeSlop || x1 > rightSlop || y1 > bottomSlop
//        if (p0sloppy && p1sloppy) {
//            return true
//        } else if (p0sloppy) {
//            return true
//        } else if (p1sloppy) {
//            return true
//        }
        return false
    }

    override fun resetState() {
        super.resetState()
        mPrevFingerDiffX = 0F
        mPrevFingerDiffY = 0F
        mCurrFingerDiffX = 0F
        mCurrFingerDiffY = 0F
    }

    companion object {
        protected fun getRawX(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.x - event.rawX
            return if (pointerIndex < event.pointerCount) {
                event.getX(pointerIndex) + offset
            } else 0f
        }

        protected fun getRawY(event: MotionEvent, pointerIndex: Int): Float {
            val offset = event.y - event.rawY
            return if (pointerIndex < event.pointerCount) {
                event.getY(pointerIndex) + offset
            } else 0f
        }
    }

    init {
        val config = ViewConfiguration.get(context!!)
        mEdgeSlop = config.scaledEdgeSlop.toFloat()
    }
}