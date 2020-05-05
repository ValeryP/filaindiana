package com.filaindiana.favourites

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.filaindiana.R
import com.filaindiana.data.KEY_SUBSCRIPTON_LOCATION
import com.filaindiana.data.Subscription
import com.filaindiana.network.ShopsResponse
import com.filaindiana.utils.GraphicsProvider
import com.filaindiana.utils.logDebug
import kotlinx.android.synthetic.main.item_favourites.view.*


/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 03.05.2020
 */
class FavouritesAdapter(private val data: MutableList<Subscription> = mutableListOf()) :
    RecyclerView.Adapter<FavouritesAdapter.FavouritesViewHolder>() {

    private var state = mutableMapOf<String, ShopsResponse.Shop>()

    class FavouritesViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val img: ImageView = view.item_favourites_img
        val name: TextView = view.item_favourites_name
        val address: TextView = view.item_favourites_address
        val time: TextView = view.item_favourites_details
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouritesViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = layoutInflater.inflate(R.layout.item_favourites, parent, false)
        return FavouritesViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FavouritesViewHolder, position: Int) {
        val subscription = data[position]
        holder.img.setImageResource(GraphicsProvider.getShopImgResId(subscription.shopBrand))
        holder.name.text = subscription.shopName
        holder.address.text = subscription.shopAddress
        val shop = state[subscription.shopId]
        val lastUpdate = shop?.shopShopState?.getLastUpdate()
        if (shop == null || lastUpdate == null) {
            holder.time.text = ""
        } else {
            val queueSizePeople = shop.shopShopState.queueSizePeople
            val queueWaitMinutes = shop.shopShopState.queueWaitMinutes
            val queue = holder.itemView.resources.getString(
                R.string.queue,
                queueSizePeople,
                queueWaitMinutes
            )
            holder.time.text = "$queue ~ $lastUpdate"
        }

        val item = data.getOrNull(position)
        holder.itemView.setOnClickListener {
            logDebug { "Item: $position" }
            item?.let {
                logDebug { "Item: $it" }
                (holder.itemView.context as FavouritesActivity).apply {
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

    fun setShopsState(states: List<ShopsResponse.Shop>) {
        states.forEach { shop -> state[shop.shopData.marketId] = shop }
        notifyDataSetChanged()
    }
}