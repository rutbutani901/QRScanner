package scan.reader.qrscanner.barcodescanner.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.android.billingclient.api.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivitySplashBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant


class SplashAct : AppCompatActivity() {
    lateinit var splashBinding:ActivitySplashBinding

    private var isAdLoadTimeExceeded=false
    var isAdLoadingStarted=false

    var billingClient:BillingClient?=null

    private lateinit var remoteConfig:FirebaseRemoteConfig
//    private lateinit var firebaseAnalytics:FirebaseAnalytics
    

    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)

        splashBinding=ActivitySplashBinding.inflate(layoutInflater)
        setContentView(splashBinding.root)

        remoteConfig= Firebase.remoteConfig


        sendFirebaseEvents(this,"SplashActivity")
        sendAppMetricaEvents("SplashActivity")

        Constant.isSplashScreen=true
        billingClient=BillingClient.newBuilder(this)
            .setListener{billingResult:BillingResult?,
                            list:List<Purchase>?->}
            .enablePendingPurchases()
            .build()

        if(AdHandler.getInstance(this).isNetworkAvailable(this)){
            billingClient!!.startConnection(object :BillingClientStateListener{
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if(billingResult.responseCode==BillingClient.BillingResponseCode.OK){
                        billingClient!!.queryPurchasesAsync(
                            QueryPurchasesParams.newBuilder()
                                .setProductType(BillingClient.ProductType.INAPP)
                                .build()

                        ){billingResult: BillingResult?, list:List<Purchase>->
                            var isAnyPurchaseFound=false
                            for(purchase in list){
                                val productList= purchase.products as ArrayList<String>

                                for(sku in productList){
//                                    if(sku.equals("qremoveads_99",ignoreCase = true)){
                                    if(sku.equals("",ignoreCase = true)){
                                        sharedPref.isPremiumPurchased=true
                                        isAnyPurchaseFound=true
                                        break
                                    }
                                }
                            }
                            if(!isAnyPurchaseFound){

                                loadAdsId()
                                sharedPref.isPremiumPurchased=false
                            }
                        }
                    }
                }
                override fun onBillingServiceDisconnected() {}
            })
        }

        if(!sharedPref.privacyPolicyAccepted){

            splashBinding.policyLayout.visibility=View.VISIBLE
            splashBinding.adProgressLayout.visibility=View.GONE

            val policyText=HtmlCompat.fromHtml(
                "By clicking \"Get Started\", you indicate that you have read our"+
                        "<span style='color:#F0AF51'><a href='"+getString(R.string.privacyPolicyLink)
                        +"'> Privacy Policy</a></span>",
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )

            splashBinding.policyText.text=policyText
            splashBinding.policyText.movementMethod=LinkMovementMethod.getInstance()



            splashBinding.getStartedButton.setOnClickListener{

                sharedPref.privacyPolicyAccepted=true
                startNextScreen()
            }
        }
        else{

            splashBinding.policyLayout.visibility=View.GONE

            if(!sharedPref.isPremiumPurchased){
                if(AdHandler.getInstance(this).isNetworkAvailable(this)){
                    splashBinding.adProgressLayout.visibility=View.GONE
                }else{
                    splashBinding.adProgressLayout.visibility=View.GONE
                }

            }else{
                splashBinding.adProgressLayout.visibility=View.GONE
            }
            startNextScreen()
        }
        Handler(Looper.getMainLooper()).postDelayed({
            isAdLoadTimeExceeded=true
        },3500)
    }

    fun startNextScreen(){

        Thread{
            while (!AdHandler.getInstance(this).isNativeAdLoad && !AdHandler.getInstance(this).isNativeAdLoadFailed && !isAdLoadTimeExceeded){
            }
            runOnUiThread{
                callNextActivity()
            }
        }.start()
    }

    private fun callNextActivity(){
        if(!sharedPref.isAppIntroShown){
            sharedPref.isAppIntroShown=true

            startActivity(Intent(this, AppIntroAct::class.java))
            finish()
        }
        else if(sharedPref.languageCode.isEmpty()){

            startActivity( Intent(this, LanguageAct::class.java))
            finish()
        }
        else{

            var intent=Intent(this, MainScannerActivity::class.java)
            intent.putExtra("fromSplash",true)
            startActivity(intent )
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Constant.isSplashScreen=false
        isAdLoadingStarted=false
    }


    private fun loadAds(){
        isAdLoadingStarted=true


//        Constant.interstitialId?.let {
//            AdHandler.getInstance(this).loadInterstitialAd(this, Constant.interstitialId)//getString(R.string.inter_id)
//        }
//
        Constant.nativeId?.let {
            AdHandler.getInstance(this).loadNativeAd(this,
                Constant.nativeId
                ,object : AdEventListener {//getString(R.string.language_native_id)
                override fun onAdLoaded(`object`: Any?) {}
                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {}
                })
        }
    }


    fun loadAdsId(){

        val configSettings= remoteConfigSettings {
            minimumFetchIntervalInSeconds=3600// fetches data in once in a hour
        }
        remoteConfig.setConfigSettingsAsync(configSettings)


        remoteConfig.fetchAndActivate().addOnCompleteListener(this){task->
            if(task.isSuccessful){
                Constant.appMainId=remoteConfig.getString("ad_id")
                Constant.appOpenId= remoteConfig.getString("appOpenId")
                Constant.bannerId= remoteConfig.getString("banner")
                Constant.interstitialId= remoteConfig.getString("interstitial")
                Constant.nativeId= remoteConfig.getString("native")

                if(!isAdLoadingStarted) loadAds()
            }else{
                Constant.appMainId=null
                Constant.appOpenId= null
                Constant.bannerId= null
                Constant.interstitialId= null
                Constant.nativeId= null
            }
        }




//        remoteConfig.addOnConfigUpdateListener(object :ConfigUpdateListener{
//            override fun onUpdate(configUpdate: ConfigUpdate) {
//                remoteConfig.activate().addOnCompleteListener{task->
//                    if(task.isSuccessful){
//                        val canShowAd=remoteConfig.getBoolean("can_show_ad")
//                        if(canShowAd){
//                            Constant.appMainId=remoteConfig.getString("ad_id")
//                            Constant.appOpenId= remoteConfig.getString("appOpenId")
//                            Constant.bannerId= remoteConfig.getString("banner")
//                            Constant.interstitialId= remoteConfig.getString("interstitial")
//                            Constant.nativeId= remoteConfig.getString("native")
//                        }else{
//
//                            Constant.appMainId=null
//                            Constant.appOpenId= null
//                            Constant.bannerId= null
//                            Constant.interstitialId= null
//                            Constant.nativeId= null
//                        }
//
//
//                        if(!isAdLoadingStarted) loadAds()
//                    }else{
//                        Log.d("remocteconfiff","failed")
//                    }
//                }
//
//            }
//
//            override fun onError(error: FirebaseRemoteConfigException) {
//
//            }
//        })




//            ApiClient.getApiClient().getAdIds().enqueue(object :retrofit2.Callback<AdsPojo>{
//                override fun onResponse(call: Call<AdsPojo>, response: Response<AdsPojo>) {
//                    Constant.appMainId=  response.body()?.Ids?.AppID
//                    Constant.appOpenId= response.body()?.Ids?.AppOpen
//                    Constant.bannerId= response.body()?.Ids?.Banner
//                    Constant.interstitialId= response.body()?.Ids?.Interstitial
//                    Constant.nativeId= response.body()?.Ids?.Native
//                    if(!isAdLoadingStarted) loadAds()
//                }
//                override fun onFailure(call: Call<AdsPojo>, t: Throwable) {
//                    Log.d("AddLoading","Faileds")
//                }
//            })
    }
}