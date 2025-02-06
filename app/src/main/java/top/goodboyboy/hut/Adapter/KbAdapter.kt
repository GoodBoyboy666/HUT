package top.goodboyboy.hut.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import top.goodboyboy.hut.GridAdapterItems
import top.goodboyboy.hut.R
import top.goodboyboy.hut.Util.AlertDialogUtil

class KbAdapter(
    private val context: Context,
    private val data: GridAdapterItems,
    private val dark: Boolean
) : BaseAdapter() {
    override fun getCount(): Int {
        return data.kbTitle?.size ?: 0
    }

    override fun getItem(position: Int): Any {
        return data.kbTitle?.get(position) ?: "N/A"
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        if (convertView == null) {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.kb_item_layout, null)
        } else {
            view = convertView
        }

        val textView = view.findViewById<TextView>(R.id.kb_item)
        textView.text = data.kbTitle?.get(position) ?: "N/A"


        var kbItemBackground1 = R.drawable.kb_item_background1
        var kbItemBackground2 = R.drawable.kb_item_background2
        var kbItemBackground3 = R.drawable.kb_item_background3

        if (dark) {
            kbItemBackground1 = R.drawable.kb_item_background_dark1
            kbItemBackground2 = R.drawable.kb_item_background_dark2
            kbItemBackground3 = R.drawable.kb_item_background_dark3
        }

        if (position % 2 == 0) {
            textView.setBackgroundResource(kbItemBackground1)
        } else {
            textView.setBackgroundResource(kbItemBackground2)
        }

        if (position % 8 == 0) {
            textView.setBackgroundResource(kbItemBackground3)
        }

        view.setOnClickListener {
            val selectedItem = data.kbInfo?.get(position)

            AlertDialogUtil(
                context,
                data.kbTitle?.get(position) ?: "N/A",
                "$selectedItem",
                dark
            ).show()
        }

        return view
    }
}
