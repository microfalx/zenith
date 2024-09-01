CREATE TABLE zenith_session
(
    id             INTEGER UNSIGNED                                          NOT NULL auto_increment,
    browser_id     SMALLINT UNSIGNED                                         NOT NULL,
    project_id     SMALLINT UNSIGNED,

    natural_id     VARCHAR(200)                                              NOT NULL,
    name           VARCHAR(200)                                              NOT NULL,

    category       VARCHAR(200),
    namespace      VARCHAR(200),
    started_at     DATETIME                                                  NOT NULL,
    ended_at       DATETIME,
    duration       INTEGER,
    status         ENUM ('CREATED','RUNNING','SUCCESSFUL','FAILED','KILLED') NOT NULL,
    reason         ENUM ('REGISTRATION','CREATION_FAILED','TIMEOUT','SOCKET_TIMEOUT',
        'BROWSER_TIMEOUT', 'ORPHAN','CLIENT_GONE','NODE_REMOVED','NODE_FAILED'),

    tags           VARCHAR(500),
    description    VARCHAR(1000),
    reason_message VARCHAR(1000),

    CONSTRAINT pk$zenith_session PRIMARY KEY (id),
    CONSTRAINT uk$zenith_session$nk UNIQUE (natural_id),
    CONSTRAINT fk$zenith_session$browser FOREIGN KEY (browser_id) REFERENCES zenith_browser (id),
    CONSTRAINT fk$zenith_session$project FOREIGN KEY (project_id) REFERENCES zenith_project (id)
) ENGINE INNODB;

CREATE TABLE zenith_session_capability
(
    id          INTEGER UNSIGNED NOT NULL auto_increment,
    session_id  INTEGER UNSIGNED NOT NULL,

    name        VARCHAR(200)     NOT NULL,
    value       VARCHAR(200)     NOT NULL,

    category    VARCHAR(200),
    tags        VARCHAR(500),
    description VARCHAR(1000),

    CONSTRAINT pk$zenith_session_capability PRIMARY KEY (id),
    CONSTRAINT uk$zenith_session_capability$nk UNIQUE (name),
    CONSTRAINT fk$zenith_session_capability$session FOREIGN KEY (session_id) REFERENCES zenith_session (id)
) ENGINE INNODB;