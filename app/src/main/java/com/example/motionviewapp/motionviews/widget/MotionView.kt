package com.example.motionviewapp.motionviews.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.view.GestureDetectorCompat
import com.example.motionviewapp.R
import com.example.motionviewapp.motionviews.model.EditorInfo
import com.example.motionviewapp.motionviews.model.Layer
import com.example.motionviewapp.motionviews.widget.content.IconContent
import com.example.motionviewapp.motionviews.widget.content.ImageContent
import com.example.motionviewapp.motionviews.widget.content.BaseContent
import com.example.motionviewapp.multitouch.MoveGestureDetector
import com.example.motionviewapp.multitouch.RotateGestureDetector
import com.example.motionviewapp.utils.BorderUtil
import com.example.motionviewapp.utils.MathUtils.calculateAngledBounce
import com.example.motionviewapp.utils.MathUtils.calculateIntersectionBetweenMainLine
import com.example.motionviewapp.utils.MathUtils.calculateIntersectionBetweenMainLineAndHorizontalAxis
import com.example.motionviewapp.utils.MathUtils.calculateIntersectionBetweenMainLineAndVerticalAxis
import com.example.motionviewapp.utils.MathUtils.calculateSlope
import com.example.motionviewapp.utils.MathUtils.checkIntersection
import com.example.motionviewapp.utils.MathUtils.checkValidMovement
import com.example.motionviewapp.utils.MathUtils.convertToPositivePoint
import com.example.motionviewapp.motionviews.widget.content.TextContent

class MotionView : FrameLayout {

    private val contents: MutableList<BaseContent> = ArrayList()
    private val icons: MutableList<IconContent> = ArrayList()
    private lateinit var motionViewCallback: MotionViewCallback
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var rotateGestureDetector: RotateGestureDetector
    private lateinit var moveGestureDetector: MoveGestureDetector
    private lateinit var gestureDetectorCompat: GestureDetectorCompat
    private lateinit var motionViewRectF: RectF
    var editorInfo = EditorInfo()
    var selectedContent: BaseContent? = null

    @ColorInt
    private var themeColor: Int = Color.WHITE

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        init(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        configDefaultIcons()

        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        rotateGestureDetector = RotateGestureDetector(context, RotateListener())
        moveGestureDetector = MoveGestureDetector(context, MoveListener())
        gestureDetectorCompat = GestureDetectorCompat(context, TapsListener())

        motionViewRectF = RectF(0f, 0f, width.toFloat(), height.toFloat())
        setOnTouchListener(onTouchListener)
        updateUI()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        drawThemeColor(canvas)
        drawAllContents(canvas)
    }

    fun setThemeColor(@ColorInt color: Int) {
        themeColor = color
        invalidate()
    }

    private fun drawThemeColor(canvas: Canvas, color: Int = themeColor) {
        canvas.drawColor(color)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return when (editorInfo.editMode) {
            EditMode.CROP -> false // Pass touch event to child views
            EditMode.CONTENT_EDIT -> true // Handle touch event
        }
    }

    fun setMotionViewCallback(callback: MotionViewCallback) {
        init(context)
        motionViewCallback = callback
    }

    fun addContent(content: BaseContent, action: AddAction = AddAction.TO_POSITION) {
        initContentBorderAndIconBackground(content)
        contents.add(content)
        when (action) {
            AddAction.TO_POSITION -> {}

            AddAction.TO_CENTER -> {
                content.moveToCanvasCenter()
                initTranslateAndScale(content)
            }

            AddAction.EDIT_DONE -> {
            }

            AddAction.EDIT_CANCEL -> {
            }
        }
        selectContent(content, true)
        motionViewCallback.onContentAdded()
    }

    fun addContents(contents: List<BaseContent>, needToDraw: Boolean) {
        for (content in contents) {
            initContentBorderAndIconBackground(content)
            this.contents.add(content)
        }
        if (needToDraw) updateUI()
    }

    fun getContents(): List<BaseContent> = contents

    private fun initTranslateAndScale(content: BaseContent) {
        content.layer.scale = content.layer.initialScale
    }

