package scan.reader.qrscanner.barcodescanner.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import scan.reader.qrscanner.barcodescanner.databinding.ActivityLanguageBinding
import scan.reader.qrscanner.barcodescanner.adapter.LanguageAdapter
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.modelClass.LangModelClass

import scan.reader.qrscanner.barcodescanner.viewModel.LanguageViewModel
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler

import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents

class LanguageAct : AppCompatActivity() {

    lateinit var binding: ActivityLanguageBinding
    lateinit var viewModel: LanguageViewModel
    var cameFromSettings = false

    var tempLangCode=""

    override fun onCreate(savedInstanceState: Bundle?) {

        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        Constant.interstitialId?.let {
//            showInterAd()
//        }


        viewModel = ViewModelProvider(this).get(LanguageViewModel::class.java)

        val languageList = viewModel.getLanList()

        cameFromSettings = intent.getBooleanExtra("cameFromSetting", false)
        if (cameFromSettings) {

            sendFirebaseEvents(this,"LangActFromSettings")
            sendAppMetricaEvents("LangActFromSettings")

            Constant.nativeId?.let {
                loadNewNativeAd()
            }
            binding.backButton.visibility = View.VISIBLE

            val lanCode = sharedPref.languageCode
            tempLangCode=lanCode
            languageList.find { it.isSelected }?.isSelected = false
            languageList.find { it.lanCode.equals(lanCode) }?.isSelected = true

        } else {

            sendFirebaseEvents(this,"LangActFromSplash")
            sendAppMetricaEvents("LangActFromSplash")


            Constant.nativeId?.let {
                    loadNativeAd()
                }
            binding.backButton.visibility = View.GONE
        }
        binding.backButton.setOnClickListener {
            onBackPressed()
        }


        setAdapter(languageList)
        binding.tickButton.setOnClickListener {
            sharedPref.languageCode=lanSelec
            setResult(RESULT_OK)
            if (sharedPref.languageCode.isEmpty()) {
                sharedPref.languageCode = "en"
            }
            setLocale(sharedPref.languageCode)
            val intent = Intent(this, MainScannerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (cameFromSettings) intent.putExtra("fromSplash", false)
            else intent.putExtra("fromSplash", true)
            startActivity(intent)
            finish()
        }
    }

    lateinit var languageAdapter: LanguageAdapter

    var lanSelec=""
    fun setAdapter(languageList: ArrayList<LangModelClass>) {

        languageAdapter = LanguageAdapter(this, languageList) { lanCode ->
//            sharedPref.languageCode = lanCode
            lanSelec= lanCode
        }
        binding.recylerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recylerView.adapter = languageAdapter

    }

    fun loadNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            binding.shimmerLayout.root.visibility = View.VISIBLE
            binding.adContainer.visibility = View.GONE

            if (AdHandler.getInstance(this).nativeAd != null) {

                binding.adContainer.visibility = View.VISIBLE
                binding.shimmerLayout.root.visibility = View.GONE
                AdHandler.getInstance(this).showNativeAd(
                    this,
                    binding.adContainer,
                    AdHandler.getInstance(this).nativeAd, true
                )

            } else {
                loadNewNativeAd()
            }
        } else {
            binding.adLinearLayout.visibility = View.GONE
        }
    }


    fun loadNewNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            binding.adContainer.visibility = View.GONE
            binding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            binding.adContainer.visibility = View.VISIBLE
                            binding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(this@LanguageAct).showNativeAd(
                                this@LanguageAct,
                                binding.adContainer,
                                adObject as NativeAd, true
                            )
                        } else {
                            binding.adContainer.visibility = View.GONE
                            binding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        binding.adContainer.visibility = View.GONE
                        binding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            binding.adLinearLayout.visibility = View.GONE

        }

    }

    fun showInterAd(){

        if(AdHandler.getInstance(this).isNetworkAvailable(this)){

            if(AdHandler.getInstance(this).interstitialAd!=null){
                AdHandler.getInstance(this).loadInterstitialAdWithRandomNumber(this, Constant.interstitialId,1) {
                    Log.d("adclosed", "yes")

                }
            }else{
                AdHandler.getInstance(this).showProgress(this)
                AdHandler.getInstance(this).loadSplashInter(this, Constant.interstitialId
                ) {
                    AdHandler.getInstance(this@LanguageAct).dismissProgress(this@LanguageAct)
                }
            }
            loadInterAdForMainScreen()
        }
    }

    fun loadInterAdForMainScreen(){
        Constant.interstitialId?.let {
            AdHandler.getInstance(this).loadInterstitialAd(this, Constant.interstitialId)//getString(R.string.inter_id)
        }
    }


    override fun onBackPressed() {
        sharedPref.languageCode = tempLangCode
        val languageList = viewModel.getLanList()
        languageList.find { it.isSelected }?.isSelected = false
        languageList.find { it.lanCode.equals(tempLangCode) }?.isSelected = true
        super.onBackPressed()

    }

    override fun onDestroy() {

        super.onDestroy()
    }
}