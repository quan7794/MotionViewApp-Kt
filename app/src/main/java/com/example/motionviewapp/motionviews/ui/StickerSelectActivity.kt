package com.example.motionviewapp.motionviews.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.motionviewapp.R
import com.example.motionviewapp.motionviews.ui.StickerSelectActivity.StickersAdapter.StickerViewHolder

/**
 * selects sticker
 * result - Integer, resource id of the sticker, bundled at key EXTRA_STICKER_ID
 *
 *
 * Stickers borrowed from : http://www.flaticon.com/packs/pokemon-go
 */
class StickerSelectActivity : AppCompatActivity() {
    private val stickerIds = intArrayOf(
        R.drawable.abra,
        R.drawable.bellsprout,
        R.drawable.bracelet,
        R.drawable.bullbasaur,
        R.drawable.camera,
        R.drawable.candy,
        R.drawable.caterpie,
        R.drawable.charmander,
        R.drawable.mankey,
        R.drawable.map,
        R.drawable.mega_ball,
        R.drawable.meowth,
        R.drawable.pawprints,
        R.drawable.pidgey,
        R.drawable.pikachu,
        R.drawable.pikachu_1,
        R.drawable.pikachu_2,
        R.drawable.player,
        R.drawable.pointer,
        R.drawable.pokebag,
        R.drawable.pokeball,
        R.drawable.pokeballs,
        R.drawable.pokecoin,
        R.drawable.pokedex,
        R.drawable.potion,
        R.drawable.psyduck,
        R.drawable.rattata,
        R.drawable.revive,
        R.drawable.squirtle,
        R.drawable.star,
        R.drawable.star_1,
        R.drawable.superball,
        R.drawable.tornado,
        R.drawable.venonat,
        R.drawable.weedle,
        R.drawable.zubat
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.select_sticker_activity)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        val recyclerView = findViewById<View>(R.id.stickers_recycler_view) as RecyclerView
        val glm = GridLayoutManager(this, 3)
        recyclerView.layoutManager = glm

        val stickers: MutableList<Int> = ArrayList(stickerIds.size)
        for (id in stickerIds) {
            stickers.add(id)
        }

        recyclerView.adapter = StickersAdapter(stickers, this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onStickerSelected(stickerId: Int) {
        val intent = Intent()
        intent.putExtra(EXTRA_STICKER_ID, stickerId)
        setResult(RESULT_OK, intent)
        finish()
    }

    internal inner class StickersAdapter(private val stickerIds: List<Int>, private val context: Context) : RecyclerView.Adapter<StickerViewHolder>() {
        private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
            return StickerViewHolder(layoutInflater.inflate(R.layout.sticker_item, parent, false))
        }

        override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
            holder.image.setImageDrawable(ContextCompat.getDrawable(context, getItem(position)))
        }

        override fun getItemCount(): Int {
            return stickerIds.size
        }

        private fun getItem(position: Int): Int {
            return stickerIds[position]
        }

        internal inner class StickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var image: ImageView = itemView.findViewById<View>(R.id.sticker_image) as ImageView

            init {
                itemView.setOnClickListener {
                    val pos = adapterPosition
                    if (pos >= 0) { // might be NO_POSITION
                        onStickerSelected(getItem(pos))
                    }
                }
            }
        }
    }

    companion object {
        const val EXTRA_STICKER_ID: String = "extra_sticker_id"
    }
}
