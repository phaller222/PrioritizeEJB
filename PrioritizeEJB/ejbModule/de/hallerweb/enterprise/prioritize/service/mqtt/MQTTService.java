/*
 * Copyright 2015-2020 Peter Michael Haller and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.hallerweb.enterprise.prioritize.service.mqtt;

import de.hallerweb.enterprise.prioritize.controller.CompanyController;
import de.hallerweb.enterprise.prioritize.controller.InitializationController;
import de.hallerweb.enterprise.prioritize.controller.resource.MQTTResourceController;
import de.hallerweb.enterprise.prioritize.controller.resource.ResourceController;
import de.hallerweb.enterprise.prioritize.controller.security.AuthorizationController;
import de.hallerweb.enterprise.prioritize.model.Department;
import de.hallerweb.enterprise.prioritize.model.resource.Resource;
import de.hallerweb.enterprise.prioritize.model.resource.ResourceGroup;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jboss.resteasy.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * MQTTservice.java - Implements a Service to connect to a MQTT broker and
 * automatically discover resources and manage them. Resources must publish a
 * message to a topic with the name "DISCOVERY" containing the following payload
 * data:
 * <p>
 * [UUID]:[DEPARTMENT_TOKEN][GROUP][NAME]:[DESCRIPTION]:[DATA SEND TOPIC]:[DATA
 * RECEIVE TOPIC]:[MAX NUMBER OF SLOTS]
 * <p>
 * Example:
 * e2ff27c8-2120-11e5-b5f7-727283247c7f:e94bbf75-1d30-484e-a900-aedf737558bd:group1:Test:Testresource:SEND:RECEIVE:1
 **/
@Singleton
@LocalBean
@Startup
@ApplicationScoped
public class MQTTService implements MqttCallback {

    public static final String COMMAND_REMOVE = "REMOVE";
    public static final String COMMAND_STARTUP = "STARTUP";
    public static final String COMMAND_SHUTDOWN = "SHUTDOWN";
    public static final String COMMAND_SLOTS = "SLOTS";
    public static final String COMMAND_GET = "GET";
    public static final String COMMAND_SET = "SET";
    public static final String COMMAND_CLEAR = "CLEAR";
    public static final String COMMAND_GEO = "GEO";
    public static final String COMMAND_PING = "PING";
    public static final String COMMAND_COMMANDS = "COMMANDS";
    public static final String COMMAND_SEND_COMMAND = "SENDCOMMAND";
    public static final String COMMAND_GET_COMMANDS = "GETCOMMANDS";
    public static final String COMMAND_SCAN_DEVICES = "SCANDEVICES";

    public static final int QOS = 0;

    private MqttClient client;
    private MqttConnectOptions options;

    static final String CLIENT_ID = MqttClient.generateClientId();

    @EJB
    ResourceController resourceController;
    @EJB
    MQTTResourceController controller;
    @EJB
    CompanyController companyController;
    @EJB
    InitializationController initController;
    @EJB
    AuthorizationController authController;

    private void connect() {
        if (Boolean
                .parseBoolean(initController.config.get(InitializationController.ENABLE_MQTT_SERVICE))) {
            Map<String, String> config = initController.config;
            String mqttHost = "tcp://" + config.get(InitializationController.MQTT_HOST) + ":"
                    + config.get(InitializationController.MQTT_PORT);
            try {
                if (client == null) {
                    options = new MqttConnectOptions();
                    options.setKeepAliveInterval(60);
                    options.setCleanSession(true);

                    Logger.getLogger(this.getClass())
                            .info("Using username " + config.get(InitializationController.MQTT_USERNAME) + " for MQTT");
                    Logger.getLogger(this.getClass())
                            .info("Using pass "
                                    + Arrays.toString(config.get(InitializationController.MQTT_PASSWORD).toCharArray())
                                    + " for MQTT");
                    options.setUserName(config.get(InitializationController.MQTT_USERNAME));
                    options.setPassword(config.get(InitializationController.MQTT_PASSWORD).toCharArray());
                    MemoryPersistence persistence = new MemoryPersistence();
                    client = new MqttClient(mqttHost, CLIENT_ID, persistence);
                }
                client.setCallback(this);
                client.connect(options);
                client.subscribe("DISCOVERY", QOS);
                // subscribe to all registered resources
                List<String> registeredResources = controller.getAllMqttUuids();
                for (String uuid : registeredResources) {
                    Resource resource = controller.getResource(uuid, authController.getSystemUser());
                    String[] subscription = new String[3];
                    subscription[0] = uuid;
                    subscription[1] = resource.getMqttDataSendTopic();
                    subscription[2] = resource.getMqttDataReceiveTopic();
                    client.subscribe(subscription, new int[]{QOS, QOS, QOS});
                }

            } catch (Exception e) {
                Logger.getLogger(this.getClass()).error("MQTT: error connecting..." + e.getMessage());
            }

        }
    }

