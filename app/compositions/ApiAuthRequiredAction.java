package compositions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.SerializationUtils;

import bo.app.AppBo;
import bo.app.IAppDao;
import controllers.ApiController;
import modules.registry.IRegistry;
import play.Logger;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import utils.PngConstants;
import utils.PngUtils;

public class ApiAuthRequiredAction extends Action<AdminAuthRequired> {

    @Inject
    private Provider<IRegistry> registry;

    @Override
    public CompletionStage<Result> call(Context context) {
        Map<String, String> headers = PngUtils.parseRequestHeaders(context.request(),
                ApiController.HEADER_APP_ID, ApiController.HEADER_API_KEY);
        String appId = headers.get(ApiController.HEADER_APP_ID);
        String apiKey = headers.get(ApiController.HEADER_API_KEY);

        if (Logger.isDebugEnabled()) {
            String clientIp = PngUtils.getClientIp(context.request());
            Logger.debug("Request [" + context.request().uri() + "] from [" + clientIp
                    + "], headers: " + headers);
        }

        IAppDao appDao = registry.get().getAppDao();
        AppBo app = appDao.getApp(appId);
        boolean ok = app != null && !app.isDisabled()
                && StringUtils.equals(app.getApiKey(), apiKey);
        if (!ok) {
            Logger.warn("App [" + appId + "] "
                    + (app == null ? "not found"
                            : (app.isDisabled() ? "is disabled"
                                    : ("does not match the supplied API key [" + apiKey + "]"))));
            return CompletableFuture.supplyAsync(new Supplier<Result>() {
                @Override
                public Result get() {
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", PngConstants.RESPONSE_ACCESS_DENIED);
                    response.put("status", "App [" + appId
                            + "] not found or has been disabled, or API key does not match!");
                    return ok(SerializationUtils.toJsonString(response)).as("application/json");
                }
            });
        } else {
            return delegate.call(context);
        }
    }

}
