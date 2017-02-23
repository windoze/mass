package marketplace.azure.cn

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Created by Chen Xu on 2/23/2017.
 * Copyright(C) 2016, All rights reserved.
 */

@ConfigurationProperties(prefix = "mass")
data class MassConfiguration(
        var urlbase: String = "http://localhost"
)