package top.goodboyboy.hut.Adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter

class SpinnerAdapter(
    context: Context,
    resource: Int,
    items: List<String>, isDarkMode: Boolean
) : ArrayAdapter<String>(context, resource, items) {
    private var darkMode = isDarkMode


    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        var spinnerBackground = android.graphics.Color.parseColor("#F4BB6E")
        if (darkMode) {
            spinnerBackground = android.graphics.Color.parseColor("#141414")
        }
        view.setBackgroundColor(spinnerBackground)
        return view
    }
}