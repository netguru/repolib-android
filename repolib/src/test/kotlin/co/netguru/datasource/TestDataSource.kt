package co.netguru.datasource

import co.netguru.TestDataEntity
import io.reactivex.Completable
import io.reactivex.Flowable

class TestDataSource(private val data: List<TestDataEntity>) : DataSource<TestDataEntity>() {
    override fun delete(query: Query<TestDataEntity>): Completable = Completable.complete()

    override fun update(entity: TestDataEntity): Completable = Completable.complete()

    override fun create(entity: TestDataEntity): Completable = Completable.complete()

    public override fun query(query: Query<TestDataEntity>)
            : Flowable<TestDataEntity> = Flowable.fromIterable(data)

}