package scan.reader.qrscanner.barcodescanner.fragments

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import scan.reader.qrscanner.barcodescanner.BuildConfig
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.activity.*
import scan.reader.qrscanner.barcodescanner.util.MySharedPrefrence
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.willy.ratingbar.BaseRatingBar
import scan.reader.qrscanner.barcodescanner.databinding.*
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.util.Constant
import kotlin.math.roundToInt


class SettingsFrag : Fragment() {


    lateinit var binding:FragmentSettingsBinding
    lateinit var fragmentActivity: FragmentActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentActivity=context as FragmentActivity
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(activity!=null) fragmentActivity=activity as FragmentActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentSettingsBinding.inflate(layoutInflater)

        if(fragmentActivity.sharedPref.isPremiumPurchased){

            binding.premium.visibility=View.GONE
            binding.continousScanningPremiumIcon.visibility=View.GONE
            binding.duplicateBarcodesPremiumIcon.visibility=View.GONE
            binding.confirmScansManuallyPremiumIcon.visibility=View.GONE
        }else{

            binding.premium.visibility=View.VISIBLE
            binding.continousScanningPremiumIcon.visibility=View.VISIBLE
            binding.duplicateBarcodesPremiumIcon.visibility=View.VISIBLE
            binding.confirmScansManuallyPremiumIcon.visibility=View.VISIBLE
        }
        binding.introduction.setOnClickListener{

            fragmentActivity.sendFirebaseEvents(fragmentActivity,"AppIntroClickInSettingFragment")
            fragmentActivity.sendAppMetricaEvents("AppIntroClickInSettingFragment")

            var intent=Intent(fragmentActivity, AppIntroAct::class.java)
            intent.putExtra("cameFromSetting",true)
            startActivity(intent)
        }

        binding.appVersionMessage.text=BuildConfig.VERSION_NAME

        binding.theme.setOnClickListener{
                showThemeDialog()
        }

        binding.premium.setOnClickListener{

            fragmentActivity.sendFirebaseEvents(fragmentActivity,"PremiumClickedInSettingFragment")
            fragmentActivity.sendAppMetricaEvents("PremiumClickedInSettingFragment")

            startForResult.launch(Intent(fragmentActivity,
                PurchaseAct().javaClass))

        }
        binding.helpFeedback.setOnClickListener{

            fragmentActivity.sendFirebaseEvents(fragmentActivity,"HelpButtonClickSettingFrag")
            fragmentActivity.sendAppMetricaEvents("HelpButtonClickSettingFrag")

            startActivity(Intent(fragmentActivity, HelpAct::class.java))
        }

        val appTheme=fragmentActivity.sharedPref.appTheme
        when(appTheme){
            0->{
                //system theme
                binding.themeType.text=getString(R.string.systemDefault)
            }
            1->{
                //light
                binding.themeType.text=getString(R.string.light)

            }
            2->{//dark
                binding.themeType.text=getString(R.string.dark)

            }
        }
        var openAutomatically= MySharedPrefrence.newInstance(fragmentActivity).openWebsiteAutomatically
        binding.openWebsiteAutomaticallySwitch.isChecked= openAutomatically
        binding.openWebsiteAutomatically.setOnClickListener{

            var openAuto= MySharedPrefrence.newInstance(fragmentActivity).openWebsiteAutomatically
            binding.openWebsiteAutomaticallySwitch.isChecked=!openAuto
            MySharedPrefrence.newInstance(fragmentActivity).openWebsiteAutomatically=!openAuto

        }

        var continousScan= MySharedPrefrence.newInstance(fragmentActivity).isContinousScanningEnabled
        binding.continousScanningSwitch.isChecked= continousScan
        binding.continuousScanning.setOnClickListener{

            if(fragmentActivity.sharedPref.isPremiumPurchased){

                val continousScan=fragmentActivity.sharedPref.isContinousScanningEnabled

                binding.continousScanningSwitch.isChecked=!continousScan
                MySharedPrefrence.newInstance(fragmentActivity).isContinousScanningEnabled=!continousScan

            }else{

                binding.continousScanningSwitch.isChecked=false
                fragmentActivity.sharedPref.isContinousScanningEnabled=false
                startActivity(Intent(fragmentActivity,
                    PurchaseAct::class.java))

                //start premiums screen
            }
        }


