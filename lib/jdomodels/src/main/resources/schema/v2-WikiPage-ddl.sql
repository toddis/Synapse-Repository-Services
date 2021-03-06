CREATE TABLE IF NOT EXISTS `V2_WIKI_PAGE` (
	`ID`     bigint(20)  NOT NULL,
	`ETAG` char(36) NOT NULL,
	`TITLE` varchar(256) CHARACTER SET latin1 COLLATE latin1_bin DEFAULT NULL,
	`CREATED_BY` bigint(20) NOT NULL,
	`CREATED_ON` bigint(20) NOT NULL,
	`MODIFIED_BY` bigint(20) NOT NULL,
	`MODIFIED_ON` bigint(20) NOT NULL,
	`PARENT_ID` bigint(20) DEFAULT NULL,
	`ROOT_ID` bigint(20) NOT NULL,
	`MARKDOWN_VERSION` bigint(20) NOT NULL,
	PRIMARY KEY (`ID`),
	CONSTRAINT `V2_WIKI_PARENT_FK` FOREIGN KEY (`PARENT_ID`) REFERENCES `V2_WIKI_PAGE` (`ID`) ON DELETE CASCADE,
	CONSTRAINT `V2_WIKI_ROOT_FK` FOREIGN KEY (`ROOT_ID`) REFERENCES `V2_WIKI_PAGE` (`ID`) ON DELETE CASCADE,
	CONSTRAINT `V2_WIKI_CREATED_BY_FK` FOREIGN KEY (`CREATED_BY`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE,
	CONSTRAINT `V2_WIKI_MODIFIED_BY_FK` FOREIGN KEY (`MODIFIED_BY`) REFERENCES `JDOUSERGROUP` (`ID`) ON DELETE CASCADE
)
