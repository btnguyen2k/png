package akka.utils;

public class AkkaConstants {
    /**
     * Cluster node: frontend.
     * 
     * <ul>
     * <li>Handles API requests from clients.</li>
     * <li>Processes messages in app-event queue.</li>
     * </ul>
     */
    public final static String ROLE_FRONTEND = "frontend";

    /**
     * Cluster node: backend.
     * 
     * <ul>
     * <li>Processes messages in push-notification queue & pushes notifications.
     * </li>
     * </ul>
     */
    public final static String ROLE_BACKEND = "backend";
}
