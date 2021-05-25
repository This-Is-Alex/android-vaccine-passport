package seng440.vaccinepassport

import java.io.Serializable

class SerializableVPass(val date: Int,
                        val vacId: Byte,
                        val drAdministered: String,
                        val dosageNum: Short,
                        val name: String,
                        val passportNum: String,
                        val passportExpDate: Int,
                        val dob: Int,
                        val country: String): Serializable {

}