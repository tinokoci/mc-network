package net.exemine.api.instance;

import net.exemine.api.model.Channel;
import net.exemine.api.redis.RedisService;
import net.exemine.api.redis.pubsub.RedisMessage;
import net.exemine.api.redis.pubsub.model.InstanceCommandModel;
import net.exemine.api.redis.pubsub.model.InstanceHeartbeatModel;
import net.exemine.api.redis.pubsub.model.generic.StringModel;
import net.exemine.api.util.callable.TypeCallback;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InstanceService {

    private final RedisService redisService;

    private final Map<String, Instance> instances = new ConcurrentHashMap<>();
    private final Instance emptyInstance = new Instance(null, InstanceType.UNKNOWN);
    private final Channel channel = Channel.ADMIN;

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> scheduledFuture;

    private Instance currentInstance;

    public InstanceService(RedisService redisService) {
        this.redisService = redisService;
        subscribeToInstanceHeartbeat();
        subscribeToInstanceShutdown();
    }

    public void registerInstance(String name, InstanceType type, TypeCallback<Instance> heartbeatCallback, TypeCallback<String> commandCallback) {
        currentInstance = new Instance(name, type);
        storeInstance(currentInstance);
        subscribeToInstanceCommand(commandCallback);
        scheduledFuture = scheduledExecutor.scheduleAtFixedRate(() -> heartbeatCallback.run(currentInstance), 0L, 500L, TimeUnit.MILLISECONDS);
        sendAlert(channel -> channel.getFormatColor() + channel.getPrefix() + ' ' + channel.getInfoColor() + "Instance " + channel.getValueColor()
                + name + channel.getInfoColor() + " has booted up and is now active.");
    }

    public void unregisterInstance() {
        if (currentInstance == null) return;
        String name = currentInstance.getName();

        scheduledFuture.cancel(false);
        redisService.getPublisher().sendInstanceShutdown(name);
        sendAlert(channel -> channel.getFormatColor() + channel.getPrefix() + ' ' + channel.getInfoColor() + "Instance " + channel.getValueColor()
                + name + channel.getInfoColor() + " has shutdown and is no longer active.");
    }

    private void storeInstance(@NotNull Instance instance) {
        instances.put(instance.getName().toLowerCase(), instance);
    }

    private void sendAlert(@NotNull Function<Channel, String> callback) {
        redisService.getPublisher().sendAlertStaffMessage(channel.getRank(), callback.apply(channel));
    }

    public void runCommand(@NotNull Instance instance, String command) {
        sendInstanceCommand(instance.getName(), command);
    }

    public void runCommand(@NotNull InstanceType type, String command) {
        sendInstanceCommand(type.name(), command);
    }

    public void runCommand(String command) {
        sendInstanceCommand(null, command);
    }

    private void sendInstanceCommand(String instanceName, String command) {
        redisService.getPublisher().sendInstanceCommand(instanceName, command);
    }

    private void subscribeToInstanceCommand(TypeCallback<String> commandCallback) {
        redisService.subscribe(RedisMessage.INSTANCE_COMMAND, InstanceCommandModel.class, model -> {
            InstanceType type = InstanceType.get(model.getName(), false);

            if (model.getName() == null
                    || currentInstance.getType() == type
                    || currentInstance.getName().equals(model.getName())) {
                commandCallback.run(model.getCommand());
            }
        });
    }

    private void subscribeToInstanceHeartbeat() {
        redisService.subscribe(RedisMessage.INSTANCE_HEARTBEAT, InstanceHeartbeatModel.class, model -> {
            Instance instance = instances.get(model.getName().toLowerCase());

            if (instance == null) {
                storeInstance(instance = new Instance(model.getName(), model.getType()));
            }
            instance.setPlayerNames(model.getPlayerNames());
            instance.setOnlinePlayers(model.getOnlinePlayers());
            instance.setMaxPlayers(model.getMaxPlayers());
            instance.setTps1(model.getTps1());
            instance.setTps2(model.getTps2());
            instance.setTps3(model.getTps3());
            instance.setLastUpdate(System.currentTimeMillis());
            instance.setWhitelistRank(model.getWhitelistRank());
            instance.setExtra(model.getExtra());
        });
    }

    private void subscribeToInstanceShutdown() {
        redisService.subscribe(RedisMessage.INSTANCE_SHUTDOWN, StringModel.class, model -> instances.remove(model.getMessage().toLowerCase()));
    }

    public Instance getCurrentInstance() {
        return Optional.ofNullable(currentInstance).orElse(emptyInstance);
    }

    public Instance getInstance(String name) {
        return instances.getOrDefault(name.toLowerCase(), emptyInstance);
    }

    public Instance getInstanceByUser(String userName) {
        return getAllInstances()
                .stream()
                .filter(instance -> instance.getPlayerNames().contains(userName))
                .findFirst()
                .orElse(null);
    }

    public List<Instance> getAllInstances(InstanceType type) {
        return getAllInstances()
                .stream()
                .filter(instance -> type == null || instance.getType() == type)
                .collect(Collectors.toList());
    }

    public Collection<Instance> getAllInstances() {
        return instances.values();
    }

    public int getOnlinePlayers(InstanceType type) {
        return getAllInstances(type)
                .stream()
                .mapToInt(Instance::getOnlinePlayers)
                .sum();
    }

    public int getOnlinePlayers() {
        return getOnlinePlayers(null);
    }
}
