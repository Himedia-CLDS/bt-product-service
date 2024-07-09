package com.clds.bottletalk.product.model;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Entity
@Getter
@ToString
@Builder //setter 대신 엔티티사용
@Table(name="product")
public class Product {

    @Id
    @Column(name ="product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name ="product_name")
    private String name;

    @Column(name ="product_img")
    private String img;

    @Column(name ="product_price")
    private int price;

    @Column(name ="product_country")
    private String country;

    @Column(name ="product_description")
    private String description;

    protected Product() {
    }

    public Product(Long id, String name, String img, int price, String country, String description) {
        this.id = id;
        this.name = name;
        this.img = img;
        this.price = price;
        this.country = country;
        this.description = description;
    }




}
