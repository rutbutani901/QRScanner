package scan.reader.qrscanner.barcodescanner.viewModel

import androidx.lifecycle.ViewModel

class ScannerActivityViewModel: ViewModel() {

    private var previousFragmentIndex=0

    fun getPreviousFragmentIndex()=previousFragmentIndex

    fun setpreviousFragmentIndex(prevFragIndex:Int){
        this.previousFragmentIndex=prevFragIndex
    }
}