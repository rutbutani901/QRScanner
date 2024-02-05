package scan.reader.qrscanner.barcodescanner.dataBase

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface BarcodeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(barcodeDataModel: DataModel):Long

    @Query("SELECT * FROM barcodeDataTable WHERE barValue == :barcodeValue")
    fun doesBarcodesExist(barcodeValue: String): LiveData<DataModel>


    @Query("SELECT EXISTS(SELECT * FROM barcodeDataTable WHERE barValue = :barcodeValue)")
    fun newdoesBarcodesExist(barcodeValue: String): Boolean


    @Query("DELETE FROM barcodeDataTable WHERE id = :id")
    fun delete(id: Int)

    @Query("SELECT * FROM barcodeDataTable ORDER BY isFav DESC, id DESC")//id ASC
    fun getAllBarcodes():  LiveData<List<DataModel>>


    @Query("SELECT * FROM barcodeDataTable ORDER BY id DESC LIMIT 1")
    fun getLatestAdded():LiveData<DataModel>

    @Query("UPDATE barcodeDataTable SET isFav=:makeFav WHERE id=:id")
    fun updateFav( id: Int,makeFav:Boolean)

    @Query("UPDATE barcodeDataTable SET note=:notes WHERE id=:id")
    fun updateNotes( id: Int,notes:String)

    @Query("delete from barcodeDataTable where id in (:idList)")
    fun deleteMultipleBarcodes(idList: List<Int>)

    @Query("UPDATE barcodeDataTable SET isFav = :makeFav WHERE id IN (:ids)")
    fun makeMultipleFav(ids:List<Int>, makeFav:Boolean)

    @Query("DELETE from barcodeDataTable")
    fun deleteAllHistory()





}