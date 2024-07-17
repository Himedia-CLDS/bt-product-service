package com.clds.bottletalk.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products")
public class Product {

    @Id
    @Field(type = FieldType.Keyword)
    private String id;
    private long product_id;
    @Field(name = "kor_name", type = FieldType.Text)
    private String korName;
    @Field(name = "eng_name", type = FieldType.Text)
    private String engName;
    private String img;
    private double price;
    private String alcohol;
    private String country;
    private String capacity;
    private String description;
    private String category;
    private TastingNotes tasting_notes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TastingNotes {
        private String aroma;
        private String taste;
        private String finish;
    }
}
