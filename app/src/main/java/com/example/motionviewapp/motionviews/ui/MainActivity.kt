package com.example.motionviewapp.motionviews.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.motionviewapp.R
import com.example.motionviewapp.motionviews.ui.TextEditorDialogFragment.OnTextLayerCallback
import com.example.motionviewapp.motionviews.ui.adapter.FontsAdapter
import com.example.motionviewapp.motionviews.utils.FontProvider
import com.example.motionviewapp.motionviews.viewmodel.Font
import com.example.motionviewapp.motionviews.viewmodel.Layer
import com.example.motionviewapp.motionviews.viewmodel.TextLayer
import com.example.motionviewapp.motionviews.widget.MotionView
import com.example.motionviewapp.motionviews.widget.MotionView.MotionViewCallback
import com.example.motionviewapp.motionviews.widget.entity.ImageEntity
import com.example.motionviewapp.motionviews.widget.entity.MotionEntity
import com.example.motionviewapp.motionviews.widget.entity.TextEntity
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder

class MainActivity : AppCompatActivity(), OnTextLayerCallback {
    protected var motionView: MotionView? = null
    protected var textEntityEditPanel: View? = null
    private val motionViewCallback: MotionViewCallback = object : MotionViewCallback {
        override fun onEntitySelected(entity: MotionEntity?) {
            if (entity is TextEntity) {
                textEntityEditPanel!!.visibility = View.VISIBLE
            } else {
                textEntityEditPanel!!.visibility = View.GONE
            }
        }

        override fun onEntityDoubleTap(entity: MotionEntity) {
            startTextEntityEditing()
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
            val pica = BitmapFactory.decodeResource(resources, stickerResId)

            val entity = ImageEntity(layer, pica, motionView!!.width, motionView!!.height)
            motionView!!.addEntityAndPosition(entity)
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
            textEntity.layer.font.increaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity()
            motionView!!.invalidate()
        }
    }

    private fun decreaseTextEntitySize() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            textEntity.layer.font.decreaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity()
            motionView!!.invalidate()
        }
    }

    private fun changeTextEntityColor() {
        val textEntity = currentTextEntity() ?: return

        val initialColor = textEntity.layer.font.color

        ColorPickerDialogBuilder
            .with(this@MainActivity)
            .setTitle(R.string.select_color)
            .initialColor(initialColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(8) // magic number
            .setPositiveButton(R.string.ok) { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                val textEntity1 = currentTextEntity()
                if (textEntity1 != null) {
                    textEntity1.layer.font.color = selectedColor
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
                    textEntity.layer.font.typeface = fonts[which]
                    textEntity.updateEntity()
                    motionView!!.invalidate()
                }
            }
            .show()
    }

    private fun startTextEntityEditing() {
        val textEntity = currentTextEntity()
        if (textEntity != null) {
            val fragment = TextEditorDialogFragment.getInstance(textEntity.layer.text)
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
        if (item.itemId == R.id.main_add_sticker) {
            val intent = Intent(this, StickerSelectActivity::class.java)
            startActivityForResult(intent, SELECT_STICKER_REQUEST_CODE)
            return true
        } else if (item.itemId == R.id.main_add_text) {
            addTextSticker()
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun addTextSticker() {
        val textLayer = createTextLayer()
        val textEntity = TextEntity(
            textLayer, motionView!!.width,
            motionView!!.height, fontProvider!!
        )
        motionView!!.addEntityAndPosition(textEntity)

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

        font.color = TextLayer.Limits.INITIAL_FONT_COLOR
        font.size = TextLayer.Limits.INITIAL_FONT_SIZE
        font.typeface = fontProvider!!.defaultFontName

        textLayer.font = font

//        if (BuildConfig.DEBUG) {
            textLayer.text = "Hello, world :))"
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
            val textLayer = textEntity.layer
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
