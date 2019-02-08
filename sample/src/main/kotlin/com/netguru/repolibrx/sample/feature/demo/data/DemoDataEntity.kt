package com.netguru.repolibrx.sample.feature.demo.data

import android.os.Parcelable
import com.netguru.repolibrx.RepoLib.Companion.UNDEFINED
import com.netguru.repolibrx.realmadapter.Identified
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DemoDataEntity(
        override val id: Long = UNDEFINED,
        val value: String = "",
        var sourceType: SourceType = SourceType.UNKNOWN
) : Identified, Parcelable