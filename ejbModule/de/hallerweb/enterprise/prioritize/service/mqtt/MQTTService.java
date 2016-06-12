package de.hallerweb.enterprise.prioritize.service.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.controller.security.UserRoleController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import de.hallerweb.enterprise.prioritize.model.security.User;

/**
 * MQTTservice.java - Implements a Service to connect to a MQTT broker and automatically discover resources and manage them. Resources must
 * publish a message to a topic with the name "DISCOVERY" containing the following payload data:
 * 
 * [UUID]:[DEPARTMENT_TOKEN][GROUP][NAME]:[DESCRIPTION]:[DATA SEND TOPIC]:[DATA RECEIVE TOPIC]:[MAX NUMBER OF SLOTS]
 * 
 * Example: e2ff27c8-2120-11e5-b5f7-727283247c7f:group1:e94bbf75-1d30-484e-a900-aedf737558bd:Test:Testresource:SEND:RECEIVE:1
 **/
@Singleton
@LocalBean
@Startup
@ApplicationScoped
public class MQTTService implements MqttCallback {

	public final static String COMMAND_REMOVE = "REMOVE";
	public final static String COMMAND_STARTUP = "STARTUP";
	public final static String COMMAND_SHUTDOWN = "SHUTDOWN";
	public final static String COMMAND_SLOTS = "SLOTS";
	public final static String COMMAND_GET = "GET";
	public final static String COMMAND_SET = "SET";
	public final static String COMMAND_CLEAR = "CLEAR";
	public final static String COMMAND_GEO = "GEO";
	public final static String COMMAND_PING = "PING";
	public final static String COMMAND_COMMANDS = "COMMANDS";
	public final static String COMMAND_SEND_COMMAND = "SENDCOMMAND";
	public final static String COMMAND_GET_COMMANDS = "GETCOMMANDS";
	public final static String COMMAND_SCAN_DEVICES = "SCANDEVICES";
	public final static int QOS = 2;

	private String mqttHost;
	private String mqttHostWrite;
	private MqttClient client;
	private MqttClient clientWrite;
	private MqttConnectOptions options;
	private MemoryPersistence persistence;

	static final String clientID = "prioritize123";

	@Inject
	InitializationController init;

	@EJB
	ResourceController controller;

	@EJB
	UserRoleController userRoleController;

	@EJB
	CompanyController companyController;

	private void connect() {
		if (Boolean.parseBoolean(InitializationController.config.get(InitializationController.ENABLE_MQTT_SERVICE))) {
			HashMap<String, String> config = InitializationController.config;
			mqttHost = "tcp://" + config.get(InitializationController.MQTT_HOST) + ":" + config.get(InitializationController.MQTT_PORT);
			try {
				if (client == null) {
					options = new MqttConnectOptions();
					persistence = new MemoryPersistence();
					options.setKeepAliveInterval(60);
					options.setCleanSession(true);
					client = new MqttClient(mqttHost, clientID, persistence);
				}
				client.setCallback(this);
				client.connect(options);
				client.subscribe("DISCOVERY", QOS);
				// subscribe to all registered resources
				List<String> registeredResources = controller.getAllMqttUuids();
				for (String uuid : registeredResources) {
					Resource resource = controller.getResource(uuid, AuthorizationController.getSystemUser());
					String[] subscription = new String[3];
					subscription[0] = uuid;
					subscription[1] = resource.getDataSendTopic();
					subscription[2] = resource.getDataReceiveTopic();
					client.subscribe(subscription, new int[] { QOS, QOS, QOS });
				}

			} catch (Exception e) {
				System.out.println("MQTT: error connecting...");
				e.printStackTrace();
			}

		}
	}

	/**
	 * Connects to the MQTT broker responsible for sending acknowledge messages to clients
	 */
	private void connectClientWrite() {

		HashMap<String, String> config = InitializationController.config;
		mqttHostWrite = "tcp://" + config.get(InitializationController.MQTT_HOST_WRITE) + ":"
				+ config.get(InitializationController.MQTT_PORT_WRITE);
		if (mqttHost != null && mqttHost.equalsIgnoreCase(mqttHostWrite) && clientWrite != null && clientWrite.isConnected()) {
			return;
		}
		try {
			if (clientWrite == null) {
				options = new MqttConnectOptions();
				persistence = new MemoryPersistence();
				options.setKeepAliveInterval(60);
				options.setCleanSession(false);
				clientWrite = new MqttClient(mqttHostWrite, clientID, persistence);
				clientWrite.connect(options);
			}
		} catch (Exception ex) {
			System.out.println("MQTT: error connecting...");
			ex.printStackTrace();
		}

	}

