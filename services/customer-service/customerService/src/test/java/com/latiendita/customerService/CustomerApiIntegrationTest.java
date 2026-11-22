package com.latiendita.customerService;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.latiendita.customerService.customer.CustomerRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = "eureka.client.enabled=false")
@AutoConfigureMockMvc
@TestPropertySource(properties = "outbox.relay.poll-interval-ms=600000")
class CustomerApiIntegrationTest {

    private static final String SUB = "auth0|abc-123";
    private static final String EMAIL = "ana@mail.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setup() {
        customerRepository.deleteAll();
    }

    @Test
    void shouldCreateCustomerReturning201WithLocationAndHeaderIdentity() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", SUB)
                        .header("X-User-Email", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorizerSub":"spoofed","email":"ana@mail.com",\
                                "firstName":"Ana","lastName":"Diaz","phone":"555-1234"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/customers/")))
                .andExpect(jsonPath("$.authorizerSub").value(SUB))
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.newsletterOptIn").value(false))
                .andExpect(jsonPath("$.deletedAt").doesNotExist());
    }

    @Test
    void shouldReturn200OnIdempotentSecondCreate() throws Exception {
        String body = """
                {"authorizerSub":"auth0|abc-123","email":"ana@mail.com",\
                "firstName":"Ana","lastName":"Diaz"}
                """;

        String firstLocation = mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", SUB)
                        .header("X-User-Email", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", SUB)
                        .header("X-User-Email", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", firstLocation))
                .andExpect(jsonPath("$.authorizerSub").value(SUB));
    }

    @Test
    void shouldRejectInvalidEmailWith400ProblemDetail() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", SUB)
                        .header("X-User-Email", "not-an-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorizerSub":"auth0|abc-123","email":"not-an-email",\
                                "firstName":"Ana","lastName":"Diaz"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void shouldRejectBodySuppliedDuplicateEmailWithoutTrustedClaim() throws Exception {
        String firstBody = """
                {"authorizerSub":"auth0|abc-123","email":"ana@mail.com",\
                "firstName":"Ana","lastName":"Diaz"}
                """;
        mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", SUB)
                        .header("X-User-Email", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBody))
                .andExpect(status().isCreated());

        // Without the gateway's email claim, the body email is untrusted: a
        // different sub must never be able to steal the existing profile.
        mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", "auth0|other-456")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.detail").value(containsString("Email already registered")));
    }

    @Test
    void shouldReclaimProfileWhenAccountWasRecreatedWithSameTrustedEmail() throws Exception {
        String body = """
                {"authorizerSub":"ignored","email":"ana@mail.com",\
                "firstName":"Ana","lastName":"Diaz"}
                """;
        String firstLocation = mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", SUB)
                        .header("X-User-Email", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getHeader("Location");

        // Authorizer deleted and recreated the account: same trusted email, new sub.
        mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", "auth0|new-sub-after-reset")
                        .header("X-User-Email", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(header().string("Location", firstLocation))
                .andExpect(jsonPath("$.authorizerSub").value("auth0|new-sub-after-reset"))
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void shouldReturn404ProblemDetailForUnknownId() throws Exception {
        mockMvc.perform(get("/api/customers/{id}", UUID.randomUUID())
                        .header("X-User-Id", SUB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Customer not found"));
    }

    @Test
    void shouldUpdateProfileAndNewsletterConsent() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .header("X-User-Id", SUB)
                        .header("X-User-Email", EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"authorizerSub":"auth0|abc-123","email":"ana@mail.com",\
                                "firstName":"Ana","lastName":"Diaz"}
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(patch("/api/customers/me")
                        .header("X-User-Id", SUB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Ana Maria","lastName":"Diaz","phone":"555-9999"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ana Maria"))
                .andExpect(jsonPath("$.phone").value("555-9999"));

        mockMvc.perform(put("/api/customers/me/newsletter")
                        .header("X-User-Id", SUB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newsletterOptIn\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletterOptIn").value(true))
                .andExpect(jsonPath("$.newsletterConsentAt").isNotEmpty());

        mockMvc.perform(put("/api/customers/me/newsletter")
                        .header("X-User-Id", SUB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newsletterOptIn\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newsletterOptIn").value(false))
                .andExpect(jsonPath("$.newsletterConsentAt").isEmpty());
    }

    @Test
    void shouldRequireGatewayIdentityHeader() throws Exception {
        mockMvc.perform(get("/api/customers/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }
}
