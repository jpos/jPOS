# SQL command to create the table: jposMessageLog
#
# make sure the datatypes work with your database
# if not make changes accordingly

CREATE TABLE jposMessageLog (
      msgId bigint NOT NULL,
      incomingMsg text,
      transformMsg text,
      replyMsg text,
      outgoingMsg text,
      dateAdd datetime,
PRIMARY KEY(msgId),
INDEX jposMessageLog_msgId_INDEX (msgId));

