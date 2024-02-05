package scan.reader.qrscanner.barcodescanner.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxInterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import com.willy.ratingbar.BaseRatingBar
import com.willy.ratingbar.BaseRatingBar.OnRatingChangeListener
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityScannerBinding
import scan.reader.qrscanner.barcodescanner.databinding.DialogExitBinding
import scan.reader.qrscanner.barcodescanner.databinding.DialogRateNewBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.fragments.CreateFrag
import scan.reader.qrscanner.barcodescanner.fragments.HistoryFrag
import scan.reader.qrscanner.barcodescanner.fragments.ScanFrag
import scan.reader.qrscanner.barcodescanner.fragments.SettingsFrag
import scan.reader.qrscanner.barcodescanner.util.Constant
import scan.reader.qrscanner.barcodescanner.viewModel.ScannerActivityViewModel
import scan.reader.qrscanner.barcodescanner.viewPager.FragmentsShowerViewPager
import java.util.concurrent.TimeUnit


class MainScannerActivity : AppCompatActivity(), HistoryFrag.HistoryInterface {//,ZXingScannerView.ResultHandler

    lateinit var scannerBinding:ActivityScannerBinding
    lateinit var fragmentsShowerViewPager: FragmentsShowerViewPager
    var prevSelectedTab=-1
    lateinit var viewModel: ScannerActivityViewModel

    private lateinit var interstitialAd: MaxInterstitialAd
    private var retryAttempt = 0.0

    fun createInterstitialAd()
    {
        interstitialAd = MaxInterstitialAd( "28ebbe8ee4e52231", this )
        interstitialAd.setListener( object : MaxAdListener{
            override fun onAdLoaded(p0: MaxAd) {

                Log.d("9988","onAdLoaded")

                if(interstitialAd.isReady )
                {
                    Log.d("checkingInterAD","false")
                    interstitialAd.showAd()
                }else{
                    Log.d("checkingInterAD","true")
                }
            }

            override fun onAdDisplayed(p0: MaxAd) {
                Log.d("9988","onAdDisplayed")
            }

            override fun onAdHidden(p0: MaxAd) {
                 Log.d("9988","onAdHidden")

            }

            override fun onAdClicked(p0: MaxAd) {
                 Log.d("9988","onAdClicked")

            }

            override fun onAdLoadFailed(p0: String, p1: MaxError) {
                 Log.d("9988","onAdLoadFailed")

            }

            override fun onAdDisplayFailed(p0: MaxAd, p1: MaxError) {
                 Log.d("9988","onAdDisplayFailed")

            }
        })

        // Load the first ad
        interstitialAd.loadAd()
    }

//    // MAX Ad Listener
//    override fun onAdLoaded(maxAd: MaxAd)
//    {
//        if(interstitialAd.isReady )
//        {
//            Log.d("checkingInterAD","false")
//            interstitialAd.showAd()
//        }else{
//            Log.d("checkingInterAD","true")
//        }
//        // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'
//
//        // Reset retry attempt
//        retryAttempt = 0.0
//    }
//
//    override fun onAdLoadFailed(adUnitId: String, error: MaxError)
//    {
//        Log.d("9988","onAdLoadFailed")
//        // Interstitial ad failed to load
//        // AppLovin recommends that you retry with exponentially higher delays up to a maximum delay (in this case 64 seconds)
//
//        retryAttempt++
//        val delayMillis = TimeUnit.SECONDS.toMillis( Math.pow( 2.0, Math.min( 6.0, retryAttempt ) ).toLong() )
//
//        Handler().postDelayed( {
//            interstitialAd.loadAd()
//                               }, delayMillis )
//    }
//
//    override fun onAdDisplayFailed(ad: MaxAd, error: MaxError)
//    {
//        Log.d("9988","onAdDisplayFailed")
//
//        // Interstitial ad failed to display. AppLovin recommends that you load the next ad.
//        interstitialAd.loadAd()
//    }
//
//    override fun onAdDisplayed(maxAd: MaxAd) {
//        Log.d("9988","onAdDisplayed")
//
//    }
//
//    override fun onAdClicked(maxAd: MaxAd) {
//        Log.d("9988","onAdClicked")
//
//    }
//
//    override fun onAdHidden(maxAd: MaxAd)
//    {
//        Log.d("9988","onAdHidden")
//
//        // Interstitial ad is hidden. Pre-load the next ad
//        interstitialAd.loadAd()
//    }


