package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Webhook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WebhookRepository extends JpaRepository<Webhook, Long> {
    List<Webhook> findByProjectIdAndActiveTrue(Long projectId);
}
