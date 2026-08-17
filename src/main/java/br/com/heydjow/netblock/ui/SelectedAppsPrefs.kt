package br.com.heydjow.netblock.data

import android.content.Context

object SelectedAppsPrefs {

    private const val PREF = "selected_apps"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun isSelected(ctx: Context, pkg: String): Boolean =
        prefs(ctx).getBoolean(pkg, false)

    fun setSelected(ctx: Context, pkg: String, value: Boolean) {
        prefs(ctx).edit().putBoolean(pkg, value).apply()
    }

    fun getAll(ctx: Context): Set<String> =
        prefs(ctx).all
            .filterValues { it == true }
            .keys
}
