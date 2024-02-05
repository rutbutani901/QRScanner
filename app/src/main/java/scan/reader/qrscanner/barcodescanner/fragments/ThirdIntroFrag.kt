package scan.reader.qrscanner.barcodescanner.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import scan.reader.qrscanner.barcodescanner.R
import scan.reader.qrscanner.barcodescanner.activity.PurchaseAct
import scan.reader.qrscanner.barcodescanner.databinding.FragmentThirdIntroBinding
import scan.reader.qrscanner.barcodescanner.extensions.sendAppMetricaEvents
import scan.reader.qrscanner.barcodescanner.extensions.sendFirebaseEvents
import scan.reader.qrscanner.barcodescanner.extensions.sharedPref
import scan.reader.qrscanner.barcodescanner.util.Constant

class ThirdIntroFrag : Fragment() {

    lateinit var binding:FragmentThirdIntroBinding

    lateinit var fragmentActivity: FragmentActivity


    public interface FinishInterface{
        fun canFinishAct(canFinish:Boolean)
    }

    lateinit var finishInterface: FinishInterface

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentActivity=context as FragmentActivity

        finishInterface= activity as FinishInterface
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(activity!=null) fragmentActivity=activity as FragmentActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentThirdIntroBinding.inflate(layoutInflater)


        binding.includeLayout.root.findViewById<View>(R.id.mainDialog).setOnClickListener{

            fragmentActivity.sendFirebaseEvents(fragmentActivity,"ProFeaturesDialogClickInAppIntroScreen")
            fragmentActivity.sendAppMetricaEvents("ProFeaturesDialogClickInAppIntroScreen")

            if(fragmentActivity.sharedPref.isPremiumPurchased){

                Toast.makeText(
                    fragmentActivity,
                    getString(R.string.purchaseAlreadyDone),
                    Toast.LENGTH_SHORT
                ).show()

            }else{

                var intent= Intent(fragmentActivity,
                    PurchaseAct::class.java)
                startForResult.launch(intent)
            }
//            finishInterface.canFinishAct(true)
        }
        return binding.root
    }
    val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {

            Constant.nativeId=null
            Constant.interstitialId=null
            Constant.appOpenId=null
            fragmentActivity.sharedPref.isPremiumPurchased=true

        }
        finishInterface.canFinishAct(true)
    }

}