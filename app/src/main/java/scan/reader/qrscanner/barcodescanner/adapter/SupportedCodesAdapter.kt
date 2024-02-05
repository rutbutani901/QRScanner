package scan.reader.qrscanner.barcodescanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ItemSupportedCodeNativeHolderBinding
import scan.reader.qrscanner.barcodescanner.databinding.ItemSupportedCodesBinding
import scan.reader.qrscanner.barcodescanner.modelClass.SupportedCodesModelClass
import scan.reader.qrscanner.barcodescanner.util.Constant

class ItemSupportedCodeNativeHolder(val supportedCodeNativeAdHolderBinding: ItemSupportedCodeNativeHolderBinding) :
    RecyclerView.ViewHolder(supportedCodeNativeAdHolderBinding.root)


class ItemSupportedCodes(val binding: ItemSupportedCodesBinding) :
    RecyclerView.ViewHolder(binding.root)


class SupportedCodesAdapter(val context: Context, val codeList: ArrayList<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 0) {
            return ItemSupportedCodes(
                ItemSupportedCodesBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
        return ItemSupportedCodeNativeHolder(
            ItemSupportedCodeNativeHolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


        var item = codeList[holder.layoutPosition]

        if (item is String) {//show ad

//                Constant.nativeId?.let {
//                    loadNativeAd(holder)
//                }

        } else {

            (holder as ItemSupportedCodes).binding.supportedCodesTitle.text =
                (item as SupportedCodesModelClass).codeTitle
            Glide.with(context)
                .load(item.codeIcon)
                .into(holder.binding.icon)

            holder.binding.isSupportedIcon.imageTintList =
                ContextCompat.getColorStateList(context, R.color.whiteColorForBothThemes)

            if (item.isSupported) {

                holder.binding.isSupportedIcon.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_circle)
                holder.binding.isSupportedIcon.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.mainBackGroundColorGreen)
                holder.binding.isSupportedIcon.setImageResource(R.drawable.tick_icon)

                holder.binding.isSupportedIcon.setPadding(15)
            } else {

                holder.binding.isSupportedIcon.background =
                    ContextCompat.getDrawable(context, R.drawable.shape_circle)
                holder.binding.isSupportedIcon.backgroundTintList =
                    ContextCompat.getColorStateList(context, R.color.redColor)
                holder.binding.isSupportedIcon.setImageResource(R.drawable.cross_icon)
                holder.binding.isSupportedIcon.setPadding(12)
            }
        }
    }

    override fun getItemCount(): Int {

        return codeList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (codeList[position] is String) {
            //show ad
            return 1
        } else {
            return 0
        }
    }

    fun loadNativeAd(holder: ViewHolder) {

        if (AdHandler.getInstance(context).isNetworkAvailable(context)) {

            (holder as ItemSupportedCodeNativeHolder).supportedCodeNativeAdHolderBinding.shimmerLayout.root.visibility =
                View.VISIBLE
            holder.supportedCodeNativeAdHolderBinding.adContainer.visibility = View.GONE

            AdHandler.getInstance(context).loadNativeAd(
                context,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {
                            holder.supportedCodeNativeAdHolderBinding.shimmerLayout.root.visibility = View.GONE
                            holder.supportedCodeNativeAdHolderBinding.adContainer.visibility = View.VISIBLE

                            AdHandler.getInstance(context).showNativeAdInSupportedCodesAdapter(
                                context,
                                (holder as ItemSupportedCodeNativeHolder).supportedCodeNativeAdHolderBinding.adContainer,
                                adObject as NativeAd
                            )
                        } else {

                            holder.supportedCodeNativeAdHolderBinding.shimmerLayout.root.visibility = View.VISIBLE
                            holder.supportedCodeNativeAdHolderBinding.adContainer.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {
                        holder.supportedCodeNativeAdHolderBinding.shimmerLayout.root.visibility = View.VISIBLE
                        holder.supportedCodeNativeAdHolderBinding.adContainer.visibility = View.GONE
                    }
                })

        } else {

            (holder as ItemSupportedCodeNativeHolder).supportedCodeNativeAdHolderBinding.shimmerLayout.root.visibility = View.VISIBLE
            holder.supportedCodeNativeAdHolderBinding.adContainer.visibility = View.GONE
        }

    }

}