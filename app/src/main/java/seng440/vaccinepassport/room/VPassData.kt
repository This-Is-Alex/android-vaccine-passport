package seng440.vaccinepassport.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

//import androidx.room.TypeConverter
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken

@Entity(tableName = "vPass")
class VPassData (
    @PrimaryKey(autoGenerate = true) val id: Long,
    @ColumnInfo val date: Date,
    @ColumnInfo val vacId: Short,
    @ColumnInfo val drAdiministered: String,
    @ColumnInfo val dosageNum: Short,
    @ColumnInfo val name: String,
    @ColumnInfo val passportNum: String,
    @ColumnInfo val passportExpDate: Date,
    @ColumnInfo val doB: Date,
    @ColumnInfo val country: String) {

    override fun toString() = "Vaccine: $vacId | $date, $name"
}

//class TagsTypeConverter {
//
//    @TypeConverter
//    fun fromUri(uri: Uri?): String? {
//        return uri.toString()
//    }
//
//    @TypeConverter
//    fun fromStringUri(string: String?): Uri? {
//        return Uri.parse(string)
//    }
//}