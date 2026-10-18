package nl.adg.rummikub.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.UUID;
import nl.adg.rummikub.game.GameRegistry;
import nl.adg.rummikub.game.GameSession;
import nl.adg.rummikub.game.Player;
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

    @Test
    void addPlayer_addsThePlayerToTheSession() throws Exception {
        String sessionId = GameRegistry.createGame("Kitchen table", 4);
        String playerId = "3f2a9c1e-7b4d-4e8a-9c2f-1d6b8e0a5c73";

        mockMvc.perform(post("/games/{sessionId}/players", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + playerId + "\",\"name\":\"Ann\",\"profilePic\":7}"))
                .andExpect(status().isCreated());

        assertThat(GameRegistry.getGameOrThrow(sessionId).players())
                .containsExactly(new Player(UUID.fromString(playerId), "Ann", 7));
    }

    @Test
    void addPlayer_returns404ForAnUnknownSession() throws Exception {
        mockMvc.perform(post("/games/{sessionId}/players", "no-such-session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"3f2a9c1e-7b4d-4e8a-9c2f-1d6b8e0a5c73\",\"name\":\"Ann\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value(containsString("no-such-session")));
    }

    @Test
    void addPlayer_returns409OnceTheSessionIsFull() throws Exception {
        String sessionId = GameRegistry.createGame("Kitchen table", 1);
        GameRegistry.getGameOrThrow(sessionId).addPlayer(Player.of("Ann"));

        mockMvc.perform(post("/games/{sessionId}/players", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"3f2a9c1e-7b4d-4e8a-9c2f-1d6b8e0a5c73\",\"name\":\"Bob\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(409));

        assertThat(GameRegistry.getGameOrThrow(sessionId).players())
                .extracting(Player::name).containsExactly("Ann");
    }

    @Test
    void addPlayer_rejectsAnIdThatIsNotAUuid() throws Exception {
        String sessionId = GameRegistry.createGame("Kitchen table", 4);

        mockMvc.perform(post("/games/{sessionId}/players", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"player-1\",\"name\":\"Ann\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        assertThat(GameRegistry.getGameOrThrow(sessionId).players()).isEmpty();
    }

    @Test
    void deleteGame_removesTheSession() throws Exception {
        String sessionId = GameRegistry.createGame("Kitchen table", 4);

        mockMvc.perform(delete("/games/{sessionId}", sessionId))
                .andExpect(status().isNoContent());

        assertThat(GameRegistry.getGame(sessionId)).isNull();
    }

    @Test
    void deleteGame_returns404WhenTheSessionIsAlreadyGone() throws Exception {
        String sessionId = GameRegistry.createGame("Kitchen table", 4);
        GameRegistry.removeGame(sessionId);

        mockMvc.perform(delete("/games/{sessionId}", sessionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }
}
