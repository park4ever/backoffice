package com.youngwon.backoffice.dto.order.command;

import jakarta.validation.constraints.Size;

public record OrderChangeMemoRequest(
        @Size(max = 500) String memo
) {}