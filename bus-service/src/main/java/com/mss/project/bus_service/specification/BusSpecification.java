package com.mss.project.bus_service.specification;

import com.mss.project.bus_service.entity.Bus;
import com.mss.project.bus_service.entity.BusCategory;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

public class BusSpecification {
    public static Specification<Bus> hasSearchString(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isEmpty()) {
                return criteriaBuilder.conjunction(); // No filter applied
            }
            Join<Bus, BusCategory> categoryJoin = root.join("category", JoinType.LEFT);

            String lowerCaseSearch = "%" + searchString.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("color")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("plateNumber")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get("name")), lowerCaseSearch)
            );
        };
    }
    public static Specification<Bus> withCategoryJoin() {
        return (root, query, criteriaBuilder) -> {
            root.fetch("category", JoinType.LEFT);
            return criteriaBuilder.conjunction();
        };
    }
}
