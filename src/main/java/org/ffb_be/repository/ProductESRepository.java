package org.ffb_be.repository;

import org.ffb_be.entity.Product;
import org.ffb_be.entity.ProductES;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;


public interface ProductESRepository extends ElasticsearchRepository<ProductES, Long> {

    @Query("""
    {
        "bool": {
            "must": [
                {
                    "more_like_this": {
                        "fields": ["name^3", "description^2"],
                        "like": "?0",
                        "min_term_freq": 1,
                        "min_doc_freq": 1,
                        "minimum_should_match": "90%",
                        "boost": 1.5
                    }
                }
            ],
            "should": [
                {
                    "match_phrase": {
                        "name": {
                            "query": "?0",
                            "slop": 2,
                            "boost": 2.0
                        }
                    }
                }
            ]
        }
    }
    """)
    Page<Product> findHighlySimilarProducts(String searchText, Pageable pageable);
}