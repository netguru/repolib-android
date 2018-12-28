package co.netguru.repolib.feature.demo.datasource.api

import okhttp3.*

class MockingInterceptor : Interceptor {

    private val singleElement = "{\n" +
            "  \"id\": 1,\n" +
            "  \"note\": \"new element note\"\n" +
            "}"

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

    override fun intercept(chain: Interceptor.Chain): Response {
        val builder = Response.Builder()
                .request(chain.request())
                .message("ok")
                .protocol(Protocol.HTTP_1_0)

        return generateResponse(chain.request().url().toString(), builder).build()
    }

    private fun generateResponse(request: String, builder: Response.Builder): Response.Builder = when {
        request.contains("/getAll") -> builder.code(200)
                .body(ResponseBody.create(MediaType.parse("application/json"), content))

        request.contains("/create") -> builder.code(200)
                .body(ResponseBody.create(MediaType.parse("application/json"), singleElement))
        else -> builder.code(404)
                .body(ResponseBody.create(MediaType.parse("application/json"), content))
    }


}