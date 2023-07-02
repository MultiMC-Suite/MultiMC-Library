package orm;

import fr.xen0xys.multimc.common.orm.annotations.field.Column;
import fr.xen0xys.multimc.common.orm.annotations.field.PrimaryKey;
import fr.xen0xys.multimc.common.orm.annotations.type.Table;

import java.util.UUID;

@Table(name = "players")
class DBPlayer {
    @PrimaryKey(autoIncrement = true)
    @Column(name = "id")
    private int id;

    @Column(name = "username")
    private String username;

    @Column(name = "uuid", unique = true)
    private UUID uuid;

    public DBPlayer(String username, UUID uuid) {
        this.username = username;
        this.uuid = uuid;
    }

    public DBPlayer() {}

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof DBPlayer player))
            return false;
        return player.getId() == this.getId() && player.getUsername().equals(this.getUsername()) && player.getUuid().equals(this.getUuid());
    }
}
