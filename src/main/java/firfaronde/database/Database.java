package firfaronde.database;

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
    private static final DataSource dataSource = createDataSource();

    private static DataSource createDataSource() {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        dataSource.setUrl("jdbc:postgresql://"+db_host+":"+db_port+"/"+db);
        dataSource.setUser(db_user);
        dataSource.setPassword(db_password);
        return dataSource;
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
            System.out.println(e);
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
            System.out.println(e);
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
            System.out.println(e);
        }
        return results;
    }
    /**
     * @author https://github.com/ols45234
     * */
    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws SQLException;
    }
    /**
     * @author https://github.com/ols45234
     * */
    @FunctionalInterface
    private interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }
}
