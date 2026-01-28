package at.fhv.simplyevents.rest;

import at.fhv.simplyevents.application.port.in.EventUseCase;
import at.fhv.simplyevents.application.port.in.EventUseCase.CreateEventCommand;
import at.fhv.simplyevents.application.port.in.EventUseCase.EventResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = EventRestController.class, properties = "spring.jpa.open-in-view=false")
@AutoConfigureMockMvc(addFilters = false) // Disable security login for this test
class EventRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventUseCase eventUseCase;

    @Test
    void createEvent_should_return_200_when_service_succeeds() throws Exception {
        // Arrange
        // We create a dummy result to return
        EventResult mockResult = new EventResult(
                1L, "Test Event", null, null, null, null,
                0, 0, 0, 0, 0, "Other", null, null, null,
                null, null, null, null, false,
                "PLANNED", false
        );

        when(eventUseCase.createEvent(any(CreateEventCommand.class))).thenReturn(mockResult);

        // JSON payload sent by frontend
        String jsonPayload = """
            {
                "title": "My Event",
                "publishNow": false
            }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isOk()); // Expect 200 OK
    }

    @Test
    void createEvent_should_return_400_when_service_throws_validation_error() throws Exception {
        // Arrange
        // Simulate the Service saying "Title is required"
        when(eventUseCase.createEvent(any(CreateEventCommand.class)))
                .thenThrow(new IllegalArgumentException("Title is required."));

        String jsonPayload = """
            {
                "title": "Valid Title",
                "publishNow": false
            }
        """;

        // Act & Assert
        // CRITICAL: We expect 400 (Bad Request), NOT 500
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Title is required."));
    }
}
