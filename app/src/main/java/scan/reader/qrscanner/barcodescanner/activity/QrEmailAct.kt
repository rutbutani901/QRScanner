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
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromEmailBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref

class QrEmailAct : AppCompatActivity() {
    lateinit var emailBinding:ActivityQrFromEmailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        emailBinding= ActivityQrFromEmailBinding.inflate(layoutInflater)
        setContentView(emailBinding.root)


        sendFirebaseEvents(this,"EmailActivityInsideMoreQrCodes")
        sendAppMetricaEvents("EmailActivityInsideMoreQrCodes")


        emailBinding.backButton.setOnClickListener{
            onBackPressed()
        }

        emailBinding.tickButton.setOnClickListener{

            sendFirebaseEvents(this,"TickClickEmailActivityInsideMoreQrCodes")
            sendAppMetricaEvents("TickClickEmailActivityInsideMoreQrCodes")

            var emailAddress=emailBinding.emailAddress.text.toString().trim()
            var subject=emailBinding.emailSubject.text.toString().trim()
            var message=emailBinding.emailMessage.text.toString().trim()


            emailAddress=emailAddress.replace(";", "\\;")
            subject =subject.replace(";", "\\;")
            message =message.replace(";", "\\;")

            if(emailAddress != ""){

                var mailString=""
                if(subject != "" || message != ""){

                    emailAddress.replace(";", "\\;")
                    mailString="MATMSG:TO:${emailAddress};SUB:${subject};BODY:${message};;"


                }else{
                    //subject and message fiels is empty

                    emailAddress.replace(";", "\\;")
                    mailString="MAILTO;${emailAddress}"

                }

                var intent= Intent(this@QrEmailAct, ViewCodeAct::class.java)
                intent.putExtra("customGenerator",1)
                intent.putExtra("barcodeValue",mailString)
                intent.putExtra("barcodeType",2)// 2 is for text type
                intent.putExtra("barcodeFormat",256)// 7 is for text type
                startActivity(intent)

            }else{

                if(!inputErrorIconShown){

                    inputErrorIconShown=true
                    emailBinding.emailNoInputIcon.visibility=View.VISIBLE
                    noInputPopUp(emailBinding.emailAddress)
                }
            }
        }


        emailBinding.emailAddress.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(emailBinding.emailAddress.text.toString().trim()!=""){

                    inputErrorIconShown=false
                    popUpWindow?.dismiss()
                    emailBinding.emailNoInputIcon.visibility=View.GONE
                }else{

                    if(!inputErrorIconShown){

                        inputErrorIconShown=true
                        emailBinding.emailNoInputIcon.visibility=View.VISIBLE
                        noInputPopUp(emailBinding.emailAddress)
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