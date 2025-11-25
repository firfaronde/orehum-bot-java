package firfaronde.database.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BanNotification {
    @JsonProperty("ban_id")
    public int banId;
    @JsonProperty("server_id")
    public int serverId;
}