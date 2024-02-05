package scan.reader.qrscanner.barcodescanner.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.modelClass.OtherBarcodeTypeModelClass

import scan.reader.qrscanner.barcodescanner.databinding.ActivityOtherBarcodesInputBinding
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.util.Constant

class OtherBarcodesInputAct : AppCompatActivity() {

    lateinit var otherBacrcodesBinding:ActivityOtherBarcodesInputBinding

    var isValidInput=false


    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)

        otherBacrcodesBinding= ActivityOtherBarcodesInputBinding.inflate(layoutInflater)
        setContentView(otherBacrcodesBinding.root)

//        Constant.nativeId?.let {
//            loadNewNativeAd()
//        }

        sendFirebaseEvents(this,"InputActFromBarcodeAndOther2DCodes")
        sendAppMetricaEvents("InputActFromBarcodeAndOther2DCodes")

        val barcode=intent.getSerializableExtra("barcodeData") as OtherBarcodeTypeModelClass
        if(barcode.barcodeFormat==256 ||
            barcode.barcodeFormat==16 ||
            barcode.barcodeFormat==2048 ||
            barcode.barcodeFormat==4096 ||
            barcode.barcodeFormat==1 ||
            barcode.barcodeFormat==4 ||
            barcode.barcodeFormat==2 ){

            //otherBacrcodesBinding.inputText.inputType=InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE
        }
        else{
            otherBacrcodesBinding.inputText.inputType=InputType.TYPE_CLASS_NUMBER
        }

        otherBacrcodesBinding.backButton.setOnClickListener{
            onBackPressed()
        }

        otherBacrcodesBinding.inputText.hint=barcode.description
        otherBacrcodesBinding.title.text=barcode.barcodeName
        otherBacrcodesBinding.tickButton.setOnClickListener{

            sendFirebaseEvents(this,"TickClickInputActFromBarcodeAndOther2DCodes")
            sendAppMetricaEvents("TickClickInputActFromBarcodeAndOther2DCodes")


            val inputText=otherBacrcodesBinding.inputText.text.toString().trim()
            val multiFormatWriter= MultiFormatWriter();
            val formatValue=barcode.barcodeFormat
            when(formatValue){

                256->{
                    isValidInput=true
                }
                16 ->{
                    try{

                        multiFormatWriter.encode(inputText, BarcodeFormat.DATA_MATRIX, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }


                }
                2048->{
                    try{

                        multiFormatWriter.encode(inputText, BarcodeFormat.PDF_417, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                4096->{
                    try{

                        multiFormatWriter.encode(inputText, BarcodeFormat.AZTEC, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                32->{
                    try{


                        multiFormatWriter.encode(inputText, BarcodeFormat.EAN_13, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }

                }
                64->{
                    try{
                        multiFormatWriter.encode(inputText, BarcodeFormat.EAN_8, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                1024->{
                    try{

                        multiFormatWriter.encode(inputText, BarcodeFormat.UPC_E, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                512->{
                    try{


                        multiFormatWriter.encode(inputText, BarcodeFormat.UPC_A, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                1->{
                    try{

                        multiFormatWriter.encode(inputText, BarcodeFormat.CODE_128, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                4->{
                    try{


                        multiFormatWriter.encode(inputText, BarcodeFormat.CODE_93, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                2->{
                    try{

                        multiFormatWriter.encode(inputText, BarcodeFormat.CODE_39, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                8->{
                    try{

                        multiFormatWriter.encode(inputText, BarcodeFormat.CODABAR, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }
                }
                128->{

                    try{


                        multiFormatWriter.encode(inputText, BarcodeFormat.ITF, 1, 1)
                        isValidInput=true

                    }catch (e:java.lang.Exception){
                        isValidInput=false
                    }}

            }


            if(inputText != ""){

                if(isValidInput)
                {
                    val intent= Intent(this, ViewCodeAct::class.java)
                    intent.putExtra("customGenerator",1)
                    intent.putExtra("barcodeValue",inputText)
                    if(formatValue==32 || formatValue==64|| formatValue==1024||formatValue==512){
                        intent.putExtra("barcodeType",5)// 7 means product type
                    }else{
                        intent.putExtra("barcodeType",7)// 7 means text type
                    }
                    intent.putExtra("otherBarcodes",true)// 7 means text type
                    intent.putExtra("barcodeFormat",barcode.barcodeFormat)//256


                    startActivity(intent)
                }
                else {

                    if(!inputErrorIconShown){

                        inputErrorIconShown=true
                        otherBacrcodesBinding.textNoInputIcon.visibility=View.VISIBLE
                        noInputPopUp(otherBacrcodesBinding.textNoInputIcon,true)
                    }
                }

            }else{

                if(!inputErrorIconShown){

                    inputErrorIconShown=true
                    otherBacrcodesBinding.textNoInputIcon.visibility=View.VISIBLE
                    noInputPopUp(otherBacrcodesBinding.textNoInputIcon,false)
                }
            }

        }

        otherBacrcodesBinding.inputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(otherBacrcodesBinding.inputText.text.toString().trim()!=""){

                    inputErrorIconShown=false
                    popUpWindow?.dismiss()
                    otherBacrcodesBinding.textNoInputIcon.visibility=View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

    }

    var popUpWindow: PopupWindow?=null

    var inputErrorIconShown=false



    fun noInputPopUp(refView:View, isInValidInput:Boolean){

        val inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.item_no_input_popup,null)
        popUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)

        if(isInValidInput) view.findViewById<TextView>(R.id.title).text=getString(R.string.invalidBarcodeInputType)
        else               view.findViewById<TextView>(R.id.title).text=getString(R.string.noInputFoundText)
        popUpWindow?.showAsDropDown(refView,0,10, Gravity.END)
        popUpWindow?.isFocusable=false
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }


    fun loadNewNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            otherBacrcodesBinding.adContainer.visibility = View.GONE
            otherBacrcodesBinding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            otherBacrcodesBinding.adContainer.visibility = View.VISIBLE
                            otherBacrcodesBinding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(this@OtherBarcodesInputAct).showNativeAd(
                                this@OtherBarcodesInputAct,
                                otherBacrcodesBinding.adContainer,
                                adObject as NativeAd, true
                            )
                        } else {
                            otherBacrcodesBinding.adContainer.visibility = View.GONE
                            otherBacrcodesBinding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        otherBacrcodesBinding.adContainer.visibility = View.GONE
                        otherBacrcodesBinding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            otherBacrcodesBinding.adLinearLayout.visibility = View.GONE

        }

    }

}