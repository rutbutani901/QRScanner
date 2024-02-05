package scan.reader.qrscanner.barcodescanner.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.adapter.SupportedCodesAdapter
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivitySupportedCodesBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.modelClass.SupportedCodesModelClass
import scan.reader.qrscanner.barcodescanner.util.Constant

class SupportedCodesAct : AppCompatActivity() {

    lateinit var binding:ActivitySupportedCodesBinding

    var codeList= arrayListOf(

        SupportedCodesModelClass("QR", R.drawable.qr,true),
        "",
        SupportedCodesModelClass("Model 1 QR", R.drawable.model_one,false),
        SupportedCodesModelClass("Micro QR", R.drawable.micro_qr,false),
        SupportedCodesModelClass("EAN 13/JAN", R.drawable.ean_thirteen,true),
        SupportedCodesModelClass("EAN-8", R.drawable.ean_eight,true),
        SupportedCodesModelClass("EAN-5", R.drawable.ean_five,false),
        SupportedCodesModelClass("Code 25 interl. (itf)", R.drawable.code_twenty_five_inter,true),
        "",
        SupportedCodesModelClass("Code 25 Industrial", R.drawable.code_industrial,false),
        SupportedCodesModelClass("UPC-A", R.drawable.upca_img,true),//upca
        SupportedCodesModelClass("UPC-E", R.drawable.upce_img,true),//upce
        SupportedCodesModelClass("Codabar", R.drawable.code_twenty_five_inter,true),//upce
        SupportedCodesModelClass("Code 39", R.drawable.code_twenty_five_inter,true),//upce
        SupportedCodesModelClass("Code 93", R.drawable.code_twenty_five_inter,true),//upce
        "",
        SupportedCodesModelClass("Code 128", R.drawable.code_twenty_five_inter,true),//upce
        SupportedCodesModelClass("RSS-14/GS1 Databar", R.drawable.code_twenty_five_inter,true),//upce
        SupportedCodesModelClass("Aztec", R.drawable.aztec_img,true),//upce
        SupportedCodesModelClass("Data Matrix", R.drawable.qr,true),//upce
        SupportedCodesModelClass("PDF417", R.drawable.pdf_img,true),//upce
        SupportedCodesModelClass("Micro Pdf417", R.drawable.pdf_img,false),//upce
    )
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding= ActivitySupportedCodesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sendFirebaseEvents(this,"SupportedCodeActivity")
        sendAppMetricaEvents("SupportedCodeActivity")


        binding.backButton.setOnClickListener{
            onBackPressed()
        }
//        if(!(Constant.nativeId!=null && AdHandler.getInstance(this).isNetworkAvailable(this))){
//
//            codeList.removeAt(1)
//            codeList.removeAt(7)
//            codeList.removeAt(13)
//        }

        setAdapter(codeList)
    }

    lateinit var supprtedAdapter: SupportedCodesAdapter

    fun setAdapter(suportedCodeList:ArrayList<Any>) {//SupportedCodesModelClass

        supprtedAdapter= SupportedCodesAdapter(this, suportedCodeList)//SupportedCodesModelClass
        binding.recycler.setHasFixedSize(true)
        binding.recycler.setItemViewCacheSize(suportedCodeList.size)
        binding.recycler.layoutManager=  GridLayoutManager(this,2, GridLayoutManager.VERTICAL,false)
        binding.recycler.adapter=supprtedAdapter

    }

}