package orm;

import fr.xen0xys.multimc.common.orm.Orm;
import fr.xen0xys.multimc.common.orm.orms.SqliteOrm;
import fr.xen0xys.multimc.common.orm.session.SimpleSession;
import org.junit.jupiter.api.*;
import org.sqlite.SQLiteException;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class OrmTests {

    private static Orm orm;
    private static DBPlayer player1;
    private static DBPlayer player2;
    private static DBPlayer player3;

    @BeforeAll
    public static void setup() throws SQLException {
        orm = new SqliteOrm("database.db");
        player1 = new DBPlayer("test", UUID.randomUUID());
        player2 = new DBPlayer("test", UUID.randomUUID());
        player3 = new DBPlayer("test", player1.getUuid());
    }

    @Order(1)
    @Test
    public void createPlayersTable() throws SQLException {
        orm.registerTable(DBPlayer.class);
    }

    @Order(2)
    @Test
    public void addPlayers() throws SQLException, IllegalAccessException {
        SimpleSession<DBPlayer> session = orm.createSession();
        session.save(player1);
        session.save(player2);
    }

    @Order(3)
    @Test
    public void addPlayerUniqueException() {
        assertThrows(SQLException.class, () -> {
            SimpleSession<DBPlayer> session = orm.createSession();
            session.save(player3);
        });
    }

    @Order(4)
    @Test
    public void getPlayerFromId() throws SQLException {
        SimpleSession<DBPlayer> session = orm.createSession();
        DBPlayer fetchedPlayer = session.get(DBPlayer.class, player1.getId());
        assertEquals(player1, fetchedPlayer);
    }

    @Order(5)
    @Test
    public void getPlayersFromColumn() throws SQLException {
        SimpleSession<DBPlayer> session = orm.createSession();
        List<DBPlayer> fetchedPlayers = session.get(DBPlayer.class, "username", player1.getUsername());
        assert(fetchedPlayers.size() == 2);
        assertEquals(fetchedPlayers.get(0), player1);
        assertEquals(fetchedPlayers.get(1), player2);
    }

    @Order(6)
    @Test
    public void updatePlayers() throws SQLException, IllegalAccessException {
        SimpleSession<DBPlayer> session = orm.createSession();
        player1.setUsername("test2");
        player2.setUuid(UUID.randomUUID());
        session.update(player1);
        session.update(player2);
    }

    @Order(7)
    @Test
    public void getAllPlayers() throws SQLException {
        SimpleSession<DBPlayer> session = orm.createSession();
        List<DBPlayer> fetchedPlayers = session.getAll(DBPlayer.class);
        assert(fetchedPlayers.size() == 2);
        assertEquals(fetchedPlayers.get(0), player1);
        assertEquals(fetchedPlayers.get(1), player2);
    }

    @Order(8)
    @Test
    public void removePlayer() throws SQLException, IllegalAccessException {
        SimpleSession<DBPlayer> session = orm.createSession();
        session.delete(player1);
        session.delete(player2);
    }
}

