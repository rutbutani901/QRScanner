package scan.reader.qrscanner.barcodescanner.modelClass

import scan.reader.qrscanner.barcodescanner.R

data class OtherBarcodeTypeModelClass(
    var barcodeName:String,
    var description:String,
    var barcodeFormat:Int,
    var barcodeIcon:Int= R.drawable.other_qrcodes_icon
):java.io.Serializable
