CREATE TABLE zenith_project
(
    id          SMALLINT UNSIGNED    NOT NULL auto_increment,
    natural_id  VARCHAR(200)         NOT NULL,

    name        VARCHAR(200)         NOT NULL,
    active      BOOLEAN DEFAULT TRUE NOT NULL,

    created_at  DATETIME             NOT NULL,
    modified_at DATETIME             NOT NULL,

    description VARCHAR(1000),

    CONSTRAINT pk$zenith_project PRIMARY KEY (id),
    CONSTRAINT uk$zenith_project$nk UNIQUE (natural_id)
) ENGINE INNODB;