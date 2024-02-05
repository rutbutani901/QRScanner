package scan.reader.qrscanner.barcodescanner.fragments

import android.Manifest
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.format.DateFormat
import android.util.Log
import android.util.Size
import android.view.*
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.activity.BarcodeAnalyzer
import scan.reader.qrscanner.barcodescanner.activity.ScanResultAct
import scan.reader.qrscanner.barcodescanner.dataBase.DataModel
import scan.reader.qrscanner.barcodescanner.activity.HelpAct
import scan.reader.qrscanner.barcodescanner.util.MySharedPrefrence
import scan.reader.qrscanner.barcodescanner.viewModel.BarcodeListViewModel
import scan.reader.qrscanner.barcodescanner.databinding.DialogCameraPermissionSettingBinding
import scan.reader.qrscanner.barcodescanner.databinding.FragmentScanBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.util.Constant
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean


class ScanFrag : Fragment() {

    lateinit var barcodeListViewModel: BarcodeListViewModel
    private var processingBarcode = AtomicBoolean(false)
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var preview: Preview
    private lateinit var imageAnalysis: ImageAnalysis

    // Select back camera
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private var flashOn = false
    var cam: Camera?=null
    var cameraZoomFactor:Float=0.0f

    lateinit var scanFragmentBinding:FragmentScanBinding
    lateinit var fragmentActivity:FragmentActivity
    private var vibrator: Vibrator? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentActivity=context as FragmentActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(activity!=null) fragmentActivity=activity as FragmentActivity


    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        scanFragmentBinding= FragmentScanBinding.inflate(layoutInflater)
        val view=scanFragmentBinding.root

        barcodeListViewModel = ViewModelProvider(this)[BarcodeListViewModel::class.java]
       vibrator= fragmentActivity.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator?
        cameraExecutor = Executors.newSingleThreadExecutor()

        if(ActivityCompat.checkSelfPermission(fragmentActivity, android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED) {
                MySharedPrefrence.newInstance(fragmentActivity).cameraPermissionCounter = 0
                initializeCamera()
        }else{
            scanFragmentBinding.scanUsingCamera.visibility=View.VISIBLE
            scanFragmentBinding.mainScanLayout.visibility=View.GONE
        }
        if(MySharedPrefrence.newInstance(fragmentActivity).cameraOrientationBack) cameraSelector=CameraSelector.DEFAULT_BACK_CAMERA
        else cameraSelector=CameraSelector.DEFAULT_FRONT_CAMERA


        scanFragmentBinding.scanUsingCamera.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"ScanUsingCameraInScanFragmentAtFirstTimeInstall")
            fragmentActivity.sendAppMetricaEvents("ScanUsingCameraInScanFragmentAtFirstTimeInstall")

