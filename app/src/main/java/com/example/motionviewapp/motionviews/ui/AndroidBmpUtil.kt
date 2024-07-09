package com.example.motionviewapp.motionviews.ui

import android.graphics.Bitmap
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.Arrays

/**
 * Android Bitmap Object to .bmp image (Windows BMP v3 24bit) file util class
 *
 * ref : http://en.wikipedia.org/wiki/BMP_file_format
 *
 * @author ultrakain ( ultrasonic@gmail.com )
 * @since 2012-09-27
 */
class AndroidBmpUtil {
    private val BMP_WIDTH_OF_TIMES = 4
    private val BYTE_PER_PIXEL = 3

    /**
     * Android Bitmap Object to Window's v3 24bit Bmp Format File
     * @param orgBitmap
     * @param filePath
     * @return file saved result
     */
    fun save(orgBitmap: Bitmap?, filePath: String?): Boolean {
        if (orgBitmap == null) {
            return false
        }
        if (filePath == null) {
            return false
        }
        var isSaveSuccess = true

        //image size
        val width = orgBitmap.getWidth()
        val height = orgBitmap.getHeight()

        //image dummy data size
        //reason : bmp file's width equals 4's multiple
        var dummySize = 0
        var dummyBytesPerRow: ByteArray? = null
        var hasDummy = false
        if (isBmpWidth4Times(width)) {
            hasDummy = true
            dummySize = BMP_WIDTH_OF_TIMES - width % BMP_WIDTH_OF_TIMES
            dummyBytesPerRow = ByteArray(dummySize * BYTE_PER_PIXEL)
            for (i in dummyBytesPerRow.indices) {
                dummyBytesPerRow[i] = 0xFF.toByte()
            }
        }
        val pixels = IntArray(width * height)
        val imageSize = pixels.size * BYTE_PER_PIXEL + height * dummySize * BYTE_PER_PIXEL
        val imageDataOffset = 0x36
        val fileSize = imageSize + imageDataOffset

        //Android Bitmap Image Data
        orgBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        //ByteArrayOutputStream baos = new ByteArrayOutputStream(fileSize);
        val buffer = ByteBuffer.allocate(fileSize)
        try {
            /**
             * BITMAP FILE HEADER Write Start
             */
            buffer.put(0x42.toByte())
            buffer.put(0x4D.toByte())

            //size
            buffer.put(writeInt(fileSize))

            //reserved
            buffer.put(writeShort(0.toShort()))
            buffer.put(writeShort(0.toShort()))

            //image data start offset
            buffer.put(writeInt(imageDataOffset))
            /** BITMAP FILE HEADER Write End  */

            //*******************************************
            /** BITMAP INFO HEADER Write Start  */
            //size
            buffer.put(writeInt(0x28))

            //width, height
            buffer.put(writeInt(width))
            buffer.put(writeInt(height))

            //planes
            buffer.put(writeShort(1.toShort()))

            //bit count
            buffer.put(writeShort(24.toShort()))

            //bit compression
            buffer.put(writeInt(0))

            //image data size
            buffer.put(writeInt(imageSize))

            //horizontal resolution in pixels per meter
            buffer.put(writeInt(0))

            //vertical resolution in pixels per meter (unreliable)
            buffer.put(writeInt(0))

            //컬러 사용 유무
            buffer.put(writeInt(0))

            //중요하게 사용하는 색
            buffer.put(writeInt(0))
            /** BITMAP INFO HEADER Write End  */
            var row = height
            var startPosition = 0
            var endPosition = 0
            while (row > 0) {
                startPosition = (row - 1) * width
                endPosition = row * width
                for (i in startPosition until endPosition) {
                    buffer.put(write24BitForPixcel(pixels[i]))
                    if (hasDummy) {
                        if (isBitmapWidthLastPixcel(width, i)) {
                            buffer.put(dummyBytesPerRow)
                        }
                    }
                }
                row--
            }
            val fos = FileOutputStream(filePath)
            fos.write(buffer.array())
            fos.close()
        } catch (e1: IOException) {
            e1.printStackTrace()
            isSaveSuccess = false
        } finally {
        }
        return isSaveSuccess
    }

