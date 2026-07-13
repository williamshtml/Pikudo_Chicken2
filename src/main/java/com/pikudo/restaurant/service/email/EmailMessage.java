package com.pikudo.restaurant.service.email;

import java.util.List;

public record EmailMessage(
        List<String> to,
        String subject,
        String html,
        String text
) {
}