            checkCameraPermission()
        }
        scanFragmentBinding.scanUsingImage.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"ScanUsingImageInScanFragmentAtFirstTimeInstall")
            fragmentActivity.sendAppMetricaEvents("ScanUsingImageInScanFragmentAtFirstTimeInstall")

            checkReadExternalPermission()
        }
        scanFragmentBinding.scanImage.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"ScanImageClickInScanFragment")
            fragmentActivity.sendAppMetricaEvents("ScanImageClickInScanFragment")
            Log.d("ScannClicked","yes")
            checkReadExternalPermission()
        }
        scanFragmentBinding.torch.setOnClickListener {
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"TorchClickInScanFragment")
            fragmentActivity.sendAppMetricaEvents("TorchClickInScanFragment")
            flash()
        }
        scanFragmentBinding.help.setOnClickListener{
            fragmentActivity.sendFirebaseEvents(fragmentActivity,"HelpClickInScanFragment")
            fragmentActivity.sendAppMetricaEvents("HelpClickInScanFragment")
            startActivity(Intent(fragmentActivity, HelpAct::class.java))
        }
        scanFragmentBinding.seekbar.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    val zoom=(progress/40.0).toFloat()
                    cam?.cameraControl?.setLinearZoom(zoom.toFloat())
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            });
        scanFragmentBinding.zoomOutIcon.setOnClickListener{
            cam?.cameraControl?.setLinearZoom(0.0F)
            scanFragmentBinding.seekbar.progress=0
        }
        scanFragmentBinding.zoomInIcon.setOnClickListener{
            cam?.let {

                cam!!.cameraControl.setLinearZoom(1.0F)
            }
            scanFragmentBinding.seekbar.progress=40
        }

        //tap focus
        scanFragmentBinding.previewView.afterMeasured {
            scanFragmentBinding.previewView.setOnTouchListener { _, event ->
                return@setOnTouchListener when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        true
                    }
                    MotionEvent.ACTION_UP -> {
                        val factory: MeteringPointFactory = SurfaceOrientedMeteringPointFactory(
                            scanFragmentBinding.previewView.width.toFloat(), scanFragmentBinding.previewView.height.toFloat()
                        )
                        val autoFocusPoint = factory.createPoint(event.x, event.y)
                        try {
                            cam?.cameraControl?.startFocusAndMetering(
                                FocusMeteringAction.Builder(
                                    autoFocusPoint,
                                    FocusMeteringAction.FLAG_AF
                                ).apply {
                                    //focus only when the user tap the preview
                                    disableAutoCancel()
                                }.build()
                            )
                        } catch (e: CameraInfoUnavailableException) {
                            Log.d("ERROR", "cannot access camera", e)
                        }
                        true
                    }
                    else -> false // Unhandled event.
                }
            }
        }
        return view
    }



    fun checkReadExternalPermission(){

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU){
            if(ActivityCompat.checkSelfPermission(fragmentActivity, android.Manifest.permission.READ_MEDIA_IMAGES)==
                PackageManager.PERMISSION_GRANTED) {

                MySharedPrefrence.newInstance(fragmentActivity).readExternalStorageCounter=0
                openPhotoChooseIntent()
            }
            else {
                requestCameraPermmission(Manifest.permission.READ_MEDIA_IMAGES,false)
            }
        }
        else{
            if(ActivityCompat.checkSelfPermission(fragmentActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)==
                PackageManager.PERMISSION_GRANTED)
            {
                MySharedPrefrence.newInstance(fragmentActivity).readExternalStorageCounter=0
                openPhotoChooseIntent()

            }
            else {
                requestCameraPermmission(Manifest.permission.READ_EXTERNAL_STORAGE,false)
            }
        }
    }
    private fun readBarcodeImageFromBitmap(myBitmap:Bitmap){

        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_ALL_FORMATS)
            .build()

        val image = InputImage.fromBitmap(myBitmap,0)
        val scanner = BarcodeScanning.getClient(options)
        scanner.process(image)
            .addOnSuccessListener { barcodes ->
               if(barcodes.size==0){
                   Toast.makeText(fragmentActivity,getString(R.string.noBarcodesFound),Toast.LENGTH_SHORT).show()
               }else{
                   val barcodeValue=barcodes[0].rawValue
                   val barcodeFormat=barcodes[0].format
                   val barcodeValueType=barcodes[0].valueType

                   val intent = Intent(this.activity, ScanResultAct::class.java)
                   intent.putExtra("barcodeValue", barcodeValue)
                   intent.putExtra("barcodeFormat", barcodeFormat)
                   intent.putExtra("barcodeValueType", barcodeValueType)
                   startActivity(intent)
               }
//                for (barcode in barcodes) {
//
//                    val barcodeValue=barcode.rawValue
//                    val barcodeFormat=barcode.format
//                   val barcodeValueType=barcode.valueType
//
//                    val intent = Intent(this.activity, ScanResult::class.java)
//                    intent.putExtra("barcodeValue", barcodeValue)
//                    intent.putExtra("barcodeFormat", barcodeFormat)
//                    intent.putExtra("barcodeValueType", barcodeValueType)
//                    startActivity(intent)
//                }
            }
            .addOnFailureListener {
                Toast.makeText(fragmentActivity,"Failed",Toast.LENGTH_SHORT).show()
            }

    }
    private fun openPhotoChooseIntent() {
        Constant.goingOutFromApp=true
        val getInt = Intent(Intent.ACTION_GET_CONTENT)
        getInt.type = "image/*"
        val pickInt = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickInt.type = "image/*"
        val chooserInt = Intent.createChooser(getInt, "Select Image")
        chooserInt.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(pickInt))
        filePicherResultLauncher.launch(chooserInt)
    }
    private var filePicherResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

        Constant.goingOutFromApp=false
            if (result.resultCode == Activity.RESULT_OK) {

                Glide.with(this).asBitmap().load(result.data?.data)
                    .apply(RequestOptions.skipMemoryCacheOf(true))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.NONE))
                    .into(object : CustomTarget<Bitmap?>() {
                        override fun onResourceReady(
                            bitmap: Bitmap,
                            transition: Transition<in Bitmap?>?,
                        ) {
                            readBarcodeImageFromBitmap(bitmap)

                        }

                        override fun onLoadCleared(placeholder: Drawable?) {}
                    })
            }
        }

    private fun checkCameraPermission()
    {
        if(ActivityCompat.checkSelfPermission(fragmentActivity, android.Manifest.permission.CAMERA)==
            PackageManager.PERMISSION_GRANTED) {


            initializeCamera()
        }
        else {

            requestCameraPermmission(Manifest.permission.CAMERA,true)

        }
    }
    var permissionLauncher=registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->

        if (isGranted) {
            MySharedPrefrence.newInstance(fragmentActivity).cameraPermissionCounter=0
            initializeCamera()
        } else {


        }
    }

    private fun requestCameraPermmission(permission:String,isCameraPermission:Boolean) {

        Dexter.withContext(fragmentActivity)
            .withPermissions(
                permission
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(multiplePermissionsReport: MultiplePermissionsReport) {

                    if (multiplePermissionsReport.areAllPermissionsGranted()) {
                        if(isCameraPermission){

                            initializeCamera()
                        }else{
                            openPhotoChooseIntent()
                        }
                    }else if (multiplePermissionsReport.isAnyPermissionPermanentlyDenied) {

                        if(isCameraPermission){

                            showCameraSettingDialog()
                        }else{
                           //ask read imagefile
                            showImageSettingDialog()
                        }

                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    list: List<PermissionRequest?>?,
                    permissionToken: PermissionToken
                ) {

                    permissionToken.continuePermissionRequest()
                }
            }).withErrorListener { error: DexterError? ->

            }
            .onSameThread().check()
    }
    lateinit var cameraSettingDialog:DialogCameraPermissionSettingBinding
    fun showCameraSettingDialog()
    {
        cameraSettingDialog= DialogCameraPermissionSettingBinding.inflate(layoutInflater)
        var dialog=MaterialAlertDialogBuilder(fragmentActivity,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(cameraSettingDialog.root)
            .setOnCancelListener(object :DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {
                }

            })
            .show()

        cameraSettingDialog.cameraPermissionMessage.text=getString(R.string.cameraPermissionSettingText)
        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        cameraSettingDialog.cancel.setOnClickListener{
            dialog.cancel()
        }
        cameraSettingDialog.settings.setOnClickListener{
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package",fragmentActivity.packageName, null)
            intent.data = uri
            cameraPermissionResultLauncher.launch(intent)
        }
    }


    fun showImageSettingDialog()
    {
        cameraSettingDialog= DialogCameraPermissionSettingBinding.inflate(layoutInflater)
        var dialog=MaterialAlertDialogBuilder(fragmentActivity,R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(cameraSettingDialog.root)
            .setOnCancelListener(object :DialogInterface.OnCancelListener{
                override fun onCancel(dialog: DialogInterface?) {


                }
            })
            .show()

        cameraSettingDialog.cameraPermissionMessage.text=getString(R.string.imagePermissionSettingText)
        dialog.window?.setBackgroundDrawable(resources.getDrawable(R.drawable.rounded_corners))
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)

        cameraSettingDialog.cancel.setOnClickListener{
            dialog.cancel()
        }
        cameraSettingDialog.settings.setOnClickListener{
            dialog.cancel()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri: Uri = Uri.fromParts("package",fragmentActivity.packageName, null)
            intent.data = uri
            imagePermissionResultLauncher.launch(intent)
        }
    }
    var cameraPermissionResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->

        if(ActivityCompat.checkSelfPermission(fragmentActivity, android.Manifest.permission.CAMERA)== PackageManager.PERMISSION_GRANTED){
                MySharedPrefrence.newInstance(fragmentActivity).cameraPermissionCounter=0
                initializeCamera()
        }

    }
    var imagePermissionResultLauncher=registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        result->

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.TIRAMISU)//API33
        {
            if(ActivityCompat.checkSelfPermission(fragmentActivity, android.Manifest.permission.READ_MEDIA_IMAGES)==
                PackageManager.PERMISSION_GRANTED) {

                openPhotoChooseIntent()
            }

        }
        else{
            if(ActivityCompat.checkSelfPermission(fragmentActivity, android.Manifest.permission.READ_EXTERNAL_STORAGE)==
                PackageManager.PERMISSION_GRANTED) {

                openPhotoChooseIntent()
            }
        }
    }

    fun initializeCamera()
    {
        scanFragmentBinding.scanUsingCamera.visibility=View.GONE
        scanFragmentBinding.mainScanLayout.visibility=View.VISIBLE
        startCamera()

    }

    private fun startCamera() {
        try {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(fragmentActivity)

            cameraProviderFuture.addListener(Runnable{

                cameraProvider = cameraProviderFuture.get()

                preview = Preview.Builder().build()
                scanFragmentBinding.previewView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                preview.setSurfaceProvider(scanFragmentBinding.previewView.surfaceProvider)


                // Setup the ImageAnalyzer for the ImageAnalysis use case
                val builder = ImageAnalysis.Builder().setTargetResolution(Size(1080, 1920))
                //val builder = ImageAnalysis.Builder().setTargetResolution(Size(120, 500))


                imageAnalysis = builder.build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcodeValue, barcodeFormat,barcodeValueType ,bitmape->//
                            if (processingBarcode.compareAndSet(false, true)) {

                                if(MySharedPrefrence.newInstance(fragmentActivity).playSound){
                                    beep()
                                }
                                if(MySharedPrefrence.newInstance(fragmentActivity).vibrateOnScan){
                                   vibrate()
                                }

                                if(MySharedPrefrence.newInstance(fragmentActivity).confirmScanManually){

                                    scanFragmentBinding.manualScan.visibility=View.VISIBLE
                                    scanFragmentBinding.manualScan.setOnClickListener{

                                        fragmentActivity.sendFirebaseEvents(fragmentActivity,"ManualScanClickInScanFragment")
                                        fragmentActivity.sendAppMetricaEvents("ManualScanClickInScanFragment")


                                        val scanContinous= MySharedPrefrence.newInstance(fragmentActivity).isContinousScanningEnabled
                                        if(scanContinous){

                                            val creationDate=curentDataTime()
                                            val dataModel= DataModel(
                                                barcodeValue,
                                                barcodeFormat.toInt(),
                                                creationDate,
                                                "",
                                                false,
                                                barcodeValueType)

                                            // barcodeListViewModel.insert(fragmentActivity,dataModel)
                                            var calledOneTime=false
                                            if(!MySharedPrefrence.newInstance(fragmentActivity).isDuplicateBarcodeEnable){

                                                CoroutineScope(Dispatchers.IO).launch {
                                                    if(!barcodeListViewModel.newdoesBarcodesExist(fragmentActivity,barcodeValue)){
                                                        if(!calledOneTime){

                                                            barcodeListViewModel.insert(fragmentActivity,dataModel)
                                                            calledOneTime=true
                                                        }
                                                    }
                                                }
                                            }else{
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    barcodeListViewModel.insert(fragmentActivity,dataModel)
                                                }

                                            }
                                            processingBarcode.set(false)

                                        }
                                        else{
                                            val intent = Intent(this.activity, ScanResultAct::class.java)
                                            intent.putExtra("barcodeValue", barcodeValue)
                                            intent.putExtra("barcodeFormat", barcodeFormat.toInt())
                                            intent.putExtra("barcodeValueType", barcodeValueType)
                                            startActivity(intent)
                                        }
                                    }

                                }
                                else{
                                    scanFragmentBinding.manualScan.visibility=View.GONE
//
                                    val scanContinous= MySharedPrefrence.newInstance(fragmentActivity).isContinousScanningEnabled
                                    if(scanContinous){

                                        val creationDate=curentDataTime()
                                        val dataModel= DataModel(
                                            barcodeValue,
                                            barcodeFormat.toInt(),
                                            creationDate,
                                            "",
                                            false,
                                            barcodeValueType)

                                        // barcodeListViewModel.insert(fragmentActivity,dataModel)
                                        var calledOneTime=false
                                        if(!MySharedPrefrence.newInstance(fragmentActivity).isDuplicateBarcodeEnable){

                                            CoroutineScope(Dispatchers.IO).launch {
                                                if(!barcodeListViewModel.newdoesBarcodesExist(fragmentActivity,barcodeValue)){
                                                    if(!calledOneTime){

                                                        barcodeListViewModel.insert(fragmentActivity,dataModel)
                                                        calledOneTime=true
                                                    }
                                                }
                                            }
                                        }else{
                                            CoroutineScope(Dispatchers.IO).launch {
                                                barcodeListViewModel.insert(fragmentActivity,dataModel)
                                            }

                                        }
                                        processingBarcode.set(false)

                                    }
                                    else{
                                        val intent = Intent(this.activity, ScanResultAct::class.java)
                                        intent.putExtra("barcodeValue", barcodeValue)
                                        intent.putExtra("barcodeFormat", barcodeFormat.toInt())
                                        intent.putExtra("barcodeValueType", barcodeValueType)
                                        startActivity(intent)
                                    }
                                }


                            }
                        })
                    }
                try {
                    cameraProvider.unbindAll()
                    cam = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalysis)
                      //cam.cameraControl.startFocusAndMetering()
                    autoFocus()

                    cam?.let {
                        if (cam!!.cameraInfo.hasFlashUnit()) {
                            cam!!.cameraControl.enableTorch(flashOn)
                            cam!!.cameraControl.setLinearZoom(cameraZoomFactor)
                            scanFragmentBinding.torch.visibility = View.VISIBLE
                        }
                    }

                } catch (e: Exception) {
                    Log.e("PreviewUseCase", "Binding failed! :(", e)
                }
            }, ContextCompat.getMainExecutor(fragmentActivity))

        } catch (e: Exception) {
            e.printStackTrace()
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
        var time= DateFormat.format(delegate, Calendar.getInstance().time)// 11:17 pm
        dateTime += time

        return dateTime
    }

    fun autoFocus()
    {
        scanFragmentBinding.previewView.afterMeasured {
            val autoFocusPoint = SurfaceOrientedMeteringPointFactory(1f, 1f)
                .createPoint(.5f, .5f)
            try {
                val autoFocusAction = FocusMeteringAction.Builder(
                    autoFocusPoint,
                    FocusMeteringAction.FLAG_AF
                ).apply {
                    //start auto-focusing after 2 seconds
                    setAutoCancelDuration(2, TimeUnit.SECONDS)
                }.build()

                    cam?.cameraControl?.startFocusAndMetering(autoFocusAction)

            } catch (e: CameraInfoUnavailableException) {
                Log.d("ERROR", "cannot access camera", e)
            }

        }
    }
    inline fun View.afterMeasured(crossinline block: () -> Unit) {
        if (measuredWidth > 0 && measuredHeight > 0) {
            block()
        } else {
            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener{
                override fun onGlobalLayout() {
                    if (measuredWidth > 0 && measuredHeight > 0) {
                        viewTreeObserver.removeOnGlobalLayoutListener(this)
                        block()
                    }
                }

            })
        }
    }

    private fun flash() {
        flashOn = !flashOn
        // Change icon
//        val id = if (flashOn) R.drawable.ic_flash_off else R.drawable.ic_flash_on
//        binding.btnFlash.setImageDrawable(ContextCompat.getDrawable(this, id))
        try {
            // Bind use cases to lifecycleOwner
            val cam = cameraProvider.bindToLifecycle(viewLifecycleOwner, cameraSelector, preview, imageAnalysis)
            cam.cameraControl.enableTorch(flashOn)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    private fun beep() {
        val toneGen = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200)
    }
    private fun vibrate() {
        vibrator?.vibrate(100);
    }




    override fun onResume() {
        fragmentActivity.sendFirebaseEvents(fragmentActivity,"OnResumeScanFragment")
        fragmentActivity.sendAppMetricaEvents("OnResumeScanFragment")


        super.onResume()
        processingBarcode.set(false)
        scanFragmentBinding.manualScan.visibility=View.GONE
        if(MySharedPrefrence.newInstance(fragmentActivity).cameraOrientationBack) cameraSelector=CameraSelector.DEFAULT_BACK_CAMERA
        else cameraSelector=CameraSelector.DEFAULT_FRONT_CAMERA
        startCamera()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        scanFragmentBinding.manualScan.visibility=View.GONE
       vibrator?.cancel();
    }


}