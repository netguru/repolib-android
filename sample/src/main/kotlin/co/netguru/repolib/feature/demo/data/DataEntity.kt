package co.netguru.repolib.feature.demo.data

data class DataEntity(
        val id: Long,
        val value: String = "",
        val sourceType: SourceType = SourceType.UNKNOWN
)