package seng440.vaccinepassport.passportreader

class IDPassport(
    public val success: Boolean,
    public val errorMessage: String?,
    public val fullName: String?
) {
}