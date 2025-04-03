//package org.ffb_be.entity;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import org.springframework.data.annotation.Id;
//import org.springframework.data.elasticsearch.annotations.Document;
//import org.springframework.data.elasticsearch.annotations.Field;
//import org.springframework.data.elasticsearch.annotations.FieldType;
//
//// File: ProductES.java (chỉ dành cho Elasticsearch)
//@Document(indexName = "products")
//@Data
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//public class ProductES {
//    @Id
//    private Long id;
//
//    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
//    private String name;
//
//    @Field(type = FieldType.Text, analyzer = "vi_analyzer")
//    private String description;
//
//    @Field(type = FieldType.Text)
//    private String manufacturer;
//
//}