package co.netguru.repolib.feature.demo.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DemoDataEntity(
        val id: Long = UNDEFINED,
        val value: String = "",
        val sourceType: SourceType = SourceType.UNKNOWN
) : Parcelable

const val UNDEFINED: Long = -1
