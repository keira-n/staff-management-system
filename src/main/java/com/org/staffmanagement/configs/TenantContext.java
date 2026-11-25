package com.org.staffmanagement.configs;

public final class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setCurrentTenant(String organisationId) {
        if (organisationId == null || organisationId.isBlank()) {
            throw new IllegalArgumentException("Organisation ID cannot be empty.");
        }

        CURRENT_TENANT.set(organisationId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
