package com.netguru.repolibrx.realmadapter

/**
 * [RuntimeException] that is thrown when [RxRealmDataSource] cannot find the item for update in
 * [RxRealmDataSource.update] function
 */
class ItemNotFoundException(identifiedItem: Identified)
    : RuntimeException("Requested item not found - item: $identifiedItem")