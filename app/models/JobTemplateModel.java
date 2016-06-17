package models;

import java.util.Date;
import java.util.List;

import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.djs.bo.job.JobTemplateBo;

import controllers.routes;
import utils.DjsMasterConstants;

public class JobTemplateModel extends JobTemplateBo {
    public static JobTemplateModel newInstance(JobTemplateBo bo) {
        JobTemplateModel model = new JobTemplateModel();
        model.fromMap(bo.toMap());
        return model;
    }

    public static JobTemplateModel[] newInstances(JobTemplateBo[] boList) {
        JobTemplateModel[] models = new JobTemplateModel[boList.length];
        for (int i = 0; i < models.length; i++) {
            models[i] = newInstance(boList[i]);
        }
        return models;
    }

    public static JobTemplateModel[] newInstances(List<JobTemplateBo> boList) {
        return newInstances(boList.toArray(JobTemplateBo.EMPTY_ARRAY));
    }

    public String getUpdateTimestampStr() {
        Date timestamp = getUpdateTimestamp();
        return DateFormatUtils.toString(timestamp, DjsMasterConstants.DF_FULL);
    }

    public String urlEdit() {
        return routes.AdminCPController.editJobTemplate(getId()).url();
    }

    public String urlDelete() {
        return routes.AdminCPController.deleteJobTemplate(getId()).url();
    }

}
