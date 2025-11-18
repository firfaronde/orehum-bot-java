package firfaronde.database;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.Duration;

@Data
@AllArgsConstructor
public class PlayTime {
    public int id;
    public String userId;
    public String tracker;
    public Duration timeSpent;
}