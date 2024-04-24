package com.example.motionviewapp.motionviews.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import androidx.core.view.GestureDetectorCompat
import com.example.motionviewapp.R
import com.example.motionviewapp.motionviews.model.EditorInfo
import com.example.motionviewapp.motionviews.model.Layer
import com.example.motionviewapp.motionviews.widget.entity.IconEntity
import com.example.motionviewapp.motionviews.widget.entity.ImageEntity
import com.example.motionviewapp.motionviews.widget.entity.MotionEntity
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
import com.example.motionviewapp.motionviews.widget.entity.TextEntity
import kotlin.math.abs

class MotionView : FrameLayout {

    private val entities: MutableList<MotionEntity> = ArrayList()
    private val icons: MutableList<IconEntity> = ArrayList()
    private lateinit var motionViewCallback: MotionViewCallback
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var rotateGestureDetector: RotateGestureDetector
    private lateinit var moveGestureDetector: MoveGestureDetector
    private lateinit var gestureDetectorCompat: GestureDetectorCompat
    private lateinit var motionViewRectF: RectF
    var editorInfo = EditorInfo()
    var selectedEntity: MotionEntity? = null

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
        drawAllEntities(canvas)
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

    fun addEntity(entity: MotionEntity, action: AddAction = AddAction.TO_POSITION) {
        initEntityBorderAndIconBackground(entity)
        entities.add(entity)
        when (action) {
            AddAction.TO_POSITION -> {}

            AddAction.TO_CENTER -> {
                entity.moveToCanvasCenter()
                initTranslateAndScale(entity)
            }

            AddAction.EDIT_DONE -> {
            }

            AddAction.EDIT_CANCEL -> {
            }
        }
        selectEntity(entity, true)
        motionViewCallback.onEntityAdded()
    }

    fun addEntities(entityList: List<MotionEntity>, needToDraw: Boolean) {
        for (entity in entityList) {
            initEntityBorderAndIconBackground(entity)
            entities.add(entity)
        }
        if (needToDraw) updateUI()
    }

    fun getEntities(): List<MotionEntity> = entities

    private fun initTranslateAndScale(entity: MotionEntity) {
        entity.layer.scale = entity.layer.initialScale
    }

    fun getFinalBitmap(bitmap: Bitmap): Bitmap {
        selectEntity(null, false)
        val canvas = Canvas(bitmap)
        drawEntitiesForSave(canvas, bitmap)
        return bitmap
    }

    fun deleteSelectedEntity() {
        selectedEntity?.let {
            entities.remove(it)
            unSelectEntity()
            updateUI()
            motionViewCallback.onEntityDeleted()
        }
    }

    fun reset() {
        entities.clear()
        selectEntity(null, false)
        updateUI()
        motionViewCallback.onEntityUnselected()
    }

    fun isReachMaxEntities(): Boolean {
        return (entities.size == MAX_ENTITIES)
    }

    fun isEntitiesEmpty(): Boolean {
        return entities.isEmpty()
    }

    private fun configDefaultIcons() {
        val rawDeleteIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_photo_delete)
        val rawRotateIcon = BitmapFactory.decodeResource(resources, R.drawable.ic_photo_rotate)
        val iconSize = context.resources.getDimension(R.dimen.function_icon_size).toInt()
        val deleteIcon = Bitmap.createScaledBitmap(rawDeleteIcon, iconSize, iconSize, false)
        val rotateIcon = Bitmap.createScaledBitmap(rawRotateIcon, iconSize, iconSize, false)

