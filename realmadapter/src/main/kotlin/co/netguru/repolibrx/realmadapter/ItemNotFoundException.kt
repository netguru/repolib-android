package co.netguru.repolibrx.realmadapter

class ItemNotFoundException(identifiedItem: Identified)
    : RuntimeException("Requested item not found - item: $identifiedItem")