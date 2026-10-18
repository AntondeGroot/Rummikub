package nl.adg.rummikub.game;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import nl.adg.rummikub.game.exception.GameAlreadyStartedException;
import nl.adg.rummikub.game.exception.SessionFullException;
import org.junit.jupiter.api.Test;

class GameSessionTest {

    @Test
    void addPlayer_refusesAPlayerOnceTheSessionIsFull() {
        GameSession session = new GameSession("s1", "Kitchen table", 2);
        session.addPlayer(Player.of("Ann"));
        session.addPlayer(Player.of("Bob"));

        assertThatThrownBy(() -> session.addPlayer(Player.of("Cas")))
                .isInstanceOf(SessionFullException.class);
        assertThat(session.players()).extracting(Player::name).containsExactly("Ann", "Bob");
    }

    @Test
    void addPlayer_refusesAPlayerAfterTheGameHasStarted() {
        GameSession session = new GameSession("s1", "Kitchen table", 4);
        session.addPlayer(Player.of("Ann"));
        session.start();

        assertThatThrownBy(() -> session.addPlayer(Player.of("Bob")))
                .isInstanceOf(GameAlreadyStartedException.class);
        assertThat(session.players()).extracting(Player::name).containsExactly("Ann");
    }

    @Test
    void start_keepsEveryPlayerAndMovesToInProgress() {
        GameSession session = new GameSession("s1", "Kitchen table", 4);
        session.addPlayer(Player.of("Ann"));
        session.addPlayer(Player.of("Bob"));
        session.addPlayer(Player.of("Cas"));

        session.start();

        assertThat(session.status()).isEqualTo(SessionStatus.IN_PROGRESS);
        assertThat(session.players()).extracting(Player::name)
                .containsExactlyInAnyOrder("Ann", "Bob", "Cas");
    }

    @Test
    void start_refusesToStartTwice() {
        GameSession session = new GameSession("s1", "Kitchen table", 4);
        session.addPlayer(Player.of("Ann"));
        session.start();

        assertThatThrownBy(session::start).isInstanceOf(GameAlreadyStartedException.class);
        assertThat(session.status()).isEqualTo(SessionStatus.IN_PROGRESS);
    }
}
