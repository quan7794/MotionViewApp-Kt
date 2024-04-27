package com.example.motionviewapp.motionviews.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.example.motionviewapp.R
import com.example.motionviewapp.ePaper.ext.readAssets
import com.example.motionviewapp.ePaper.parseTemplate
import com.example.motionviewapp.motionviews.model.TextLayer
import com.example.motionviewapp.motionviews.ui.TextEditorDialogFragment.OnTextLayerCallback
import com.example.motionviewapp.motionviews.ui.adapter.FontsAdapter
import com.example.motionviewapp.motionviews.widget.MotionView
import com.example.motionviewapp.motionviews.widget.MotionView.MotionViewCallback
import com.example.motionviewapp.motionviews.widget.content.BaseContent
import com.example.motionviewapp.motionviews.widget.content.TextContent
import com.example.motionviewapp.utils.FontProvider
import com.example.motionviewapp.utils.addImageContent
import com.example.motionviewapp.utils.addTextContent
import com.example.motionviewapp.utils.setCurrentTextFont
import com.example.motionviewapp.utils.currentTextEntity
import com.example.motionviewapp.utils.getBitmap
import com.example.motionviewapp.utils.importEdpTemplate
import com.example.motionviewapp.utils.saveImage
import com.example.motionviewapp.utils.setCurrentTextColor
import com.example.motionviewapp.utils.setImageForSelectedContent
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import timber.log.Timber

class MainActivity : AppCompatActivity(), OnTextLayerCallback {
    protected var motionView: MotionView? = null
    protected var textEntityEditPanel: View? = null

//    private val epd = EPDTemplate()

    private val motionViewCallback: MotionViewCallback = object : MotionViewCallback {
        override fun onTouch() {
        }

        override fun onRelease() {
        }

        override fun onEntitySelected(entity: BaseContent?) {
            if (entity is TextContent) {
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

        override fun onEntityDoubleTap(entity: BaseContent) {
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

        initEpdTemplateData()
        initClickListeners()
    }

    private fun initEpdTemplateData() {
        Timber.tag("AAA").d("addEpdTemplateContent Entry")
        val filenameTemplate = "L_05_006_M(4)_ALL" + ".xml"
        val epdTemplate = parseTemplate(this.readAssets("template/${filenameTemplate}"), filenameTemplate)
        motionView!!.importEdpTemplate(epdTemplate, fontProvider!!)
    }

    private fun initClickListeners() {
        findViewById<View>(R.id.text_entity_font_size_increase).setOnClickListener { view: View? -> increaseTextEntitySize() }
        findViewById<View>(R.id.text_entity_font_size_decrease).setOnClickListener { view: View? -> decreaseTextEntitySize() }
        findViewById<View>(R.id.text_entity_color_change).setOnClickListener { view: View? -> changeTextEntityColor() }
        findViewById<View>(R.id.text_entity_font_change).setOnClickListener { view: View? -> changeTextEntityFont() }
        findViewById<View>(R.id.text_entity_edit).setOnClickListener { view: View? -> startTextEntityEditing() }
        findViewById<View>(R.id.btnChangeImage).setOnClickListener { view: View? ->
            motionView!!.selectedEntity
            cropImage.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = CropImageOptions(
                        guidelines = CropImageView.Guidelines.ON,
                        outputCompressFormat = Bitmap.CompressFormat.PNG,
                        outputCompressQuality = 50,
                        fixAspectRatio = true,
                        aspectRatioX = motionView!!.selectedEntity!!.bmWidth,
                        aspectRatioY = motionView!!.selectedEntity!!.bmHeight,
                    )
                )
            )
        }
        findViewById<View>(R.id.theme1).setOnClickListener { view: View? -> changeTheme(Color.DKGRAY) }
        findViewById<View>(R.id.theme2).setOnClickListener { view: View? -> changeTheme(Color.BLUE) }
        findViewById<View>(R.id.theme3).setOnClickListener { view: View? -> changeTheme(Color.MAGENTA) }
    }

    private fun changeTheme(@ColorInt color: Int) {
        motionView!!.setThemeColor(color)
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent
            val uriFilePath = result.getUriFilePath(this) // optional usage
            uriContent!!.getBitmap(contentResolver)?.let { motionView!!.setImageForSelectedContent(it) }
        } else {
            // An error occurred.
            val exception = result.error
        }
    }

    private fun increaseTextEntitySize() {
        val textEntity = motionView!!.currentTextEntity()
        if (textEntity != null) {
            textEntity.textLayer.font.increaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity()
            motionView!!.invalidate()
        }
    }

    private fun decreaseTextEntitySize() {
        val textEntity = motionView!!.currentTextEntity()
        if (textEntity != null) {
            textEntity.textLayer.font.decreaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textEntity.updateEntity()
            motionView!!.invalidate()
        }
    }

    private fun changeTextEntityColor() {
        val textEntity = motionView!!.currentTextEntity() ?: return

        val initialColor = textEntity.textLayer.font.color

        ColorPickerDialogBuilder
            .with(this@MainActivity)
            .setTitle(R.string.select_color)
            .initialColor(initialColor)
            .wheelType(ColorPickerView.WHEEL_TYPE.CIRCLE)
            .density(8) // magic number
            .setPositiveButton(R.string.ok) { dialog: DialogInterface?, selectedColor: Int, allColors: Array<Int?>? ->
                motionView!!.setCurrentTextColor(selectedColor)
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
                motionView!!.setCurrentTextFont(fonts[which])
            }
            .show()
    }

    private fun startTextEntityEditing() {
        val textEntity = motionView!!.currentTextEntity()
        if (textEntity != null) {
            val fragment = TextEditorDialogFragment.getInstance(textEntity.textLayer.text)
            fragment.show(supportFragmentManager, TextEditorDialogFragment::class.java.name)
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

            R.id.main_add_text -> {
                motionView!!.addTextContent(fontProvider!!)
                startTextEntityEditing()
            }

            R.id.main_save -> lifecycleScope.launch { motionView?.saveImage()?.let {
                findViewById<ImageView>(R.id.ivOutput).setImageBitmap(it)
            } }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_STICKER_REQUEST_CODE) {
                if (data != null) {
                    val stickerId = data.getIntExtra(StickerSelectActivity.EXTRA_STICKER_ID, 0)
                    if (stickerId != 0) {
                        val image = BitmapFactory.decodeResource(resources, stickerId)
                        motionView!!.addImageContent(image)
                    }
                }
            }
        }
    }

    override fun textChanged(text: String) {
        val textEntity = motionView!!.currentTextEntity()
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
