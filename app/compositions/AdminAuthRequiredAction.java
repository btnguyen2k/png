package compositions;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Provider;

import org.apache.commons.lang3.exception.ExceptionUtils;

import bo.user.UserBo;
import modules.registry.IRegistry;
import play.mvc.Action;
import play.mvc.Http.Context;
import play.mvc.Result;
import utils.UserUtils;

public class AdminAuthRequiredAction extends Action<AdminAuthRequired> {

    private CompletionStage<Result> goLogin(final Context ctx) {
        return CompletableFuture.supplyAsync(new Supplier<Result>() {
            @Override
            public Result get() {
                String urlReturn = ctx.request().uri();
                return redirect(controllers.routes.AdminCPController.login(urlReturn));
            }
        });
    }

    @Inject
    private Provider<IRegistry> registry;

    @Override
    public CompletionStage<Result> call(Context context) {
        UserBo user = UserUtils.currentAdmin(registry.get(), context.session());
        if (user == null) {
            return goLogin(context);
        }
        try {
            return delegate.call(context);
        } catch (Exception e) {
            return CompletableFuture.supplyAsync(new Supplier<Result>() {
                @Override
                public Result get() {
                    StringBuilder sb = new StringBuilder(
                            "Error occured, refresh the page to retry. If the error persists, please contact site admin for support.");
                    String stacktrace = ExceptionUtils.getStackTrace(e);
                    sb.append("\n\nError details: ").append(e.getMessage()).append("\n")
                            .append(stacktrace);
                    Throwable cause = e.getCause();
                    while (cause != null) {
                        sb.append("\n").append(ExceptionUtils.getStackTrace(cause));
                        cause = cause.getCause();
                    }
                    return ok(sb.toString());
                }
            });
        }
    }

}
