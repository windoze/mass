package marketplace.azure.cn

import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * Created by Chen Xu on 2/23/2017.
 * Copyright(C) 2016, All rights reserved.
 */

@RestController
@RequestMapping(value="/indexes/{index}/docs")
public class SearchService {
    @RequestMapping(value="/suggest")
    fun suggest(@PathVariable index:String, @RequestParam("api-version") apiVersion:String) : String {
        return ""
    }

    @RequestMapping(value="/search")
    fun search(@PathVariable index:String, @RequestParam("api-version") apiVersion:String) : String {
        return ""
    }
}