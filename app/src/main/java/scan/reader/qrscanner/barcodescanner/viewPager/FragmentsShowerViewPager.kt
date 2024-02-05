package scan.reader.qrscanner.barcodescanner.viewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentsShowerViewPager(fragmentActivity: FragmentActivity, val fragList:ArrayList<Fragment>):FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int {
       return 4
    }

    override fun createFragment(position: Int): Fragment {

        var fragment:Fragment?=null

//        when(position)
//        {
//            0-> fragment=ScanFragment()
//            1-> fragment=CreateFragment()
//            2-> fragment=HistoryFragment()
//            3-> fragment=SettingsFragment()
//        }
       // return fragment!!
        return fragList[position]
    }

}