        val iconEntities: MutableList<IconEntity> = ArrayList()
        iconEntities.add(IconEntity(deleteIcon, IconEntity.RIGHT_TOP))
        iconEntities.add(IconEntity(rotateIcon, IconEntity.LEFT_TOP))
        iconEntities.add(IconEntity(rotateIcon, IconEntity.LEFT_BOTTOM))
        iconEntities.add(IconEntity(rotateIcon, IconEntity.RIGHT_BOTTOM))
        icons.clear()
        iconEntities.forEach {
            icons.add(it)
        }
    }

    private fun selectIconEntity(iconEntity: IconEntity?) {
        when (iconEntity?.gravity) {
            IconEntity.RIGHT_TOP -> {
                deleteSelectedEntity()
            }
        }
    }

    private fun drawAllEntities(canvas: Canvas) {
        for (i in entities.indices) {
            entities[i].draw(canvas, null, icons, editorInfo)
        }
    }

    private fun drawEntitiesForSave(canvas: Canvas, bitmap: Bitmap) {
        for (i in entities.indices) {
            val scaledMatrixInfo = getScaledMatrixInfo(entities[i], bitmap)
            if (entities[i] is TextEntity) {
                var tempEntity = entities[i].clone() as TextEntity
                tempEntity.matrix.setValues(scaledMatrixInfo)
                tempEntity.updateLayer()
                tempEntity = getRedrawTextEntity(tempEntity)
                tempEntity.drawForSave(canvas, editorInfo)
//                tempEntity.release()
            } else {
                var saveImEntity = entities[i].clone() as ImageEntity
                saveImEntity.matrix.setValues(scaledMatrixInfo)
//                saveImEntity = redrawImageEntity(imageEntity) //todo: create high resolution later
                saveImEntity.drawForSave(canvas, editorInfo)
                //               imageEntity.release()
            }
        }
    }

    fun releaseAllEntities() {
        Log.d("MotionView", "releaseAllEntities START")
        for (i in entities.indices) {
            if (entities[i] is TextEntity)
                (entities[i] as TextEntity).release()
            else (entities[i] as ImageEntity).release()
        }
        Log.d("MotionView", "releaseAllEntities DONE")
    }

    private fun redrawImageEntity(imageEntity: ImageEntity): ImageEntity {
        val ratio = imageEntity.layer.scale / imageEntity.layer.initialScale
        val currentBitmapWidth = imageEntity.bitmap.width
        val currentBitmapHeight = imageEntity.bitmap.height
        var newBitmapWidth = currentBitmapWidth * ratio
        var newBitmapHeight = currentBitmapHeight * ratio

        // Limit sticker size when save image
        if (newBitmapWidth > ImageEntity.MAX_SIZE_TO_SAVE) {
            newBitmapWidth = ImageEntity.MAX_SIZE_TO_SAVE
            newBitmapHeight = newBitmapWidth * currentBitmapHeight / currentBitmapWidth
        } else if (newBitmapHeight > ImageEntity.MAX_SIZE_TO_SAVE) {
            newBitmapHeight = ImageEntity.MAX_SIZE_TO_SAVE
            newBitmapWidth = newBitmapHeight * currentBitmapWidth / currentBitmapHeight
        }

        val newScaleRatio = newBitmapWidth / currentBitmapWidth
//        val newBitmap = StickerUtil.getBitmapFromSVG(context, imageEntity.resId, newBitmapWidth.toInt(), newBitmapHeight.toInt()) //svg case
        val newBitmap = BitmapFactory.decodeResource(context.resources, imageEntity.resId)

        val newMatrixInfo = FloatArray(10)
        imageEntity.matrix.getValues(newMatrixInfo)

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

        val newImageEntity = ImageEntity(
            Layer().apply { initialScale = 1f * newBitmapWidth / this@MotionView.width },
            newBitmap,
            imageEntity.resId,
            this.width,
            this.height,
        )
        newImageEntity.matrix.setValues(newMatrixInfo)

        return newImageEntity
    }

    private fun getScaledMatrixInfo(motionEntity: MotionEntity, bitmap: Bitmap): FloatArray {
        // Scale entities matrix
        val ratio = 1f * bitmap.width / this.width
        val matrixInfo = FloatArray(10)

        motionEntity.matrix.getValues(matrixInfo)
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

    fun selectEntity(entity: MotionEntity?, updateCallback: Boolean) {
        selectedEntity?.isSelected = false
        entity?.let {
            it.isSelected = true
            bringLayerToFront(it)
        }
        selectedEntity = entity
        if (updateCallback) {
            motionViewCallback.onEntitySelected(entity)
        }
        updateUI()
    }

    fun unSelectEntity() {
        selectedEntity?.let {
            selectEntity(null, true)
            motionViewCallback.onEntityUnselected()
        }
    }

    private fun findEntityAtPoint(x: Float, y: Float): MotionEntity? {
        var selected: MotionEntity? = null
        val p = PointF(x, y)
        for (i in entities.indices.reversed()) {
            if (entities[i].pointInLayerRect(p, editorInfo)) {
                selected = entities[i]
                break
            }
        }
        return selected
    }

    private fun updateSelectionOnTap(e: MotionEvent) {
        val iconEntity: IconEntity? = findIconAtPoint(e.x, e.y)
        selectIconEntity(iconEntity)
        val entity: MotionEntity? = findEntityAtPoint(e.x, e.y)
        when (entity) {
            null -> unSelectEntity()
            selectedEntity -> motionViewCallback.onEntityReselected()
            else -> {
                selectEntity(entity, true)
            }
        }
    }

    private fun findIconAtPoint(x: Float, y: Float): IconEntity? {
        var selected: IconEntity? = null
        selectedEntity?.let {
            for (i in icons.indices.reversed()) {
                if (it.pointInLayerRectIcon(PointF(x, y), icons[i])) {
                    selected = icons[i]
                    break
                }
            }
        }
        return selected
    }

    private fun bringLayerToFront(entity: MotionEntity) {
        val pos = entities.indexOf(entity)
        if (pos != entities.size - 1) {
            if (entities.remove(entity)) {
                entities.add(entity)
                updateUI()
            }
        }
    }

//    private fun moveEntityToBack(entity: MotionEntity?) {
//        if (entity == null) {
//            return
//        }
//        if (entities.remove(entity)) {
//            entities.add(0, entity)
//            updateUI()
//        }
//    }

//    private fun flipSelectedEntity() {
//        if (selectedEntity == null) {
//            return
//        }
//        selectedEntity!!.layer.flip()
//        updateUI()
//    }

//    private fun moveSelectedToBack() {
//        moveEntityToBack(selectedEntity)
//    }

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = OnTouchListener { _, event ->
        event?.let {
            when (it.action and MotionEvent.ACTION_MASK) {
                MotionEvent.ACTION_POINTER_DOWN -> {
                    selectEntity(checkTouchEventWithEntity(it), false)
                    if (selectedEntity != null) motionViewCallback.onTouch()
                }

                MotionEvent.ACTION_DOWN -> {
                    val iconEntity: IconEntity? = findIconAtPoint(event.x, event.y)

                    when (iconEntity?.gravity) {
                        IconEntity.LEFT_TOP -> {
                            toggleOneFingerRotation(true)
                            motionViewCallback.onTouch()
                        }

                        IconEntity.LEFT_BOTTOM -> {
                            toggleOneFingerRotation(true)
                            motionViewCallback.onTouch()
                        }

                        IconEntity.RIGHT_BOTTOM -> {
                            toggleOneFingerRotation(true)
                            motionViewCallback.onTouch()
                        }
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    if (selectedEntity != null) motionViewCallback.onTouch()
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
                rotateGestureDetector.entityCenterPoint = selectedEntity?.currCenter
                setOnTouchListener(onRotateTouchListener)
            }

            else -> {
                rotateGestureDetector.entityCenterPoint = null
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
            selectedEntity?.let {
                motionViewCallback.onEntityDoubleTap(it)
            }
            return true
        }

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            updateSelectionOnTap(e)
            return true
        }
    }

    fun redrawAllTextEntity() {
        for (position in entities.indices) {
            if (entities[position] is TextEntity) {
                redrawTextEntityAt(position)
            }
        }
    }

    private fun redrawTextEntityAt(position: Int) {
        val currTextEntity = entities[position] as TextEntity
//        currTextEntity.textLayer.apply {
//            val currFontSize = font.size
//            font.size = font.initialSize * scale / initialScale
//            // Limit text font size when redraw
//            if (font.size > Font.MAX_FONT_SIZE_FOR_PREVIEW) font.size = Font.MAX_FONT_SIZE_FOR_PREVIEW
//            if (currFontSize == font.size) return
//        }

        val newTextEntity = currTextEntity.clone()
        newTextEntity.moveCenterTo(currTextEntity.currCenter)
        newTextEntity.updateMatrix(editorInfo)

        initEntityBorderAndIconBackground(newTextEntity)

        entities.removeAt(position)
        entities.add(position, newTextEntity)
        if (selectedEntity != null) selectEntity(newTextEntity, true)
    }

    private fun getRedrawTextEntity(currTextEntity: TextEntity): TextEntity {
//        currTextEntity.textLayer.apply {
//            font.size = font.initialSize * scale / initialScale
//            // Limit text font size when save image
//            if (font.size > Font.MAX_FONT_SIZE_FOR_SAVE) font.size = Font.MAX_FONT_SIZE_FOR_SAVE
//        }

        val newTextEntity = currTextEntity.clone() as TextEntity
        newTextEntity.moveCenterTo(currTextEntity.currCenter)
        return newTextEntity
    }

    private fun initEntityBorderAndIconBackground(entity: MotionEntity) {
        BorderUtil.initEntityBorder(entity, context, false)
        BorderUtil.initEntityIconBackground(entity)
    }

    private fun checkTouchEventWithEntity(event: MotionEvent): MotionEntity? {
        val px1 = event.getX(0)
        val py1 = event.getY(0)
        val px2 = event.getX(1)
        val py2 = event.getY(1)
        val slopeMainLine = calculateSlope(px1, py1, px2, py2)
        val bounceMainline = calculateAngledBounce(slopeMainLine, px1, py1, px2)

        for (entity in entities.reversed()) {
            entity.pointInLayerRect(PointF(0f, 0f), editorInfo)
            val pAx = entity.destPoints[0]
            val pAy = entity.destPoints[1]
            val pBx = entity.destPoints[2]
            val pBy = entity.destPoints[3]
            val pCx = entity.destPoints[4]
            val pCy = entity.destPoints[5]
            val pDx = entity.destPoints[6]
            val pDy = entity.destPoints[7]

            if (entity.pointInLayerRect(PointF(px1, py1), editorInfo)
                || entity.pointInLayerRect(PointF(px2, py2), editorInfo)
            )
                return entity
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
                ) return entity
            }
        }
        return null
    }

    private fun handleTranslate(delta: PointF) {
        // TODO - SHRC: Need to discuss more about entity behavior when user interact with it
        selectedEntity?.let {
            // Limit entity center movable area to motion view bounds
            // Allow user move entity to motion view bounds after crop photo

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

            // Limit entity movable area to motion view bounds

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
            selectedEntity?.let {
                val scaleFactorDiff = detector.scaleFactor
                it.layer.postScale(scaleFactorDiff - 1f)
                updateUI()
            }
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            selectedEntity.let {
                return true
            }
            return false
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            super.onScaleEnd(detector)
            if (selectedEntity is TextEntity) {
                redrawTextEntityAt(entities.indexOf(selectedEntity!!))
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
            sumRotationDegreesDelta %= MotionEntity.INITIAL_ENTITY_DEGREES_DELTA
            selectedEntity?.let {
                if ((it.layer.rotationInDegrees in -MotionEntity.NORMAL_DEGREES_DELTA..MotionEntity.NORMAL_DEGREES_DELTA
                            || (it.layer.rotationInDegrees < -MotionEntity.UNUSUAL_DEGREES_DELTA || it.layer.rotationInDegrees > MotionEntity.UNUSUAL_DEGREES_DELTA))
                    && sumRotationDegreesDelta in -MotionEntity.RANGE_ENTITY_DEGREES_DELTA..MotionEntity.RANGE_ENTITY_DEGREES_DELTA
                ) {
                    it.layer.resetRotationInDegrees()
                    BorderUtil.initEntityBorder(it, context, true)
                } else {
                    BorderUtil.initEntityBorder(it, context, false)
                    it.layer.postRotate(-detector.rotationDegreesDelta)
                }
                updateUI()
            }
            return true
        }

        override fun onRotateBegin(detector: RotateGestureDetector): Boolean {
            selectedEntity?.let {
                return true
            }
            return false
        }

        override fun onRotateEnd(detector: RotateGestureDetector) {
            selectedEntity?.let {
                BorderUtil.initEntityBorder(it, context, false)
                updateUI()
            }
        }
    }

    private inner class MoveListener : MoveGestureDetector.SimpleOnMoveGestureListener() {
        lateinit var previousTouchedPoint: PointF
        override fun onMove(detector: MoveGestureDetector): Boolean {
            selectedEntity?.let {
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
                    if (findEntityAtPoint(it.x, it.y) == null || findEntityAtPoint(it.x, it.y) != selectedEntity) {
                        unSelectEntity()
                        return false
                    }
                }
            }

            selectedEntity?.let {
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
        fun onEntitySelected(entity: MotionEntity?)
        fun onEntityDeleted()
        fun onEntityAdded()
        fun onEntityReselected()
        fun onEntityDoubleTap(entity: MotionEntity)
        fun onEntityUnselected()
    }

    companion object {
        private const val MAX_ENTITIES = Int.MAX_VALUE
    }
}