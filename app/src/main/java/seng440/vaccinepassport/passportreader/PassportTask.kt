package seng440.vaccinepassport.passportreader

import android.content.Context
import android.nfc.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import seng440.vaccinepassport.SerializableVPass

class PassportTask {
    suspend fun readTag(tag: Tag, vpass: SerializableVPass, context: Context, callback: PassportReaderCallback) {
        withContext(Dispatchers.IO) {
            try {
                val scanner = PassportScanner(context)
                val passport = scanner.scan(tag, vpass)
                withContext(Dispatchers.Main) {
                    if (passport.success) {
                        callback.onReadSuccess(passport)
                    } else {
                        callback.onReadFailure(passport.errorMessage!!)
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                val message = if (exception.message != null) exception.message!! else "This should never happen"
                callback.onReadFailure(message)
            }
        }
    }
}