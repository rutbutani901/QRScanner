package scan.reader.qrscanner.barcodescanner.activity



import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import scan.reader.qrscanner.barcodescanner.GPSTracker
import scan.reader.qrscanner.barcodescanner.modelClass.LatLng
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromLocationBinding
import scan.reader.qrscanner.barcodescanner.databinding.DialogCameraPermissionSettingBinding
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import scan.reader.qrscanner.barcodescanner.ads.AdHandler
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.util.Constant
import kotlin.collections.HashMap


class QrLocationAct : AppCompatActivity() {

    var mLocationOverlay: MyLocationNewOverlay? = null
    var mCompassOverlay: CompassOverlay? = null


    lateinit var locationBinding: ActivityQrFromLocationBinding


    val COUNTRY_ISOS: HashMap<String?, LatLng?> = object : HashMap<String?, LatLng?>() {
        init {
            put("AD", LatLng(42.546245, 1.601554))
            put("AE", LatLng(23.424076, 53.847818))
            put("AF", LatLng(33.93911, 67.709953))
            put("AG", LatLng(17.060816, -61.796428))
            put("AI", LatLng(18.220554, -63.068615))
            put("AL", LatLng(41.153332, 20.168331))
            put("AM", LatLng(40.069099, 45.038189))
            put("AN", LatLng(12.226079, -69.060087))
            put("AO", LatLng(-11.202692, 17.873887))
            put("AQ", LatLng(-75.250973, -0.071389))
            put("AR", LatLng(-38.416097, -63.616672))
            put("AS", LatLng(-14.270972, -170.132217))
            put("AT", LatLng(47.516231, 14.550072))
            put("AU", LatLng(-25.274398, 133.775136))
            put("AW", LatLng(12.52111, -69.968338))
            put("AZ", LatLng(40.143105, 47.576927))
            put("BA", LatLng(43.915886, 17.679076))
            put("BB", LatLng(13.193887, -59.543198))
            put("BD", LatLng(23.684994, 90.356331))
            put("BE", LatLng(50.503887, 4.469936))
            put("BF", LatLng(12.238333, -1.561593))
            put("BG", LatLng(42.733883, 25.48583))
            put("BH", LatLng(25.930414, 50.637772))
            put("BI", LatLng(-3.373056, 29.918886))
            put("BJ", LatLng(9.30769, 2.315834))
            put("BM", LatLng(32.321384, -64.75737))
            put("BN", LatLng(4.535277, 114.727669))
            put("BO", LatLng(-16.290154, -63.588653))
            put("BR", LatLng(-14.235004, -51.92528))
            put("BS", LatLng(25.03428, -77.39628))
            put("BT", LatLng(27.514162, 90.433601))
            put("BV", LatLng(-54.423199, 3.413194))
            put("BW", LatLng(-22.328474, 24.684866))
            put("BY", LatLng(53.709807, 27.953389))
            put("BZ", LatLng(17.189877, -88.49765))
            put("CA", LatLng(56.130366, -106.346771))
            put("CC", LatLng(-12.164165, 96.870956))
            put("CD", LatLng(-4.038333, 21.758664))
            put("CF", LatLng(6.611111, 20.939444))
            put("CG", LatLng(-0.228021, 15.827659))
            put("CH", LatLng(46.818188, 8.227512))
            put("CI", LatLng(7.539989, -5.54708))
            put("CK", LatLng(-21.236736, -159.777671))
            put("CL", LatLng(-35.675147, -71.542969))
            put("CM", LatLng(7.369722, 12.354722))
            put("CN", LatLng(35.86166, 104.195397))
            put("CO", LatLng(4.570868, -74.297333))
            put("CR", LatLng(9.748917, -83.753428))
            put("CU", LatLng(21.521757, -77.781167))
            put("CV", LatLng(16.002082, -24.013197))
            put("CX", LatLng(-10.447525, 105.690449))
            put("CY", LatLng(35.126413, 33.429859))
            put("CZ", LatLng(49.817492, 15.472962))
            put("DE", LatLng(51.165691, 10.451526))
            put("DJ", LatLng(11.825138, 42.590275))
            put("DK", LatLng(56.26392, 9.501785))
            put("DM", LatLng(15.414999, -61.370976))
            put("DO", LatLng(18.735693, -70.162651))
            put("DZ", LatLng(28.033886, 1.659626))
            put("EC", LatLng(-1.831239, -78.183406))
            put("EE", LatLng(58.595272, 25.013607))
            put("EG", LatLng(26.820553, 30.802498))
            put("EH", LatLng(24.215527, -12.885834))
            put("ER", LatLng(15.179384, 39.782334))
            put("ES", LatLng(40.463667, -3.74922))
            put("ET", LatLng(9.145, 40.489673))
            put("FI", LatLng(61.92411, 25.748151))
            put("FJ", LatLng(-16.578193, 179.414413))
            put("FK", LatLng(-51.796253, -59.523613))
            put("FM", LatLng(7.425554, 150.550812))
            put("FO", LatLng(61.892635, -6.911806))
            put("FR", LatLng(46.227638, 2.213749))
            put("GA", LatLng(-0.803689, 11.609444))
            put("GB", LatLng(55.378051, -3.435973))
            put("GD", LatLng(12.262776, -61.604171))
            put("GE", LatLng(42.315407, 43.356892))
            put("GF", LatLng(3.933889, -53.125782))
            put("GG", LatLng(49.465691, -2.585278))
            put("GH", LatLng(7.946527, -1.023194))
            put("GI", LatLng(36.137741, -5.345374))
            put("GL", LatLng(71.706936, -42.604303))
            put("GM", LatLng(13.443182, -15.310139))
            put("GN", LatLng(9.945587, -9.696645))
            put("GP", LatLng(16.995971, -62.067641))
            put("GQ", LatLng(1.650801, 10.267895))
            put("GR", LatLng(39.074208, 21.824312))
            put("GS", LatLng(-54.429579, -36.587909))
            put("GT", LatLng(15.783471, -90.230759))
            put("GU", LatLng(13.444304, 144.793731))
            put("GW", LatLng(11.803749, -15.180413))
            put("GY", LatLng(4.860416, -58.93018))
            put("GZ", LatLng(31.354676, 34.308825))
            put("HK", LatLng(22.396428, 114.109497))
            put("HM", LatLng(-53.08181, 73.504158))
            put("HN", LatLng(15.199999, -86.241905))
            put("HR", LatLng(45.1, 15.2))
            put("HT", LatLng(18.971187, -72.285215))
            put("HU", LatLng(47.162494, 19.503304))
            put("ID", LatLng(-0.789275, 113.921327))
            put("IE", LatLng(53.41291, -8.24389))
            put("IL", LatLng(31.046051, 34.851612))
            put("IM", LatLng(54.236107, -4.548056))
            put("IN", LatLng(20.593684, 78.96288))
            put("IO", LatLng(-6.343194, 71.876519))
            put("IQ", LatLng(33.223191, 43.679291))
            put("IR", LatLng(32.427908, 53.688046))
            put("IS", LatLng(64.963051, -19.020835))
            put("IT", LatLng(41.87194, 12.56738))
            put("JE", LatLng(49.214439, -2.13125))
            put("JM", LatLng(18.109581, -77.297508))
            put("JO", LatLng(30.585164, 36.238414))
            put("JP", LatLng(36.204824, 138.252924))
            put("KE", LatLng(-0.023559, 37.906193))
            put("KG", LatLng(41.20438, 74.766098))
            put("KH", LatLng(12.565679, 104.990963))
            put("KI", LatLng(-3.370417, -168.734039))
            put("KM", LatLng(-11.875001, 43.872219))
            put("KN", LatLng(17.357822, -62.782998))
            put("KP", LatLng(40.339852, 127.510093))
            put("KR", LatLng(35.907757, 127.766922))
            put("KW", LatLng(29.31166, 47.481766))
            put("KY", LatLng(19.513469, -80.566956))
            put("KZ", LatLng(48.019573, 66.923684))
            put("LA", LatLng(19.85627, 102.495496))
            put("LB", LatLng(33.854721, 35.862285))
            put("LC", LatLng(13.909444, -60.978893))
            put("LI", LatLng(47.166, 9.555373))
            put("LK", LatLng(7.873054, 80.771797))
            put("LR", LatLng(6.428055, -9.429499))
            put("LS", LatLng(-29.609988, 28.233608))
            put("LT", LatLng(55.169438, 23.881275))
            put("LU", LatLng(49.815273, 6.129583))
            put("LV", LatLng(56.879635, 24.603189))
            put("LY", LatLng(26.3351, 17.228331))
            put("MA", LatLng(31.791702, -7.09262))
            put("MC", LatLng(43.750298, 7.412841))
            put("MD", LatLng(47.411631, 28.369885))
            put("ME", LatLng(42.708678, 19.37439))
            put("MG", LatLng(-18.766947, 46.869107))
            put("MH", LatLng(7.131474, 171.184478))
            put("MK", LatLng(41.608635, 21.745275))
            put("ML", LatLng(17.570692, -3.996166))
            put("MM", LatLng(21.913965, 95.956223))
            put("MN", LatLng(46.862496, 103.846656))
            put("MO", LatLng(22.198745, 113.543873))
            put("MP", LatLng(17.33083, 145.38469))
            put("MQ", LatLng(14.641528, -61.024174))
            put("MR", LatLng(21.00789, -10.940835))
            put("MS", LatLng(16.742498, -62.187366))
            put("MT", LatLng(35.937496, 14.375416))
            put("MU", LatLng(-20.348404, 57.552152))
            put("MV", LatLng(3.202778, 73.22068))
            put("MW", LatLng(-13.254308, 34.301525))
            put("MX", LatLng(23.634501, -102.552784))
            put("MY", LatLng(4.210484, 101.975766))
            put("MZ", LatLng(-18.665695, 35.529562))
            put("NA", LatLng(-22.95764, 18.49041))
            put("NC", LatLng(-20.904305, 165.618042))
            put("NE", LatLng(17.607789, 8.081666))
            put("NF", LatLng(-29.040835, 167.954712))
            put("NG", LatLng(9.081999, 8.675277))
            put("NI", LatLng(12.865416, -85.207229))
            put("NL", LatLng(52.132633, 5.291266))
            put("NO", LatLng(60.472024, 8.468946))
            put("NP", LatLng(28.394857, 84.124008))
            put("NR", LatLng(-0.522778, 166.931503))
            put("NU", LatLng(-19.054445, -169.867233))
            put("NZ", LatLng(-40.900557, 174.885971))
            put("OM", LatLng(21.512583, 55.923255))
            put("PA", LatLng(8.537981, -80.782127))
            put("PE", LatLng(-9.189967, -75.015152))
            put("PF", LatLng(-17.679742, -149.406843))
            put("PG", LatLng(-6.314993, 143.95555))
            put("PH", LatLng(12.879721, 121.774017))
            put("PK", LatLng(30.375321, 69.345116))
            put("PL", LatLng(51.919438, 19.145136))
            put("PM", LatLng(46.941936, -56.27111))
            put("PN", LatLng(-24.703615, -127.439308))
            put("PR", LatLng(18.220833, -66.590149))
            put("PS", LatLng(31.952162, 35.233154))
            put("PT", LatLng(39.399872, -8.224454))
            put("PW", LatLng(7.51498, 134.58252))
            put("PY", LatLng(-23.442503, -58.443832))
            put("QA", LatLng(25.354826, 51.183884))
            put("RE", LatLng(-21.115141, 55.536384))
            put("RO", LatLng(45.943161, 24.96676))
            put("RS", LatLng(44.016521, 21.005859))
            put("RU", LatLng(61.52401, 105.318756))
            put("RW", LatLng(-1.940278, 29.873888))
            put("SA", LatLng(23.885942, 45.079162))
            put("SB", LatLng(-9.64571, 160.156194))
            put("SC", LatLng(-4.679574, 55.491977))
            put("SD", LatLng(12.862807, 30.217636))
            put("SE", LatLng(60.128161, 18.643501))
            put("SG", LatLng(1.352083, 103.819836))
            put("SH", LatLng(-24.143474, -10.030696))
            put("SI", LatLng(46.151241, 14.995463))
            put("SJ", LatLng(77.553604, 23.670272))
            put("SK", LatLng(48.669026, 19.699024))
            put("SL", LatLng(8.460555, -11.779889))
            put("SM", LatLng(43.94236, 12.457777))
            put("SN", LatLng(14.497401, -14.452362))
            put("SO", LatLng(5.152149, 46.199616))
            put("SR", LatLng(3.919305, -56.027783))
            put("ST", LatLng(0.18636, 6.613081))
            put("SV", LatLng(13.794185, -88.89653))
            put("SY", LatLng(34.802075, 38.996815))
            put("SZ", LatLng(-26.522503, 31.465866))
            put("TC", LatLng(21.694025, -71.797928))
            put("TD", LatLng(15.454166, 18.732207))
            put("TF", LatLng(-49.280366, 69.348557))
            put("TG", LatLng(8.619543, 0.824782))
            put("TH", LatLng(15.870032, 100.992541))
            put("TJ", LatLng(38.861034, 71.276093))
            put("TK", LatLng(-8.967363, -171.855881))
            put("TL", LatLng(-8.874217, 125.727539))
            put("TM", LatLng(38.969719, 59.556278))
            put("TN", LatLng(33.886917, 9.537499))
            put("TO", LatLng(-21.178986, -175.198242))
            put("TR", LatLng(38.963745, 35.243322))
            put("TT", LatLng(10.691803, -61.222503))
            put("TV", LatLng(-7.109535, 177.64933))
            put("TW", LatLng(23.69781, 120.960515))
            put("TZ", LatLng(-6.369028, 34.888822))
            put("UA", LatLng(48.379433, 31.16558))
            put("UG", LatLng(1.373333, 32.290275))
            put("US", LatLng(37.09024, -95.712891))
            put("UY", LatLng(-32.522779, -55.765835))
            put("UZ", LatLng(41.377491, 64.585262))
            put("VA", LatLng(41.902916, 12.453389))
            put("VC", LatLng(12.984305, -61.287228))
            put("VE", LatLng(6.42375, -66.58973))
            put("VG", LatLng(18.420695, -64.639968))
            put("VI", LatLng(18.335765, -64.896335))
            put("VN", LatLng(14.058324, 108.277199))
            put("VU", LatLng(-15.376706, 166.959158))
            put("WF", LatLng(-13.768752, -177.156097))
            put("WS", LatLng(-13.759029, -172.104629))
            put("XK", LatLng(42.602636, 20.902977))
            put("YE", LatLng(15.552727, 48.516388))
            put("YT", LatLng(-12.8275, 45.166244))
            put("ZA", LatLng(-30.559482, 22.937506))
            put("ZM", LatLng(-13.133897, 27.849332))
            put("ZW", LatLng(-19.015438, 29.154857))
        }
    }

