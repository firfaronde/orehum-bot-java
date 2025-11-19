package firfaronde.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import static firfaronde.database.Database.executeQueryAsync;

@Data
@AllArgsConstructor
public class JobPreference {
    public int id;
    public int profileId;
    public String jobName;
    public int priority;

    public static Optional<JobPreference> getBestJob(int profileid) {
        return executeQueryAsync(
                "SELECT * FROM job WHERE profile_id = ? ORDER BY priority DESC LIMIT 1;",
                stmt->stmt.setInt(1, profileid),
                JobPreference::rsToJobPreference
        );
    }

    public static JobPreference rsToJobPreference(ResultSet rs) throws SQLException {
        return new JobPreference(
                rs.getInt("job_id"),
                rs.getInt("profile_id"),
                rs.getString("job_name"),
                rs.getInt("priority")
        );
    }
}