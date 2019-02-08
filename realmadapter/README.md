# Realm Adapter for RepoLibRx

This is a **Realm Adapter**  that contains DataSources adapter for RepoLibRx based on [Realm storage](https://realm.io/blog/realm-for-android/). 
The module includes implementation of the DataSource interface based on Realm database. It contains implementation
of all basic operation required by the interface like *create*, *update*, *delete* and *fetch*.

## Download
To use this module with RepoLibRx in your project, add Netguru maven urls to the repositories blocks
to the build.gradle in your project root dir:
```
repositories {
    maven {  url 'https://dl.bintray.com/netguru/maven/' }
}
```

Then add following dependencies to the app module build.gradle:
```
dependencies {
   implementation 'com.netguru.repolibrx:realmadapter:0.5'
}

```

## Usage
Follow this steps to setup adapter and create working DataSource interface implementation:

1. Add and initialize Realm accordingly to its [documentation](https://realm.io/blog/realm-for-android/). 
Then initialize `RealmConfiguration` accordingly to your needs, e.g.
```
val realmConfig = RealmConfiguration.Builder()
            .name("realm database")
            .schemaVersion(1)
            .deleteRealmIfMigrationNeeded()
            .build()
```

2. Create data entity that extends RealmObject, e.g.
```
open class NoteLocalRealmObject : RealmObject() {
    var id: Long = UNDEFINED
    var value: String? = null
}
```
3. Create implementation of `RealmQueryMapper` interface. The interface is responsible for translating 
RepoLib Query objects to `RealmQuery` objects required by the Realm storage. Query Mapper should 
include the property that holding the `Class` of the `RealmObject` created in step 2.  Below you can 
find [example](https://github.com/netguru/repolib-android/blob/task/RPI-18/sample/src/main/kotlin/co/netguru/repolibrx/sample/feature/demo/datasource/localstore/QueryMapper.kt) 
of such implementation:
```
class QueryMapper(override val daoClass: Class<NoteLocalRealmObject> = NoteLocalRealmObject::class.java)
    : RealmQueryMapper<NoteLocalRealmObject> {

    override fun transform(query: QueryById, realm: Realm): RealmQuery<NoteLocalRealmObject> = realm
            .where(daoClass).equalTo("id", query.identifier)

    override fun transform(query: QueryWithParams, realm: Realm): RealmQuery<NoteLocalRealmObject> {
        val realmQuery = realm.where(daoClass)
        val idKey = "id"
        val valueKey = "value"
        if (query.params.containsKey(idKey)) realmQuery.equalTo(idKey, query.param<Long>(idKey))
        if (query.params.containsKey(valueKey)) realmQuery.equalTo(valueKey, query.param<String>(valueKey))
        return realmQuery
    }
}
```

4. Create implementation of `RealmDataMapper` interface, that will be responsible for translating 
data models from RepoLib data model entity to `Realm` specific model that extends `RealmObject` 
created in 1. Below you can find [example](https://github.com/netguru/repolib-android/blob/task/RPI-18/sample/src/main/kotlin/co/netguru/repolibrx/sample/feature/demo/datasource/localstore/DataMapper.kt) 
of such implementation:
```
class DataMapper : RealmDataMapper<DemoDataEntity, NoteLocalRealmObject> {
    private var highestId = UNDEFINED

    override fun transformToEntity(): (NoteLocalRealmObject) -> DemoDataEntity = { dao ->
        DemoDataEntity(saveHighestId(dao.id), dao.value!!, SourceType.LOCAL)
    }

    override fun rewriteValuesToDao(
            entity: DemoDataEntity,
            emptyDaoObject: NoteLocalRealmObject
    ): DemoDataEntity {
        emptyDaoObject.id = if (entity.id != UNDEFINED) entity.id else highestId + 1
        emptyDaoObject.value = entity.value
        return entity.copy(id = emptyDaoObject.id, sourceType = SourceType.LOCAL)
    }

    private fun saveHighestId(currentDaoId: Long): Long {
        highestId = if (currentDaoId > highestId) currentDaoId else highestId
        return currentDaoId
    }
}
```
**Important**
Please note that Data mapper showed in example contains manual handling of *identifiers*.
 Unfortunately, Realm doesn't support any automatic handling of object ids. Because of that you 
 need to handle identifiers by yourself accordingly to your needs or project specification.

5. Add `Identified` interface to your data model used by `RepoLib`, e.g.
```
data class DemoDataEntity(
        override val id: Long = UNDEFINED,
        val value: String = "",
        var sourceType: SourceType = SourceType.UNKNOWN
) : Identified
```

5. Initialize RxRealmDataSource object with both mappers implementations and `RealmConfiguration` 
initialized in previous steps, e.g.

```
val localRealmDataSource = RxRealmDataSource(realmConfiguration, dataMapper, queryMapper)
```

6. Initialize RepoLibRx with `localRealmDataSource` as `localDataSource`. For more information 
about [RepoLib Readme](https://github.com/netguru/repolib-android)

## License  
```
Copyright 2018 Netguru

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
