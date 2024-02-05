package scan.reader.qrscanner.barcodescanner.ads

import android.app.Activity
import android.app.Application
import android.app.Application.ActivityLifecycleCallbacks
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import scan.reader.qrscanner.barcodescanner.activity.SplashAct
import scan.reader.qrscanner.barcodescanner.util.Constant
import scan.reader.qrscanner.barcodescanner.util.MySharedPrefrence


class AppOpenHandler(private val applicationClass: Application) :LifecycleEventObserver, ActivityLifecycleCallbacks {

    private lateinit var loadCallbacks: AppOpenAd.AppOpenAdLoadCallback
    private var adShowerActivity:Activity?=null
    var isLoading=false
    var appOpenAd:AppOpenAd?=null


    companion object{
        var isShowingAd=false


    }


    init {
        applicationClass.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun loadAppOpenAd(appOpenId: String?){

        if(!(MySharedPrefrence.newInstance(applicationClass).isPremiumPurchased)){
            if(AdHandler.getInstance(applicationClass).isNetworkAvailable(applicationClass)){
                if(!isLoading){
                    if(isAdAvailable){
                        showAdIfAvailable()
                        return
                    }
                }else{
                    return
                }
                isLoading=true
                loadCallbacks=object :AppOpenAd.AppOpenAdLoadCallback(){
                    override fun onAdFailedToLoad(loadError: LoadAdError) {
                        super.onAdFailedToLoad(loadError)
                        isLoading=false

                    }

                    override fun onAdLoaded(appOpenAd: AppOpenAd) {

                        super.onAdLoaded(appOpenAd)
                        this@AppOpenHandler.appOpenAd=appOpenAd
                        isLoading=false

                        if(adShowerActivity !is SplashAct) showAdIfAvailable()

                    }

                }

                isLoading=false
                try{
                    val request= AdRequest.Builder().build()
                    AppOpenAd.load(
                        applicationClass,appOpenId!!,request,loadCallbacks
                    )
                }catch (e:Exception){
                    e.printStackTrace()
                }

            }
        }

    }

    private val isAdAvailable:Boolean
        get() = appOpenAd!=null
    fun showAdIfAvailable(){
        if(!isShowingAd){
            if(isAdAvailable){
                val fullScreenContentCallback:FullScreenContentCallback=
                    object : FullScreenContentCallback(){
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            appOpenAd=null
                            isShowingAd = false
                        }

                        override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                            super.onAdFailedToShowFullScreenContent(p0)
                            isShowingAd = false
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            isShowingAd =true
                        }
                    }
                appOpenAd!!.fullScreenContentCallback= fullScreenContentCallback
                appOpenAd!!.show(adShowerActivity!!)
                isShowingAd =true

            }else{
                if(Build.VERSION.SDK_INT<= Build.VERSION_CODES.R){
                    loadAppOpenAd(Constant.appOpenId)
                }
            }
        }
    }



    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        adShowerActivity=activity
    }

    override fun onActivityResumed(activity: Activity) {
        adShowerActivity=activity
    }
    override fun onActivityStarted(activity: Activity) {
        adShowerActivity=activity
    }

    override fun onActivityPostResumed(activity: Activity) {
        super.onActivityPostResumed(activity)
        adShowerActivity=activity
    }

    override fun onActivityPaused(activity: Activity) {
        adShowerActivity=activity
    }
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }
    override fun onActivityStopped(activity: Activity) {

    }
    override fun onActivityDestroyed(activity: Activity) {

        adShowerActivity?.let {
            if(it.localClassName== activity.localClassName){
                adShowerActivity=null
            }
        }
    }

    override fun onActivityPreSaveInstanceState(activity: Activity, outState: Bundle) {
        super.onActivityPreSaveInstanceState(activity, outState)
    }
    override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onActivityPreCreated(activity, savedInstanceState)
    }
    override fun onActivityPreDestroyed(activity: Activity) {
        super.onActivityPreDestroyed(activity)
    }


    override fun onActivityPostDestroyed(activity: Activity) {
        super.onActivityPostDestroyed(activity)
    }
    override fun onActivityPostSaveInstanceState(activity: Activity, outState: Bundle) {
        super.onActivityPostSaveInstanceState(activity, outState)
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when(event){
            Lifecycle.Event.ON_START->{

                if (!MySharedPrefrence.newInstance(applicationClass).isPremiumPurchased) {
                    if(!Constant.isSplashScreen && !Constant.goingOutFromApp){
                        loadAppOpenAd(Constant.appOpenId)
                    }

                }
            }
            else-> {}
        }
    }



}

