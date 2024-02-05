package scan.reader.qrscanner.barcodescanner.activity


import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.PopupWindow
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.nativead.NativeAd
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.ads.AdEventListener
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromEventBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class QrEventAct : AppCompatActivity() , DatePickerDialog.OnDateSetListener{

    lateinit var eventBinding:ActivityQrFromEventBinding

    var allDayEvent=false

    var startYear:Int=0
    var startDate:Int=0
    var startMonth:Int=0

    var endYear:Int=0
    var endDate:Int=0
    var endMonth:Int=0

    var startHour:Int=-1
    var startMin:Int=0
    var endHour:Int=-1
    var endMin:Int=0
    override fun onCreate(savedInstanceState: Bundle?) {
        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        eventBinding= ActivityQrFromEventBinding.inflate(layoutInflater)
        setContentView(eventBinding.root)

        sendFirebaseEvents(this,"CreateEventAct")
        sendAppMetricaEvents("CreateEventAct")
//
//        Constant.nativeId?.let {
//            loadNewNativeAd()
//        }

        eventBinding.backButton.setOnClickListener{
            onBackPressed()
        }
        eventBinding.customSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->

            val startTime=eventBinding.startTime.text.toString().trim()
            val endTime=eventBinding.endTime.text.toString().trim()

            if (isChecked) {
                // Switch is ON
                allDayEvent=true
                eventBinding.startTime.visibility=View.GONE
                eventBinding.endTime.visibility=View.GONE

                eventBinding.startTimeInputErrorIcon.visibility= View.GONE
                eventBinding.endTimeInputErrorIcon.visibility= View.GONE



            } else {
                // Switch is OFF
                allDayEvent=false
                eventBinding.startTime.visibility=View.VISIBLE
                eventBinding.endTime.visibility=View.VISIBLE

                if(startTime==""){
                    eventBinding.startTimeInputErrorIcon.visibility= View.VISIBLE
                }else{
                    eventBinding.startTimeInputErrorIcon.visibility= View.GONE
                }

                if(endTime==""){
                    eventBinding.endTimeInputErrorIcon.visibility= View.VISIBLE
                }else{
                    eventBinding.startTimeInputErrorIcon.visibility= View.GONE
                }
            }
        })

        eventBinding.startDate.setOnClickListener{
            val mcurrentDate = Calendar.getInstance()
            val mYear = mcurrentDate[Calendar.YEAR]
            val mMonth = mcurrentDate[Calendar.MONTH]
            val mDay = mcurrentDate[Calendar.DAY_OF_MONTH]

            val mDatePicker: DatePickerDialog = DatePickerDialog(this, R.style.DialogTheme, object : OnDateSetListener {
                    override fun onDateSet(
                        view: android.widget.DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {

                        startYear=year
                        startDate=dayOfMonth
                        startMonth=month
                        eventBinding.startDate.text="${month+1}/${dayOfMonth}/${year}"
                        eventBinding.startDateInputErrorIcon.visibility=View.GONE

                    }

                }, mYear, mMonth, mDay)

            mDatePicker.show()

        }
        eventBinding.endDate.setOnClickListener{
            val mcurrentDate = Calendar.getInstance()
            val mYear = mcurrentDate[Calendar.YEAR]
            val mMonth = mcurrentDate[Calendar.MONTH]
            val mDay = mcurrentDate[Calendar.DAY_OF_MONTH]

            val mDatePicker: DatePickerDialog = DatePickerDialog(this, R.style.DialogTheme, object : OnDateSetListener {
                    override fun onDateSet(
                        view: android.widget.DatePicker?,
                        year: Int,
                        month: Int,
                        dayOfMonth: Int
                    ) {
                        endYear=year
                        endDate=dayOfMonth
                        endMonth=month

                        eventBinding.endDate.text="${month+1}/${dayOfMonth}/${year}"

                        eventBinding.endDateInputErrorIcon.visibility=View.GONE
                    }

                }, mYear, mMonth, mDay)

            mDatePicker.show()

        }
        eventBinding.tickButton.setOnClickListener{

            sendFirebaseEvents(this,"TickClickCreateEventAct")
            sendAppMetricaEvents("TickClickCreateEventAct")


            title= eventBinding.eventTitle.text.toString().trim()
            location= eventBinding.location.text.toString().trim()
            description= eventBinding.description.text.toString().trim()

            if(title==""){

                if(!titleInputErrorIconShown){

                    titleInputErrorIconShown=true
                    titleNoInputPopUp(eventBinding.eventTitle)
                    eventBinding.titleInputErrorIcon.visibility= View.VISIBLE
                }

            }

            val startDate=eventBinding.startDate.text.toString().trim()
            val endDate=eventBinding.endDate.text.toString().trim()
            val startTime=eventBinding.startTime.text.toString().trim()
            val endTime=eventBinding.endTime.text.toString().trim()

            if(startDate==""){
                eventBinding.startDateInputErrorIcon.visibility=View.VISIBLE
            }
            if(endDate==""){
                eventBinding.endDateInputErrorIcon.visibility=View.VISIBLE
            }
            if(allDayEvent)
            {
                if(startDate!="" && endDate!="" && title != ""){
                    checkNewer()
                }
            }else{
                if(startTime==""){
                    eventBinding.startTimeInputErrorIcon.visibility=View.VISIBLE
                }
                if(endTime==""){
                    eventBinding.endTimeInputErrorIcon.visibility=View.VISIBLE
                }
                if(startDate!="" && endDate!="" && startTime!="" && endTime!="" && title != ""){
                    checkNewer()
                }
            }
        }

        eventBinding.eventTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(eventBinding.eventTitle.text.toString().trim()!=""){

                    titleInputErrorIconShown=false
                    titlePopUpWindow?.dismiss()
                    eventBinding.titleInputErrorIcon.visibility= View.GONE
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        eventBinding.startTime.setOnClickListener{
            val c = Calendar.getInstance()

            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                this,
                R.style.TimePickerTheme,
                { view, hourOfDay, selectedMinute ->

                    var amPmChecker=""
                    //view.is24HourView
                    if(hourOfDay<12){
                        amPmChecker="A.M"
                    }else{
                        amPmChecker="P.M"
                    }
                    startHour=hourOfDay
                    startMin=selectedMinute

                    eventBinding.startTimeInputErrorIcon.visibility=View.GONE
                    eventBinding.startTime.text="${hourOfDay}:${selectedMinute}  ${amPmChecker}"
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }
        eventBinding.endTime.setOnClickListener{
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            val timePickerDialog = TimePickerDialog(
                this,
                R.style.TimePickerTheme,
                { view, hourOfDay, selectedMinute ->

                    var amPmChecker=""
                    //view.is24HourView
                    if(hourOfDay<12){
                        amPmChecker="A.M"
                    }else{
                        amPmChecker="P.M"
                    }

                    endHour=hourOfDay
                    endMin=selectedMinute
                    eventBinding.endTime.text="${hourOfDay}:${selectedMinute}  ${amPmChecker}"

                    eventBinding.endTimeInputErrorIcon.visibility=View.GONE
                },
                hour,
                minute,
                true
            )

            timePickerDialog.show()
        }
    }



    fun loadNewNativeAd() {

        if (AdHandler.getInstance(this).isNetworkAvailable(this)) {

            eventBinding.adContainer.visibility = View.GONE
            eventBinding.shimmerLayout.root.visibility = View.VISIBLE

            AdHandler.getInstance(this).loadNativeAd(
                this,
                Constant.nativeId,
                object : AdEventListener {
                    override fun onAdLoaded(adObject: Any?) {
                        if (adObject != null) {

                            eventBinding.adContainer.visibility = View.VISIBLE
                            eventBinding.shimmerLayout.root.visibility = View.GONE

                            AdHandler.getInstance(this@QrEventAct).showNativeAd(
                                this@QrEventAct,
                                eventBinding.adContainer,
                                adObject as NativeAd, true
                            )
                        } else {
                            eventBinding.adContainer.visibility = View.GONE
                            eventBinding.shimmerLayout.root.visibility = View.GONE
                        }
                    }

                    override fun onAdClosed() {}
                    override fun onLoadError(errorCode: String?) {

                        eventBinding.adContainer.visibility = View.GONE
                        eventBinding.shimmerLayout.root.visibility = View.GONE
                    }
                })

        } else {

            eventBinding.adLinearLayout.visibility = View.GONE

        }

    }


    var titlePopUpWindow: PopupWindow?=null
    var titleInputErrorIconShown=false
    fun titleNoInputPopUp(refView:View){

        var inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        titlePopUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        titlePopUpWindow?.showAsDropDown(refView,0,0, Gravity.END)
        titlePopUpWindow?.isFocusable=false
    }


    var title=""
    var location=""
    var description=""
    fun checkNewer()
    {
        val firstDate = Calendar.getInstance()
        val secondDate = Calendar.getInstance()
        firstDate.set(startYear,startMonth,startDate)
        secondDate.set(endYear,endMonth,endDate)

        if (secondDate.timeInMillis >= firstDate.timeInMillis) {

            if(allDayEvent)
            {
                getStartEndDate()
                var veventString="BEGIN:VCALENDAR\n"+
                        "VERSION:2.0\n"+
                        "PRODID:${title}\n"+
                        "BEGIN:VEVENT\n"+
                        "DTSTART:${startDateString}\n"+
                        "DTEND:${endDateString}\n"

                if(!location.equals("null") && !location.equals("")) veventString+="LOCATION:${location}\n"
                if(!description.equals("null") && !description.equals("")) veventString+="DESCRIPTION:${description}\n"

                veventString+="END:VEVENT\n"+"END:VCALENDAR"

                val intent=Intent(this@QrEventAct, ViewCodeAct::class.java)
                intent.putExtra("customGenerator",1)
                intent.putExtra("barcodeValue",veventString)
                intent.putExtra("barcodeType",11)//1 is for calender event
                intent.putExtra("barcodeFormat",256)// 7 is for text type
                startActivity(intent)
            }
            else{
                val firstTime = Date()
                val secondTime = Date()

                firstTime.hours = startHour
                firstTime.minutes = startMin
                secondTime.hours = endHour
                secondTime.minutes =endMin

                if(secondDate.timeInMillis > firstDate.timeInMillis){

                    dataApproved()
                }else{// they are equal
                    if (secondTime.time >= firstTime.time) {

                        dataApproved()

                    } else {
                        Log.d("dateChekerTime","smaller")
                    }
                }

            }

        }
        else {
            Log.d("dateCheker","smaller")
        }
    }

    var startDateString=""
    var endDateString=""

    fun getStartEndDate()
    {
        startDateString=""
        endDateString=""
        startDateString+=startYear.toString()
        if((startMonth/10)<1) startDateString+="0${startMonth+1}"
        else startDateString+=(startMonth+1)

        if((startDate/10<1))  startDateString+="0${startDate}"
        else startDateString+=startDate.toString()

        endDateString+=endYear.toString()
        if((endMonth/10)<1) endDateString+="0${endMonth+1}"
        else endDateString+=(endMonth+1)

        if((endDate/10<1))  endDateString+="0${endDate}"
        else endDateString+=endDate.toString()


    }
    fun mergeDateTime(year:Int,month:Int, date:Int,hour:Int, min:Int): String
    {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.YEAR,year)
        calendar.set(Calendar.MONTH,month)
        calendar.set(Calendar.DAY_OF_MONTH,date)
       // calendar.set(year,month,date)
        calendar.set(Calendar.HOUR_OF_DAY,hour) // Set the hour (3:00 PM)
        calendar.set(Calendar.MINUTE,min) // Set the min (3:00 PM)
        val dateFormat = SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'")
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
        val timestamp: String = dateFormat.format(calendar.time)
        return timestamp
    }


    fun dataApproved()
    {
        var startValue= mergeDateTime(startYear,startMonth,startDate,startHour,startMin)
        var endValue=mergeDateTime(endYear,endMonth,endDate,endHour,endMin)
        var veventString="BEGIN:VCALENDAR\n"+
                "VERSION:2.0\n"+
                "PRODID:${title}\n"+
                "BEGIN:VEVENT\n"+
                "DTSTART:${startValue}\n"+
                "DTEND:${endValue}\n"

        if(!location.equals("null") && !location.equals("")) veventString+="LOCATION:${location}\n"
        if(!description.equals("null") && !description.equals("")) veventString+="DESCRIPTION:${description}\n"

        veventString+="END:VEVENT\n"+"END:VCALENDAR"
        var intent=Intent(this@QrEventAct, ViewCodeAct::class.java)
        intent.putExtra("customGenerator",1)
        intent.putExtra("barcodeValue",veventString)
        intent.putExtra("barcodeType",11)//11 is for calender event
        intent.putExtra("barcodeFormat",256)// 7 is for text type

        startActivity(intent)
    }

    override fun onDateSet(
        view: android.widget.DatePicker?,
        year: Int,
        month: Int,
        dayOfMonth: Int
    ) {
        val mCalendar: Calendar = Calendar.getInstance()
        mCalendar.set(Calendar.YEAR, year)
        mCalendar.set(Calendar.MONTH, month)
        mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val selectedDate: String = DateFormat.getDateInstance(DateFormat.FULL).format(mCalendar.getTime())
        eventBinding.startDate.setText(selectedDate)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}