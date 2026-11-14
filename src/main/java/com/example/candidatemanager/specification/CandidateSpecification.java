package com.example.candidatemanager.specification;

import com.example.candidatemanager.entity.Candidate;
import com.example.candidatemanager.entity.CandidateStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public final class CandidateSpecification {

    private CandidateSpecification() {
    }

    public static Specification<Candidate> filter(
            String keyword,
            List<String> skills,
            Integer minExperience,
            Integer maxExperience,
            String location,
            CandidateStatus status
    ) {
        Specification<Candidate> specification = (root, query, builder) -> builder.conjunction();
        if (!isBlank(keyword)) {
            specification = specification.and(hasKeyword(keyword));
        }
        if (hasValues(skills)) {
            specification = specification.and(hasAnySkill(skills));
        }
        if (minExperience != null) {
            specification = specification.and(hasMinimumExperience(minExperience));
        }
        if (maxExperience != null) {
            specification = specification.and(hasMaximumExperience(maxExperience));
        }
        if (!isBlank(location)) {
            specification = specification.and(hasLocation(location));
        }
        if (status != null) {
            specification = specification.and(hasStatus(status));
        }
        return specification;
    }

    private static Specification<Candidate> hasKeyword(String keyword) {
        if (isBlank(keyword)) {
            return null;
        }
        String pattern = containsPattern(keyword);
        return (root, query, builder) -> builder.or(
                builder.like(builder.lower(root.get("fullName")), pattern),
                builder.like(builder.lower(root.get("email")), pattern),
                builder.like(builder.lower(root.get("skills")), pattern)
        );
    }

    private static Specification<Candidate> hasAnySkill(List<String> skills) {
        return (root, query, builder) -> {
            List<Predicate> skillPredicates = skills.stream()
                    .filter(skill -> !isBlank(skill))
                    .map(skill -> builder.like(builder.lower(root.get("skills")), containsPattern(skill)))
                    .toList();
            return builder.or(skillPredicates.toArray(Predicate[]::new));
        };
    }

    private static Specification<Candidate> hasMinimumExperience(Integer minExperience) {
        if (minExperience == null) {
            return null;
        }
        return (root, query, builder) -> builder.greaterThanOrEqualTo(root.get("experienceYears"), minExperience);
    }

    private static Specification<Candidate> hasMaximumExperience(Integer maxExperience) {
        if (maxExperience == null) {
            return null;
        }
        return (root, query, builder) -> builder.lessThanOrEqualTo(root.get("experienceYears"), maxExperience);
    }

    private static Specification<Candidate> hasLocation(String location) {
        if (isBlank(location)) {
            return null;
        }
        return (root, query, builder) -> builder.equal(
                builder.lower(root.get("location")), location.trim().toLowerCase());
    }

    private static Specification<Candidate> hasStatus(CandidateStatus status) {
        if (status == null) {
            return null;
        }
        return (root, query, builder) -> builder.equal(root.get("status"), status);
    }

    private static String containsPattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static boolean hasValues(List<String> values) {
        return values != null && values.stream().anyMatch(value -> !isBlank(value));
    }
}
