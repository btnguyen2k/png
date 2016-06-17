package akka.actors;

import java.util.Date;

import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.message.bus.TickMessage;

import modules.registry.IRegistry;

public class WorkerDummyActor extends TickJobActor {

    public WorkerDummyActor(IRegistry registry, JobInfoBo jobInfo) {
        super(registry, jobInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doTick(TickMessage tick) {
        Date d = new Date(tick.timestampMillis);
        System.out.println("=========={" + getActorName() + "} TICK matches [" + d + "] against ["
                + getJobInfo().getCron() + "]");
    }
}
