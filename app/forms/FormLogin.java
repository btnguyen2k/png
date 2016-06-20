package forms;

import java.util.List;

import bo.user.UserBo;
import play.data.validation.ValidationError;
import utils.PngGlobals;

public class FormLogin extends BaseForm {

    public String username, password;
    public UserBo user;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<ValidationError> validate() throws Exception {
        return PngGlobals.formValidator.validate(this);
    }

}