    override fun onCreate(savedInstanceState: Bundle?) {

        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        scannerBinding=ActivityScannerBinding.inflate(layoutInflater)
        setContentView(scannerBinding.root)

//        Constant.bannerId?.let {
//            loadAdaptiveBannerAd()
//        }

        sendFirebaseEvents(this,"ScanerAct")
        sendAppMetricaEvents("ScanerAct")

        if(savedInstanceState==null){
            var fromSplash= intent.getBooleanExtra("fromSplash",false)
            if(fromSplash){


//                Constant.interstitialId?.let {
//                    loadInterAd()
//
//                }
            }
        }

        viewModel=ViewModelProvider(this).get(ScannerActivityViewModel::class.java)
        createFragments()
    }
     var scanFragment= ScanFrag()
     var createFragment= CreateFrag()
     var historyFragment= HistoryFrag()
     var settingsFragment= SettingsFrag()
    fun createFragments() {

        val fragList= arrayListOf(
            scanFragment,
            createFragment,
            historyFragment,
            settingsFragment
        )
        fragmentsShowerViewPager= FragmentsShowerViewPager(this,fragList)
        scannerBinding.viewPager.adapter= fragmentsShowerViewPager
        scannerBinding.viewPager.offscreenPageLimit= fragmentsShowerViewPager.itemCount
        scannerBinding.viewPager.currentItem=viewModel.getPreviousFragmentIndex()

        when(viewModel.getPreviousFragmentIndex()) {
            0->{
                scannerBinding.scanindicator.visibility=View.VISIBLE
            }
            1->{
                scannerBinding.createindicator.visibility=View.VISIBLE
            }
            2->{
                scannerBinding.historyindicator.visibility=View.VISIBLE
            }
            3->{
                scannerBinding.settingindicator.visibility=View.VISIBLE
            }
        }
        scannerBinding.viewPager.registerOnPageChangeCallback(
            object :ViewPager2.OnPageChangeCallback(){

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if(viewModel.getPreviousFragmentIndex()==2){
                        historyFragment.deSelectAllFromOtherActivity()
                    }
                    when(position) {
                        0->{
                            scannerBinding.scanindicator.visibility=View.VISIBLE
                            scannerBinding.createindicator.visibility=View.GONE
                            scannerBinding.historyindicator.visibility=View.GONE
                            scannerBinding.settingindicator.visibility=View.GONE
                        }
                        1->{
                            scannerBinding.scanindicator.visibility=View.GONE
                            scannerBinding.createindicator.visibility=View.VISIBLE
                            scannerBinding.historyindicator.visibility=View.GONE
                            scannerBinding.settingindicator.visibility=View.GONE
                        }
                        2->{
                            scannerBinding.scanindicator.visibility=View.GONE
                            scannerBinding.createindicator.visibility=View.GONE
                            scannerBinding.historyindicator.visibility=View.VISIBLE
                            scannerBinding.settingindicator.visibility=View.GONE
                        }
                        3->{
                            scannerBinding.scanindicator.visibility=View.GONE
                            scannerBinding.createindicator.visibility=View.GONE
                            scannerBinding.historyindicator.visibility=View.GONE
                            scannerBinding.settingindicator.visibility=View.VISIBLE
                        }
                    }
                    viewModel.setpreviousFragmentIndex(position)
                }
            })

        scannerBinding.scan.setOnClickListener{
            scannerBinding.viewPager.currentItem=0
            viewModel.setpreviousFragmentIndex(0)
        }
        scannerBinding.create.setOnClickListener {
            scannerBinding.viewPager.currentItem=1
            viewModel.setpreviousFragmentIndex(1)
        }

