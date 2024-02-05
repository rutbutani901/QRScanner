package scan.reader.qrscanner.barcodescanner.activity

import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Website
import android.text.TextUtils
import android.text.format.DateFormat
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import biweekly.Biweekly
import biweekly.ICalendar
import biweekly.component.VEvent
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.analytics.FirebaseAnalytics
import ezvcard.Ezvcard
import ezvcard.VCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.adapter.BarcodeFunctionalityAdapter
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.ads.OnAdClosedListener
import scan.reader.qrscanner.barcodescanner.dataBase.DataModel
import scan.reader.qrscanner.barcodescanner.databinding.ActivityScanResultBinding
import scan.reader.qrscanner.barcodescanner.databinding.DialogAddNotesBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.modelClass.CustomCreatedViewForScanResult
import scan.reader.qrscanner.barcodescanner.util.Constant
import scan.reader.qrscanner.barcodescanner.util.WifiConnector
import scan.reader.qrscanner.barcodescanner.viewModel.BarcodeListViewModel
import java.net.URLDecoder
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class ScanResultAct : AppCompatActivity() {

    lateinit var scanResultBinding:ActivityScanResultBinding
    lateinit var barcodeListViewModel: BarcodeListViewModel
    var isFav=false
    var note:String=""
    var favNotesDataUpdated=true
    var barcode: DataModel?=null
    val barcodeFormatHashMap: HashMap<Int, String> = hashMapOf(
        32 to "EAN_13",
        512 to "UPC_A",
        2 to "CODE_39",
        4 to "CODE_93",
        256 to "QR_CODE",
        8 to "CODABAR",
        4096 to "AZTEC",
        1 to "CODE_128",
        16 to "DATA_MATRIX",
        64 to "EAN_8",
        128 to "ITF",
        2048 to "PDF_417",
        1024 to "UPC_E"
    )
    var barcodeInsertedId=-1L

    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        scanResultBinding= ActivityScanResultBinding.inflate(layoutInflater)
        setContentView(scanResultBinding.root)




//        Constant.nativeId?.let {
//            loadBigNativeAd()
//        }
        barcodeListViewModel = ViewModelProvider(this).get(BarcodeListViewModel::class.java)
        barcodeInsertedId=-1
        var barcodeValue:String=""
        var barcodeFormat:Int=0
        var barcodeValueType:Int=0
        var creationDateTime:String=""
        if(intent.hasExtra("barcodeData"))//comes from history
        {
            sendFirebaseEvents(this,"ScanResultActivityFromHistory")
            sendAppMetricaEvents("ScanResultActivityFromHistory")


            if (savedInstanceState == null) {
//                Constant.interstitialId?.let {
//                    loadInterAd(4)
//                }
            }

            barcode= intent.getSerializableExtra("barcodeData") as DataModel

            barcode?.let {
                barcodeValue=barcode!!.barValue
                barcodeValueType=barcode!!.barcodeType
                barcodeFormat=barcode!!.barFormat
                creationDateTime=barcode!!.creationTime
                isFav=barcode!!.isFav
                note=barcode!!.note

                barcodeInsertedId=barcode!!.id.toLong()
            }
        }
        else{// comes from scan

//            if (savedInstanceState == null) {
//                Constant.interstitialId?.let {
//                    loadInterAd(1)
//                }
//            }
            sendFirebaseEvents(this,"ScanResultActivityFromScanCode")
            sendAppMetricaEvents("ScanResultActivityFromScanCode")


            barcodeValue=intent.getStringExtra("barcodeValue").toString()
            barcodeFormat=intent.getIntExtra("barcodeFormat",0)
            barcodeValueType=intent.getIntExtra("barcodeValueType",0)
            creationDateTime= curentDataTime()

            val dataModel= DataModel(
                barcodeValue,
                barcodeFormat,
                creationDateTime,
                note,
                isFav,
                barcodeValueType)

            barcode=dataModel

            var calledOneTime=false
            if(!sharedPref.isDuplicateBarcodeEnable){

                barcodeListViewModel.doesBarcodesExist(this,barcodeValue).observe(this, Observer {

                    if(it==null){
                        if(!calledOneTime){
                            CoroutineScope(Dispatchers.IO).launch {
                                barcodeInsertedId=barcodeListViewModel.insert(this@ScanResultAct,dataModel)
                            }
                            calledOneTime=true
                        }
                    }
                })
            }else{
                CoroutineScope(Dispatchers.IO).launch {
                    barcodeInsertedId=barcodeListViewModel.insert(this@ScanResultAct,dataModel)
                }
            }
        }
        //setting intial fav and note
        barcode?.let {
            if(isFav) {
                scanResultBinding.fav.setImageResource(R.drawable.selected_fav_icon)
            }else {
                scanResultBinding.fav.setImageResource(R.drawable.fav_icon)
            }
            if(note != "") {
                scanResultBinding.notesText.visibility = View.VISIBLE
                scanResultBinding.notesText.text = note
            }
        }

        var formatText= barcodeFormatHashMap[barcodeFormat]
        if(formatText==null) {
                formatText=getString(R.string.barcodeFormatNotFound)
        }
        scanResultBinding.barcodeFormat.text=formatText
        scanResultBinding.date.text= creationDateTime

        scanResultBinding.viewCodeLayout.setOnClickListener{
            val intent= Intent(this, ViewCodeAct::class.java)
            intent.putExtra("barcodeValue",barcodeValue)
            intent.putExtra("barcodeFormat",barcodeFormat)
            intent.putExtra("barcodeValueType",barcodeValueType)
            intent.putExtra("barcodeNote",note)
            startActivity(intent)
        }
        scanResultBinding.deleteBarcode.setOnClickListener{
            sendFirebaseEvents(this,"DeleteClickScanResult")
            sendAppMetricaEvents("DeleteClickScanResult")

            barcode?.let {
                barcodeListViewModel.delete(this@ScanResultAct,barcodeInsertedId.toInt())//barcode!!.id
            }
            finish()
        }

        scanResultBinding.shareAsText.setOnClickListener{
            sendFirebaseEvents(this,"ShareAsTextClickSanResult")
            sendAppMetricaEvents("ShareAsTextClickSanResult")

            shareBarcodeValueAsText(barcodeValue)
        }
        scanResultBinding.funtionDotsIcon.setOnClickListener{
            showMoreFeaturesDialog(scanResultBinding.funtionDotsIcon)
        }

        scanResultBinding.backButton.setOnClickListener{
            onBackPressed()
        }
        scanResultBinding.fav.setOnClickListener{
            performFavTask()
        }
        scanResultBinding.notesIcon.setOnClickListener{
            showNotesDialog()
        }
        scanResultBinding.copyCode.setOnClickListener{
           copyToClipBoard()
        }

        if(barcodeValueType==8)//isfrom website
        {
            scanResultBinding.title.text=getString(R.string.website)
            val websiteUrl=barcodeValue
            val openAutomatically=sharedPref.openWebsiteAutomatically
            if(openAutomatically)  openWebsite(websiteUrl)
            val funList=ArrayList<CustomCreatedViewForScanResult>()
            funList.add(CustomCreatedViewForScanResult(getString(R.string.openWebsite),R.drawable.open_website_icon,websiteUrl))
            settingAdapter(funList,barcodeValue)
            scanResultBinding.value.text=barcodeValue
        }
        else if(barcodeValueType==1)// is for contact
        {
            scanResultBinding.title.text=getString(R.string.contact)

            val funcList=generateRedableContact(barcodeValue)
            if(funcList.isNotEmpty()){
                scanResultBinding.value.text=contactDetailsInHumanReadable.trim()
                settingAdapter(funcList,barcodeValue)
            }else{
                scanResultBinding.value.text=getString(R.string.contactDetailsNotFound)
            }
        }
        else if(barcodeValueType==9)// for wifi
        {
            scanResultBinding.title.text=getString(R.string.wifi)
            val finalString=retrieveWifiData(barcodeValue)
            scanResultBinding.value.text=finalString
            if(finalString.isNotEmpty()){
                val wifiFuncList=ArrayList<CustomCreatedViewForScanResult>()
                wifiFuncList.add(CustomCreatedViewForScanResult(getString(R.string.connectToWifi),R.drawable.wifi_icon))
                wifiFuncList.add(CustomCreatedViewForScanResult(getString(R.string.copyPassword),R.drawable.copy_clipboard_icon_in_white))
                wifiFuncList.add(CustomCreatedViewForScanResult(getString(R.string.copyNetWorkNmae),R.drawable.copy_clipboard_icon_in_white))

                settingAdapter(wifiFuncList,barcodeValue)
            }
        }
        else if(barcodeValueType==11)// event
        {
            scanResultBinding.title.text=getString(R.string.event)

            val funcList=ArrayList<CustomCreatedViewForScanResult>()
            funcList.add(CustomCreatedViewForScanResult(getString(R.string.addToCalender),R.drawable.event_icon))
            settingAdapter(funcList,barcodeValue)

           val outPutstring= extractCalender(barcodeValue)
            if(outPutstring == "") {
                scanResultBinding.value.text=barcodeValue.trim()
            }
            else {
                scanResultBinding.value.text=outPutstring.trim()
            }

        }
        else if(barcodeValueType==7)// for text type
        {
            scanResultBinding.title.text=getString(R.string.qrcode)
            barcode?.let {
                val format=barcode!!.barFormat
                if(format==16 || format==2048 || format==4096 || format==1  || format==4 || format==2 || format==8
                    || format==128 ){
                    scanResultBinding.title.text=getString(R.string.barCode)
                }
            }
            textToSearchOnWeb=barcodeValue
            scanResultBinding.value.text=barcodeValue
            val funcList=ArrayList<CustomCreatedViewForScanResult>()
            funcList.add(CustomCreatedViewForScanResult(getString(R.string.searchOnWeb),R.drawable.search_icon))
            settingAdapter(funcList,barcodeValue)
        }
        else if(barcodeValueType==2)// for EMAIL type
        {
            scanResultBinding.title.text=getString(R.string.email)
            getDataFromEmailTypeFormat(barcodeValue)

            var finalOutput=""

                finalOutput="${getString(R.string.email)}:- "+emailAddress+ "\n"+
                        "${getString(R.string.subject)}:- "+emailSubject+"\n"+
                        "${getString(R.string.body)}:- "+emailMessage

                scanResultBinding.value.text=finalOutput.trim()

            if(emailAddress!=""){
                val funcList=ArrayList<CustomCreatedViewForScanResult>()
                funcList.add(CustomCreatedViewForScanResult(getString(R.string.sendEmail),R.drawable.email_icon))
                settingAdapter(funcList,barcodeValue)
            }

        }
        else if(barcodeValueType==6)// for sms type
        {
            scanResultBinding.title.text=getString(R.string.sms)

            getValueFromSms(barcodeValue)

            scanResultBinding.value.text="${getString(R.string.phone)}:- ${smsPhone}\n${getString(R.string.message)}:- ${smsMessage}"

            if(smsPhone!=""){
                val funcList=ArrayList<CustomCreatedViewForScanResult>()
                funcList.add(CustomCreatedViewForScanResult(getString(R.string.sendSms),R.drawable.send_sms_icon))
                funcList.add(CustomCreatedViewForScanResult(getString(R.string.addContact),R.drawable.add_contact_icon_with_person))

                settingAdapter(funcList,barcodeValue)
            }
        }
        else if(barcodeValueType==10)// for geo type
        {
            try{
                scanResultBinding.title.text=getString(R.string.location)

                val latitudeEndIndex = barcodeValue.indexOf(",")
                val latitude = barcodeValue.substring(4, latitudeEndIndex)
                val longitude = barcodeValue.substringAfter(",")

                val finalString = "Latitude:- ${latitude}\nLongitude:- ${longitude}"
                scanResultBinding.value.text = finalString
                val geoLocation = "${latitude},${longitude}"
                val funcList = ArrayList<CustomCreatedViewForScanResult>()
                funcList.add(
                    CustomCreatedViewForScanResult(
                        getString(R.string.showLocation),
                        R.drawable.current_location_icon,
                        geoLocation
                    )
                )
                settingAdapter(funcList, barcodeValue)
            }catch (e:Exception){
                e.printStackTrace()
            }

        }
        else if(barcodeValueType==5)//product
        {
            scanResultBinding.title.text=getString(R.string.barCode)
            barcode?.let {
                val format=barcode!!.barFormat

                if(format==32 || format==64){
                    scanResultBinding.title.text=getString(R.string.ean)
                }
                else if(format==1024 || format==512){
                    scanResultBinding.title.text=getString(R.string.upc)
                }
            }

            val funcList=ArrayList<CustomCreatedViewForScanResult>()
            funcList.add(CustomCreatedViewForScanResult(getString(R.string.searchProductOnWeb),R.drawable.search_icon))
            funcList.add(CustomCreatedViewForScanResult(getString(R.string.searchProductOnEbay),R.drawable.open_website_icon))
           // funcList.add(CustomCreatedViewForScanResult("Search on Amazon.in",R.drawable.open_website_icon))
            funcList.add(CustomCreatedViewForScanResult(getString(R.string.searchProductOnAmazon),R.drawable.open_website_icon))
            funcList.add(CustomCreatedViewForScanResult(getString(R.string.searchProductOnFoodFacts),R.drawable.open_website_icon))

            settingAdapter(funcList,barcodeValue)

            scanResultBinding.value.text=barcodeValue
        }
        else{
            scanResultBinding.value.text=barcodeValue
            textToSearchOnWeb=barcodeValue
            scanResultBinding.value.text=barcodeValue
            val funcList=ArrayList<CustomCreatedViewForScanResult>()
            funcList.add(CustomCreatedViewForScanResult(getString(R.string.searchOnWeb),R.drawable.search_icon))
            settingAdapter(funcList,barcodeValue)

        }

    }

    fun loadInterAd(adRandomNumber:Int){
        if(AdHandler.getInstance(this).isNetworkAvailable(this)){
            AdHandler.getInstance(this).loadInterstitialAdWithRandomNumber(this,
                Constant.interstitialId,adRandomNumber,object :
                    OnAdClosedListener {
                override fun onAdClosed() {
                    if(sharedPref.copyToClipboard){
                        copyToClipBoard()
                    }
                }
            })
        }else{
            if(sharedPref.copyToClipboard){
                copyToClipBoard()
            }
        }
    }
    fun loadBigNativeAd(){

        if(AdHandler.getInstance(this).isNetworkAvailable(this)){
            scanResultBinding.bigShimmerLayout.root.visibility=View.VISIBLE
            scanResultBinding.bigAdContainer.visibility=View.GONE
            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if(adObject!= null){

                            scanResultBinding.bigAdContainer.visibility=View.VISIBLE
                            scanResultBinding.bigShimmerLayout.root.visibility=View.GONE

                            AdHandler.getInstance(this@ScanResultAct).showNativeAd(
                                this@ScanResultAct,
                                scanResultBinding.bigAdContainer,
                                adObject as NativeAd,true
                            )
                        }else{
                            scanResultBinding.bigAdContainer.visibility=View.GONE
                            scanResultBinding.bigShimmerLayout.root.visibility=View.VISIBLE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {
                        scanResultBinding.bigAdContainer.visibility=View.GONE
                        scanResultBinding.bigShimmerLayout.root.visibility=View.GONE
                    }
                })

        }else{

            scanResultBinding.bigAdLinearLayout.visibility=View.GONE
        }


    }
    fun performFavTask(){
        isFav=!isFav
        if(barcode==null) {
            favNotesDataUpdated=false
        }else {
            if(barcodeInsertedId.toInt()!=-1){
                barcodeListViewModel.updateFav(this,barcodeInsertedId.toInt(), isFav)
            }
        }
        if(isFav) {
            scanResultBinding.fav.setImageResource(R.drawable.selected_fav_icon)
        }else {
            scanResultBinding.fav.setImageResource(R.drawable.fav_icon)
        }
    }

    fun copyToClipBoard(){
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val value=scanResultBinding.value.text
        val clip = ClipData.newPlainText(getString(R.string.barcodeValueCopied), value)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "${getString(R.string.copied)}:${value}", Toast.LENGTH_SHORT).show()
    }

    fun showMoreFeaturesDialog(buttonView: View){

        val popup = PopupMenu(this, buttonView)

        if(isFav){
            popup.getMenuInflater().inflate(R.menu.scan_result_function_remove_fav, popup.getMenu())
        }else{

            popup.getMenuInflater().inflate(R.menu.scan_result_more_function_menu, popup.getMenu())
        }



        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem): Boolean {

                item.title?.let{
                    if(item.title!!.contains(getString(R.string.removeFromFav))
                        || item.title!!.contains(getString(R.string.addToFav))
                    ){

                        performFavTask()
                    }
                    else if(item.title!!.contains(getString(R.string.menuNotes))){

                            showNotesDialog()
                    }
                    else if(item.title!!.contains(getString(R.string.copyToClipBoard))){

                        copyToClipBoard()
                    }


                }

                return true
            }
        })
        popup.show()
    }
    fun shareBarcodeValueAsText(barcodeValue: String){

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT,scanResultBinding.value.text.toString())//barcodeValue
        startActivity(Intent.createChooser(intent,"Share Using"))
    }


    fun searchOnEbay(barcodeValue: String){

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.ebay.com/sch/i.html?_from=R40&_trksid=p2540003.m570.l1313&_nkw=$barcodeValue"))
        startActivity(intent)
    }
