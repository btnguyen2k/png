-- DB Schema for Queue - MySQL

DROP TABLE IF EXISTS queue_appevent;
CREATE TABLE queue_appevent (
    queue_id                    BIGINT              AUTO_INCREMENT,
        PRIMARY KEY (queue_id),
    msg_org_timestamp           DATETIME            NOT NULL            COMMENT "Message's original timestamp; when requeued original timestamp will not be changed",
    msg_timestamp               DATETIME            NOT NULL            COMMENT "Message's queue timestamp",
    msg_num_requeues            INT                 NOT NULL DEFAULT 0  COMMENT "How many times message has been requeued",
    msg_content                 BLOB                                    COMMENT "Message's content"
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP TABLE IF EXISTS queue_appevent_ephemeral;
CREATE TABLE queue_appevent_ephemeral (
    queue_id                    BIGINT              NOT NULL,
        PRIMARY KEY (queue_id),
    msg_org_timestamp           DATETIME            NOT NULL,
    msg_timestamp               DATETIME            NOT NULL,
        INDEX (msg_timestamp),
    msg_num_requeues            INT                 NOT NULL DEFAULT 0,
    msg_content                 BLOB
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP TABLE IF EXISTS queue_pushnotification;
CREATE TABLE queue_pushnotification (
    queue_id                    BIGINT              AUTO_INCREMENT,
        PRIMARY KEY (queue_id),
    msg_org_timestamp           DATETIME            NOT NULL            COMMENT "Message's original timestamp; when requeued original timestamp will not be changed",
    msg_timestamp               DATETIME            NOT NULL            COMMENT "Message's queue timestamp",
    msg_num_requeues            INT                 NOT NULL DEFAULT 0  COMMENT "How many times message has been requeued",
    msg_content                 BLOB                                    COMMENT "Message's content"
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

DROP TABLE IF EXISTS queue_pushnotification_ephemeral;
CREATE TABLE queue_pushnotification_ephemeral (
    queue_id                    BIGINT              NOT NULL,
        PRIMARY KEY (queue_id),
    msg_org_timestamp           DATETIME            NOT NULL,
    msg_timestamp               DATETIME            NOT NULL,
        INDEX (msg_timestamp),
    msg_num_requeues            INT                 NOT NULL DEFAULT 0,
    msg_content                 BLOB
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
