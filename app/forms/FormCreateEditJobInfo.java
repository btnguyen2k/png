package forms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.djs.bo.job.JobInfoBo;
import com.github.ddth.djs.utils.CronFormat;

import play.data.validation.ValidationError;
import utils.DjsMasterGlobals;

public class FormCreateEditJobInfo extends BaseForm {

    public final static FormCreateEditJobInfo defaultInstance = new FormCreateEditJobInfo();

    public static FormCreateEditJobInfo newInstance(JobInfoBo bo) {
        FormCreateEditJobInfo form = new FormCreateEditJobInfo();
        form.id = bo.getId();
        form.description = bo.getDescription();
        form.templateId = bo.getTemplateId();
        {
            form.tagList = bo.getTagsAsArray();
            form.tags = form.tagList != null ? StringUtils.join(form.tagList, ",") : "";
        }
        {
            form.paramsMap = bo.getParams();
            form.params = form.paramsMap != null ? SerializationUtils.toJsonString(form.paramsMap)
                    : "{}";
        }
        CronFormat cronFormat = bo.getCronFormat();
        if (cronFormat != null) {
            form.cronSecond = cronFormat.getSecond();
            form.cronMinute = cronFormat.getMinute();
            form.cronHour = cronFormat.getHour();
            form.cronDay = cronFormat.getDayOfMonth();
            form.cronMonth = cronFormat.getMonth();
            form.cronDow = cronFormat.getDayOfWeek();
        }
        form.cron = form.cronSecond + " " + form.cronMinute + " " + form.cronHour + " "
                + form.cronDay + " " + form.cronMonth + " " + form.cronDow;
        form.editId = form.id;
        return form;
    }

    public String id = "", description = "", templateId = "", tags = "", params = "{}";
    public String cronSecond = "0", cronMinute = "*", cronHour = "*", cronDay = "*",
            cronMonth = "*", cronDow = "*", cron = "";
    public String editId = "";
    public String[] tagList = ArrayUtils.EMPTY_STRING_ARRAY;
    public Map<String, Object> paramsMap = new HashMap<>();

    public Map<String, String> toMap() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", id);
        data.put("description", description);
        data.put("templateId", templateId);
        data.put("tags", tags);
        data.put("params", params);
        data.put("cronSecond", cronSecond);
        data.put("cronMinute", cronMinute);
        data.put("cronHour", cronHour);
        data.put("cronDay", cronDay);
        data.put("cronMonth", cronMonth);
        data.put("cronDow", cronDow);
        data.put("editId", editId);
        return data;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getCronSecond() {
        return cronSecond;
    }

    public void setCronSecond(String cronSecond) {
        this.cronSecond = cronSecond;
    }

    public String getCronMinute() {
        return cronMinute;
    }

    public void setCronMinute(String cronMinute) {
        this.cronMinute = cronMinute;
    }

    public String getCronHour() {
        return cronHour;
    }

    public void setCronHour(String cronHour) {
        this.cronHour = cronHour;
    }

    public String getCronDay() {
        return cronDay;
    }

    public void setCronDay(String cronDay) {
        this.cronDay = cronDay;
    }

    public String getCronMonth() {
        return cronMonth;
    }

    public void setCronMonth(String cronMonth) {
        this.cronMonth = cronMonth;
    }

    public String getCronDow() {
        return cronDow;
    }

    public void setCronDow(String cronDow) {
        this.cronDow = cronDow;
    }

    public String getEditId() {
        return editId;
    }

    public void setEditId(String editId) {
        this.editId = editId;
    }

    public List<ValidationError> validate() throws Exception {
        return DjsMasterGlobals.formValidator.validate(this);
    }

}
