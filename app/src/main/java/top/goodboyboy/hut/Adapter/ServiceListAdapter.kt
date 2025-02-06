package top.goodboyboy.hut.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import top.goodboyboy.hut.Activity.BrowseActivity
import top.goodboyboy.hut.R
import top.goodboyboy.hut.ServiceItem

class ServiceListAdapter(
    private val context: Context,
    private val data: List<ServiceItem>,
    private val dark: Boolean, private val jwt: String
) : BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context)
                .inflate(R.layout.service_list_item_layout, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val item = data[position]
        Glide.with(context)
            .load(item.imageUrl)
            .placeholder(R.drawable.icon_pic)
            .error(R.drawable.icon_pic)
            .into(viewHolder.imageView)
        viewHolder.textView.text = item.text

        view.setOnClickListener {

            val intent = Intent(context, BrowseActivity::class.java)
            intent.putExtra("url", item.serviceUrl)
            intent.putExtra("jwt", jwt)
            intent.putExtra("tokenAccept", item.tokenAccept)
            context.startActivity(intent)
        }

        return view
    }

    private class ViewHolder(view: View) {
        val imageView: ImageView = view.findViewById(R.id.service_item_image)
        val textView: TextView = view.findViewById(R.id.service_item_text)
    }
}