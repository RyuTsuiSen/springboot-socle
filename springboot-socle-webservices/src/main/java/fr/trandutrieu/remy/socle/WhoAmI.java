package fr.trandutrieu.remy.socle;

import java.util.ResourceBundle;


public class WhoAmI {
	private static WhoAmI instance;
	public String version;
	public String name;
	public String socleVersion;
	private WhoAmI() {
		
	}
	
	
	public static WhoAmI getInstance() {
        if (null == instance) {
            instance = new WhoAmI();
    		ResourceBundle resource = ResourceBundle.getBundle("application-whoami");
    		instance.version = resource.getString("version");
    		instance.name = resource.getString("name");
    		instance.socleVersion = resource.getString("socle.version");
        }
        return instance;
    }
}