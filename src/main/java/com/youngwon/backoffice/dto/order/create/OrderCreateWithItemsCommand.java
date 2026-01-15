package com.youngwon.backoffice.dto.order.create;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record OrderCreateWithItemsCommand(
        @NotNull OrderCreateCommand order,
        @NotNull @Size(min = 1) List<@Valid OrderCreateItemCommand> items
) {}