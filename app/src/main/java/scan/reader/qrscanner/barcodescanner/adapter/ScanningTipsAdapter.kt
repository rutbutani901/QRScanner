package scan.reader.qrscanner.barcodescanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ItemSupportedCodeNativeHolderBinding
import scan.reader.qrscanner.barcodescanner.databinding.ItemSupportedCodesBinding
import scan.reader.qrscanner.barcodescanner.modelClass.SupportedCodesModelClass
import scan.reader.qrscanner.barcodescanner.util.Constant


class ItemScanningFormatNativeHolder(val scanningFormatNativeAdHolderBinding: ItemSupportedCodeNativeHolderBinding) :
    RecyclerView.ViewHolder(scanningFormatNativeAdHolderBinding.root)

class ItemScanningFormat(val bindingScan: ItemSupportedCodesBinding) :
    RecyclerView.ViewHolder(bindingScan.root)


class ScanningTipsAdapter(val context: Context, val codeList:ArrayList<Any>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return ItemScanningFormat(ItemSupportedCodesBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
        return ItemScanningFormatNativeHolder(
            ItemSupportedCodeNativeHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun getItemCount(): Int {

        return codeList.size
    }
    override fun getItemViewType(position: Int): Int {
        if (codeList[position] is String) {
            return 1
        } else {
            return 0
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        var item = codeList[holder.layoutPosition]

        if (item is String) {//show ad

//            Constant.nativeId?.let {
//                loadNativeAd(holder)
//            }

        } else {
            (holder as ItemScanningFormat).bindingScan.supportedCodesTitle.text= (item as SupportedCodesModelClass).codeTitle

            Glide.with(context)
                .load(item.codeIcon)
                .into(holder.bindingScan.icon)

            holder.bindingScan.isSupportedIcon.imageTintList= ContextCompat.getColorStateList(context, R.color.whiteColorForBothThemes)

            if(item.isSupported){

                holder.bindingScan.isSupportedIcon.background= ContextCompat.getDrawable(context, R.drawable.shape_circle)
                holder.bindingScan.isSupportedIcon.backgroundTintList= ContextCompat.getColorStateList(context, R.color.mainBackGroundColorGreen)
                holder.bindingScan.isSupportedIcon.setImageResource(R.drawable.tick_icon)

                holder.bindingScan.isSupportedIcon.setPadding(15)
            }else{

                holder.bindingScan.isSupportedIcon.background= ContextCompat.getDrawable(context, R.drawable.shape_circle)
                holder.bindingScan.isSupportedIcon.backgroundTintList= ContextCompat.getColorStateList(context, R.color.redColor)
                holder.bindingScan.isSupportedIcon.setImageResource(R.drawable.cross_icon)
                holder.bindingScan.isSupportedIcon.setPadding(12)
            }

        }
    }

    fun loadNativeAd(holder: RecyclerView.ViewHolder) {

        if (AdHandler.getInstance(context).isNetworkAvailable(context)) {

            (holder as ItemScanningFormatNativeHolder).scanningFormatNativeAdHolderBinding.shimmerLayout.root.visibility =
                View.VISIBLE
            holder.scanningFormatNativeAdHolderBinding.adContainer.visibility = View.GONE

            AdHandler.getInstance(context).loadNativeAd(
                context,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {
                            holder.scanningFormatNativeAdHolderBinding.shimmerLayout.root.visibility = View.GONE
                            holder.scanningFormatNativeAdHolderBinding.adContainer.visibility = View.VISIBLE

                            AdHandler.getInstance(context).showNativeAdInSupportedCodesAdapter(
                                context,
                                holder .scanningFormatNativeAdHolderBinding.adContainer,
                                adObject as NativeAd
                            )
                        } else {

                            holder.scanningFormatNativeAdHolderBinding.shimmerLayout.root.visibility = View.VISIBLE
                            holder.scanningFormatNativeAdHolderBinding.adContainer.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {
                        holder.scanningFormatNativeAdHolderBinding.shimmerLayout.root.visibility = View.VISIBLE
                        holder.scanningFormatNativeAdHolderBinding.adContainer.visibility = View.GONE
                    }
                })

        } else {

            (holder as ItemScanningFormatNativeHolder).scanningFormatNativeAdHolderBinding.shimmerLayout.root.visibility = View.VISIBLE
            holder.scanningFormatNativeAdHolderBinding.adContainer.visibility = View.GONE
        }

    }

}