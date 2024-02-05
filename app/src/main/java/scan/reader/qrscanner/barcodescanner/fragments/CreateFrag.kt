package scan.reader.qrscanner.barcodescanner.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.activity.*
import scan.reader.qrscanner.barcodescanner.databinding.FragmentCreateBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents


//import com.google.android.gms.maps.model.LatLng

class CreateFrag : Fragment() {

    lateinit var createFragmentBinding:FragmentCreateBinding

    lateinit var fragmentActivity: FragmentActivity

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
        createFragmentBinding= FragmentCreateBinding.inflate(layoutInflater)

        createFragmentBinding.contentClipboard.setOnClickListener{

            try {
                val clipboard: ClipboardManager? = fragmentActivity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager?
                clipboard?.let {
                    if (clipboard.hasPrimaryClip()) {
                        val clipData: ClipData = clipboard.primaryClip!!
                        if (clipData.itemCount > 0) {
                            val item = clipData.getItemAt(0)
                            val clipBoardData:CharSequence?= item.text

                            clipBoardData?.let {
                                if(clipBoardData.isNotEmpty()){

                                    val intent= Intent(fragmentActivity, ViewCodeAct::class.java)
                                    intent.putExtra("customGenerator",1)
                                    intent.putExtra("barcodeValue", clipBoardData)
                                    intent.putExtra("barcodeType", 7)//8 is for url, barcodeType
                                    intent.putExtra("barcodeFormat",256)// 7 is for text type
                                    startActivity(intent)
                                }else
                                {
                                    Toast.makeText(fragmentActivity,getString(R.string.noClipboardDataFound),Toast.LENGTH_SHORT).show()
                                }
                            }



                        }
                    }
                }
            }catch (e:Exception){
                e.printStackTrace()
            }


        }

        createFragmentBinding.useShare.setOnClickListener{
            startActivity(Intent(fragmentActivity, ShareFromOtherAppAct::class.java))
        }
        createFragmentBinding.website.setOnClickListener{
            startActivity(Intent(fragmentActivity, QrWebsiteAct::class.java))
        }
        createFragmentBinding.contact.setOnClickListener{
            startActivity(Intent(fragmentActivity, QrContactAct::class.java))
        }
        createFragmentBinding.wifi.setOnClickListener{
            startActivity(Intent(fragmentActivity, QrWifiAct::class.java))
        }
        createFragmentBinding.location.setOnClickListener{

            startActivity(Intent(fragmentActivity, QrLocationAct::class.java))
        }
        createFragmentBinding.event.setOnClickListener{

            startActivity(Intent(fragmentActivity, QrEventAct::class.java))
        }


        createFragmentBinding.moreQRcodes.setOnClickListener{

            startActivity(Intent(fragmentActivity, MoreQrCodesAct::class.java))
        }


        createFragmentBinding.barcodesAndOther.setOnClickListener{
           var intent= Intent(fragmentActivity, Other2dCodesAct::class.java)

            startActivity(intent)
        }

        return createFragmentBinding.root
    }


    override fun onResume() {
        fragmentActivity.sendFirebaseEvents(fragmentActivity,"OnResumeCreateFragment")
        fragmentActivity.sendAppMetricaEvents("OnResumeCreateFragment")

        super.onResume()
    }

}