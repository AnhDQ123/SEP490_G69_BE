//package org.ffb_be.config.security;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.json.jackson.JacksonJsonpMapper;
//import co.elastic.clients.transport.ElasticsearchTransport;
//import co.elastic.clients.transport.rest_client.RestClientTransport;
//import org.apache.http.HttpHost;
//import org.elasticsearch.client.RestClient;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
//import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
//import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
//import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
//import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
//
//@Configuration
//public class ElasticsearchConfig {
//
//    @Bean
//    public RestClient restClient() {
//        return RestClient.builder(new HttpHost("localhost", 9200)).build();
//    }
//
//    @Bean
//    public ElasticsearchTransport elasticsearchTransport() {
//        return new RestClientTransport(restClient(), new JacksonJsonpMapper());
//    }
//
//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//        return new ElasticsearchClient(elasticsearchTransport());
//    }
//
//    @Bean
//    public ElasticsearchConverter elasticsearchConverter() {
//        return new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
//    }
//
//    @Bean(name = "elasticsearchTemplate")
//    public ElasticsearchOperations elasticsearchOperations() {
//        return new ElasticsearchTemplate(elasticsearchClient(), elasticsearchConverter());
//    }
//}