package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    List<TaskAttachment> findByTaskIdOrderByUploadedAtDesc(Long taskId);
}