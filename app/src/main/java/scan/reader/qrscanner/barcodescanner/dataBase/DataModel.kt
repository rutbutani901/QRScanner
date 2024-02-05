package scan.reader.qrscanner.barcodescanner.dataBase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barcodeDataTable")
data class DataModel(
    @ColumnInfo(name = "barValue") val barValue:String,
    @ColumnInfo(name = "barFormat") val barFormat:Int,
    @ColumnInfo(name = "creationTime") val creationTime:String,
    @ColumnInfo(name = "note")val note:String,
    @ColumnInfo(name = "isFav")val isFav:Boolean,
    @ColumnInfo(name = "barcodeType")val barcodeType:Int,
    @ColumnInfo(name = "isSelected")var isSelected:Boolean=false,

    ):java.io.Serializable{
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0 // or foodId: Int? = null
}
