package scan.reader.qrscanner.barcodescanner.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.adapter.ScanningTipsAdapter
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityScanningTipsBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.modelClass.SupportedCodesModelClass
import scan.reader.qrscanner.barcodescanner.util.Constant

class ScanningTipsAct : AppCompatActivity() {

    lateinit var binding: ActivityScanningTipsBinding

    var codeList = ArrayList<Any>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScanningTipsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sendFirebaseEvents(this,"ScanningTipsAct")
        sendAppMetricaEvents("ScanningTipsAct")


        getList()


        binding.backButton.setOnClickListener {
            onBackPressed()
        }

//        if(!(Constant.nativeId!=null && AdHandler.getInstance(this).isNetworkAvailable(this))){
//
//            codeList.removeAt(1)
//            codeList.removeAt(6)
//        }
        setAdapter(codeList)

    }

    fun getList() {
        codeList.add(
            SupportedCodesModelClass(
                getString(R.string.orientation90),
                R.drawable.orientation_ninety,
                true
            )
        )
        codeList.add("")
        codeList.add(
            SupportedCodesModelClass(
                getString(R.string.orientation0),
                R.drawable.orientation_zero,
                true
            )
        )
        codeList.add(
            SupportedCodesModelClass(
                getString(R.string.otherOrientation),
                R.drawable.orientaion_other,
                true
            )
        )
        codeList.add(
            SupportedCodesModelClass(
                getString(R.string.lightOrShadow),
                R.drawable.light_or_shadow_img,
                false
            )
        )
        codeList.add(
            SupportedCodesModelClass(
                getString(R.string.tooClose),
                R.drawable.blurry,
                false
            )
        )
        codeList.add(
            SupportedCodesModelClass(
                getString(R.string.ledWhenDark),
                R.drawable.led_when_dark_img,
                true
            )
        )
        codeList.add("")
        codeList.add(
            SupportedCodesModelClass(
                getString(R.string.lowContrast),
                R.drawable.low_constrast_img,
                false
            )
        )

    }

    lateinit var tipsAdapter: ScanningTipsAdapter
    fun setAdapter(suportedCodeList: ArrayList<Any>) {

        tipsAdapter = ScanningTipsAdapter(this, suportedCodeList)
        binding.recycler.setHasFixedSize(true)
        binding.recycler.setItemViewCacheSize(suportedCodeList.size)
        binding.recycler.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        binding.recycler.adapter = tipsAdapter

    }


}