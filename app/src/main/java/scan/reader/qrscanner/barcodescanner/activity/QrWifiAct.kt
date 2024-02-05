package scan.reader.qrscanner.barcodescanner.activity


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromWifiBinding
import scan.reader.qrscanner.barcodescanner.databinding.DialogWifiPasswordNotCorrectBinding
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.util.Constant


class QrWifiAct : AppCompatActivity() {



    lateinit var wifiBinding:ActivityQrFromWifiBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        wifiBinding= ActivityQrFromWifiBinding.inflate(layoutInflater)
        setContentView(wifiBinding.root)

        sendFirebaseEvents(this,"CreateWifiActivity")
        sendAppMetricaEvents("CreateWifiActivity")


//        Constant.nativeId?.let {
//            loadNewNativeAd()
//        }

        createDropdown()

        wifiBinding.backButton.setOnClickListener{
            onBackPressed()
        }

        wifiBinding.tickButton.setOnClickListener{
            sendFirebaseEvents(this,"TickClickCreateWifiActivity")
            sendAppMetricaEvents("TickClickCreateWifiActivity")


            var selected= wifiBinding.spinner2.selectedItem.toString()


            var password=wifiBinding.password.text.toString()
            var name=wifiBinding.networkName.text.toString()


            if(name == "")
            {
                if(!inputErrorIconShown){

                    wifiBinding.inputErrorIcon.visibility= View.VISIBLE
                    inputErrorIconShown=true
                    nameNoInputPopup(wifiBinding.networkName)
                }
            }
            else
            {
                if(selected == getString(R.string.wpa))
                {
                    if(password=="") {

                        if(!passwordInputErrorIconShown){

                            passwordInputErrorIconShown=true
                            wifiBinding.passwordInputErrorIcon.visibility= View.VISIBLE
                            passwordNoInputPopUp(wifiBinding.password)
                        }

                    }
                    else if(password.length<8) {

                        encryptionNotCorrectDialog()
                    }
                    else{

                        passwordInputErrorIconShown=false

                        name = name.replace(":", "\\:").replace(";", "\\;")
                         password = password.replace(":", "\\:").replace(";", "\\;")

                        val wifiConfigStr = "WIFI:S:${name};T:WPA;P:${password};;"

                        val intent= Intent(this, ViewCodeAct::class.java)
                        intent.putExtra("customGenerator",1)
                        intent.putExtra("barcodeValue",wifiConfigStr)
                        intent.putExtra("barcodeType",9)// 9 is for wifi type
                        intent.putExtra("barcodeFormat",256)// 7 is for text type
                        startActivity(intent)
                    }
                }
                else{
                    var encrypType=""
                    if(selected == getString(R.string.wep)){
                        encrypType="WEP"

                        if(password==""){

                            if(!passwordInputErrorIconShown){

                                passwordInputErrorIconShown=true
                                wifiBinding.passwordInputErrorIcon.visibility= View.VISIBLE
                                passwordNoInputPopUp(wifiBinding.password)
                            }

                        }else{

                            startViewCodeActivity(name,encrypType,password)
                        }

                    }
                    else {
                        encrypType="nopass"
                        password=""

                        startViewCodeActivity(name,encrypType,password)
                    }

                }
            }

        }


        wifiBinding.networkName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(wifiBinding.networkName.text.toString().trim()!=""){

                    inputErrorIconShown=false
                    namePopUpWindow?.dismiss()
                    wifiBinding.inputErrorIcon.visibility= View.GONE
                }
//                else{
//
//                    if(!inputErrorIconShown){
//
//                        wifiBinding.inputErrorIcon.visibility= View.VISIBLE
//                        inputErrorIconShown=true
//                        showEnteredWebsiteNoInputPopUp(wifiBinding.networkName)
//                    }
//                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        wifiBinding.password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(wifiBinding.password.text.toString().trim()!=""){

                    passwordInputErrorIconShown=false
                    passwordPopUpWindow?.dismiss()
                    wifiBinding.passwordInputErrorIcon.visibility= View.GONE
                }
//                else{
//
//                    if(!inputErrorIconShown){
//
//                        passwordInputErrorIconShown=true
//                        wifiBinding.passwordInputErrorIcon.visibility= View.VISIBLE
//                        showEnteredWebsiteNoInputPopUp(wifiBinding.password)
//                    }
//                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }


    fun startViewCodeActivity(name:String, encrypType:String, password:String){

        var newName = name.replace(":", "\\:").replace(";", "\\;")
        var newPassword = password.replace(":", "\\:").replace(";", "\\;")


        val wifiConfigStr = "WIFI:S:${newName};T:${encrypType};P:${newPassword};;"

        var intent= Intent(this, ViewCodeAct::class.java)
        intent.putExtra("customGenerator",1)
        intent.putExtra("barcodeValue",wifiConfigStr)
        intent.putExtra("barcodeType",9)// 9 is for wifi type
        intent.putExtra("barcodeFormat",256)// 7 is for text type
        startActivity(intent)
    }




    lateinit var dialogWifiPasswordNotCorrectBinding: DialogWifiPasswordNotCorrectBinding

    fun encryptionNotCorrectDialog() {
        dialogWifiPasswordNotCorrectBinding = DialogWifiPasswordNotCorrectBinding.inflate(layoutInflater)

        var dialog = MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(dialogWifiPasswordNotCorrectBinding.root)
            .setOnCancelListener { }
            .show()
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)

        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))

        dialogWifiPasswordNotCorrectBinding.ok.setOnClickListener {

            dialog.cancel()
        }
    }




    fun createDropdown()
    {
        val spinner = wifiBinding.spinner2 as Spinner
        val encryptionType = arrayOf(
            getString(R.string.wpa),
            getString(R.string.wep),
            getString(R.string.noneWifi)
        )
        val langAdapter = ArrayAdapter<CharSequence>(this, R.layout.spinner_text, encryptionType)
        langAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        wifiBinding.spinner2.setAdapter(langAdapter)


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()

                if(selectedItem == getString(R.string.noneWifi))
                {
                    wifiBinding.password.visibility= View.GONE

                    passwordInputErrorIconShown=false
                    wifiBinding.passwordInputErrorIcon.visibility= View.GONE
                    passwordPopUpWindow?.dismiss()

                }else
                {
                    wifiBinding.password.visibility= View.VISIBLE
                }

            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

    }

    var namePopUpWindow: PopupWindow?=null
    var passwordPopUpWindow: PopupWindow?=null

    var inputErrorIconShown=false
    var passwordInputErrorIconShown=false

    fun nameNoInputPopup(refView:View){


        val inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        namePopUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        namePopUpWindow?.showAsDropDown(refView,0,0, Gravity.END)
        namePopUpWindow?.isFocusable=false
    }

    fun passwordNoInputPopUp(refView:View){


        val inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        passwordPopUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        passwordPopUpWindow?.showAsDropDown(refView,0,0, Gravity.END)
        passwordPopUpWindow?.isFocusable=false
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }



    fun loadNewNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            wifiBinding.adContainer.visibility = View.GONE
            wifiBinding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            wifiBinding.adContainer.visibility = View.VISIBLE
                            wifiBinding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(this@QrWifiAct).showNativeAd(
                                this@QrWifiAct,
                                wifiBinding.adContainer,
                                adObject as NativeAd, true
                            )
                        } else {
                            wifiBinding.adContainer.visibility = View.GONE
                            wifiBinding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        wifiBinding.adContainer.visibility = View.GONE
                        wifiBinding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            wifiBinding.adLinearLayout.visibility = View.GONE

        }

    }


}