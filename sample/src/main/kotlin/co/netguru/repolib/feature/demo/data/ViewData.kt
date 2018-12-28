package co.netguru.repolib.feature.demo.data

data class ViewData(
        val error: String? = null,
        val items: List<DemoDataEntity> = arrayListOf()
)