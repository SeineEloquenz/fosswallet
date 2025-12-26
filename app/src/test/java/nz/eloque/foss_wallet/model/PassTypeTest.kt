package nz.eloque.foss_wallet.model

import org.junit.Test

class PassTypeTest {

    @Test
    fun testIsSameType() {
        val allPassTypes = PassType.all()
        for (pass in allPassTypes) {
            assert(allPassTypes.any { pass.isSameType(it) })
        }
        val boardingPassTypes = TransitType.entries.map { PassType.Boarding(it) }
        for (pass in boardingPassTypes) {
            assert(allPassTypes.any { pass.isSameType(it) })
        }
    }
}