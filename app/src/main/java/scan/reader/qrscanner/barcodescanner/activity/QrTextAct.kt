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
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromTextBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref

class QrTextAct : AppCompatActivity() {

    lateinit var textBinding:ActivityQrFromTextBinding

    override fun onCreate(savedInstanceState: Bundle?) {


        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        textBinding= ActivityQrFromTextBinding.inflate(layoutInflater)
        setContentView(textBinding.root)

        sendFirebaseEvents(this,"TextActivityInsideMoreQrCodes")
        sendAppMetricaEvents("TextActivityInsideMoreQrCodes")


        textBinding.backButton.setOnClickListener{
            onBackPressed()
        }

        textBinding.tickButton.setOnClickListener{
            sendFirebaseEvents(this,"TickClickTextActivityInsideMoreQrCodes")
            sendAppMetricaEvents("TickClickTextActivityInsideMoreQrCodes")

            val inputText=textBinding.inputText.text.toString().trim()
            if(inputText != "")
            {
                var intent= Intent(this@QrTextAct, ViewCodeAct::class.java)

                intent.putExtra("customGenerator",1)
                intent.putExtra("barcodeValue",inputText)
                intent.putExtra("barcodeType",7)// 7 is for text type
                intent.putExtra("barcodeFormat",256)// 7 is for text type
                startActivity(intent)
            }else{

                if(!inputErrorIconShown){

                    inputErrorIconShown=true
                    textBinding.textNoInputIcon.visibility=View.VISIBLE
                    noInputPopUp(textBinding.textNoInputIcon)
                }
            }
        }


        textBinding.inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(textBinding.inputText.text.toString().trim()!=""){

                    inputErrorIconShown=false
                    popUpWindow?.dismiss()
                    textBinding.textNoInputIcon.visibility=View.GONE
                }else{

                    if(!inputErrorIconShown){

                        inputErrorIconShown=true
                        textBinding.textNoInputIcon.visibility=View.VISIBLE
                        noInputPopUp(textBinding.textNoInputIcon)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


    }

    var popUpWindow: PopupWindow?=null

    var inputErrorIconShown=false

    fun noInputPopUp(refView:View){


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