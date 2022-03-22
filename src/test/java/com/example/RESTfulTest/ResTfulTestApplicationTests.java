package com.example.RESTfulTest;

import java.util.Optional;

import com.example.RESTfulTest.model.Widget;
import com.example.RESTfulTest.service.WidgetService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doReturn;
import static org.mockito.ArgumentMatchers.any;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ResTfulTestApplicationTests {

	@MockBean
    private WidgetService service;

    @Autowired
    private MockMvc mockMvc;

	@Test
	void contextLoads() {
	}

	@Test
    @DisplayName("GET /rest/widget/1")
    void testGetWidgetById() throws Exception {
        // Setup our mocked service
        Widget widget = new Widget(1l, "Widget Name", "Description", 1);
        doReturn(Optional.of(widget)).when(service).findById(1l);

        // Execute the GET request
        mockMvc.perform(get("/rest/widget/{id}", 1L))
                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"1\""))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Widget Name")))
                .andExpect(jsonPath("$.description", is("Description")))
                .andExpect(jsonPath("$.version", is(1)));
    }

	@Test
    @DisplayName("PUT /rest/widget/1 - Not Found")
    void testUpdateWidgetNotFound() throws Exception {
        // Setup our mocked service
        Widget widgetToPut = new Widget("New Widget", "This is my widget");
        doReturn(Optional.empty()).when(service).findById(1L);

        // Execute the POST request
        mockMvc.perform(put("/rest/widget/{id}", 1l)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, 3)
                .content(asJsonString(widgetToPut)))

                // Validate the response code and content type
                .andExpect(status().isNotFound());
    }

	@Test
    @DisplayName("PUT /rest/widget/1")
    void testUpdateWidget() throws Exception {
        // Setup our mocked service
        Widget widgetToPut = new Widget("New Widget", "This is my widget");
        Widget widgetToReturnFindBy = new Widget(1L, "New Widget", "This is my widget", 2);
        Widget widgetToReturnSave = new Widget(1L, "New Widget", "This is my widget", 3);
        doReturn(Optional.of(widgetToReturnFindBy)).when(service).findById(1L);
        doReturn(widgetToReturnSave).when(service).save(any());

        // Execute the POST request
        mockMvc.perform(put("/rest/widget/{id}", 1l)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.IF_MATCH, 2)
                .content(asJsonString(widgetToPut)))

                // Validate the response code and content type
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))

                // Validate headers
                .andExpect(header().string(HttpHeaders.LOCATION, "/rest/widget/1"))
                .andExpect(header().string(HttpHeaders.ETAG, "\"3\""))

                // Validate the returned fields
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("New Widget")))
                .andExpect(jsonPath("$.description", is("This is my widget")))
                .andExpect(jsonPath("$.version", is(3)));
    }

	static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
