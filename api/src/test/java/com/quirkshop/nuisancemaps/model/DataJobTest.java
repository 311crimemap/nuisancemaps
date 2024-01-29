package com.quirkshop.nuisancemaps.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.service.SourceLoaderService;

@SpringBootTest(classes = NuisancemapsApplication.class)
public class DataJobTest {

    @Autowired
    SourceLoaderService sourceLoaderService;

    @Test
    public void DataJobBuildURLTest() throws UnsupportedEncodingException {
        sourceLoaderService.loadJSON("data/source_config.json");
        Source s = sourceLoaderService.findBySourceConfigID(1);
        final int limit = 10000;
        final int offset = 20000;
        final String order_key = "id";

        DataJob d = new DataJob(s, limit, offset, order_key);
        String url = d.buildURL();
        assertThat(s.getUrl()).isEqualTo(d.getSourceURL());
        assertThat(url).isEqualTo(s.getUrl() + "?$limit=" + limit + "&$offset=" + offset + "&$order=" + order_key);
        assertThat(d.getUrl()).isEqualTo(url);
    }
}
