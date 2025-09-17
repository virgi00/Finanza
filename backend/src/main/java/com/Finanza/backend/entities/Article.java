package com.Finanza.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "article")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String body;
    
    private LocalDateTime date;
    
    @Lob
    @Column(columnDefinition = "LONGTEXT")  // Supporta URL lunghi o dati base64
    private String srcImg;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToMany
    private List<Tag> tags;

    // Constructors
    public Article() {}

    public Article(String title, String body, Category category) {
        this.title = title;
        this.body = body;
        this.category = category;
        this.date = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
    
    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }
    
    public String getSrcImg() { return srcImg; }
    public void setSrcImg(String srcImg) { this.srcImg = srcImg; }
    
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    
    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }
}