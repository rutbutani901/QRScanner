package scan.reader.qrscanner.barcodescanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import scan.reader.qrscanner.barcodescanner.modelClass.OtherBarcodeTypeModelClass
import scan.reader.qrscanner.barcodescanner.databinding.ItemOtherBarcodesBinding


class ItemBarcode(val itemBinding: ItemOtherBarcodesBinding) :
    RecyclerView.ViewHolder(itemBinding.root)

class OtherBarcodesShowerAdapter(val context: Context,
                                 var barcodesTypesList:ArrayList<OtherBarcodeTypeModelClass>,
                                 val callback:(holderPosition:Int)->Unit
) : RecyclerView.Adapter<ItemBarcode>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemBarcode {

        return ItemBarcode(ItemOtherBarcodesBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: ItemBarcode, position: Int) {

        Glide.with(context)
            .load(barcodesTypesList.get(holder.layoutPosition).barcodeIcon)
            .into(holder.itemBinding.barcodeIcon)

        holder.itemBinding.barcodeName.text=barcodesTypesList.get(holder.layoutPosition).barcodeName
        holder.itemBinding.barcodeDescription.text=barcodesTypesList.get(holder.layoutPosition).description

//        Glide.with(context)
//            .load(installedAppList.get(holder.layoutPosition).appIcon)
//            .into(holder.itemBinding.appLogo)

        holder.itemView.setOnClickListener{
            callback.invoke(holder.layoutPosition)
        }
    }




    override fun getItemCount(): Int {
        return barcodesTypesList.size
    }
}
