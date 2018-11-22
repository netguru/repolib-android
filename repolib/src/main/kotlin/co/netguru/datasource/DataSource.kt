package co.netguru.datasource

import io.reactivex.BackpressureStrategy
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.subjects.BehaviorSubject

abstract class DataSource<T> {

    private val dataOutputBehaviourSubject = BehaviorSubject.create<T>()

    /**
     * The methods returns [Flowable] that
     *
     * @return [Flowable] - reactive stream of data entity that is responsible for passing
     * all data updates. To receive data call fetch with appropriate [Query]
     */
    fun dataOutput(): Flowable<T> = dataOutputBehaviourSubject
            .toFlowable(BackpressureStrategy.LATEST)

    /**
     * The method that returns input stream. Send new Query object to trigger
     * data emission on output stream. After successful resolution of Query<T> data will
     * be published to output stream if [dataOutput] [Flowable] is subscribed.
     *
     * @param query object that contains constraints to find specific entity in DataSource
     *
     * @return [Completable] that will emmit complete event when query is resolved with success,
     *
     * @throws Throwable if [Query] fails
     */
    fun fetch(query: Query<T>): Completable = Flowable.just(query)
            .flatMap { query(it) }
            .map {
                dataOutputBehaviourSubject.onNext(it)
                it
            }.flatMapCompletable { Completable.complete() }

    /**
     * The delete method allows to remove entity specified by the query param
     *
     * @param query object that contains constraints to find specific entity in DataSource
     *
     * @return [Completable] that will emmit complete event when query is resolved with success,
     *
     * @throws Throwable if [Query] fails
     */
    abstract fun delete(query: Query<T>): Completable

    /**
     * Updates existing entity in DataSource passed in param
     *
     * @param entity data entity that will be updated in DataSource
     *
     * @return [Completable] that will emmit complete event when update is complete
     *
     * @throws Throwable if [Query] fails
     */
    abstract fun update(entity: T): Completable

    /**
     * Creates new entity in data source passed in param
     *
     * @param entity data entity that will be created in data source
     *
     * @return [Completable] that will emmit complete event when entity is created,
     *
     * @throws Throwable if [Query] fails
     */
    abstract fun create(entity: T): Completable

    /**
     * Abstract method that need to be implemented to resolve Query for specific implementation
     * of the [DataSource]. The method is used
     *
     * @param query contains constraints used to find specific object in the database
     * Resolution of the constraints should be implemented according the DataSource implementation
     *
     * @return [Flowable] that represents output stream of with entity,
     *
     * @throws Throwable when object is not found
     */
    protected abstract fun query(query: Query<T>): Flowable<T>
}