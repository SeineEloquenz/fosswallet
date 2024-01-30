package nz.eloque.foss_wallet.model

object PassStore {
    private var cnt: Int = 0
    private val passes: MutableMap<String, RawPass> = HashMap()

    fun add(rawPass: RawPass): String {
        val id = "${cnt++}"
        passes[id] = rawPass
        return id
    }

    fun get(id: String): RawPass {
        return passes[id]!!
    }
}