package scan.reader.qrscanner.barcodescanner.viewPager

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import scan.reader.qrscanner.barcodescanner.fragments.FirstIntroFrag
import scan.reader.qrscanner.barcodescanner.fragments.SecondIntroFrag
import scan.reader.qrscanner.barcodescanner.fragments.ThirdIntroFrag

class AppIntroViewPager(fragmentActivity: FragmentActivity,val fragList:ArrayList<Fragment>): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {

        var fragment: Fragment?=null

        when(position)
        {
            0-> fragment= FirstIntroFrag()
            1-> fragment= SecondIntroFrag()
            2-> fragment= ThirdIntroFrag()
        }
        //return fragment!!

        return fragList[position]

    }

}