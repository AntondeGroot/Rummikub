package nl.adg.rummikub.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import nl.adg.rummikub.game.GameRegistry;
import nl.adg.rummikub.game.GameSession;
import nl.adg.rummikub.game.SessionStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GameroomApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @AfterEach
    void clearRegistry() {
        GameRegistry.clear();
    }

    @Test
    void createGame_returns201WithASessionTheRegistryCanFind() throws Exception {
        String body = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomName\":\"Kitchen table\",\"maxPlayers\":4}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String sessionId = JsonPath.read(body, "$.sessionId");
        GameSession session = GameRegistry.getGame(sessionId);
        assertThat(session).isNotNull();
        assertThat(session.roomName()).isEqualTo("Kitchen table");
        assertThat(session.maxPlayers()).isEqualTo(4);
        assertThat(session.status()).isEqualTo(SessionStatus.WAITING);
    }

    @Test
    void createGame_rejectsMorePlayersThanRummikubSupports() throws Exception {
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomName\":\"Kitchen table\",\"maxPlayers\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value(containsString("at most 4 players")));

        assertThat(GameRegistry.getAllGames()).isEmpty();
    }

    @Test
    void createGame_rejectsARequestWithoutARoomName() throws Exception {
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"maxPlayers\":4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        assertThat(GameRegistry.getAllGames()).isEmpty();
    }

    @Test
    void createGame_rejectsABodyThatIsNotValidJson() throws Exception {
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"roomName\":\"Kitchen table\","))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.message").value("request body is not valid JSON"));

        assertThat(GameRegistry.getAllGames()).isEmpty();
    }
}
