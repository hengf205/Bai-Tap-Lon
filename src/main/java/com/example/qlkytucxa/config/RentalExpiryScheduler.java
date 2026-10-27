package com.example.qlkytucxa.config;

import com.example.qlkytucxa.service.RentalRequestService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RentalExpiryScheduler {
    private final RentalRequestService rentalRequests;

    public RentalExpiryScheduler(RentalRequestService rentalRequests) {
        this.rentalRequests = rentalRequests;
    }

    /** Runs shortly after midnight and frees beds for unpaid same-day invoices. */
    @Scheduled(cron = "0 * * * * *", zone = "Asia/Ho_Chi_Minh")
    public void expireUnpaidRentalRequests() {
        rentalRequests.cancelExpiredRequests();
    }
}
