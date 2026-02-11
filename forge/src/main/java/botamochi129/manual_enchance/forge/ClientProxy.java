package botamochi129.manual_enchance.forge;

import botamochi129.manual_enchance.MainClient;
import botamochi129.manual_enchance.client.ManualEnchanceClient;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public class ClientProxy {

    public static class ModEventBusListener {

        @SubscribeEvent
        public static void onClientSetupEvent(FMLClientSetupEvent event) {
            MainClient.init();
            ManualEnchanceClient.init();
        }

    }

    public static class ForgeEventBusListener {

    }
}