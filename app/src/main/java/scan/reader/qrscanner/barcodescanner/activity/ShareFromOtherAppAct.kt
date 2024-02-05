package scan.reader.qrscanner.barcodescanner.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityShareFromOtherAppBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant

class ShareFromOtherAppAct : AppCompatActivity() {

    lateinit var binding:ActivityShareFromOtherAppBinding
    override fun onCreate(savedInstanceState: Bundle?) {


        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        binding=ActivityShareFromOtherAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if(savedInstanceState==null){
//            Constant.interstitialId?.let {
//                loadInterAd()
//            }
//        }


        sendFirebaseEvents(this,"UseShareInOtherAppInsideCreateFragment")
        sendAppMetricaEvents("UseShareInOtherAppInsideCreateFragment")



        binding.backButton.setOnClickListener{
            onBackPressed()
        }

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
                    AdHandler.getInstance(this@ShareFromOtherAppAct).dismissProgress(this@ShareFromOtherAppAct)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}