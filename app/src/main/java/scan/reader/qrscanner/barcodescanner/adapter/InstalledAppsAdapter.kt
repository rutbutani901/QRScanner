package scan.reader.qrscanner.barcodescanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import scan.reader.qrscanner.barcodescanner.viewModel.InstalledAppDataModelClass
import scan.reader.qrscanner.barcodescanner.databinding.ItemInstalledAppBinding



class ItemInstalledApp(val itemBinding: ItemInstalledAppBinding) :
    RecyclerView.ViewHolder(itemBinding.root)

class InstalledAppsAdapter(val context: Context,
                           var installedAppList:ArrayList<InstalledAppDataModelClass>,
                           val callback:(holderPosition:Int)->Unit
) : RecyclerView.Adapter<ItemInstalledApp>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemInstalledApp {

        return ItemInstalledApp(ItemInstalledAppBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: ItemInstalledApp, position: Int) {


        holder.itemBinding.appName.text=installedAppList.get(holder.layoutPosition).appName

        Glide.with(context)
            .load(installedAppList.get(holder.layoutPosition).appIcon)
            .into(holder.itemBinding.appLogo)

        holder.itemView.setOnClickListener{
            callback.invoke(holder.layoutPosition)
        }
    }




    override fun getItemCount(): Int {
        return installedAppList.size
    }
}
