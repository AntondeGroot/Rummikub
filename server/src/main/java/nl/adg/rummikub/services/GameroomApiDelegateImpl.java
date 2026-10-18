package nl.adg.rummikub.services;

import java.util.UUID;
import nl.adg.rummikub.game.GameRegistry;
import nl.adg.rummikub.game.Player;
import nl.adg.rummikub.generated.gameroom.api.GameroomApiDelegate;
import nl.adg.rummikub.generated.gameroom.model.NewGameRequestDto;
import nl.adg.rummikub.generated.gameroom.model.NewGameResponseDto;
import nl.adg.rummikub.generated.gameroom.model.PlayerDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class GameroomApiDelegateImpl implements GameroomApiDelegate {

  private static final Logger log = LoggerFactory.getLogger(GameroomApiDelegateImpl.class);

  @Override
  public ResponseEntity<NewGameResponseDto> createGame(NewGameRequestDto request) {
    String sessionId = GameRegistry.createGame(request.getRoomName(), request.getMaxPlayers());
    log.info("Game created: sessionId={} room='{}' maxPlayers={}",
        sessionId, request.getRoomName(), request.getMaxPlayers());
    return ResponseEntity.status(HttpStatus.CREATED).body(new NewGameResponseDto(sessionId));
  }

  @Override
  public ResponseEntity<Void> addPlayer(String sessionId, PlayerDto request) {
    Player player = new Player(UUID.fromString(request.getId()), request.getName(), request.getProfilePic());
    GameRegistry.getGameOrThrow(sessionId).addPlayer(player);
    log.info("Player added: sessionId={} playerId={}", sessionId, player.id());
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @Override
  public ResponseEntity<Void> deleteGame(String sessionId) {
    GameRegistry.removeGame(sessionId);
    log.info("Game deleted: sessionId={}", sessionId);
    return ResponseEntity.noContent().build();
  }
}
