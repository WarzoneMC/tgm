package network.warzone.warzoneapi.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor @Getter
public class GetPlayerByNameResponse {

  private UserProfile user;
  private List<Death> deaths;

}
