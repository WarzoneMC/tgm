package network.warzone.warzoneapi.models;

import lombok.Getter;

import java.util.List;

@Getter
public class PlayerAltsResponse {

  private boolean error;
  private String message;

  private UserProfile lookupUser;
  private List<UserProfile> users;

}