    fun getFinalBitmap(bitmap: Bitmap): Bitmap {
        selectContent(null, false)
        val canvas = Canvas(bitmap)
        drawThemeColor(canvas)
        drawContentsForSave(canvas, bitmap)
        return bitmap
    }

    fun deleteSelectedContent() {
        selectedContent?.use { deleteContent(it) }
    }

    fun deleteContent(content: BaseContent) {
        if (contents.remove(content)) {
            unSelectContent()
            updateUI()
            motionViewCallback.onContentDeleted()
        } else {
            Log.e("MotionView", "Not found content to delete")
        }
    }

    fun reset() {
        contents.clear()
        selectContent(null, false)
        updateUI()
        motionViewCallback.onContentUnselected()
    }

    fun isReachMaxContents() = (contents.size == MAX_CONTENTS)

    fun isNoContent() = contents.isEmpty()

    private fun configDefaultIcons() {
        val rawDeleteIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_photo_delete)
        val rawRotateIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_photo_rotate)
        val iconSize = context.resources.getDimension(R.dimen.function_icon_size).toInt()
        val deleteIcon = Bitmap.createScaledBitmap(rawDeleteIcon, iconSize, iconSize, false)
        val rotateIcon = Bitmap.createScaledBitmap(rawRotateIcon, iconSize, iconSize, false)

        ArrayList<IconContent>().apply {
            add(IconContent(deleteIcon, IconContent.RIGHT_TOP))
            add(IconContent(rotateIcon, IconContent.LEFT_TOP))
            add(IconContent(rotateIcon, IconContent.LEFT_BOTTOM))
            add(IconContent(rotateIcon, IconContent.RIGHT_BOTTOM))
            icons.clear()
            forEach { icons.add(it) }
        }
    }

    private fun onIconContentClick(iconContent: IconContent?) {
        when (iconContent?.gravity) {
            IconContent.RIGHT_TOP -> deleteSelectedContent()
            else -> {}
        }
    }

    private fun drawAllContents(canvas: Canvas) {
        for (i in contents.indices) {
            contents[i].draw(canvas, null, icons, editorInfo)
        }
    }

    private fun drawContentsForSave(canvas: Canvas, bitmap: Bitmap) {
        for (i in contents.indices) {
            val scaledMatrixInfo = getScaledMatrixInfo(contents[i], bitmap)
            if (contents[i] is TextContent) {
                var tempContent = (contents[i].clone() as TextContent).apply {
                    matrix.setValues(scaledMatrixInfo)
                    updateLayer()
                }
                tempContent = getRedrawTextContent(tempContent)
                tempContent.drawForSave(canvas, editorInfo)
            } else {
                var tempContent = contents[i].clone() as ImageContent
                tempContent.matrix.setValues(scaledMatrixInfo)
//                saveImContent = redrawImageContent(tempContent) //todo: create high resolution later
                tempContent.drawForSave(canvas, editorInfo)
            }
        }
    }

    fun releaseAllContents() {
        Log.d("MotionView", "releaseAllContents START")
        contents.forEach { content -> content.release() }
        Log.d("MotionView", "releaseAllContents DONE")
    }

    private fun redrawImageContent(imageContent: ImageContent): ImageContent {
        val ratio = imageContent.layer.scale / imageContent.layer.initialScale
        val currentBitmapWidth = imageContent.bitmap.width
        val currentBitmapHeight = imageContent.bitmap.height
        var newBitmapWidth = currentBitmapWidth * ratio
        var newBitmapHeight = currentBitmapHeight * ratio

        // Limit sticker size when save image
        if (newBitmapWidth > ImageContent.MAX_SIZE_TO_SAVE) {
            newBitmapWidth = ImageContent.MAX_SIZE_TO_SAVE
            newBitmapHeight = newBitmapWidth * currentBitmapHeight / currentBitmapWidth
        } else if (newBitmapHeight > ImageContent.MAX_SIZE_TO_SAVE) {
            newBitmapHeight = ImageContent.MAX_SIZE_TO_SAVE
            newBitmapWidth = newBitmapHeight * currentBitmapWidth / currentBitmapHeight
        }

        val newScaleRatio = newBitmapWidth / currentBitmapWidth
//        val newBitmap = StickerUtil.getBitmapFromSVG(context, imageContent.resId, newBitmapWidth.toInt(), newBitmapHeight.toInt()) //svg case
        val newBitmap = BitmapFactory.decodeResource(context.resources, imageContent.resId)

        val newMatrixInfo = FloatArray(10)
        imageContent.matrix.getValues(newMatrixInfo)

        for (i in newMatrixInfo.indices) {
            if (i < 6) {
                when (i) {
                    2, 5 -> {//abort transform X - Y
                    }

                    else -> {
                        newMatrixInfo[i] /= newScaleRatio
                    }
                }
            } else break
        }

        val newImageContent = ImageContent(
            Layer().apply { initialScale = 1f * newBitmapWidth / this@MotionView.width },
            newBitmap,
            imageContent.resId,
            this.width,
            this.height,
        )
        newImageContent.matrix.setValues(newMatrixInfo)

        return newImageContent
    }

    private fun getScaledMatrixInfo(baseContent: BaseContent, bitmap: Bitmap): FloatArray {
        // Scale contents matrix
        val ratio = 1f * bitmap.width / this.width
        val matrixInfo = FloatArray(10)

        baseContent.matrix.getValues(matrixInfo)
        for (i in matrixInfo.indices) {
            if (i < 6) {
                matrixInfo[i] *= ratio
            }
        }
        return matrixInfo
    }

    private fun updateUI() {
        invalidate()
    }

    fun selectContent(content: BaseContent?, updateCallback: Boolean) {
        selectedContent?.isSelected = false
        content?.let {
            it.isSelected = true
            bringLayerToFront(it)
        }
        selectedContent = content
        if (updateCallback) {
            motionViewCallback.onContentSelected(content)
        }
        updateUI()
    }

    fun unSelectContent() {
        selectedContent?.let {
            selectContent(null, true)
            motionViewCallback.onContentUnselected()
        }
    }

    private fun findContentAtPoint(x: Float, y: Float): BaseContent? {
        var selected: BaseContent? = null
        val p = PointF(x, y)
        for (i in contents.indices.reversed()) {
            if (contents[i].pointInLayerRect(p, editorInfo)) {
                selected = contents[i]
                break
            }
        }
        return selected
    }

    private fun updateSelectionOnTap(e: MotionEvent) {
        val iconContent: IconContent? = findIconAtPoint(e.x, e.y)
        onIconContentClick(iconContent)
        when (val content: BaseContent? = findContentAtPoint(e.x, e.y)) {
            null -> unSelectContent()
            selectedContent -> motionViewCallback.onContentReselected()
            else -> {
                selectContent(content, true)
            }
        }
    }

    private fun findIconAtPoint(x: Float, y: Float): IconContent? {
        var selected: IconContent? = null
        selectedContent?.let {
            for (i in icons.indices.reversed()) {
                if (it.pointInLayerRectIcon(PointF(x, y), icons[i])) {
                    selected = icons[i]
                    break
                }
            }
        }
        return selected
    }

    private fun bringLayerToFront(content: BaseContent) {
        val pos = contents.indexOf(content)
        if (pos != contents.size - 1) {
            if (contents.remove(content)) {
                contents.add(content)
                updateUI()
            }
        }
    }

