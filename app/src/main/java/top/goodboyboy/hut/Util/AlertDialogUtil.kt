package top.goodboyboy.hut.Util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import top.goodboyboy.hut.R

class AlertDialogUtil(val context: Context,
                      val title: String,
                      val message: String,
                      val isDark: Boolean, val event: AlertDialogEvent=AlertDialogEvent.DEFAULT, val onClickListener: (View) -> Unit={}) {

    fun show(){
        val builder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(R.layout.custom_dialog, null)
        val dialog: LinearLayout = dialogView.findViewById(R.id.custom_alertdialog)
        val titleTextView: TextView = dialogView.findViewById(R.id.dialog_title)
        val messageTextView: TextView = dialogView.findViewById(R.id.dialog_message)
        val positiveButton: Button = dialogView.findViewById(R.id.dialog_positiveButton)
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
        builder.setView(dialogView)
        val alertDialog = builder.create()
        if(event==AlertDialogEvent.CUSTOM) {
            positiveButton.setOnClickListener(onClickListener)
        }else if(event==AlertDialogEvent.DEFAULT){
            positiveButton.setOnClickListener {
                alertDialog.dismiss()
            }
        }
        alertDialog.show()
    }

    enum class AlertDialogEvent{
        DEFAULT,
        CUSTOM
    }
}