        val duplicateBarcodes= MySharedPrefrence.newInstance(fragmentActivity).isDuplicateBarcodeEnable
        binding.duplicateBarcodesSwitch.isChecked= duplicateBarcodes
        binding.duplicateBarcodes.setOnClickListener{
            var isPremiumPurchased= MySharedPrefrence.newInstance(fragmentActivity).isPremiumPurchased

            if(isPremiumPurchased){

                val isduplicateBarocdeEnable= MySharedPrefrence.newInstance(fragmentActivity).isDuplicateBarcodeEnable

                binding.duplicateBarcodesSwitch.isChecked=!isduplicateBarocdeEnable
                MySharedPrefrence.newInstance(fragmentActivity).isDuplicateBarcodeEnable=!isduplicateBarocdeEnable

            }else{
                binding.duplicateBarcodesSwitch.isChecked=true
                MySharedPrefrence.newInstance(fragmentActivity).isDuplicateBarcodeEnable=true
                startActivity(Intent(fragmentActivity,
                    PurchaseAct::class.java))
                //start premiums screen
            }
        }


       var scanManually= MySharedPrefrence.newInstance(fragmentActivity).confirmScanManually
        binding.confirmScansManuallySwitch.isChecked=scanManually
        binding.confirmScansManually.setOnClickListener{

            var isPremiumPurchased= MySharedPrefrence.newInstance(fragmentActivity).isPremiumPurchased

            if(isPremiumPurchased){
                var isConfirmScanMauallyEnable= MySharedPrefrence.newInstance(fragmentActivity).confirmScanManually

                binding.confirmScansManuallySwitch.isChecked=!isConfirmScanMauallyEnable
                MySharedPrefrence.newInstance(fragmentActivity).confirmScanManually=!isConfirmScanMauallyEnable
            }else{
                binding.confirmScansManuallySwitch.isChecked=false
                MySharedPrefrence.newInstance(fragmentActivity).confirmScanManually=false
                startActivity(Intent(fragmentActivity,
                    PurchaseAct::class.java))

            }


        }


        var playSound= MySharedPrefrence.newInstance(fragmentActivity).playSound
        binding.playSoundSwitch.isChecked=playSound
        binding.playSound.setOnClickListener{
            var sound= MySharedPrefrence.newInstance(fragmentActivity).playSound

            binding.playSoundSwitch.isChecked=!sound
            MySharedPrefrence.newInstance(fragmentActivity).playSound=!sound
        }

        var vibrateOnScan= MySharedPrefrence.newInstance(fragmentActivity).vibrateOnScan
        binding.vibrateSwitch.isChecked=vibrateOnScan
        binding.vibrate.setOnClickListener{
            var vibrate= MySharedPrefrence.newInstance(fragmentActivity).vibrateOnScan

            binding.vibrateSwitch.isChecked=!vibrate
            MySharedPrefrence.newInstance(fragmentActivity).vibrateOnScan=!vibrate
        }

        var copyToClipboard= MySharedPrefrence.newInstance(fragmentActivity).copyToClipboard
        binding.copyToClipboardSwitch.isChecked=copyToClipboard
        binding.copyToClipboard.setOnClickListener{
            var copyClipBoard= MySharedPrefrence.newInstance(fragmentActivity).copyToClipboard

            binding.copyToClipboardSwitch.isChecked=!copyClipBoard
            MySharedPrefrence.newInstance(fragmentActivity).copyToClipboard=!copyClipBoard
        }


