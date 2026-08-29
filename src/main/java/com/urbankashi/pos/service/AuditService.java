package com.urbankashi.pos.service;

import com.urbankashi.pos.model.AuditLog;
import com.urbankashi.pos.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    public void record(String action, String entityType, Object entityId, String details) {
        String username = SecurityContextHolder.getContext().getAuthentication() == null ? "system" : SecurityContextHolder.getContext().getAuthentication().getName();
        auditLogRepository.save(AuditLog.builder().action(action).entityType(entityType).entityId(entityId == null ? null : String.valueOf(entityId)).performedBy(username).details(details).build());
    }
}
