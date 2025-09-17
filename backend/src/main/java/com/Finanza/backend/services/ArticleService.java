package com.Finanza.backend.services;

import com.Finanza.backend.entities.Article;
import com.Finanza.backend.entities.Tag;
import com.Finanza.backend.entities.Category;
import com.Finanza.backend.repositories.ArticleRepository;
import com.Finanza.backend.repositories.CategoryRepository;
import com.Finanza.backend.repositories.TagRepository;
import com.Finanza.backend.dto.CreateArticleRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

@Service
public class ArticleService {
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private TagRepository tagRepository;

    // GET methods
    public Article getArticleById(Long id) {
        return articleRepository.findById(id).orElse(null);
    }

    // TODO: Aggiornare per LocalDateTime se necessario
    /*
    public List<Article> getListArticleByDate(LocalDateTime date) {
        return articleRepository.findByDate(date);
    }
    */

    public List<Article> getListArticlesByTags(String tag) {
        return articleRepository.findByTagsNameOrderByDateDesc(tag);
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAllOrderByDateDesc();
    }

    public List<Article> getListArticlesByCategoryId(Long categoryId) {
        return articleRepository.findByCategoryId(categoryId);
    }

    public List<Article> getListArticlesByTagListAndCategory(Long categoryId, List<Long> tagIds) {
        if (categoryId != null && tagIds != null && !tagIds.isEmpty()) {
            return articleRepository.findByCategoryIdAndTagsIdIn(categoryId, tagIds);
        } else if (categoryId != null) {
            return articleRepository.findByCategoryId(categoryId);
        } else if (tagIds != null && !tagIds.isEmpty()) {
            return articleRepository.findByTagsIdIn(tagIds);
        }
        return getAllArticles();
    }

    public Long getMaxId() {
        return articleRepository.findMaxId();
    }

    public List<Article> getLatestNews() {
        return articleRepository.findTop10ByOrderByDateDesc();
    }

    // POST method
    public Article newArticle(Article article) {
        if (article.getCategory() == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        
        if (article.getDate() == null) {
            article.setDate(LocalDateTime.now());
        }
        
        // Gestione dei tag: se l'articolo ha tag, assicuriamoci che esistano nel database
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            List<Tag> managedTags = new ArrayList<>();
            for (Tag tag : article.getTags()) {
                Tag existingTag = tagRepository.findTagByName(tag.getName());
                if (existingTag != null) {
                    // Il tag esiste già, usa quello dal database
                    managedTags.add(existingTag);
                } else {
                    // Il tag non esiste, crealo
                    Tag newTag = new Tag();
                    newTag.setName(tag.getName());
                    Tag savedTag = tagRepository.save(newTag);
                    managedTags.add(savedTag);
                }
            }
            article.setTags(managedTags);
        }
        
        return articleRepository.save(article);
    }
    
    // Metodo per creare un articolo da DTO con tag come stringhe
    public Article createArticleFromRequest(CreateArticleRequest request) {
        Article article = new Article();
        article.setTitle(request.getTitle());
        article.setBody(request.getBody());
        article.setDate(request.getDate() != null ? request.getDate() : LocalDateTime.now());
        article.setSrcImg(request.getSrcImg());
        
        // Gestione categoria
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            article.setCategory(category);
        } else {
            // Categoria di default
            Category defaultCategory = categoryRepository.findByName("STOCKS")
                .orElseGet(() -> {
                    Category stocks = new Category();
                    stocks.setName("STOCKS");
                    return categoryRepository.save(stocks);
                });
            article.setCategory(defaultCategory);
        }
        
        // Gestione tag
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            List<Tag> managedTags = new ArrayList<>();
            for (String tagName : request.getTags()) {
                if (tagName != null && !tagName.trim().isEmpty()) {
                    Tag existingTag = tagRepository.findTagByName(tagName.trim());
                    if (existingTag != null) {
                        managedTags.add(existingTag);
                    } else {
                        Tag newTag = new Tag();
                        newTag.setName(tagName.trim());
                        Tag savedTag = tagRepository.save(newTag);
                        managedTags.add(savedTag);
                    }
                }
            }
            article.setTags(managedTags);
        }
        
        return articleRepository.save(article);
    }
}