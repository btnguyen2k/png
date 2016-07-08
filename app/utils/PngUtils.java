package utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.IdGenerator;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.impl.universal.UniversalQueueMessage;

import bo.pushtoken.PushTokenBo;
import controllers.ApiController;
import play.Logger;
import play.mvc.Http.Request;
import queue.message.BaseMessage;
import queue.message.DeletePushNotificationMessage;
import queue.message.DeliverPushNotificationMessage;
import queue.message.SendPushNotificationsMessage;
import queue.message.UpdatePushNotificationMessage;

public class PngUtils {

    public final static IdGenerator IDGEN = IdGenerator.getInstance(IdGenerator.getMacAddr());

    public static String getClientIp(Request request) {
        String[] headerFields = new String[] { "X-Forwarded-For", "X-Real-IP", "Real-IP" };
        String clientIPHeader = null;
        for (String field : headerFields) {
            clientIPHeader = request.getHeader(field);
            if (!StringUtils.isBlank(clientIPHeader)) {
                if (Logger.isDebugEnabled()) {
                    Logger.debug(
                            "Got client ip [" + clientIPHeader + "] from field [" + field + "]");
                }
                break;
            }
        }
        if (StringUtils.isBlank(clientIPHeader)) {
            clientIPHeader = request.remoteAddress();
            if (Logger.isDebugEnabled()) {
                Logger.debug("Got client ip [" + clientIPHeader + "] from field [Remote-Address]");
            }
        }
        return clientIPHeader;
    }

    public static Collection<String> parseTags(Object tagsData) {
        Set<String> tags = new HashSet<>();
        if (tagsData instanceof List) {
            tagsData = ((List<?>) tagsData).toArray();
        }
        if (tagsData instanceof String[]) {
            for (String tag : (String[]) tagsData) {
                tags.add(tag);
            }
        } else if (tagsData instanceof Object[]) {
            for (Object tag : (Object[]) tagsData) {
                tags.add(tag.toString());
            }
        } else if (tagsData instanceof String) {
            String[] tokens = ((String) tagsData).split("[,;\\s]+");
            for (String tag : tokens) {
                tags.add(tag);
            }
        }
        return tags;
    }

    public static Collection<SendPushNotificationsMessage.Target> parseTargets(
            List<Map<String, Object>> targetsData) {
        if (targetsData == null || targetsData.size() == 0) {
            return null;
        }
        Collection<SendPushNotificationsMessage.Target> result = new HashSet<>();
        for (Map<String, Object> item : targetsData) {
            String token = DPathUtils.getValue(item, ApiController.PARAM_TOKEN, String.class);
            String os = DPathUtils.getValue(item, ApiController.PARAM_OS, String.class);
            Collection<String> tags = parseTags(
                    DPathUtils.getValue(item, ApiController.PARAM_TAGS));
            SendPushNotificationsMessage.Target target = null;
            if (tags != null && tags.size() > 0) {
                target = SendPushNotificationsMessage.Target.newInstance(tags);
            } else {
                target = SendPushNotificationsMessage.Target.newInstance(token, os);
            }
            if (target != null) {
                result.add(target);
            }
        }
        return result;
    }

    public static Map<String, String> parseRequestHeaders(Request request) {
        return parseRequestHeaders(request, ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static Map<String, String> parseRequestHeaders(Request request, String... filters) {
        boolean hasFilters = false;
        if (filters != null && filters.length > 1) {
            Arrays.sort(filters);
            hasFilters = true;
        }
        Map<String, String> result = new HashMap<String, String>();
        Map<String, String[]> headers = request.headers();
        if (headers != null) {
            for (Entry<String, String[]> entry : headers.entrySet()) {
                String key = entry.getKey();
                if (!hasFilters || Arrays.binarySearch(filters, key) >= 0) {
                    String[] values = entry.getValue();
                    result.put(key, values != null & values.length > 0 ? values[0] : "");
                }
            }
        }
        return result;
    }

    /**
     * Counts number of available applications.
     * 
     * @return
     */
    public static int countApplications() {
        return PngGlobals.registry.getAppDao().getAllAppIds().length;
    }

    public static Date extractTimestamp(String id128Hex) {
        return new Date(IdGenerator.extractTimestamp128(id128Hex));
    }

    public static byte[] base64Decode(String encodedStr) {
        return encodedStr != null ? Base64.decodeBase64(encodedStr) : null;
    }

    public static String base64Encode(byte[] data) {
        return data != null ? Base64.encodeBase64String(data) : null;
    }

    public static byte[] toBytes(BaseMessage msg) {
        return SerializationUtils.toByteArray(msg);
    }

    public static <T extends BaseMessage> T fromBytes(byte[] data, Class<T> clazz) {
        return SerializationUtils.fromByteArray(data, clazz);
    }

    public static boolean queuePushNotificationsSend(IQueue queue, String appId, String title,
            String content, Collection<SendPushNotificationsMessage.Target> targets) {
        SendPushNotificationsMessage msg = SendPushNotificationsMessage.newInstance(appId, title,
                content, targets);
        UniversalQueueMessage queueMsg = UniversalQueueMessage.newInstance();
        queueMsg.content(toBytes(msg));
        return queue.queue(queueMsg);
    }

    public static boolean queuePushNotificationDelivery(IQueue queue, String appId, String title,
            String content, PushTokenBo[] pushTokens) {
        DeliverPushNotificationMessage msg = DeliverPushNotificationMessage.newInstance(appId,
                title, content, pushTokens);
        UniversalQueueMessage queueMsg = UniversalQueueMessage.newInstance();
        queueMsg.content(toBytes(msg));
        return queue.queue(queueMsg);
    }

    public static boolean queuePushTokenDelete(IQueue queue, String appId, String token,
            String os) {
        DeletePushNotificationMessage msg = DeletePushNotificationMessage.newInstance(appId, token,
                os);
        UniversalQueueMessage queueMsg = UniversalQueueMessage.newInstance();
        queueMsg.content(toBytes(msg));
        return queue.queue(queueMsg);
    }

    public static boolean queuePushTokenUpdate(IQueue queue, String appId, String token, String os,
            Collection<String> tags) {
        UpdatePushNotificationMessage msg = UpdatePushNotificationMessage.newInstance(appId, token,
                os, tags);
        UniversalQueueMessage queueMsg = UniversalQueueMessage.newInstance();
        queueMsg.content(toBytes(msg));
        return queue.queue(queueMsg);
    }
}