        val orientation= MySharedPrefrence.newInstance(fragmentActivity).cameraOrientationBack
        if(orientation) binding.cameraOrientationMessage.text=getString(R.string.backCameraRecomd)
        else             binding.cameraOrientationMessage.text=getString(R.string.frontCamera)
        binding.cameraOrientation.setOnClickListener{
            showCameraOrientationDialog()
        }

        binding.language.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"LanguageClickInSettingFragment")
            fragmentActivity.sendAppMetricaEvents("LanguageClickInSettingFragment")

            val intent=Intent(fragmentActivity, LanguageAct::class.java)
            intent.putExtra("cameFromSetting",true)
//            startActivity(intent)
            newStart.launch(intent)

        }

       // var cameraOrientation= MySharedPrefrence.newInstance(fragmentActivity).cameraOrientationBack


        binding.rate.setOnClickListener{
//            showRateDialog()
            showNewRateDialog()
        }
        binding.share.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"ShareClickInSettingFragment")
            fragmentActivity.sendAppMetricaEvents("ShareClickInSettingFragment")

            shareApp()
        }
        binding.feedBack.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"FeedBackClickInSettingFragment")
            fragmentActivity.sendAppMetricaEvents("FeedBackClickInSettingFragment")

            fragmentActivity.feedBack()
        }

        binding.privacyPolicy.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"PrivacyPolicyClickInSettingFragment")
            fragmentActivity.sendAppMetricaEvents("PrivacyPolicyClickInSettingFragment")

            startActivity(Intent(fragmentActivity, PrivacyPolicyAct::class.java))
        }


        return binding.root
    }

    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            Constant.nativeId=null
            Constant.interstitialId=null
            Constant.appOpenId=null

            fragmentActivity.sharedPref.isPremiumPurchased=true
            val intent=Intent(fragmentActivity, MainScannerActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("fromSplash",false)
            startActivity(intent)

        }
    }

    val newStart = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            fragmentActivity.finish()
        }
    }

    override fun onResume() {

        fragmentActivity.sendFirebaseEvents(fragmentActivity,"OnResumeSettingFragment")
        fragmentActivity.sendAppMetricaEvents("OnResumeSettingFragment")

        super.onResume()

    }





    lateinit var themeDialogBinding:DialogSelectThemeBinding
    lateinit var cameraOrientationBinding:DialogCameraOrientationBinding

    fun showThemeDialog(){

        fragmentActivity.sendFirebaseEvents(fragmentActivity,"ThemeDialogOpenInSettingFragment")
        fragmentActivity.sendAppMetricaEvents("ThemeDialogOpenInSettingFragment")


        themeDialogBinding= DialogSelectThemeBinding.inflate(layoutInflater)

        var appTheme=fragmentActivity.sharedPref.appTheme
        when(appTheme){
            0->{
                //system theme
                themeDialogBinding.systemThemeLogo.setImageResource(R.drawable.on_radio_button)
                themeDialogBinding.systemThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.mainBackGroundColorGreen)

                themeDialogBinding.lightThemeLogo.setImageResource(R.drawable.off_radio_button)
                themeDialogBinding.lightThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.unselectedThumbColor)

                themeDialogBinding.darkThemeLogo.setImageResource(R.drawable.off_radio_button)
                themeDialogBinding.darkThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.unselectedThumbColor)
            }
            1->{//light


                themeDialogBinding.systemThemeLogo.setImageResource(R.drawable.off_radio_button)
                themeDialogBinding.systemThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.unselectedThumbColor)

                themeDialogBinding.lightThemeLogo.setImageResource(R.drawable.on_radio_button)
                themeDialogBinding.lightThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.mainBackGroundColorGreen)

                themeDialogBinding.darkThemeLogo.setImageResource(R.drawable.off_radio_button)
                themeDialogBinding.darkThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.unselectedThumbColor)
            }
            2->{//dark

                themeDialogBinding.systemThemeLogo.setImageResource(R.drawable.off_radio_button)
                themeDialogBinding.systemThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.unselectedThumbColor)

                themeDialogBinding.lightThemeLogo.setImageResource(R.drawable.off_radio_button)
                themeDialogBinding.lightThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.unselectedThumbColor)

                themeDialogBinding.darkThemeLogo.setImageResource(R.drawable.on_radio_button)
                themeDialogBinding.darkThemeLogo.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.mainBackGroundColorGreen)

            }
        }


        var dialog= MaterialAlertDialogBuilder(fragmentActivity,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(themeDialogBinding.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {

                    //DIALOG CANCEL
                }

            })
            .show()


        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCanceledOnTouchOutside(false)
        themeDialogBinding.cancel.setOnClickListener{

            dialog.cancel()
        }
        themeDialogBinding.systemTheme.setOnClickListener{

            binding.themeType.text=getString(R.string.systemDefault)
          fragmentActivity.sharedPref.appTheme=0

            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.Q){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }

            themeDialogBinding.systemThemeLogo.setImageResource(R.drawable.on_radio_button)
            themeDialogBinding.lightThemeLogo.setImageResource(R.drawable.off_radio_button)
            themeDialogBinding.darkThemeLogo.setImageResource(R.drawable.off_radio_button)
            dialog.cancel()

           // fragmentActivity.recreate()
        }
        themeDialogBinding.lightTheme.setOnClickListener{

            binding.themeType.text=getString(R.string.light)
            fragmentActivity.sharedPref.appTheme=1

            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)


            themeDialogBinding.systemThemeLogo.setImageResource(R.drawable.off_radio_button)
            themeDialogBinding.lightThemeLogo.setImageResource(R.drawable.on_radio_button)
            themeDialogBinding.darkThemeLogo.setImageResource(R.drawable.off_radio_button)
            dialog.cancel()

           // fragmentActivity.recreate()
        }
        themeDialogBinding.darkTheme.setOnClickListener{

            binding.themeType.text=getString(R.string.dark)
            fragmentActivity.sharedPref.appTheme=2
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)


            themeDialogBinding.systemThemeLogo.setImageResource(R.drawable.on_radio_button)
            themeDialogBinding.lightThemeLogo.setImageResource(R.drawable.off_radio_button)
            themeDialogBinding.darkThemeLogo.setImageResource(R.drawable.on_radio_button)
            dialog.cancel()

           // fragmentActivity.recreate()
        }

    }

    fun showCameraOrientationDialog(){

        fragmentActivity.sendFirebaseEvents(fragmentActivity,"CameraOrientationDialogOpenInSettingFragment")
        fragmentActivity.sendAppMetricaEvents("CameraOrientationDialogOpenInSettingFragment")

        cameraOrientationBinding= DialogCameraOrientationBinding.inflate(layoutInflater)

        var orientation= MySharedPrefrence.newInstance(fragmentActivity).cameraOrientationBack

        if(orientation) {
            cameraOrientationBinding.frontCameraRadioButton.setImageResource(R.drawable.off_radio_button)
            cameraOrientationBinding.backCameraRadioButton.setImageResource(R.drawable.on_radio_button)
            cameraOrientationBinding.backCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.mainBackGroundColorGreen)
            cameraOrientationBinding.frontCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.offRadioGreyColor)

        }else{
            cameraOrientationBinding.frontCameraRadioButton.setImageResource(R.drawable.on_radio_button)
            cameraOrientationBinding.backCameraRadioButton.setImageResource(R.drawable.off_radio_button)
            cameraOrientationBinding.frontCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.mainBackGroundColorGreen)
            cameraOrientationBinding.backCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.offRadioGreyColor)

        }



        var dialog= MaterialAlertDialogBuilder(fragmentActivity,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(cameraOrientationBinding.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {

                    //DIALOG CANCEL
                }

            })
            .show()


        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCanceledOnTouchOutside(false)
        cameraOrientationBinding.cancel.setOnClickListener{

            dialog.cancel()
        }
        cameraOrientationBinding.backCamera.setOnClickListener{

            binding.cameraOrientationMessage.text=getString(R.string.backCameraRecomd)
            MySharedPrefrence.newInstance(fragmentActivity).cameraOrientationBack=true
            cameraOrientationBinding.backCameraRadioButton.setImageResource(R.drawable.on_radio_button)
            cameraOrientationBinding.frontCameraRadioButton.setImageResource(R.drawable.off_radio_button)
            cameraOrientationBinding.backCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.mainBackGroundColorGreen)
            cameraOrientationBinding.frontCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.offRadioGreyColor)

            dialog.cancel()
        }
        cameraOrientationBinding.frontCamera.setOnClickListener{

            binding.cameraOrientationMessage.text=getString(R.string.frontCamera)
            MySharedPrefrence.newInstance(fragmentActivity).cameraOrientationBack=false
            cameraOrientationBinding.backCameraRadioButton.setImageResource(R.drawable.off_radio_button)
            cameraOrientationBinding.frontCameraRadioButton.setImageResource(R.drawable.on_radio_button)
            cameraOrientationBinding.frontCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.mainBackGroundColorGreen)
            cameraOrientationBinding.backCameraRadioButton.imageTintList=ContextCompat.getColorStateList(fragmentActivity,R.color.offRadioGreyColor)

            dialog.cancel()
        }




    }

    lateinit var  newRateDialogBinding: DialogRateNewBinding
    fun showNewRateDialog() {

        fragmentActivity.sendFirebaseEvents(fragmentActivity,"RateDialogOpenInSettingFragment")
        fragmentActivity.sendAppMetricaEvents("RateDialogOpenInSettingFragment")


        newRateDialogBinding= DialogRateNewBinding.inflate(layoutInflater)
        val dialog= MaterialAlertDialogBuilder(fragmentActivity,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(newRateDialogBinding.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {

                    //DIALOG CANCEL
                }

            })
            .show()
        var totalRating=5
        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))

        newRateDialogBinding.ratingBar.setOnRatingChangeListener(object :
            BaseRatingBar.OnRatingChangeListener {

            override fun onRatingChange(
                ratingBar: BaseRatingBar?,
                rating: Float,
                fromUser: Boolean
            ) {
                totalRating= rating.roundToInt()
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

            fragmentActivity.sharedPref.isAppRated=true
            if(totalRating>2){

                fragmentActivity.rate()
            }else{

                fragmentActivity. feedBack()
            }
            dialog.cancel()
            //finishAffinity()
        }
        newRateDialogBinding.exitText.visibility=View.GONE
    }

    fun Context.rate(){
        val url="https://play.google.com/store/apps/details?id=${packageName}"
        val intent=Intent(Intent.ACTION_VIEW)
        intent.flags=Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data= Uri.parse(url)
        startActivity(intent)
    }
    fun Context.feedBack(){

        val intent=Intent(Intent.ACTION_SEND)
        var receiptant=arrayOf(resources.getString(R.string.feedBackEmail))
       intent.putExtra(Intent.EXTRA_EMAIL,receiptant)
       intent.putExtra(Intent.EXTRA_SUBJECT,"FeedBack")
       intent.putExtra(Intent.EXTRA_CC,resources.getString(R.string.feedBackEmail))
        intent.type="text/html"
        intent.setPackage("com.google.android.gm")
        startActivity(Intent.createChooser(intent,"Send Mail"))
    }

    fun shareApp(){
        try{
            var intent=Intent(Intent.ACTION_SEND)
            intent.type="text/plain"
            intent.putExtra(Intent.EXTRA_SUBJECT,getString(R.string.app_name))
            var shareMessage="\nLet me recommend you this App\n"
            shareMessage=
                """
                ${shareMessage+"https://play.google.com/store/apps/details?id="+BuildConfig.APPLICATION_ID}
                """.trimIndent()
            intent.putExtra(Intent.EXTRA_TEXT,shareMessage)
            startActivity(Intent.createChooser(intent,"Choose One"))
        }
        catch (e:Exception){
            e.printStackTrace()
        }
    }



}