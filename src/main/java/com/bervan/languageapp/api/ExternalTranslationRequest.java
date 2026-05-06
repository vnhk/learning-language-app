package com.bervan.languageapp.api;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ExternalTranslationRequest {
    private String englishText;
    private String polishText;
    private Boolean saveWithSound;
    private Boolean loadNewImages = true;
    private Boolean generateExample;
    private String level;
    private String apiKey;
    private String language;
}
