package com.quirkshop.nuisancemaps.controller;

//Annotations
import org.junit.jupiter.api.Test; //@Test
import org.springframework.beans.factory.annotation.Autowired; //@Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc; //@AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest; //@SpringBootTest

//Mock classes
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc; 
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.quirkshop.nuisancemaps.NuisancemapsApplication;

//mock value methods
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;  //status()
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; //content()
//import static org.hamcrest.Matchers.equalTo; //equalTo(operand:....)


@SpringBootTest(classes = NuisancemapsApplication.class)
@AutoConfigureMockMvc  //these anotations inject MockMvc instance into test
public class TestControllerTest {

	@Autowired
	private MockMvc mvc; //send HTTP requests into the DispatcherServlet and make assertions about the result.

	@Test
	public void getHello() throws Exception {
	    mvc.perform(MockMvcRequestBuilders.get("/").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().json("{ 'id': 1, 'name': 'hello'}"));
	}
}