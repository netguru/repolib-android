package co.netguru.repolibrx.data

interface Query

object QueryAll : Query

class QueryWithParams(vararg paramPairs: Pair<String, Any>) : Query {

    val params = mutableMapOf<String, Any>()

    init {
        paramPairs.forEach { (key, value) -> params[key] = value }
    }

    inline fun <reified T> param(key: String): T = params[key] as T
}