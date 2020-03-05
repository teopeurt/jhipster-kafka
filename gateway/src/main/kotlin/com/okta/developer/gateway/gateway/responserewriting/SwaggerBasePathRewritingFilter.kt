package com.okta.developer.gateway.gateway.responserewriting

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.zuul.context.RequestContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.nio.charset.StandardCharsets
import java.util.LinkedHashMap
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties
import org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter
import springfox.documentation.swagger2.web.Swagger2Controller

/**
 * Zuul filter to rewrite micro-services Swagger URL Base Path.
 */
class SwaggerBasePathRewritingFilter : SendResponseFilter(ZuulProperties()) {

    private val log = LoggerFactory.getLogger(javaClass)

    private val mapper = ObjectMapper()

    override fun filterType() = "post"

    override fun filterOrder() = 100

    /**
     * Filter requests to micro-services Swagger docs.
     */
    override fun shouldFilter(): Boolean =
        RequestContext.getCurrentContext().request.requestURI.endsWith(Swagger2Controller.DEFAULT_URL)

    override fun run(): Any? {
        val context = RequestContext.getCurrentContext()

        context.response.characterEncoding = "UTF-8"

        val rewrittenResponse = rewriteBasePath(context)
        if (context.responseGZipped) {
            try {
                context.responseDataStream = ByteArrayInputStream(gzipData(rewrittenResponse))
            } catch (e: IOException) {
                log.error("Swagger-docs filter error", e)
            }
        } else {
            context.responseBody = rewrittenResponse
        }
        return null
    }

    private fun rewriteBasePath(context: RequestContext): String? {
        var responseDataStream = context.responseDataStream
        val requestUri = RequestContext.getCurrentContext().request.requestURI
        try {
            if (context.responseGZipped) {
                responseDataStream = GZIPInputStream(context.responseDataStream)
            }
            val response = IOUtils.toString(responseDataStream, StandardCharsets.UTF_8)
            if (response != null) {
                @Suppress("UNCHECKED_CAST")
                val map = this.mapper.readValue(response, LinkedHashMap::class.java) as LinkedHashMap<String, Any>

                val basePath = requestUri.replace(Swagger2Controller.DEFAULT_URL, "")
                map["basePath"] = basePath
                log.debug("Swagger-docs: rewritten Base URL with correct micro-service route: {}", basePath)
                return mapper.writeValueAsString(map)
            }
        } catch (e: IOException) {
            log.error("Swagger-docs filter error", e)
        }

        return null
    }
}

@Throws(IOException::class)
fun gzipData(content: String?): ByteArray {
    val bos = ByteArrayOutputStream()
    val gzip = PrintWriter(GZIPOutputStream(bos))
    gzip.print(content)
    gzip.flush()
    gzip.close()
    return bos.toByteArray()
}
