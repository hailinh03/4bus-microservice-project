package com.mss.project.trip_service.service;

import com.mss.project.trip_service.dto.request.RoutePointCreateRequest;
import com.mss.project.trip_service.dto.response.RoutePointDTO;
import com.mss.project.trip_service.dto.response.RoutePointListDTO;
import com.mss.project.trip_service.enums.RoutePointSortField;
import org.springframework.data.domain.Sort;

public interface IRoutePointService {
    /**
     * Creates a new route point based on the provided request.
     *
     * @param routePointCreateRequest the request containing details for the new route point
     * @return the created RoutePointDTO
     */
    RoutePointDTO createRoutePoint(RoutePointCreateRequest routePointCreateRequest);

    /**
     * Retrieves all route points with optional filtering, pagination, and sorting.
     *
     * @param searchString the search string to filter route points (optional)
     * @param page the page number for pagination (default is 0)
     * @param size the size of each page (default is 10)
     * @param sortBy the field to sort by (default is CREATED_AT)
     * @param sortDir the direction of sorting (default is DESC)
     * @param isDeleted flag to include deleted route points (default is false)
     * @return a RoutePointListDTO containing the list of route points
     */
    RoutePointListDTO getAllRoutePoints(String searchString, Integer page, Integer size, RoutePointSortField sortBy, Sort.Direction sortDir,
                                        boolean isDeleted);

    /**
     * Retrieves a route point by its ID.
     * @param id the ID of the route point to retrieve
     * @return the RoutePointDTO corresponding to the given ID
     */
    RoutePointDTO getRoutePointById(int id);

    /**
     * Updates an existing route point based on the provided request.
     *
     * @param id the ID of the route point to update
     * @param routePointCreateRequest the request containing updated details for the route point
     */
    void updateRoutePoint(int id, RoutePointCreateRequest routePointCreateRequest);

    /**
     * Deletes a route point by its ID.
     *
     * @param id the ID of the route point to delete
     */
    void deleteRoutePoint(int id);
}
