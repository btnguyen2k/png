package models;

import java.util.Date;
import java.util.List;

import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.djs.bo.job.JobTemplateBo;

import bo.app.AppBo;
import controllers.routes;
import utils.PngConstants;

public class AppModel extends AppBo {
    public static AppModel newInstance(AppBo bo) {
        AppModel model = new AppModel();
        model.fromMap(bo.toMap());
        return model;
    }

    public static AppModel[] newInstances(AppBo[] boList) {
        AppModel[] models = new AppModel[boList.length];
        for (int i = 0; i < models.length; i++) {
            models[i] = newInstance(boList[i]);
        }
        return models;
    }

    public static AppModel[] newInstances(List<AppBo> boList) {
        return newInstances(boList.toArray(AppBo.EMPTY_ARRAY));
    }

    public String urlEdit() {
        return routes.AdminCPController.editApplication(getId()).url();
    }

    public String urlDelete() {
        return routes.AdminCPController.deleteApplication(getId()).url();
    }

}
