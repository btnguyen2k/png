package controllers;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.queue.IQueue;

import akka.util.ByteString;
import bo.pushtoken.IPushTokenDao;
import bo.pushtoken.PushTokenBo;
import compositions.ApiAuthRequired;
import play.Logger;
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import queue.message.SendPushNotificationsMessage;
import utils.PngConstants;
import utils.PngUtils;

/**
 * Controller that handles API requests.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ApiController extends BaseController {

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRequestContent() throws IOException {
        try {
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
        } catch (Exception e) {
            Logger.warn("Error while parsing request params: " + e.getMessage(), e);
            return null;
        }
    }

    public final static String HEADER_API_KEY = "api_key";
    public final static String HEADER_APP_ID = "app_id";
    public final static String PARAM_TOKEN = "token";
    public final static String PARAM_OS = "os";
    public final static String PARAM_TAGS = "tags";
    public final static String PARAM_TARGETS = "targets";
    public final static String PARAM_TITLE = "title";
    public final static String PARAM_CONTENT = "content";

    // private final static String API_ERROR_INVALID_TAGS = "Parameter [" +
    // PARAM_TAGS
    // + "] is invalid!";

    private Result validateRequestParams(Map<String, Object> params, String... fields) {
        for (String field : fields) {
            String fieldValue = DPathUtils.getValue(params, field, String.class);
            if (StringUtils.isBlank(fieldValue)) {
                return doResponseJson(PngConstants.RESPONSE_CLIENT_ERROR,
                        "Field [" + field + "] is missting or invalid!");
            }
        }
        return null;
    }

    /**
     * Adds/Updates a push notification token.
     * 
     * <p>
     * Params:
     * <ul>
     * <li>{@code token}: (required) String, push notification token.</li>
     * <li>{@code os}: (required) String, OS name/identifier.</li>
     * <li>{@code tags}: (optional) String or array of Strings.</li>
     * </ul>
     * </p>
     * 
     * @return
     */
    @ApiAuthRequired
    public Result apiAddToken() {
        try {
            Map<String, String> reqHeaders = PngUtils.parseRequestHeaders(request(), HEADER_APP_ID);
            String appId = reqHeaders.get(HEADER_APP_ID);

            Map<String, Object> reqParams = parseRequestContent();
            Result result = validateRequestParams(reqParams, PARAM_TOKEN, PARAM_OS);
            if (result != null) {
                return result;
            }
            String token = DPathUtils.getValue(reqParams, PARAM_TOKEN, String.class);
            String os = DPathUtils.getValue(reqParams, PARAM_OS, String.class);

            Collection<String> tags = PngUtils
                    .parseTags(DPathUtils.getValue(reqParams, PARAM_TAGS));

            if (Logger.isDebugEnabled()) {
                String clientIp = PngUtils.getClientIp(request());
                Logger.debug("Request [" + request().uri() + "] from [" + clientIp + "], params: "
                        + reqParams);
            }

            IQueue queue = getRegistry().getQueueAppEvents();
            if (PngUtils.queuePushTokenUpdate(queue, appId, token, os, tags)) {
                return doResponseJson(PngConstants.RESPONSE_OK, "true", true);
            } else {
                Logger.warn("Cannot put push-token-update message to queue.");
                return doResponseJson(PngConstants.RESPONSE_OK, "false", false);
            }
        } catch (Exception e) {
            return doResponseJson(PngConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

    @ApiAuthRequired
    public Result apiGetTokenGet() {
        return apiGetToken();
    }

    @ApiAuthRequired
    public Result apiGetTokenPost() {
        return apiGetToken();
    }

    /**
     * Fetches an existing push notification token.
     * 
     * <p>
     * Params:
     * <ul>
     * <li>{@code token}: (required) String, push notification token.</li>
     * <li>{@code os}: (required) String, OS name/identifier.</li>
     * </ul>
     * </p>
     * 
     * @return
     */
    public Result apiGetToken() {
        try {
            Map<String, String> reqHeaders = PngUtils.parseRequestHeaders(request(), HEADER_APP_ID);
            String appId = reqHeaders.get(HEADER_APP_ID);

            Map<String, Object> reqParams = parseRequestContent();
            Result result = validateRequestParams(reqParams, PARAM_TOKEN, PARAM_OS);
            if (result != null) {
                return result;
            }
            String token = DPathUtils.getValue(reqParams, PARAM_TOKEN, String.class);
            String os = DPathUtils.getValue(reqParams, PARAM_OS, String.class);

            IPushTokenDao pushTokenDao = getRegistry().getPushTokenDao();
            PushTokenBo pushToken = pushTokenDao.getPushToken(appId, token, os);
            if (pushToken != null) {
                Map<String, Object> data = new HashMap<>();
                data.put("app_id", pushToken.getAppId());
                data.put("token", pushToken.getToken());
                data.put("timestamp", pushToken.getTimestampUpdate());
                data.put("tags", pushToken.getTagsAsList());
                data.put("os", pushToken.getOs());
                return doResponseJson(PngConstants.RESPONSE_OK, "Successful", data);
            } else {
                return doResponseJson(PngConstants.RESPONSE_NOT_FOUND, "Push token not found!");
            }

        } catch (Exception e) {
            return doResponseJson(PngConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Deletes an existing push notification token.
     * 
     * <p>
     * Params:
     * <ul>
     * <li>{@code token}: (required) String, push notification token.</li>
     * <li>{@code os}: (required) String, OS name/identifier.</li>
     * </ul>
     * </p>
     * 
     * @return
     */
    @ApiAuthRequired
    public Result apiDeleteToken() {
        try {
            Map<String, String> reqHeaders = PngUtils.parseRequestHeaders(request(), HEADER_APP_ID);
            String appId = reqHeaders.get(HEADER_APP_ID);

            Map<String, Object> reqParams = parseRequestContent();
            Result result = validateRequestParams(reqParams, PARAM_TOKEN, PARAM_OS);
            if (result != null) {
                return result;
            }
            String token = DPathUtils.getValue(reqParams, PARAM_TOKEN, String.class);
            String os = DPathUtils.getValue(reqParams, PARAM_OS, String.class);

            if (Logger.isDebugEnabled()) {
                String clientIp = PngUtils.getClientIp(request());
                Logger.debug("Request [" + request().uri() + "] from [" + clientIp + "], params: "
                        + reqParams);
            }

            IQueue queue = getRegistry().getQueueAppEvents();
            if (PngUtils.queuePushTokenDelete(queue, appId, token, os)) {
                return doResponseJson(PngConstants.RESPONSE_OK, "true", true);
            } else {
                Logger.warn("Cannot put push-token-delete message to queue.");
                return doResponseJson(PngConstants.RESPONSE_OK, "false", false);
            }
        } catch (Exception e) {
            return doResponseJson(PngConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

    /**
     * Push notifications.
     * 
     * <p>
     * Params:
     * <ul>
     * <li>{@code title}: (optional) String, notification's title.</li>
     * <li>{@code content}: (required) String, notification's content.</li>
     * <li>{@code targets}: (required) list of maps, map's fields: {@code tags}
     * or a pair of {@code token} and {@code os}
     * <ul>
     * <li>{@code tags}: String or array of Strings.</li>
     * <li>{@code token}: String, push notification token.</li>
     * <li>{@code os}: String, OS name/identifier.</li>
     * </ul>
     * </li>
     * </ul>
     * </p>
     * 
     * @return
     */
    @SuppressWarnings("unchecked")
    public Result apiPushNotifications() {
        try {
            Map<String, String> reqHeaders = PngUtils.parseRequestHeaders(request(), HEADER_APP_ID);
            String appId = reqHeaders.get(HEADER_APP_ID);

            Map<String, Object> reqParams = parseRequestContent();
            Result result = validateRequestParams(reqParams, PARAM_CONTENT);
            if (result != null) {
                return result;
            }
            String title = DPathUtils.getValue(reqParams, PARAM_TITLE, String.class);
            String content = DPathUtils.getValue(reqParams, PARAM_CONTENT, String.class);
            Collection<SendPushNotificationsMessage.Target> targets = PngUtils
                    .parseTargets(DPathUtils.getValue(reqParams, PARAM_TARGETS, List.class));
            if (targets == null || targets.size() == 0) {
                return doResponseJson(PngConstants.RESPONSE_CLIENT_ERROR,
                        "Field [" + PARAM_TARGETS + "] is missing or invalid!");
            }

            if (Logger.isDebugEnabled()) {
                String clientIp = PngUtils.getClientIp(request());
                Logger.debug("Request [" + request().uri() + "] from [" + clientIp + "], params: "
                        + reqParams);
            }

            IQueue queue = getRegistry().getQueueAppEvents();
            if (PngUtils.queuePushNotificationsSend(queue, appId, title, content, targets)) {
                return doResponseJson(PngConstants.RESPONSE_OK, "true", true);
            } else {
                Logger.warn("Cannot put push-notifications-send message to queue.");
                return doResponseJson(PngConstants.RESPONSE_OK, "false", false);
            }
        } catch (Exception e) {
            return doResponseJson(PngConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }
}
