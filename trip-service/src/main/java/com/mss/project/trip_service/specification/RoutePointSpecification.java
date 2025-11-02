package com.mss.project.trip_service.specification;

import com.mss.project.trip_service.entity.RoutePoint;
import org.springframework.data.jpa.domain.Specification;

public class RoutePointSpecification {

    // Search by name, description, and full address
    public static Specification<RoutePoint> hasSearchString(String searchString) {
        return (root, query, criteriaBuilder) -> {
            if (searchString == null || searchString.isEmpty()) {
                return criteriaBuilder.conjunction(); // No filter applied
            }
            String lowerCaseSearch = "%" + searchString.toLowerCase() + "%";
            return criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("fullAddress")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("province")), lowerCaseSearch),
                criteriaBuilder.like(criteriaBuilder.lower(root.get("district")), lowerCaseSearch)
            );
        };
    }

    public static Specification<RoutePoint> hasDeleted(boolean isDeleted) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isDeleted"), isDeleted);
    }

    public static Specification<RoutePoint> hasActive(boolean isActive) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("isActive"), isActive);
    }


}
