package com.mss.project.bus_service.specification;

import com.mss.project.bus_service.entity.BusCategory;
import org.springframework.data.jpa.domain.Specification;

public class BusCategorySpecification {
    public static Specification<BusCategory> hasSearchString(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isEmpty()) {
                return criteriaBuilder.conjunction(); // No filter applied
            }
            String lowerCaseSearch = "%" + searchString.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), lowerCaseSearch)
            );
        };
    }
}
