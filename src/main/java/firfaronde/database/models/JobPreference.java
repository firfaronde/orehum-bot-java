package firfaronde.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JobPreference {
    public int id;
    public int profileId;
    public String jobName;
    public int priority;
}