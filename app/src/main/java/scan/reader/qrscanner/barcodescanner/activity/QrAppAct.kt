package scan.reader.qrscanner.barcodescanner.activity

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import scan.reader.qrscanner.barcodescanner.adapter.InstalledAppsAdapter
import scan.reader.qrscanner.barcodescanner.viewModel.InstalledAppDataModelClass
import scan.reader.qrscanner.barcodescanner.databinding.ActivityQrFromAppBinding
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import com.github.ybq.android.spinkit.style.Circle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents


class QrAppAct : AppCompatActivity() {

    lateinit var appBinding:ActivityQrFromAppBinding

    override fun onCreate(savedInstanceState: Bundle?) {


        setLocale(sharedPref.languageCode)

        super.onCreate(savedInstanceState)

        appBinding= ActivityQrFromAppBinding.inflate(layoutInflater)
        setContentView(appBinding.root)

        sendFirebaseEvents(this,"AppActivityInsideMoreQrCodes")
        sendAppMetricaEvents("AppActivityInsideMoreQrCodes")

        appBinding.backButton.setOnClickListener{
            onBackPressed()
        }
        appBinding.spinKit.visibility=View.VISIBLE
        val loadingType=Circle()
        appBinding.spinKit.setIndeterminateDrawable(loadingType)

       CoroutineScope(Dispatchers.IO).launch {
           getInstalledApps()
       }

    }

    var installedAppList=ArrayList<InstalledAppDataModelClass>()

    fun getInstalledApps()
    {
        installedAppList.clear()
        val pm = packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        for (packageInfo in packages) {
            if (packageInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val appName = packageInfo.loadLabel(pm).toString()
                val packageName = packageInfo.packageName
                val appIcon = packageInfo.loadIcon(pm)

                installedAppList.add(InstalledAppDataModelClass(appName,packageName,appIcon))


            }
        }
        if(installedAppList.size>0)
        {
            runOnUiThread{
                appBinding.spinKit.visibility=View.GONE
                settingAdapter(installedAppList)
            }
        }
    }

    lateinit var adapter: InstalledAppsAdapter
    fun settingAdapter(appList:ArrayList<InstalledAppDataModelClass>)
    {

        adapter= InstalledAppsAdapter(this,appList){
            holderPosition ->

             var app=installedAppList.get(holderPosition)
            genrateQrFromData(app)
        }

        appBinding.recyler.layoutManager=
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        appBinding.recyler.adapter=adapter
    }

    fun genrateQrFromData(app: InstalledAppDataModelClass) {

        val playStoreLink = "https://play.google.com/store/apps/details?id=${app.packageName}"
        val intent= Intent(this@QrAppAct, ViewCodeAct::class.java)
        intent.putExtra("customGenerator",1)
        intent.putExtra("barcodeValue", playStoreLink)
        intent.putExtra("installedAppName", app.appName)
        intent.putExtra("barcodeType", 8)//8 is for url, barcodeType
        intent.putExtra("barcodeFormat",256)// 7 is for text type
        startActivity(intent)

    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

}