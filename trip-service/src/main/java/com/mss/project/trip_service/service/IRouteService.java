package com.mss.project.trip_service.service;

import com.mss.project.trip_service.dto.request.RouteCreateRequest;
import com.mss.project.trip_service.dto.response.RouteDTO;
import com.mss.project.trip_service.dto.response.RouteListDTO;
import com.mss.project.trip_service.enums.RouteSortField;
import org.springframework.data.domain.Sort;

public interface IRouteService {

    /**
     * Retrieves a paginated list of routes based on search criteria.
     *
     * @param searchString the search string to filter routes by name, origin, or destination
     * @param page the page number to retrieve
     * @param size the number of routes per page
     * @param sortBy the field to sort by
     * @param sortDir the direction of sorting (ascending or descending)
     * @param isDeleted flag to indicate whether to include deleted routes
     * @return a RouteListDTO containing the list of routes and pagination information
     */
    RouteListDTO getRoutes(
            String searchString, Integer page, Integer size, RouteSortField sortBy, Sort.Direction sortDir,
            boolean isDeleted
    );

    /**
     * Creates a new route based on the provided request.
     *
     * @param routeCreateRequest the request containing route creation details
     * @return a RouteDTO representing the created route
     */
    RouteDTO createRoute(RouteCreateRequest routeCreateRequest);

    /** Update an existing route.
     *
     * @param id ID of the route to be updated
     * @param routeCreateRequest request object containing updated route details
     */
    void updateRoute(int id, RouteCreateRequest routeCreateRequest);

    /** Delete a route by its ID.
     *
     * @param id ID of the route to be deleted
     */
    void deleteRoute(int id);
}
