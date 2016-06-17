package controllers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.SerializationUtils;
import com.google.inject.Provider;

import modules.cluster.ICluster;
import modules.registry.IRegistry;
import play.data.FormFactory;
import play.i18n.Lang;
import play.i18n.Messages;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Result;
import play.twirl.api.Html;

/**
 * Base class for other controllers.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class BaseController extends Controller {

    @Inject
    protected Provider<ICluster> cluster;

    @Inject
    protected Provider<IRegistry> registry;

    @Inject
    protected FormFactory formFactory;

    @Inject
    protected MessagesApi messagesApi;

    protected ICluster getCluster() {
        return cluster.get();
    }

    protected IRegistry getRegistry() {
        return registry.get();
    }

    protected static Result doResponseJson(int status) {
        return doResponseJson(status, null, null);
    }

    protected static Result doResponseJson(int status, String message) {
        return doResponseJson(status, message, null);
    }

    protected static Result doResponseJson(int status, String message, Object data) {
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("status", status);
        if (!StringUtils.isBlank(message)) {
            dataMap.put("message", message);
        }
        if (data != null) {
            dataMap.put("data", data);
        }
        return doResponseJson(dataMap);
    }

    /**
     * Responses to client a JSON object.
     * 
     * @param data
     * @return
     */
    protected static Result doResponseJson(Object data) {
        return ok(data != null ? SerializationUtils.toJsonString(data) : "").as("application/json");
    }

    protected Lang calcLang() {
        return lang();
    }

    protected Messages calcMessages() {
        return messagesApi.preferred(Collections.singleton(calcLang()));
    }

    /**
     * Utility method to render a HTML page.
     * 
     * @param view
     * @param params
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    protected Html render(String view, Object... params) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException, SecurityException,
            NoSuchMethodException, IllegalArgumentException, InvocationTargetException {
        String clazzName = "views.html." + view;
        Class<?> clazz = Class.forName(clazzName);

        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("render")) {
                Messages messages = calcMessages();
                Object[] combinedParams = new Object[params.length + 1];
                combinedParams[params.length] = messages;
                for (int i = 0; i < params.length; i++) {
                    combinedParams[i] = params[i];
                }
                return (Html) method.invoke(null, combinedParams);
            }
        }
        return null;
    }
}
