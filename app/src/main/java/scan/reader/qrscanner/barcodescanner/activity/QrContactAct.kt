package scan.reader.qrscanner.barcodescanner.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromContactBinding
import scan.reader.qrscanner.barcodescanner.databinding.DialogCameraPermissionSettingBinding
import scan.reader.qrscanner.barcodescanner.databinding.DialogContactPermissionRequestBinding
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ezvcard.Ezvcard
import ezvcard.VCard
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.util.Constant
import java.io.BufferedReader
import java.io.InputStreamReader


class QrContactAct : AppCompatActivity() {

    lateinit var qrFromContactBinding:ActivityQrFromContactBinding
    var RECORD_REQUEST_CODE=100

    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        qrFromContactBinding= ActivityQrFromContactBinding.inflate(layoutInflater)
        setContentView(qrFromContactBinding.root)
//        if(savedInstanceState==null){
//            Constant.interstitialId?.let {
//                loadInterAd()
//            }
//        }

        sendFirebaseEvents(this,"CreateContactAct")
        sendAppMetricaEvents("CreateContactAct")


        if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)== PackageManager.PERMISSION_GRANTED) {
            sharedPref.contactPermissionCounter = 0
        }

        if(intent.type=="text/x-vcard"){

            try{
                val uri=intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                uri?.let {

                    val contentResolver=applicationContext.contentResolver
                    contentResolver.openInputStream(uri)?.use {
                        val bufferedReader=BufferedReader(InputStreamReader(it))
                        val stringBuilder= StringBuilder()
                        var line:String?
                        while (bufferedReader.readLine().also { line=it }!=null){
                            stringBuilder.append(line).append('\n')
                        }
                        val fileContents=stringBuilder.toString()
                        val ezvCard=Ezvcard.parse(fileContents).first()

                        getContactDataFromContactUri(ezvCard)
                    }
                    finish()

                }
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
        if(intent.type=="text/plain"){

            try{
                val value=intent.getStringExtra(Intent.EXTRA_TEXT)

                value?.let {
                    val inputText= value.toString().toString()

                    val intent= Intent(this@QrContactAct, ViewCodeAct::class.java)

                    intent.putExtra("customGenerator",1)
                    intent.putExtra("barcodeValue",inputText)
                    intent.putExtra("barcodeType",7)// 7 is for text type
                    intent.putExtra("barcodeFormat",256)// 7 is for text type
                    startActivity(intent)

                    finish()
                }
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

        qrFromContactBinding.backButton.setOnClickListener{
            onBackPressed()
        }

        qrFromContactBinding.gotoShareFromOther.setOnClickListener{
            startActivity(Intent(this, ShareFromOtherAppAct::class.java))
        }


        qrFromContactBinding.fetchContactButton.setOnClickListener{

            sendFirebaseEvents(this,"FetchContactClickCreateContactAct")
            sendAppMetricaEvents("FetchContactClickCreateContactAct")

            checkContactPermission()

        }
        qrFromContactBinding.tickButton.setOnClickListener{

            sendFirebaseEvents(this,"TickClickCreateContactAct")
            sendAppMetricaEvents("TickClickCreateContactAct")

            //if first namr not found and last not found then show error in first name
            //if first namr found and last not found no prob
            //if first namr not found and last  found no prob
            var firstName=qrFromContactBinding.firstName.text.toString().trim()
            firstName = firstName.replace(";", "\\;")

            var lastName=qrFromContactBinding.lastName.text.toString().trim()
            lastName = lastName.replace(";", "\\;")

            var company=qrFromContactBinding.company.text.toString().trim()
            company = company.replace(";", "\\;")

            var jobTitle=qrFromContactBinding.jobTitle.text.toString().trim()
            jobTitle = jobTitle.replace(";", "\\;")

            var phoneNumber=qrFromContactBinding.phoneNumber.text.toString().trim()
            phoneNumber = phoneNumber.replace(";", "\\;")

            var email=qrFromContactBinding.email.text.toString().trim()
            email = email.replace(";", "\\;")

            var streetAddress=qrFromContactBinding.streetAddress.text.toString().trim()
            streetAddress = streetAddress.replace(";", "\\;")

            var zipCode=qrFromContactBinding.zipCode.text.toString().trim()
            zipCode = zipCode.replace(";", "\\;")

            var city=qrFromContactBinding.city.text.toString().trim()
            city = city.replace(";", "\\;")

            var region=qrFromContactBinding.region.text.toString().trim()
            region = region.replace(";", "\\;")

            var country=qrFromContactBinding.country.text.toString().trim()
            country = country.replace(";", "\\;")

            var website=qrFromContactBinding.website.text.toString().trim()
            website = website.replace(";", "\\;")


            if(firstName=="")// || lastName==""
            {
                if(!inputErrorIconShown){

                   qrFromContactBinding.inputErrorIcon.visibility=View.VISIBLE
                   showEnteredWebsiteNoInputPopUp(qrFromContactBinding.firstName)
               }
//
            }
           else{// good to go

               var vCardString="BEGIN:VCARD\n" +
                                "VERSION:2.1\n"

                vCardString+= "N:${lastName};${firstName}\n"
//                if(firstName!="") firstName+=" "
                vCardString+= "FN:${firstName} ${lastName}\n"

                if(company!="")   vCardString+= "ORG:${company}\n"
                if(jobTitle!="")   vCardString+= "TITLE:${jobTitle}\n"
                if(phoneNumber!="")   vCardString+= "TEL:${phoneNumber}\n"
                if(email!="")   vCardString+= "EMAIL:${email}\n"

                val addressString=";;${streetAddress};${city};${region};${zipCode};${country}"
                if(addressString!=";;;;;") vCardString+= "ADR:${addressString}\n"
                if(website!="")   vCardString+= "URL:${website}\n"

                vCardString+="END:VCARD"

                val intent= Intent(this, ViewCodeAct::class.java)
                intent.putExtra("customGenerator",1)
                intent.putExtra("barcodeValue",vCardString)
                intent.putExtra("barcodeType",1)//1 is for contact type bqr code
                intent.putExtra("barcodeFormat",256)// 7 is for text type
                startActivity(intent)

            }
        }

        qrFromContactBinding.firstName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(qrFromContactBinding.firstName.text.toString().trim()!=""){

                    inputErrorIconShown=false
                    popUpWindow?.dismiss()
                    qrFromContactBinding.inputErrorIcon.visibility= View.GONE
                }else{

                    if(!inputErrorIconShown){

                        qrFromContactBinding.inputErrorIcon.visibility= View.VISIBLE
                        showEnteredWebsiteNoInputPopUp(qrFromContactBinding.firstName)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })




    }

    fun loadInterAd(){

        if(AdHandler.getInstance(this).isNetworkAvailable(this)){

            if(AdHandler.getInstance(this).interstitialAd!=null){
                AdHandler.getInstance(this).loadInterstitialAdWithRandomNumber(this,
                    Constant.interstitialId,1
                ) { Log.d("adclosed", "yes") }
            }else{
                AdHandler.getInstance(this).showProgress(this)
                AdHandler.getInstance(this).loadSplashInter(this, Constant.interstitialId
                ) {
                    AdHandler.getInstance(this@QrContactAct).dismissProgress(this@QrContactAct)
                }
            }
        }
    }

    var popUpWindow: PopupWindow?=null

    var inputErrorIconShown=false

    fun showEnteredWebsiteNoInputPopUp(refView:View){

        inputErrorIconShown=true
        val inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        popUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        popUpWindow?.showAsDropDown(refView,0,0, Gravity.START)
        popUpWindow?.isFocusable=false
    }

     var resultLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        if(result.resultCode==Activity.RESULT_OK){
            val data:Intent?=result.data
            data?.let {
                val uri= data.data
                uri?.let {
                    val cursor=contentResolver.query(
                        uri,
                        arrayOf(ContactsContract.Contacts.LOOKUP_KEY),
                        null,null,null
                    )
                    if(cursor!=null && cursor.moveToFirst()) {
                        val lookUpKey = cursor.getString(0)
                        val contactUri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_VCARD_URI, lookUpKey
                        )

                        val vCardInput = contentResolver.openInputStream(contactUri)
                        val ezvCard=Ezvcard.parse(vCardInput).first()

                        getContactDataFromContactUri(ezvCard)
                    }
                }
            }
        }
    }

    fun getContactDataFromContactUri(ezvCard:VCard){

            var vCardString="BEGIN:VCARD\n" +
                    "VERSION:2.1\n"

            var firstName=""
            var lastName=""
            ezvCard.structuredName?.let {
                if(ezvCard.structuredName.given!=null){
                    firstName=ezvCard.structuredName.given.trim()

                }
                if(ezvCard.structuredName.family!=null){
                    lastName=ezvCard.structuredName.family.trim()
                }
                vCardString+= "N:${lastName};${firstName}\n"
                vCardString+= "FN:${firstName} ${lastName}\n"
            }

            ezvCard.organization?.let {

                ezvCard.organization.values[0]?.let {
                    val organisation=ezvCard.organization.values[0].trim()
                    if(organisation!="")   vCardString+= "ORG:${organisation}\n"
                }

            }
            if(ezvCard.titles.size!=0){
                for (item in ezvCard.titles){
                    val title=item.value.trim()
                    vCardString+= "TITLE:${title}\n"
                }
            }


            ezvCard.telephoneNumbers?.let {
                var numbers=ezvCard.telephoneNumbers

                for (item in numbers){
                    var number= item.text.trim()
                    vCardString+= "TEL:${number}\n"

                }
            }

            ezvCard.emails?.let {
                var emails=ezvCard.emails
                for (item in emails){
                    val emailText= item.value.trim()
                    vCardString+= "EMAIL:${emailText}\n"
                }
            }

            var addressList=ArrayList<String>()
            ezvCard.addresses?.let {
                var address=ezvCard.addresses

                for (item in address){
                    var fullAddress=""

                    var streetAddress=""
                    var city=""
                    var region=""
                    var zipCode=""
                    var country=""

                    item.streetAddress?.let {
                        streetAddress=item.streetAddress
                    }
                    item.locality?.let {
                        city=item.locality
                    }
                    item.region?.let {
                        region=item.region
                    }
                    item.postalCode?.let {
                        zipCode=item.postalCode
                    }
                    item.country?.let {
                        country=item.country
                    }


                    var addressString=";;${streetAddress};${city};${region};${zipCode};${country}"
                    if(addressString!=";;;;;") vCardString+= "ADR:${addressString}\n"

                }
            }

            if(ezvCard.urls.size!=0){
                for(item in ezvCard.urls){
                    var website= item.value
                    vCardString+= "URL:${website}\n"
                }

            }

            vCardString+="END:VCARD"

            var intent= Intent(this, ViewCodeAct::class.java)
            intent.putExtra("customGenerator",1)
            intent.putExtra("barcodeValue",vCardString)
            intent.putExtra("barcodeType",1)//1 is for contact type bqr code
            intent.putExtra("barcodeFormat",256)// 7 is for text type
            startActivity(intent)



    }

    fun checkContactPermission()
    {
            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)== PackageManager.PERMISSION_GRANTED)
            {
                fetchContact()
            }
            else
            {
                showPermissionDialog()
            }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode==RECORD_REQUEST_CODE)
        {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    sharedPref.cameraPermissionCounter=0
                    fetchContact()

                }

        }

    }

    lateinit var contactPermissionDialog: DialogContactPermissionRequestBinding


    fun showPermissionDialog(){

        contactPermissionDialog= DialogContactPermissionRequestBinding.inflate(layoutInflater)

        var dialog= MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(contactPermissionDialog.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {

                }

            })
            .show()

        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        contactPermissionDialog.cancel.setOnClickListener{
            dialog.cancel()
        }
        contactPermissionDialog.ok.setOnClickListener{//setting refers hers ok button


            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_CONTACTS)){

                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS),RECORD_REQUEST_CODE)
                sharedPref.contactPermissionCounter=1
            }else{

                if(sharedPref.contactPermissionCounter==0){
                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS),RECORD_REQUEST_CODE)
                }else
                {
                    showPermissionSettingDialog()

                }
            }

            dialog.cancel()

        }


    }


    lateinit var contactPermissionSettingDialog:DialogCameraPermissionSettingBinding
    fun showPermissionSettingDialog()
    {
        contactPermissionSettingDialog= DialogCameraPermissionSettingBinding.inflate(layoutInflater)

        var dialog=MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(contactPermissionSettingDialog.root)
            .setOnCancelListener(object :DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {


                }

            })
            .show()


        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))

        contactPermissionSettingDialog.cancel.setOnClickListener{
            dialog.cancel()
        }
        contactPermissionSettingDialog.settings.setOnClickListener{
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package",this.packageName, null)
            intent.data = uri
            permissionSettingLauncher.launch(intent)
        }


    }

    var permissionSettingLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result->
        if(result.resultCode==Activity.RESULT_OK){

            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)== PackageManager.PERMISSION_GRANTED ){
                sharedPref.cameraPermissionCounter=0


            }
        }
        if(result.resultCode==Activity.RESULT_CANCELED){

            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)== PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_CONTACTS)== PackageManager.PERMISSION_GRANTED ){
                sharedPref.cameraPermissionCounter=0


            }
        }
    }


    fun fetchContact(){
        val intent = Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI)
        resultLauncher.launch(intent)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }




}