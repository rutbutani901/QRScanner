package scan.reader.qrscanner.barcodescanner.modelClass

import android.graphics.drawable.Icon

data class SupportedCodesModelClass(
    var codeTitle:String,
    var codeIcon: Int,
    var isSupported:Boolean
    )