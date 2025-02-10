package sn2.crafttakestime.networking;

import com.google.gson.Gson;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sn2.crafttakestime.common.config.CraftConfig;

@Data
public class PacketCraftConfig {

    private static final Logger log = LogManager.getLogger(PacketCraftConfig.class);
    public CraftConfig config;

    public PacketCraftConfig(CraftConfig config) {
        this.config = config;
    }

    public byte[] toBytes() {
        Gson gson = new Gson();
        try {
            String json = gson.toJson(config);
            return json.getBytes();
        } catch (Exception e) {
            log.error("Failed to write config to bytes");
        }
        return null;
    }
}
