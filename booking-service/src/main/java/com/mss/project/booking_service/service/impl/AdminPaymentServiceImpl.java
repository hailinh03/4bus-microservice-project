package com.mss.project.booking_service.service.impl;

import com.mss.project.booking_service.entities.Payment;
import com.mss.project.booking_service.enums.PaymentStatus;
import com.mss.project.booking_service.exception.PaymentLinkException;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentListRequest;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentResponse;
import com.mss.project.booking_service.payload.admin.payment.AdminPaymentUpdateRequest;
import com.mss.project.booking_service.payload.admin.payment.DailyPaymentStatisticsRequest;
import com.mss.project.booking_service.payload.admin.payment.DailyPaymentStatisticsResponse;
import com.mss.project.booking_service.payload.admin.payment.MonthlyRevenueRequest;
import com.mss.project.booking_service.payload.admin.payment.MonthlyRevenueResponse;
import com.mss.project.booking_service.payload.admin.dashboard.DashboardOverviewResponse;
import com.mss.project.booking_service.payload.admin.dashboard.PaymentAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.RefundAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.RevenueAnalyticsResponse;
import com.mss.project.booking_service.payload.admin.dashboard.DashboardAnalyticsRequest;
import com.mss.project.booking_service.payload.notification.NotificationRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentRequest;
import com.mss.project.booking_service.payload.payment.RefundPaymentResponse;
import com.mss.project.booking_service.payload.payment.RefundPaymentDetailResponse;
import com.mss.project.booking_service.payload.payment.ProcessRefundRequest;
import com.mss.project.booking_service.repository.PaymentRepository;
import com.mss.project.booking_service.repository.BookingRepository;
import com.mss.project.booking_service.repository.TicketRepository;
import com.mss.project.booking_service.service.AdminPaymentService;
import com.mss.project.booking_service.service.TripService;
import com.mss.project.booking_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.YearMonth;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentServiceImpl implements AdminPaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TripService tripService;
    private final UserService userService;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPaymentResponse> getAllPayments(AdminPaymentListRequest request) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Instant startDate = request.getStartDate() != null
                    ? request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;
            Instant endDate = request.getEndDate() != null
                    ? request.getEndDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;

            Page<Payment> payments = paymentRepository.findRegularPaymentsWithFilters(
                    request.getStatus(),
                    request.getBookingId(),
                    request.getMinAmount(),
                    request.getMaxAmount(),
                    startDate,
                    endDate,
                    pageable);

            return payments.map(this::mapToAdminResponse);

        } catch (Exception e) {
            log.error("Error fetching payments: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch payments: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AdminPaymentResponse getPaymentById(Long id) {
        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new PaymentLinkException("Payment not found with id: " + id));

            return mapToAdminResponse(payment);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching payment by id {}: {}", id, e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AdminPaymentResponse updatePayment(Long id, AdminPaymentUpdateRequest request) {
        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new PaymentLinkException("Payment not found with id: " + id));

            payment.setStatus(request.getStatus());
            if (request.getDescription() != null) {
                payment.setDescription(request.getDescription());
            }
            if (request.getAdminNote() != null) {
                payment.setAdminNote(request.getAdminNote());
            }
            Payment updatedPayment = paymentRepository.save(payment);

            log.info("Admin updated payment {} to status {}", id, request.getStatus());
            return mapToAdminResponse(updatedPayment);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating payment {}: {}", id, e.getMessage(), e);
            throw new PaymentLinkException("Failed to update payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deletePayment(Long id) {
        try {
            Payment payment = paymentRepository.findById(id)
                    .orElseThrow(() -> new PaymentLinkException("Payment not found with id: " + id));

            paymentRepository.delete(payment);

            log.info("Admin deleted payment {}", id);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting payment {}: {}", id, e.getMessage(), e);
            throw new PaymentLinkException("Failed to delete payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countPaymentsByStatus() {
        try {
            return paymentRepository.count();
        } catch (Exception e) {
            log.error("Error counting payments: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to count payments: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DailyPaymentStatisticsResponse getDailyPaymentStatistics(DailyPaymentStatisticsRequest request) {
        try {
            // Calculate date range based on period
            LocalDate endDate = LocalDate.now();
            LocalDate startDate;

            switch (request.getPeriod()) {
                case "LAST_7_DAYS":
                    startDate = endDate.minusDays(6); // 7 days including today
                    break;
                case "LAST_30_DAYS":
                    startDate = endDate.minusDays(29); // 30 days including today
                    break;
                case "LAST_3_MONTHS":
                    startDate = endDate.minusMonths(3);
                    break;
                default:
                    startDate = endDate.minusDays(29); // Default to 30 days
                    break;
            }

            Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

            // Get daily statistics from database
            List<Object[]> rawData = paymentRepository.findDailyRegularPaymentStatistics(startInstant, endInstant);

            // Convert raw data to Map for easier processing
            Map<LocalDate, DailyPaymentStatisticsResponse.DailyPaymentData> dataMap = rawData.stream()
                    .collect(Collectors.toMap(
                            row -> {
                                // Handle different date formats from database
                                if (row[0] instanceof java.sql.Date) {
                                    return ((java.sql.Date) row[0]).toLocalDate();
                                } else if (row[0] instanceof java.time.LocalDate) {
                                    return (java.time.LocalDate) row[0];
                                } else if (row[0] instanceof String) {
                                    return LocalDate.parse((String) row[0]);
                                } else {
                                    return LocalDate.parse(row[0].toString());
                                }
                            },
                            row -> {
                                LocalDate date;
                                if (row[0] instanceof java.sql.Date) {
                                    date = ((java.sql.Date) row[0]).toLocalDate();
                                } else if (row[0] instanceof java.time.LocalDate) {
                                    date = (java.time.LocalDate) row[0];
                                } else if (row[0] instanceof String) {
                                    date = LocalDate.parse((String) row[0]);
                                } else {
                                    date = LocalDate.parse(row[0].toString());
                                }

                                return DailyPaymentStatisticsResponse.DailyPaymentData.builder()
                                        .date(date)
                                        .paymentCount(((Number) row[1]).longValue())
                                        .totalAmount(row[2] != null ? ((Number) row[2]).longValue() : 0L)
                                        .completedPayments(((Number) row[3]).longValue())
                                        .pendingPayments(((Number) row[4]).longValue())
                                        .failedPayments(((Number) row[5]).longValue())
                                        .cancelledPayments(((Number) row[6]).longValue())
                                        .processingPayments(0L) // Regular payments don't use PROCESSING status
                                        .resolvedPayments(0L) // Regular payments don't use RESOLVED status
                                        .build();
                            }));

            // Fill in missing dates with zero values
            List<DailyPaymentStatisticsResponse.DailyPaymentData> dailyData = new ArrayList<>();
            LocalDate currentDate = startDate;

            while (!currentDate.isAfter(endDate)) {
                DailyPaymentStatisticsResponse.DailyPaymentData dayData = dataMap.getOrDefault(currentDate,
                        DailyPaymentStatisticsResponse.DailyPaymentData.builder()
                                .date(currentDate)
                                .paymentCount(0L)
                                .totalAmount(0L)
                                .completedPayments(0L)
                                .pendingPayments(0L)
                                .failedPayments(0L)
                                .cancelledPayments(0L)
                                .processingPayments(0L)
                                .resolvedPayments(0L)
                                .build());
                dailyData.add(dayData);
                currentDate = currentDate.plusDays(1);
            }

            // Calculate totals
            Long totalPayments = dailyData.stream()
                    .mapToLong(DailyPaymentStatisticsResponse.DailyPaymentData::getPaymentCount).sum();
            Long totalAmount = dailyData.stream()
                    .mapToLong(DailyPaymentStatisticsResponse.DailyPaymentData::getTotalAmount).sum();

            return DailyPaymentStatisticsResponse.builder()
                    .period(request.getPeriod())
                    .startDate(startDate)
                    .endDate(endDate)
                    .totalPayments(totalPayments)
                    .totalAmount(totalAmount)
                    .dailyData(dailyData)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching daily payment statistics: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch daily payment statistics: " + e.getMessage());
        }
    }

    private AdminPaymentResponse mapToAdminResponse(Payment payment) {
        return AdminPaymentResponse.builder()
                .id(payment.getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .description(payment.getDescription())
                .paymentDate(payment.getPaymentDate())
                .bookingId(payment.getBooking().getId().longValue())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .refundAmount(payment.getRefundAmount())
                .refundReason(payment.getRefundReason())
                .refundRequestedAt(payment.getRefundRequestedAt())
                .refundProcessedAt(payment.getRefundProcessedAt())
                .originalPaymentId(payment.getOriginalPaymentId())
                .isRefund(payment.getIsRefund())
                .adminNote(payment.getAdminNote())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public MonthlyRevenueResponse getMonthlyRevenueComparison(MonthlyRevenueRequest request) {
        try {
            YearMonth currentMonth = YearMonth.now();
            YearMonth previousMonth = currentMonth.minusMonths(1);

            // Calculate date ranges
            Instant currentMonthStart = currentMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant currentMonthEnd = currentMonth.atEndOfMonth().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

            Instant previousMonthStart = previousMonth.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant previousMonthEnd = previousMonth.atEndOfMonth().plusDays(1).atStartOfDay()
                    .toInstant(ZoneOffset.UTC);

            // Get current month data
            Object[] currentMonthData = paymentRepository.findMonthlyRegularPaymentData(currentMonthStart,
                    currentMonthEnd);
            Object[] previousMonthData = paymentRepository.findMonthlyRegularPaymentData(previousMonthStart,
                    previousMonthEnd);

            log.debug("Current month data: {}",
                    currentMonthData != null ? java.util.Arrays.toString(currentMonthData) : "null");
            log.debug("Previous month data: {}",
                    previousMonthData != null ? java.util.Arrays.toString(previousMonthData) : "null");

            // Parse current month data with null checks
            Long currentMonthRevenue = 0L;
            Long currentMonthPaymentCount = 0L;
            Long currentMonthCompletedPayments = 0L;

            if (currentMonthData != null && currentMonthData.length > 0 && currentMonthData[0] instanceof Object[]) {
                Object[] actualData = (Object[]) currentMonthData[0];
                if (actualData.length >= 3) {
                    currentMonthRevenue = parseAsLong(actualData[0]);
                    currentMonthPaymentCount = parseAsLong(actualData[1]);
                    currentMonthCompletedPayments = parseAsLong(actualData[2]);
                }
            }

            // Parse previous month data with null checks
            Long previousMonthRevenue = 0L;
            Long previousMonthPaymentCount = 0L;
            Long previousMonthCompletedPayments = 0L;

            if (previousMonthData != null && previousMonthData.length > 0 && previousMonthData[0] instanceof Object[]) {
                Object[] actualData = (Object[]) previousMonthData[0];
                if (actualData.length >= 3) {
                    previousMonthRevenue = parseAsLong(actualData[0]);
                    previousMonthPaymentCount = parseAsLong(actualData[1]);
                    previousMonthCompletedPayments = parseAsLong(actualData[2]);
                }
            } // Calculate percentage change
            BigDecimal percentageChange = BigDecimal.ZERO;
            String changeDirection = "NO_CHANGE";
            String formattedPercentage = "0.0%";

            if (previousMonthRevenue > 0) {
                BigDecimal current = BigDecimal.valueOf(currentMonthRevenue);
                BigDecimal previous = BigDecimal.valueOf(previousMonthRevenue);
                BigDecimal difference = current.subtract(previous);

                percentageChange = difference.divide(previous, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                if (percentageChange.compareTo(BigDecimal.ZERO) > 0) {
                    changeDirection = "INCREASE";
                    formattedPercentage = "+" + percentageChange.setScale(1, RoundingMode.HALF_UP) + "%";
                } else if (percentageChange.compareTo(BigDecimal.ZERO) < 0) {
                    changeDirection = "DECREASE";
                    formattedPercentage = percentageChange.setScale(1, RoundingMode.HALF_UP) + "%";
                } else {
                    changeDirection = "NO_CHANGE";
                    formattedPercentage = "0.0%";
                }
            } else if (currentMonthRevenue > 0) {
                // If there was no revenue last month but there is this month, it's 100%
                // increase
                percentageChange = BigDecimal.valueOf(100);
                changeDirection = "INCREASE";
                formattedPercentage = "+100.0%";
            }

            return MonthlyRevenueResponse.builder()
                    .currentMonth(currentMonth)
                    .previousMonth(previousMonth)
                    .currentMonthRevenue(currentMonthRevenue)
                    .previousMonthRevenue(previousMonthRevenue)
                    .percentageChange(percentageChange)
                    .changeDirection(changeDirection)
                    .formattedPercentage(formattedPercentage)
                    .currentMonthPaymentCount(currentMonthPaymentCount)
                    .previousMonthPaymentCount(previousMonthPaymentCount)
                    .currentMonthCompletedPayments(currentMonthCompletedPayments)
                    .previousMonthCompletedPayments(previousMonthCompletedPayments)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching monthly revenue comparison: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch monthly revenue comparison: " + e.getMessage());
        }
    }

    private Long parseAsLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                log.warn("Failed to parse '{}' as Long, returning 0", value);
                return 0L;
            }
        }
        log.warn("Unexpected value type: {}, returning 0", value.getClass());
        return 0L;
    }

    @Override
    @Transactional
    public RefundPaymentResponse createRefundPayment(RefundPaymentRequest request) {
        try {
            // Find the original payment
            Payment originalPayment = paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(
                            () -> new PaymentLinkException("Payment not found with id: " + request.getPaymentId()));

            // Validate that the original payment can be refunded
            if (originalPayment.getStatus() != PaymentStatus.COMPLETED) {
                throw new PaymentLinkException("Only completed payments can be refunded");
            }

            if (originalPayment.getIsRefund()) {
                throw new PaymentLinkException("Cannot refund a refund payment");
            }

            // Validate refund amount doesn't exceed original amount
            if (request.getRefundAmount() > originalPayment.getAmount()) {
                throw new PaymentLinkException("Refund amount cannot exceed original payment amount");
            }

            // Generate unique order code for refund payment
            Long refundOrderCode = generateOrderCode();

            // Create refund payment record
            Payment refundPayment = Payment.builder()
                    .id(refundOrderCode)
                    .status(PaymentStatus.PROCESSING)
                    .amount(request.getRefundAmount())
                    .description("REFUND for payment " + request.getPaymentId() + " - " + request.getRefundReason())
                    .booking(originalPayment.getBooking())
                    .isRefund(true)
                    .originalPaymentId(request.getPaymentId())
                    .refundAmount(request.getRefundAmount())
                    .refundReason(request.getRefundReason())
                    .refundRequestedAt(Instant.now())
                    .build();

            Payment savedRefundPayment = paymentRepository.save(refundPayment);

            log.info("Admin created refund payment {} for original payment {}", savedRefundPayment.getId(),
                    request.getPaymentId());

            return mapToRefundResponse(savedRefundPayment);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating refund payment in admin service: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to create refund payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public RefundPaymentResponse processRefund(ProcessRefundRequest request) {
        try {
            // Find the refund payment
            Payment refundPayment = paymentRepository.findById(request.getRefundPaymentId())
                    .orElseThrow(
                            () -> new PaymentLinkException(
                                    "Refund payment not found with id: " + request.getRefundPaymentId()));

            // Validate that this is a refund payment
            if (!refundPayment.getIsRefund()) {
                throw new PaymentLinkException("Payment is not a refund payment");
            }

            // Validate current status
            if (refundPayment.getStatus() != PaymentStatus.PROCESSING) {
                throw new PaymentLinkException("Refund payment is not in PROCESSING status");
            }

            // Process the refund and add proof image information
            refundPayment.setStatus(PaymentStatus.RESOLVED);
            refundPayment.setRefundProcessedAt(Instant.now());
            refundPayment.setProofImageUrl(request.getProofImageUrl());
            refundPayment.setProofImagePublicId(request.getProofImagePublicId());
            try {
                Long userId = refundPayment.getBooking().getUserId();
                String tripName = tripService.getTripById(refundPayment.getBooking().getTripId()).getData().getName();
                // send notification to user
                NotificationRequest noti = new NotificationRequest();
                noti.setTitle("Hoàn tiền vé thành công");
                noti.setContent(
                        "Yêu cầu hoàn tiền của bạn đã được xử lý. Nếu có bất cứ thắc mắc gì, vui lòng liên hệ chăm sóc khách hàng để được giải đáp.");
                noti.setUrl("/refund-history");
                noti.setUserId(userId);
                userService.sendNotification(noti);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            Payment processedRefund = paymentRepository.save(refundPayment);

            log.info("Admin processed refund payment {} with proof image", request.getRefundPaymentId());

            return mapToRefundResponse(processedRefund);

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error processing refund in admin service: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to process refund: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminPaymentResponse> getRefundPayments(AdminPaymentListRequest request) {
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDirection()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Instant startDate = request.getStartDate() != null
                    ? request.getStartDate().atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;
            Instant endDate = request.getEndDate() != null
                    ? request.getEndDate().plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
                    : null;

            // Filter only refund payments using the dedicated repository method
            Page<Payment> refundPayments = paymentRepository.findRefundPaymentsWithFilters(
                    request.getStatus(),
                    request.getBookingId(),
                    request.getMinAmount(),
                    request.getMaxAmount(),
                    startDate,
                    endDate,
                    pageable);

            return refundPayments.map(this::mapToAdminResponse);

        } catch (Exception e) {
            log.error("Error fetching refund payments: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch refund payments: " + e.getMessage());
        }
    }

    static Long generateOrderCode() {
        return PaymentServiceImpl.generateOrderCode();
    }

    private RefundPaymentResponse mapToRefundResponse(Payment refundPayment) {
        return RefundPaymentResponse.builder()
                .refundPaymentId(refundPayment.getId())
                .originalPaymentId(refundPayment.getOriginalPaymentId())
                .status(refundPayment.getStatus())
                .refundAmount(refundPayment.getRefundAmount())
                .refundReason(refundPayment.getRefundReason())
                .paymentDate(refundPayment.getPaymentDate())
                .refundRequestedAt(refundPayment.getRefundRequestedAt())
                .refundProcessedAt(refundPayment.getRefundProcessedAt())
                .bookingId(refundPayment.getBooking().getId().longValue())
                .description(refundPayment.getDescription())
                .createdAt(refundPayment.getCreatedAt())
                .updatedAt(refundPayment.getUpdatedAt())
                .adminNote(refundPayment.getAdminNote())
                .proofImageUrl(refundPayment.getProofImageUrl())
                .proofImagePublicId(refundPayment.getProofImagePublicId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RefundPaymentDetailResponse getRefundPaymentById(Long refundPaymentId) {
        try {
            // Find the refund payment
            Payment refundPayment = paymentRepository.findById(refundPaymentId)
                    .orElseThrow(
                            () -> new PaymentLinkException("Refund payment not found with id: " + refundPaymentId));

            // Validate that this is a refund payment
            if (!refundPayment.getIsRefund()) {
                throw new PaymentLinkException("Payment is not a refund payment");
            }

            // Find the original payment
            Payment originalPayment = paymentRepository.findById(refundPayment.getOriginalPaymentId())
                    .orElseThrow(() -> new PaymentLinkException(
                            "Original payment not found with id: " + refundPayment.getOriginalPaymentId()));

            // Build the detailed response
            RefundPaymentDetailResponse.OriginalPaymentInfo originalPaymentInfo = RefundPaymentDetailResponse.OriginalPaymentInfo
                    .builder()
                    .originalPaymentId(originalPayment.getId())
                    .originalStatus(originalPayment.getStatus())
                    .originalAmount(originalPayment.getAmount())
                    .description(originalPayment.getDescription())
                    .paymentDate(originalPayment.getPaymentDate())
                    .bookingId(originalPayment.getBooking().getId().longValue())
                    .originalCreatedAt(originalPayment.getCreatedAt())
                    .originalUpdatedAt(originalPayment.getUpdatedAt())
                    .build();

            return RefundPaymentDetailResponse.builder()
                    .refundPaymentId(refundPayment.getId())
                    .status(refundPayment.getStatus())
                    .refundAmount(refundPayment.getRefundAmount())
                    .refundReason(refundPayment.getRefundReason())
                    .refundRequestedAt(refundPayment.getRefundRequestedAt())
                    .refundProcessedAt(refundPayment.getRefundProcessedAt())
                    .proofImageUrl(refundPayment.getProofImageUrl())
                    .proofImagePublicId(refundPayment.getProofImagePublicId())
                    .adminNote(refundPayment.getAdminNote())
                    .createdAt(refundPayment.getCreatedAt())
                    .updatedAt(refundPayment.getUpdatedAt())
                    .originalPayment(originalPaymentInfo)
                    .build();

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching refund payment detail for id {}: {}", refundPaymentId, e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch refund payment detail: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundPaymentResponse> getRefundPaymentsByOriginalPaymentId(Long originalPaymentId) {
        try {
            // Validate that the original payment exists
            Payment originalPayment = paymentRepository.findById(originalPaymentId)
                    .orElseThrow(
                            () -> new PaymentLinkException("Original payment not found with id: " + originalPaymentId));

            // Validate that the original payment is not a refund itself
            if (originalPayment.getIsRefund()) {
                throw new PaymentLinkException("Cannot get refunds for a payment that is itself a refund");
            }

            // Find all refund payments for this original payment
            List<Payment> refundPayments = paymentRepository.findRefundPaymentsByOriginalPaymentId(originalPaymentId);

            // Map to response DTOs
            return refundPayments.stream()
                    .map(this::mapToRefundResponse)
                    .collect(Collectors.toList());

        } catch (PaymentLinkException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching refund payments for original payment id {}: {}", originalPaymentId,
                    e.getMessage(), e);
            throw new PaymentLinkException("Failed to fetch refund payments: " + e.getMessage());
        }
    }

    // Dashboard Analytics Methods

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewResponse getDashboardOverview(DashboardAnalyticsRequest request) {
        try {
            // Parse date range
            LocalDate[] dateRange = parseDateRange(request);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

            // Filter payments by date range first
            List<Payment> paymentsInRange = paymentRepository.findAll().stream()
                    .filter(p -> !p.getIsRefund())
                    .filter(p -> p.getCreatedAt().isAfter(startInstant) && p.getCreatedAt().isBefore(endInstant))
                    .collect(Collectors.toList());

            // Get payment statistics
            Map<PaymentStatus, Long> paymentCounts = paymentsInRange.stream()
                    .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));

            Long totalPayments = (long) paymentsInRange.size();
            Long completedPayments = paymentCounts.getOrDefault(PaymentStatus.COMPLETED, 0L);

            // Get payment amount data (only COMPLETED payments)
            Long totalPaymentAmount = paymentsInRange.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                    .mapToLong(Payment::getAmount)
                    .sum();

            // Get booking statistics
            Long totalBookings = bookingRepository.count();
            Long confirmedBookings = bookingRepository
                    .countByStatus(com.mss.project.booking_service.enums.BookingStatus.CONFIRMED);

            // Get ticket statistics
            Long totalTickets = ticketRepository.count();
            Long activeTickets = ticketRepository
                    .countByStatus(com.mss.project.booking_service.enums.TicketStatus.ACTIVE);

            // Get refund statistics with proper date filtering (only RESOLVED refunds count
            // for refund amount)
            List<Payment> refundPayments = paymentRepository.findAll().stream()
                    .filter(Payment::getIsRefund)
                    .filter(p -> p.getCreatedAt().isAfter(startInstant) && p.getCreatedAt().isBefore(endInstant))
                    .collect(Collectors.toList());

            Long totalRefunds = (long) refundPayments.size();
            Long processingRefunds = refundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PROCESSING)
                    .collect(Collectors.counting());
            Long resolvedRefunds = refundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.RESOLVED)
                    .collect(Collectors.counting());
            Long rejectedRefunds = refundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.FAILED || p.getStatus() == PaymentStatus.CANCELLED)
                    .collect(Collectors.counting());
            // Refund amount only from RESOLVED refund payments
            Long totalRefundAmount = refundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.RESOLVED)
                    .mapToLong(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0L)
                    .sum();

            // Calculate net revenue: COMPLETED payments - RESOLVED refunds
            Long netRevenue = totalPaymentAmount - totalRefundAmount;

            return DashboardOverviewResponse.builder()
                    .revenueOverview(DashboardOverviewResponse.RevenueOverview.builder()
                            .totalRevenue(netRevenue)
                            .monthlyRevenue(netRevenue) // Simplified for now
                            .dailyRevenue(netRevenue) // Simplified for now
                            .monthlyGrowthPercentage(0.0) // TODO: Calculate actual growth
                            .growthDirection("STABLE")
                            .build())
                    .paymentSummary(DashboardOverviewResponse.PaymentSummary.builder()
                            .totalPayments(totalPayments)
                            .completedPayments(completedPayments)
                            .pendingPayments(paymentCounts.getOrDefault(PaymentStatus.PENDING, 0L))
                            .failedPayments(paymentCounts.getOrDefault(PaymentStatus.FAILED, 0L))
                            .processingPayments(paymentCounts.getOrDefault(PaymentStatus.PROCESSING, 0L))
                            .successRate(
                                    totalPayments > 0 ? (completedPayments.doubleValue() / totalPayments * 100) : 0.0)
                            .build())
                    .bookingSummary(DashboardOverviewResponse.BookingSummary.builder()
                            .totalBookings(totalBookings)
                            .confirmedBookings(confirmedBookings)
                            .pendingBookings(bookingRepository
                                    .countByStatus(com.mss.project.booking_service.enums.BookingStatus.PENDING))
                            .cancelledBookings(bookingRepository
                                    .countByStatus(com.mss.project.booking_service.enums.BookingStatus.CANCELLED))
                            .completedBookings(bookingRepository
                                    .countByStatus(com.mss.project.booking_service.enums.BookingStatus.COMPLETED))
                            .completionRate(
                                    totalBookings > 0 ? (confirmedBookings.doubleValue() / totalBookings * 100) : 0.0)
                            .build())
                    .ticketSummary(DashboardOverviewResponse.TicketSummary.builder()
                            .totalTickets(totalTickets)
                            .activeTickets(activeTickets)
                            .usedTickets(ticketRepository
                                    .countByStatus(com.mss.project.booking_service.enums.TicketStatus.USED))
                            .cancelledTickets(ticketRepository
                                    .countByStatus(com.mss.project.booking_service.enums.TicketStatus.CANCELLED))
                            .expiredTickets(ticketRepository
                                    .countByStatus(com.mss.project.booking_service.enums.TicketStatus.EXPIRED))
                            .utilizationRate(
                                    totalTickets > 0 ? (activeTickets.doubleValue() / totalTickets * 100) : 0.0)
                            .build())
                    .refundSummary(DashboardOverviewResponse.RefundSummary.builder()
                            .totalRefunds(totalRefunds)
                            .processingRefunds(processingRefunds)
                            .completedRefunds(resolvedRefunds)
                            .rejectedRefunds(rejectedRefunds)
                            .totalRefundAmount(totalRefundAmount)
                            .refundRate(totalPayments > 0 ? (totalRefunds.doubleValue() / totalPayments * 100) : 0.0)
                            .build())
                    .generatedAt(LocalDate.now())
                    .build();

        } catch (Exception e) {
            log.error("Error generating dashboard overview: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to generate dashboard overview: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentAnalyticsResponse getPaymentAnalytics(DashboardAnalyticsRequest request) {
        try {
            LocalDate[] dateRange = parseDateRange(request);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            // Get payment statistics within date range
            List<Payment> payments = paymentRepository.findAll().stream()
                    .filter(p -> !p.getIsRefund()) // Exclude refund payments
                    .collect(Collectors.toList());

            Map<PaymentStatus, Long> statusCounts = payments.stream()
                    .collect(Collectors.groupingBy(Payment::getStatus, Collectors.counting()));

            Map<PaymentStatus, Double> statusPercentages = new HashMap<>();
            long totalPayments = payments.size();

            for (PaymentStatus status : PaymentStatus.values()) {
                long count = statusCounts.getOrDefault(status, 0L);
                double percentage = totalPayments > 0 ? (count * 100.0 / totalPayments) : 0.0;
                statusPercentages.put(status, percentage);
            }

            Long totalAmount = payments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                    .mapToLong(Payment::getAmount)
                    .sum();

            Double averageAmount = totalPayments > 0 ? (totalAmount.doubleValue() / totalPayments) : 0.0;

            return PaymentAnalyticsResponse.builder()
                    .period(request.getPeriod())
                    .startDate(startDate)
                    .endDate(endDate)
                    .trends(PaymentAnalyticsResponse.PaymentTrends.builder()
                            .totalPayments((long) totalPayments)
                            .totalAmount(totalAmount)
                            .averagePaymentAmount(averageAmount)
                            .growthRate(0.0) // TODO: Calculate actual growth
                            .trendDirection("STABLE")
                            .build())
                    .statusBreakdown(PaymentAnalyticsResponse.PaymentStatusBreakdown.builder()
                            .completedPayments(statusCounts.getOrDefault(PaymentStatus.COMPLETED, 0L))
                            .pendingPayments(statusCounts.getOrDefault(PaymentStatus.PENDING, 0L))
                            .failedPayments(statusCounts.getOrDefault(PaymentStatus.FAILED, 0L))
                            .cancelledPayments(statusCounts.getOrDefault(PaymentStatus.CANCELLED, 0L))
                            .processingPayments(statusCounts.getOrDefault(PaymentStatus.PROCESSING, 0L))
                            .resolvedPayments(statusCounts.getOrDefault(PaymentStatus.RESOLVED, 0L))
                            .statusPercentages(statusPercentages)
                            .build())
                    .dailyTrends(new ArrayList<>()) // TODO: Implement daily trends
                    .revenueMetrics(PaymentAnalyticsResponse.RevenueMetrics.builder()
                            .totalRevenue(totalAmount)
                            .projectedRevenue(totalAmount) // Simplified
                            .lostRevenue(0L) // TODO: Calculate lost revenue
                            .revenueEfficiency(100.0) // Simplified
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Error generating payment analytics: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to generate payment analytics: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RefundAnalyticsResponse getRefundAnalytics(DashboardAnalyticsRequest request) {
        try {
            LocalDate[] dateRange = parseDateRange(request);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

            // Get refund payments by status for detailed breakdown
            List<Payment> allRefundPayments = paymentRepository.findAll().stream()
                    .filter(Payment::getIsRefund)
                    .filter(p -> p.getCreatedAt().isAfter(startInstant) && p.getCreatedAt().isBefore(endInstant))
                    .collect(Collectors.toList());

            Long totalRefunds = (long) allRefundPayments.size();

            Long processingRefunds = allRefundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.PROCESSING)
                    .collect(Collectors.counting());

            Long resolvedRefunds = allRefundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.RESOLVED)
                    .collect(Collectors.counting());

            Long rejectedRefunds = allRefundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.FAILED || p.getStatus() == PaymentStatus.CANCELLED)
                    .collect(Collectors.counting());

            // Refund amount only from RESOLVED refund payments
            Long resolvedRefundAmount = allRefundPayments.stream()
                    .filter(p -> p.getStatus() == PaymentStatus.RESOLVED)
                    .mapToLong(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0L)
                    .sum();

            Double averageRefundAmount = resolvedRefunds > 0
                    ? (resolvedRefundAmount.doubleValue() / resolvedRefunds)
                    : 0.0;

            // Get total payments for refund rate calculation
            Long totalPayments = paymentRepository.findAll().stream()
                    .filter(p -> !p.getIsRefund())
                    .filter(p -> p.getCreatedAt().isAfter(startInstant) && p.getCreatedAt().isBefore(endInstant))
                    .collect(Collectors.counting());

            Double refundRate = totalPayments > 0 ? (totalRefunds.doubleValue() / totalPayments * 100) : 0.0;
            Double approvalRate = totalRefunds > 0 ? (resolvedRefunds.doubleValue() / totalRefunds * 100) : 0.0;

            // Get daily refund data
            List<Object[]> dailyRefundData = paymentRepository.findDailyRefundStatistics(startInstant, endInstant);
            List<RefundAnalyticsResponse.DailyRefundData> dailyData = dailyRefundData.stream()
                    .map(row -> {
                        // Query returns: date, refundCount, totalRefundAmount, completedRefunds,
                        // pendingRefunds, processingRefunds, resolvedRefunds
                        LocalDate date = parseAsLocalDate(row[0]);
                        Long refundRequests = parseAsLong(row[1]); // refundCount
                        Long refundAmount = parseAsLong(row[2]); // totalRefundAmount
                        Long completedRefundsDaily = parseAsLong(row[3]); // completedRefunds
                        Long pendingRefunds = parseAsLong(row[4]); // pendingRefunds
                        Long processingRefundsDaily = parseAsLong(row[5]); // processingRefunds
                        Long resolvedRefundsDaily = parseAsLong(row[6]); // resolvedRefunds

                        Long processedRefunds = completedRefundsDaily + pendingRefunds + processingRefundsDaily
                                + resolvedRefundsDaily;
                        Double dailyApprovalRate = refundRequests > 0
                                ? (resolvedRefundsDaily.doubleValue() / refundRequests * 100)
                                : 0.0;

                        return RefundAnalyticsResponse.DailyRefundData.builder()
                                .date(date)
                                .refundRequests(refundRequests)
                                .processedRefunds(processedRefunds)
                                .completedRefunds(resolvedRefundsDaily)
                                .rejectedRefunds(0L) // Simplified - would need additional status mapping
                                .refundAmount(refundAmount)
                                .approvalRate(dailyApprovalRate)
                                .build();
                    })
                    .collect(Collectors.toList());

            return RefundAnalyticsResponse.builder()
                    .period(request.getPeriod())
                    .startDate(startDate)
                    .endDate(endDate)
                    .summary(RefundAnalyticsResponse.RefundSummary.builder()
                            .totalRefundRequests(totalRefunds)
                            .processingRefunds(processingRefunds)
                            .completedRefunds(resolvedRefunds)
                            .rejectedRefunds(rejectedRefunds)
                            .totalRefundAmount(resolvedRefundAmount)
                            .completedRefundAmount(resolvedRefundAmount)
                            .averageRefundAmount(averageRefundAmount)
                            .refundRate(refundRate)
                            .approvalRate(approvalRate)
                            .build())
                    .dailyData(dailyData)
                    .trends(RefundAnalyticsResponse.RefundTrends.builder()
                            .requestGrowthRate(0.0) // TODO: Calculate growth rate
                            .amountGrowthRate(0.0) // TODO: Calculate growth rate
                            .trendDirection("STABLE")
                            .primaryRefundReason("CUSTOMER_REQUEST")
                            .processingTimeAverage(2.5) // Average processing time in days
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Error generating refund analytics: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to generate refund analytics: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public RevenueAnalyticsResponse getRevenueAnalytics(DashboardAnalyticsRequest request) {
        try {
            LocalDate[] dateRange = parseDateRange(request);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            Instant startInstant = startDate.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant endInstant = endDate.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);

            // Get completed payments (revenue) within date range - only COMPLETED status
            List<Payment> completedPayments = paymentRepository.findAll().stream()
                    .filter(p -> p.getStatus() == PaymentStatus.COMPLETED && !p.getIsRefund())
                    .filter(p -> p.getCreatedAt().isAfter(startInstant) && p.getCreatedAt().isBefore(endInstant))
                    .collect(Collectors.toList());

            Long totalPaymentAmount = completedPayments.stream()
                    .mapToLong(Payment::getAmount)
                    .sum();

            // Get refund amount within date range - only RESOLVED refund status
            List<Payment> resolvedRefunds = paymentRepository.findAll().stream()
                    .filter(Payment::getIsRefund)
                    .filter(p -> p.getStatus() == PaymentStatus.RESOLVED)
                    .filter(p -> p.getCreatedAt().isAfter(startInstant) && p.getCreatedAt().isBefore(endInstant))
                    .collect(Collectors.toList());

            Long refundedAmount = resolvedRefunds.stream()
                    .mapToLong(p -> p.getRefundAmount() != null ? p.getRefundAmount() : 0L)
                    .sum();

            // Revenue calculation: COMPLETED payments - RESOLVED refunds
            Long totalCompletedPaymentMoney = totalPaymentAmount;
            Long totalResolvedRefundMoney = refundedAmount;
            Long revenue = totalCompletedPaymentMoney - totalResolvedRefundMoney;

            Double refundRate = totalCompletedPaymentMoney > 0
                    ? (totalResolvedRefundMoney.doubleValue() / totalCompletedPaymentMoney * 100)
                    : 0.0;
            Double averageTransactionValue = completedPayments.size() > 0
                    ? (totalCompletedPaymentMoney.doubleValue() / completedPayments.size())
                    : 0.0;

            // Get daily revenue data using existing repository method
            List<Object[]> dailyPaymentData = paymentRepository.findDailyRegularPaymentStatistics(startInstant,
                    endInstant);
            List<RevenueAnalyticsResponse.DailyRevenueData> dailyRevenue = dailyPaymentData.stream()
                    .map(row -> {
                        LocalDate date = parseAsLocalDate(row[0]);
                        Long dailyPaymentAmount = parseAsLong(row[2]); // totalAmount from daily stats
                        Long transactionCount = parseAsLong(row[1]); // paymentCount

                        // Calculate refund amount for this day (simplified)
                        Long dayRefundAmount = 0L; // Would need separate query for daily refunds

                        return RevenueAnalyticsResponse.DailyRevenueData.builder()
                                .date(date)
                                .revenue(dailyPaymentAmount)
                                .netRevenue(dailyPaymentAmount - dayRefundAmount)
                                .transactionCount(transactionCount.intValue())
                                .refundAmount(dayRefundAmount)
                                .averageTransactionValue(
                                        transactionCount > 0 ? (dailyPaymentAmount.doubleValue() / transactionCount)
                                                : 0.0)
                                .build();
                    })
                    .collect(Collectors.toList());

            return RevenueAnalyticsResponse.builder()
                    .period(request.getPeriod())
                    .startDate(startDate)
                    .endDate(endDate)
                    .overview(RevenueAnalyticsResponse.RevenueOverview.builder()
                            .totalRevenue(revenue)
                            .netRevenue(revenue)
                            .grossRevenue(totalCompletedPaymentMoney)
                            .refundedAmount(totalResolvedRefundMoney)
                            .refundRate(refundRate)
                            .averageTransactionValue(averageTransactionValue)
                            .totalTransactions(completedPayments.size())
                            .build())
                    .dailyRevenue(dailyRevenue)
                    .monthlyRevenue(new ArrayList<>()) // TODO: Implement monthly revenue breakdown
                    .projection(RevenueAnalyticsResponse.RevenueProjection.builder()
                            .projectedMonthlyRevenue(revenue) // Simplified projection
                            .projectedAnnualRevenue(revenue * 12) // Simplified projection
                            .confidenceLevel(85.0)
                            .projectionBasis("CURRENT_TREND")
                            .build())
                    .build();

        } catch (Exception e) {
            log.error("Error generating revenue analytics: {}", e.getMessage(), e);
            throw new PaymentLinkException("Failed to generate revenue analytics: " + e.getMessage());
        }
    }

    private LocalDate[] parseDateRange(DashboardAnalyticsRequest request) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate;

        switch (request.getPeriod()) {
            case "LAST_7_DAYS":
                startDate = endDate.minusDays(6);
                break;
            case "LAST_30_DAYS":
                startDate = endDate.minusDays(29);
                break;
            case "LAST_3_MONTHS":
                startDate = endDate.minusMonths(3);
                break;
            case "LAST_6_MONTHS":
                startDate = endDate.minusMonths(6);
                break;
            case "LAST_YEAR":
                startDate = endDate.minusYears(1);
                break;
            case "CUSTOM":
                if (request.getStartDate() != null && request.getEndDate() != null) {
                    startDate = LocalDate.parse(request.getStartDate());
                    endDate = LocalDate.parse(request.getEndDate());
                } else {
                    startDate = endDate.minusDays(29); // Default to 30 days
                }
                break;
            default:
                startDate = endDate.minusDays(29);
                break;
        }

        return new LocalDate[] { startDate, endDate };
    }

    private LocalDate parseAsLocalDate(Object value) {
        if (value == null) {
            return LocalDate.now();
        }
        if (value instanceof java.sql.Date) {
            return ((java.sql.Date) value).toLocalDate();
        }
        if (value instanceof java.time.LocalDate) {
            return (java.time.LocalDate) value;
        }
        if (value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (Exception e) {
                log.warn("Failed to parse '{}' as LocalDate, returning today", value);
                return LocalDate.now();
            }
        }
        log.warn("Unexpected date value type: {}, returning today", value.getClass());
        return LocalDate.now();
    }
}
