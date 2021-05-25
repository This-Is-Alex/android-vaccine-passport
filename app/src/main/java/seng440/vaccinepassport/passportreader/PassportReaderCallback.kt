package seng440.vaccinepassport.passportreader

interface PassportReaderCallback {
    fun onReadSuccess(passport: IDPassport)
    fun onReadFailure(message: String)
}