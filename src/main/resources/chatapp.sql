-- Adminer 4.8.1 MySQL 8.3.0 dump

SET NAMES utf8;
SET time_zone = '+00:00';
SET foreign_key_checks = 0;
SET sql_mode = 'NO_AUTO_VALUE_ON_ZERO';

SET NAMES utf8mb4;

DROP TABLE IF EXISTS `chatgroups`;
CREATE TABLE `chatgroups` (
  `group_id` int NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) NOT NULL,
  PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `groupmembers`;
CREATE TABLE `groupmembers` (
  `group_id` int NOT NULL,
  `username` varchar(255) NOT NULL,
  PRIMARY KEY (`group_id`,`username`),
  KEY `username` (`username`),
  CONSTRAINT `groupmembers_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `chatgroups` (`group_id`),
  CONSTRAINT `groupmembers_ibfk_2` FOREIGN KEY (`username`) REFERENCES `users` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `messages`;
CREATE TABLE `messages` (
    `id` int NOT NULL AUTO_INCREMENT,
    `sender` varchar(255) NOT NULL,
    `receiver` varchar(255) DEFAULT NULL,
    `content` varchar(255) NOT NULL,
    `group_id` int DEFAULT NULL,
    `createdAt` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `sender` (`sender`),
    KEY `receiver` (`receiver`),
    KEY `group_id` (`group_id`),
    CONSTRAINT `messages_ibfk_1` FOREIGN KEY (`sender`) REFERENCES `users` (`username`),
    CONSTRAINT `messages_ibfk_2` FOREIGN KEY (`receiver`) REFERENCES `users` (`username`),
    CONSTRAINT `messages_ibfk_3` FOREIGN KEY (`group_id`) REFERENCES `chatgroups` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `username` varchar(255) NOT NULL DEFAULT 'null',
  `password` varchar(255) DEFAULT 'null',
  `avatar` varchar(255) DEFAULT 'null',
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- 2024-04-11 11:07:05
