package com.example.motionviewapp.motionviews.widget.content

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import com.example.motionviewapp.motionviews.model.EditorInfo
import com.example.motionviewapp.motionviews.model.Layer
import com.example.motionviewapp.motionviews.widget.MotionView
import com.example.motionviewapp.utils.MathUtils

abstract class BaseContent(
    open var layer: Layer,
    protected open var canvasWidth: Int,
    protected open var canvasHeight: Int
) : AutoCloseable {
    var matrix = Matrix() // Apply to motion view
    private var orgPhotoMatrix = Matrix() // Apply to origin photo
    protected var holyScale = 0f
    val destPoints = FloatArray(10) // x0, y0, x1, y1, x2, y2, x3, y3, x0, y0
    val srcPoints = FloatArray(10)
    var currCenter = PointF()
    var isSelected = false
    private var borderPaint = Paint()
    private var closePaint = Paint()
    private var iconBackgroundPaint = Paint()
    protected var bitmapWidth = 0f

//    fun updateEntityForOrientationChanged(newWidth: Int, newHeight: Int) {
//        canvasWidth = newWidth
//        canvasHeight = newHeight
//        holyScale = newWidth / bitmapWidth
//    }

    fun updateMatrix(editorInfo: EditorInfo) {

        /* Decor -> Update layer info match with touch event -> update matrix via layer -> update orgPhotoMatrix via matrix (convert coordinates)
           Crop -> Update matrix match with crop rect via orgPhotoMatrix (convert coordinates) -> update layer info via matrix */

        when (editorInfo.editMode) {
            MotionView.EditMode.CONTENT_EDIT -> {
                matrix.reset()
                orgPhotoMatrix.reset()

                // Calculate layer params
                val topLeftX = layer.x * canvasWidth
                val topLeftY = layer.y * canvasHeight
                val centerX = topLeftX + bmWidth * holyScale * 0.5f
                val centerY = topLeftY + bmHeight * holyScale * 0.5f
                currCenter = PointF(centerX, centerY)
                var rotationInDegree = layer.rotationInDegrees
                var scaleX = layer.scale
                val scaleY = layer.scale
                if (layer.isFlipped) {
                    // flip (by X-coordinate) if needed
                    rotationInDegree *= -1.0f
                    scaleX *= -1.0f
                }

                matrix.preScale(scaleX, scaleY, centerX, centerY)
                matrix.preRotate(rotationInDegree, centerX, centerY)
                matrix.preTranslate(topLeftX, topLeftY)
                matrix.preScale(holyScale, holyScale)

                // Convert coordinate based on motion view to coordinate based on original photo
                val matrixInfo = FloatArray(10)
                matrix.getValues(matrixInfo)
                for (i in matrixInfo.indices) {
                    if (i < 6) {
                        matrixInfo[i] *= editorInfo.ratio
                    } else break
                }

                matrixInfo[2] = matrixInfo[2] + editorInfo.left
                matrixInfo[5] = matrixInfo[5] + editorInfo.top
                orgPhotoMatrix.setValues(matrixInfo)
            }

            MotionView.EditMode.CROP -> {
                // Convert coordinate based on original photo to coordinate based on motion view
                val orgPhotoMatrixInfo = FloatArray(10)
                orgPhotoMatrix.getValues(orgPhotoMatrixInfo)
                orgPhotoMatrixInfo[2] = orgPhotoMatrixInfo[2] - editorInfo.left
                orgPhotoMatrixInfo[5] = orgPhotoMatrixInfo[5] - editorInfo.top

                for (i in orgPhotoMatrixInfo.indices) {
                    if (i < 6) {
                        orgPhotoMatrixInfo[i] *= 1.0f / editorInfo.ratio
                    } else break
                }
                matrix.setValues(orgPhotoMatrixInfo)

                // Recalculate layer params
                updateLayer()
            }
        }
    }

    fun updateLayer() {
        val centerPoint = centerPointOfRect(matrix)
        currCenter = centerPoint
        val topLeftX = centerPoint.x - bmWidth * holyScale * 0.5f
        val topLeftY = centerPoint.y - bmHeight * holyScale * 0.5f
        layer.x = topLeftX / canvasWidth
        layer.y = topLeftY / canvasHeight
        layer.scale = 1.0f * rectWidth(matrix) / (this.bmWidth * holyScale)
    }

    fun absoluteCenterX(): Float {
        val topLeftX: Float = layer.x * canvasWidth
        return topLeftX + bmWidth * holyScale * 0.5f
    }

    fun absoluteCenterY(): Float {
        val topLeftY: Float = layer.y * canvasHeight
        return topLeftY + bmHeight * holyScale * 0.5f
    }

    fun absoluteCenter(): PointF {
        val topLeftX: Float = layer.x * canvasWidth
        val topLeftY: Float = layer.y * canvasHeight
        val centerX = topLeftX + bmWidth * holyScale * 0.5f
        val centerY = topLeftY + bmHeight * holyScale * 0.5f
        return PointF(centerX, centerY)
    }

    fun moveToCanvasCenter() {
        moveCenterTo(PointF(canvasWidth * 0.5f, canvasHeight * 0.5f))
    }

    fun moveCenterTo(newCenter: PointF) {
        val currentCenter = absoluteCenter()
        layer.postTranslate(
                1.0f * (newCenter.x - currentCenter.x) / canvasWidth,
                1.0f * (newCenter.y - currentCenter.y) / canvasHeight
        )
    }

    fun moveTopLeftCornerTo() {
        val centerPoint = absoluteCenter()
        val newCenterX = bmWidth * layer.scale * 0.5f  + layer.x
        val newCenterY = bmHeight * layer.scale * 0.5f  + layer.y
        moveCenterTo(PointF(newCenterX, newCenterY))
//        val centerPoint = absoluteCenter()
//        val newTopLeftX = position.x - bmWidth * holyScale * 0.5f
//        val newTopLeftY = position.y - bmHeight * holyScale * 0.5f
//
//        // Di chuyển đến vị trí mới
//        layer.postTranslate((newTopLeftX - centerPoint.x) / canvasWidth, (newTopLeftY - centerPoint.y) / canvasHeight)
    }

    private val pA = PointF()
    private val pB = PointF()
    private val pC = PointF()
    private val pD = PointF()

    fun pointInLayerRect(point: PointF, editorInfo: EditorInfo): Boolean {
        updateMatrix(editorInfo)
        // map rect vertices
        matrix.mapPoints(destPoints, srcPoints)
        pA.x = destPoints[0]
        pA.y = destPoints[1]
        pB.x = destPoints[2]
        pB.y = destPoints[3]
        pC.x = destPoints[4]
        pC.y = destPoints[5]
        pD.x = destPoints[6]
        pD.y = destPoints[7]
        return MathUtils.pointInTriangle(point, pA, pB, pC)
                || MathUtils.pointInTriangle(point, pA, pD, pC)

    }

    fun pointInLayerRectIcon(point: PointF, iconContent: IconContent): Boolean {
        iconContent.pA.x = iconContent.destPoints[0]
        iconContent.pA.y = iconContent.destPoints[1]
        iconContent.pB.x = iconContent.destPoints[2]
        iconContent.pB.y = iconContent.destPoints[3]
        iconContent.pC.x = iconContent.destPoints[4]
        iconContent.pC.y = iconContent.destPoints[5]
        iconContent.pD.x = iconContent.destPoints[6]
        iconContent.pD.y = iconContent.destPoints[7]
        return MathUtils.pointInTriangle(point, iconContent.pA, iconContent.pB, iconContent.pC) ||
                MathUtils.pointInTriangle(point, iconContent.pA, iconContent.pD, iconContent.pC)
    }

    private fun centerPointOfRect(matrix: Matrix): PointF {
        matrix.mapPoints(destPoints, srcPoints)

        pA.x = destPoints[0]
        pA.y = destPoints[1]
        pC.x = destPoints[4]
        pC.y = destPoints[5]

        return PointF((pA.x + pC.x) / 2, (pA.y + pC.y) / 2)
    }

    private fun rectWidth(matrix: Matrix): Float {
        matrix.mapPoints(destPoints, srcPoints)

        pA.x = destPoints[0]
        pA.y = destPoints[1]
        pB.x = destPoints[2]
        pB.y = destPoints[3]

        return distanceOfTwoPoints(PointF(pA.x, pA.y), PointF(pB.x, pB.y))
    }

    private fun rectHeight(matrix: Matrix): Float {
        matrix.mapPoints(destPoints, srcPoints)

        pB.x = destPoints[2]
        pB.y = destPoints[3]
        pC.x = destPoints[4]
        pC.y = destPoints[5]

        return distanceOfTwoPoints(PointF(pB.x, pB.y), PointF(pC.x, pC.y))
    }

    private fun distanceOfTwoPoints(point1: PointF, point2: PointF): Float {
        return Math.sqrt(((point2.y - point1.y) * (point2.y - point1.y) + (point2.x - point1.x) * (point2.x - point1.x)).toDouble()).toFloat()
    }

    fun draw(canvas: Canvas, drawingPaint: Paint?, icons: MutableList<IconContent>, editorInfo: EditorInfo) {
        updateMatrix(editorInfo)
        canvas.save()
        drawContent(canvas, drawingPaint)
        if (isSelected) { // get alpha from drawingPaint
            val storedAlpha = borderPaint.alpha
            val closeStoredAlpha = closePaint.alpha
            if (drawingPaint != null) {
                borderPaint.alpha = drawingPaint.alpha
                closePaint.alpha = drawingPaint.alpha
            }
            drawSelectedBg(canvas)
            drawIcons(canvas, icons)

            // restore border alpha
            borderPaint.alpha = storedAlpha
            closePaint.alpha = closeStoredAlpha
        }
        canvas.restore()
    }

    fun drawForSave(canvas: Canvas, editorInfo: EditorInfo) {
        if (this is TextContent) {
            updateMatrix(editorInfo)
        }
        canvas.save()
        drawContent(canvas, null)
        canvas.restore()
        release()
    }

    private fun drawSelectedBg(canvas: Canvas) {
        matrix.mapPoints(destPoints, srcPoints)
        val path = Path()
        path.moveTo(destPoints[2], destPoints[3])
        path.lineTo(destPoints[4], destPoints[5])
        path.lineTo(destPoints[6], destPoints[7])
        path.lineTo(destPoints[0], destPoints[1])
        path.lineTo(destPoints[2], destPoints[3])
        canvas.drawPath(path, borderPaint)
    }

    private fun drawIcons(canvas: Canvas, icons: MutableList<IconContent>) {
        var x = 0f
        var y = 0f
        for (iconEntity in icons) {
            when (iconEntity.gravity) {
                IconContent.LEFT_TOP -> {
                    x = destPoints[0]
                    y = destPoints[1]
                }

                IconContent.RIGHT_BOTTOM -> {
                    x = destPoints[4]
                    y = destPoints[5]
                }

                IconContent.LEFT_BOTTOM -> {
                    x = destPoints[6]
                    y = destPoints[7]
                }

                IconContent.RIGHT_TOP -> {
                    x = destPoints[2]
                    y = destPoints[3]
                }
            }
            configIconMatrix(iconEntity, x, y)
            canvas.drawBitmap(iconEntity.bitmapIcon, iconEntity.matrix, null)
        }
    }

    private fun configIconMatrix(iconContent: IconContent, x: Float, y: Float) {
        iconContent.matrix.reset()
        val topLeftX: Float = x - iconContent.width / 2
        val topLeftY: Float = y - iconContent.height / 2
        // calculate params
        val rotationInDegree: Float = layer.rotationInDegrees

        iconContent.matrix.preRotate(rotationInDegree, x, y)
        iconContent.matrix.preTranslate(topLeftX, topLeftY)
        iconContent.matrix.mapPoints(iconContent.destPoints, iconContent.srcPoints)
    }

    fun setBorderPaint(borderPaint: Paint) {
        this.borderPaint = borderPaint
    }

    fun setIconBackground(iconBackground: Paint) {
        this.iconBackgroundPaint = iconBackground
    }

    protected abstract fun drawContent(canvas: Canvas, drawingPaint: Paint?)
    abstract val bmWidth: Int
    abstract val bmHeight: Int
    abstract fun clone(): BaseContent
    abstract fun release()

    override fun close() {
        release()
    }

    companion object {
        const val NORMAL_DEGREES_DELTA = 5f
        const val UNUSUAL_DEGREES_DELTA = 355f
        const val INITIAL_CONTENT_DEGREES_DELTA = 30f
        const val RANGE_CONTENT_DEGREES_DELTA = 20f
        const val RATIO_POINTER = 10f
    }
}