package co.netguru.repolibrx.sample.feature.demo.datasource.api

import android.net.ConnectivityManager
import co.netguru.repolibrx.sample.feature.demo.data.DemoDataEntity
import co.netguru.repolibrx.sample.feature.demo.data.SourceType
import com.google.gson.Gson
import okhttp3.*
import okio.Buffer

/**
 * [MockingInterceptor] is an implementation of the [Interceptor] interface used by the [OkHttpClient]
 * to intercept all HTTP requests. [MockingInterceptor] is responsible to mock REST API behaviour
 * and its responses. This interceptor was created to illustrate communication with typical REST
 * web service.
 *
 * @param gson instance of [Gson] converter used to translate JSON body to data models and reverse
 * @param connectivityManager is an instance of system service called [ConnectivityManager]. It is
 * used to symulate the network errors. Eg. if the user will enable airplane mode in the device
 * that Sample app is running, the Mocking Interceptor will return to [OkHttpClient] typical errors
 * that will appear when REST web service is unreachable.
 */
class MockingInterceptor(
        private val gson: Gson,
        private val connectivityManager: ConnectivityManager
) : Interceptor {

    private val remoteDataBaseMock: MutableList<DemoDataEntity> = arrayListOf(
            DemoDataEntity(1, "remote note 1", SourceType.REMOTE),
            DemoDataEntity(2, "remote note 2", SourceType.REMOTE),
            DemoDataEntity(3, "remote note 3", SourceType.REMOTE),
            DemoDataEntity(4, "remote note 4", SourceType.REMOTE),
            DemoDataEntity(5, "remote note 5", SourceType.REMOTE)
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

        request.url().toString().contains("/sqlDelete") -> builder.code(200)
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
        val requestedElement = extractElementFromBody(request.body()).copy(sourceType = SourceType.REMOTE)
        with(remoteDataBaseMock) {
            set(
                    indexOf(find { it.id == requestedElement.id }),
                    requestedElement
            )
        }
        return gson.toJson(requestedElement)
    }

    private fun createElement(request: Request): String {
        val requestedElement = extractElementFromBody(request.body())
        remoteDataBaseMock.add(requestedElement.copy(
                id = remoteDataBaseMock.last().id.plus(1),
                sourceType = SourceType.REMOTE
        ))
        return gson.toJson(remoteDataBaseMock.last())
    }

    private fun extractElementFromBody(requestBody: RequestBody?): DemoDataEntity {
        val sink = Buffer()
        requestBody?.writeTo(sink)
        val json = sink.readUtf8()
        sink.writeUtf8(json)
        return gson.fromJson(json, DemoDataEntity::class.java)
    }
}