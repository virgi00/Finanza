package com.Finanza.backend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Finanza.backend.entities.Category;
import com.Finanza.backend.services.CategoryService;

import java.util.List;

@RestController
@CrossOrigin()
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /*FUNZIONA
        getAllCategory(): metodo che restituisce una lista di tutte le categorie
     */
    @GetMapping(value="/all", produces = "application/json")
    public ResponseEntity getAllCategory () throws Exception {
        List<Category> listCategory = categoryService.getAll();
        if (listCategory == null)
            throw new Exception("[X]-- ERROR: No Article was published!");
        else {
            return new ResponseEntity(listCategory, HttpStatus.OK);
        }
    }
}
