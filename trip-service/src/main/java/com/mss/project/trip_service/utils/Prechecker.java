package com.mss.project.trip_service.utils;

public class Prechecker {
    public static final int MAX_PAGE_SIZE = 200;

    public static void checkValidPageSize(int pageSize) {
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new RuntimeException("Page size must be between 0 and " + MAX_PAGE_SIZE);
        }
    }
    public static void checkPageNumber(int pageNumber) {
        if (pageNumber < 0) {
            throw new RuntimeException("Page number must be greater than or equal to 0");
        }
    }
    public static void checkPaginationParameters(int pageNumber, int pageSize) {
        checkPageNumber(pageNumber);
        checkValidPageSize(pageSize);
    }
}
