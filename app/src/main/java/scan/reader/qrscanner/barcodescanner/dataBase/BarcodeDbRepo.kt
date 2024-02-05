package scan.reader.qrscanner.barcodescanner.dataBase

import android.content.Context
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BarcodeDbRepo {


    companion object {
        var dataBase: BarcodeDataBase? = null

//        private fun intialiseDB(context: Context): BarcodeDataBase?
//        {
//            return BarcodeDataBase.getInstance(context)
//        }

        suspend fun insert(context: Context, barcode: DataModel):Long {
            //dataBase= intialiseDB(context)

          return  BarcodeDataBase.getInstance(context).barcodeDao().insert(barcode)
//            CoroutineScope(Dispatchers.IO).launch {
//
//            }

        }


        fun doesBarcodesExist(context: Context,barcodeValue:String):LiveData<DataModel>{
           return  BarcodeDataBase.getInstance(context).barcodeDao().doesBarcodesExist(barcodeValue)
        }

        fun newdoesBarcodesExist(context: Context,barcodeValue:String):Boolean{
            return  BarcodeDataBase.getInstance(context).barcodeDao().newdoesBarcodesExist(barcodeValue)
        }


        fun deleteAllHistory(context: Context){
            CoroutineScope(Dispatchers.IO).launch {
                BarcodeDataBase.getInstance(context).barcodeDao().deleteAllHistory()
            }
        }
        fun delete(context: Context, id: Int) {
            CoroutineScope(Dispatchers.IO).launch {
                BarcodeDataBase.getInstance(context).barcodeDao().delete(id)
            }
        }

        fun getAllBarcodes(context: Context): LiveData<List<DataModel>> {

            return BarcodeDataBase.getInstance(context).barcodeDao().getAllBarcodes()
        }


        fun getLatestAdded(context: Context): LiveData<DataModel> {
            return BarcodeDataBase.getInstance(context).barcodeDao().getLatestAdded()
        }

        fun updateFav(context: Context, id: Int, isFav: Boolean) {

            CoroutineScope(Dispatchers.IO).launch {
                BarcodeDataBase.getInstance(context).barcodeDao().updateFav(id, isFav)
            }
        }

        fun updateNotes(context: Context, id: Int, note: String) {

            CoroutineScope(Dispatchers.IO).launch {
                BarcodeDataBase.getInstance(context).barcodeDao().updateNotes(id, note)
            }
        }

        fun deleteMultipleIds(context: Context, idList: List<Int>) {
            CoroutineScope(Dispatchers.IO).launch {
                BarcodeDataBase.getInstance(context).barcodeDao().deleteMultipleBarcodes(idList)
            }
        }

        fun makeMultipleFav(context: Context, idList: List<Int>, favOrNot: Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                BarcodeDataBase.getInstance(context).barcodeDao().makeMultipleFav(idList, favOrNot)
            }
        }

    }

}