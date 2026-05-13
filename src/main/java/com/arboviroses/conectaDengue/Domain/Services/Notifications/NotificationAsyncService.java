package com.arboviroses.conectaDengue.Domain.Services.Notifications;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class NotificationAsyncService {

    private final NotificationService notificationService;

    @Async
    public void processDbfAsync(byte[] fileBytes) {
        try {
            notificationService.saveNotificationsFromDbfBytes(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void processXlsxAsync(byte[] fileBytes) {
        try {
            notificationService.saveNotificationsFromXlsxBytes(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Async
    public void processCsvAsync(byte[] fileBytes) {
        try {
            notificationService.saveNotificationsFromCsvBytes(fileBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
