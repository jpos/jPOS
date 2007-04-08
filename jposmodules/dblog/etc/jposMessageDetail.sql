# SQL command to create the table: jposMessageDetail
#
# make sure the datatypes work with your database
# if not make changes accordingly

CREATE TABLE jposMessageDetail (
      detailId bigint NOT NULL,
      msgId bigint,
      bit varchar(3),
      value varchar(255),
      msgType enum('incomingMsg','transformMsg','replyMsg','outgoingMsg'),
PRIMARY KEY(detailId),
INDEX jposMessageDetail_detailId_INDEX (detailId));

