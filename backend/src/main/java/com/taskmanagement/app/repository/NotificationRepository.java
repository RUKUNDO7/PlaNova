package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    @Modifying
    @Query("delete from Notification n where n.task.id in :taskIds")
    int deleteByTaskIds(@Param("taskIds") Collection<Long> taskIds);
}