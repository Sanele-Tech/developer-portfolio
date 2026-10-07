package com.sanele.portfolio.settings.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sanele.portfolio.auth.security.PortfolioUserDetails;
import com.sanele.portfolio.audit.entity.AuditLog;
import com.sanele.portfolio.audit.repository.AuditLogRepository;
import com.sanele.portfolio.common.exception.ResourceNotFoundException;
import com.sanele.portfolio.settings.dto.SiteSettingResponse;
import com.sanele.portfolio.settings.dto.SiteSettingWriteRequest;
import com.sanele.portfolio.settings.entity.SiteSetting;
import com.sanele.portfolio.settings.repository.SiteSettingRepository;
import com.sanele.portfolio.users.entity.User;
import com.sanele.portfolio.users.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SiteSettingAdminService {
    private static final Set<String> PUBLIC_SETTING_KEYS = Set.of(
            "profile.availability", "profile.education", "profile.focus", "profile.location");

    private final SiteSettingRepository siteSettingRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public SiteSettingAdminService(SiteSettingRepository siteSettingRepository, UserRepository userRepository,
            AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.siteSettingRepository = siteSettingRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<SiteSettingResponse> getSettings() {
        Map<String, SiteSetting> existing = siteSettingRepository
                .findAllBySettingKeyInOrderBySettingKeyAsc(PUBLIC_SETTING_KEYS).stream()
                .collect(java.util.stream.Collectors.toMap(SiteSetting::getSettingKey, setting -> setting));
        return PUBLIC_SETTING_KEYS.stream().sorted()
                .map(key -> toResponse(key, existing.get(key)))
                .toList();
    }

    @Transactional
    public SiteSettingResponse updateSetting(String key, SiteSettingWriteRequest request,
            PortfolioUserDetails actor) {
        requirePublicSettingKey(key);
        User user = userRepository.getReferenceById(actor.getId());
        SiteSetting setting = siteSettingRepository.findBySettingKey(key).orElseGet(() ->
                new SiteSetting(key, null, user));
        boolean creating = setting.getId() == null;
        Map<String, Object> before = creating ? null : snapshot(setting);
        setting.updateDetails(normalizeOptional(request.settingValue()), user);
        siteSettingRepository.save(setting);
        writeAudit(actor, creating ? "CREATE" : "UPDATE", setting, before, snapshot(setting));
        return toResponse(key, setting);
    }

    @Transactional
    public void deleteSetting(String key, PortfolioUserDetails actor) {
        requirePublicSettingKey(key);
        SiteSetting setting = siteSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Site setting", key));
        Map<String, Object> before = snapshot(setting);
        writeAudit(actor, "DELETE", setting, before, null);
        siteSettingRepository.delete(setting);
    }

    private void requirePublicSettingKey(String key) {
        if (!PUBLIC_SETTING_KEYS.contains(key)) {
            throw new ResourceNotFoundException("Editable site setting", key);
        }
    }

    private SiteSettingResponse toResponse(String key, SiteSetting setting) {
        return setting == null
                ? new SiteSettingResponse(null, key, null, null)
                : new SiteSettingResponse(setting.getId(), setting.getSettingKey(),
                        setting.getSettingValue(), setting.getUpdatedAt());
    }

    private Map<String, Object> snapshot(SiteSetting setting) {
        return Map.of(
                "settingKey", setting.getSettingKey(),
                "settingValue", setting.getSettingValue() == null ? "" : setting.getSettingValue());
    }

    private String normalizeOptional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void writeAudit(PortfolioUserDetails actor, String action, SiteSetting setting,
            Map<String, Object> before, Map<String, Object> after) {
        Map<String, Object> details = Map.of(
                "before", before == null ? Map.of() : before,
                "after", after == null ? Map.of() : after);
        auditLogRepository.save(new AuditLog(userRepository.getReferenceById(actor.getId()),
                action, "SITE_SETTING", setting.getId(), objectMapper.valueToTree(details)));
    }
}
