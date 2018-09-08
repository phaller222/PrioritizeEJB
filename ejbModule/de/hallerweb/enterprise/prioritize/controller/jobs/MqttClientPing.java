package de.hallerweb.enterprise.prioritize.controller.jobs;

import java.util.Date;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;

import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.resource.MQTTResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * Session Bean implementation class MqttClientPing. This Singleton bean is called every minute to check if all registered Mqtt devices are
 * still present. if the last ping (timestamp entered in DB) is more than 1 minute old, the client gets set offline.
 */
@Singleton
@LocalBean
public class MqttClientPing {

	@EJB
	MQTTResourceController mqttResourceController;

	/**
	 * Default constructor.
	 */
	public MqttClientPing() {
		// Auto-generated constructor stub
	}

	// TODO: Set persistence of timer for releases to "true"
	@Schedule(minute = "*/1", hour = "*", persistent = false)
	public void checkMqttClientPings() {
		if (Boolean.parseBoolean(InitializationController.getConfig().get(InitializationController.ENABLE_MQTT_SERVICE))) {

			// Get all online MQTT resources
			User systemUser = new User();
			systemUser.setUsername("system");
			List<Resource> onlineMqttResources = mqttResourceController.getOnlineMqttResources(AuthorizationController.getSystemUser());
			if (onlineMqttResources != null) {
				for (Resource resource : onlineMqttResources) {
					if (isResourceTimedOut(resource)) {
						resource.setMqttOnline(false);
					}
				}
			}
		}

	}

	private boolean isResourceTimedOut(Resource resource) {
		Date lastPing = resource.getMqttLastPing();
		return (System.currentTimeMillis() - lastPing.getTime() > Long
				.valueOf(InitializationController.getConfig().get(InitializationController.MQTT_PING_TIMEOUT)));
	}
}
