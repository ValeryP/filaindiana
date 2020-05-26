package com.codaliscia.favourites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.codaliscia.R
import com.codaliscia.data.AppDB
import com.codaliscia.data.SubscriptionRepository
import com.codaliscia.network.RestClient
import com.codaliscia.utils.GraphicsProvider
import kotlinx.android.synthetic.main.activity_favourites.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch


class FavouritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favourites)

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(true)
            setDisplayShowHomeEnabled(true)
            setIcon(
                GraphicsProvider.getColoredIcon(
                    this@FavouritesActivity,
                    R.drawable.ic_stars_24px,
                    R.color.colorTextWhite
                )
            )
            title = "\t${getString(R.string.favorites)}"
        }

        val favoyritesAdapter = FavouritesAdapter()
        val linearLayoutManager = LinearLayoutManager(this)
        favourites_recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = favoyritesAdapter
            addItemDecoration(DividerItemDecoration(context, VERTICAL))
        }
        val subscriptions = AppDB.getDatabase(this).subscriptionDao().let {
            SubscriptionRepository.getInstance(it).getSubscriptions()
        }
        subscriptions.observe(this, Observer { list ->
            if (list.isNotEmpty()) {
                favoyritesAdapter.update(list)
                CoroutineScope(IO).launch {
                    val shops = list.map { RestClient.getShops(it.lat, it.lng) }.flatten()
                    CoroutineScope(Main).launch {
                        favoyritesAdapter.setShopsState(shops)
                    }
                }
            } else {
                finish()
            }
        })
    }
}