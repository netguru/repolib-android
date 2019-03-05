# Room Adapter for RepoLibRx

This is a **Room Adapter**  that contains DataSources adapter for RepoLibRx based on [Room storage](https://developer.android.com/topic/libraries/architecture/room). 
The module includes implementation of the DataSource interface based on Room database. It contains implementation
of all basic operation required by the interface like *create*, *update*, *delete* and *fetch*.

## Download
To use this module with RepoLibRx in your project, add Netguru maven urls to the repositories block:

```gradle
repositories {
    maven {  url 'https://dl.bintray.com/netguru/maven/' }
}
```

Then add following dependencies to the app module build.gradle:
```gradle
dependencies {
   implementation 'com.netguru.repolibrx:roomadapter:0.5'
}

```

## Usage
Follow this steps to setup adapter and create working DataSource interface implementation:

1. Initialize Room accordingly to its [documentation](https://developer.android.com/training/data-storage/room/defining-data)
2. Create implementation of `RoomQueryMapper` interface. The interface is responsible for translating
 RepoLib Query objects to SQL queries required by the ROOM. Below you can find 
 [example](https://github.com/netguru/repolib-android/blob/task/master/sample/src/main/kotlin/com/netguru/repolibrx/sample/feature/demo/datasource/roomdatastorce/NotesQueryMapper.kt)
  of such implementation:

```kotlin
class NotesQueryMapper : RoomQueryMapper {
    override fun transformQueryByIdToStringPredicate(query: QueryById) = "id=${query.identifier}"
    override fun transformQueryAllToStringPredicate(query: QueryAll) = ""
    override fun transformQueryToStringPredicate(query: Query) = ""
    override fun transformQueryWithParamsToStringPredicate(query: QueryWithParams) = "id=${query.param<Long>("id")}"
}
```

3. Create implementation of `RoomDataMapper` interface, that will be responsible for translating 
data models from RepoLib data model entity to Room specific model created in 1. Below you can 
find [example](https://github.com/netguru/repolib-android/blob/task/master/sample/src/main/kotlin/com/netguru/repolibrx/sample/feature/demo/datasource/roomdatastorce/NotesDataMapper.kt) 
of such implementation:

```kotlin
class NotesDataMapper : RoomDataMapper<DemoDataEntity, Note> {

    override fun transformEntityToDaoModel(): (DemoDataEntity) -> Note = { entity ->
        if (entity.id != RepoLib.UNDEFINED) {
            Note(entity.id.toInt(), entity.value)
        } else {
            Note(value = entity.value)
        }
    }

    override fun transformModelToEntity(): (Note) -> DemoDataEntity = {
        DemoDataEntity(it.id.toLong(), it.value, SourceType.LOCAL)
    }
}
```

4. Modify DAO object created in point 1. to extend `BaseDao`, e.g.

```kotlin
@Dao
interface NoteLocalDao : BaseDao<Note> {

    @RawQuery
    override fun query(rawQuery: SupportSQLiteQuery): Single<List<Note>>

    @Insert
    override fun create(dataModel: Note)

    @Update
    override fun update(dataModel: Note)

    @RawQuery
    override fun delete(rawQuery: SupportSQLiteQuery): Int
}

```

5. Initialize RoomDataSource object with both mappers implementations, name of the Room/SQL table
 related to the data model and your Dao interface that extends `BaseDao` interface. Below you can 
 find example of such initialization:

```
val localRoomDataSource = RxRoomDataSource(
            "Notes",
            noteDatabase.noteDao(),
            roomQueryMapper,
            roomDataMapper
            )
```

6. Initialize RepoLibRx with `localRoomDataSource` as `localDataSource`. For more information about
 [RepoLib Readme](https://github.com/netguru/repolib-android)

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
