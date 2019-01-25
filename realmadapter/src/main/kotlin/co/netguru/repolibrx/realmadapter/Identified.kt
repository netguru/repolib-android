package co.netguru.repolibrx.realmadapter

import co.netguru.repolibrx.data.QueryById

/**
 * Interface that is used by the [RxRealmDataSource.update] to find item to update using [QueryById]
 * and [id] field
 */
interface Identified {
    /**
     * [id] val that should contains data entity Id
     */
    val id: Long
}