package com.Finanza.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Finanza.backend.entities.Article;
import com.Finanza.backend.entities.Tag;
import com.Finanza.backend.repositories.ArticleRepository;
import com.Finanza.backend.repositories.TagRepository;
import com.Finanza.backend.services.ArticleService;

import com.Finanza.backend.entities.Category;
import com.Finanza.backend.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

@RestController
@CrossOrigin()
@RequestMapping("/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;
    
    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;  // Aggiungi questa riga

    @Autowired
    private TagRepository tagRepository;


//********************************************************************************************************
//METODI GET

    /*  FUNZIONA
        searchArticleById(): metodo che restituisce un articolo dato il suo id
     */
    @GetMapping(value="/searchById/{idArticle}", produces = "application/json")
    public ResponseEntity searchArticleById (@PathVariable("idArticle") Long idArticle) throws Exception {
        Article a = articleService.getArticleById(idArticle);
        if (a == null)
            throw new Exception("[X]-- ERROR: Article with id="+idArticle+" not found!");
        else {
            return new ResponseEntity(a, HttpStatus.OK);
        }
    }

    /*  TODO: Aggiornare per LocalDateTime
        searchArticleByDate(): metodo che restituisce una lista di articoli scritti in una data
     */
    /*
    @GetMapping(value="/searchByDate/{date}", produces = "application/json")
    public ResponseEntity searchArticleByDate (@PathVariable("date") LocalDateTime date) throws Exception {
        List<Article> a = articleService.getListArticleByDate(date);
        if (a.isEmpty())
            throw new Exception("[X]-- ERROR: No Article was found for date="+date.toString()+"!");
        else {
            return new ResponseEntity(a, HttpStatus.OK);
        }
    }
    */

    /* FUNZIONA
        searchArticleByTag(): metodo che restituisce tutti gli articoli che hanno quel tag
     */
    @GetMapping(value="/searchByTag/{tag}", produces = "application/json")
    public ResponseEntity<?> searchArticleByTag (@PathVariable("tag") String tag) {
        try {
            if (tag == null || tag.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Il tag non può essere vuoto");
            }
            
            List<Article> articles = articleService.getListArticlesByTags(tag.trim());
            // Restituisce sempre la lista, anche se vuota, invece di lanciare un'eccezione
            return ResponseEntity.ok(articles);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Errore durante la ricerca per tag '" + tag + "': " + e.getMessage());
        }
    }

    /*  FUNZIONA
        getAll(): metodo che restituisce una lista di tutti gli articoli
     */
    @GetMapping(value="/all", produces = "application/json")
    public ResponseEntity getAllArticle () throws Exception {
        List<Article> a = articleService.getAllArticles();
        if (a == null)
            throw new Exception("[X]-- ERROR: No Article was published!");
        else {
            return new ResponseEntity(a, HttpStatus.OK);
        }
    }

    /*  FUNZIONA
        getArticlesByCategoryId(): metodo che restituisce gli articoli per categoria
     */
    @GetMapping(value="/byCategoryId/{categoryId}", produces = "application/json")
    public ResponseEntity<?> getArticlesByCategoryId(@PathVariable("categoryId") Long categoryId) throws Exception{
        try {
            // Verifica che la categoria esista
            if (!categoryRepository.existsById(categoryId)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Categoria con ID " + categoryId + " non trovata");
            }
            
            List<Article> articles = articleService.getListArticlesByCategoryId(categoryId);
            if (articles == null || articles.isEmpty()) {
                return ResponseEntity.ok(articles); // Restituisce lista vuota invece di errore
            } else {
                return ResponseEntity.ok(articles);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Errore durante il recupero degli articoli per categoria: " + e.getMessage());
        }
    }

    /*  FUNZIONA
        getArticlesByCategoryAndTags(): forniti una lista di tag ed una categoria,
        restituisce una lista di articoli
    */
    @GetMapping
    public List<Article> getArticlesByCategoryAndTags(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> listTags) {
        return articleService.getListArticlesByTagListAndCategory(categoryId, listTags);
    }

    /*  FUNZIONA
        getMaxArticleId(): restituisce il numero id massimo
     */
    @GetMapping("/maxId")
    public Long getMaxArticleId() throws Exception {
        return articleService.getMaxId();
    }

    @GetMapping("/latest")
    public List<Article> getLatestNews() {
        return articleService.getLatestNews();
    }

//********************************************************************************************************
//METODI POST, PUT E DELETE

    /*  FUNZIONA
        createArticle(): crea un nuovo articolo
    */
    @PostMapping("/newArticle/")
    public ResponseEntity<?> createArticle(@RequestBody Map<String, Object> requestData) {
        try {
            // Estrai i dati dalla richiesta
            String title = (String) requestData.get("title");
            String body = (String) requestData.get("body");
            String srcImg = (String) requestData.get("srcImg");
            
            // Valida che il titolo non sia vuoto
            if (title == null || title.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Il titolo dell'articolo non può essere vuoto");
            }
            
            // Valida che il body non sia vuoto
            if (body == null || body.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Il contenuto dell'articolo non può essere vuoto");
            }
            
            // Crea l'articolo
            Article newArticle = new Article();
            newArticle.setTitle(title.trim());
            newArticle.setBody(body.trim());
            
            // Gestione data - per ora usiamo sempre l'ora corrente
            newArticle.setDate(LocalDateTime.now());
            
            // Gestione immagine
            if (srcImg != null && !srcImg.isEmpty()) {
                // Tronca l'URL dell'immagine se troppo lungo per evitare l'errore del database
                if (srcImg.length() > 65535) {
                    srcImg = srcImg.substring(0, 65535);
                }
                newArticle.setSrcImg(srcImg);
            }
            
            // Gestione categoria di default
            Category defaultCategory = categoryRepository.findByName("STOCKS")
                .orElseGet(() -> {
                    Category stocks = new Category();
                    stocks.setName("STOCKS");
                    return categoryRepository.save(stocks);
                });
            newArticle.setCategory(defaultCategory);
            
            // Gestione tag (se presenti)
            @SuppressWarnings("unchecked")
            List<String> tagNames = (List<String>) requestData.get("tags");
            if (tagNames != null && !tagNames.isEmpty()) {
                List<Tag> managedTags = new ArrayList<>();
                for (String tagName : tagNames) {
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
                newArticle.setTags(managedTags);
            }
            
            Article createdArticle = articleService.newArticle(newArticle);
            return ResponseEntity.ok(createdArticle);
            
        } catch (Exception e) {
            System.err.println("[ArticleController] Error creating article: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Errore durante la creazione dell'articolo: " + e.getMessage());
        }
    }

    /*  FUNZIONA
        updateArticle(): permette di modificare un articolo già presente nel db
     */
    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody Article updatedArticle) {
        return articleRepository.findById(id)
                .map(article -> {
                    article.setTitle(updatedArticle.getTitle());
                    article.setBody(updatedArticle.getBody());
                    article.setDate(updatedArticle.getDate());
                    article.setCategory(updatedArticle.getCategory());
                    article.setTags(updatedArticle.getTags());
                    article.setSrcImg(updatedArticle.getSrcImg());
                    Article savedArticle = articleRepository.save(article);
                    return ResponseEntity.ok(savedArticle);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /*  FUNZIONA
        deleteArticle(): permette di eliminare un articolo presente nel db
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        if (articleRepository.existsById(id)) {
            articleRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
