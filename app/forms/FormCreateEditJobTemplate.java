package forms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.ddth.djs.bo.job.JobTemplateBo;

import play.data.validation.ValidationError;
import utils.DjsMasterGlobals;

public class FormCreateEditJobTemplate extends BaseForm {

    public final static FormCreateEditJobTemplate defaultInstance = new FormCreateEditJobTemplate();

    public static FormCreateEditJobTemplate newInstance(JobTemplateBo bo) {
        FormCreateEditJobTemplate form = new FormCreateEditJobTemplate();
        form.id = bo.getId();
        form.description = bo.getDescription();
        form.params = bo.getParams();
        form.editId = form.id;
        return form;
    }

    public String id = "", description = "", params = "";
    public String editId = "";

    public Map<String, String> toMap() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", id);
        data.put("description", description);
        data.put("params", params);
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

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
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
