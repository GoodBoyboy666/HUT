package top.goodboyboy.hut.Util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import top.goodboyboy.hut.R

/**
 * 提示框Util
 *
 * @property context 上下文
 * @property title 标题
 * @property message 内容
 * @property isDark 暗色模式
 * @property event 确定按钮事件
 * @property type 提示框类型
 * @property onClickListener 事件内容
 */
class AlertDialogUtil(
    val context: Context,
    val title: String,
    val message: String,
    val isDark: Boolean,
    val event: AlertDialogEvent = AlertDialogEvent.DEFAULT,
    val type: AlertDialogType = AlertDialogType.DEFAULT,
    val onClickListener: (View) -> Unit = {}
) {

    var onClickIgnoreTheVersionButton: (() -> Unit)? = null

    /**
     * 展示提示框
     *
     */
    fun show() {
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.custom_dialog, null)
        val dialog: LinearLayout = dialogView.findViewById(R.id.custom_alertdialog)
        val titleTextView: TextView = dialogView.findViewById(R.id.dialog_title)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
        val positiveButton: Button = dialogView.findViewById(R.id.dialog_positiveButton)
        val customButton: Button = dialogView.findViewById(R.id.dialog_customButton)
        val ignoreButton: Button = dialogView.findViewById(R.id.dialog_ignore_the_version)
        var dialogBackground = R.drawable.info_border
        var okBackground = R.drawable.kb_ok
        titleTextView.text = title
        messageTextView.text = message

        if (isDark) {
            dialogBackground = R.color.grey
            okBackground = R.color.grey
        }
        dialog.setBackgroundResource(dialogBackground)
        positiveButton.setBackgroundResource(okBackground)
        customButton.setBackgroundResource(okBackground)
        ignoreButton.setBackgroundResource(okBackground)
        builder.setView(dialogView)
        val alertDialog = builder.create()
        if (event == AlertDialogEvent.CUSTOM) {
            positiveButton.setOnClickListener(onClickListener)
        } else if (event == AlertDialogEvent.DEFAULT) {
            positiveButton.setOnClickListener {
                alertDialog.dismiss()
            }
        }
        if (type == AlertDialogType.DEFAULT) {
            customButton.visibility = View.GONE
            ignoreButton.visibility = View.GONE
        } else if (type == AlertDialogType.NEW_VERSION) {
            customButton.setOnClickListener {
                val setting = SettingsUtil(context)
                setting.globalSettings.noMoreReminders = true
                setting.save()
                alertDialog.dismiss()
            }
            if (onClickIgnoreTheVersionButton != null) {
                ignoreButton.visibility = View.VISIBLE
                ignoreButton.setOnClickListener {
                    onClickIgnoreTheVersionButton?.invoke()
                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.show()
    }

    /**
     * 提示框事件
     *
     */
    enum class AlertDialogEvent {
        DEFAULT,
        CUSTOM
    }

    /**
     * /提示框类型
     *
     */
    enum class AlertDialogType {
        DEFAULT,
        NEW_VERSION
    }
}
