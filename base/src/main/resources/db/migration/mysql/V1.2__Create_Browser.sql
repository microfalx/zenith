CREATE TABLE zenith_browser
(
    id          SMALLINT UNSIGNED NOT NULL auto_increment,
    natural_id  VARCHAR(200)      NOT NULL,

    name        VARCHAR(200)      NOT NULL,
    version     VARCHAR(50)       NOT NULL,

    created_at  DATETIME          NOT NULL,
    modified_at DATETIME          NOT NULL,

    description VARCHAR(1000),

    CONSTRAINT pk$zenith_browser PRIMARY KEY (id),
    CONSTRAINT uk$zenith_browser$nk UNIQUE (natural_id)
) ENGINE INNODB;