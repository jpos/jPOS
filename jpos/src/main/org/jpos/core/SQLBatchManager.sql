-- $Id$
--
--
DROP TABLE IF EXISTS journal;

CREATE TABLE journal (
    rrn CHAR(12) NOT NULL,
    pan CHAR(19) NOT NULL,
    exp CHAR(4),
    amount DECIMAL (10,2) NOT NULL,
    PRIMARY KEY (rrn),
    INDEX rrn (rrn)
);


