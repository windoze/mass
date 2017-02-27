Azure Search Simulator
========================

This application is *<B>not</B>* a full-functional simulator, not even close, it contains very few incomplete APIs for Azure Marketplace usage only.

Pre-requests
------------

* Working Java environment, Oracle JDK 8 is preferred, OpenJDK 8 should also work.
* ElasticSearch 2.4.x, with JiaBa-analysis plugin.
    * Uses following command to install the plugin, replace `2.4.x` with ES version:
        * `/path/to/elasticsearch/bin/plugin install https://github.com/windoze/elasticsearch-analysis-jieba/releases/download/v2.4.x/elasticsearch-analysis-jieba-2.4.x-bin.zip`
        * Create index mapping with `create-index.sh`.

Build
-----

Run `gradle bootRepackage`(or `gradlew bootRepackage` under Windows) under the project root directory to create distribution JAR file, which includes all dependencies except JRE.

The output JAR file can be found at `<project_root>/build/libs/mass-<VERSION>.jar`.


Run
---

* Create a text file with name `application.properties` under the same directory with the JAR file, with following keys:
    * `server.port`, TCP port to listen
    * `server.ssl.key-store`, the file to store certificate for HTTPS.
    * `server.ssl.key-store-password`, password of certificate.
    * `spring.data.elasticsearch.cluster-nodes`, ElasticSearch listening address and port.
    * `mass.apiVersion`, the version of API to simulate, it does not change application behaviors, just checks client requests. Leave blank to disable checking.
    * `mass.token`, API token, request without the token will be rejected, leave blank to disable checking.
    * `mass.urlbase`, OData support, should be set to `https://<hostname>`, the `hostname` is the one this application is using to serve requests.
* Create a certificate or convert an existing certificate, detailed info can be found at <a href="https://docs.oracle.com/cd/E19509-01/820-3503/6nf1il6er/index.html">Oracle site</a>.
* Uses `java -jar mass.jar` to start the application, make sure your certificate is under the current directory or configurated correctly in application.propertoes.
* Or you can use default config file for development, uses `java -jar -Dspring.profiles.active=dev mass.jar` to start application with following settings:
    * It connects to ES running on localhost:9300
    * It uses keystore-dev-selfsigned.p12 certificate, it's a self-signed certificate, password is `mypassword`
    * It doesn't check API token and API version.
