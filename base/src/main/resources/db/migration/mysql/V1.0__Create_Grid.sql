CREATE TABLE zenith_hub
(
    id          SMALLINT UNSIGNED    NOT NULL auto_increment,
    natural_id  VARCHAR(60)          NOT NULL,

    name        VARCHAR(200)         NOT NULL,
    hostname    VARCHAR(200)         NOT NULL,
    port        INT                  NOT NULL,

    created_at  DATETIME             NOT NULL,
    modified_at DATETIME             NOT NULL,
    pinged_at   DATETIME             NOT NULL,

    description VARCHAR(1000),

    CONSTRAINT pk$zenith_hub PRIMARY KEY (id),
    CONSTRAINT uk$zenith_hub$nk UNIQUE (natural_id)
) ENGINE INNODB;

CREATE TABLE zenith_node
(
    id          SMALLINT UNSIGNED    NOT NULL auto_increment,
    natural_id  VARCHAR(60)          NOT NULL,

    name        VARCHAR(200)         NOT NULL,
    hostname    VARCHAR(200)         NOT NULL,
    port        INT                  NOT NULL,

    active      BOOLEAN DEFAULT TRUE NOT NULL,

    created_at  DATETIME             NOT NULL,
    modified_at DATETIME             NOT NULL,
    pinged_at   DATETIME             NOT NULL,

    description VARCHAR(1000),

    CONSTRAINT pk$zenith_node PRIMARY KEY (id),
    CONSTRAINT uk$zenith_node$nk UNIQUE (natural_id)
) ENGINE INNODB;

