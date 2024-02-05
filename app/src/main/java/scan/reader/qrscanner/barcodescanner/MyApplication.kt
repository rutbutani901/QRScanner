package scan.reader.qrscanner.barcodescanner

import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.yandex.metrica.YandexMetrica
import com.yandex.metrica.YandexMetricaConfig
import scan.reader.qrscanner.barcodescanner.ads.AppOpenHandler
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref

class   MyApplication: Application() {

    private var appOpenHandler: AppOpenHandler?=null

    override fun onCreate() {
        super.onCreate()
//        appOpenHandler = AppOpenHandler(this)

        AppLovinSdk.getInstance( this ).setMediationProvider( "max" )
        AppLovinSdk.getInstance( this ).initializeSdk({ configuration: AppLovinSdkConfiguration ->
            // AppLovin SDK is initialized, start loading ads

        })

        val yandexConfig= YandexMetricaConfig.newConfigBuilder(
            resources.getString(R.string.app_metrica_id)).build()
        YandexMetrica.activate(applicationContext,yandexConfig)
        YandexMetrica.enableActivityAutoTracking(this)


        var appTheme=sharedPref.appTheme
        when(appTheme){
            0->{//dafult
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                }
            }
            1->{//light
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            2->{//dark
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }

        }
    }

}