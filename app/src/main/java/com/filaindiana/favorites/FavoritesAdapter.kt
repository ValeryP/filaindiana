package com.filaindiana.favorites

import android.app.Activity
import android.content.Intent
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.filaindiana.R
import com.filaindiana.data.KEY_SUBSCRIPTON_LOCATION
import com.filaindiana.data.Subscription
import com.filaindiana.utils.GraphicsProvider
import com.filaindiana.utils.logDebug
import kotlinx.android.synthetic.main.item_favorites.view.*
import org.joda.time.DateTime


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 03.05.2020
 */
class FavoritesAdapter(private val data: MutableList<Subscription> = mutableListOf()) :
    RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder>() {

    class FavoritesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.item_favorites_img
        val name: TextView = view.item_favorites_name
        val address: TextView = view.item_favorites_address
        val time: TextView = view.item_favorites_time
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoritesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_favorites, parent, false)
        return FavoritesViewHolder(view)
    }

    override fun onBindViewHolder(holder: FavoritesViewHolder, position: Int) {
        val subscription = data[position]
        holder.img.setImageResource(GraphicsProvider.getShopImgResId(subscription.shopBrand))
        holder.name.text = subscription.shopName
        holder.address.text = subscription.shopAddress
        holder.time.text = holder.itemView.context.getString(
            R.string.added,
            DateUtils.getRelativeTimeSpanString(
                subscription.getTime().millis,
                DateTime.now().millis,
                DateUtils.MINUTE_IN_MILLIS
            ).toString()
        )

        val item = data.getOrNull(position)
        holder.itemView.setOnClickListener {
            logDebug { "Item: $position" }
            item?.let {
                logDebug { "Item: $it" }
                (holder.itemView.context as FavoritesActivity).apply {
                    setResult(Activity.RESULT_OK, Intent().apply {
                        putExtra(KEY_SUBSCRIPTON_LOCATION, item.getLocation())
                    })
                    finish()
                }
            }
        }
    }

    override fun getItemCount() = data.size

    fun update(newData: List<Subscription>) {
        data.clear()
        data.addAll(newData)
        notifyDataSetChanged()
    }
}