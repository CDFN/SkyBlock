package io.github.cdfn.skyblock.messages.handler;

import com.google.inject.Inject;
import io.github.cdfn.skyblock.commons.messages.PlayerDataMessages.PlayerDataRequest;
import io.github.cdfn.skyblock.commons.messages.PlayerDataMessages.PlayerDataResponse;
import io.github.cdfn.skyblock.commons.messages.api.AbstractMessageHandler;
import io.github.cdfn.skyblock.commons.messages.api.MessagePublisher;
import io.github.cdfn.skyblock.util.EntityPlayerDataManager;
import io.lettuce.core.RedisClient;
import org.bukkit.Server;

public class PlayerDataRequestHandler extends AbstractMessageHandler<PlayerDataRequest> {

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
    publisher.publish(new PlayerDataResponse(uuid, EntityPlayerDataManager.readPlayerNBT(player)));
  }
}
