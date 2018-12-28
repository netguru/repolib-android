package co.netguru.repolib.feature.demo.datasource.api

import okhttp3.*

class MockingInterceptor : Interceptor {

    private val content = "[\n" +
            "{\n" +
            "  \"id\": 1,\n" +
            "  \"note\": \"test note\"\n" +
            "},\n" +
            "{\n" +
            "  \"id\": 2,\n" +
            "  \"note\": \"test note\"\n" +
            "},\n" +
            "{\n" +
            "  \"id\": 3,\n" +
            "  \"note\": \"test note\"\n" +
            "},\n" +
            "{\n" +
            "  \"id\": 4,\n" +
            "  \"note\": \"test note\"\n" +
            "}\n" +
            "]" +
            ""

    override fun intercept(chain: Interceptor.Chain): Response = selectResponse(chain.request().url())

    private fun selectResponse(url: HttpUrl?): Response = when {
        url.toString().contains("/getAll") -> Response.Builder()
                .code(200)
                .body(ResponseBody.create(MediaType.parse("application/json"), content))
                .build()
        else -> Response.Builder()
                .code(404)
                .build()
    }


}