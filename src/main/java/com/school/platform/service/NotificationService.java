package com.school.platform.service;

import com.school.platform.dto.notification.NotificationResponse;
import com.school.platform.entity.Notification;
import com.school.platform.entity.User;
import com.school.platform.exception.ResourceNotFoundException;
import com.school.platform.repository.NotificationRepository;
import com.school.platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getNotifications(String userEmail) {
        User user = getUser(userEmail);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(NotificationResponse::from)
                .toList();
    }

    public long getUnreadCount(String userEmail) {
        User user = getUser(userEmail);
        return notificationRepository.countByUserIdAndReadFalse(user.getId());
    }

    @Transactional
    public void markAsRead(String userEmail, Long notificationId) {
        User user = getUser(userEmail);
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Notification introuvable"));
        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(String userEmail) {
        User user = getUser(userEmail);
        notificationRepository.markAllAsReadByUserId(user.getId());
    }

    @Transactional
    public void createNotification(User user, String title, String message, String type, Long referenceId) {
        Notification notification = Notification.builder()
                .user(user)
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    }
}
