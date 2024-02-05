package scan.reader.qrscanner.barcodescanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import scan.reader.qrscanner.barcodescanner.modelClass.CustomCreatedViewForScanResult
import scan.reader.qrscanner.barcodescanner.databinding.ItemScanedBarcodeFunctionalityBinding


class ItemScannedBarcodeFunc(val itemBinding: ItemScanedBarcodeFunctionalityBinding) :
    RecyclerView.ViewHolder(itemBinding.root)

class BarcodeFunctionalityAdapter(val context: Context,
                                  var funcList:ArrayList<CustomCreatedViewForScanResult>,
                                  val callback:(holderTitle:String,holderValue:String)->Unit//holdername:String
) : RecyclerView.Adapter<ItemScannedBarcodeFunc>() {//  val callback:(holderPosition:Int,longPressed:Boolean,holderView:View)->Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemScannedBarcodeFunc {

        return ItemScannedBarcodeFunc(ItemScanedBarcodeFunctionalityBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: ItemScannedBarcodeFunc, position: Int) {

        var listItem=funcList[holder.layoutPosition]
        holder.itemBinding.functionName.text= listItem.title
        holder.itemBinding.functionLogo.setImageResource(listItem.icon)

        holder.itemView.setOnClickListener{

//            callback.invoke(funcList.get(holder.layoutPosition))
            callback.invoke(listItem.title,listItem.value.toString())
        }
    }

    override fun getItemCount(): Int {
        return funcList.size
    }




}