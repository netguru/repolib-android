package com.netguru.repolibrx.datasource

import io.reactivex.Observable

fun <T> DataSource<T>.asObservable(): Observable<DataSource<T>> = Observable.just(this)

fun <T> DataSource<T>.applyAdditionalAction(modifier: (DataSource<T>) -> Observable<T>)
        : Observable<T> = this.asObservable().flatMap(modifier)
