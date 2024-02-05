package scan.reader.qrscanner.barcodescanner.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityAppIntroBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.fragments.FirstIntroFrag
import scan.reader.qrscanner.barcodescanner.fragments.SecondIntroFrag
import scan.reader.qrscanner.barcodescanner.fragments.ThirdIntroFrag

import scan.reader.qrscanner.barcodescanner.viewPager.AppIntroViewPager

import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant

class AppIntroAct : AppCompatActivity(), ThirdIntroFrag.FinishInterface {

lateinit var binding: ActivityAppIntroBinding


var cameFromSetting=false
    override fun onCreate(savedInstanceState: Bundle?) {

        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        binding= ActivityAppIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        Constant.nativeId?.let {
//            loadNativeAd()
//        }

        cameFromSetting=intent.getBooleanExtra("cameFromSetting",false)
        if(cameFromSetting){
//            Constant.nativeId?.let {
//                loadNewNativeAd()
//            }
            sendFirebaseEvents(this,"AppIntroActivityFromSetting")
            sendAppMetricaEvents("AppIntroActivityFromSetting")

        }else{
//            Constant.nativeId?.let {
//                loadNativeAd()
//            }

            sendFirebaseEvents(this,"AppIntroActivityFromSplash")
            sendAppMetricaEvents("AppIntroActivityFromSplash")

        }
        createFragments()

        binding.gotoNextScreen.setOnClickListener{

            sendFirebaseEvents(this,"NextButtonClickAppIntroActivity")
            sendAppMetricaEvents("NextButtonClickAppIntroActivity")

           var currentPosition= binding.viewPager.currentItem
            if(currentPosition==2) {

                if(cameFromSetting){
                    onBackPressed()
                }else{
                    if(sharedPref.languageCode.isEmpty()){
                        startActivity(Intent(this, LanguageAct::class.java))
                        finish()
                    }else{
                        startActivity(Intent(this, MainScannerActivity::class.java))
                        finish()
                    }
                }
            }
            else  binding.viewPager.currentItem=++currentPosition

        }
        binding.goToMainScreen.setOnClickListener{

            sendFirebaseEvents(this,"SkipButtonClickAppIntroActivity")
            sendAppMetricaEvents("SkipButtonClickAppIntroActivity")

            if(cameFromSetting){
                onBackPressed()
            }else{
                if(sharedPref.languageCode.isEmpty()){
                    startActivity(Intent(this, LanguageAct::class.java))
                    finish()
                }
            }
        }
    }


    lateinit var appIntroViewPager: AppIntroViewPager
    var prevSelectedTab=-1


    var firstIntro= FirstIntroFrag()
    var secondIntro= SecondIntroFrag()
    var thirdIntro= ThirdIntroFrag()

    fun createFragments() {

        var introFragList= arrayListOf(
            firstIntro,
            secondIntro,
            thirdIntro
        )
        appIntroViewPager= AppIntroViewPager(this,introFragList)
        binding.viewPager.adapter= appIntroViewPager
        binding.viewPager.offscreenPageLimit= appIntroViewPager.itemCount

        binding.viewPager.currentItem=0
        binding.viewPager.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback(){

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    when(position)
                    {
                        0->{
                            binding.goToMainScreen.visibility=View.VISIBLE
                            binding.goToMainScreen.isClickable=true
                            binding.goToMainScreen.setImageResource(R.drawable.goto_main_screen_icon)
                            binding.goToMainScreen.background=ContextCompat.getDrawable(this@AppIntroAct,R.drawable.shape_circle)
                            binding.goToMainScreen.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct, R.color.introScrenButtonBackgroundColor)

                            binding.firstCircle.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct,
                            R.color.white)
                            binding.gotoNextScreen.setImageResource(R.drawable.goto_next_screen_icon)
                        }
                        1->{
                            binding.goToMainScreen.visibility=View.VISIBLE
                            binding.goToMainScreen.isClickable=true
                            binding.goToMainScreen.setImageResource(R.drawable.goto_main_screen_icon)
                            binding.goToMainScreen.background=ContextCompat.getDrawable(this@AppIntroAct,R.drawable.shape_circle)
                            binding.goToMainScreen.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct, R.color.introScrenButtonBackgroundColor)


                            binding.secondCircle.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct,
                                R.color.white)
                            binding.gotoNextScreen.setImageResource(R.drawable.goto_next_screen_icon)
                        }
                        2->{
                           // binding.goToMainScreen.visibility=View.GONE

                            binding.goToMainScreen.setImageDrawable(null)
                            binding.goToMainScreen.isClickable=false
                            binding.goToMainScreen.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct,R.color.mainBackGroundColorGreen)

                            binding.thirdCircle.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct, R.color.white)
                            binding.gotoNextScreen.setImageResource(R.drawable.tick_icon)

                        }

                    }
                    if(prevSelectedTab!=-1) {

                        when(prevSelectedTab) {

                            0->{
                                binding.firstCircle.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct,
                                    R.color.introScrenButtonBackgroundColor)
                            }
                            1->{
                                binding.secondCircle.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct,
                                    R.color.introScrenButtonBackgroundColor)
                            }
                            2->{
                                binding.thirdCircle.backgroundTintList=ContextCompat.getColorStateList(this@AppIntroAct,
                                    R.color.introScrenButtonBackgroundColor)
                            }
                        }
                    }
                    prevSelectedTab=position
                }
            })

    }

    override fun canFinishAct(canFinish: Boolean) {
        finish()
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
                    AdHandler.getInstance(this).nativeAd, false
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

                            AdHandler.getInstance(this@AppIntroAct).showNativeAd(
                                this@AppIntroAct,
                                binding.adContainer,
                                adObject as NativeAd, false
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


}