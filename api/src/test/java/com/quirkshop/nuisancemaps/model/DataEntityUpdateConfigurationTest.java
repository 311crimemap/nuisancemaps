package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.hibernate.annotations.DynamicUpdate;
import org.junit.jupiter.api.Test;

class DataEntityUpdateConfigurationTest {

    @Test
    void dataEntitiesUseDynamicUpdate() {
        assertThat(DataCrime.class.isAnnotationPresent(DynamicUpdate.class)).isTrue();
        assertThat(Data311.class.isAnnotationPresent(DynamicUpdate.class)).isTrue();
    }
}
