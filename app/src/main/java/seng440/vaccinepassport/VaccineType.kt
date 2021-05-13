package seng440.vaccinepassport

enum class VaccineType(val id: Byte, val fullName: String) {
    PFIZER(1, "Pfizer-BioNTech"),
    JANSSEN(2, "Johnson & Johnson's Janssen"),
    NOVAVAX(3, "NovaVax"),
    ASTRA_ZENECA(4, "AstraZeneca"),
    MODERNA(5, "Moderna");

    companion object {
        fun fromId(id: Byte): VaccineType? {
            for (type in values()) {
                if (type.id == id) {
                    return type
                }
            }
            return null
        }
    }
}