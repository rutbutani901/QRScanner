package scan.reader.qrscanner.barcodescanner.viewModel

import android.graphics.drawable.Drawable

data class InstalledAppDataModelClass(
    var appName:String,
    var packageName:String,
    var appIcon:Drawable
)