//    fun searchOnAmazonIn(barcodeValue: String){
//
//        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.amazon.in/s?k=${barcodeValue}"))
//        startActivity(intent)
//    }
    fun searchOnAmazonCom(barcodeValue: String){

        val intent = Intent(Intent.ACTION_VIEW,Uri.parse("https://www.amazon.com/s?k=${barcodeValue}"));
        startActivity(intent)

    }
    fun searchOnFoodFacts(barcodeValue: String){
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$barcodeValue"))
        startActivity(intent)
    }
    var textToSearchOnWeb=""
    var eventTitle=""
    var eventStartDate=""
    var eventEndDate=""
    var eventLocation=""
    var eventDescription=""
    var eventStartYear=0
    var eventStartMonth=0
    var eventStartDay=0
    var eventStartHour=0
    var eventStartMin=0
    var eventEndYear=0
    var eventEndMonth=0
    var eventEndDay=0
    var eventEndHour=0
    var eventEndMin=0

    fun manipulateBarcodeValue(barcodeValue: String):String
    {
        val titleStartIndex=barcodeValue.indexOf("SUMMARY:")+8
        val titleEndIndex=barcodeValue.indexOf("\n",titleStartIndex)
        val title=barcodeValue.substring(titleStartIndex,titleEndIndex)// getting title completety correct

        val preString="BEGIN:VCALENDAR\n"+
                "VERSION:2.0\n"+
                "PRODID:${title}\n"+
                barcodeValue +
                "END:VCALENDAR"

        return preString
    }

    fun extractCalender( inputString: String):String
    {
        var barcodeValue=inputString

        if(!barcodeValue.startsWith("BEGIN:VCALENDAR")){
            if(barcodeValue.startsWith("BEGIN:VEVENT")){

                var changeInputString= manipulateBarcodeValue(barcodeValue)
                if(!changeInputString.isEmpty()){
                    barcodeValue=changeInputString
                }
            }else{
                return barcodeValue
            }
        }

        var finalOutputSting=""
            try{
                val ical: ICalendar = Biweekly.parse(barcodeValue).first()
                val event: VEvent = ical.getEvents().get(0)

                var dateStart=""
                event.dateStart?.let {
                    dateStart= event.dateStart.value.toString()
                }

                var dateEnd=""
                event.dateEnd?.let {
                    dateEnd = event.dateEnd.value.toString()

                }
                event.location?.let {
                    eventLocation = event.location.value.toString()
                }
                event.description?.let {
                    eventDescription = event.description.value.toString()
                }
                val pattern = Regex("(?<=PRODID:)[^\n\r]+")

                try{
                    val matchResult = pattern.find(barcodeValue)
                    eventTitle = matchResult?.value.toString()
                }catch (e:Exception) {
                    e.printStackTrace()
                    // MATCH NOT FOUND
                }
                val utc = TimeZone.getTimeZone("UTC")
                val sourceFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
                var destFormat=SimpleDateFormat()
                if(!dateStart.contains("00:00:00")) {
                    destFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm aa")
                }else{
                    destFormat = SimpleDateFormat("dd-MMM-yyyy")
                }

                sourceFormat.timeZone = utc
                val convertedDateStart = sourceFormat.parse(dateStart)


                convertedDateStart?.let {
                    calculateParametersToAddInCalernder(convertedDateStart,true)
                    eventStartDate=destFormat.format(convertedDateStart)
                }

                val convertedDateEnd = sourceFormat.parse(dateEnd)
                convertedDateEnd?.let {

                    calculateParametersToAddInCalernder(convertedDateEnd,false)
                    eventEndDate=destFormat.format(convertedDateEnd)
                }

                if(!eventTitle.equals("")) finalOutputSting+="EventTitle:- "+eventTitle+"\n"
                if(!eventStartDate.equals("")) finalOutputSting+="EventStartDate:- "+eventStartDate+"\n"
                if(!eventEndDate.equals("")) finalOutputSting+="EventEndDate:- "+eventEndDate+"\n"
                if(!eventLocation.equals("")) finalOutputSting+="EventLocation:- "+eventLocation+"\n"
                if(!eventDescription.equals("")) finalOutputSting+="EventDecription:- "+eventDescription+"\n"

            }catch (e:Exception) {
                e.printStackTrace()
            }
        return finalOutputSting
    }

    fun calculateParametersToAddInCalernder(eventDates:Date,isStart:Boolean)
    {

        val formatter = SimpleDateFormat("dd");
        val dateString = formatter.format( eventDates);
        if(isStart) eventStartDay=dateString.toInt()
        else        eventEndDay=dateString.toInt()


        val formatterM = SimpleDateFormat("MM");
        val dateStringM = formatterM.format( eventDates);

        if(isStart) eventStartMonth=dateStringM.toInt()
        else        eventEndMonth=dateStringM.toInt()

        val formatterY = SimpleDateFormat("yyyy");
        val dateStringY = formatterY.format( eventDates);
        if(isStart) eventStartYear=dateStringY.toInt()
        else        eventEndYear=dateStringY.toInt()

        val formatterH = SimpleDateFormat("HH");
        val dateStringH = formatterH.format( eventDates);
        if(isStart) eventStartHour=dateStringH.toInt()
        else        eventEndHour=dateStringH.toInt()

        val formatterm = SimpleDateFormat("mm");
        val dateStringm = formatterm.format( eventDates);
        if(isStart) eventStartMin=dateStringm.toInt()
        else        eventEndMin=dateStringm.toInt()

    }

    fun addToCalender()
    {
        val startDate = Calendar.getInstance()
        startDate.set(Calendar.YEAR, eventStartYear)
        startDate.set(Calendar.MONTH, Calendar.MARCH)
        startDate.set(Calendar.DAY_OF_MONTH, eventStartDay)
        startDate.set(Calendar.HOUR_OF_DAY, eventStartHour)
        startDate.set(Calendar.MINUTE, eventStartMin)
        startDate.set(Calendar.SECOND, 0)

        val endDate = Calendar.getInstance()
        endDate.set(Calendar.YEAR, eventEndYear)
        endDate.set(Calendar.MONTH, Calendar.MARCH)
        endDate.set(Calendar.DAY_OF_MONTH, eventEndDay)
        endDate.set(Calendar.HOUR_OF_DAY, eventEndHour)
        endDate.set(Calendar.MINUTE, eventEndMin)
        endDate.set(Calendar.SECOND, 0)

        val intent = Intent(Intent.ACTION_INSERT)
        intent.type = "vnd.android.cursor.item/event"
        intent.putExtra(CalendarContract.Events.TITLE, "Title")
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Location")
        intent.putExtra(CalendarContract.Events.DESCRIPTION, "Description")
        intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate.timeInMillis)
        intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endDate.timeInMillis)//endDate.timeInMillis

        startActivity(intent)

    }


    var smsPhone=""
    var smsMessage=""

    fun getValueFromSms(barcodeValue: String)
    {
        if(barcodeValue.startsWith("SMSTO:"))
        {
            val pattern = Pattern.compile("SMSTO:(\\d+):(.+)")
            val matcher = pattern.matcher(barcodeValue)
            smsPhone = if (matcher.matches()) matcher.group(1) else ""
            smsMessage = if (matcher.matches()) matcher.group(2) else ""

        }else{

            smsPhone = barcodeValue.substringAfter("sms:").substringBefore("?")
            phone=smsPhone
            val bodyStart = barcodeValue.indexOf("body=") + 5
            smsMessage = URLDecoder.decode(barcodeValue.substring(bodyStart), "UTF-8")

        }


    }

    fun sendSms()
    {
        val smsIntent = Intent(Intent.ACTION_SENDTO)
        smsIntent.data = Uri.parse("smsto:$smsPhone")
        smsIntent.putExtra("sms_body", smsMessage)
        startActivity(smsIntent)
    }
    fun getEmailFromEncoder(barcodeValue: String)
    {
        emailAddress = barcodeValue.substringAfter("mailto:").substringBefore("?")

        // Extract the subject and decode it
        val subjectStart = barcodeValue.indexOf("subject=") + 8
        val subjectEnd = barcodeValue.indexOf("&", startIndex = subjectStart)
        emailSubject = URLDecoder.decode(barcodeValue.substring(subjectStart, subjectEnd), "UTF-8")

        // Extract the body and decode it
        val bodyStart = barcodeValue.indexOf("body=") + 5
        emailMessage = URLDecoder.decode(barcodeValue.substring(bodyStart), "UTF-8")

    }

    fun getDataFromEmailTypeFormat(barcodeValue: String) {

        val emailString = barcodeValue

        val patternList=ArrayList<Pattern>()
        patternList.add(Pattern.compile("MATMSG:TO:(.*?);SUB:(.*?);BODY:(.*?);;"))
        patternList.add(Pattern.compile("MATMSG:TO:(.*?);BODY:(.*?);SUB:(.*?);;"))
        patternList.add(Pattern.compile("MATMSG:SUB:(.*?);TO:(.*?);BODY:(.*?);;"))
        patternList.add(Pattern.compile("MATMSG:SUB:(.*?);BODY:(.*?);TO:(.*?);;"))
        patternList.add(Pattern.compile("MATMSG:BODY:(.*?);TO:(.*?);SUB:(.*?);;"))
        patternList.add(Pattern.compile("MATMSG:BODY:(.*?);SUB:(.*?);TO:(.*?);;"))

        patternList.add(Pattern.compile("mailto:(.*?)?subject=(.*?)&body=(.*?)"))

        for(item in patternList)
        {
            val matcher:Matcher=item.matcher(emailString)
            if(matcher.find())
            {
                if(item.toString().startsWith("mailto")) {
                    getEmailFromEncoder(barcodeValue)
                }else{
                    emailMatchFound(matcher)
                }
                break
            }
        }
    }

    var emailAddress=""
    var emailSubject=""
    var emailMessage=""

    fun emailMatchFound(matcher: Matcher)
    {
        emailAddress = ""
        emailSubject = ""
        emailMessage = ""
        emailAddress = matcher.group(1)?.toString().toString() // Wi-Fi network name
        emailAddress = emailAddress.substring(0, emailAddress.length)

        emailSubject = matcher.group(2)?.toString().toString()// Wi-Fi network type
        emailMessage = matcher.group(3)?.toString().toString()// Wi-Fi network password

        emailAddress=emailAddress.replace("\\;", ";")
        emailSubject =emailSubject.replace("\\;", ";")
        emailMessage =emailMessage.replace("\\;", ";")
    }


    lateinit var barcodeFuncAdapter: BarcodeFunctionalityAdapter
    var fullName=""
    var job=""
    var phone=""
    var email=""
    var company=""
    var street=""
    var locality=""
    var region=""
    var zipCode=""
    var url=""
    var country=""
    var fullAddress=""
    var contactDetailsInHumanReadable=""
    var poBox=""

    var totalAddress= ArrayList<String>()
    var totalNumber= ArrayList<String>()
    var totalemails= ArrayList<String>()
    var totalUrls= ArrayList<String>()

    fun generateRedableContact(inputString:String):ArrayList<CustomCreatedViewForScanResult>
    {
        val contactList=ArrayList<CustomCreatedViewForScanResult>()
        val vcard: VCard? = Ezvcard.parse(inputString).first()

       vcard?.let {
           contactList.add(CustomCreatedViewForScanResult(getString(R.string.addContact),R.drawable.contact_icon))
            var fullNameFound=false
            vcard.formattedName?.let {
                it.value?.let {
                    fullName = vcard.formattedName.value// first name and last name
                    contactDetailsInHumanReadable+="Full Name:-"+fullName+"\n"
                    fullNameFound=true
                }
            }
            if(!fullNameFound){
                vcard.structuredName.family?.let {

                    fullName = vcard.structuredName.family// first name and last name
                    contactDetailsInHumanReadable+="Full Name:-"+fullName+"\n"

                }
            }
            if(vcard.titles.size!=0){
                val jobText:String? = vcard.titles[0].value
                jobText?.let{
                    job=jobText.trim()
                    contactDetailsInHumanReadable+="Job Title:-"+job+"\n"
                }
            }
            var isAddress=false
            vcard.organization?.let {
                val companyText=vcard.organization?.values?.firstOrNull()
                company=  companyText.toString().trim()
                contactDetailsInHumanReadable+="Company:-"+company+"\n"
            }

            totalAddress.clear()
            vcard.addresses?.let {

                for(item in vcard.addresses){
                    var address=""
                    if(item.poBox == null) {

                        item.streetAddress?.let {
                            street=item.streetAddress.trim()
                            address+=street
                        }
                        item.locality?.let {
                            locality=item.locality.trim()
                            address+=locality
                        }
                        item.region?.let {
                            region=item.region.trim()
                            address+=region
                        }
                        item.postalCode?.let {
                            zipCode=item.postalCode.trim()
                            address+=zipCode
                        }
                        item.country?.let {
                            country=item.country.trim()
                            address+=country
                        }

                        if(address != ""){
                            isAddress=true
                            totalAddress.add(address.trim())
                            contactDetailsInHumanReadable+="Address:-"+address.trim()+"\n"

                        }
                    }
                    else  {

                        isAddress=true
                        poBox=item.poBox
                        contactDetailsInHumanReadable+="Address:-"+poBox+"\n"
                        totalAddress.add(poBox.trim())
                    }
                }
                if(isAddress){
                    contactList.add(CustomCreatedViewForScanResult(getString(R.string.viewAddress),R.drawable.current_location_icon,totalAddress[0]))
                }
            }
            totalNumber.clear()
            if(vcard.telephoneNumbers.size!=0){

                for(item in vcard.telephoneNumbers){

                    phone= item.text.trim()

                    if(phone != "null") {

                        var title="${getString(R.string.dial)} ${phone}"
                        totalNumber.add(phone)
                        contactList.add(CustomCreatedViewForScanResult(title,R.drawable.call_icon,phone))
                        contactDetailsInHumanReadable+="Phone:-"+phone+"\n"
                    }
                }
            }
            totalemails.clear()
            if(vcard.emails.size!=0){

                for(item in vcard.emails){

                    email= item.value.trim()

                    if(email != "null") {

                        var title="${getString(R.string.email)} to ${email}"
                        totalemails.add(email)
                        contactList.add(CustomCreatedViewForScanResult(title,R.drawable.email_icon,email))
                        contactDetailsInHumanReadable+="Email:-"+email+"\n"
                    }
                }
            }
            totalUrls.clear()
            if(vcard.urls.size!=0){

                for(item in vcard.urls){

                    val websiteInContact= item.value.trim()

                    if(websiteInContact != "null" && websiteInContact!="") {

                        totalUrls.add(websiteInContact)
                        contactList.add(CustomCreatedViewForScanResult("${getString(R.string.openWebsite)} ${websiteInContact}",R.drawable.website_icon,websiteInContact))
                        contactDetailsInHumanReadable+="Website:-"+websiteInContact+"\n"
                    }
                }
            }
        }

        return contactList
    }


    fun settingAdapter(barcodeFunList:ArrayList<CustomCreatedViewForScanResult>, barcodeValue:String) {
        barcodeFuncAdapter= BarcodeFunctionalityAdapter(this,barcodeFunList) {
                holderTitle,holderValue->//holderName

            if(holderTitle == getString(R.string.addContact))
            {
                addToContact()
            }
            if(holderTitle == getString(R.string.viewAddress))
            {
                viewAddress(holderValue)
            }
            if(holderTitle == getString(R.string.showLocation))//check this
            {
                viewAddress(holderValue)
            }
            if(holderTitle.contains(getString(R.string.dial)))//done
            {
                dial(holderValue)

            }
            if(holderTitle.contains(getString(R.string.email)))//done
            {
                sendEmail(holderValue)
            }
            if(holderTitle.contains(getString(R.string.openWebsite)))
            {
                openWebsite(holderValue)
            }
            if(holderTitle == getString(R.string.connectToWifi))
            {
                connectToWifi()
//                if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED) {
//
//                }else{
//                    ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),100)
//                }

            }
            if(holderTitle.equals(getString(R.string.copyPassword)))
            {
                copyWifiPassword()
            }
            if(holderTitle.equals(getString(R.string.copyNetWorkNmae)))
            {
                copyNetworkName()
            }
            if(holderTitle.equals(getString(R.string.addToCalender)))
            {
                addToCalender()

            }
            if(holderTitle.equals(getString(R.string.sendEmail)))
            {
                sendEmailFromEmailQr()
            }
            if(holderTitle.equals(getString(R.string.sendSms)))
            {
                sendSms()
            }
            if(holderTitle.equals(getString(R.string.searchOnWeb)))
            {
                searchTextOnWeb()
            }
            if(holderTitle.equals(getString(R.string.searchProductOnWeb))) {
                searchProductOnWeb(barcodeValue)
            }
            if(holderTitle.equals(getString(R.string.searchProductOnEbay)))
            {
                searchOnEbay(barcodeValue)
            }
//            if(holderTitle.equals("Search on Amazon.in"))
//            {
//                searchOnAmazonIn(barcodeValue)
//            }
            if(holderTitle.equals(getString(R.string.searchProductOnAmazon)))
            {
                searchOnAmazonCom(barcodeValue)
            }
            if(holderTitle.equals(getString(R.string.searchProductOnFoodFacts)))
            {
                searchOnFoodFacts(barcodeValue)
            }
        }

        val myLinearLayoutManager = object : LinearLayoutManager(this) {
            override fun canScrollVertically(): Boolean {
                return false
            }
        }
        scanResultBinding.recylerView.layoutManager = myLinearLayoutManager
        scanResultBinding.recylerView.adapter=barcodeFuncAdapter
    }

    fun searchTextOnWeb() {
        if(!textToSearchOnWeb.equals("")){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$textToSearchOnWeb"))
            startActivity(intent)
        }
    }
    fun searchProductOnWeb(barcodeValue: String) {
        if(!TextUtils.isEmpty(barcodeValue)){
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$barcodeValue"))
            startActivity(intent)
        }

    }
    fun copyWifiPassword()
    {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        var value=wifiPassword
        val clip = ClipData.newPlainText("PasswordCopied", value)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "${getString(R.string.copied)}:${getString(R.string.wifiPassword)}:${value}", Toast.LENGTH_SHORT).show()
    }
    fun copyNetworkName()
    {
        val clipboard: ClipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        var value=wifiName
        val clip = ClipData.newPlainText("NetworkNameCopied", value)
        clipboard.setPrimaryClip(clip)

        Toast.makeText(this, "${getString(R.string.copied)}:${getString(R.string.networkName)}:${value}", Toast.LENGTH_SHORT).show()
    }
    fun sendEmailFromEmailQr()
    {
//        val mailtoUri = "mailto:$emailAddress"
//        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(mailtoUri))
//        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
//        intent.putExtra(Intent.EXTRA_TEXT, emailMessage)
//
//        startActivity(Intent.createChooser(intent, "Send Email"))

        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:") // only email apps should handle this

        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
        intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
        intent.putExtra(Intent.EXTRA_TEXT, emailMessage)
        startActivity(intent)

//        val emailIntent = Intent(Intent.ACTION_SEND)
//        emailIntent.type = "text/plain"
//        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
//        emailIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject)
//        emailIntent.putExtra(Intent.EXTRA_TEXT, emailMessage)
//
//        val chooserIntent = Intent.createChooser(emailIntent, "Send email using...")
//        startActivity(chooserIntent)
    }

    fun connectToWifi()
    {
        WifiConnector().connect(
                this,
               wifiEncrypType,
                wifiName,
                wifiPassword,
                false,
               "",
               "",
                "",
               ""
            )

//        val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
//        if (!wifiManager.isWifiEnabled()) {
//            wifiManager.setWifiEnabled(true);
//        }
//        var wifiName="Redmi Note 7 Pro"
//        var wifiPassword=12121212
//        val wifiConfig = WifiConfiguration()
//        wifiConfig.SSID = "\"" +  wifiName+ "\""
//        wifiConfig.preSharedKey = "\"" + wifiPassword + "\""
//       // if(wifiEncrypType.contains("WPA"))
//        wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
//        val networkId = wifiManager.addNetwork(wifiConfig)
//        wifiManager.enableNetwork(networkId, true);

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray)
    { super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==100)
        {
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED) {

                connectToWifi()
            }else {

                Toast.makeText(applicationContext,"Permissino denied", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun addToContact()
    {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION)
        intent.type = ContactsContract.RawContacts.CONTENT_TYPE

        if(!fullName.equals("null") && !fullName.equals("")){

            intent.putExtra(ContactsContract.Intents.Insert.NAME, fullName)
        }
        if(!job.equals("null") && !job.equals("")){// NOT BEEN ADDED

            intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, job)
        }
        if(!company.equals("null") && !company.equals("")){

            intent.putExtra(ContactsContract.Intents.Insert.COMPANY, company)
        }

        if(totalNumber.size!=0){

            if(totalNumber.size>3){
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, totalNumber[0])
                intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, totalNumber[1])
                intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, totalNumber[2])
            }else{
                when(totalNumber.size){
                    1->{
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, totalNumber[0])
                    }
                    2->{
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, totalNumber[0])
                        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, totalNumber[1])
                    }
                    3->{
                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, totalNumber[0])
                        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, totalNumber[1])
                        intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, totalNumber[2])
                    }
                }
            }
        }else{
            if(smsPhone!=""){
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, smsPhone)

            }
        }

        if(totalemails.size!=0){

            if(totalemails.size>3){
                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, totalemails[0])
                intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, totalemails[1])
                intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, totalemails[2])
            }else{
                when(totalemails.size){
                    1->{
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, totalemails[0])
                    }
                    2->{
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, totalemails[0])
                        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, totalemails[1])
                    }
                    3->{
                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, totalemails[0])
                        intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_EMAIL, totalemails[1])
                        intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_EMAIL, totalemails[2])
                    }
                }
            }
        }

        if(totalUrls.size!=0){

            val data = ArrayList<ContentValues>()
            val row1 = ContentValues()
            row1.put(ContactsContract.Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
            row1.put(Website.URL, totalUrls[0])
            row1.put(Website.LABEL, "abc")
            row1.put(Website.TYPE, Website.TYPE_HOME)
            data.add(row1)
            intent.putExtra(ContactsContract.Intents.Insert.DATA, data)
        }

//        if(!url.equals("null") && !url.equals("")){
//
//            val data = ArrayList<ContentValues>()
//            val row1 = ContentValues()
//            row1.put(ContactsContract.Data.MIMETYPE, Website.CONTENT_ITEM_TYPE)
//            row1.put(Website.URL, url)
//            row1.put(Website.LABEL, "abc")
//            row1.put(Website.TYPE, Website.TYPE_HOME)
//            data.add(row1)
//            intent.putExtra(ContactsContract.Intents.Insert.DATA, data)
//        }


        if(totalAddress.size!=0){
            intent.putExtra(ContactsContract.Intents.Insert.POSTAL, totalAddress[0])
        }
//        if(!fullAddress.equals("null") && !fullAddress.equals("")){
//
//            intent.putExtra(ContactsContract.Intents.Insert.POSTAL, fullAddress)
//        }

        if(!note.equals("")){
            intent.putExtra(ContactsContract.Intents.Insert.NOTES, note)
        }
        startActivity(intent)
    }

    fun viewAddress( addressToSearch:String)
    {

        val encodedAddress = Uri.encode(addressToSearch)
        val searchUri = "https://www.google.com/maps/search/?api=1&query=$encodedAddress"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(searchUri))
        intent.setPackage("com.google.android.apps.maps")
        startActivity(intent)
    }

    fun dial(numberToDial:String)
    {

        val dialerUri = "tel:" + numberToDial
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(dialerUri))
        startActivity(intent)
    }

    fun sendEmail(repEmail:String)
    {
        val mailtoUri = "mailto:$repEmail"
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(mailtoUri))
        startActivity(Intent.createChooser(intent, "Send Email"))
    }

    fun openWebsite(websiteUrl:String)
    {
        var newUrl=websiteUrl
        // Remain:-check if  barcodeCodeType is of product type the remove https . when prduct barcode is scanned
        if(!newUrl.contains("https://")){
            newUrl ="https://"+newUrl
        }
        try{
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newUrl))
            startActivity(intent)
        }catch (e:Exception)
        {
            Toast.makeText(this, getString(R.string.invalidUrl), Toast.LENGTH_SHORT).show()
        }


    }
    var wifiName=""
    var wifiEncrypType=""
    var wifiPassword=""
    var isWifiHidden=""

    fun retrieveWifiData(bValue:String):String
    {
        var finalFormatedString=""
        val wifiString = bValue
        val patternList=ArrayList<Pattern>()
        patternList.add(Pattern.compile("WIFI:S:(.*?);T:(.*?);P:(.*?);H:(.*?);"))
        patternList.add(Pattern.compile("WIFI:S:(.*?);P:(.*?);T:(.*?);H:(.*?);"))
        patternList.add(Pattern.compile("WIFI:T:(.*?);S:(.*?);P:(.*?);H:(.*?);"))
        patternList.add(Pattern.compile("WIFI:T:(.*?);P:(.*?);S:(.*?);H:(.*?);"))
        patternList.add(Pattern.compile("WIFI:P:(.*?);T:(.*?);S:(.*?);H:(.*?);"))
        patternList.add(Pattern.compile("WIFI:P:(.*?);S:(.*?);T:(.*?);H:(.*?);"))

        patternList.add(Pattern.compile("WIFI:S:(.*?);T:(.*?);P:(.*?);;"))
        patternList.add(Pattern.compile("WIFI:S:(.*?);P:(.*?);T:(.*?);;"))
        patternList.add(Pattern.compile("WIFI:T:(.*?);S:(.*?);P:(.*?);;"))
        patternList.add(Pattern.compile("WIFI:T:(.*?);P:(.*?);S:(.*?);;"))
        patternList.add(Pattern.compile("WIFI:P:(.*?);T:(.*?);S:(.*?);;"))
        patternList.add(Pattern.compile("WIFI:P:(.*?);S:(.*?);T:(.*?);;"))
        for(item in patternList)
        {
            val matcher:Matcher=item.matcher(wifiString)
            if(matcher.find())
            {
                finalFormatedString=wifiMatchFound(matcher,bValue)
                break
            }
        }

        return finalFormatedString
    }

    fun wifiMatchFound(matcher: Matcher,barcodeValue: String):String
    {
        var finalFormatedString=""
        val endTrueString="H:true;;"
        var endFalseString="H:false;;"
        if(barcodeValue.endsWith(endTrueString) || barcodeValue.endsWith(endFalseString)) {

            wifiName = matcher.group(1)?.toString().toString() // Wi-Fi network name
            wifiEncrypType = matcher.group(2)?.toString().toString()// Wi-Fi network type
            wifiPassword = matcher.group(3)?.toString().toString()// Wi-Fi network password
            isWifiHidden = matcher.group(4)?.toString().toString()// Wi-Fi network password

            wifiName = wifiName.replace("\\:", ":").replace("\\;", ";")
            wifiPassword = wifiPassword.replace("\\:", ":").replace("\\;", ";")
        }else {
            wifiName = matcher.group(1)?.toString().toString() // Wi-Fi network name
            wifiEncrypType = matcher.group(2)?.toString().toString()// Wi-Fi network type
            wifiPassword = matcher.group(3)?.toString().toString()// Wi-Fi network password

            wifiName = wifiName.replace("\\:", ":").replace("\\;", ";")
            wifiPassword = wifiPassword.replace("\\:", ":").replace("\\;", ";")

        }
        finalFormatedString="WI-FI Name:- "+wifiName+"\n"+"EncryptionType:- "+wifiEncrypType
        if(!wifiPassword.equals("")) finalFormatedString +="\n"+"Password:- "+wifiPassword
        if(!isWifiHidden.equals("")) finalFormatedString +="\n"+"Hidden:- "+isWifiHidden

        return  finalFormatedString
    }

    lateinit var notesDialog:DialogAddNotesBinding
    fun showNotesDialog()
    {
        sendFirebaseEvents(this,"NotesDialogOpenScanResultActivity")
        sendAppMetricaEvents("NotesDialogOpenScanResultActivity")


        notesDialog=DialogAddNotesBinding.inflate(layoutInflater)
        notesDialog.inputText.setText(note)
       var dialog= MaterialAlertDialogBuilder(this,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(notesDialog.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {

                    //DIALOG CANCEL
                }

            })
           .show()

        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCanceledOnTouchOutside(false)
        notesDialog.cancel.setOnClickListener{

            dialog.cancel()
        }
        notesDialog.ok.setOnClickListener{

            note=notesDialog.inputText.editableText.toString()

            note=note.trim()// remove starting space

            if(barcode==null)
            {
                favNotesDataUpdated=false

            }else {

                if(barcodeInsertedId.toInt()!=-1){

                    barcodeListViewModel.updateNotes(this,barcodeInsertedId.toInt(), note)
                }
            }


            if(note!="") {

                scanResultBinding.notesText.visibility= View.VISIBLE
                scanResultBinding.notesText.text= note
            }
            else{//first there was note and then u remove so close view

                if(scanResultBinding.notesText.visibility== View.VISIBLE) {

                    scanResultBinding.notesText.visibility= View.GONE
                }
            }
            dialog.cancel()

        }
    }
    fun curentDataTime():String
    {
        var dateTime=""
        val currentTime: Date = Calendar.getInstance().getTime()
        val df = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())//MM GIVES 03
        val formattedDate: String = df.format(currentTime)// 18 marc/2023

        dateTime += formattedDate+"   "

        val delegate = "hh:mm aaa"
        var time=DateFormat.format(delegate, Calendar.getInstance().time)// 11:17 pm
        dateTime += time

        return dateTime
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}