-- DB Schema - MySQL

-- table to store user accounts
DROP TABLE IF EXISTS png_user;
CREATE TABLE png_user (
    uid                 VARCHAR(32),
        PRIMARY KEY (uid),
    ugroup_id           INT,
    uname               VARCHAR(64),
        UNIQUE INDEX (uname),
    upassword           VARCHAR(255),
    uemail              VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;

-- admin account: admin/password
-- MERGE INTO png_user (uid, ugroup, uname, upassword, uemail) KEY (uid)
-- VALUES("1", 1, "admin", "3254a4988474afb14cdf2e3fc3d29066461d756f3647569acda6400aec3aecff", "admin@localhost");


-- table to store application info
DROP TABLE IF EXISTS png_app;
CREATE TABLE png_app (
    aid                 VARCHAR(32),
        PRIMARY KEY (aid),
    adisabled           TINYINT(4)                          NOT NULL DEFAULT 0,
    api_key             VARCHAR(255),
    ios_p12_content     TEXT,
    ios_p12_password    VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;


-- table to store push tokens
DROP TABLE IF EXISTS png_push_token;
CREATE TABLE png_push_token (
    push_token          VARCHAR(128),
    push_os             VARCHAR(32),
        PRIMARY KEY (push_token, push_os),
    timestamp_update    DATETIME,
    tags                TEXT,
    tags_checksum       VARCHAR(64)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;


-- table to store reverse indexing tag -> token
DROP TABLE IF EXISTS png_tag_lookup;
CREATE TABLE png_tag_lookup (
    tag_value           VARCHAR(64),
    push_token          VARCHAR(128),
    push_os             VARCHAR(32),
        PRIMARY KEY (tag_value, push_token, push_os)
) ENGINE=InnoDB DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci;
