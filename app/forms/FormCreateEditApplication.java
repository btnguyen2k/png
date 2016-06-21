package forms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bo.app.AppBo;
import play.data.validation.ValidationError;
import utils.PngGlobals;

public class FormCreateEditApplication extends BaseForm {

    public final static FormCreateEditApplication defaultInstance = new FormCreateEditApplication();

    public static FormCreateEditApplication newInstance(AppBo bo) {
        FormCreateEditApplication form = new FormCreateEditApplication();
        form.id = bo.getId();
        form.isDisabled = bo.isDisabled() ? 1 : 0;
        form.apiKey = bo.getApiKey();
        form.iOSP12Password = bo.getIOSP12Password();
        byte[] iOSP12ContentRaw = bo.getIOSP12ContentRaw();
        form.iOSP12Size = iOSP12ContentRaw != null ? iOSP12ContentRaw.length : 0;
        form.editId = form.id;
        return form;
    }

    public int isDisabled = 0, iOSP12Size = 0;
    public String id = "", apiKey = "", iOSP12Password = "";
    public String editId = "";
    public byte[] iOSP12Content = null;

    public Map<String, String> toMap() {
        Map<String, String> data = new HashMap<String, String>();
        data.put("id", id);
        data.put("editId", editId);
        data.put("isDisabled", String.valueOf(isDisabled));
        data.put("apiKey", apiKey);
        data.put("iOSP12Password", iOSP12Password);
        data.put("iOSP12Size", String.valueOf(iOSP12Size));
        return data;
    }

    public int getIsDisabled() {
        return isDisabled;
    }

    public void setIsDisabled(int isDisabled) {
        this.isDisabled = isDisabled;
    }

    public int getiOSP12Size() {
        return iOSP12Size;
    }

    public void setiOSP12Size(int iOSP12Size) {
        this.iOSP12Size = iOSP12Size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getiOSP12Password() {
        return iOSP12Password;
    }

    public void setiOSP12Password(String iOSP12Password) {
        this.iOSP12Password = iOSP12Password;
    }

    public String getEditId() {
        return editId;
    }

    public void setEditId(String editId) {
        this.editId = editId;
    }

    public List<ValidationError> validate() throws Exception {
        return PngGlobals.formValidator.validate(this);
    }

}
