package nz.eloque.foss_wallet.model

object PassStore {
    private var cnt: Int = 0
    private val passes: MutableMap<String, Pass> = HashMap()

    fun add(rawPass: RawPass): String {
        val id = "${cnt++}"
        passes[id] = Pass.from(rawPass)
        return id
    }

    fun get(id: String): Pass {
        return passes[id]!!
    }
}