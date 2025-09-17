package com.Finanza.backend.repositories;

import com.Finanza.backend.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    // TODO: Aggiornare per LocalDateTime se necessario
    // List<Article> findByDate(LocalDateTime date);
    
    // Query corretta per cercare articoli per nome del tag
    @Query("SELECT DISTINCT a FROM Article a JOIN a.tags t WHERE LOWER(t.name) = LOWER(:tagName)")
    List<Article> findByTagsName(@Param("tagName") String tagName);
    
    List<Article> findByCategoryId(Long categoryId);
    List<Article> findByCategoryIdAndTagsIdIn(Long categoryId, List<Long> tagIds);
    List<Article> findByTagsIdIn(List<Long> tagIds);
    List<Article> findTop10ByOrderByDateDesc();
    
    // Trova tutti gli articoli ordinati per data decrescente (più recenti prima)
    @Query("SELECT a FROM Article a ORDER BY a.date DESC")
    List<Article> findAllOrderByDateDesc();
    
    // Trova articoli per tag ordinati per data decrescente
    @Query("SELECT DISTINCT a FROM Article a JOIN a.tags t WHERE LOWER(t.name) = LOWER(:tagName) ORDER BY a.date DESC")
    List<Article> findByTagsNameOrderByDateDesc(@Param("tagName") String tagName);
    
    @Query("SELECT MAX(a.id) FROM Article a")
    Long findMaxId();
}