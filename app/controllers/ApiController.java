package controllers;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ddth.commons.utils.DPathUtils;
import com.github.ddth.commons.utils.DateFormatUtils;
import com.github.ddth.commons.utils.SerializationUtils;
import com.github.ddth.djs.message.BaseMessage;
import com.github.ddth.djs.message.queue.TaskFinishMessage;
import com.github.ddth.djs.message.queue.TaskFireoffMessage;
import com.github.ddth.djs.message.queue.TaskPickupMessage;
import com.github.ddth.djs.utils.DjsConstants;
import com.github.ddth.djs.utils.DjsUtils;
import com.github.ddth.queue.IQueue;
import com.github.ddth.queue.IQueueMessage;
import com.github.ddth.queue.impl.BaseUniversalQueueMessage;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import akka.util.ByteString;
import play.Logger;
import play.mvc.Http.RawBuffer;
import play.mvc.Http.RequestBody;
import play.mvc.Result;
import queue.IQueueService;
import utils.JobUtils;

/**
 * Controller that handles API requests.
 * 
 * @author Thanh Nguyen <btnguyen2k@gmail.com>
 * @since 0.1.0
 */
public class ApiController extends BaseController {

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseRequest() throws IOException {
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
                    requestContent = buff != null ? new String(buff, DjsConstants.UTF8) : "";
                }
            } else {
                requestContent = requestBody.asText();
            }
        }
        return SerializationUtils.fromJsonString(requestContent, Map.class);
    }

    private final static String API_ERROR_CLIENT_ERROR = "Request data is invalid or unable to parse.";
    private final static String API_ERROR_CLIENT_ID_MISSING_INVALID = "Parameter ["
            + DjsConstants.API_PARAM_CLIENT_ID + "] is missing or invalid.";
    private final static String API_ERROR_TASK_INVALID_MISSING = "Parameter ["
            + DjsConstants.API_PARAM_TASK + "] is missing or invalid.";
    private final static String API_OK = "Successful";

    /**
     * Polls a job task from server.
     * 
     * @return
     */
    public Result apiPollTask() {
        try {
            Map<String, Object> reqData = parseRequest();
            if (reqData == null) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR, API_ERROR_CLIENT_ERROR);
            }
            String clientId = DPathUtils.getValue(reqData, DjsConstants.API_PARAM_CLIENT_ID,
                    String.class);
            if (StringUtils.isBlank(clientId)) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR,
                        API_ERROR_CLIENT_ID_MISSING_INVALID);
            }

            IQueueService queueService = getRegistry().getQueueService();
            IQueue queue = queueService.getQueue(clientId);
            IQueueMessage _queueMsg = queue != null ? queue.take() : null;
            try {
                BaseMessage taskMsg = JobUtils.fromQueueMessage(_queueMsg);
                if (_queueMsg != null && Logger.isDebugEnabled()) {
                    Logger.debug("\tTook a message from fireoff queue [" + _queueMsg.qId() + "/"
                            + (taskMsg != null ? taskMsg.id : "[null]") + "], queue size: "
                            + queue.queueSize());
                }

                if (taskMsg instanceof TaskFireoffMessage) {
                    return doResponseJson(DjsConstants.RESPONSE_OK, API_OK,
                            _queueMsg instanceof BaseUniversalQueueMessage
                                    ? ((BaseUniversalQueueMessage) _queueMsg).content()
                                    : taskMsg.toBytes());
                }
                if (_queueMsg != null) {
                    Logger.warn("Invalid fetched queue message [" + _queueMsg.qId() + "]: "
                            + (taskMsg != null ? taskMsg.getClass().getName() : "[null]"));
                } else if (queue == null) {
                    Logger.warn("Cannot obtain queue instance for [" + clientId + "].");
                }
                return doResponseJson(DjsConstants.RESPONSE_NOT_FOUND);
            } finally {
                if (_queueMsg != null) {
                    queue.finish(_queueMsg);
                }
            }
        } catch (Exception e) {
            return doResponseJson(DjsConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

    public Result apiReturnTask() {
        try {
            Map<String, Object> reqData = parseRequest();
            if (reqData == null) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR, API_ERROR_CLIENT_ERROR);
            }

            String taskDataBase64 = DPathUtils.getValue(reqData, DjsConstants.API_PARAM_TASK,
                    String.class);
            byte[] taskData = taskDataBase64 != null ? DjsUtils.base64Decode(taskDataBase64) : null;
            TaskFireoffMessage taskFireoffMsg = null;
            try {
                taskFireoffMsg = TaskFireoffMessage.deserialize(taskData, TaskFireoffMessage.class);
            } catch (Exception e) {
                taskFireoffMsg = null;
            }
            if (taskFireoffMsg == null) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR,
                        API_ERROR_TASK_INVALID_MISSING);
            }

            IQueue queue = getRegistry().getQueueTaskFeedback();
            if (!JobUtils.queueEvent(queue, taskFireoffMsg)) {
                Logger.warn("Cannot put task event to queue: " + taskFireoffMsg);
                return doResponseJson(DjsConstants.RESPONSE_OK, "False", false);
            } else {
                return doResponseJson(DjsConstants.RESPONSE_OK, "True", true);
            }
        } catch (Exception e) {
            return doResponseJson(DjsConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

    public Result apiNotifyTaskPickup() {
        try {
            Map<String, Object> reqData = parseRequest();
            if (reqData == null) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR, API_ERROR_CLIENT_ERROR);
            }

            String taskDataBase64 = DPathUtils.getValue(reqData, DjsConstants.API_PARAM_TASK,
                    String.class);
            byte[] taskData = taskDataBase64 != null ? DjsUtils.base64Decode(taskDataBase64) : null;
            TaskPickupMessage taskPickupMsg = null;
            try {
                taskPickupMsg = TaskPickupMessage.deserialize(taskData, TaskPickupMessage.class);
            } catch (Exception e) {
                taskPickupMsg = null;
            }
            if (taskPickupMsg == null) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR,
                        API_ERROR_TASK_INVALID_MISSING);
            }

            IQueue queue = getRegistry().getQueueTaskFeedback();
            if (!JobUtils.queueEvent(queue, taskPickupMsg)) {
                Logger.warn("Cannot put task event to queue: " + taskPickupMsg);
                return doResponseJson(DjsConstants.RESPONSE_OK, "False", false);
            } else {
                return doResponseJson(DjsConstants.RESPONSE_OK, "True", true);
            }
        } catch (Exception e) {
            return doResponseJson(DjsConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

    public Result apiNotifyTaskFinish() {
        try {
            Map<String, Object> reqData = parseRequest();
            if (reqData == null) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR, API_ERROR_CLIENT_ERROR);
            }

            String taskDataBase64 = DPathUtils.getValue(reqData, DjsConstants.API_PARAM_TASK,
                    String.class);
            byte[] taskData = taskDataBase64 != null ? DjsUtils.base64Decode(taskDataBase64) : null;
            TaskFinishMessage taskFinishMsg = null;
            try {
                taskFinishMsg = TaskFinishMessage.deserialize(taskData, TaskFinishMessage.class);
            } catch (Exception e) {
                taskFinishMsg = null;
            }
            if (taskFinishMsg == null) {
                return doResponseJson(DjsConstants.RESPONSE_CLIENT_ERROR,
                        API_ERROR_TASK_INVALID_MISSING);
            }

            IQueue queue = getRegistry().getQueueTaskFeedback();
            if (!JobUtils.queueEvent(queue, taskFinishMsg)) {
                Logger.warn("Cannot put task event to queue: " + taskFinishMsg);
                return doResponseJson(DjsConstants.RESPONSE_OK, "False", false);
            } else {
                return doResponseJson(DjsConstants.RESPONSE_OK, "True", true);
            }
        } catch (Exception e) {
            return doResponseJson(DjsConstants.RESPONSE_SERVER_ERROR, e.getMessage());
        }
    }

    public Result index() {
        Date now = new Date();
        StringBuilder msg = new StringBuilder("Your application is ready: "
                + DateFormatUtils.toString(now, "yyyy-MM-dd HH:mm:ss"));
        msg.append("<br>\nCluster: ").append(cluster != null ? cluster.get() : "[null]")
                .append("<br>\n");
        msg.append("<br>\nRegistry: ").append(registry != null ? registry.get() : "[null]")
                .append("<br>\n");

        Config conf = ConfigFactory.load().getConfig("master");
        int port = conf.getInt("akka.remote.netty.tcp.port");
        msg.append("Port: " + port);

        // ActorSystem actorSystem = akka.getActorSystem();
        // // ActorRef myActor =
        // // actorSystem.actorOf(Props.create(MyUntypedActor.class),
        // "myactor");
        //
        // ClusterSingletonManagerSettings settings =
        // ClusterSingletonManagerSettings.create(actorSystem);
        // try {
        // actorSystem.actorOf(ClusterSingletonManager.props(MyUntypedActor.PROPS,
        // PoisonPill.getInstance(), settings),
        // "myactor");
        // } catch (InvalidActorNameException e) {
        // e.printStackTrace();
        // }
        // ClusterSingletonProxySettings proxySettings =
        // ClusterSingletonProxySettings.create(actorSystem);
        // ActorRef mySingletonActorRef =
        // actorSystem.actorOf(ClusterSingletonProxy.props("/user/myactor",
        // proxySettings),
        // "myactorProxy");
        // actorSystem.scheduler().schedule(Duration.Zero(), Duration.create(5,
        // TimeUnit.SECONDS), mySingletonActorRef,
        // "Tick", actorSystem.dispatcher(), null);

        response().setHeader(CONTENT_TYPE, "text/html; charset=utf-8");
        return ok(msg.toString());
    }
}
