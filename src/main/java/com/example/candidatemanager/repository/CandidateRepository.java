package com.example.candidatemanager.repository;

import com.example.candidatemanager.entity.Candidate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CandidateRepository extends JpaRepository<Candidate, Long>, JpaSpecificationExecutor<Candidate> {

    Optional<Candidate> findByEmailIgnoreCase(String email);
}
