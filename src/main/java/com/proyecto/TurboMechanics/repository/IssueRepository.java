package com.proyecto.TurboMechanics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.Issue;
import com.proyecto.TurboMechanics.enums.StatusIssue;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

    List<Issue> findByWorkOrderId(Long workOrderId);

    List<Issue> findByWorkOrderIdAndStatus(Long workOrderId, StatusIssue status);
}