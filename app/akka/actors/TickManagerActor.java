package akka.actors;

import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.message.bus.JobInfoAddedMessage;
import com.github.ddth.djs.message.bus.JobInfoRemovedMessage;
import com.github.ddth.djs.message.bus.JobInfoStartedMessage;
import com.github.ddth.djs.message.bus.JobInfoStoppedMessage;
import com.github.ddth.djs.message.bus.JobInfoUpdatedMessage;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.utils.AkkaConstants;
import modules.registry.IRegistry;
import play.Logger;

/**
 * Actor on [tick] node(s) that manages (create/update/delete) job-tick actors.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class TickManagerActor extends BaseDjsActor {

    public final static String NAME = TickManagerActor.class.getSimpleName();

    public TickManagerActor(IRegistry registry) {
        super(registry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void preStart() throws Exception {
        subscribeToTopic(AkkaConstants.TOPIC_JOBEVENT);
        super.preStart();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postStop() throws Exception {
        unsubscribeFromTopic(AkkaConstants.TOPIC_JOBEVENT);
        super.postStop();
    }

    protected ActorRef lookupChild(JobInfoBo jobInfo) {
        String path = TickJobActor.calcInstanceName(jobInfo);
        return getContext().getChild(path);
    }

    protected void _eventJobInfoAdded(JobInfoAddedMessage msg) {
        Logger.info("ADDED new job: " + msg.id + "/" + msg.timestampMillis + "/" + msg.jobInfo);
        JobInfoBo jobInfo = msg.jobInfo;
        Props props = TickJobActor.newProps(getRegistry(), jobInfo);
        getContext().actorOf(props, TickJobActor.calcInstanceName(jobInfo));
    }

    protected void _eventJobInfoRemoved(JobInfoRemovedMessage msg) {
        ActorRef actorRef = lookupChild(msg.jobInfo);
        if (actorRef != null) {
            Logger.info("REMOVED job: " + msg.id + "/" + msg.timestampMillis + "/" + msg.jobInfo);
            getContext().stop(actorRef);
        }
    }

    protected void _eventJobInfoUpdated(JobInfoUpdatedMessage msg) {
        ActorRef actorRef = lookupChild(msg.jobInfo);
        if (actorRef != null) {
            Logger.info("UPDATED job: " + msg.id + "/" + msg.timestampMillis + "/" + msg.jobInfo);
            actorRef.tell(msg, getSelf());
        }
    }

    protected void _eventJobInfoUpdated(JobInfoStartedMessage msg) {
        ActorRef actorRef = lookupChild(msg.jobInfo);
        if (actorRef != null) {
            Logger.info("STARTED job: " + msg.id + "/" + msg.timestampMillis + "/" + msg.jobInfo);
            actorRef.tell(msg, getSelf());
        }
    }

    protected void _eventJobInfoUpdated(JobInfoStoppedMessage msg) {
        ActorRef actorRef = lookupChild(msg.jobInfo);
        if (actorRef != null) {
            Logger.info("STOPPED job: " + msg.id + "/" + msg.timestampMillis + "/" + msg.jobInfo);
            actorRef.tell(msg, getSelf());
        }
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof JobInfoAddedMessage) {
            _eventJobInfoAdded((JobInfoAddedMessage) message);
        } else if (message instanceof JobInfoRemovedMessage) {
            _eventJobInfoRemoved((JobInfoRemovedMessage) message);
        } else if (message instanceof JobInfoUpdatedMessage) {
            _eventJobInfoUpdated((JobInfoUpdatedMessage) message);
        } else if (message instanceof JobInfoStartedMessage) {
            _eventJobInfoUpdated((JobInfoStartedMessage) message);
        } else if (message instanceof JobInfoStoppedMessage) {
            _eventJobInfoUpdated((JobInfoStoppedMessage) message);
        } else {
            super.onReceive(message);
        }
    }
}
