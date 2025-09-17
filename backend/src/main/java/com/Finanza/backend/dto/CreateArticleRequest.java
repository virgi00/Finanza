package com.Finanza.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class CreateArticleRequest {
    private String title;
    private String body;
    private LocalDateTime date;
    private String srcImg;
    private Long categoryId;
    private List<String> tags;

    // Constructors
    public CreateArticleRequest() {}

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    
    public String getSrcImg() { return srcImg; }
    public void setSrcImg(String srcImg) { this.srcImg = srcImg; }
    
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
