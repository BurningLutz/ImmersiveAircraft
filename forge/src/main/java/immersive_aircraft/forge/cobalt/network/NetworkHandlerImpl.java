package immersive_aircraft.forge.cobalt.network;

import immersive_aircraft.Main;
import immersive_aircraft.cobalt.network.Message;
import immersive_aircraft.cobalt.network.NetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandlerImpl extends NetworkHandler.Impl {
    private final String PROTOCOL_VERSION = "1";
    private final SimpleChannel channel = NetworkRegistry.newSimpleChannel(
            new Identifier(Main.SHORT_MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private int id = 0;

    @SuppressWarnings("unchecked")
    @Override
    public <T extends Message> void registerMessage(Class<T> msg) {
        channel.registerMessage(id++, msg,
                Message::encode,
                b -> (T)Message.decode(b),
                (m, ctx) -> {
                    ctx.get().enqueueWork(() -> {
                        ServerPlayerEntity sender = ctx.get().getSender();
                        m.receive(sender);
                    });
                    ctx.get().setPacketHandled(true);
                });
    }

    @Override
    public void sendToServer(Message m) {
        channel.sendToServer(m);
    }

    @Override
    public void sendToPlayer(Message m, ServerPlayerEntity e) {
        channel.send(PacketDistributor.PLAYER.with(() -> e), m);
    }
}
