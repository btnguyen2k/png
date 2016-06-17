package models;

import java.util.Date;
import java.util.List;

import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.djs.bo.job.JobInfoBo;

import controllers.routes;
import utils.DjsMasterConstants;

public class JobInfoModel extends JobInfoBo {
    public static JobInfoModel newInstance(JobInfoBo bo) {
        JobInfoModel model = new JobInfoModel();
        model.fromMap(bo.toMap());
        return model;
    }

    public static JobInfoModel[] newInstances(JobInfoBo[] boList) {
        JobInfoModel[] models = new JobInfoModel[boList.length];
        for (int i = 0; i < models.length; i++) {
            models[i] = newInstance(boList[i]);
        }
        return models;
    }

    public static JobInfoModel[] newInstances(List<JobInfoBo> boList) {
        return newInstances(boList.toArray(JobInfoBo.EMPTY_ARRAY));
    }

    public String getUpdateTimestampStr() {
        Date timestamp = getUpdateTimestamp();
        return DateFormatUtils.toString(timestamp, DjsMasterConstants.DF_FULL);
    }

    public String urlEdit() {
        return routes.AdminCPController.editJob(getId()).url();
    }

    public String urlDelete() {
        return routes.AdminCPController.deleteJob(getId()).url();
    }

}
