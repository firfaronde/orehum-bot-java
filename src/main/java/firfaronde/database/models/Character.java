package firfaronde.database.models;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static firfaronde.database.Database.executeQueryAsync;
import static firfaronde.database.Database.executeQueryList;

@Data
@AllArgsConstructor
public class Character {
    public int id, slot;
    public String charName;
    public int age;
    public String sex, skinColor;
    public int preferenceId;
    public String gender, flavorText;
    public float height, width;
    public String customSpecieName, lifepath, nationality, species;

    public static List<Character> getCharacters(String ckey) {
        return executeQueryList(
                "SELECT pr.* FROM profile pr JOIN preference pref ON pr.preference_id = pref.preference_id JOIN player pl ON pref.user_id = pl.user_id WHERE pl.last_seen_user_name = ? ORDER BY pr.char_name DESC",
                stmt->stmt.setString(1, ckey),
                Character::rsToCharacter
        );
    }

    public static Character rsToCharacter(ResultSet rs) throws SQLException {
        return new Character(
                rs.getInt("profile_id"),
                rs.getInt("slot"),
                rs.getString("char_name"),
                rs.getInt("age"),
                rs.getString("sex"),
                rs.getString("skin_color"),
                rs.getInt("preference_id"),
                rs.getString("gender"),
                rs.getString("flavor_text"),
                rs.getFloat("height"),
                rs.getFloat("width"),
                rs.getString("custom_specie_name"),
                rs.getString("lifepath"),
                rs.getString("nationality"),
                rs.getString("species")
        );
    }

    public static Optional<Integer> getSelected(String ckey) {
        return executeQueryAsync(
                "SELECT pref.selected_character_slot FROM preference pref JOIN player pl ON pref.user_id = pl.user_id WHERE pl.last_seen_user_name = ?",
                stmt->stmt.setString(1, ckey),
                rs->rs.getInt("selected_character_slot")
        );
    }
}