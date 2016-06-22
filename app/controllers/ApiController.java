package controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;

import akka.util.ByteString;
import bo.app.AppBo;
import bo.app.IAppDao;
import bo.pushtoken.IPushTokenDao;
import bo.pushtoken.PushTokenBo;
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import utils.PngConstants;

/**
 * Controller that handles API requests.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ApiController extends BaseController {

    protected static Map<String, String> parseRequestHeaders() {
        Map<String, String> result = new HashMap<String, String>();
        Map<String, String[]> headers = request().headers();
        if (headers != null) {
            for (Entry<String, String[]> entry : headers.entrySet()) {
                String key = entry.getKey();
                String[] values = entry.getValue();
                result.put(key, values != null & values.length > 0 ? values[0] : "");
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRequestContent() throws IOException {
        RequestBody requestBody = request().body();
        String requestContent = null;
        JsonNode jsonNode = requestBody.asJson();
        if (jsonNode != null) {
            requestContent = jsonNode.toString();
        } else {
            RawBuffer rawBuffer = requestBody.asRaw();
            if (rawBuffer != null) {
                ByteString buffer = rawBuffer.asBytes();
                if (buffer != null) {
                    requestContent = buffer.toString();
                } else {
                    byte[] buff = FileUtils.readFileToByteArray(rawBuffer.asFile());
                    requestContent = buff != null ? new String(buff, PngConstants.UTF8) : "";
                }
            } else {
                requestContent = requestBody.asText();
            }
        }
        return SerializationUtils.fromJsonString(requestContent, Map.class);
    }

    private final static String HEADER_API_KEY = "api_key";
    private final static String HEADER_APP_ID = "app_id";
    private final static String PARAM_TOKEN = "token";
    private final static String PARAM_OS = "os";
    private final static String PARAM_TAGS = "tags";

    private boolean validateRequest(String appId, String apiKey) {
        IAppDao appDao = getRegistry().getAppDao();
        AppBo app = appDao.getApp(appId);
        return app != null && !app.isDisabled() && StringUtils.equals(app.getApiKey(), apiKey);
    }

    /**
     * Adds/Updates a push notification token.
     * 
     * @return
     */
    public Result apiAddToken() {
        try {
            Map<String, String> reqHeaders = parseRequestHeaders();
            String appId = reqHeaders.get(HEADER_APP_ID);
            String apiKey = reqHeaders.get(HEADER_API_KEY);
            if (!validateRequest(appId, apiKey)) {
                return doResponseJson(PngConstants.RESPONSE_ACCESS_DENIED, "App [" + appId
                        + "] not found or has been disabled, or API key does not match!");
            }

            Map<String, Object> reqParams = parseRequestContent();
            String token = DPathUtils.getValue(reqParams, PARAM_TOKEN, String.class);

            IPushTokenDao pushTokenDao = getRegistry().getPushTokenDao();
            // PushTokenBo pushToken = pushTokenDao.getPushToken(appId, token,
            // os);
            return ok();
        } catch (Exception e) {
            return doResponseJson(PngConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

}
