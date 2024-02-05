package scan.reader.qrscanner.barcodescanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import scan.reader.qrscanner.barcodescanner.modelClass.LangModelClass
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ItemLanguageBinding



class ItemLanguageView(val itemBinding: ItemLanguageBinding) :
    RecyclerView.ViewHolder(itemBinding.root)

class LanguageAdapter(val context: Context,
                      var languageList:ArrayList<LangModelClass>,
                      val callback:(lanCode:String)->Unit
) : RecyclerView.Adapter<ItemLanguageView>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemLanguageView {

        return ItemLanguageView(ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }


    override fun onBindViewHolder(holder: ItemLanguageView, position: Int) {

        val listData= languageList[holder.layoutPosition]

        Glide.with(context)
            .load(listData.lanIcon)
            .into(holder.itemBinding.lanuageIcon)



        if(listData.isSelected){
            holder.itemBinding.mainCardView.background=ContextCompat.getDrawable(context, R.drawable.rounded_corners_with_stroke)
            holder.itemBinding.languageTickIcon.setImageResource(R.drawable.on_radio_button)
            holder.itemBinding.languageTickIcon.imageTintList=ContextCompat.getColorStateList(context,R.color.mainBackGroundColorGreen)



            holder.itemBinding.languageTickIcon.setPadding(15,15,15,15)
        }
        else{
            holder.itemBinding.mainCardView.background=ContextCompat.getDrawable(context, R.drawable.language_holder_bg)

            holder.itemBinding.languageTickIcon.setImageResource(R.drawable.off_radio_button)
            holder.itemBinding.languageTickIcon.setPadding(15,15,15,15)
            holder.itemBinding.languageTickIcon.imageTintList=ContextCompat.getColorStateList(context,R.color.unselectedThumbColor)
        }

        holder.itemBinding.lanuageName.text=listData.lanName

        holder.itemView.setOnClickListener{

            val prevSelectedIndex=languageList.indexOfFirst { it.isSelected==true }
            if(prevSelectedIndex!=-1){

                languageList[prevSelectedIndex].isSelected=false
                listData.isSelected=true

                callback.invoke(listData.lanCode)

                notifyItemChanged(holder.layoutPosition)
                notifyItemChanged(prevSelectedIndex)
            }
        }
    }




    override fun getItemCount(): Int {
        return languageList.size
    }
}