//    private fun moveContentToBack(content: MotionContent?) {
//        if (content == null) {
//            return
//        }
//        if (contents.remove(content)) {
//            contents.add(0, content)
//            updateUI()
//        }
//    }

//    private fun flipSelectedContent() {
//        if (selectedContent == null) {
//            return
//        }
//        selectedContent!!.layer.flip()
//        updateUI()
//    }

//    private fun moveSelectedToBack() {
//        moveContentToBack(selectedContent)
//    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _, event ->
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    selectContent(checkTouchEventWithContent(it), false)
                    if (selectedContent != null) motionViewCallback.onTouch()
                }

                MotionEvent.ACTION_DOWN -> {
                    val iconContent: IconContent? = findIconAtPoint(event.x, event.y)

                    when (iconContent?.gravity) {
                        IconContent.LEFT_TOP -> {
                            toggleOneFingerRotation(true)
                            motionViewCallback.onTouch()
                        }

                        IconContent.LEFT_BOTTOM -> {
                            toggleOneFingerRotation(true)
                            motionViewCallback.onTouch()
                        }

                        IconContent.RIGHT_BOTTOM -> {
                            toggleOneFingerRotation(true)
                            motionViewCallback.onTouch()
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (selectedContent != null) motionViewCallback.onTouch()
                }

                MotionEvent.ACTION_UP -> motionViewCallback.onRelease()

            }

            scaleGestureDetector.onTouchEvent(it)
            rotateGestureDetector.onTouchEvent(it)
            gestureDetectorCompat.onTouchEvent(it)
            moveGestureDetector.onTouchEvent(it)
        }
        true
    }

    private fun toggleOneFingerRotation(isEnabled: Boolean) {
        when (isEnabled) {
            true -> {
                rotateGestureDetector.contentCenterPoint = selectedContent?.currCenter
                setOnTouchListener(onRotateTouchListener)
            }

            else -> {
                rotateGestureDetector.contentCenterPoint = null
                setOnTouchListener(onTouchListener)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private val onRotateTouchListener = OnTouchListener { _, event ->
        event?.let {
            rotateGestureDetector.onTouchEvent(it)

            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_UP -> {
                    toggleOneFingerRotation(false)
                    motionViewCallback.onRelease()
                }

                MotionEvent.ACTION_POINTER_DOWN -> {
                    toggleOneFingerRotation(false)
                }
            }
        }
        true
    }

    private inner class TapsListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            selectedContent?.let { motionViewCallback.onContentDoubleTap(it) }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            updateSelectionOnTap(e)
            return true
        }
    }

    fun redrawAllTextContent() {
        for (position in contents.indices) {
            if (contents[position] is TextContent) {
                redrawTextContentAt(position)
            }
        }
    }

    private fun redrawTextContentAt(position: Int) {
        val currTextContent = contents[position] as TextContent
//        currTextContent.textLayer.apply {
//            val currFontSize = font.size
//            font.size = font.initialSize * scale / initialScale
//            // Limit text font size when redraw
//            if (font.size > Font.MAX_FONT_SIZE_FOR_PREVIEW) font.size = Font.MAX_FONT_SIZE_FOR_PREVIEW
//            if (currFontSize == font.size) return
//        }

        val newTextContent = currTextContent.clone().apply {
            moveCenterTo(currTextContent.currCenter)
            updateMatrix(editorInfo)
        }

        initContentBorderAndIconBackground(newTextContent)

        contents.removeAt(position)
        contents.add(position, newTextContent)
        if (selectedContent != null) selectContent(newTextContent, true)
    }

    private fun getRedrawTextContent(currTextContent: TextContent): TextContent {
//        currTextContent.textLayer.apply {
//            font.size = font.initialSize * scale / initialScale
//            // Limit text font size when save image
//            if (font.size > Font.MAX_FONT_SIZE_FOR_SAVE) font.size = Font.MAX_FONT_SIZE_FOR_SAVE
//        }

        val newTextContent = currTextContent.clone() as TextContent
        newTextContent.moveCenterTo(currTextContent.currCenter)
        return newTextContent
    }

    private fun initContentBorderAndIconBackground(content: BaseContent) {
        BorderUtil.initContentBorder(content, context, false)
        BorderUtil.initContentIconBackground(content)
    }

    private fun checkTouchEventWithContent(event: MotionEvent): BaseContent? {
        val px1 = event.getX(0)
        val py1 = event.getY(0)
        val px2 = event.getX(1)
        val py2 = event.getY(1)
        val slopeMainLine = calculateSlope(px1, py1, px2, py2)
        val bounceMainline = calculateAngledBounce(slopeMainLine, px1, py1, px2)

        for (content in contents.reversed()) {
            content.pointInLayerRect(PointF(0f, 0f), editorInfo)
            val pAx = content.destPoints[0]
            val pAy = content.destPoints[1]
            val pBx = content.destPoints[2]
            val pBy = content.destPoints[3]
            val pCx = content.destPoints[4]
            val pCy = content.destPoints[5]
            val pDx = content.destPoints[6]
            val pDy = content.destPoints[7]

            if (content.pointInLayerRect(PointF(px1, py1), editorInfo) || content.pointInLayerRect(PointF(px2, py2), editorInfo)) return content
            else {
                val intersectionAB: PointF
                val intersectionBC: PointF
                val intersectionCD: PointF
                val intersectionAD: PointF
                if (pAx == pDx && pAy == pBy) {
                    intersectionAB = calculateIntersectionBetweenMainLineAndVerticalAxis(slopeMainLine, bounceMainline, pAy)
                    intersectionCD = calculateIntersectionBetweenMainLineAndVerticalAxis(slopeMainLine, bounceMainline, pDy)
                    intersectionAD = calculateIntersectionBetweenMainLineAndHorizontalAxis(slopeMainLine, bounceMainline, pAx)
                    intersectionBC = calculateIntersectionBetweenMainLineAndHorizontalAxis(slopeMainLine, bounceMainline, pBx)
                } else {
                    val slopeAB = calculateSlope(pAx, pAy, pBx, pBy)
                    val bounceAB = calculateAngledBounce(slopeAB, pAx, pAy, pBx)
                    intersectionAB = calculateIntersectionBetweenMainLine(slopeAB, bounceAB, slopeMainLine, bounceMainline)

                    val slopeBC = calculateSlope(pBx, pBy, pCx, pCy)
                    val bounceBC = calculateAngledBounce(slopeBC, pCx, pCy, pBx)
                    intersectionBC = calculateIntersectionBetweenMainLine(slopeBC, bounceBC, slopeMainLine, bounceMainline)

                    val slopeCD = calculateSlope(pDx, pDy, pCx, pCy)
                    val bounceCD = calculateAngledBounce(slopeCD, pCx, pCy, pDx)
                    intersectionCD = calculateIntersectionBetweenMainLine(slopeCD, bounceCD, slopeMainLine, bounceMainline)

                    val slopeAD = calculateSlope(pAx, pAy, pDx, pDy)
                    val bounceAD = calculateAngledBounce(slopeAD, pAx, pAy, pDx)
                    intersectionAD = calculateIntersectionBetweenMainLine(slopeAD, bounceAD, slopeMainLine, bounceMainline)

                }
                if (checkIntersection(PointF(px1, py1), PointF(px2, py2), intersectionAB, PointF(pAx, pAy), PointF(pBx, pBy)) ||
                    checkIntersection(PointF(px1, py1), PointF(px2, py2), intersectionCD, PointF(pDx, pDy), PointF(pCx, pCy)) ||
                    checkIntersection(PointF(px1, py1), PointF(px2, py2), intersectionAD, PointF(pAx, pAy), PointF(pDx, pDy)) ||
                    checkIntersection(PointF(px1, py1), PointF(px2, py2), intersectionBC, PointF(pBx, pBy), PointF(pCx, pCy))
                ) return content
            }
        }
        return null
    }

    private fun handleTranslate(delta: PointF) {
        selectedContent?.let {
            // Limit content center movable area to motion view bounds
            // Allow user move content to motion view bounds after crop photo

            val oldCenterX = it.absoluteCenterX()
            val oldCenterY = it.absoluteCenterY()
            val newCenterX = oldCenterX + delta.x
            val newCenterY = oldCenterY + delta.y
            var needUpdateUI = false
            val isMoveToMotionViewX = (newCenterX in oldCenterX..0f) || (newCenterX in width.toFloat()..oldCenterX)
            val isMoveToMotionViewY = (newCenterY in oldCenterY..0f) || (newCenterY in height.toFloat()..oldCenterY)

            if ((newCenterX in 0f..width.toFloat()) || isMoveToMotionViewX) {
                it.layer.postTranslate(delta.x / width, 0f)
                needUpdateUI = true
            }

            if ((newCenterY in 0f..height.toFloat()) || isMoveToMotionViewY) {
                it.layer.postTranslate(0f, delta.y / height)
                needUpdateUI = true
            }

            if (needUpdateUI) {
                updateUI()
            }

            // Limit content movable area to motion view bounds

//            it.matrix.mapPoints(it.destPoints, it.srcPoints)
//            var isNeedUpdateUI = false
//            val newTopLeftX = PointF(it.destPoints[0] + delta.x, it.destPoints[1])
//            val newTopRightX = PointF(it.destPoints[2] + delta.x, it.destPoints[3])
//            val newBottomRightX = PointF(it.destPoints[4] + delta.x, it.destPoints[5])
//            val newBottomLeftX = PointF(it.destPoints[6] + delta.x, it.destPoints[7])
//            val newTopLeftY = PointF(it.destPoints[0], it.destPoints[1] + delta.y)
//            val newTopRightY = PointF(it.destPoints[2], it.destPoints[3] + delta.y)
//            val newBottomRightY = PointF(it.destPoints[4], it.destPoints[5] + delta.y)
//            val newBottomLeftY = PointF(it.destPoints[6], it.destPoints[7] + delta.y)
//
//            if ((isPointInMotionView(newTopLeftX) && isPointInMotionView(newTopRightX) && isPointInMotionView(newBottomRightX) && isPointInMotionView(newBottomLeftX))
//                    || isPointMovingToMotionViewX(newTopLeftX, delta.x) || isPointMovingToMotionViewX(newTopRightX, delta.x)
//                            || isPointMovingToMotionViewX(newBottomRightX, delta.x) || isPointMovingToMotionViewX(newBottomLeftX, delta.x)) {
//                it.layer.postTranslate(delta.x / this.width, 0f)
//                isNeedUpdateUI = true
//            }
//
//            if ((isPointInMotionView(newTopLeftY) && isPointInMotionView(newTopRightY) && isPointInMotionView(newBottomRightY) && isPointInMotionView(newBottomLeftY))
//                    || isPointMovingToMotionViewY(newTopLeftY, delta.y) || isPointMovingToMotionViewY(newTopRightY, delta.y)
//                    || isPointMovingToMotionViewY(newBottomRightY, delta.y) || isPointMovingToMotionViewY(newBottomLeftY, delta.y)) {
//                it.layer.postTranslate(0f, delta.y / this.height)
//                isNeedUpdateUI = true
//            }
//
//            if (isNeedUpdateUI) {
//                updateUI()
//            }
        }
    }

