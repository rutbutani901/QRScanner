package scan.reader.qrscanner.barcodescanner.extensions

import android.app.Activity
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.yandex.metrica.YandexMetrica
import scan.reader.qrscanner.barcodescanner.util.MySharedPrefrence
import java.util.*


fun Activity.setLocale(lanCode:String) {

    val locale=Locale(lanCode)
    Locale.setDefault(locale)
    val config=this.resources.configuration
    config.setLocale(locale)
    resources.updateConfiguration(config,this.resources.displayMetrics)

}


val Context.sharedPref: MySharedPrefrence get() = MySharedPrefrence.newInstance(applicationContext)
val Context.wifiManager: WifiManager?
    get() = applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager


fun Context.sendFirebaseEvents(context: Context,screenName:String){
   val firebaseAnalytics:FirebaseAnalytics= FirebaseAnalytics.getInstance(context)
    val params = Bundle()
    params.putString("ScreenName", screenName)
//    firebaseAnalytics.logEvent("ScreenViewChecker", params)
    firebaseAnalytics.logEvent(screenName, params)
}

fun Context.sendAppMetricaEvents(screenName:String){
//    val eventParameters = "{\"name\":\"Alice\", \"age\":\"18\"}"
    val eventParameters = "{\"screenName\":\"screenName\"}"
    YandexMetrica.reportEvent(screenName, eventParameters)
}


