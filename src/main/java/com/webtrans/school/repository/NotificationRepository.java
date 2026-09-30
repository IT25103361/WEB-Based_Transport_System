package com.webtrans.school.repository;

import com.webtrans.school.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByParentIdOrderByCreatedAtDesc(Long parentId);

    @Transactional
    @Modifying
    void deleteByParentId(Long parentId);
}