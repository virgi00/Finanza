package com.Finanza.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.Finanza.backend.entities.Tag;
import com.Finanza.backend.repositories.TagRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TagService {
    @Autowired
    private TagRepository tagRepository;

    @Transactional
    public void newTag(Tag tag) {
        tagRepository.save(tag);
    }

    public List<Tag> getAllTags(){
        return tagRepository.findAll();
    }

    public Tag getById(Long id) {
        return tagRepository.findTagById(id);
    }

    public Tag getByName(String name) {
        return tagRepository.findTagByName(name);
    }

    public void deleteTag(Long id) {
        tagRepository.deleteTagById(id);
    }
}
