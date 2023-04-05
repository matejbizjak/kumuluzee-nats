package com.kumuluz.ee.nats.testapp.common;

import java.math.BigDecimal;
import java.sql.Blob;
import java.time.Instant;

/**
 * DTO for the test application.
 *
 * @author Matej Bizjak
 */

public class Product {

    private int id;

    private String name;

    private String description;

    private BigDecimal price;

    private int stock;

    private Blob image;

    private Instant addedDate;

    public Product() {
    }

    public Product(int id, String name, String description, BigDecimal price, int stock, Blob image, Instant addedDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.image = image;
        this.addedDate = addedDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Blob getImage() {
        return image;
    }

    public void setImage(Blob image) {
        this.image = image;
    }

    public Instant getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Instant addedDate) {
        this.addedDate = addedDate;
    }
}
