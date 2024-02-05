package scan.reader.qrscanner.barcodescanner.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromWebsiteBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant


class QrWebsiteAct : AppCompatActivity() {

    lateinit var websiteBinding:ActivityQrFromWebsiteBinding

    override fun onCreate(savedInstanceState: Bundle?) {


        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        websiteBinding= ActivityQrFromWebsiteBinding.inflate(layoutInflater)
        setContentView(websiteBinding.root)

//        Constant.nativeId?.let {
//            loadNewNativeAd()
//        }

        sendFirebaseEvents(this,"CreateWebsiteAct")
        sendAppMetricaEvents("CreateWebsiteAct")


        websiteBinding.backButton.setOnClickListener{
            onBackPressed()
        }

       websiteBinding.tickButton.setOnClickListener{

           sendFirebaseEvents(this,"TickButtonClickCreateWebsiteAct")
           sendAppMetricaEvents("TickButtonClickCreateWebsiteAct")


           var website= websiteBinding.enteredWebsite.text.toString().trim()

           if(website != "")
           {
               websiteBinding.enteredWebsiteNoInputIcon.visibility=View.GONE

               if(!website.contains("https://")){
                   website ="https://"+website
               }else{
                   val substring = "https://"
                   website = website.replace(Regex("(\\Q$substring\\E.*)$substring"), "$substring")
               }

                var intent= Intent(this@QrWebsiteAct, ViewCodeAct::class.java)
                intent.putExtra("customGenerator",1)
               intent.putExtra("barcodeValue", website)
               intent.putExtra("barcodeType", 8)//8 is for url, barcodeType
               intent.putExtra("barcodeFormat",256)// 7 is for text type
               startActivity(intent)
           }
//           else{
//
//               if(!inputErrorIconShown){
//
//                   websiteBinding.enteredWebsiteNoInputIcon.visibility=View.VISIBLE
//                   showEnteredWebsiteNoInputPopUp(websiteBinding.enteredWebsite)
//               }
//           }
       }


        websiteBinding.enteredWebsite.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(websiteBinding.enteredWebsite.text.toString().trim()!=""){

                    inputErrorIconShown=false
                    popUpWindow?.dismiss()
                    websiteBinding.enteredWebsiteNoInputIcon.visibility=View.GONE
                }else{

                    if(!inputErrorIconShown){

                        websiteBinding.enteredWebsiteNoInputIcon.visibility=View.VISIBLE
                        showEnteredWebsiteNoInputPopUp(websiteBinding.enteredWebsite)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        websiteBinding.gotoShareFromOther.setOnClickListener{
            startActivity(Intent(this@QrWebsiteAct, ShareFromOtherAppAct::class.java))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }




    fun loadNewNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            websiteBinding.adContainer.visibility = View.GONE
            websiteBinding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            websiteBinding.adContainer.visibility = View.VISIBLE
                            websiteBinding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(this@QrWebsiteAct).showNativeAd(
                                this@QrWebsiteAct,
                                websiteBinding.adContainer,
                                adObject as NativeAd, true
                            )
                        } else {
                            websiteBinding.adContainer.visibility = View.GONE
                            websiteBinding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        websiteBinding.adContainer.visibility = View.GONE
                        websiteBinding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            websiteBinding.adLinearLayout.visibility = View.GONE

        }

    }





     var popUpWindow:PopupWindow?=null

    var inputErrorIconShown=false

    fun showEnteredWebsiteNoInputPopUp(refView:View){

        inputErrorIconShown=true
        var inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        popUpWindow= PopupWindow(view,RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        popUpWindow?.showAsDropDown(refView,0,0,Gravity.END)
        popUpWindow?.isFocusable=false
    }

}