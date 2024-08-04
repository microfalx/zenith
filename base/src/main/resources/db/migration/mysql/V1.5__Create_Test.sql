CREATE TABLE zenith_test_suite
(
    id          SMALLINT UNSIGNED NOT NULL auto_increment,
    project_id  SMALLINT UNSIGNED NOT NULL,
    session_id  INTEGER UNSIGNED NOT NULL,
    natural_id  VARCHAR(200) NOT NULL,

    name        VARCHAR(200) NOT NULL,
    category    VARCHAR(200),
    tags        VARCHAR(500),
    description VARCHAR(1000),

    CONSTRAINT pk$zenith_test_suite PRIMARY KEY (id),
    CONSTRAINT uk$zenith_test_suite$nk UNIQUE (natural_id),
    CONSTRAINT fk$zenith_test_suite$project FOREIGN KEY (project_id) REFERENCES zenith_project (id),
    CONSTRAINT fk$zenith_test_suite$session FOREIGN KEY (session_id) REFERENCES zenith_session (id)
) ENGINE INNODB;

CREATE TABLE zenith_test
(
    id            SMALLINT UNSIGNED NOT NULL,
    project_id    SMALLINT UNSIGNED NOT NULL,
    session_id    INTEGER UNSIGNED NOT NULL,
    test_suite_id SMALLINT UNSIGNED,
    natural_id    VARCHAR(200) NOT NULL,

    name          VARCHAR(200) NOT NULL,
    `package`     VARCHAR(200),
    category      VARCHAR(200),
    tags          VARCHAR(500),
    description   VARCHAR(1000),

    CONSTRAINT pk$zenith_test PRIMARY KEY (id),
    CONSTRAINT uk$zenith_test$nk UNIQUE (natural_id),
    CONSTRAINT fk$zenith_test$project FOREIGN KEY (project_id) REFERENCES zenith_project (id),
    CONSTRAINT fk$zenith_test$session FOREIGN KEY (session_id) REFERENCES zenith_session (id)
) ENGINE INNODB;

CREATE TABLE zenith_test_execution
(
    id         BIGINT UNSIGNED NOT NULL,
    node_id    SMALLINT UNSIGNED NOT NULL,
    test_id    SMALLINT UNSIGNED NOT NULL,
    project_id SMALLINT UNSIGNED NOT NULL,
    session_id INTEGER UNSIGNED NOT NULL,

    started_at DATETIME NOT NULL,
    ended_at   DATETIME NOT NULL,
    duration   INTEGER  NOT NULL,

    status     ENUM ('SUCCESSFUL','FAILED') NOT NULL,
    reason     ENUM ('TIMEOUT', 'SOCKET_TIMEOUT', 'BROWSER_TIMEOUT', 'ORPHAN', 'CLIENT_GONE',
        'NODE_FAILED', 'CREATION_FAILED', 'REREGISTRATION'),

    CONSTRAINT pk$zenith_test_execution PRIMARY KEY (id),
    CONSTRAINT fk$zenith_test_execution$node FOREIGN KEY (node_id) REFERENCES zenith_node (id),
    CONSTRAINT fk$zenith_test_execution$test FOREIGN KEY (project_id) REFERENCES zenith_project (id),
    CONSTRAINT fk$zenith_test_execution$project FOREIGN KEY (test_id) REFERENCES zenith_test (id),
    CONSTRAINT fk$zenith_test_execution$session FOREIGN KEY (session_id) REFERENCES zenith_session (id)
) ENGINE INNODB;

create index ix$zenith_test_execution$status on zenith_test_execution (status);
create index ix$zenith_test_execution$start on zenith_test_execution (started_at);

CREATE TABLE zenith_test_logs
(
    id       BIGINT UNSIGNED NOT NULL,

    selenium MEDIUMTEXT,
    driver   MEDIUMTEXT,
    browser  MEDIUMTEXT,

    CONSTRAINT pk$zenith_test_logs PRIMARY KEY (id)
) ENGINE INNODB;

