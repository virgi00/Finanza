package com.Finanza.backend.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Finanza.backend.entities.Category;
import com.Finanza.backend.repositories.CategoryRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

}
