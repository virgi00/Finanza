package com.Finanza.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Finanza.backend.entities.Tag;
import com.Finanza.backend.services.TagService;

import java.util.List;


@RestController
@CrossOrigin()
@RequestMapping("/tags")
public class TagController {
    @Autowired
    private TagService tagService;

//********************************************************************************************************
//METODI GET

    /*  FUNZIONA
        getAllTags(): metodo che restituisce tutti i tag esistenti
     */
    @GetMapping(value="/all", produces = "application/json")
    public ResponseEntity getAllTags() throws Exception {
        List<Tag> taglist = tagService.getAllTags();
        if (taglist.isEmpty())
            throw new Exception("[X]-- ERROR: There is no tags in the database!");
        else {
            return new ResponseEntity(taglist, HttpStatus.OK);
        }
    }

    /*  FUNZIONA
        getTagById(): metodo che, dato un id, restituisce il tag corrispondente
     */
    @GetMapping("/searchById")
    public ResponseEntity getTagById(@RequestParam Long id) throws Exception {
        Tag tag = tagService.getById(id);
        if (tag == null) {
            throw new Exception("[X]-- ERROR: No id Tag was found!");
        } else {
            return new ResponseEntity(tag, HttpStatus.OK);
        }
    }

    /*  FUNZIONA
        getTagByName(): metodo che, dato un nome, restituisce il tag corrispondente
     */
    @GetMapping("/searchByName")
    public ResponseEntity getTagByName(@RequestParam String name) throws Exception {
        Tag tag = tagService.getByName(name);
        if(tag == null) {
            throw new Exception("[X]-- ERROR: No id Tag was found!");
        } else {
            return new ResponseEntity(tag, HttpStatus.OK);
        }
    }

//********************************************************************************************************
//METODI POST, PUT E DELETE

    /*  FUNZIONA
        createTag(): metodo che, dato un nome, crea un tag
     */
    @PostMapping("/newTag")
    public ResponseEntity createTag(@RequestBody String tagName) {
        Tag tag = new Tag();
        tag.setName(tagName);
        tagService.newTag(tag);
        return new ResponseEntity(new HttpHeaders(),HttpStatus.CREATED);
    }

    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long tagId) {
        tagService.deleteTag(tagId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
