//package org.ffb_be.config.security;
//
//import co.elastic.clients.elasticsearch.ElasticsearchClient;
//import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
//import jakarta.annotation.PostConstruct;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ElasticsearchIndexInitializer {
//
//    private final ElasticsearchClient client;
//
//    // Constructor injection thay vì tự gọi elasticsearchClient()
//    public ElasticsearchIndexInitializer(ElasticsearchClient client) {
//        this.client = client;
//    }
//
//
//    @PostConstruct
//    public void createIndex() throws Exception {
//        if (!client.indices().exists(e -> e.index("products")).value()) {
//            CreateIndexRequest request = CreateIndexRequest.of(b -> b
//                    .index("products")
//                    .settings(s -> s
//                            .analysis(a -> a
//                                    .analyzer("vi_analyzer", an -> an
//                                            .custom(cu -> cu
//                                                    .tokenizer("icu_tokenizer")
//                                                    .filter("lowercase", "asciifolding")
//                                            )
//                                    )
//                            )
//                    )
//                    .mappings(m -> m
//                            .properties("name", p -> p.text(t -> t.analyzer("vi_analyzer")))
//                            .properties("description", p -> p.text(t -> t.analyzer("vi_analyzer")))
//                    )
//            );
//            client.indices().create(request);
//        }
//    }
//}