package scan.reader.qrscanner.barcodescanner.fragments



import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.activity.ScanResultAct
import scan.reader.qrscanner.barcodescanner.adapter.BarcodeListDbAdapter
import scan.reader.qrscanner.barcodescanner.dataBase.BarcodeDbRepo
import scan.reader.qrscanner.barcodescanner.dataBase.DataModel
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.util.Constant
import scan.reader.qrscanner.barcodescanner.viewModel.BarcodeListViewModel
import scan.reader.qrscanner.barcodescanner.databinding.DialogDeleteHistoryBinding
import scan.reader.qrscanner.barcodescanner.databinding.FragmentHistoryBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents


class HistoryFrag : Fragment() {

    interface HistoryInterface {

        fun setTotalSelected(total:Int)
    }

    lateinit var historyInterface: HistoryInterface

    lateinit var historyBinding:FragmentHistoryBinding

    lateinit var fragmentActivity:FragmentActivity

    var isAllSelected=false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        historyInterface=activity as HistoryInterface

        fragmentActivity=context as FragmentActivity
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(activity!=null) fragmentActivity=activity as FragmentActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        historyBinding= FragmentHistoryBinding.inflate(layoutInflater)

        getAllBarcodes()
        activity?.let {


            fragmentActivity.findViewById<View>(R.id.deleteButton).setOnClickListener {

               enableOldTabs()
                isAllSelected=false
                val selectedObject= allBarcodeList.filter { it -> it.isSelected==true }
                val noOfDeletedItem=selectedObject.size
               val selectedIdList= selectedObject.map { it.id }


                BarcodeDbRepo.deleteMultipleIds(fragmentActivity, selectedIdList)

                val deleteView=historyBinding.noOfBarcodeDeletedMessage
                deleteView.text="${noOfDeletedItem}  ${getString(R.string.entryDeleted)}"
                deleteView.visibility=View.VISIBLE
                deleteView.postDelayed(Runnable {
                   deleteView.visibility = View.GONE
                }, 800)
            }
            fragmentActivity.findViewById<View>(R.id.backButton).setOnClickListener{

                isAllSelected=false
                enableOldTabs()
                deSelectAll()


            }
            fragmentActivity.findViewById<View>(R.id.copyButton).setOnClickListener{

                enableOldTabs()
                isAllSelected=false
                val selectedObject= allBarcodeList.filter { it -> it.isSelected==true }
                val selectedBarcodeValue= selectedObject.map { it.barValue }

                deSelectAll()
                val clipboard: ClipboardManager = fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                val joinedStrings = selectedBarcodeValue.joinToString(separator = "\n")
                val clip = ClipData.newPlainText("List of strings", joinedStrings)
                clipboard.setPrimaryClip(clip)

            }
            fragmentActivity.findViewById<View>(R.id.optionsButton).setOnClickListener{

               // favPopUpWindow(fragmentActivity.findViewById<View>(R.id.optionsButton))

                moreFunctionMenu(fragmentActivity.findViewById<View>(R.id.optionsButton))

            }


        }

        historyBinding.deleteAll.setOnClickListener{

            if(allBarcodeList.size!=0){
                showDeleteEntireHistoryDialog()
            }
            // BarcodeDbRepo.deleteAllHistory(fragmentActivity)
        }
        return historyBinding.root

    }

    lateinit var deleteDialogBinding: DialogDeleteHistoryBinding

    fun showDeleteEntireHistoryDialog() {

        fragmentActivity.sendFirebaseEvents(fragmentActivity,"DeleteAllDialogOpenInHistoryFragment")
        fragmentActivity.sendAppMetricaEvents("DeleteAllDialogOpenInHistoryFragment")


        deleteDialogBinding = DialogDeleteHistoryBinding.inflate(layoutInflater)

        var dialog = MaterialAlertDialogBuilder(fragmentActivity,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(deleteDialogBinding.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener {
                override fun onCancel(dialog: DialogInterface?) {

                }
            })
            .show()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

//        loadNewNativeAd()

        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        deleteDialogBinding.cancel.setOnClickListener {

            dialog.cancel()
        }
        deleteDialogBinding.ok.setOnClickListener {

            BarcodeListViewModel().deleteAll(fragmentActivity)
            dialog.cancel()
        }
    }


    fun loadNewNativeAd() {

        if (AdHandler.getInstance(fragmentActivity).isNetworkAvailable(fragmentActivity)) {

            deleteDialogBinding.adContainer.visibility = View.GONE
            deleteDialogBinding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(fragmentActivity).loadNativeAd(
                fragmentActivity,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            deleteDialogBinding.adContainer.visibility = View.VISIBLE
                            deleteDialogBinding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(fragmentActivity).showNativeAd(
                                fragmentActivity,
                                deleteDialogBinding.adContainer,
                                adObject as NativeAd, false
                            )
                        } else {
                            deleteDialogBinding.adContainer.visibility = View.GONE
                            deleteDialogBinding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        deleteDialogBinding.adContainer.visibility = View.GONE
                        deleteDialogBinding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            deleteDialogBinding.adLinearLayout.visibility = View.GONE

        }

    }









    fun moreFunctionMenu(buttonView: View){

        val popup = PopupMenu(fragmentActivity, buttonView)

        popup.getMenuInflater().inflate(R.menu.history_barcodes_function_menu, popup.getMenu())

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {

                item.title.let{

                    if(item.title!!.equals("Add to favorites")){

                        enableOldTabs()

                        isAllSelected=false
                        val selectedObject= allBarcodeList.filter { it -> it.isSelected==true }
                        val selectedIdList= selectedObject.map { it.id }
                        BarcodeDbRepo.makeMultipleFav(fragmentActivity, selectedIdList,true)
                        popup.dismiss()

                    } else if(item.title!!.equals("Remove from favorites")){

                        enableOldTabs()
                        isAllSelected=false
                        val selectedObject= allBarcodeList.filter { it -> it.isSelected==true }
                        val selectedIdList= selectedObject.map { it.id }
                        BarcodeDbRepo.makeMultipleFav(fragmentActivity, selectedIdList,false)
                        popup.dismiss()
                    }

                }

                return true
            }
        })
        popup.show()
    }


    fun deSelectAll()
    {
        allBarcodeList.forEach { it.isSelected=false }
//        barcodeListAdapter.resetLongPressedAndCount()
        barcodeListAdapter?.notifyDataSetChanged()
    }


    fun enableOldTabs()
    {
        Constant.selctedBarcodeLayoutOpenInHistoryFrag=false
        historyBinding.deleteAll.visibility=View.VISIBLE
        barcodeListAdapter?.resetLongPressedAndCount()
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.multipleSelectedLayout).visibility=View.GONE
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.scan).isEnabled=true
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.create).isEnabled=true
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.history).isEnabled=true
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.settings).isEnabled=true
    }
    fun disableOldTabs()
    {
        Constant.selctedBarcodeLayoutOpenInHistoryFrag=true
        historyBinding.deleteAll.visibility=View.GONE
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.multipleSelectedLayout).visibility=View.VISIBLE
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.scan).isEnabled=false
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.create).isEnabled=false
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.history).isEnabled=false
        fragmentActivity.findViewById<View>(scan.reader.qrscanner.barcodescanner.R.id.settings).isEnabled=false
    }