    var latitude = 0.0
    var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {


        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)

        locationBinding = ActivityQrFromLocationBinding.inflate(layoutInflater)
        setContentView(locationBinding.root)

//        if(savedInstanceState==null){
//            Constant.interstitialId?.let {
//                loadInterAd()
//            }
//        }
        sendFirebaseEvents(this,"CreateLocationAct")
        sendAppMetricaEvents("CreateLocationAct")



        val tm = this.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val countryCodeValue = tm.networkCountryIso

        var coordinates = COUNTRY_ISOS[countryCodeValue.uppercase()]
        coordinates?.let {

            latitude = coordinates.lat
            longitude = coordinates.lon
            locationBinding.latitude.setText(latitude.toString())
            locationBinding.longitude.setText(longitude.toString())
            osm(latitude, longitude)

        }

        locationBinding.mapView.setOnTouchListener(object : OnTouchListener {
            private var startClickTime: Long = 0
            private var startX = 0f
            private var startY = 0f
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    startClickTime = System.currentTimeMillis()
                    startX = event.x
                    startY = event.y
                } else if (event.action == MotionEvent.ACTION_UP) {
                    val clickDuration = System.currentTimeMillis() - startClickTime
                    val endX = event.x
                    val endY = event.y
                    val deltaX = Math.abs(endX - startX)
                    val deltaY = Math.abs(endY - startY)
                    if (clickDuration < 200 && deltaX < 50 && deltaY < 50) { // adjust thresholds as needed
                        // Get the coordinates of the touch event
                        val touchedPoint =
                            locationBinding.mapView.getProjection()
                                .fromPixels(event.x.toInt(), event.y.toInt())

                        // Do something with the touched coordinates
                        latitude = touchedPoint.latitude
                        longitude = touchedPoint.longitude
                        locationBinding.latitude.setText(latitude.toString())
                        locationBinding.longitude.setText(longitude.toString())

                        osm(latitude, longitude)
                        return true // consume the event
                    }
                }
                return false // pass the event to other listeners
            }
        })
        locationBinding.mapView.minZoomLevel = 3.0


        locationBinding.currenLocationIcon.setOnClickListener {

            sendFirebaseEvents(this,"CurrentLocationClickCreateLocationAct")
            sendAppMetricaEvents("CurrentLocationClickCreateLocationAct")

            checkLocationPermission()
        }

        locationBinding.moveToPrevLocation.setOnClickListener {
            sendFirebaseEvents(this,"MoveToPreviousLocationClickCreateLocationAct")
            sendAppMetricaEvents("MoveToPreviousLocationClickCreateLocationAct")

            val myLocationOverlay =
                MyLocationNewOverlay(GpsMyLocationProvider(this), locationBinding.mapView)
            locationBinding.mapView.getOverlays().add(myLocationOverlay)
            // val myLocation = myLocationOverlay.myLocation
            val myLocation = GeoPoint(latitude, longitude)
            if (myLocation != null) {
                val mapController: IMapController = locationBinding.mapView.getController()
                mapController.animateTo(myLocation)
            } else {
                Toast.makeText(this, getString(R.string.unableToGetCurrentLocation), Toast.LENGTH_SHORT).show()
            }
            //osm(21.1474074,72.8083000)
        }

        locationBinding.backButton.setOnClickListener {
            onBackPressed()
        }
        locationBinding.tickButton.setOnClickListener {
            sendFirebaseEvents(this,"TickClickCreateLocationAct")
            sendAppMetricaEvents("TickClickCreateLocationAct")


            var newLat = locationBinding.latitude.text.toString().trim()
           var newLon = locationBinding.longitude.text.toString().trim()

            if(newLat==""){

                if(!latInputErrorIconShown){

                    latInputErrorIconShown=true
                    latitudeNoInputPopUp(locationBinding.latitude)
                    locationBinding.latitudeInputErrorIcon.visibility= View.VISIBLE
                }
            }
            if(newLon==""){

                if(!lonInputErrorIconShown){

                    lonInputErrorIconShown=true
                    longitudeNoInputPopUp(locationBinding.longitude)
                    locationBinding.longitudeInputErrorIcon.visibility= View.VISIBLE

                }

            }
            if (newLat !="" && newLon !="") {

                val barcodeValue = "geo:${newLat},${newLon}"

                val intent = Intent(this@QrLocationAct, ViewCodeAct::class.java)
                intent.putExtra("customGenerator", 1)
                intent.putExtra("barcodeValue", barcodeValue)
                intent.putExtra("barcodeType", 10)//8 is for url, barcodeType
                intent.putExtra("barcodeFormat", 256)// 7 is for text type
                startActivity(intent)
            }

        }


        locationBinding.latitude.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(locationBinding.latitude.text.toString().trim()!=""){

                    latInputErrorIconShown=false
                    latPopUpWindow?.dismiss()
                    locationBinding.latitudeInputErrorIcon.visibility= View.GONE

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })


        locationBinding.longitude.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if(locationBinding.longitude.text.toString().trim()!=""){

                    lonInputErrorIconShown=false
                    lonPopUpWindow?.dismiss()
                    locationBinding.longitudeInputErrorIcon.visibility= View.GONE

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
                    AdHandler.getInstance(this@QrLocationAct).dismissProgress(this@QrLocationAct)
                }
            }
        }
    }


    var latPopUpWindow: PopupWindow?=null
    var lonPopUpWindow: PopupWindow?=null

    var latInputErrorIconShown=false
    var lonInputErrorIconShown=false

    fun latitudeNoInputPopUp(refView:View){

        var inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        latPopUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        latPopUpWindow?.showAsDropDown(refView,0,0, Gravity.END)
        latPopUpWindow?.isFocusable=false
    }

    fun longitudeNoInputPopUp(refView:View){

        var inflater= applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view= inflater.inflate(R.layout.item_no_input_popup,null)
        lonPopUpWindow= PopupWindow(view,
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT,true)
        lonPopUpWindow?.showAsDropDown(refView,0,0, Gravity.END)
        lonPopUpWindow?.isFocusable=false
    }




    fun checkLocationPermission() {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            sharedPref.contactPermissionCounter = 0

            //GET LAT AND LON HERE

            val newLongitude = GPSTracker(this).longitude
            val newLatitude = GPSTracker(this).latitude

            if (newLongitude != 0.0 && newLatitude != 0.0) {

                longitude=newLongitude
                latitude=newLatitude

                locationBinding.latitude.setText(latitude.toString())
                locationBinding.latitude.setText(longitude.toString())
                osm(latitude, longitude)
            } else {

                Toast.makeText(
                    this@QrLocationAct,
                    getString(R.string.unableToGetCurrentLocation),
                    Toast.LENGTH_SHORT
                ).show()
            }


        } else {

            requestLocationPermission()
        }

    }

    var RECORD_REQUEST_CODE = 100

    fun requestLocationPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                RECORD_REQUEST_CODE
            )
            sharedPref.locationPermissionCounter = 1
        } else {

            if (sharedPref.locationPermissionCounter == 0) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    RECORD_REQUEST_CODE
                )
            } else {

                showPermissionSettingDialog()

            }


        }
    }


    lateinit var contactPermissionSettingDialog: DialogCameraPermissionSettingBinding
    fun showPermissionSettingDialog() {
        contactPermissionSettingDialog =
            DialogCameraPermissionSettingBinding.inflate(layoutInflater)

        var dialog = MaterialAlertDialogBuilder(this@QrLocationAct, R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(contactPermissionSettingDialog.root)
            .setOnCancelListener(object : DialogInterface.OnCancelListener {
                override fun onCancel(dialog: DialogInterface?) {

                }

            })
            .show()

        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        contactPermissionSettingDialog.cameraPermissionMessage.text =
            "Getting Current location is only possible with permission to access the location"
        contactPermissionSettingDialog.cancel.setOnClickListener {
            dialog.cancel()
        }
        contactPermissionSettingDialog.settings.setOnClickListener {
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            permissionSettingLauncher.launch(intent)
        }


    }

    var permissionSettingLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sharedPref.locationPermissionCounter = 0


                    longitude = GPSTracker(this).longitude
                    latitude = GPSTracker(this).latitude

                    if (longitude != 0.0 && latitude != 0.0) {
                        locationBinding.latitude.setText(latitude.toString())
                        locationBinding.latitude.setText(longitude.toString())
                        osm(latitude, longitude)
                    } else {

                        Toast.makeText(
                            this@QrLocationAct,
                            getString(R.string.unableToGetCurrentLocation),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            if (result.resultCode == Activity.RESULT_CANCELED) {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(
                        this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    sharedPref.locationPermissionCounter = 0

                    longitude = GPSTracker(this).longitude
                    latitude = GPSTracker(this).latitude

                    if (longitude != 0.0 && latitude != 0.0) {
                        locationBinding.latitude.setText(latitude.toString())
                        locationBinding.latitude.setText(longitude.toString())
                        osm(latitude, longitude)
                    } else {
                        Toast.makeText(
                            this@QrLocationAct,
                            getString(R.string.unableToGetCurrentLocation),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == RECORD_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                sharedPref.locationPermissionCounter = 0

//                osm(21.1474074,72.8083000)
                osm(latitude, longitude)

            } else {

                Toast.makeText(applicationContext, getString(R.string.permissionDenied), Toast.LENGTH_SHORT).show()
            }
        }
    }

    var marker: Marker? = null
    fun osm(lat: Double, lon: Double) {
        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        locationBinding.mapView.setTileSource(TileSourceFactory.MAPNIK)

        locationBinding.mapView.setBuiltInZoomControls(true);
        locationBinding.mapView.setMultiTouchControls(true);
        locationBinding.mapView.setTilesScaledToDpi(true);
        locationBinding.mapView.tilesScaleFactor = 0.9F


        val mapController: IMapController = locationBinding.mapView.getController()
        mapController.setZoom(10)

        val startPoint = GeoPoint(lat, lon)
        mapController.setCenter(startPoint)

        marker?.let {
           it.remove(locationBinding.mapView)
        }
        marker = Marker(locationBinding.mapView)
        marker?.position = startPoint

        locationBinding.mapView.getOverlays().add(marker)
        locationBinding.mapView.getController().animateTo(startPoint)

        this.mLocationOverlay =
            MyLocationNewOverlay(GpsMyLocationProvider(this), locationBinding.mapView)
        this.mLocationOverlay?.enableMyLocation()
        locationBinding.mapView.getOverlays().add(this.mLocationOverlay)



        mCompassOverlay =
            CompassOverlay(this, InternalCompassOrientationProvider(this), locationBinding.mapView)

        mCompassOverlay?.enableCompass()
        locationBinding.mapView.overlays.add(mCompassOverlay)



    }


    override fun onResume() {
        super.onResume()
        locationBinding.mapView.onResume() //needed for compass, my location overlays, v6.0.0 and up

    }

    override fun onPause() {
        super.onPause()
        locationBinding.mapView.onPause() //needed for compass, my location overlays, v6.0.0 and up
    }

    override fun onBackPressed() {
        mCompassOverlay?.let {
            if(mCompassOverlay!!.isCompassEnabled)   mCompassOverlay?.disableCompass()
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
       mCompassOverlay?.let {
           if(mCompassOverlay!!.isCompassEnabled)   mCompassOverlay?.disableCompass()
       }
        super.onDestroy()
    }
}



//    class CustomMapTileLabelStyle :
//        MapTileLabelStyle(16, Color.BLACK, Color.WHITE, 2, null) {
//        val textPaint: Paint
//
//        init {
//            textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
//            textPaint.textAlign = Paint.Align.CENTER
//            textPaint.textSize = 24f // Set the font size to 24
//        }
//    }



//
//
//import android.R
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.*
//import android.os.Build
//import android.os.Bundle
//import android.preference.PreferenceManager
//import android.util.Log
//import android.view.MotionEvent
//import android.view.View
//import android.view.View.OnTouchListener
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import scan.reader.qrscanner.barcodescanner.GPSTracker
//import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromLocationBinding
//import org.osmdroid.api.IMapController
//import org.osmdroid.config.Configuration
//import org.osmdroid.tileprovider.tilesource.TileSourceFactory
//import org.osmdroid.util.GeoPoint
//import org.osmdroid.views.overlay.Marker
//import org.osmdroid.views.overlay.compass.CompassOverlay
//import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
//import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
//import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
//
//
//class QrFromLocation : AppCompatActivity() {
//
//    var mLocationOverlay: MyLocationNewOverlay? = null
//    var mCompassOverlay: CompassOverlay? = null
//
//    lateinit var locationBinding: ActivityQrFromLocationBinding
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        locationBinding= ActivityQrFromLocationBinding.inflate(layoutInflater)
//        setContentView(locationBinding.root)
//
//        checkpermission()
//
//
//        locationBinding.mapView.setOnTouchListener(object : OnTouchListener {
//            private var startClickTime: Long = 0
//            private var startX = 0f
//            private var startY = 0f
//            override fun onTouch(v: View, event: MotionEvent): Boolean {
//                if (event.action == MotionEvent.ACTION_DOWN) {
//                    startClickTime = System.currentTimeMillis()
//                    startX = event.x
//                    startY = event.y
//                } else if (event.action == MotionEvent.ACTION_UP) {
//                    val clickDuration = System.currentTimeMillis() - startClickTime
//                    val endX = event.x
//                    val endY = event.y
//                    val deltaX = Math.abs(endX - startX)
//                    val deltaY = Math.abs(endY - startY)
//                    if (clickDuration < 200 && deltaX < 50 && deltaY < 50) { // adjust thresholds as needed
//                        // Get the coordinates of the touch event
//                        val touchedPoint =
//                            locationBinding.mapView.getProjection().fromPixels(event.x.toInt(), event.y.toInt())
//
//                        // Do something with the touched coordinates
//                        Toast.makeText(
//                            this@QrFromLocation,
//                            "Touched location: " + touchedPoint.latitude + ", " + touchedPoint.longitude,
//                            Toast.LENGTH_SHORT
//                        ).show()
//                        return true // consume the event
//                    }
//                }
//                return false // pass the event to other listeners
//            }
//        })
//
//
//        locationBinding.mapView.minZoomLevel= 3.0
//
//
//        locationBinding.starttingPosition.setOnClickListener{
//            val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), locationBinding.mapView)
//            locationBinding.mapView.getOverlays().add(myLocationOverlay)
//           // val myLocation = myLocationOverlay.myLocation
//            val myLocation =GeoPoint(21.1474074,72.8082997)
//            if (myLocation != null) {
//                val mapController: IMapController = locationBinding.mapView.getController()
//                mapController.animateTo(myLocation)
//            } else {
//                Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show()
//            }
//            //osm(21.1474074,72.8083000)
//        }
//
//    }
//    fun checkpermission()
//    {
//        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)//API33
//        {
//            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_VIDEO)==
//                PackageManager.PERMISSION_GRANTED)
//            {
//                osm(21.1474074,72.8083000)
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_MEDIA_VIDEO),100)
//            }
//        }
//        else{
//            if(ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)==
//                PackageManager.PERMISSION_GRANTED
//                &&
//                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)==
//                PackageManager.PERMISSION_GRANTED
//                &&
//                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)==
//                PackageManager.PERMISSION_GRANTED)
//            {
//                osm(21.1474074,72.8083000)
//
//            }
//            else
//            {
//                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                    android.Manifest.permission.ACCESS_FINE_LOCATION
//                ),100)
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int,
//                                            permissions: Array<String>,
//                                            grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        if (requestCode == 100) {
//            if (grantResults.size > 1) {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
//                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
//                    grantResults[2] == PackageManager.PERMISSION_GRANTED
//                ) {
//                    osm(21.1474074, 72.8083000)
//
//
//                } else {
//                    Toast.makeText(applicationContext, "Permissino denied", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            } else {
//                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                } else {
//                    Toast.makeText(applicationContext, "Permissino denied", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            }
//        }
//    }
//
////    fun getCurrentLocation()
////    {
////        val gpsTracker = GPSTracker(this)
////
////        if (gpsTracker.isGPSTrackingEnabled) {
////            val stringLatitude = gpsTracker.latitude.toString()
////            Log.d("GpsDetail",stringLatitude)
////            val stringLongitude = gpsTracker.longitude.toString()
////            Log.d("GpsDetail",stringLongitude)
////
////            gpsTracker.getl
////
////
////        } else {
////            // can't get location
////            // GPS or Network is not enabled
////            // Ask user to enable GPS/network in settings
////            gpsTracker.showSettingsAlert()
////        }
////    }
//    fun osm(lat:Double,lon:Double)
//    {
//
//
//
//        val ctx: Context = applicationContext
//        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
//
//        locationBinding.mapView.setTileSource(TileSourceFactory.MAPNIK)
//
//        locationBinding.mapView.setBuiltInZoomControls(true);
//        locationBinding.mapView.setMultiTouchControls(true);
//      locationBinding.mapView.setTilesScaledToDpi(true);
//      locationBinding.mapView.tilesScaleFactor=0.9F
//
//
//        val mapController: IMapController = locationBinding.mapView.getController()
//        mapController.setZoom(10)
//
//        val startPoint = GeoPoint(lat, lon)
//        mapController.setCenter(startPoint)
//        val marker = Marker(locationBinding.mapView)
//        marker.setPosition(startPoint)
//        locationBinding.mapView.getOverlays().add(marker)
//        locationBinding.mapView.getController().animateTo(startPoint)
//
//        this.mLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), locationBinding.mapView)
//        this.mLocationOverlay!!.enableMyLocation()
//        locationBinding.mapView.getOverlays().add(this.mLocationOverlay)
//
//        mCompassOverlay = CompassOverlay(this, InternalCompassOrientationProvider(this), locationBinding.mapView)
//
//        mCompassOverlay!!.enableCompass()
//        locationBinding.mapView.getOverlays().add(mCompassOverlay)
//
//
//
//
//    }
//    override fun onResume() {
//        super.onResume()
//        //this will refresh the osmdroid configuration on resuming.
//        //if you make changes to the configuration, use
//        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
//        locationBinding.mapView.onResume() //needed for compass, my location overlays, v6.0.0 and up
//    }
//
//    override fun onPause() {
//        super.onPause()
//        //this will refresh the osmdroid configuration on resuming.
//        //if you make changes to the configuration, use
//        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//        //Configuration.getInstance().save(this, prefs);
//        locationBinding.mapView.onPause() //needed for compass, my location overlays, v6.0.0 and up
//    }
//
//
////    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
////        // Inflate the menu; this adds items to the action bar if it is present.
////        menuInflater.inflate(R.menu.varna_lab_geo_locations, menu)
////        return true
////    }
//}