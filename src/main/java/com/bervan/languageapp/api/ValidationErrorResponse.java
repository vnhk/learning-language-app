package com.bervan.languageapp.api;

import com.bervan.common.config.EntityConfigValidator;

import java.util.List;

public record ValidationErrorResponse(List<EntityConfigValidator.FieldError> errors) {
}
