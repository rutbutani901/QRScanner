package scan.reader.qrscanner.barcodescanner.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityHelpBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.util.Constant


class HelpAct : AppCompatActivity() {

    lateinit var binding: ActivityHelpBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding=ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        if(savedInstanceState==null){
//            Constant.interstitialId?.let {
//                loadInterAd()
//            }
//        }

        sendFirebaseEvents(this,"HelpActivity")
        sendAppMetricaEvents("HelpActivity")

        binding.backButton.setOnClickListener{
            onBackPressed()
        }

        binding.supportedCodes.setOnClickListener{
            startActivity(Intent(this, SupportedCodesAct::class.java))
        }
        binding.tips.setOnClickListener{
            startActivity(Intent(this, ScanningTipsAct::class.java))
        }
        binding.feedBack.setOnClickListener{

            sendFirebaseEvents(this,"FeedBackClickHelpActivity")
            sendAppMetricaEvents("FeedBackClickHelpActivity")

            feedBack()
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
                    AdHandler.getInstance(this@HelpAct).dismissProgress(this@HelpAct)
                }
            }
        }
    }

    fun Context.feedBack(){

        val intent= Intent(Intent.ACTION_SEND)
        var receiptant= arrayOf(resources.getString(R.string.feedBackEmail))
        intent.putExtra(Intent.EXTRA_EMAIL,receiptant)
        intent.putExtra(Intent.EXTRA_SUBJECT,"FeedBack")
        intent.putExtra(Intent.EXTRA_CC,resources.getString(R.string.feedBackEmail))
        intent.type="text/html"
        intent.setPackage("com.google.android.gm")
        startActivity(Intent.createChooser(intent,"Send Mail"))
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}