//
//    fun favPopUpWindow(refrenceView:View)
//    {
//        var mypopupWindow: PopupWindow
//        var inflater = fragmentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
//       var view= inflater.inflate(  R.layout.item_fav_popup,null)
//
//        mypopupWindow = PopupWindow(view, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT, true)
//        mypopupWindow.showAsDropDown(refrenceView,0,0);
//
//        view.findViewById<View>(R.id.addToFav).setOnClickListener{
//
//            enableOldTabs()
//
//            val selectedObject= allBarcodeList.filter { it -> it.isSelected==true }
//            val selectedIdList= selectedObject.map { it.id }
//            BarcodeDbRepo.makeMultipleFav(fragmentActivity, selectedIdList,true)
//            mypopupWindow.dismiss()
//
//
//        }
//        view.findViewById<View>(R.id.removeFromFav).setOnClickListener{
//
//            enableOldTabs()
//            val selectedObject= allBarcodeList.filter { it -> it.isSelected==true }
//            val selectedIdList= selectedObject.map { it.id }
//            BarcodeDbRepo.makeMultipleFav(fragmentActivity, selectedIdList,false)
//            mypopupWindow.dismiss()
//        }
//    }

    lateinit var allBarcodeList:ArrayList<DataModel>
    var barcodeListAdapter: BarcodeListDbAdapter?=null

    fun getAllBarcodes()
    {
        BarcodeDbRepo.getAllBarcodes(fragmentActivity).observe(fragmentActivity, Observer {
            allBarcodeList =it as ArrayList<DataModel>

            val size=allBarcodeList.size
            if((size/10<1)) historyBinding.totalEntryInDb.text="0${size}"
            else  historyBinding.totalEntryInDb.text=size.toString()

            if(size==0){
                historyBinding.deleteAll.visibility=View.GONE
                historyBinding.noDataText.visibility=View.VISIBLE
                historyBinding.recylerView.visibility=View.GONE
            }else{
                historyBinding.deleteAll.visibility=View.VISIBLE
                historyBinding.noDataText.visibility=View.GONE
                historyBinding.recylerView.visibility=View.VISIBLE
                settingAdapter(allBarcodeList)
            }
        })

    }


    fun settingAdapter(barcodeList:ArrayList<DataModel>) {

        barcodeListAdapter= BarcodeListDbAdapter(
            fragmentActivity,
            barcodeList
        ) { isLongPressed,isLastItem, holderPosition ->

            historyInterface.setTotalSelected(barcodeListAdapter!!.count)
            if (isLongPressed) {

                historyBinding.deleteAll.visibility=View.GONE
                isAllSelected=true
                disableOldTabs()
            }
            else if(isLastItem){

                historyBinding.deleteAll.visibility=View.VISIBLE
                isAllSelected=false
                enableOldTabs()
            }
            else {
                historyBinding.deleteAll.visibility=View.VISIBLE
                var intent = Intent(fragmentActivity, ScanResultAct::class.java)
                intent.putExtra("barcodeData", barcodeList[holderPosition])
                startActivity(intent)

//                enableOldTabs()
            }
        }

        historyBinding.recylerView.layoutManager= LinearLayoutManager(fragmentActivity, LinearLayoutManager.VERTICAL,false)
        historyBinding.recylerView.adapter=barcodeListAdapter

    }


    override fun onResume() {
        fragmentActivity.sendFirebaseEvents(fragmentActivity,"OnResumeHistoryFragment")
        fragmentActivity.sendAppMetricaEvents("OnResumeHistoryFragment")

        super.onResume()
       // barcodeListAdapter.resetLongPressed()
    }

    override fun onDetach() {
        super.onDetach()

        barcodeListAdapter?.resetLongPressedAndCount()
    }

    fun deSelectAllFromOtherActivity():Boolean{
        if(isAllSelected){
            isAllSelected=false
            enableOldTabs()
            deSelectAll()
            return true
        }else{
            return false
        }

    }

}