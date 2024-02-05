package scan.reader.qrscanner.barcodescanner.dataBase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DataModel::class], version = 1,exportSchema = false)
abstract class BarcodeDataBase:RoomDatabase() {

    abstract fun barcodeDao(): BarcodeDao

   companion object{

       private var instance: BarcodeDataBase? = null

       @Synchronized
       fun getInstance(ctx: Context): BarcodeDataBase {
           if(instance == null){
               instance = Room.databaseBuilder(ctx.applicationContext, BarcodeDataBase::class.java,
                   "barcode_database")
                   .build()
           }
           return instance!!
       }

   }

}