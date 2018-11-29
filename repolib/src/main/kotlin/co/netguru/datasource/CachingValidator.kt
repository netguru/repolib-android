package co.netguru.datasource

interface CachingValidator {

    fun isOperationPermitted(): Boolean
}