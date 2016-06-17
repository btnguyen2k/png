package modules.registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.djs.bo.job.IJobDao;
import com.github.ddth.djs.utils.CronFormat;

import bo.user.IUserDao;
import forms.FormCreateEditJobInfo;
import forms.FormCreateEditJobTemplate;
import forms.FormLogin;
import play.data.validation.ValidationError;
import utils.DjsMasterGlobals;
import utils.UserUtils;

@Singleton
public class FormValidatorImpl implements IFormValidator {

    protected Provider<IRegistry> registry;

    @Inject
    public FormValidatorImpl(Provider<IRegistry> registry) {
        DjsMasterGlobals.formValidator = this;

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
    public List<ValidationError> validate(FormCreateEditJobTemplate form) {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        String id = !StringUtils.isBlank(form.id) ? form.id.trim().toLowerCase() : null;
        if (StringUtils.isBlank(id)) {
            errors.add(new ValidationError("id", "error.job_template.empty_id||"));
            return errors;
        }
        if (!StringUtils.equalsIgnoreCase(form.id, form.editId)) {
            IJobDao jobDao = registry.get().getJobDao();
            if (jobDao.getJobTemplate(id) != null) {
                errors.add(new ValidationError("id", "error.job_template.exists||" + id));
                return errors;
            }
        }

        form.id = id;
        form.description = form.description != null ? form.description.trim() : "";
        form.params = form.params != null ? form.params.trim() : "";

        return errors.isEmpty() ? null : errors;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<ValidationError> validate(FormCreateEditJobInfo form) {
        List<ValidationError> errors = new ArrayList<ValidationError>();

        String id = !StringUtils.isBlank(form.id) ? form.id.trim().toLowerCase() : null;
        if (StringUtils.isBlank(id)) {
            errors.add(new ValidationError("id", "error.job_info.empty_id||"));
        }
        if (!StringUtils.equalsIgnoreCase(form.id, form.editId)) {
            IJobDao jobDao = registry.get().getJobDao();
            if (jobDao.getJobTemplate(id) != null) {
                errors.add(new ValidationError("id", "error.job_info.exists||" + id));
            }
        }
        if (!CronFormat.isValidSecondPattern(form.cronSecond)) {
            errors.add(new ValidationError("id",
                    "error.job_info.cron.invalid_second||" + form.cronSecond));
        }
        if (!CronFormat.isValidMinutePattern(form.cronMinute)) {
            errors.add(new ValidationError("id",
                    "error.job_info.cron.invalid_minute||" + form.cronMinute));
        }
        if (!CronFormat.isValidHourPattern(form.cronHour)) {
            errors.add(new ValidationError("id",
                    "error.job_info.cron.invalid_hour||" + form.cronHour));
        }
        if (!CronFormat.isValidDayPattern(form.cronDay)) {
            errors.add(
                    new ValidationError("id", "error.job_info.cron.invalid_day||" + form.cronDay));
        }
        if (!CronFormat.isValidMonthPattern(form.cronMonth)) {
            errors.add(new ValidationError("id",
                    "error.job_info.cron.invalid_month||" + form.cronMonth));
        }
        if (!CronFormat.isValidDowPattern(form.cronDow)) {
            errors.add(
                    new ValidationError("id", "error.job_info.cron.invalid_dow||" + form.cronDow));
        }

        if (!errors.isEmpty()) {
            return errors;
        }

        form.id = id;
        form.description = form.description != null ? form.description.trim() : "";
        form.templateId = form.templateId != null ? form.templateId.trim().toLowerCase() : "";
        {
            String[] tokens = form.tags != null ? form.tags.trim().toLowerCase().split("[,;\\s]+")
                    : ArrayUtils.EMPTY_STRING_ARRAY;
            form.tags = SerializationUtils.toJsonString(tokens);
        }
        try {
            form.paramsMap = form.params != null
                    ? SerializationUtils.fromJsonString(form.params, Map.class) : new HashMap<>();
            form.params = SerializationUtils.toJsonString(form.paramsMap);
        } catch (Exception e) {
            form.params = "{}";
            form.paramsMap = new HashMap<>();
        }
        form.cron = form.cronSecond + " " + form.cronMinute + " " + form.cronHour + " "
                + form.cronDay + " " + form.cronMonth + " " + form.cronDow;
        return errors.isEmpty() ? null : errors;
    }
}
