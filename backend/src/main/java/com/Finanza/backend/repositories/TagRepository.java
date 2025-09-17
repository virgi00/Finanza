package com.Finanza.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Finanza.backend.entities.Tag;

import java.util.List;
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Tag findTagById(Long id);
    Tag findTagByName(String name);
    void deleteTagById(Long id);
}
