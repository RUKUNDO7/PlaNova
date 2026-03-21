package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.CustomFieldValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomFieldValueRepository extends JpaRepository<CustomFieldValue, Long> {
    List<CustomFieldValue> findByTaskId(Long taskId);
    Optional<CustomFieldValue> findByTaskIdAndDefinitionId(Long taskId, Long definitionId);
    long deleteByDefinitionId(Long definitionId);
}
