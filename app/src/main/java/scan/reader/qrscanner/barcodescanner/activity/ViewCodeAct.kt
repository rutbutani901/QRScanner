package scan.reader.qrscanner.barcodescanner.activity


import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.text.format.DateFormat
import android.util.Log
import android.view.PixelCopy
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import androidx.print.PrintHelper
import scan.reader.qrscanner.barcodescanner.dataBase.DataModel
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.viewModel.BarcodeListViewModel
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.ads.OnAdClosedListener
import scan.reader.qrscanner.barcodescanner.databinding.ActivityViewCodeBinding
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant
import com.google.android.gms.ads.nativead.NativeAd
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ViewCodeAct : AppCompatActivity() {
    lateinit var barcodeListViewModel: BarcodeListViewModel

    val barcodeValueTypeHashMap: HashMap<Int, String> = hashMapOf(
        11 to "Calender Event",
        1 to "Contact",
        12 to "Driver License",
        2 to "Email",
        10 to "Geo",
        3 to "ISBN",
        4 to "Phone",
        5 to "Product",
        6 to "SMS",
        7 to "Text",
        0 to "Unknown",
        8 to "URL",
        9 to "WIFI"
    )
    val barcodeFormatMap: HashMap<Int, BarcodeFormat> = hashMapOf(
        32 to BarcodeFormat.EAN_13,
        512 to BarcodeFormat.UPC_A,
        2 to BarcodeFormat.CODE_39,
        4 to BarcodeFormat.CODE_93,
        256 to BarcodeFormat.QR_CODE,
        8 to BarcodeFormat.CODABAR,
        4096 to BarcodeFormat.AZTEC,//
        1 to BarcodeFormat.CODE_128,
        16 to BarcodeFormat.DATA_MATRIX,//
        64 to BarcodeFormat.EAN_8,
        128 to BarcodeFormat.ITF,
        2048 to BarcodeFormat.PDF_417,
        1024 to BarcodeFormat.UPC_E,
    )

    lateinit var viewCodeBinding: ActivityViewCodeBinding


    var barcodeValue: String = ""
    var barcodeFormat: Int = 0
    var barcodeType: Int = 0
    var note=""
    var comesFromBarcodeCreation=false

    var canPrint=true

    override fun onCreate(savedInstanceState: Bundle?) {


        setLocale(sharedPref.languageCode)
            //uncomment this only to shoe add
//          if(savedInstanceState==null){
//              if (intent.hasExtra("customGenerator")) {//generate qr
//                  Constant.interstitialId?.let {
//                      loadInterAd(1)
//                  }
//              }else{
//                  Constant.interstitialId?.let {
//                      loadInterAd(1)
//                  }
//              }
//          }










//        if (intent.hasExtra("customGenerator")) {//generate qr
//
//            if (savedInstanceState == null) {
//
//                Constant.interstitialId?.let {
//                    loadInterAd(2)
//                }
//            }
//        }else{
//            Constant.interstitialId?.let {
//                loadInterAd(1)
//            }
//
//        }


        super.onCreate(savedInstanceState)
        viewCodeBinding = ActivityViewCodeBinding.inflate(layoutInflater)
        setContentView(viewCodeBinding.root)



        Constant.nativeId?.let {
            loadNativeAd()
        }

        barcodeListViewModel = ViewModelProvider(this).get(BarcodeListViewModel::class.java)
        var installedAppName = ""


        viewCodeBinding.backButton.setOnClickListener{
            onBackPressed()
        }
        var comeFromOtherBarcodes = false

        if (intent.hasExtra("customGenerator"))// generate qr
        {
            sendFirebaseEvents(this,"ViewCodeActivityAfterCreatingManualCodes")
            sendAppMetricaEvents("ViewCodeActivityAfterCreatingManualCodes")

            comesFromBarcodeCreation=true
            barcodeValue = intent.getStringExtra("barcodeValue").toString()
            barcodeType = intent.getIntExtra("barcodeType", 0)
            barcodeFormat = intent.getIntExtra("barcodeFormat", 0)
            if (intent.hasExtra("installedAppName")) {

                installedAppName = intent.getStringExtra("installedAppName").toString()
            }
            if (intent.hasExtra("otherBarcodes")) {
                comeFromOtherBarcodes = intent.getBooleanExtra("otherBarcodes", false)
            }
            // barcodeFormat=256


//            val defaultBrowser = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_BROWSER)
//            defaultBrowser.setData(Uri.parse(barcodeValue))
//            startActivity(defaultBrowser)

//            viewCodeBinding.barcodeType.text=""
//            viewCodeBinding.barcodeFormat.text="Website"// give format in createsQr type
//            viewCodeBinding.barcodeValue.text=barcodeValue
        }
        else {// generate scanned imagges

            sendFirebaseEvents(this,"ViewCodeActivityFromScanResult")
            sendAppMetricaEvents("ViewCodeActivityFromScanResult")


            comesFromBarcodeCreation=false
            barcodeValue = intent.getStringExtra("barcodeValue").toString()
            barcodeFormat = intent.getIntExtra("barcodeFormat", 0)
            barcodeType = intent.getIntExtra("barcodeValueType", 0)
            note = intent.getStringExtra("barcodeNote").toString()


        }

        if(!TextUtils.isEmpty(note)){
            viewCodeBinding.barcodeNote.visibility=View.VISIBLE
            viewCodeBinding.barcodeNote.text="${getString(R.string.note)}:- \n"+note
        }else{
            viewCodeBinding.barcodeNote.visibility=View.GONE
        }

        var barcodeFormatText = ""
        var format = barcodeFormatMap.get(barcodeFormat)
        if (format == null) {
            barcodeFormatText = getString(R.string.barcodeFormatNotFound)
            //you can show image of not found in imageview
        } else {
            barcodeFormatText = format.toString()
            generateQrImage(barcodeValue, format, 1000)

        }

        var barcodeValueTypeText = ""
        var type = barcodeValueTypeHashMap.get(barcodeType)
        if (type == null) {
            barcodeValueTypeText = getString(R.string.barcodeTypeNotSupported)

        } else {
            barcodeValueTypeText = type
        }


        if (barcodeValueTypeText.equals("URL")) barcodeValueTypeText = getString(R.string.website)

        if (!installedAppName.equals("")) {
            viewCodeBinding.barcodeType.text = installedAppName
        } else {
            if (comeFromOtherBarcodes) {
                if (!format.toString().contains(getString(R.string.qr))) viewCodeBinding.barcodeType.text = getString(R.string.barCode)
                else viewCodeBinding.barcodeType.text = getString(R.string.qrcode)
            } else {

                viewCodeBinding.barcodeType.text = barcodeValueTypeText
            }
        }//barcodeValueTypeHashMap.get(barcodeType)
        viewCodeBinding.barcodeValue.text = barcodeValue
        viewCodeBinding.barcodeFormat.text =
            barcodeFormatText//barcodeFormatMap.get(barcodeFormat).toString()

        viewCodeBinding.sharePngButton.setOnClickListener{

            sendFirebaseEvents(this,"SharePngClickViewCodeActivity")
            sendAppMetricaEvents("SharePngClickViewCodeActivity")


            if(barcodeImageToShare!=null){
                    shareImageandText(barcodeImageToShare!!)
            }
        }

        viewCodeBinding.printButton.setOnClickListener{

            if(canPrint){

                sendFirebaseEvents(this,"PrintButtonToolbarClickViewCodeActivity")
                sendAppMetricaEvents("PrintButtonToolbarClickViewCodeActivity")


                viewCodeBinding.sharePngButton.visibility=View.GONE
                viewCodeBinding.scrollView.visibility=View.GONE
                viewCodeBinding.printPageFormat.visibility=View.VISIBLE
                viewCodeBinding.printButton.setImageResource(R.drawable.cross_icon)
                viewCodeBinding.printButton.setPadding(1,1,1,1)
                viewCodeBinding.printButton.imageTintList=ContextCompat.getColorStateList(this,R.color.white)

                makePdf()
            }else{

                viewCodeBinding.sharePngButton.visibility=View.VISIBLE
                viewCodeBinding.scrollView.visibility=View.VISIBLE
                viewCodeBinding.printPageFormat.visibility=View.GONE
                viewCodeBinding.printButton.setImageResource(R.drawable.print_icon)
                viewCodeBinding.printButton.setPadding(8,8,8,8)
            }
            canPrint=!canPrint

        }

    }
    fun loadNativeAd(){

        if(AdHandler.getInstance(this).isNetworkAvailable(this)){

            viewCodeBinding.shimmerLayout.root.visibility=View.VISIBLE
            viewCodeBinding.adContainer.visibility=View.GONE

            AdHandler.getInstance(this).loadNativeAd(
                this,
              Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if(adObject!= null){

                            viewCodeBinding.adContainer.visibility=View.VISIBLE
                            viewCodeBinding.shimmerLayout.root.visibility=View.GONE

                            AdHandler.getInstance(this@ViewCodeAct).showNativeAd(
                                this@ViewCodeAct,
                                viewCodeBinding.adContainer,
                                adObject as NativeAd,false
                            )
                        }else{
                            viewCodeBinding.adContainer.visibility=View.GONE
                            viewCodeBinding.shimmerLayout.root.visibility=View.VISIBLE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {
                        viewCodeBinding.adContainer.visibility=View.GONE
                        viewCodeBinding.shimmerLayout.root.visibility=View.GONE
                    }
                })

        }else{

            viewCodeBinding.adLinearLayout.visibility=View.GONE
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    fun makePdf(){


        viewCodeBinding.printImage.setImageBitmap(barcodeImageToShare)
        viewCodeBinding.printCodeType.text=viewCodeBinding.barcodeType.text

        viewCodeBinding.printLayoutButton.setOnClickListener{

            sendFirebaseEvents(this,"RealPrintButtonClickViewCodeActivity")
            sendAppMetricaEvents("RealPrintButtonClickViewCodeActivity")


            createBitmapFromLayout(viewCodeBinding.pageCardView){
                newBitmap->

                val photoPrinter = PrintHelper(this@ViewCodeAct)
                photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                photoPrinter.printBitmap("Print Bitmap", newBitmap)
            }
            viewCodeBinding.scrollView.visibility=View.VISIBLE
            viewCodeBinding.printPageFormat.visibility=View.GONE
            viewCodeBinding.sharePngButton.visibility=View.VISIBLE
            viewCodeBinding.printButton.setImageResource(R.drawable.print_icon)
            viewCodeBinding.printButton.setPadding(8,8,8,8)
            canPrint=!canPrint

        }

    }

    private  fun createBitmapFromLayout(view:View,callbacks: (bitmap:Bitmap)-> Unit){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            val location=IntArray(2)
            view.getLocationInWindow(location)
            PixelCopy.request(
                window,
                Rect(location[0],location[1],location[0]+view.width,location[1]+view.height),
                bitmap,{
                    if(it==PixelCopy.SUCCESS){
                        callbacks.invoke(bitmap)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }else{
            val bitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
            val canvas=Canvas(bitmap)
            val bgDrawable=view.background
            if(bgDrawable!=null){
                bgDrawable.draw(canvas)
            }else{
                canvas.drawColor(Color.WHITE)
            }
            view.draw(canvas)
            callbacks.invoke(bitmap)
        }

    }

    var barcodeImageToShare:Bitmap?=null

    private fun shareImageandText(bitmap: Bitmap) {
        val uri = getImageToShare(bitmap)
        val intent = Intent(Intent.ACTION_SEND)

        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        // adding text to share
        intent.putExtra(Intent.EXTRA_TEXT, barcodeValue)// add barcode value here

        // Add subject Here
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

        // setting type to image
        intent.type = "image/png"

        // calling startactivity() to share
        startActivity(Intent.createChooser(intent, "Share Via"))
    }

    private fun getImageToShare(bitmap: Bitmap): Uri? {
        val imagefolder = File(cacheDir, "images")
        var uri: Uri? = null
        try {
            imagefolder.mkdirs()
            val file = File(imagefolder, "shared_image.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = FileProvider.getUriForFile(this, "com.anni.shareimage.fileprovider", file)
        } catch (e: Exception) {
            Toast.makeText(this, "" + e.message, Toast.LENGTH_LONG).show()
        }
        return uri
    }


    fun generateQrImage(barcodevalue: String, format: BarcodeFormat, width: Int) {

        val multiFormatWriter = MultiFormatWriter();
        try {
            val hintMap = mapOf(
                EncodeHintType.MARGIN to 0
            )
            val bitMatrix = multiFormatWriter.encode(barcodevalue, format, width, width, hintMap)
            val barcodeEncoder = BarcodeEncoder();

            if (format.toString().contains("QR_CODE") || format.toString()
                    .contains("DATA_MATRIX") || format.toString().contains("AZTEC")
            ) {

                val bitmap = barcodeEncoder.createBitmap(bitMatrix)

                viewCodeBinding.qrImage.visibility = View.VISIBLE
                viewCodeBinding.barcodeImage.visibility = View.GONE
                viewCodeBinding.qrImage.setImageBitmap(bitmap)
                barcodeImageToShare=bitmap
            } else {

                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                viewCodeBinding.qrImage.visibility = View.GONE
                viewCodeBinding.barcodeImage.visibility = View.VISIBLE
                viewCodeBinding.barcodeImage.setImageBitmap(bitmap)
                barcodeImageToShare=bitmap
            }

            if(comesFromBarcodeCreation){

                saveBarcodeInDb()
            }

        } catch (e: Exception) {
            Log.d("erroror", "Could not generate QR image")

        }
    }

    fun saveBarcodeInDb() {
        val creationDateTime = curentDataTime()
        var dataModel = DataModel(
            barcodeValue,
            barcodeFormat,
            creationDateTime,
            "",
            false,
            barcodeType
        )

        //barcodeListViewModel.insert(this, dataModel)
        var calledOneTime=false
        if(!sharedPref.isDuplicateBarcodeEnable){
            barcodeListViewModel.doesBarcodesExist(this,barcodeValue).observe(this@ViewCodeAct, androidx.lifecycle.Observer {

                if(it==null){

                    if(!calledOneTime){
                        CoroutineScope(Dispatchers.IO).launch {
                           barcodeListViewModel.insert(this@ViewCodeAct,dataModel)

                        }
                        calledOneTime=true
                    }

                }
            })


        }else{
//               barcodeListViewModel.insert(this,dataModel)
            CoroutineScope(Dispatchers.IO).launch {
                barcodeListViewModel.insert(this@ViewCodeAct,dataModel)
            }

        }
    }

    fun curentDataTime(): String {
        var dateTime = ""
        val currentTime: Date = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())//MM GIVES 03
        val formattedDate: String = df.format(currentTime)// 18 marc/2023

        dateTime += formattedDate + "   "

        val delegate = "hh:mm aaa"
        var time = DateFormat.format(delegate, Calendar.getInstance().time)// 11:17 pm
        dateTime += time

        return dateTime
    }

    fun loadInterAd(adRandomNumber:Int){
        if(AdHandler.getInstance(this).isNetworkAvailable(this)){


            AdHandler.getInstance(this).loadInterstitialAdWithRandomNumber(this,
                Constant.interstitialId,adRandomNumber,object :
                    OnAdClosedListener {

                override fun onAdClosed() {
                    Log.d("addClosed","hii")

                }
            })
        }

    }
}




