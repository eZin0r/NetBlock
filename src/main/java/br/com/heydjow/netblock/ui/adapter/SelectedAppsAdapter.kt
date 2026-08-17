package br.com.heydjow.netblock.ui.adapter

import android.content.Context
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.heydjow.netblock.R
import br.com.heydjow.netblock.data.FilterStatePrefs
import br.com.heydjow.netblock.model.SelectedApp

class SelectedAppsAdapter(
    private val apps: MutableList<SelectedApp>
) : RecyclerView.Adapter<SelectedAppsAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        //val pkg: TextView = view.findViewById(R.id.packageName)
        val remove: ImageView = view.findViewById(R.id.btnRemove)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_selected_app, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val context = holder.itemView.context
        val pm = context.packageManager
        val app = apps[position]

        holder.name.text = app.appName
        //holder.pkg.text = app.packageName

        try {
            holder.icon.setImageDrawable(
                pm.getApplicationIcon(app.packageName)
            )
        } catch (e: PackageManager.NameNotFoundException) {
            holder.icon.setImageResource(android.R.drawable.sym_def_app_icon)
        }

        holder.remove.setOnClickListener {
            // 🔥 remove do SharedPreferences (fonte única de verdade)
            FilterStatePrefs.removePackage(context, app.packageName)

            // 🔥 remove da lista visível
            apps.removeAt(holder.bindingAdapterPosition)
            notifyItemRemoved(holder.bindingAdapterPosition)
        }
    }

    override fun getItemCount(): Int = apps.size
}