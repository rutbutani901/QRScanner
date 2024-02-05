package scan.reader.qrscanner.barcodescanner.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromSmsBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref

class QrSmsAct : AppCompatActivity() {

    lateinit var smsBinding:ActivityQrFromSmsBinding

    override fun onCreate(savedInstanceState: Bundle?) {


        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        smsBinding= ActivityQrFromSmsBinding.inflate(layoutInflater)
        setContentView(smsBinding.root)

        sendFirebaseEvents(this,"SmsActivityInsideMoreQrCodes")
        sendAppMetricaEvents("SmsActivityInsideMoreQrCodes")

        smsBinding.backButton.setOnClickListener{
            onBackPressed()
        }
        smsBinding.tickButton.setOnClickListener{

            sendFirebaseEvents(this,"TickClickSmsActivityInsideMoreQrCodes")
            sendAppMetricaEvents("TickClickSmsActivityInsideMoreQrCodes")

            var phone=smsBinding.phoneNumber.text.toString().trim()

            if(phone != "")
            {
                // create qr
                var message=smsBinding.message.text.toString().trim()

                var barcodeValue="sms:${phone}?body=${message}"

                var intent= Intent(this@QrSmsAct, ViewCodeAct::class.java)
                intent.putExtra("customGenerator",1)
                intent.putExtra("barcodeValue",barcodeValue)
                intent.putExtra("barcodeType",6)// 6 is for sms type
                intent.putExtra("barcodeFormat",256)// 7 is for text type
                startActivity(intent)
            }else
            {
                if(!inputErrorIconShown){

                    inputErrorIconShown=true
                    smsBinding.phoneNoInputIcon.visibility=View.VISIBLE
                    noInputPopUp(smsBinding.phoneNumber)
                }
            }
        }

        smsBinding.phoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(smsBinding.phoneNumber.text.toString().trim()!=""){

                    inputErrorIconShown=false
                    popUpWindow?.dismiss()
                    smsBinding.phoneNoInputIcon.visibility=View.GONE
                }else{

                    if(!inputErrorIconShown){

                        inputErrorIconShown=true
                        smsBinding.phoneNoInputIcon.visibility=View.VISIBLE
                        noInputPopUp(smsBinding.phoneNumber)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
    }


    var popUpWindow: PopupWindow?=null

    var inputErrorIconShown=false

    fun noInputPopUp(refView: View){


        var inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        popUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        popUpWindow?.showAsDropDown(refView,0,0, Gravity.END)
        popUpWindow?.isFocusable=false
    }


    override fun onBackPressed() {
        super.onBackPressed()
    }
}