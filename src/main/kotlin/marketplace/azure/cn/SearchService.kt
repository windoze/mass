package marketplace.azure.cn

import com.fasterxml.jackson.databind.ObjectMapper
import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilders.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.web.bind.annotation.*

/**
 * Created by Chen Xu on 2/23/2017.
 * Copyright(C) 2016, All rights reserved.
 */

@RestController
@RequestMapping(value = "/indexes/{index}/docs")
public class SearchService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    data class SearchRequest(
            var search: String = "",
            var top: Int = 50,
            var queryType: String = "simple",
            var orderby: String = "",
            var select: String = ""
    )

    data class SuggestRequest(
            var search: String = "",
            var fuzzy: Boolean = false,
            var top: Int = 50,
            var suggesterName: String = "",
            var searchFields: String = "",
            var select: String = ""
    )

    @Autowired
    lateinit var config: MassConfiguration

    @Autowired
    lateinit var esTemplate: ElasticsearchTemplate
    val esClient: Client
        get() = esTemplate.client

    @RequestMapping(value = "/suggest")
    fun suggest(@PathVariable index: String,
                @RequestParam("api-version") apiVersion: String,
                @RequestBody request: SuggestRequest): Map<String, Any> {
        // TODO: Check api-version and access token

        // Azure Search uses Lucene query string syntax
        val q = queryStringQuery(request.search)

        // Search only in these fields
        request.searchFields.split(",").map {
            q.field(it.trim())
        }
        // Set return fields
        val fields = request.select.split(",").map(String::trim).toTypedArray()
        // Trimmed field list, remove all spaces
        val fieldList = fields.joinToString(",")

        val result = esClient.prepareSearch(index).setQuery(q)
                .addFields(*fields)
                .setFrom(0)
                .setSize(request.top)
                .get()

        logger.debug("Found ${result.hits.totalHits} documents from es")

        // AzureSearch response format
        return mapOf(
                "@odata.context" to "${config.urlbase}/indexes('$index')/\$metadata#docs($fieldList)",
                "value" to result.hits.hits.map {
                    // map of "field name" -> "field value"
                    it.fields.map { it.key to it.value.values[0] }.toMap()
                })
    }

    @Suppress("UNCHECKED_CAST")
    @RequestMapping(value = "/search")
    fun search(@PathVariable index: String,
               @RequestParam("api-version") apiVersion: String,
               @RequestBody request: SearchRequest): Map<String, Any> {
        return ObjectMapper().convertValue(request, Map::class.java) as Map<String, Any>
    }
}
