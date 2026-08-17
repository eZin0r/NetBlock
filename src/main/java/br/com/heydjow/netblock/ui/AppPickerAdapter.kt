package br.com.heydjow.netblock.ui.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import br.com.heydjow.netblock.R
import br.com.heydjow.netblock.data.FilterStatePrefs

class AppPickerAdapter(
    private val context: Context,
    apps: List<ApplicationInfo>
) : RecyclerView.Adapter<AppPickerAdapter.VH>() {

    private val pm: PackageManager = context.packageManager

    // 🔠 ordena por nome do app
    private val appList: List<ApplicationInfo> =
        apps.sortedBy { pm.getApplicationLabel(it).toString().lowercase() }

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val name: TextView = view.findViewById(R.id.appName)
        //val pkg: TextView = view.findViewById(R.id.packageName)
        val check: CheckBox = view.findViewById(R.id.checkApp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_picker, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val app = appList[position]
        val pkg = app.packageName

        holder.name.text = pm.getApplicationLabel(app)
        //holder.pkg.text = pkg

        try {
            holder.icon.setImageDrawable(pm.getApplicationIcon(app))
        } catch (_: Exception) {}

        // 🔥 estado atual vindo do SharedPreferences
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked =
            FilterStatePrefs.getSelectedPackages(context).contains(pkg)

        holder.check.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                FilterStatePrefs.addPackage(context, pkg)
            } else {
                FilterStatePrefs.removePackage(context, pkg)
            }
        }

        // permite clicar na linha inteira
        holder.itemView.setOnClickListener {
            holder.check.isChecked = !holder.check.isChecked
        }
    }

    override fun getItemCount(): Int = appList.size
}
