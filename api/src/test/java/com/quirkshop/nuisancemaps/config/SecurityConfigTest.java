package com.quirkshop.nuisancemaps.config;

//Annotations
import org.junit.jupiter.api.Test; //@Test
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired; //@Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc; //@AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest; //@SpringBootTest

//Mock classes
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;

//mock value methods
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status; //status()
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; //content()
//import static org.hamcrest.Matchers.equalTo; //equalTo(operand:....)

@SpringBootTest(classes = NuisancemapsApplication.class)
@AutoConfigureMockMvc // these anotations inject MockMvc instance into test
public class SecurityConfigTest {

    @Autowired
    private MockMvc mvc; // send HTTP requests into the DispatcherServlet and make assertions about the
                         // result.

    @Test
    public void getForbidden() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError())
                .andExpect(content().string("Invalid API Key"));
    }

    @Test
    public void getOpenRouteInit() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/init").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getOpenRouteCategories() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/categories").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getOpenRouteSources() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/locales").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getOpenRouteDataCrimes() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/datacrimes.geojson?sw_lat=42.599&sw_lng=-72.201&ne_lat=42.899&ne_lng=-71.801").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getOpenRouteData311s() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/data311s.geojson?sw_lat=42.599&sw_lng=-72.201&ne_lat=42.899&ne_lng=-71.801").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void headOpenRouteInit() throws Exception {
        mvc.perform(MockMvcRequestBuilders.head("/init"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/init", "/categories", "/locales", "/data311s.geojson", "/datacrimes.geojson", "/actuator/health"})
    public void writesToPublicReadRoutesRequireApiKey(String path) throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid API Key"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/locales/1", "/sources", "/datajobs", "/datacrimes/nested", "/data311s/nested"})
    public void protectedAndNestedRoutesRequireApiKey(String path) throws Exception {
        mvc.perform(MockMvcRequestBuilders.get(path))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid API Key"));
    }

}