	/**
	 * Shuts down the connection to all mqtt brokers.
	 */
	@PreDestroy
	public void shutdown() {
		if (Boolean.parseBoolean(init.config.get(init.ENABLE_MQTT_SERVICE))) {
			try {
				List<String> registeredResources = controller.getAllMqttUuids();
				for (String uuid : registeredResources) {
					// client.unsubscribe(uuid);
				}
				client.disconnect();
				client.close();
				clientWrite.disconnect();
				clientWrite.close();
			} catch (Exception e) {
				System.out.println("MQTT: error disconnecting...");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Test connection to MQTT broker every 30 seconds. If connection is lost, try to reconnect
	 */
	@Schedule(second = "*/30", hour = "*", minute = "*", persistent = false)
	@PostConstruct
	public void checkConnection() {
		if (Boolean.parseBoolean(InitializationController.config.get(InitializationController.ENABLE_MQTT_SERVICE))) {
			if (client == null) {
				connect();
			} else if (!client.isConnected()) {
				connect();
			} else {
			}

			if (clientWrite == null) {
				connectClientWrite();
			} else if (!clientWrite.isConnected()) {
				connectClientWrite();
			} else {
			}
		}
	}

	@Override
	public void connectionLost(Throwable arg0) {
		arg0.printStackTrace();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reconnect();
	}

	private void reconnect() {
		if (Boolean.parseBoolean(InitializationController.config.get(InitializationController.ENABLE_MQTT_SERVICE))) {
			try {
				Thread.sleep(100);
				if (!client.isConnected()) {
					connect();
				}
				Thread.sleep(100);

				if (!clientWrite.isConnected()) {
					connectClientWrite();
				}
				Thread.sleep(100);

			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {

	}

	@Override
	public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
		try {
			if (topic.equalsIgnoreCase("DISCOVERY")) {
				handleDiscovery(mqttMessage);
			} else if (isUUID(topic)) {
				handleStatusMessage(topic, mqttMessage);
			} else {
				handleDataReceivedMessage(topic, mqttMessage);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void writeToTopic(String topic, byte[] data) {
		try {
			client.publish(topic, data, QOS, false);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void handleDiscovery(MqttMessage mqttMessage) {

		// Read MQTT Resource data
		String message = new String(mqttMessage.getPayload());
		String data[] = message.split(":");

		String uuid = data[0];
		String token = data[1];
		if ((token == null) || (token.length() < 1)) {
			if (Boolean.valueOf(InitializationController.config.get(InitializationController.DISCOVERY_ALLOW_DEFAULT_DEPARTMENT))) {
				token = InitializationController.DEFAULT_DEPARTMENT_TOKEN;
			}
		}
		String group = data[2];
		if ((group == null) || (group.length() < 1)) {
			group = "default";
		}
		String name = data[3];
		String desc = data[4];
		String dataSendTopic = uuid + "/" + data[5];
		String dataReceiveTopic = uuid + "/" + data[6];
		int slots = Integer.valueOf(data[7]);

		// Create resource if not already discovered
		if (!controller.exists(uuid)) {
			User admin = userRoleController.findUserByUsername("admin", AuthorizationController.getSystemUser());
			Resource resource = controller.createMqttResource(name, token, group, admin, desc, "", slots, false, true, false, uuid,
					dataSendTopic, dataReceiveTopic);
			try {
				try {
					clientWrite.publish(resource.getDataReceiveTopic(), new MqttMessage("REGISTERED".getBytes()));
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// TODO: Neccessary?
				client.disconnectForcibly();
			} catch (MqttException e) {
				e.printStackTrace();
			}

		}
	}

	private void handleDataReceivedMessage(String topic, MqttMessage message) {
		String[] topicSplit = topic.split("/");
		String uuid = topicSplit[0];
		String mode = topicSplit[1];

		if (mode.equalsIgnoreCase("WRITE")) {
			byte[] data = message.getPayload();
			// Get Resource with UUID and write received data from device to it.
			try {
				Resource resource = controller.getResource(uuid, AuthorizationController.getSystemUser());
				controller.writeMqttDataReceived(resource, data);
			} catch (Exception ex) {

			}
		} else {
			// ignore READ
		}
	}

	private void handleStatusMessage(String uuid, MqttMessage message) {
		if (controller.exists(uuid)) {
			// Get Resource with UUID
			Resource resource = controller.getResource(uuid, AuthorizationController.getSystemUser());
			String data = new String(message.getPayload());
			String statusData = null;
			if (data.contains(":")) {
				statusData = data.split(":")[0];
			} else {
				statusData = data;
			}

			switch (statusData) {
			// Remove resource
			case COMMAND_REMOVE:
				controller.deleteResource(resource.getId());
				try {
					clientWrite.publish(resource.getDataReceiveTopic(), new MqttMessage("UNREGISTERED".getBytes()));
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case COMMAND_STARTUP:
				controller.setMqttResourceOnline(resource);
				try {
					clientWrite.publish(resource.getDataReceiveTopic(), new MqttMessage("ONLINE".getBytes()));
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case COMMAND_SHUTDOWN:
				controller.setMqttResourceOffline(resource);
				try {
					clientWrite.publish(resource.getDataReceiveTopic(), new MqttMessage("OFFLINE".getBytes()));
				} catch (MqttException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case COMMAND_SET:
				String commandData = data.split(":")[1] + ":" + data.split(":")[2];
				String name = commandData.split(":")[0];
				String value = commandData.split(":")[1];
				controller.addMqttValueForResource(resource, name, value);
				break;
			case COMMAND_CLEAR:
				String keyData = data.split(":")[1];
				controller.clearMqttValueForResource(resource, keyData);
				break;
			case COMMAND_GEO:
				commandData = data.split(":")[1] + ":" + data.split(":")[2];
				String latitude = commandData.split(":")[0];
				String longitude = commandData.split(":")[1];
				controller.setCoordinates(resource, latitude, longitude);
				break;
			case COMMAND_PING:
				controller.updateMqttPing(resource);
				break;
			case COMMAND_COMMANDS:
				String[] commands = data.split(":");
				controller.clearCommands(resource);
				HashSet<String> reportedCommands = new HashSet<String>();
				for (String cmd : commands) {
					if (!cmd.equalsIgnoreCase("COMMANDS")) {
						reportedCommands.add(cmd);
					}
				}
				controller.setCommands(resource, reportedCommands);
				break;

			case COMMAND_SEND_COMMAND:
				String[] commandString = data.split(":");
				String destUuid = commandString[1];
				String destCommand = commandString[2];
				String destParam = commandString[3];

				if (controller.exists(destUuid)) {
					Resource targetResource = controller.getResource(destUuid, AuthorizationController.getSystemUser());
					controller.sendCommandToResource(targetResource, destCommand, destParam);
				}
				break;
			case COMMAND_GET_COMMANDS:
				String uuidToQuery = data;
				if (controller.exists(uuidToQuery)) {
					Resource targetResource = controller.getResource(uuidToQuery, AuthorizationController.getSystemUser());
					String[] targetCommandList = targetResource.getMqttCommands().toArray(new String[] {});
				}
				break;
			case COMMAND_SCAN_DEVICES:
				String[] scanDevicesData = data.split(":");
				String deviceUuid = scanDevicesData[1];
				String departmentKey = scanDevicesData[2];
				List<Resource> devicesFound = new ArrayList<Resource>();
				String scanResult = "";
				if (controller.exists(deviceUuid)) {
					Department department = companyController.getDepartmentByToken(departmentKey);
					List<ResourceGroup> groups = department.getResourceGroups();
					for (ResourceGroup group : groups) {
						for (Resource res : group.getResources()) {
							if (res.isMqttResource()) {
								if (res.getMqttUUID().equals(deviceUuid)) {
									continue;
								}
								devicesFound.add(res);
							}
						}
					}
					if (!devicesFound.isEmpty()) {
						scanResult = "SCANRESULT";
						for (Resource res : devicesFound) {
							scanResult += ":" + res.getMqttUUID() + ";" + res.getName() + ";" + res.getDescription() + ";"
									+ res.getMaxSlots();
						}
						this.fireScanResult(deviceUuid, scanResult);
					}

				}

				break;

			case COMMAND_SLOTS:
				break;
			default:
				break;
			}
		}
	}

	private void fireScanResult(String sourceUuid, String scanResult) {
		try {
			this.clientWrite.publish(sourceUuid + "/read", scanResult.getBytes(), QOS, false);
			// this.clientWrite.disconnectForcibly();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private boolean isUUID(String string) {
		try {
			UUID.fromString(string);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}
}
