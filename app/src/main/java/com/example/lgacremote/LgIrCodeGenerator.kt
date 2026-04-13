package com.example.lgacremote

object LgIrCodeGenerator {

    private const val HDR_MARK = 3200
    private const val HDR_SPACE = 9800
    private const val BIT_MARK = 500
    private const val ONE_SPACE = 1500
    private const val ZERO_SPACE = 550
    private const val FREQUENCY = 38000

    fun getFrequency() = FREQUENCY

    // mode: 0=Cool, 1=Dry, 2=Fan, 3=Auto, 4=Heat
    // fan: 0=Low, 2=Mid, 4=High, 5=Auto
    fun generateAcCode(powerToggle: Boolean, tempC: Int, mode: Int, fan: Int): Int {
        if (powerToggle) {
            return 0x88C0051
        }
        val n0 = 8
        val n1 = 8
        val n2 = 0
        val n3 = mode
        val n4 = (tempC - 15).coerceIn(0, 15)
        val n5 = fan
        val checksum = (n0 + n1 + n2 + n3 + n4 + n5) % 16

        return (n0 shl 24) or (n1 shl 20) or (n2 shl 16) or (n3 shl 12) or (n4 shl 8) or (n5 shl 4) or checksum
    }

    fun buildIrPattern(code: Int, bits: Int = 28): IntArray {
        val pattern = mutableListOf<Int>()
        pattern.add(HDR_MARK)
        pattern.add(HDR_SPACE)

        for (i in (bits - 1) downTo 0) {
            val bit = (code shr i) and 1
            pattern.add(BIT_MARK)
            if (bit == 1) {
                pattern.add(ONE_SPACE)
            } else {
                pattern.add(ZERO_SPACE)
            }
        }
        pattern.add(BIT_MARK) // Stop bit
        return pattern.toIntArray()
    }
}
