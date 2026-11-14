package com.example.candidatemanager.controller;

import com.example.candidatemanager.dto.CandidateRequest;
import com.example.candidatemanager.dto.CandidatePageResponse;
import com.example.candidatemanager.dto.CandidateResponse;
import com.example.candidatemanager.entity.CandidateStatus;
import com.example.candidatemanager.service.CandidateService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.Set;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/candidates")
@RequiredArgsConstructor
@Slf4j
public class CandidateController {

    private static final Set<String> SORTABLE_PROPERTIES = Set.of(
            "id", "fullName", "email", "experienceYears", "location", "status", "createdAt", "updatedAt");

    private final CandidateService candidateService;

    @PostMapping
    public ResponseEntity<CandidateResponse> create(@Valid @RequestBody CandidateRequest request) {
        CandidateResponse response = candidateService.create(request);
        return ResponseEntity.created(URI.create("/api/v1/candidates/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public CandidateResponse getById(@PathVariable Long id) {
        return candidateService.getById(id);
    }

    @PutMapping("/{id}")
    public CandidateResponse update(@PathVariable Long id, @Valid @RequestBody CandidateRequest request) {
        return candidateService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        candidateService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public CandidatePageResponse search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> skill,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(required = false) Integer maxExperience,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) CandidateStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return CandidatePageResponse.from(
                candidateService.search(q, skill, minExperience, maxExperience, location, status, pageable));
    }

    private Sort.Order parseSort(String[] sort) {
        if (sort.length != 2
                || sort[0].isBlank()
                || !SORTABLE_PROPERTIES.contains(sort[0])
                || !(sort[1].equalsIgnoreCase("asc") || sort[1].equalsIgnoreCase("desc"))) {
            throw new IllegalArgumentException("sort must use the format property,asc or property,desc");
        }
        return new Sort.Order(Sort.Direction.fromString(sort[1]), sort[0]);
    }
}
