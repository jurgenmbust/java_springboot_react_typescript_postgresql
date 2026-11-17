package com.inventory.dto;
import java.time.Instant;
import java.util.Map;
/** Describes errors consistently and identifies invalid form fields. */
public record ApiError(Instant timestamp, int status, String message, String path, Map<String, String> fieldErrors) {}