    /**
     * Shuts down the connection to all mqtt brokers.
     */
    @PreDestroy
    public void shutdown() {
        if (Boolean
                .parseBoolean(initController.config.get(InitializationController.ENABLE_MQTT_SERVICE))) {
            try {
                client.disconnect();
                client.close();
            } catch (Exception e) {
                Logger.getLogger(this.getClass()).error("MQTT: error disconnecting..." + e.getMessage());
            }
        }
    }

    /**
     * Test connection to MQTT broker every 30 seconds. If connection is lost, try
     * to reconnect
     */
    @Schedule(second = "*/30", hour = "*", minute = "*", persistent = false)
    @PostConstruct
    public void checkConnection() {
        if (Boolean.parseBoolean(initController.config.get(InitializationController.ENABLE_MQTT_SERVICE))
                && (client == null || !client.isConnected())) {
            connect();
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        Logger.getLogger(this.getClass()).warn("MQTT: connectionLost... " + throwable.getMessage());
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Logger.getLogger(getClass()).warn(e.getMessage());
        }
        reconnect();
    }

    private void reconnect() {
        if (Boolean
                .parseBoolean(initController.config.get(InitializationController.ENABLE_MQTT_SERVICE))) {
            try {
                Thread.sleep(100);
                if (!client.isConnected()) {
                    connect();
                }
                Thread.sleep(100);

            } catch (Exception ex) {
                Logger.getLogger(this.getClass()).error(ex.getMessage());
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken arg0) {
        // Empty MQTT callback - not used yet.
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) {
        try {
            if (topic.equalsIgnoreCase("DISCOVERY")) {
                handleDiscovery(mqttMessage);
            } else if (isUUID(topic)) {
                handleStatusMessage(topic, mqttMessage);
            } else {
                handleDataReceivedMessage(topic, mqttMessage);
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass()).error(ex.getMessage());
        }
    }

    public void writeToTopic(String topic, byte[] data) {
        try {
            client.publish(topic, data, QOS, false);
        } catch (MqttException e) {
            Logger.getLogger(this.getClass()).error(e.getMessage());
        }
    }

    private void handleDiscovery(MqttMessage mqttMessage) {
        try {
            // Read MQTT Resource data
            String message = new String(mqttMessage.getPayload());
            String[] data = message.split(":");

            String uuid = data[0];
            String token = data[1];
            if ((token == null) || (token.length() < 1) && Boolean.parseBoolean(initController.config
                    .get(InitializationController.DISCOVERY_ALLOW_DEFAULT_DEPARTMENT))) {
                token = InitializationController.DEFAULT_DEPARTMENT_TOKEN;
            }
            String group = data[2];
            if ((group == null) || (group.length() < 1)) {
                group = "default";
            }
            String name = data[3];
            String desc = data[4];
            String dataSendTopic = uuid + "/" + data[5];
            String dataReceiveTopic = uuid + "/" + data[6];
            int slots = Integer.parseInt(data[7]);

            // Create resource if not already discovered
            if (!controller.exists(uuid)) {
                Resource tempResource = new Resource();
                tempResource.setName(name);
                tempResource.setDescription(desc);
                tempResource.setMaxSlots(slots);
                tempResource.setStationary(false);
                tempResource.setRemote(true);
                tempResource.setAgent(false);
                tempResource.setMqttUUID(uuid);
                tempResource.setDataReceiveTopic(dataReceiveTopic);
                tempResource.setDataSendTopic(dataSendTopic);
                tempResource.setIp("");
                Resource resource = controller.createMqttResource(tempResource, token, group,
                        authController.getSystemUser());

                String[] subscription = new String[3];
                subscription[0] = uuid;
                subscription[1] = resource.getMqttDataSendTopic();
                subscription[2] = resource.getMqttDataReceiveTopic();
                client.subscribe(subscription, new int[]{QOS, QOS, QOS});
                client.publish(resource.getMqttDataReceiveTopic(), new MqttMessage("REGISTERED".getBytes()));
            }
        } catch (Exception ex) {
            Logger.getLogger(this.getClass()).error(ex.getMessage());
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
                Resource resource = controller.getResource(uuid, authController.getSystemUser());
                controller.writeMqttDataReceived(resource, data);
            } catch (Exception ex) {
                Logger.getLogger(this.getClass()).error(ex.getMessage());
            }
        } else {
            // ignore READ
        }
    }

    private void handleStatusMessage(String uuid, MqttMessage message) {
        if (controller.exists(uuid)) {
            // Get Resource with UUID
            Resource resource = controller.getResource(uuid, authController.getSystemUser());
            String data = new String(message.getPayload());
            String statusData;
            if (data.contains(":")) {
                statusData = data.split(":")[0];
            } else {
                statusData = data;
            }
            handleStatusData(resource, data, statusData);
        }
    }

    private void handleStatusData(Resource resource, String data, String statusData) {
        switch (statusData) {
            // Remove resource
            case COMMAND_REMOVE:
                resourceController.deleteResource(resource.getId(), authController.getSystemUser());
                try {
                    client.publish(resource.getMqttDataReceiveTopic(), new MqttMessage("UNREGISTERED".getBytes()));
                } catch (MqttException e) {
                    Logger.getLogger(this.getClass()).error(e.getMessage());
                }
                break;
            case COMMAND_STARTUP:
                controller.setMqttResourceOnline(resource);
                try {
                    client.publish(resource.getMqttDataReceiveTopic(), new MqttMessage("ONLINE".getBytes()));
                } catch (MqttException e) {
                    Logger.getLogger(this.getClass()).error(e.getMessage());
                }

                break;
            case COMMAND_SHUTDOWN:
                controller.setMqttResourceOffline(resource);
                try {
                    client.publish(resource.getMqttDataReceiveTopic(), new MqttMessage("OFFLINE".getBytes()));
                } catch (MqttException e) {
                    Logger.getLogger(this.getClass()).error(e.getMessage());
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
                handleCommands(resource, data);
                break;

            case COMMAND_SEND_COMMAND:
                String[] commandString = data.split(":");
                String destUuid = commandString[1];
                String destCommand = commandString[2];
                String destParam = commandString[3];

                if (controller.exists(destUuid)) {
                    Resource targetResource = controller.getResource(destUuid, authController.getSystemUser());
                    controller.sendCommandToResource(targetResource, destCommand, destParam);
                }
                break;
            case COMMAND_GET_COMMANDS:
                if (controller.exists(data)) {
                    // Resource targetResource = controller.getResource(uuidToQuery,
                    // authController.getSystemUser());
                    // TODO: Implement GET_COMMANDS!
                    // String[] targetCommandList = targetResource.getMqttCommands().toArray(new
                    // String[] {});
                }
                break;
            case COMMAND_SCAN_DEVICES:
                handleScanDevices(data);
                break;

            case COMMAND_SLOTS:
            default:
                break;
        }
    }

    private void handleCommands(Resource resource, String data) {
        String[] commands = data.split(":");
        controller.clearCommands(resource);
        HashSet<String> reportedCommands = new HashSet<>();
        for (String cmd : commands) {
            if (!cmd.equalsIgnoreCase(COMMAND_COMMANDS)) {
                reportedCommands.add(cmd);
            }
        }
        controller.setCommands(resource, reportedCommands);
    }

    private void handleScanDevices(String data) {
        String[] scanDevicesData = data.split(":");
        String deviceUuid = scanDevicesData[1];
        String departmentKey = scanDevicesData[2];
        StringBuilder scanResult = new StringBuilder();
        if (controller.exists(deviceUuid)) {
            Department department = companyController.getDepartmentByToken(departmentKey,
                    authController.getSystemUser());
            Set<ResourceGroup> groups = department.getResourceGroups();
            List<Resource> devicesFound = scanDevices(deviceUuid, groups);
            if (!devicesFound.isEmpty()) {
                scanResult.append("SCANRESULT");
                for (Resource res : devicesFound) {
                    scanResult.append(":").append(res.getMqttUUID()).append(";").append(res.getName()).append(";")
                            .append(res.getDescription()).append(";").append(res.getMaxSlots());
                }
                this.fireScanResult(deviceUuid, scanResult.toString());
            }
        }
    }

    private List<Resource> scanDevices(String deviceUuid, Set<ResourceGroup> groups) {
        List<Resource> devicesFound = new ArrayList<>();
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
        return devicesFound;
    }

    private void fireScanResult(String sourceUuid, String scanResult) {
        try {
            this.client.publish(sourceUuid + "/read", scanResult.getBytes(), QOS, false);
        } catch (MqttException e) {
            Logger.getLogger(this.getClass()).error(e.getMessage());
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
