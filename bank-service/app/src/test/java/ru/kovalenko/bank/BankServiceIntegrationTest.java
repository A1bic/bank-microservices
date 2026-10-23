package ru.kovalenko.bank;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.kovalenko.bank.infrastructure.rates.InMemoryExchangeRateProvider;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Runs the whole bank-service against a real PostgreSQL started by Testcontainers.
 * Kafka is not needed: the listener is disabled and rates are put into the provider directly.
 */
@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
class BankServiceIntegrationTest {
    private static final String PASSWORD = "secret123";
    private static final AtomicInteger USER_SEQUENCE = new AtomicInteger();

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private InMemoryExchangeRateProvider exchangeRates;

    @Test
    void requestWithoutCredentials_isUnauthorized() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    void wrongPassword_isUnauthorized() throws Exception {
        mvc.perform(get("/api/auth/me").with(httpBasic("admin", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminCreatesClient_whoCanLogIn() throws Exception {
        Client client = createClient("black");

        mvc.perform(get("/api/auth/me").with(as(client)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value(client.login()))
                .andExpect(jsonPath("$.roles[0]").value("CLIENT"));
    }

    @Test
    void createUser_takenLogin_isConflict() throws Exception {
        Client client = createClient("black");

        mvc.perform(json(post("/api/users"), userJson(client.login(), "MALE", "black")).with(admin()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USER_ALREADY_EXISTS"));
    }

    @Test
    void createUser_invalidBody_isBadRequest() throws Exception {
        mvc.perform(json(post("/api/users"), "{\"login\": \"\"}").with(admin()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rolesAreEnforced() throws Exception {
        Client client = createClient("black");

        mvc.perform(get("/api/users").with(as(client)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
        mvc.perform(get("/api/users/" + client.id() + "/accounts").with(admin()))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanSearchUsersByGenderAndHairColor() throws Exception {
        String uniqueHairColor = "color-" + USER_SEQUENCE.incrementAndGet();
        Client client = createClient(uniqueHairColor);

        mvc.perform(get("/api/users/search")
                        .param("gender", "MALE")
                        .param("hairColor", uniqueHairColor)
                        .with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].login").value(client.login()));
    }

    @Test
    void depositAndWithdraw_changeBalance() throws Exception {
        Client alice = createClient("black");
        long account = openAccount(alice, "1000.00");

        mvc.perform(json(post("/api/accounts/" + account + "/deposit"), "{\"amount\": 500}").with(as(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("DEPOSIT"))
                .andExpect(jsonPath("$.amount").value(500.0));
        mvc.perform(json(post("/api/accounts/" + account + "/withdraw"), "{\"amount\": 200}").with(as(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("WITHDRAW"));
        expectBalance(alice, account, 1300.0);

        mvc.perform(json(post("/api/accounts/" + account + "/withdraw"), "{\"amount\": 5000}").with(as(alice)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INSUFFICIENT_FUNDS"));
        expectBalance(alice, account, 1300.0);
    }

    @Test
    void transferCommission_dependsOnRelationship() throws Exception {
        Client alice = createClient("black");
        Client bob = createClient("black");
        long aliceAccount = openAccount(alice, "1000.00");
        long aliceSecondAccount = openAccount(alice, "0.00");
        long bobAccount = openAccount(bob, "0.00");

        // Strangers: 10%
        transfer(alice, aliceAccount, bobAccount, "100")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(110.0))
                .andExpect(jsonPath("$.commission").value(10.0));

        // Friends: 3%
        mvc.perform(post("/api/users/" + alice.id() + "/friends/" + bob.id()).with(as(alice)))
                .andExpect(status().isNoContent());
        transfer(alice, aliceAccount, bobAccount, "100")
                .andExpect(jsonPath("$.commission").value(3.0));

        // Own accounts: free
        transfer(alice, aliceAccount, aliceSecondAccount, "100")
                .andExpect(jsonPath("$.commission").value(0.0));

        expectBalance(alice, aliceAccount, 1000.0 - 110 - 103 - 100);
        expectBalance(bob, bobAccount, 200.0);
        expectBalance(alice, aliceSecondAccount, 100.0);
    }

    @Test
    void transfer_isVisibleInHistoryOfBothAccounts() throws Exception {
        Client alice = createClient("black");
        Client bob = createClient("black");
        long aliceAccount = openAccount(alice, "1000.00");
        long bobAccount = openAccount(bob, "0.00");

        transfer(alice, aliceAccount, bobAccount, "100").andExpect(status().isOk());

        mvc.perform(get("/api/accounts/" + aliceAccount + "/transactions").with(as(alice)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].type").value("TRANSFER_OUT"))
                .andExpect(jsonPath("$[0].counterpartyAccountId").value(bobAccount));
        mvc.perform(get("/api/accounts/" + bobAccount + "/transactions").param("type", "TRANSFER_IN").with(as(bob)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[0].counterpartyAccountId").value(aliceAccount));
    }

    @Test
    void transfer_toTheSameAccount_isBadRequest() throws Exception {
        Client alice = createClient("black");
        long account = openAccount(alice, "1000.00");

        transfer(alice, account, account, "100").andExpect(status().isBadRequest());
        expectBalance(alice, account, 1000.0);
    }

    @Test
    void balance_canBeShownInForeignCurrency() throws Exception {
        exchangeRates.updateRate("USD", new BigDecimal("80"));
        Client alice = createClient("black");
        long account = openAccount(alice, "1000.00");

        mvc.perform(get("/api/accounts/" + account + "/balance").param("currency", "usd").with(as(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(12.5))
                .andExpect(jsonPath("$.currency").value("USD"));
        mvc.perform(get("/api/accounts/" + account + "/balance").param("currency", "XYZ").with(as(alice)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CURRENCY_NOT_FOUND"));
    }

    @Test
    void invalidInput_isBadRequest() throws Exception {
        Client alice = createClient("black");
        long account = openAccount(alice, "1000.00");

        mvc.perform(json(post("/api/accounts/" + account + "/deposit"), "{\"amount\": -5}").with(as(alice)))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/accounts/" + account + "/transactions").param("type", "REFUND").with(as(alice)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownAccount_isNotFound() throws Exception {
        Client alice = createClient("black");

        mvc.perform(get("/api/accounts/999999").with(as(alice)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"));
    }

    private record Client(long id, String login) {
    }

    private Client createClient(String hairColor) throws Exception {
        String login = "client" + USER_SEQUENCE.incrementAndGet();
        MvcResult result = mvc.perform(json(post("/api/users"), userJson(login, "MALE", hairColor)).with(admin()))
                .andExpect(status().isCreated())
                .andReturn();
        return new Client(idOf(result), login);
    }

    private long openAccount(Client owner, String initialBalance) throws Exception {
        MvcResult result = mvc.perform(json(post("/api/users/" + owner.id() + "/accounts"),
                        "{\"initialBalance\": " + initialBalance + "}").with(as(owner)))
                .andExpect(status().isCreated())
                .andReturn();
        return idOf(result);
    }

    private ResultActions transfer(Client sender, long from, long to, String amount) throws Exception {
        String body = "{\"fromAccountId\": %d, \"toAccountId\": %d, \"amount\": %s}".formatted(from, to, amount);
        return mvc.perform(json(post("/api/transfers"), body).with(as(sender)));
    }

    private void expectBalance(Client owner, long account, double expected) throws Exception {
        mvc.perform(get("/api/accounts/" + account + "/balance").with(as(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(expected))
                .andExpect(jsonPath("$.currency").value("RUB"));
    }

    private static String userJson(String login, String gender, String hairColor) {
        return """
                {"login": "%s", "password": "%s", "name": "Test", "age": 30,
                 "gender": "%s", "hairColor": "%s", "role": "CLIENT"}
                """.formatted(login, PASSWORD, gender, hairColor);
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static long idOf(MvcResult result) throws Exception {
        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private static RequestPostProcessor admin() {
        return httpBasic("admin", "admin");
    }

    private static RequestPostProcessor as(Client client) {
        return httpBasic(client.login(), PASSWORD);
    }
}
