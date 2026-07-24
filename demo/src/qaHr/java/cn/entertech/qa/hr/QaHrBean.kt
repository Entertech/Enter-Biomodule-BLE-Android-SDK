package cn.entertech.qa.hr

data class QaHrBean(val rawData: ByteArray, val hr: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QaHrBean

        if (!rawData.contentEquals(other.rawData)) return false
        if (hr != other.hr) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rawData.contentHashCode()
        result = 31 * result + hr
        return result
    }
}