//    private fun isPointInMotionView(newPointF: PointF): Boolean {
//        return motionViewRectF.contains(newPointF.x, newPointF.y)
//    }
//
//    private fun isPointMovingToMotionViewX(newPointF: PointF, deltaX: Float): Boolean {
//        val oldPointF = PointF(newPointF.x - deltaX, newPointF.y)
//        return (newPointF.x in oldPointF.x..0f) || (newPointF.x in motionViewRectF.right..oldPointF.x)
//    }
//
//    private fun isPointMovingToMotionViewY(newPointF: PointF, deltaY: Float): Boolean {
//        val oldPointF = PointF(newPointF.x, newPointF.y - deltaY)
//        return (newPointF.y in oldPointF.y..0f) || (newPointF.y in motionViewRectF.bottom..oldPointF.y)
//    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            selectedContent?.let {
                val scaleFactorDiff = detector.scaleFactor
                it.layer.postScale(scaleFactorDiff - 1f)
                updateUI()
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            selectedContent.let {
                return true
            }
            return false
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            if (selectedContent is TextContent) {
                redrawTextContentAt(contents.indexOf(selectedContent!!))
            }
        }
    }

    private inner class RotateListener : RotateGestureDetector.SimpleOnRotateGestureListener() {
        var sumRotationDegreesDelta: Float = 0f
        override fun onRotate(detector: RotateGestureDetector): Boolean {
            sumRotationDegreesDelta += if (-detector.rotationDegreesDelta > 0) {
                -detector.rotationDegreesDelta
            } else {
                detector.rotationDegreesDelta
            }
            sumRotationDegreesDelta %= BaseContent.INITIAL_CONTENT_DEGREES_DELTA
            selectedContent?.let {
                if ((it.layer.rotationInDegrees in -BaseContent.NORMAL_DEGREES_DELTA..BaseContent.NORMAL_DEGREES_DELTA
                            || (it.layer.rotationInDegrees < -BaseContent.UNUSUAL_DEGREES_DELTA || it.layer.rotationInDegrees > BaseContent.UNUSUAL_DEGREES_DELTA))
                    && sumRotationDegreesDelta in -BaseContent.RANGE_CONTENT_DEGREES_DELTA..BaseContent.RANGE_CONTENT_DEGREES_DELTA
                ) {
                    it.layer.resetRotationInDegrees()
                    BorderUtil.initContentBorder(it, context, true)
                } else {
                    BorderUtil.initContentBorder(it, context, false)
                    it.layer.postRotate(-detector.rotationDegreesDelta)
                }
                updateUI()
            }
            return true
        }

        override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
            selectedContent?.let {
                return true
            }
            return false
        }

        override fun onRotateEnd(detector: RotateGestureDetector) {
            selectedContent?.let {
                BorderUtil.initContentBorder(it, context, false)
                updateUI()
            }
        }
    }

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        lateinit var previousTouchedPoint: PointF
        override fun onMove(detector: MoveGestureDetector): Boolean {
            selectedContent?.let {
                if (detector.getFocusDelta().x != 0f || detector.getFocusDelta().y != 0f) {
                    if (detector.getFocusDelta().x == 0f) {
                        if (checkValidMovement(convertToPositivePoint(previousTouchedPoint), convertToPositivePoint(PointF(1f, detector.getFocusDelta().y))))
                            handleTranslate(detector.getFocusDelta())
                    } else if (detector.getFocusDelta().y == 0f) {
                        if (checkValidMovement(convertToPositivePoint(previousTouchedPoint), convertToPositivePoint(PointF(detector.getFocusDelta().x, 1f))))
                            handleTranslate(detector.getFocusDelta())
                    } else if (checkValidMovement(convertToPositivePoint(previousTouchedPoint), convertToPositivePoint(detector.getFocusDelta()))) {
                        handleTranslate(detector.getFocusDelta())
                    }
                }
            }

            previousTouchedPoint = if (detector.getFocusDelta().x == 0f && detector.getFocusDelta().y == 0f)
                PointF(1f, 1f)
            else {
                when {
                    detector.getFocusDelta().x == 0f -> PointF(1f, detector.getFocusDelta().y)
                    detector.getFocusDelta().y == 0f -> PointF(detector.getFocusDelta().x, 1f)
                    else -> detector.getFocusDelta()
                }
            }
            return true
        }

        override fun onMoveBegin(detector: MoveGestureDetector): Boolean {
            previousTouchedPoint = PointF(1F, 1F)
            detector.mPrevEvent?.let {
                if (findIconAtPoint(it.x, it.y) != null) return false
                if (it.pointerCount < 2) {
                    if (findContentAtPoint(it.x, it.y) == null || findContentAtPoint(it.x, it.y) != selectedContent) {
                        unSelectContent()
                        return false
                    }
                }
            }

            selectedContent?.let {
                return true
            }
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {
            Log.i("MotionView", "onMoveEnd")
        }
    }

    enum class AddAction {
        TO_CENTER, TO_POSITION, EDIT_DONE, EDIT_CANCEL
    }

    enum class EditMode {
        CROP, CONTENT_EDIT
    }

    interface MotionViewCallback {
        fun onTouch()
        fun onRelease()
        fun onContentSelected(content: BaseContent?)
        fun onContentDeleted()
        fun onContentAdded()
        fun onContentReselected()
        fun onContentDoubleTap(content: BaseContent)
        fun onContentUnselected()
    }

    companion object {
        private const val MAX_CONTENTS = Int.MAX_VALUE
    }
}