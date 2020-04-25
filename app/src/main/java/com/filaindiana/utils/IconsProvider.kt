package com.filaindiana.utils

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

/*
 * @author Valeriy Palamarchuk
 * @email valeriij.palamarchuk@gmail.com
 * Created on 25.04.2020
 */
object IconsProvider {
    fun getColoredIcon(con: Context, @DrawableRes r: Int, @ColorRes col: Int): Drawable? {
        return ResourcesCompat.getDrawable(con.resources, r, null)?.let {
            val drawableWrapped = DrawableCompat.wrap(it)
            val color = ResourcesCompat.getColor(con.resources, col, null)
            DrawableCompat.setTint(drawableWrapped, color)
            drawableWrapped
        }
    }
}