package modules.registry;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import bo.app.IAppDao;
import bo.user.IUserDao;
import forms.FormCreateEditApplication;
import forms.FormLogin;
import play.data.validation.ValidationError;
import play.mvc.Controller;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;
import play.mvc.Http.RequestBody;
import utils.PngConstants;
import utils.PngGlobals;
import utils.UserUtils;

@Singleton
public class FormValidatorImpl implements IFormValidator {

    protected Provider<IRegistry> registry;

    @Inject
    public FormValidatorImpl(Provider<IRegistry> registry) {
        PngGlobals.formValidator = this;

        this.registry = registry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValidationError> validate(FormLogin form) {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        String username = !StringUtils.isBlank(form.username) ? form.username.trim().toLowerCase()
                : null;
        String password = !StringUtils.isBlank(form.password) ? form.password.trim() : null;

        if (username == null || password == null) {
            errors.add(new ValidationError("username", "error.login.failed||"));
            return errors;
        }

        IUserDao userDao = registry.get().getUserDao();
        form.user = userDao.getUserByUsername(username);
        if (!UserUtils.authenticate(form.user, password)) {
            errors.add(new ValidationError("username", "error.login.failed||"));
            return errors;
        }

        return errors.isEmpty() ? null : errors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<ValidationError> validate(FormCreateEditApplication form) {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        String id = !StringUtils.isBlank(form.id) ? form.id.trim().toLowerCase() : null;
        if (StringUtils.isBlank(id)) {
            errors.add(new ValidationError("id", "error.app.empty_id||"));
            return errors;
        }
        if (!StringUtils.equalsIgnoreCase(form.id, form.editId)) {
            IAppDao appDao = registry.get().getAppDao();
            if (appDao.getApp(id) != null) {
                errors.add(new ValidationError("id", "error.app.exists||" + id));
            }
        }

        String apiKey = !StringUtils.isBlank(form.apiKey) ? form.apiKey.trim().toLowerCase() : null;
        if (StringUtils.isBlank(apiKey)) {
            errors.add(new ValidationError("apiKey", "error.app.empty_api_key||"));
        }

        RequestBody rBody = Controller.request().body();
        MultipartFormData<File> mpartData = rBody != null ? rBody.asMultipartFormData() : null;
        FilePart<File> p12FilePart = mpartData != null ? mpartData.getFile("iOSP12File") : null;
        if (p12FilePart != null) {
            File file = p12FilePart.getFile();
            long fileSize = file.length();
            if (fileSize > PngConstants.MAX_IOS_P12_FILE_SIZE) {
                errors.add(new ValidationError("iOSP12File", "error.app.invalid_ios_p12_file||"
                        + fileSize + "||" + PngConstants.MAX_IOS_P12_FILE_SIZE));
            } else {
                if (fileSize > 0) {
                    try {
                        form.iOSP12Content = FileUtils.readFileToByteArray(file);
                    } catch (IOException e) {
                        errors.add(new ValidationError("iOSP12File", e.getMessage()));
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        form.id = id;
        form.apiKey = apiKey;
        form.iOSP12Password = form.iOSP12Password != null ? form.iOSP12Password.trim() : "";

        return errors.isEmpty() ? null : errors;
    }

}
