package com.taskmanagement.app.repository;

import com.taskmanagement.app.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findDistinctByMembers_IdOrOwner_Id(Long memberId, Long ownerId);
}