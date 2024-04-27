package com.example.motionviewapp.motionviews.ui

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.motionviewapp.R
import com.example.motionviewapp.motionviews.ui.TextEditorDialogFragment.OnTextLayerCallback


/**
 * Transparent Dialog Fragment, with no title and no background
 *
 *
 * The fragment imitates capturing input from keyboard, but does not display anything
 * the result from input from the keyboard is passed through [OnTextLayerCallback]
 *
 *
 * Activity that uses [TextEditorDialogFragment] must implement [OnTextLayerCallback]
 *
 *
 * If Activity does not implement [OnTextLayerCallback], exception will be thrown at Runtime
 */
class TextEditorDialogFragment
/**
 * deprecated
 * use [TextEditorDialogFragment.getInstance]
 */
@Deprecated("") constructor() : DialogFragment() {
    protected var editText: EditText? = null

    private var callback: OnTextLayerCallback? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        if (activity is OnTextLayerCallback) {
            this.callback = activity
        } else {
            throw IllegalStateException(
                activity.javaClass.name
                        + " must implement " + OnTextLayerCallback::class.java.name
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.text_editor_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments
        var text: String? = ""
        if (args != null) {
            text = args.getString(ARG_TEXT)
        }

        editText = view.findViewById<View>(R.id.edit_text_view) as EditText

        initWithTextEntity(text)

        editText!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable) {
                if (callback != null) {
                    callback!!.textChanged(s.toString())
                }
            }
        })

        view.findViewById<View>(R.id.text_editor_root).setOnClickListener { // exit when clicking on background
            dismiss()
        }
    }

    private fun initWithTextEntity(text: String?) {
        editText!!.setText(text)
        editText!!.post {
            if (editText != null) {
                Selection.setSelection(editText!!.text, editText!!.length())
            }
        }
    }

    override fun dismiss() {
        super.dismiss()

        // clearing memory on exit, cos manipulating with text uses bitmaps extensively
        // this does not frees memory immediately, but still can help
        System.gc()
        Runtime.getRuntime().gc()
    }

    override fun onDetach() {
        // release links
        this.callback = null
        super.onDetach()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.requestWindowFeature(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val window = dialog.window
            if (window != null) {
                // remove background
                window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

                // remove dim
                val windowParams = window.attributes
                window.setDimAmount(0.0f)
                window.attributes = windowParams
            }
        }
    }

    override fun onResume() {
        super.onResume()
        editText!!.post { // force show the keyboard
            setEditText(true)
            editText!!.requestFocus()
            val ims = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            ims.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun setEditText(gainFocus: Boolean) {
        if (!gainFocus) {
            editText!!.clearFocus()
            editText!!.clearComposingText()
        }
        editText!!.isFocusableInTouchMode = gainFocus
        editText!!.isFocusable = gainFocus
    }

    /**
     * Callback that passes all user input through the method
     * [OnTextLayerCallback.textChanged]
     */
    interface OnTextLayerCallback {
        fun textChanged(text: String)
    }

    companion object {
        const val ARG_TEXT: String = "editor_text_arg"

        fun getInstance(textValue: String?): TextEditorDialogFragment {
            @Suppress("deprecation") val fragment = TextEditorDialogFragment()
            val args = Bundle()
            args.putString(ARG_TEXT, textValue)
            fragment.arguments = args
            return fragment
        }
    }
}