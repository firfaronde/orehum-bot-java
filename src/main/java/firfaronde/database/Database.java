package firfaronde.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.ds.PGSimpleDataSource;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static firfaronde.Vars.*;

public class Database {
    // private static final DataSource dataSource = createDataSourcePG();
    public static final HikariDataSource dataSource = createDataSource();

    private static DataSource createDataSourcePG() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://"+db_host+":"+db_port+"/"+db);
        dataSource.setUser(db_user);
        dataSource.setPassword(db_password);

        return dataSource;
    }

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://" + db_host + ":" + db_port + "/" + db);
        config.setUsername(db_user);
        config.setPassword(db_password);

        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(5000);

        return new HikariDataSource(config);
    }

    /**
     * @author https://github.com/ols45234
     * */
    public static <T> Optional<T> executeQueryAsync(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapper.apply(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            // System.out.println(e);
            logger.error("SQL query failed {}", sql, e);
            return Optional.empty();
        }
    }
    /**
     * @author https://github.com/ols45234
     * */
    public static boolean executeUpdate(String sql, ThrowingConsumer<PreparedStatement> parameterSetter) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);
            int updated = pstmt.executeUpdate();
            return updated > 0;

        } catch (SQLException e) {
            // System.out.println(e);
            logger.error("SQL query failed {}", sql, e);
            return false;
        }
    }
    /**
     * @author https://github.com/ols45234
     * */
    public static <T> List<T> executeQueryList(String sql, ThrowingConsumer<PreparedStatement> parameterSetter, SQLFunction<ResultSet, T> mapper) {
        List<T> results = new ArrayList<>();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            parameterSetter.accept(pstmt);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.apply(rs));
                }
            }
        } catch (SQLException e) {
            // System.out.println(e);
            logger.error("SQL query failed {}", sql, e);
        }
        return results;
    }
    /**
     * @author https://github.com/ols45234
     * */
    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }
    /**
     * @author https://github.com/ols45234
     * */
    @FunctionalInterface
    public interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
