package co.netguru.repolib.feature.demo.datasource.api

import co.netguru.repolib.feature.demo.data.UNDEFINED

data class RemoteDataEntity(
        var id: Long = UNDEFINED,
        val note: String
)