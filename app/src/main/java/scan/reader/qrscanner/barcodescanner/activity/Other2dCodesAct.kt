package scan.reader.qrscanner.barcodescanner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ActivityOther2dCodesBinding

import scan.reader.qrscanner.barcodescanner.adapter.OtherBarcodesShowerAdapter
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.modelClass.OtherBarcodeTypeModelClass

import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant

class Other2dCodesAct : AppCompatActivity() {

    lateinit var other2dCodesBinding: ActivityOther2dCodesBinding
   var barcodeList= ArrayList<OtherBarcodeTypeModelClass>()


    override fun onCreate(savedInstanceState: Bundle?) {

        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)


        other2dCodesBinding= ActivityOther2dCodesBinding.inflate(layoutInflater)
        setContentView(other2dCodesBinding.root)

//        if(savedInstanceState==null){
//            Constant.interstitialId?.let {
//                loadInterAd()
//            }
//        }

        sendFirebaseEvents(this,"BarcodeAndOther2DCodesActFromCreateFrag")
        sendAppMetricaEvents("BarcodeAndOther2DCodesActFromCreateFrag")

        createList()
        settingAdapter(barcodeList)

        other2dCodesBinding.backButton.setOnClickListener{
            onBackPressed()
        }
    }

    lateinit var adapter: OtherBarcodesShowerAdapter

    fun createList(){
        barcodeList.add( OtherBarcodeTypeModelClass("QR CODE",getString(R.string.text),256,R.drawable.other_barcodes_qr_icon))
        barcodeList.add( OtherBarcodeTypeModelClass("Data Matrix",getString(R.string.textWithoutSpecialCharacters),16,R.drawable.other_barcodes_data_matrix_icon))
        barcodeList.add( OtherBarcodeTypeModelClass("PDF 417",getString(R.string.text),2048,R.drawable.other_barcodes_pdf_icon))
        barcodeList.add(OtherBarcodeTypeModelClass("AZtec",getString(R.string.textWithoutSpecialCharacters),4096))
        barcodeList.add(  OtherBarcodeTypeModelClass("EAN13",getString(R.string.twelveDigitsWithOneChecksum),32))
        barcodeList.add( OtherBarcodeTypeModelClass("EAN8",getString(R.string.eightDigits),64))
        barcodeList.add(  OtherBarcodeTypeModelClass("UPC E",getString(R.string.sevenDigitsWithOneChecksum),1024))
        barcodeList.add(  OtherBarcodeTypeModelClass("UPC A",getString(R.string.elevenDigitsWithOneChecksum),512))
        barcodeList.add(OtherBarcodeTypeModelClass("Code 128",getString(R.string.textWithoutSpecialCharacters),1))
        barcodeList.add( OtherBarcodeTypeModelClass("Code 93",getString(R.string.textInUpperWithoutSpecial),4))
        barcodeList.add(OtherBarcodeTypeModelClass("Code 39",getString(R.string.textInUpperWithoutSpecial),2))
        barcodeList.add( OtherBarcodeTypeModelClass("Codabar",getString(R.string.digits),8))
        barcodeList.add(  OtherBarcodeTypeModelClass("ITF",getString(R.string.evenNumberOfDigits),128))

    }
    fun settingAdapter(barcodes:ArrayList<OtherBarcodeTypeModelClass>)
    {
        adapter= OtherBarcodesShowerAdapter(this,barcodes) {
            position->
            val barcodeData= barcodes.get(position)

            sendFirebaseEvents(this,"${barcodeData.barcodeName}_click_BarcodeAndOther2DCodesActFromCreateFrag")
            sendAppMetricaEvents("${barcodeData.barcodeName}_click_BarcodeAndOther2DCodesActFromCreateFrag")


            val intent=Intent(this, OtherBarcodesInputAct::class.java)
            intent.putExtra("barcodeData",barcodeData)

            startActivity(intent)
        }

        other2dCodesBinding.recyler.layoutManager=
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        other2dCodesBinding.recyler.adapter=adapter
    }

    fun loadInterAd(){

        if(AdHandler.getInstance(this).isNetworkAvailable(this)){

            if(AdHandler.getInstance(this).interstitialAd!=null){
                AdHandler.getInstance(this).loadInterstitialAdWithRandomNumber(this,
                    Constant.interstitialId,1
                ) { Log.d("adclosed", "yes") }
            }else{
                AdHandler.getInstance(this).showProgress(this)
                AdHandler.getInstance(this).loadSplashInter(this, Constant.interstitialId
                ) {
                    AdHandler.getInstance(this@Other2dCodesAct).dismissProgress(this@Other2dCodesAct)
                }
            }
        }
    }
}