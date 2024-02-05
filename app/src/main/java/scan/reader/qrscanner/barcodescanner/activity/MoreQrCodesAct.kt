package scan.reader.qrscanner.barcodescanner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityMoreQrCodesBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents

import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant

class MoreQrCodesAct : AppCompatActivity() {

    lateinit var moreQrbinding: ActivityMoreQrCodesBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        setLocale(sharedPref.languageCode)

        super.onCreate(savedInstanceState)
        moreQrbinding= ActivityMoreQrCodesBinding.inflate(layoutInflater)
        setContentView(moreQrbinding.root)

//        if(savedInstanceState==null){
//            Constant.interstitialId?.let {
//                loadInterAd()
//            }
//        }
//        Constant.nativeId?.let {
//            loadNewNativeAd()
//        }

        sendFirebaseEvents(this,"MoreQrCodeActFromCreateFrag")
        sendAppMetricaEvents("MoreQrCodeActFromCreateFrag")

        moreQrbinding.backButton.setOnClickListener{
            onBackPressed()
        }

        moreQrbinding.textQr.setOnClickListener{

            startActivity(Intent(this, QrTextAct::class.java))
        }
        moreQrbinding.emailQr.setOnClickListener{

            startActivity(Intent(this, QrEmailAct::class.java))
        }
        moreQrbinding.smsQr.setOnClickListener{

            startActivity(Intent(this, QrSmsAct::class.java))
        }
        moreQrbinding.appQr.setOnClickListener{

            startActivity(Intent(this, QrAppAct::class.java))
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
                    AdHandler.getInstance(this@MoreQrCodesAct).dismissProgress(this@MoreQrCodesAct)
                }
            }
        }
    }



    fun loadNewNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            moreQrbinding.adContainer.visibility = View.GONE
            moreQrbinding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            moreQrbinding.adContainer.visibility = View.VISIBLE
                            moreQrbinding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(this@MoreQrCodesAct).showNativeAd(
                                this@MoreQrCodesAct,
                                moreQrbinding.adContainer,
                                adObject as NativeAd, true
                            )
                        } else {
                            moreQrbinding.adContainer.visibility = View.GONE
                            moreQrbinding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        moreQrbinding.adContainer.visibility = View.GONE
                        moreQrbinding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            moreQrbinding.adLinearLayout.visibility = View.GONE

        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
    }

}