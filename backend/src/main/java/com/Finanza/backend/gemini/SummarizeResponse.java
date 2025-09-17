package com.Finanza.backend.gemini;

import java.util.List;

public class SummarizeResponse {
    private String summary;
    private String title;
    private String body;
    private List<String> tags;
    private String category;

    // Constructors
    public SummarizeResponse() {}

    // Getters and Setters
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}