package co.netguru.repolib.feature.demo.data

data class DemoDataEntity(
        val id: Long = UNDEFINED,
        val value: String = "",
        val sourceType: SourceType = SourceType.UNKNOWN
)

const val UNDEFINED: Long = -1
