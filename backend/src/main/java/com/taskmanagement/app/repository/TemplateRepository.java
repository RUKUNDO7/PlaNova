package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    List<Template> findByCreatedById(Long userId);
}
