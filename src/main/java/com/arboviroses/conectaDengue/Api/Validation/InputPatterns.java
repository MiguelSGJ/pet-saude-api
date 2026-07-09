package com.arboviroses.conectaDengue.Api.Validation;

public final class InputPatterns {
    public static final String CPF = "\\d{11}";
    public static final String PASSWORD = "^[^\\p{Cntrl}]{6,72}$";
    public static final String PASSWORD_OPTIONAL = "^$|^[^\\p{Cntrl}]{6,72}$";
    public static final String SAFE_TEXT = "^[\\p{L}\\p{M}\\p{N} .,'_-]+$";
    public static final String SAFE_CODE = "^[A-Za-z0-9._-]+$";
    public static final String DATE_BR = "^(0[1-9]|[12]\\d|3[01])/(0[1-9]|1[0-2])/\\d{4}$";
    public static final String USER_ROLE = "^(USER|ADMIN)$";
    public static final String AGRAVO = "(?i)^(zika|dengue|dengue_geral|dengue_classica|dengue_alarmante|dengue_grave|chikungunya|A90|A92\\.0|A928)$";
    public static final String SCOPE = "^(notificados|confirmados|obitos)$";
    public static final String REFRESH_TOKEN = "^[A-Za-z0-9-]{10,100}$";

    private InputPatterns() {
    }
}
