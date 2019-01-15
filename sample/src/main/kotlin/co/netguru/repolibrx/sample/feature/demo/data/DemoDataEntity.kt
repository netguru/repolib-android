package co.netguru.repolibrx.sample.feature.demo.data

import android.os.Parcelable
import co.netguru.repolibrx.realmadapter.Identified
import co.netguru.repolibrx.realmadapter.RxRealmDataSource.Companion.UNDEFINED
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DemoDataEntity(
        override val id: Long = UNDEFINED,
        val value: String = "",
        var sourceType: SourceType = SourceType.UNKNOWN
) : Identified, Parcelable