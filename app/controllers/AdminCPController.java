package controllers;

import java.util.ArrayList;
import java.util.List;

import akka.actor.ActorRef;
import akka.cluster.pubsub.DistributedPubSub;
import bo.app.AppBo;
import bo.app.IAppDao;
import compositions.AdminAuthRequired;
import forms.FormCreateEditApplication;
import forms.FormLogin;
import models.AppModel;
import play.data.Form;
import play.mvc.Result;
import play.twirl.api.Html;
import utils.PngConstants;
import utils.UserUtils;

/**
 * AdminCP controller.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class AdminCPController extends BaseController {

    private ActorRef distributedPubSubMediator;

    private ActorRef getDistributedPubSubMediator() {
        if (distributedPubSubMediator == null) {
            synchronized (this) {
                if (distributedPubSubMediator == null) {
                    distributedPubSubMediator = DistributedPubSub
                            .get(getRegistry().getActorSystem()).mediator();
                }
            }
        }
        return distributedPubSubMediator;
    }

    public Result test() {
        return ok(getDistributedPubSubMediator().toString());
    }

    // protected void broadcastEventMessage(String topic, BaseMessage msg) {
    // getDistributedPubSubMediator()
    // .tell(new DistributedPubSubMediator.Publish(topic, msg, false), null);
    // }

    /*----------------------------------------------------------------------*/

    public Result index() throws Exception {
        return redirect(controllers.routes.AdminCPController.home());
    }

    public final static String VIEW_HOME = "home";

    @AdminAuthRequired
    public Result home() throws Exception {
        Html html = render(VIEW_HOME);
        return ok(html);
    }

    /*----------------------------------------------------------------------*/
    public final static String VIEW_LOGIN = "login";

    /*
     * Handle GET:/login?returnUrl=xxx
     */
    public Result login(final String returnUrl) throws Exception {
        Form<FormLogin> form = formFactory.form(FormLogin.class);
        Html html = render(VIEW_LOGIN, form);
        return ok(html);
    }

    /*
     * Handle POST:/login?returnUrl=xxx
     */
    public Result loginSubmit(final String returnUrl) throws Exception {
        Form<FormLogin> form = formFactory.form(FormLogin.class).bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_LOGIN, form);
            return ok(html);
        }
        FormLogin model = form.get();
        UserUtils.loginAdmin(model.user, session());
        if (returnUrl != null && returnUrl.startsWith("/")) {
            return redirect(returnUrl);
        } else {
            return redirect(controllers.routes.AdminCPController.home());
        }
    }

    /*----------------------------------------------------------------------*/
    public final static String VIEW_APPLICATION_LIST = "app_list";

    @AdminAuthRequired
    public Result applicationList() throws Exception {
        IAppDao appDao = getRegistry().getAppDao();
        String[] appIdList = appDao.getAllAppIds();
        List<AppBo> apps = new ArrayList<>();
        for (String id : appIdList) {
            AppBo app = appDao.getApp(id);
            if (app != null) {
                apps.add(app);
            }
        }
        Html html = render(VIEW_APPLICATION_LIST, (Object) AppModel.newInstances(apps));
        return ok(html);
    }

    public final static String VIEW_CREATE_APPLICATION = "create_app";

    @AdminAuthRequired
    public Result createApplication() throws Exception {
        Form<FormCreateEditApplication> form = formFactory.form(FormCreateEditApplication.class)
                .bind(FormCreateEditApplication.defaultInstance.toMap());
        form.discardErrors();
        Html html = render(VIEW_CREATE_APPLICATION, form);
        return ok(html);
    }

    @AdminAuthRequired
    public Result createApplicationSubmit() throws Exception {
        Form<FormCreateEditApplication> form = formFactory.form(FormCreateEditApplication.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_CREATE_APPLICATION, form);
            return ok(html);
        }

        FormCreateEditApplication model = form.get();
        AppBo app = AppBo.newInstance();
        app.setId(model.id).setApiKey(model.apiKey).setIOSP12Password(model.iOSP12Password);
        app.setDisabled(model.isDisabled);
        if (model.iOSP12Content != null) {
            app.setIOSP12Content(model.iOSP12Content);
        }
        IAppDao appDao = registry.get().getAppDao();
        appDao.create(app);

        flash(VIEW_APPLICATION_LIST, calcMessages().at("msg.app.create.done", app.getId()));

        return redirect(routes.AdminCPController.applicationList());
    }

    public final static String VIEW_EDIT_APPLICATION = "edit_app";

    @AdminAuthRequired
    public Result editApplication(String id) throws Exception {
        IAppDao appDao = registry.get().getAppDao();
        AppBo app = appDao.getApp(id);
        if (app == null) {
            flash(VIEW_APPLICATION_LIST, PngConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.app.not_found", id));

            return redirect(routes.AdminCPController.applicationList());
        }

        Form<FormCreateEditApplication> form = formFactory.form(FormCreateEditApplication.class)
                .bind(FormCreateEditApplication.newInstance(app).toMap());
        form.discardErrors();
        Html html = render(VIEW_EDIT_APPLICATION, form);
        return ok(html);
    }

    @AdminAuthRequired
    public Result editApplicationSubmit(String id) throws Exception {
        IAppDao appDao = registry.get().getAppDao();
        AppBo app = appDao.getApp(id);
        if (app == null) {
            flash(VIEW_APPLICATION_LIST, PngConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.app.not_found", id));

            return redirect(routes.AdminCPController.applicationList());
        }

        Form<FormCreateEditApplication> form = formFactory.form(FormCreateEditApplication.class)
                .bindFromRequest();
        if (form.hasErrors()) {
            Html html = render(VIEW_EDIT_APPLICATION, form);
            return ok(html);
        }

        FormCreateEditApplication model = form.get();
        app.setApiKey(model.apiKey).setIOSP12Password(model.iOSP12Password);
        app.setDisabled(model.isDisabled);
        if (model.iOSP12Content != null) {
            app.setIOSP12Content(model.iOSP12Content);
        }
        appDao.update(app);

        flash(VIEW_APPLICATION_LIST, calcMessages().at("msg.app.edit.done", app.getId()));

        return redirect(routes.AdminCPController.applicationList());
    }

    public final static String VIEW_DELETE_APPLICATION = "delete_app";

    @AdminAuthRequired
    public Result deleteApplication(String id) throws Exception {
        IAppDao appDao = registry.get().getAppDao();
        AppBo app = appDao.getApp(id);
        if (app == null) {
            flash(VIEW_APPLICATION_LIST, PngConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.app.not_found", id));

            return redirect(routes.AdminCPController.applicationList());
        }

        AppModel model = AppModel.newInstance(app);
        Html html = render(VIEW_DELETE_APPLICATION, model);
        return ok(html);
    }

    @AdminAuthRequired
    public Result deleteApplicationSubmit(String id) throws Exception {
        IAppDao appDao = registry.get().getAppDao();
        AppBo app = appDao.getApp(id);
        if (app == null) {
            flash(VIEW_APPLICATION_LIST, PngConstants.FLASH_MSG_PREFIX_ERROR
                    + calcMessages().at("error.app.not_found", id));

            return redirect(routes.AdminCPController.applicationList());
        }

        appDao.delete(app);

        flash(VIEW_APPLICATION_LIST, calcMessages().at("msg.app.delete.done", app.getId()));

        return redirect(routes.AdminCPController.applicationList());
    }

}
