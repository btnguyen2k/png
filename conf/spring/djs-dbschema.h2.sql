-- DB Schema for H2Database

CREATE TABLE IF NOT EXISTS djs_user (
    uid                 VARCHAR(32),
        PRIMARY KEY (uid),
    ugroup_id           INT,
    uname               VARCHAR(64),
        UNIQUE INDEX (uname),
    upassword           VARCHAR(255),
    uemail              VARCHAR(255)
);

-- admin account: admin/password
-- MERGE INTO djs_user (uid, ugroup, uname, upassword, uemail) KEY (uid)
-- VALUES("1", 1, "admin", "3254a4988474afb14cdf2e3fc3d29066461d756f3647569acda6400aec3aecff", "admin@localhost");

CREATE TABLE IF NOT EXISTS djs_jobtpl (
    tpl_id                          VARCHAR(32),
        PRIMARY KEY (tpl_id),
    tpl_desc                        VARCHAR(255),
    tpl_update_timestamp            DATETIME,
    tpl_params                      TEXT
);

CREATE TABLE IF NOT EXISTS djs_jobinfo (
    job_id                          VARCHAR(32),
        PRIMARY KEY (job_id),
    job_desc                        VARCHAR(255),
    job_template_id                 VARCHAR(32),
    job_is_running                  INT,
    job_update_timestamp            DATETIME,
    job_tags                        TEXT,
    job_metadata                    TEXT
);

CREATE TABLE IF NOT EXISTS djs_jobexecinfo (
    job_id                          VARCHAR(32),
        PRIMARY KEY (job_id),
    job_last_execute_task_id        VARCHAR(32),
    job_last_fireoff_task_id        VARCHAR(32)
);

CREATE TABLE IF NOT EXISTS djs_tasklog_base (
    task_id                 VARCHAR(32),
        PRIMARY KEY (task_id),
    job_id                  VARCHAR(32),
    task_status             INT                     NOT NULL DEFAULT (0),
    task_message            VARCHAR(255),
    task_error              TEXT,
    task_output             BLOB,
    timestamp_create        DATETIME,
    node_create             VARCHAR(255),
    timestamp_pickup        DATETIME,
    duration_pickup         INT,
    node_pickup             VARCHAR(255),
    timestamp_finish        DATETIME,
    duration_finish         INT
);
CREATE INDEX ON djs_tasklog_base(job_id);
CREATE INDEX ON djs_tasklog_base(timestamp_create);
CREATE INDEX ON djs_tasklog_base(timestamp_pickup);
CREATE INDEX ON djs_tasklog_base(timestamp_finish);

CREATE TABLE IF NOT EXISTS djs_tasklog_latest (
    task_id                 VARCHAR(32),
    PRIMARY KEY (task_id)
);

CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201601 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201602 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201603 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201604 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201605 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201606 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201607 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201608 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201609 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201610 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201611 AS SELECT * FROM djs_tasklog_base;
CREATE TEMP TABLE IF NOT EXISTS djs_tasklog_201612 AS SELECT * FROM djs_tasklog_base;
