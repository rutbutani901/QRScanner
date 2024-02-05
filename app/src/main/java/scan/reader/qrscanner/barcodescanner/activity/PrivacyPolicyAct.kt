package scan.reader.qrscanner.barcodescanner.activity

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.webkit.*
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.databinding.ActivityPrivacyPolicyBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.setLocale
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref

class PrivacyPolicyAct : AppCompatActivity() {

    lateinit var binding:ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        setLocale(sharedPref.languageCode)
        super.onCreate(savedInstanceState)
        binding= ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sendFirebaseEvents(this,"PrivacyPolicyAct")
        sendAppMetricaEvents("PrivacyPolicyAct")


        loadWebPage(getString(R.string.privacyPolicyLink))

        binding.backButton.setOnClickListener{
            onBackPressed()

        }

    }

    fun loadWebPage(url:String){
        binding.webView.webViewClient=MyWebViewClient()
        binding.webView.settings.allowContentAccess=true
        binding.webView.settings.useWideViewPort=true
        binding.webView.settings.domStorageEnabled=true
        binding.webView.settings.loadWithOverviewMode=true
        binding.webView.settings.allowFileAccess=true
        binding.webView.settings.javaScriptEnabled=true
        binding.webView.setLayerType(View.LAYER_TYPE_SOFTWARE,null)
        binding.webView.loadUrl(url)

        binding.webView.webChromeClient=object :WebChromeClient(){
            override fun onGeolocationPermissionsShowPrompt(
                origin: String,
                callback: GeolocationPermissions.Callback
            ) {
                callback.invoke(origin,true,false)
            }
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                binding.progress.progress=newProgress
            }
        }
    }

    inner class MyWebViewClient:WebViewClient(){
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            binding.progress.visibility=View.VISIBLE
        }

        override fun onPageCommitVisible(view: WebView, url: String?) {
            super.onPageCommitVisible(view, url)
        }

        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            binding.progress.visibility=View.GONE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}