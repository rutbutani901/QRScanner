package scan.reader.qrscanner.barcodescanner.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import scan.reader.qrscanner.barcodescanner.dataBase.DataModel
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ItemBarcodeDataBinding
import ezvcard.Ezvcard
import ezvcard.VCard
import java.util.HashMap


class ItemBarcodeDataView(val itemBarcodeDataBinding: ItemBarcodeDataBinding) :
    RecyclerView.ViewHolder(itemBarcodeDataBinding.root)

class BarcodeListDbAdapter(val context: Context,
                           var barcodeList:ArrayList<DataModel>,
                           val callback:(longPressed:Boolean,isLastItem:Boolean,holderPosition:Int)->Unit
                    ) : RecyclerView.Adapter<ItemBarcodeDataView>() {//  val callback:(holderPosition:Int,longPressed:Boolean,holderView:View)->Unit

    val barcodeIconMap: HashMap<Int, Int> = hashMapOf(
        11 to R.drawable.event_icon,
        1 to R.drawable.add_contact_icon_with_person,
        2 to R.drawable.email_icon,
        10 to R.drawable.location_icon,
        4 to R.drawable.call_icon,
        5 to R.drawable.other_qrcodes_icon,
        6 to R.drawable.send_sms_icon,
        7 to R.drawable.other_qrcodes_icon,
        8 to R.drawable.website_icon,
        9 to R.drawable.wifi_icon
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemBarcodeDataView {
        return ItemBarcodeDataView(ItemBarcodeDataBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }
    var longPressed=false
    var count=0
    override fun onBindViewHolder(holder: ItemBarcodeDataView, position: Int) {


        Glide.with(context)
            .load(barcodeIconMap.get(barcodeList.get(holder.layoutPosition).barcodeType))
            .into(holder.itemBarcodeDataBinding.barcodeIcon)

        if(barcodeList.get(holder.layoutPosition).barcodeType==1)
        {
            holder.itemBarcodeDataBinding.barcodeValue.text=getContactName(barcodeList.get(holder.layoutPosition).barValue)
        }else{
            holder.itemBarcodeDataBinding.barcodeValue.text=barcodeList.get(holder.layoutPosition).barValue
        }

        holder.itemBarcodeDataBinding.createdDate.text=barcodeList.get(holder.layoutPosition).creationTime

        if(!barcodeList.get(holder.layoutPosition).note.equals(""))
        {
            holder.itemBarcodeDataBinding.barcodeNote.visibility=View.VISIBLE
            holder.itemBarcodeDataBinding.barcodeNote.text=barcodeList.get(holder.layoutPosition).note
        }else{
            holder.itemBarcodeDataBinding.barcodeNote.visibility=View.GONE
        }

        if(barcodeList.get(holder.layoutPosition).isFav) {

            holder.itemBarcodeDataBinding.favIcon.visibility=View.VISIBLE
            holder.itemBarcodeDataBinding.barcodeNote.text=barcodeList.get(holder.layoutPosition).note
        }else{

            holder.itemBarcodeDataBinding.favIcon.visibility=View.GONE
        }

        if(barcodeList.get(holder.layoutPosition).isSelected) {
//            holder.itemView.setBackgroundColor(Color.parseColor("#F31404"))
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.lightYello))
        }else{

         //   holder.itemView.setBackgroundColor(Color.parseColor("#FFFFFF"))
            holder.itemView.setBackgroundColor(ContextCompat.getColor(context,R.color.white))
        }

        holder.itemView.setOnClickListener{

//            holder.itemView.setBackgroundColor(Color.parseColor("#F31404"))

            if(count==0)
            {
                longPressed=false
            }
            if(longPressed)
            {
                if( barcodeList.get(holder.layoutPosition).isSelected)
                {
                    barcodeList.get(holder.layoutPosition).isSelected=false
                    holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
                    count--
                    callback.invoke(true,false,holder.layoutPosition)//remove
                    if(count==0)// long pressed disable
                    {

                        longPressed=false
                        callback.invoke(false,true,holder.layoutPosition)
                    }
                }
                else
                {
                    barcodeList.get(holder.layoutPosition).isSelected=true
                    holder.itemView.setBackgroundColor(context.resources.getColor(R.color.lightYello))
                    count++
                    callback.invoke(true,false,holder.layoutPosition)//remove
                }


            }
            else{//single tap

                callback.invoke(false,false,holder.layoutPosition)

            }
        }

        holder.itemView.setOnLongClickListener(OnLongClickListener { v ->

                longPressed=true

                if(barcodeList.get(holder.layoutPosition).isSelected)
                {
                    barcodeList.get(holder.layoutPosition).isSelected=false
                    holder.itemView.setBackgroundColor(context.resources.getColor(R.color.white))
                    count--

                    callback.invoke(true,false,holder.layoutPosition)//remove
                    if(count==0)// long pressed disable
                    {
                        longPressed=false
                        callback.invoke(false,true,holder.layoutPosition)
                    }
                }
                else
                {
                    barcodeList.get(holder.layoutPosition).isSelected=true
                    holder.itemView.setBackgroundColor(context.resources.getColor(R.color.lightYello))
                    count++

                    callback.invoke(true,false,holder.layoutPosition)
                }


            true
        })
    }
    override fun getItemCount(): Int {
        return barcodeList.size
    }
    fun resetLongPressedAndCount(){
        longPressed=false
        count=0
    }

    fun getContactName(barcodeValue:String):String
    {
        val vcard: VCard? = Ezvcard.parse(barcodeValue).first()
        var formattedBValue=""

        var fullNameFound=false
        vcard?.formattedName?.let {
            fullNameFound=true
            formattedBValue= vcard.formattedName.value// first name and last name
        }
       if(!fullNameFound){

           vcard?.structuredName?.family?.let {
               formattedBValue= vcard.structuredName.family// first name and last name
           }
       }
        return formattedBValue
    }



}
