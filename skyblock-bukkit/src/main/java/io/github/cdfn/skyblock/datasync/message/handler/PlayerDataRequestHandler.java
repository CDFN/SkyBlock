package io.github.cdfn.skyblock.datasync.message.handler;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.message.api.handler.MessageHandler;
import io.github.cdfn.skyblock.datasync.message.PlayerDataMessages.PlayerDataRequest;
import io.github.cdfn.skyblock.datasync.message.PlayerDataMessages.PlayerDataResponse;
import io.github.cdfn.skyblock.commons.message.api.MessagePublisher;
import io.github.cdfn.skyblock.datasync.util.EntityPlayerDataManager;
import io.lettuce.core.RedisClient;
import org.bukkit.Server;

public class PlayerDataRequestHandler implements MessageHandler<PlayerDataRequest> {

  private final MessagePublisher publisher;
  private final Server server;

  @Inject
  public PlayerDataRequestHandler(Server server, RedisClient client){
    this.publisher = MessagePublisher.get(client);
    this.server = server;
  }

  @Override
  public void accept(PlayerDataRequest message) {
    var uuid = message.getUUID();

    var player = server.getPlayer(uuid);
    if(player == null) {
      return;
    }
    publisher.publish(new PlayerDataResponse(uuid, EntityPlayerDataManager.readPlayerData(player)));
  }
}
