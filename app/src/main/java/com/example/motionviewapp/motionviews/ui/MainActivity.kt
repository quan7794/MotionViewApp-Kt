package com.example.motionviewapp.motionviews.ui

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.example.motionviewapp.motionviews.widget.content.ImageContent
import com.example.motionviewapp.motionviews.widget.content.TextContent
import com.example.motionviewapp.utils.FontProvider
import com.example.motionviewapp.utils.addImageContent
import com.example.motionviewapp.utils.addTextContent
import com.example.motionviewapp.utils.setCurrentTextFont
import com.example.motionviewapp.utils.currentTextContent
import com.example.motionviewapp.utils.getBitmap
import com.example.motionviewapp.utils.importEdpTemplate
import com.example.motionviewapp.utils.saveImage
import com.example.motionviewapp.utils.setCurrentTextColor
import com.example.motionviewapp.utils.setImageForSelectedContent
import com.flask.colorpicker.ColorPickerView
import com.flask.colorpicker.builder.ColorPickerDialogBuilder
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity(), OnTextLayerCallback {
    protected var motionView: MotionView? = null
    protected var textContentEditPanel: View? = null

//    private val epd = EPDTemplate()

    private val motionViewCallback: MotionViewCallback = object : MotionViewCallback {
        override fun onTouch() {
        }

        override fun onRelease() {
        }

        override fun onContentSelected(content: BaseContent?) {
            if (content is TextContent) {
                textContentEditPanel!!.visibility = View.VISIBLE
            } else {
                textContentEditPanel!!.visibility = View.GONE
            }
        }

        override fun onContentDeleted() {
        }

        override fun onContentAdded() {
        }

        override fun onContentReselected() {
        }

        override fun onContentDoubleTap(content: BaseContent) {
            startTextEditing()
        }

        override fun onContentUnselected() {
        }
    }
    private var fontProvider: FontProvider? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        this.fontProvider = FontProvider(resources)

        motionView = findViewById(R.id.main_motion_view)
        textContentEditPanel = findViewById(R.id.main_motion_text_content_edit_panel)
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
        findViewById<View>(R.id.text_content_font_size_increase).setOnClickListener { increaseTextSize() }
        findViewById<View>(R.id.text_content_font_size_decrease).setOnClickListener { decreaseTextSize() }
        findViewById<View>(R.id.text_content_color_change).setOnClickListener { changeTextColor() }
        findViewById<View>(R.id.text_content_font_change).setOnClickListener { changeTextFont() }
        findViewById<View>(R.id.text_content_edit).setOnClickListener { startTextEditing() }
        findViewById<View>(R.id.btnChangeImage).setOnClickListener {
            motionView?.selectedContent?.let {
                if (it is ImageContent) cropImage.launch(
                    CropImageContractOptions(
                        uri = null,
                        cropImageOptions = CropImageOptions(
                            guidelines = CropImageView.Guidelines.ON,
                            outputCompressFormat = Bitmap.CompressFormat.PNG,
                            outputCompressQuality = 50,
                            fixAspectRatio = true,
                            aspectRatioX = it.bmWidth,
                            aspectRatioY = it.bmHeight,
                        )
                    )
                )
            }
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

    private fun increaseTextSize() {
        val textContent = motionView!!.currentTextContent()
        if (textContent != null) {
            textContent.textLayer.font.increaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textContent.updateContent()
            motionView!!.invalidate()
        }
    }

    private fun decreaseTextSize() {
        val textContent = motionView!!.currentTextContent()
        if (textContent != null) {
            textContent.textLayer.font.decreaseSize(TextLayer.Limits.FONT_SIZE_STEP)
            textContent.updateContent()
            motionView!!.invalidate()
        }
    }

    private fun changeTextColor() {
        val textContent = motionView!!.currentTextContent() ?: return

        val initialColor = textContent.textLayer.font.color

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

    private fun changeTextFont() {
        val fonts = fontProvider!!.fontNames
        val fontsAdapter = FontsAdapter(this, fonts, fontProvider!!)
        AlertDialog.Builder(this)
            .setTitle(R.string.select_font)
            .setAdapter(fontsAdapter) { dialogInterface: DialogInterface?, which: Int ->
                motionView!!.setCurrentTextFont(fonts[which])
            }
            .show()
    }

    private fun startTextEditing() {
        val textContent = motionView!!.currentTextContent()
        if (textContent != null) {
            val fragment = TextEditorDialogFragment.getInstance(textContent.textLayer.text)
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
                startTextEditing()
            }

            R.id.main_save -> lifecycleScope.launch {
                Log.i("AAA","SAVE_IMAGE START")
                motionView?.saveImage()?.let { filePath ->
                    Log.i("AAA","SAVE_IMAGE DONE $filePath")
                    findViewById<ImageView>(R.id.ivOutput).setImageBitmap(BitmapFactory.decodeFile(File(filePath).absolutePath))
                }
            }
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
        val textContent = motionView!!.currentTextContent()
        if (textContent != null) {
            val textLayer = textContent.textLayer
            if (text != textLayer.text) {
                textLayer.text = text
                textContent.updateContent()
                motionView!!.invalidate()
            }
        }
    }

    companion object {
        const val SELECT_STICKER_REQUEST_CODE: Int = 123
    }
}
