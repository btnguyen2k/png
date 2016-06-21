package models;

import java.util.List;

import bo.app.AppBo;
import controllers.routes;

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

    public long getIOSP12FileSize() {
        byte[] data = getIOSP12ContentRaw();
        return data != null ? data.length : 0;
    }

    public String urlEdit() {
        return routes.AdminCPController.editApplication(getId()).url();
    }

    public String urlDelete() {
        return routes.AdminCPController.deleteApplication(getId()).url();
    }

}
