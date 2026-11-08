package cz.mendelu.ipa.domain.analytics;

import lombok.Data;


@Data
public class AnalyticsResponse {

    private double value;
    private String label;

    public AnalyticsResponse(double value) {
        this.value = value;
    }

    public AnalyticsResponse(String label) {
        this.label = label;
    }
}