    fun getBmpByte(orgBitmap: Bitmap?): ByteArray? {
        if (orgBitmap == null) {
            return null
        }
        var isSaveSuccess = true

        //image size
        val width = orgBitmap.getWidth()
        val height = orgBitmap.getHeight()

        //image dummy data size
        //reason : bmp file's width equals 4's multiple
        var dummySize = 0
        var dummyBytesPerRow: ByteArray? = null
        var hasDummy = false
        if (isBmpWidth4Times(width)) {
            hasDummy = true
            dummySize = BMP_WIDTH_OF_TIMES - width % BMP_WIDTH_OF_TIMES
            dummyBytesPerRow = ByteArray(dummySize * BYTE_PER_PIXEL)
            Arrays.fill(dummyBytesPerRow, 0xFF.toByte())
        }
        val pixels = IntArray(width * height)
        val imageSize = pixels.size * BYTE_PER_PIXEL + height * dummySize * BYTE_PER_PIXEL
        val imageDataOffset = 0x36
        val fileSize = imageSize + imageDataOffset

        //Android Bitmap Image Data
        orgBitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        //ByteArrayOutputStream baos = new ByteArrayOutputStream(fileSize);
        val buffer = ByteBuffer.allocate(fileSize)
        return try {
            /**
             * BITMAP FILE HEADER Write Start
             */
            buffer.put(0x42.toByte())
            buffer.put(0x4D.toByte())

            //size
            buffer.put(writeInt(fileSize))

            //reserved
            buffer.put(writeShort(0.toShort()))
            buffer.put(writeShort(0.toShort()))

            //image data start offset
            buffer.put(writeInt(imageDataOffset))
            /** BITMAP FILE HEADER Write End  */

            //*******************************************
            /** BITMAP INFO HEADER Write Start  */
            //size
            buffer.put(writeInt(0x28))

            //width, height
            buffer.put(writeInt(width))
            buffer.put(writeInt(height))

            //planes
            buffer.put(writeShort(1.toShort()))

            //bit count
            buffer.put(writeShort(24.toShort()))

            //bit compression
            buffer.put(writeInt(0))

            //image data size
            buffer.put(writeInt(imageSize))

            //horizontal resolution in pixels per meter
            buffer.put(writeInt(0))

            //vertical resolution in pixels per meter (unreliable)
            buffer.put(writeInt(0))

            //컬러 사용 유무
            buffer.put(writeInt(0))

            //중요하게 사용하는 색
            buffer.put(writeInt(0))
            /** BITMAP INFO HEADER Write End  */
            var row = height
            var startPosition = 0
            var endPosition = 0
            while (row > 0) {
                startPosition = (row - 1) * width
                endPosition = row * width
                for (i in startPosition until endPosition) {
                    buffer.put(write24BitForPixcel(pixels[i]))
                    if (hasDummy) {
                        if (isBitmapWidthLastPixcel(width, i)) {
                            buffer.put(dummyBytesPerRow)
                        }
                    }
                }
                row--
            }
            buffer.array()
        } catch (e1: IOException) {
            e1.printStackTrace()
            isSaveSuccess = false
            null
        }
    }

    /**
     * Is last pixel in Android Bitmap width
     * @param width
     * @param i
     * @return
     */
    private fun isBitmapWidthLastPixcel(width: Int, i: Int): Boolean {
        return i > 0 && i % (width - 1) == 0
    }

    /**
     * BMP file is a multiples of 4?
     * @param width
     * @return
     */
    private fun isBmpWidth4Times(width: Int): Boolean {
        return width % BMP_WIDTH_OF_TIMES > 0
    }

    /**
     * Write integer to little-endian
     * @param value
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeInt(value: Int): ByteArray {
        val b = ByteArray(4)
        b[0] = (value and 0x000000FF).toByte()
        b[1] = (value and 0x0000FF00 shr 8).toByte()
        b[2] = (value and 0x00FF0000 shr 16).toByte()
        b[3] = (value and -0x1000000 shr 24).toByte()
        return b
    }

    /**
     * Write integer pixel to little-endian byte array
     * @param value
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun write24BitForPixcel(value: Int): ByteArray {
        val b = ByteArray(3)
        b[0] = (value and 0x000000FF).toByte()
        b[1] = (value and 0x0000FF00 shr 8).toByte()
        b[2] = (value and 0x00FF0000 shr 16).toByte()
        return b
    }

    /**
     * Write short to little-endian byte array
     * @param value
     * @return
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun writeShort(value: Short): ByteArray {
        val b = ByteArray(2)
        b[0] = (value.toInt() and 0x00FF).toByte()
        b[1] = (value.toInt() and 0xFF00 shr 8).toByte()
        return b
    }
}