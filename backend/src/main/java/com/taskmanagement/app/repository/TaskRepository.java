package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Task;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @Query("select t.id from Task t where t.completed = true")
    List<Long> findIdsByCompletedTrue();

    @Query("select t.id from Task t where t.completed = true and t.owner.id = :ownerId")
    List<Long> findIdsByCompletedTrueAndOwnerId(@Param("ownerId") Long ownerId);

    @Modifying
    @Query("update Task t set t.completed = false where t.completed = true")
    int deleteByCompletedTrue();

    @Modifying
    @Query("update Task t set t.completed = false where t.completed = true and t.owner.id = :ownerId")
    int deleteByCompletedTrueAndOwnerId(@Param("ownerId") Long ownerId);

    @EntityGraph(attributePaths = {
        "owner",
        "column",
        "column.board",
        "column.board.project",
        "labels",
        "assignees",
        "subtasks",
        "dependencies",
        "sprint",
        "sprint.board"
    })
    Optional<Task> findWithAssociationsById(Long id);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.owner.id = :ownerId AND t.completed = false")
    long countActiveTasksByOwnerId(@Param("ownerId") Long ownerId);

    long countByCompletedTrue();
    List<Task> findByColumnIsNull();
}