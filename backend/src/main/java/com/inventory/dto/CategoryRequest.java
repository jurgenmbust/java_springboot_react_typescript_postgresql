package com.inventory.dto;
import jakarta.validation.constraints.*;
/** Holds editable category fields. Spring validates these annotations. */
public record CategoryRequest(@NotBlank @Size(max = 80) String name, @Size(max = 1000) String description) {}
