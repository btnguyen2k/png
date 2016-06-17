package models;

import java.util.Date;
import java.util.List;

import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.djs.bo.log.TaskLogBo;

import controllers.routes;
import utils.DjsMasterConstants;

public class TaskLogModel extends TaskLogBo {
    public static TaskLogModel newInstance(TaskLogBo bo) {
        TaskLogModel model = new TaskLogModel();
        model.fromMap(bo.toMap());
        return model;
    }

    public static TaskLogModel[] newInstances(TaskLogBo[] boList) {
        TaskLogModel[] models = new TaskLogModel[boList.length];
        for (int i = 0; i < models.length; i++) {
            models[i] = newInstance(boList[i]);
        }
        return models;
    }

    public static TaskLogModel[] newInstances(List<TaskLogBo> boList) {
        return newInstances(boList.toArray(TaskLogBo.EMPTY_ARRAY));
    }

    public String getTimestampCreateStr() {
        Date timestamp = getTimestampCreate();
        return timestamp != null ? DateFormatUtils.toString(timestamp, DjsMasterConstants.DF_HHMMSS)
                : "[null]";
    }

    public String getTimestampPickupStr() {
        Date timestamp = getTimestampPickup();
        return timestamp != null ? DateFormatUtils.toString(timestamp, DjsMasterConstants.DF_HHMMSS)
                : "[null]";
    }

    public String getTimestampFinishStr() {
        Date timestamp = getTimestampFinish();
        return timestamp != null ? DateFormatUtils.toString(timestamp, DjsMasterConstants.DF_HHMMSS)
                : "[null]";
    }

    public String urlEdit() {
        return routes.AdminCPController.editJob(getId()).url();
    }

    public String urlDelete() {
        return routes.AdminCPController.deleteJob(getId()).url();
    }

}
