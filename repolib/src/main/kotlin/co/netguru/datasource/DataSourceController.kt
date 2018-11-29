package co.netguru.datasource

import co.netguru.data.Query
import io.reactivex.Completable
import io.reactivex.Flowable

interface DataSourceController<T> {

    /**
     * The methods returns [Flowable] that
     *
     * @return [Flowable] - reactive stream of data entity that is responsible for passing
     * all data updates. To receive data call outputDataStream with appropriate [Query]
     */
    fun dataOutput(): Flowable<T>

    /**
     * The method that returns input stream. Send new Query object to trigger
     * data emission on output stream. After successful resolution of Query<T> data will
     * be published to output stream if [dataOutput] [Flowable] is subscribed.
     *
     * @param query object that contains constraints to find specific entity in SimpleDataSourceController
     *
     * @return [Completable] that will emmit complete event when query is resolved with success,
     *
     * @throws Throwable if [Query] fails
     */
    fun fetch(query: Query<T>): Completable

    /**
     * The delete method allows to remove entity specified by the query param
     *
     * @param query object that contains constraints to find specific entity in SimpleDataSourceController
     *
     * @return [Completable] that will emmit complete event when query is resolved with success,
     *
     * @throws Throwable if [Query] fails
     */
    fun delete(query: Query<T>): Completable

    /**
     * Updates existing entity in SimpleDataSourceController passed in param
     *
     * @param entity data entity that will be updated in SimpleDataSourceController
     *
     * @return [Completable] that will emmit complete event when update is complete
     *
     * @throws Throwable if [Query] fails
     */
    fun update(entity: T): Completable

    /**
     * Creates new entity in data source passed in param
     *
     * @param entity data entity that will be created in data source
     *
     * @return [Completable] that will emmit complete event when entity is created,
     *
     * @throws Throwable if [Query] fails
     */
    fun create(entity: T): Completable
}