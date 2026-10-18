package nl.adg.rummikub.game;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class GameRegistryTest {

    @AfterEach
    void clearRegistry() {
        GameRegistry.clear();
    }

    @Test
    void removeGame_sessionIsNoLongerFound() {
        String removed = GameRegistry.createGame("Kitchen table", 4);
        String kept = GameRegistry.createGame("Living room", 2);

        GameRegistry.removeGame(removed);

        assertThat(GameRegistry.getGame(removed)).isNull();
        assertThat(GameRegistry.getAllGames()).extracting(GameSession::sessionId).containsExactly(kept);
    }
}
