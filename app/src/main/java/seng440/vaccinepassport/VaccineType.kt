package seng440.vaccinepassport

enum class VaccineType(val id: Byte, val fullName: String, val numDoses: Int, val daysBetweenDoses: Int?) {
    PFIZER(1, "Pfizer-BioNTech", 2, 21),
    JANSSEN(2, "Johnson & Johnson's Janssen", 1, null),
    NOVAVAX(3, "NovaVax", 2, 31),
    ASTRA_ZENECA(4, "AstraZeneca", 2, 56),
    MODERNA(5, "Moderna", 2, 28);

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