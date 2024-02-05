package scan.reader.qrscanner.barcodescanner.util

import android.content.Context

class MySharedPrefrence(context: Context) {

    val pref= context.getSharedPreferences("MyShardPref",Context.MODE_PRIVATE)


    public  companion object{
        fun newInstance(context: Context)= MySharedPrefrence(context)
    }

    var cameraPermissionCounter:Int
        get()= pref.getInt("permissionCount",0)
        set(count)= pref.edit().putInt("permissionCount",count).apply()

    var contactPermissionCounter:Int
        get()= pref.getInt("permissionCount",0)
        set(count)= pref.edit().putInt("permissionCount",count).apply()

    var locationPermissionCounter:Int
        get()= pref.getInt("locationPermissionCounter",0)
        set(count)= pref.edit().putInt("locationPermissionCounter",count).apply()

    var readExternalStorageCounter:Int
        get()= pref.getInt("readExternalStorageCounter",0)
        set(count)= pref.edit().putInt("readExternalStorageCounter",count).apply()


    var isPremiumPurchased:Boolean
        get()= pref.getBoolean("isPremiumPurchased",false)
        set(isPremiumPurchased)= pref.edit().putBoolean("isPremiumPurchased",isPremiumPurchased).apply()

    var openWebsiteAutomatically:Boolean
        get()= pref.getBoolean("openWebsiteAutomatically",false)
        set(openWebsiteAutomatically)= pref.edit().putBoolean("openWebsiteAutomatically",openWebsiteAutomatically).apply()

    var isContinousScanningEnabled:Boolean
        get()= pref.getBoolean("isContinousScanningEnabled",false)
        set(isContinousScanningEnabled)= pref.edit().putBoolean("isContinousScanningEnabled",isContinousScanningEnabled).apply()

    var isDuplicateBarcodeEnable:Boolean
        get()= pref.getBoolean("isDuplicateBarcodeEnable",true)
        set(isDuplicateBarcodeEnable)= pref.edit().putBoolean("isDuplicateBarcodeEnable",isDuplicateBarcodeEnable).apply()


    var confirmScanManually:Boolean
        get()= pref.getBoolean("confirmScanManually",false)
        set(confirmScanManually)= pref.edit().putBoolean("confirmScanManually",confirmScanManually).apply()

    var playSound:Boolean
        get()= pref.getBoolean("playSound",false)
        set(playSound)= pref.edit().putBoolean("playSound",playSound).apply()

    var vibrateOnScan:Boolean
        get()= pref.getBoolean("vibrateOnScan",true)
        set(vibrateOnScan)= pref.edit().putBoolean("vibrateOnScan",vibrateOnScan).apply()

    var copyToClipboard:Boolean
        get()= pref.getBoolean("copyToClipboard",true)
        set(copyToClipboard)= pref.edit().putBoolean("copyToClipboard",copyToClipboard).apply()

    var cameraOrientationBack:Boolean
        get()= pref.getBoolean("cameraOrientationBack",true)
        set(cameraOrientationBack)= pref.edit().putBoolean("cameraOrientationBack",cameraOrientationBack).apply()

    var privacyPolicyAccepted:Boolean
        get()= pref.getBoolean("privacyPolicyAccepted",false)
        set(privacyPolicyAccepted)= pref.edit().putBoolean("privacyPolicyAccepted",privacyPolicyAccepted).apply()



    var languageCode: String
        get()= pref.getString("languageCode","").toString()
        set(languageCode)= pref.edit().putString("languageCode",languageCode).apply()

    var isAppRated: Boolean
        get()= pref.getBoolean("isAppRated",false)
        set(isAppRated)= pref.edit().putBoolean("isAppRated",isAppRated).apply()

    var isAppIntroShown: Boolean
        get()= pref.getBoolean("isAppIntroShown",false)
        set(isAppIntroShown)= pref.edit().putBoolean("isAppIntroShown",isAppIntroShown).apply()

    var appTheme: Int
        get()= pref.getInt("appTheme",0)
        set(appTheme)= pref.edit().putInt("appTheme",appTheme).apply()

    //0 default  1 light 2 dark
    var startTime:Long
        get() = pref.getLong("startTime",0L)
        set(startTime) = pref.edit().putLong("startTime",startTime).apply()

}