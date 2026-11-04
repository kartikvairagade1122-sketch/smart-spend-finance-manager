package com.smartspend.model.dto.category;

import com.smartspend.model.entity.TransactionType;

public class CategoryResponse {

    private Long id;
    private String name;
    private TransactionType type;
    private String icon;
    private String color;
    private Boolean isDefault;

    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, TransactionType type, String icon, String color, Boolean isDefault) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.icon = icon;
        this.color = color;
        this.isDefault = isDefault;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}
