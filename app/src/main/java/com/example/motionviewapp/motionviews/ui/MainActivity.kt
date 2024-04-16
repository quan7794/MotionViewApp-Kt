package com.example.motionviewapp.motionviews.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.motionviewapp.R
import com.example.motionviewapp.motionviews.ui.TextEditorDialogFragment.OnTextLayerCallback
import com.example.motionviewapp.motionviews.ui.adapter.FontsAdapter
import com.example.motionviewapp.utils.FontProvider
import com.example.motionviewapp.motionviews.model.Font
import com.example.motionviewapp.motionviews.model.Layer
import com.example.motionviewapp.motionviews.model.TextLayer
import com.example.motionviewapp.motionviews.widget.MotionView
import com.example.motionviewapp.motionviews.widget.MotionView.MotionViewCallback
import com.example.motionviewapp.motionviews.widget.entity.ImageEntity
import com.example.motionviewapp.motionviews.widget.entity.MotionEntity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import com.example.motionviewapp.motionviews.widget.entity.TextEntity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity(), OnTextLayerCallback {
    protected var motionView: MotionView? = null
    protected var textEntityEditPanel: View? = null
    private val motionViewCallback: MotionViewCallback = object : MotionViewCallback {
        override fun onTouch() {
        }

        override fun onRelease() {
        }

        override fun onEntitySelected(entity: MotionEntity?) {
            if (entity is TextEntity) {
                textEntityEditPanel!!.visibility = View.VISIBLE
            } else {
                textEntityEditPanel!!.visibility = View.GONE
            }
        }

        override fun onEntityDeleted() {
        }

        override fun onEntityAdded() {
        }

        override fun onEntityReselected() {
        }

        override fun onEntityDoubleTap(entity: MotionEntity) {
            startTextEntityEditing()
        }

        override fun onEntityUnselected() {
        }
    }
    private var fontProvider: FontProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.fontProvider = FontProvider(resources)

        motionView = findViewById(R.id.main_motion_view)
        textEntityEditPanel = findViewById(R.id.main_motion_text_entity_edit_panel)
        motionView!!.setMotionViewCallback(motionViewCallback)

        addSticker(R.drawable.pikachu_2)

        initTextEntitiesListeners()
    }

    private fun addSticker(stickerResId: Int) {
        motionView!!.post {
            val layer = Layer()
            val bitmap = BitmapFactory.decodeResource(resources, stickerResId)

            val entity = ImageEntity(layer, bitmap, stickerResId, motionView!!.width, motionView!!.height)
            motionView!!.addEntity(entity, MotionView.AddAction.TO_CENTER)
        }
    }

    private fun initTextEntitiesListeners() {
        findViewById<View>(R.id.text_entity_font_size_increase).setOnClickListener { view: View? -> increaseTextEntitySize() }
        findViewById<View>(R.id.text_entity_font_size_decrease).setOnClickListener { view: View? -> decreaseTextEntitySize() }
        findViewById<View>(R.id.text_entity_color_change).setOnClickListener { view: View? -> changeTextEntityColor() }
        findViewById<View>(R.id.text_entity_font_change).setOnClickListener { view: View? -> changeTextEntityFont() }
        findViewById<View>(R.id.text_entity_edit).setOnClickListener { view: View? -> startTextEntityEditing() }
    }

    private fun increaseTextEntitySize() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            textEntity.textLayer.font.increaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity()
            motionView!!.invalidate()
        }
    }

    private fun decreaseTextEntitySize() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            textEntity.textLayer.font.decreaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity()
            motionView!!.invalidate()
        }
    }

    private fun changeTextEntityColor() {
        val textEntity = currentTextEntity() ?: return

        val initialColor = textEntity.textLayer.font.color

        ColorPickerDialogBuilder
            .with(this@MainActivity)
            .setTitle(R.string.select_color)
            .initialColor(initialColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(8) // magic number
            .setPositiveButton(R.string.ok) { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                val textEntity1 = currentTextEntity()
                if (textEntity1 != null) {
                    textEntity1.textLayer.font.color = selectedColor
                    textEntity1.updateEntity()
                    motionView!!.invalidate()
                }
            }
            .setNegativeButton(R.string.cancel) { dialog: DialogInterface?, which: Int -> }
            .build()
            .show()
    }

    private fun changeTextEntityFont() {
        val fonts = fontProvider!!.fontNames
        val fontsAdapter = FontsAdapter(this, fonts, fontProvider!!)
        AlertDialog.Builder(this)
            .setTitle(R.string.select_font)
            .setAdapter(fontsAdapter) { dialogInterface: DialogInterface?, which: Int ->
                val textEntity = currentTextEntity()
                if (textEntity != null) {
                    textEntity.textLayer.font.typefaceName = fonts[which]
                    textEntity.updateEntity()
                    motionView!!.invalidate()
                }
            }
            .show()
    }

    private fun startTextEntityEditing() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            val fragment = TextEditorDialogFragment.getInstance(textEntity.textLayer.text)
            fragment.show(supportFragmentManager, TextEditorDialogFragment::class.java.name)
        }
    }

    private fun currentTextEntity(): TextEntity? {
        return if (motionView != null && motionView!!.selectedEntity is TextEntity) {
            motionView!!.selectedEntity as TextEntity
        } else {
            null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.main_add_sticker -> {
                val intent = Intent(this, StickerSelectActivity::class.java)
                startActivityForResult(intent, SELECT_STICKER_REQUEST_CODE)
                return true
            }

            R.id.main_add_text -> addTextSticker()
            R.id.main_save -> saveImage()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveImage() {
        val rootBitmap = Bitmap.createBitmap(3840, 2160, Bitmap.Config.ARGB_8888)
        val outputBm = motionView?.getFinalBitmap(rootBitmap)

        val file = File(cacheDir, "out.jpg")
        try {
            FileOutputStream(file).use { outputBm?.compress(Bitmap.CompressFormat.PNG, 100, it) }
            Toast.makeText(this, "Done: ${file.path}", Toast.LENGTH_LONG).show()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    protected fun addTextSticker() {
        val textLayer = createTextLayer()
        val textEntity = TextEntity(textLayer, motionView!!.width, motionView!!.height, fontProvider!!)
        motionView!!.addEntity(textEntity, MotionView.AddAction.TO_CENTER)

        // move text sticker up so that its not hidden under keyboard
        val center = textEntity.absoluteCenter()
        center.y = center.y * 0.5f
        textEntity.moveCenterTo(center)

        // redraw
        motionView!!.invalidate()

        startTextEntityEditing()
    }

    private fun createTextLayer(): TextLayer {
        val textLayer = TextLayer()
        val font = Font()

        font.color = Font.DEFAULT_TEXT_COLOR
        font.size = Font.INITIAL_FONT_SIZE
        font.typefaceName = fontProvider!!.defaultFontName

        textLayer.font = font

//        if (BuildConfig.DEBUG) {
//            textLayer.text = "Hello, world :))"
//        }

        return textLayer
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_STICKER_REQUEST_CODE) {
                if (data != null) {
                    val stickerId = data.getIntExtra(StickerSelectActivity.EXTRA_STICKER_ID, 0)
                    if (stickerId != 0) {
                        addSticker(stickerId)
                    }
                }
            }
        }
    }

    override fun textChanged(text: String) {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            val textLayer = textEntity.textLayer
            if (text != textLayer.text) {
                textLayer.text = text
                textEntity.updateEntity()
                motionView!!.invalidate()
            }
        }
    }

    companion object {
        const val SELECT_STICKER_REQUEST_CODE: Int = 123
    }
}
