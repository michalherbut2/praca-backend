package pl.most.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.most.backend.service.GoogleCalendarService;

@SpringBootTest
class MostBackendApplicationTests {

    // Zastępujemy prawdziwy GoogleCalendarService atrapą,
    // żeby Spring nie próbował łączyć się z Google API przy starcie.
    @MockitoBean
    private GoogleCalendarService googleCalendarService;

    @Test
    void contextLoads() {
    }
}