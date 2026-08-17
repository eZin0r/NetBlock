package br.com.heydjow.netblock.data

import android.content.Context
import android.util.Log

object FilterStatePrefs {

    private const val PREF_NAME = "netblock_prefs"
    private const val KEY_ENABLED = "filter_enabled"
    private const val KEY_SELECTED_APPS = "selected_apps"

    // 🔒 SharedPreferences base
    private fun prefs(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // 🔘 Estado do filtro (ligado / desligado)
    fun isEnabled(context: Context): Boolean {
        return prefs(context).getBoolean(KEY_ENABLED, false)
    }

    fun toggle(context: Context): Boolean {
        val newValue = !isEnabled(context)
        prefs(context).edit()
            .putBoolean(KEY_ENABLED, newValue)
            .apply()
        return newValue
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_ENABLED, enabled)
            .apply()
    }

    // 📦 Apps selecionados
    fun getSelectedPackages(context: Context): Set<String> {
        return prefs(context)
            .getStringSet(KEY_SELECTED_APPS, emptySet())
            ?: emptySet()
    }

    fun setSelectedPackages(context: Context, packages: Set<String>) {
        prefs(context).edit()
            .putStringSet(KEY_SELECTED_APPS, packages)
            .apply()
    }

    fun addPackage(context: Context, pkg: String) {
        val set = getSelectedPackages(context).toMutableSet()
        set.add(pkg)
        setSelectedPackages(context, set)
    }

    fun removePackage(context: Context, pkg: String) {
        val set = getSelectedPackages(context).toMutableSet()
        set.remove(pkg)
        setSelectedPackages(context, set)
    }
}
