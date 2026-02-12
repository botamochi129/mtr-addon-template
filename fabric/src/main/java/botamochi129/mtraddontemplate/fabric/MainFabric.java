package botamochi129.mtraddontemplate.fabric;

import botamochi129.mtraddontemplate.Main;
import net.fabricmc.api.ModInitializer;

public class MainFabric implements ModInitializer {

	@Override
	public void onInitialize() {
		Main.init(new RegistriesWrapperImpl());
	}

}
