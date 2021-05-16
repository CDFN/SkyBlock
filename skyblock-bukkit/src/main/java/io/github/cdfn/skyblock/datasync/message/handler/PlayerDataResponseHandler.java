package io.github.cdfn.skyblock.datasync.message.handler;

import io.github.cdfn.skyblock.commons.message.api.handler.MessageHandler;
import io.github.cdfn.skyblock.datasync.listener.DataSynchronizationListener;
import io.github.cdfn.skyblock.datasync.message.PlayerDataMessages.PlayerDataResponse;

public class PlayerDataResponseHandler implements MessageHandler<PlayerDataResponse> {

  @Override
  public void accept(PlayerDataResponse message) {
    var cf = DataSynchronizationListener.WAIT_LIST.get(message.getUUID());
    if(cf == null) {
      return;
    }
    cf.complete(message.getPlayerData());
  }
}
