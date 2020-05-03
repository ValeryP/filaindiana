package com.filaindiana.favorites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.DividerItemDecoration.VERTICAL
import androidx.recyclerview.widget.LinearLayoutManager
import com.filaindiana.R
import com.filaindiana.data.AppDB
import com.filaindiana.data.SubscriptionRepository
import kotlinx.android.synthetic.main.activity_favorites.*


class FavoritesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val favoritesAdapter = FavoritesAdapter()
        val linearLayoutManager = LinearLayoutManager(this)
        val recyclerView = favorites_recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            adapter = favoritesAdapter
            addItemDecoration(DividerItemDecoration(context, VERTICAL))
        }
        val subscriptions = AppDB.getDatabase(this).subscriptionDao().let {
            SubscriptionRepository.getInstance(it).getSubscriptions()
        }
        subscriptions.observe(this, Observer { favoritesAdapter.update(it) })
    }
}