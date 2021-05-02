package io.github.cdfn.skyblock.messages.handler;

import io.github.cdfn.skyblock.messages.PlayerDataMessages.PlayerDataResponse;
import io.github.cdfn.skyblock.commons.messages.api.AbstractMessageHandler;
import io.github.cdfn.skyblock.listener.DataSynchronizationListener;

public class PlayerDataResponseHandler extends AbstractMessageHandler<PlayerDataResponse> {

  @Override
  public void accept(PlayerDataResponse message) {
    var cf = DataSynchronizationListener.WAIT_LIST.get(message.getUUID());
    if(cf == null) {
      return;
    }
    cf.complete(message.getPlayerData());
  }
}
