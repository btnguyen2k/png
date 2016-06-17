package modules.registry;

import java.util.List;

import forms.FormCreateEditJobInfo;
import forms.FormCreateEditJobTemplate;
import forms.FormLogin;
import play.data.validation.ValidationError;

public interface IFormValidator {
    /**
     * Validates a {@link FormLogin} form.
     * 
     * @param form
     * @return
     */
    public List<ValidationError> validate(FormLogin form);

    /**
     * Validates a {@link FormCreateEditJobTemplate} form.
     * 
     * @param form
     * @return
     */
    public List<ValidationError> validate(FormCreateEditJobTemplate form);

    /**
     * Validates a {@link FormCreateEditJobInfo} form.
     * 
     * @param form
     * @return
     */
    public List<ValidationError> validate(FormCreateEditJobInfo form);
}
