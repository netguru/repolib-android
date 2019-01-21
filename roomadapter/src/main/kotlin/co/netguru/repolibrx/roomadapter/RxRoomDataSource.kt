package co.netguru.repolibrx.roomadapter

import co.netguru.repolibrx.data.Query
import co.netguru.repolibrx.data.QueryAll
import co.netguru.repolibrx.data.QueryById
import co.netguru.repolibrx.data.QueryWithParams
import co.netguru.repolibrx.datasource.DataSource
import co.netguru.repolibrx.roomadapter.mappers.RoomDataMapper
import co.netguru.repolibrx.roomadapter.mappers.RoomQueryMapper
import io.reactivex.Completable
import io.reactivex.Observable

open class RxRoomDataSource<E, D>(
        private val tableName: String,
        private val baseDao: BaseDao<D>,
        private val queryMapper: RoomQueryMapper,
        private val roomDataMapper: RoomDataMapper<E, D>
) : DataSource<E> {

    override fun create(entity: E): Observable<E> = Observable
            .fromCallable { entity }
            .map(roomDataMapper.transformEntityToDaoModel())
            .flatMap { item ->
                Completable.fromAction { baseDao.create(item) }
                        .andThen(Observable.just(item))
                        .map(roomDataMapper.transformModelToEntity())
            }

    override fun update(entity: E): Observable<E> = Observable
            .fromCallable { entity }
            .map(roomDataMapper.transformEntityToDaoModel())
            .flatMapCompletable { item ->
                Completable.fromAction { baseDao.update(item) }
            }.andThen(Observable.just(entity))

    override fun delete(query: Query): Observable<E> = Observable
            .fromCallable { query }
            .flatMapCompletable {
                Completable.fromAction {
                    baseDao.delete(delete(tableName, getQueryPredicates(query)))
                }
            }
            .toObservable()

    override fun fetch(query: Query): Observable<E> = Observable
            .fromCallable { query }
            .flatMapSingle {
                baseDao.query(select("*", tableName, getQueryPredicates(query)))
            }
            .flatMapIterable { it }
            .map(roomDataMapper.transformModelToEntity())

    private fun getQueryPredicates(query: Query): String = when (query) {
        is QueryById -> queryMapper.transformQueryByIdToStringPredicate(query)
        is QueryAll -> queryMapper.transformQueryAllToStringPredicate(query)
        is QueryWithParams -> queryMapper.transformQueryWithParamsToStringPredicate(query)
        else -> queryMapper.transformQueryToStringPredicate(query)
    }
}