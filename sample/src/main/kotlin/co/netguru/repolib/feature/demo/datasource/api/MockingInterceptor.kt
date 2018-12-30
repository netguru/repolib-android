package co.netguru.repolib.feature.demo.datasource.api

import android.net.ConnectivityManager
import com.google.gson.Gson
import okhttp3.*
import okio.Buffer

class MockingInterceptor(
        private val gson: Gson,
        private val connectivityManager: ConnectivityManager
) : Interceptor {

    private val remoteDataBaseMock: MutableList<RemoteDataEntity> = arrayListOf(
            RemoteDataEntity(1, "remote note 1"),
            RemoteDataEntity(2, "remote note 2"),
            RemoteDataEntity(3, "remote note 3"),
            RemoteDataEntity(4, "remote note 4"),
            RemoteDataEntity(5, "remote note 5")
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = Response.Builder()
                .request(chain.request())
                .message("ok")
                .protocol(Protocol.HTTP_1_0)
        return if (connectivityManager.activeNetworkInfo.isConnected) {
            generateResponse(chain.request(), builder).build()
        } else {
            throw RuntimeException("Device is offline")
        }
    }

    private fun generateResponse(request: Request, builder: Response.Builder): Response.Builder = when {
        request.url().toString().contains("/getAll") -> builder.code(200)
                .body(ResponseBody.create(MediaType.parse("application/json"), gson.toJson(remoteDataBaseMock)))

        request.url().toString().contains("/create") -> builder.code(200)
                .body(ResponseBody.create(MediaType.parse("application/json"), createElement(request)))

        request.url().toString().contains("/update") -> builder.code(200)
                .body(ResponseBody.create(MediaType.parse("application/json"), updateElement(request)))

        request.url().toString().contains("/delete") -> builder.code(200)
                .body(ResponseBody.create(MediaType.parse("application/json"), deleteElement(request)))

        else -> builder.code(404)
                .body(ResponseBody.create(MediaType.parse("application/json"), ""))
    }

    private fun deleteElement(request: Request): String {
        request.url().queryParameter("id")?.let { id ->
            val requestedId = id.toLong()
            with(remoteDataBaseMock) {
                removeAt(indexOf(find { entity -> entity.id == requestedId }))
            }
        }
        return ""
    }

    private fun updateElement(request: Request): String {
        val requestedElement = extractElementFromBody(request.body())
        with(remoteDataBaseMock) {
            set(
                    indexOf(find { it.id == requestedElement.id }),
                    requestedElement
            )
        }
        return request.body().toString()
    }

    private fun createElement(request: Request): String {
        val requestedElement = extractElementFromBody(request.body())
        requestedElement.id = remoteDataBaseMock.last().id.plus(1)
        remoteDataBaseMock.add(requestedElement)
        return gson.toJson(requestedElement)
    }

    private fun extractElementFromBody(requestBody: RequestBody?): RemoteDataEntity {
        val sink = Buffer()
        requestBody?.writeTo(sink)
        val json = sink.readUtf8()
        sink.writeUtf8(json)
        return gson.fromJson(json, RemoteDataEntity::class.java)
    }
}