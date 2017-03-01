package cn.azure.marketplace

import org.elasticsearch.client.Client
import org.elasticsearch.index.query.QueryBuilder
import org.elasticsearch.index.query.QueryBuilders.*
import org.elasticsearch.search.sort.SortBuilders
import org.elasticsearch.search.sort.SortOrder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * Created by Chen Xu on 2/23/2017.
 * Copyright(C) 2016, All rights reserved.
 */

@RestController
@CrossOrigin(origins = arrayOf("*"))
@RequestMapping(value = "/indexes/{index}/docs")
class SearchService {
    private val logger = LoggerFactory.getLogger(this.javaClass)

    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    class AccessDeniedException : RuntimeException("Invalid API Key")

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    class UnsupportedAPIVersionException : RuntimeException("API Version is not supported")

    data class SearchRequest(
            var search: String = "",
            var skip: Int = 0,
            var top: Int = 50,
            var queryType: String = "simple",
            var orderby: String = "",
            var searchFields: String = "",
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
                @RequestParam("api-version") apiVersion: Optional<String>,
                @RequestBody request: SuggestRequest,
                @RequestHeader("api-key") token: Optional<String>): Map<String, Any> {
        // Skip token checking if it's not specified in config
        if(config.token.isNotBlank()) {
            if(!token.isPresent || token.get().trim().compareTo(config.token,true)!=0) {
                throw AccessDeniedException()
            }
        }
        // Skip API version checking if it's not specified in config
        if(config.apiVersion.isNotBlank()) {
            if(!apiVersion.isPresent || apiVersion.get()!=config.apiVersion) {
                throw UnsupportedAPIVersionException()
            }
        }

        // Azure Search uses Lucene query string syntax
        val q: QueryBuilder = if (request.fuzzy) {
            val query = boolQuery()
            // Search only in these fields
            request.searchFields.split(",").map {
                query.should(fuzzyQuery(it.trim(), request.search))
            }
            query
        } else {
            val query = queryStringQuery(request.search)
            // Search only in these fields
            request.searchFields.split(",").map {
                query.field(it.trim())
            }
            query
        }

        // Set return fields
        val fields = request.select.split(",").map(String::trim).toTypedArray()
        // Trimmed field list, remove all spaces
        val fieldList = fields.joinToString(",")

        // Use highlight to get matched fields, which seems to be the only way.
        val req = esClient.prepareSearch(index).setQuery(q)
                .addFields(*fields)
                .setFrom(0)
                .setSize(request.top)
        fields.map {
            req.addHighlightedField(it)
        }
        req.setHighlighterRequireFieldMatch(false)

        val result = req.get()

        logger.debug("Found ${result.hits.totalHits} documents from es")

        // AzureSearch response format
        return mapOf(
                "@odata.context" to "${config.urlbase}/indexes('$index')/\$metadata#docs($fieldList)",
                "value" to result.hits.hits.map {
                    val hitFieldName = it.highlightFields.keys.toTypedArray()[0]
                    val hitFieldText = it.fields[hitFieldName]!!.values[0]
                    // map of "field name" -> "field value"
                    it.fields.map { it.key to it.value.values[0] }.toMap() + mapOf("@search.text" to hitFieldText)
                })
    }

    @RequestMapping(value = "/search")
    fun search(@PathVariable index: String,
               @RequestParam("api-version") apiVersion: Optional<String>,
               @RequestBody request: SearchRequest,
               @RequestHeader("api-key") token: Optional<String>): Map<String, Any> {

        // Skip token checking if it's not specified in config
        if(config.token.isNotBlank()) {
            if(!token.isPresent || token.get().trim().compareTo(config.token,true)!=0) {
                throw AccessDeniedException()
            }
        }
        // Skip API version checking if it's not specified in config
        if(config.apiVersion.isNotBlank()) {
            if(!apiVersion.isPresent || apiVersion.get()!=config.apiVersion) {
                throw UnsupportedAPIVersionException()
            }
        }

        val q: QueryBuilder = if (request.queryType.trim().compareTo("simple", true) == 0) {
            val query = simpleQueryStringQuery(request.search)
            // Search only in these fields
            request.searchFields.split(",").map {
                query.field(it.trim())
            }
            query
        } else {
            // QueryString query doesn't need fields spec
            queryStringQuery(request.search)
        }

        // Set return fields
        val fields = request.select.split(",").map(String::trim).toTypedArray()
        // Trimmed field list, remove all spaces
        val fieldList = fields.joinToString(",")

        val req = esClient.prepareSearch(index).setQuery(q)
                .addFields(*fields)
                .setFrom(0)
                .setSize(request.top)
                .setFrom(request.skip)

        // Order by
        request.orderby.split(",").map {
            val clause = it.split(" ")
            // Default to ASC
            var asc = true
            if (clause.size > 1) {
                asc = clause[1].trim().compareTo("asc", true) == 0
            }
            req.addSort(clause[0].trim(), if (asc) SortOrder.ASC else SortOrder.DESC)
        }

        // Make sure score is calculated
        req.addSort(SortBuilders.scoreSort())

        val result = req.get()

        logger.debug("Found ${result.hits.totalHits} documents from es")

        return mapOf(
                "@odata.context" to "${config.urlbase}/indexes('$index')/\$metadata#docs($fieldList)",
                "value" to result.hits.hits.map {
                    // map of "field name" -> "field value"
                    it.fields.map { it.key to it.value.values[0] }.toMap() + mapOf("@search.score" to it.score)
                })
    }
}
