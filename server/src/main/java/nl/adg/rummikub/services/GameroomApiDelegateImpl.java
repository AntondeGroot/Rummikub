package nl.adg.rummikub.services;

import nl.adg.rummikub.game.GameRegistry;
import nl.adg.rummikub.generated.gameroom.api.GameroomApiDelegate;
import nl.adg.rummikub.generated.gameroom.model.NewGameRequestDto;
import nl.adg.rummikub.generated.gameroom.model.NewGameResponseDto;
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
}
