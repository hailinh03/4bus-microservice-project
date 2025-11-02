package com.mss.project.trip_service.specification;

import com.mss.project.trip_service.entity.Route;
import org.springframework.data.jpa.domain.Specification;

public class RouteSpecification {
    public static Specification<Route> hasSearchString(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + searchString.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("origin")), "%" + searchString.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("destination")), "%" + searchString.toLowerCase() + "%")
            );
        };
    }

    public static Specification<Route> hasDeleted(boolean deleted) {
        return (root, query, criteriaBuilder) -> {
            if (deleted) {
                return criteriaBuilder.isTrue(root.get("isDeleted"));
            } else {
                return criteriaBuilder.isFalse(root.get("isDeleted"));
            }
        };
    }
    public static Specification<Route> hasActive(boolean isActive) {
        return (root, query, criteriaBuilder) -> {
            if (isActive) {
                return criteriaBuilder.isTrue(root.get("isActive"));
            } else {
                return criteriaBuilder.isFalse(root.get("isActive"));
            }
        };
    }

}
