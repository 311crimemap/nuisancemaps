package com.quirkshop.nuisancemaps.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import com.quirkshop.nuisancemaps.dto.FeatureCollectionDTO;
import com.quirkshop.nuisancemaps.dto.FeatureDTO;

/** Shared rules for adding up to 1,000 older reports to sparse, recent map queries. */
public final class SparseQueryPolicy {
    // Use one calendar zone when deciding whether the requested end date is recent.
    public static final ZoneId ZONE = ZoneId.of("America/Chicago");
    public static final int THRESHOLD = 10;
    public static final int FALLBACK_LIMIT = 1000;

    private SparseQueryPolicy() {}

    public static boolean isRecent(LocalDate endDate) {
        LocalDate today = LocalDate.now(ZONE);
        return endDate.equals(today) || endDate.equals(today.minusDays(1));
    }

    /** Do not search more than six months back from the requested end date. */
    public static LocalDateTime fallbackStart(LocalDate endDate) {
        return endDate.minusMonths(6).atStartOfDay();
    }

    /** Labels requested records and older context for the map. */
    public static FeatureCollectionDTO collect(List<FeatureDTO> features, int matchCount) {
        for (int i = 0; i < features.size(); i++) {
            features.get(i).getProperties().setDateMatch(i < matchCount ? "within_range" : "older_context");
        }
        return new FeatureCollectionDTO("FeatureCollection", features);
    }
}
