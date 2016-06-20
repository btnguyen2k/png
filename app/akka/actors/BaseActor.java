package akka.actors;

import org.apache.commons.lang3.StringUtils;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.cluster.Cluster;
import akka.cluster.pubsub.DistributedPubSub;
import akka.cluster.pubsub.DistributedPubSubMediator;
import akka.cluster.pubsub.DistributedPubSubMediator.SubscribeAck;
import akka.cluster.pubsub.DistributedPubSubMediator.UnsubscribeAck;
import modules.registry.IRegistry;
import play.Logger;

/**
 * Base class for other actors.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public abstract class BaseActor extends UntypedActor {

    private ActorRef distributedPubSubMediator = DistributedPubSub.get(getContext().system())
            .mediator();

    private Cluster cluster = Cluster.get(getContext().system());

    private IRegistry registry;

    public BaseActor(IRegistry registry) {
        this.registry = registry;
    }

    protected IRegistry getRegistry() {
        return registry;
    }

    protected ActorRef getDistributedPubSubMediator() {
        return distributedPubSubMediator;
    }

    protected Cluster getCluster() {
        return cluster;
    }

    /**
     * Publishes a message to a topic.
     * 
     * @param message
     * @param topic
     */
    protected void publishToTopic(Object message, String topic) {
        publishToTopic(message, topic, false);
    }

    /**
     * Publishes a message to a topic.
     * 
     * @param message
     * @param topic
     * @param sendOneMessageToEachGroup
     */
    protected void publishToTopic(Object message, String topic, boolean sendOneMessageToEachGroup) {
        distributedPubSubMediator.tell(
                new DistributedPubSubMediator.Publish(topic, message, sendOneMessageToEachGroup),
                getSelf());
    }

    /**
     * Subscribes to a topic, without a group-id.
     * 
     * @param topic
     */
    protected void subscribeToTopic(String topic) {
        subscribeToTopic(topic, null);
    }

    /**
     * Subscribes to a topic, as a group-id.
     * 
     * @param topic
     * @param groupId
     */
    protected void subscribeToTopic(String topic, String groupId) {
        if (StringUtils.isBlank(groupId)) {
            distributedPubSubMediator
                    .tell(new DistributedPubSubMediator.Subscribe(topic, getSelf()), getSelf());
            Logger.info("{" + getActorName() + "} Subscribed to topic [" + topic + "].");
        } else {
            distributedPubSubMediator.tell(
                    new DistributedPubSubMediator.Subscribe(topic, groupId, getSelf()), getSelf());
            Logger.info("{" + getActorName() + "} Subscribed to topic [" + topic + "] as ["
                    + groupId + "].");
        }
    }

    /**
     * Unsubscribes from a topic, without a group-id.
     * 
     * @param topic
     */
    protected void unsubscribeFromTopic(String topic) {
        unsubscribeFromTopic(topic, null);
    }

    /**
     * Unsubscribes from a topic, as a group-id.
     * 
     * @param topic
     * @param groupId
     */
    protected void unsubscribeFromTopic(String topic, String groupId) {
        if (StringUtils.isBlank(groupId)) {
            distributedPubSubMediator
                    .tell(new DistributedPubSubMediator.Unsubscribe(topic, getSelf()), getSelf());
            Logger.info("{" + getActorName() + "} Unsubscribed from topic [" + topic + "].");
        } else {
            distributedPubSubMediator.tell(
                    new DistributedPubSubMediator.Unsubscribe(topic, groupId, getSelf()),
                    getSelf());
            Logger.info("{" + getActorName() + "} Unsubscribed from topic [" + topic + "] as ["
                    + groupId + "].");
        }
    }

    /**
     * Quick actor's name calculation by using class' {@code getSimpleName()}.
     * 
     * @return
     */
    public String getActorName() {
        return getClass().getSimpleName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof DistributedPubSubMediator.SubscribeAck) {
            // subscribed successfully!
            DistributedPubSubMediator.SubscribeAck ack = (SubscribeAck) message;
            Logger.info("{" + getActorName() + "} Subscribed successfully to [" + ack.subscribe()
                    + "].");
        } else if (message instanceof DistributedPubSubMediator.UnsubscribeAck) {
            // unsubscribed successfully!
            DistributedPubSubMediator.UnsubscribeAck ack = (UnsubscribeAck) message;
            Logger.info("{" + getActorName() + "} Unsubscribed successfully from ["
                    + ack.unsubscribe() + "].");
        } else {
            unhandled(message);
        }
    }
}