        scannerBinding.history.setOnClickListener {
            scannerBinding.viewPager.currentItem=2
            viewModel.setpreviousFragmentIndex(2)
        }
        scannerBinding.settings.setOnClickListener {
            scannerBinding.viewPager.currentItem=3
            viewModel.setpreviousFragmentIndex(3)
        }
    }

    override fun setTotalSelected(total: Int) {

        scannerBinding.totalSelected.text=total.toString()
    }
    override fun onBackPressed() {

        if(scannerBinding.viewPager.currentItem!=0) {
            if(scannerBinding.viewPager.currentItem==2){

                val wasAllSelected=historyFragment.deSelectAllFromOtherActivity()
                if(!wasAllSelected){

                    scannerBinding.viewPager.currentItem=0
                    viewModel.setpreviousFragmentIndex(0)
                }

            }else{
                scannerBinding.viewPager.currentItem=0
                viewModel.setpreviousFragmentIndex(0)
            }

        }else
        {
            val isAppRated=sharedPref.isAppRated
            if(!isAppRated){
                showNewRateDialog()
            }else {
                showExitDialog()
            }
        }
    }
    override fun onResume() {
        super.onResume()
       // scannerBinding.viewPager.currentItem=viewModel.getPreviousFragmentIndex()
    }


    lateinit var  newRateDialogBinding:DialogRateNewBinding
    fun showNewRateDialog() {

        sendFirebaseEvents(this,"RateDialogOpenInScannerActivity")
        sendAppMetricaEvents("RateDialogOpenInScannerActivity")

        newRateDialogBinding= DialogRateNewBinding.inflate(layoutInflater)
        val dialog= MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(newRateDialogBinding.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {}

            })
            .show()
        var totalRating=5
        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        newRateDialogBinding.ratingBar.setOnRatingChangeListener(object : OnRatingChangeListener {
            override fun onRatingChange(
                ratingBar: BaseRatingBar?,
                rating: Float,
                fromUser: Boolean
            ) {
                totalRating= rating.toInt()
                if(rating>2.0){
                    newRateDialogBinding.feedBackButton.text=getString(R.string.rateUs)
                }else{
                    newRateDialogBinding.feedBackButton.text=getString(R.string.giveFeedBack)
                }
            }
        })
        dialog.setCanceledOnTouchOutside(false)
        dialog.setCancelable(true)

        newRateDialogBinding.crossButton.setOnClickListener{
            dialog.cancel()
        }
        newRateDialogBinding.feedBackButton.setOnClickListener{

            sharedPref.isAppRated=true
            if(totalRating>2){
                rate()
            }else{
                feedBack()
            }
            dialog.cancel()
        }
        newRateDialogBinding.exitText.setOnClickListener{
            finish()
        }
    }
    override fun finish() {

        AdHandler.getInstance(this).isAdLoaded=false
        AdHandler.getInstance(this).interstitialAd=null
        super.finishAndRemoveTask()
    }

    fun Context.rate(){
        val url="https://play.google.com/store/apps/details?id=${packageName}"
        val intent= Intent(Intent.ACTION_VIEW)
        intent.flags= Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data= Uri.parse(url)
        startActivity(intent)
    }
    fun Context.feedBack(){

        val intent=Intent(Intent.ACTION_SEND)
        val receiptant=arrayOf(resources.getString(R.string.feedBackEmail))
        intent.putExtra(Intent.EXTRA_EMAIL,receiptant)
        intent.putExtra(Intent.EXTRA_SUBJECT,"FeedBack")
        intent.putExtra(Intent.EXTRA_CC,resources.getString(R.string.feedBackEmail))
        intent.type="text/html"
        intent.setPackage("com.google.android.gm")
        startActivity(Intent.createChooser(intent,"Send Mail"))
    }

    lateinit var exitDialogBinding: DialogExitBinding
    fun showExitDialog(){

        sendFirebaseEvents(this,"ExitDialogCalledInScannerActivity")
        sendAppMetricaEvents("ExitDialogCalledInScannerActivity")

        exitDialogBinding= DialogExitBinding.inflate(layoutInflater)
        val dialog= MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(exitDialogBinding.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {
                    //DIALOG CANCEL
                }

            })
            .show()


        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCanceledOnTouchOutside(false)

        loadNewNativeAd()
        exitDialogBinding.cancel.setOnClickListener{
            dialog.cancel()

        }
        exitDialogBinding.ok.setOnClickListener{
            dialog.cancel()
            finish()
        }
    }

    fun loadNewNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            exitDialogBinding.adContainer.visibility = View.GONE
            exitDialogBinding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            exitDialogBinding.adContainer.visibility = View.VISIBLE
                            exitDialogBinding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(this@MainScannerActivity).showNativeAd(
                                this@MainScannerActivity,
                                exitDialogBinding.adContainer,
                                adObject as NativeAd, false
                            )
                        } else {
                            exitDialogBinding.adContainer.visibility = View.GONE
                            exitDialogBinding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        exitDialogBinding.adContainer.visibility = View.GONE
                        exitDialogBinding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            exitDialogBinding.adLinearLayout.visibility = View.GONE

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
                    AdHandler.getInstance(this@MainScannerActivity).dismissProgress(this@MainScannerActivity)
                }
            }
        }
    }
    fun loadAdaptiveBannerAd() {

            if(AdHandler.getInstance(this).isNetworkAvailable(this)){

                AdHandler.getInstance(this)
                    .loadAdaptiveBanner(this, scannerBinding.bannerContainer,
                        Constant.bannerId, object :
                            AdEventListener {
                            override fun onAdLoaded(`object`: Any?) {}
                            override fun onAdClosed() {}
                            override fun onLoadError(errorCode: String?) {}
                        })
            }


    }
}
