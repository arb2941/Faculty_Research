/**
  * Bain, Brandon
  * Benkleman, Adam
  * Bhakta, Aisha
  * Ismaili, Shpend
  * Nura, Leon
  *
  * ISTE-330.02
  * Group 01
  */

DROP DATABASE IF EXISTS faculty_research_group1;

CREATE DATABASE IF NOT EXISTS `faculty_research_group1`;
USE `faculty_research_group1`;

/* LOGIN TABLE */
DROP TABLE IF EXISTS `login`;
CREATE TABLE `login` (
	`username` VARCHAR(20) NOT NULL DEFAULT '',
	`password` VARCHAR(50) NOT NULL DEFAULT '',
	PRIMARY KEY (`username`)
);
/* LOGIN TEST DATA */
LOCK TABLES `login` WRITE;
INSERT INTO `login`
VALUES ('habermasJim', 'c5f8b49d5509c943fb6daf48ef6682a6496cf3b7'),
	   ('defenbaughGeorge', '1910986a3260cc5c72084386eec1727f7e631e21'),
	   ('smedleyRichard', '20cd3f85cd81dea90482a21cb95bda8d7d6e2a1a'),
	   ('ericsonBarbara', '966bfdaca0504eab040a81b041ba8041c9ec4757'),
	   ('brandon', ''),
	   ('adam', ''),
	   ('aisha', ''),
	   ('shpend', ''),
	   ('leon', ''),
	   ('hardisonAlec', '');
UNLOCK TABLES;


/* FACULTY TABLE */
DROP TABLE IF EXISTS `faculty`;
CREATE TABLE `faculty` (
	`username` VARCHAR(20) NOT NULL DEFAULT '',
	`name` VARCHAR(50) NOT NULL DEFAULT '',
	`email` VARCHAR(50) NOT NULL DEFAULT '',
	`building_no` INT NOT NULL DEFAULT '0',
	`office_no` INT NOT NULL DEFAULT '0',
	PRIMARY KEY (`username`),
	CONSTRAINT `faculty_ibfk_1` FOREIGN KEY (`username`) REFERENCES `login` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
);
/* FACULTY TEST DATA */
LOCK TABLES `faculty` WRITE;
INSERT INTO `faculty`
VALUES ('habermasJim', 'Jim Habermas', 'jim.habermas@rit.edu', 2, 330),
	   ('defenbaughGeorge', 'George Defenbaugh', 'george.defenbaugh@rit.edu', 1, 111),
	   ('smedleyRichard', 'Richard Smedley', 'richard.smedley@rit.edu', 2, 222),
	   ('ericsonBarbara', 'Barbara Ericson', 'barbara.ericson@rit.edu', 3, 333);
UNLOCK TABLES;


/* STUDENT TABLE */
DROP TABLE IF EXISTS `student`;
CREATE TABLE `student` (
	`username` VARCHAR(20) NOT NULL DEFAULT '',
	`name` VARCHAR(50) NOT NULL DEFAULT '',
	`email` VARCHAR(50) NOT NULL DEFAULT '',
	PRIMARY KEY (`username`),
	CONSTRAINT `student_ibfk_1` FOREIGN KEY (`username`) REFERENCES `login` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
);
/* STUDENT TEST DATA */
LOCK TABLES `student` WRITE;
INSERT INTO `student`
VALUES ('brandon', 'Brandon Bain', 'bdb7305@rit.edu'),
	   ('adam', 'Adam Benkleman', 'arb2941@rit.edu'),
	   ('aisha', 'Aisha Bhakta', 'atb6376@rit.edu'),
	   ('shpend', 'Shpend Ismaili', 'si4778@rit.edu'),
	   ('leon', 'Leon Nura', 'ln3855@rit.edu');
UNLOCK TABLES;


/* OUTSIDE TABLE */
DROP TABLE IF EXISTS `outside`;
CREATE TABLE `outside` (
	`username` VARCHAR(20) NOT NULL DEFAULT '',
	`name` VARCHAR(50) NOT NULL DEFAULT '',
	`email` VARCHAR(50) NOT NULL DEFAULT '',
	PRIMARY KEY (`username`),
	CONSTRAINT `outside_ibfk_1` FOREIGN KEY (`username`) REFERENCES `login` (`username`) ON DELETE CASCADE ON UPDATE CASCADE
);
/* OUTSIDE TEST DATA */
LOCK TABLES `outside` WRITE;
INSERT INTO `outside`
VALUES ('hardisonAlec', 'Alec Hardison', 'alec.hardison@gmail.com');
UNLOCK TABLES;


