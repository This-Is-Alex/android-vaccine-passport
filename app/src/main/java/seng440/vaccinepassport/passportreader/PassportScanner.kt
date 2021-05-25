package seng440.vaccinepassport.passportreader

import android.content.Context
import android.nfc.Tag
import android.nfc.tech.IsoDep
import net.sf.scuba.smartcards.CardService
import net.sf.scuba.smartcards.CardServiceException
import org.jmrtd.BACKey
import org.jmrtd.PassportService
import org.jmrtd.lds.DG1File
import org.jmrtd.lds.LDSFileUtil
import seng440.vaccinepassport.SerializableVPass
import java.io.InputStream
import java.text.SimpleDateFormat

class PassportScanner(val context: Context) {

    private val dateFormatter = SimpleDateFormat("yyMMdd")

    fun scan(tag: Tag, vpass: SerializableVPass): IDPassport {
        var ps: PassportService? = null
        try {
            val nfc = IsoDep.get(tag)
            val cs = CardService.getInstance(nfc)
            ps = PassportService(cs)
            ps.open()
            ps.sendSelectApplet(false)
            val key = BACKey(vpass.passportNum, dateFormatter.format(
                seng440.vaccinepassport.MainActivity.timestampToDate(vpass.dob)
            ), dateFormatter.format(
                seng440.vaccinepassport.MainActivity.timestampToDate(vpass.passportExpDate)
            ))
            ps.doBAC(key)
            var input: InputStream? = null
            try {
                input = ps.getInputStream(PassportService.EF_DG1)
                val dg1 = LDSFileUtil.getLDSFile(PassportService.EF_DG1, input) as DG1File
                val lastName = dg1.mrzInfo.primaryIdentifier.replace("<", " ").trim()
                val name = dg1.mrzInfo.secondaryIdentifier.replace("<", " ").trim()
                val fullName = "$lastName, $name"

                input.close()
                ps.close()

                return IDPassport(true, null, fullName)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    input?.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } catch (e: CardServiceException) {
            val message = if (e.message != null &&
                            e.message!!.startsWith("Mutual authentication failed"))
                            "Passport does not match"
                          else e.message
            return IDPassport(false, message, null)
        } finally {
            try {
                ps!!.close()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
        return IDPassport(false, "Tag was lost.", null)
    }
}