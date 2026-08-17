package br.com.heydjow.netblock.data

import android.content.Context

object FloatingPrefs {

    private const val PREF = "floating_btn"
    private const val X = "x"
    private const val Y = "y"

    fun save(ctx: Context, x: Int, y: Int) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(X, x)
            .putInt(Y, y)
            .apply()
    }

    fun load(ctx: Context): Pair<Int, Int> {
        val p = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return p.getInt(X, 50) to p.getInt(Y, 200)
    }
}