/* ABSTRACTS TABLE */
DROP TABLE IF EXISTS `abstracts`;
CREATE TABLE `abstracts` (
	`abstract_id` INT NOT NULL AUTO_INCREMENT,
	`content` LONGTEXT NOT NULL,
	`type` ENUM('book', 'speaking_engagement') DEFAULT 'book',
	PRIMARY KEY (`abstract_id`)
);
/* ABSTRACTS TEST DATA */
LOCK TABLES `abstracts` WRITE;
INSERT INTO `abstracts`
VALUES (1, "This book, Learn C and C++ by Samples written by James R. Habermas, is a companion to A First Book Ansi C++ by Gary Bronson. It is the author's firm belief that one can never have too many samples. If a textbook is to be useful, it needs primary support through an instructor and/or more samples. This textbook contains a wealth of useful C & C++ samples that are fashioned to further demonstrate the topics outlined in the text.", 'book'),
	   (2, "ISTE-330 is a class about SQL, Databases, and Java to create functional programs. We will also go over the use of Prepared Statements in these programs.", 'speaking_engagement'),
	   (3, "This book presents ‘standard’ C, i.e., code that compiles cleanly with a compiler that meets the ANSI C standard. This book has over 90 example programs that illustrate the topics of each chapters. In addition complete working programs are developed fully, from design to program output. This book is filled with Antibugging Notes (the stress traps to be avoided), and Quick Notes, that emphasize important points to be remembered.", 'book'),
	   (4, "The programming language used in this book is Python. Python has been described as “executable pseudo-code.” I have found that both computer science majors and non majors can learn Python. Since Python is actually used for communications tasks (e.g., Web site Development), it’s relevant language for an in introductory computing course. The specific dialect of Python used in this book is Jython. Jython is Python. The differences between Python (normally implemented in C) and Jython (which is implemented in Java) are akin to the differences between any two language implementations (e.g., Microsoft vs. GNU C++ implementations).", 'book');
UNLOCK TABLES;


/* FACULTY ABSTRACTS TABLE */
DROP TABLE IF EXISTS `facultyAbstracts`;
CREATE TABLE `facultyAbstracts` (
	`faculty_abstract_id` INT NOT NULL AUTO_INCREMENT,
	`username` VARCHAR(20) NOT NULL DEFAULT '',
	`abstract_id` INT NOT NULL DEFAULT '0',
	PRIMARY KEY (`faculty_abstract_id`),
	CONSTRAINT `facultyAbstracts_ibfk_1` FOREIGN KEY (`username`) REFERENCES `login` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT `facultyAbstracts_ibfk_2` FOREIGN KEY (`abstract_id`) REFERENCES `abstracts` (`abstract_id`)
);
/* FACULTY ABSTRACTS TEST DATA */
LOCK TABLES `facultyAbstracts` WRITE;
INSERT INTO `facultyAbstracts`
VALUES (1, 'habermasJim', 1),
	   (2, 'habermasJim', 2),
	   (3, 'defenbaughGeorge', 3),
	   (4, 'smedleyRichard', 3),
	   (5, 'ericsonBarbara', 4);
UNLOCK TABLES;


/* INTERESTS TABLE */
DROP TABLE IF EXISTS `interests`;
CREATE TABLE `interests` (
	`interest_id` INT NOT NULL AUTO_INCREMENT,
	`content` LONGTEXT NOT NULL,
	PRIMARY KEY (`interest_id`)
);
/* INTERESTS TEST DATA */
LOCK TABLES `interests` WRITE;
INSERT INTO `interests`
VALUES (1, 'SQL'),
	   (2, 'Database'),
	   (3, 'Java'),
	   (4, 'cars'),
	   (5, 'Python'),
	   (6, 'C++'),
	   (7, 'C#'),
	   (8, 'Visual Basic'),
	   (9, 'HTML'),
	   (10, 'CSS'),
	   (11, 'JavaScript'),
	   (12, 'PHP'),
	   (13, 'postgresql'),
	   (14, 'Go'),
	   (15, 'Devops'),
	   (16, 'Anime'),
	   (17, 'code'),
	   (18, 'hack'),
	   (19, 'computer'),
	   (20, 'program'),
	   (21, 'cs'),
	   (22, 'cybersecurity');
UNLOCK TABLES;


/* USER INTERESTS TABLE */
DROP TABLE IF EXISTS `userInterests`;
CREATE TABLE `userInterests` (
	`user_interest_id` INT NOT NULL AUTO_INCREMENT,
	`username` VARCHAR(20) NOT NULL DEFAULT '',
	`interest_id` INT NOT NULL DEFAULT '0',
	PRIMARY KEY (`user_interest_id`),
	CONSTRAINT `userInterests_ibfk_1` FOREIGN KEY (`username`) REFERENCES `login` (`username`) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT `userInterests_ibfk_2` FOREIGN KEY (`interest_id`) REFERENCES `interests` (`interest_id`)
);
/* USER ABSTRACTS TEST DATA */
LOCK TABLES `userInterests` WRITE;
INSERT INTO `userInterests`
VALUES (1, 'habermasJim', 1),
	   (2, 'habermasJim', 2),
	   (3, 'habermasJim', 3),
	   (4, 'brandon', 4),
	   (5, 'adam', 5),
	   (6, 'adam', 3),
	   (7, 'adam', 6),
	   (8, 'adam', 7),
	   (9, 'adam', 8),
	   (10, 'adam', 9),
	   (11, 'adam', 10),
	   (12, 'adam', 11),
	   (13, 'adam', 12),
	   (14, 'adam', 1),
	   (15, 'adam', 2),
	   (16, 'adam', 17),
	   (17, 'aisha', 1),
	   (18, 'aisha', 2),
	   (19, 'aisha', 3),
	   (20, 'shpend', 13),
	   (21, 'shpend', 14),
	   (22, 'leon', 15),
	   (23, 'leon', 16),
	   (24, 'hardisonAlec', 17),
	   (25, 'hardisonAlec', 18),
	   (26, 'hardisonAlec', 19),
	   (27, 'hardisonAlec', 20),
	   (28, 'hardisonAlec', 21),
	   (29, 'hardisonAlec', 22);
UNLOCK TABLES;