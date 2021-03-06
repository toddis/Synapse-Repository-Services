CREATE TABLE IF NOT EXISTS `DAEMON_STATUS` (
  `ID` bigint(20) NOT NULL AUTO_INCREMENT,
  `BACKUP_URL` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `LOG` mediumblob,
  `ERROR_DETAILS` mediumblob,
  `ERROR_MESSAGE` varchar(3000) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
  `PROGRESS_CURRENT` bigint(20) NOT NULL,
  `PROGRESS_MESSAGE` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `PROGRESS_TOTAL` bigint(20) NOT NULL,
  `STARTED_BY` bigint(20),
  `STARTED_ON` bigint(20) NOT NULL,
  `STATUS` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  `RUN_TIME_MS` bigint(20) NOT NULL,
  `TYPE` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL,
  PRIMARY KEY (`ID`)
)

