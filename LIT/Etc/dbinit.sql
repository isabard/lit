-- Script to initialize the database and create tables

CREATE DATABASE litdb;

USE litdb;

START TRANSACTION;

-- Table for bills
CREATE TABLE Bills (
	Id int,
	Title varchar(128),
	Category varchar(128),
	Committee varchar(128),
	StartDate date,
	LastActiveDate date,
	Status varchar(64),
	-- Word cloud entries
	WC1 varchar(64), WC2 varchar(64), WC3 varchar(64), WC4 varchar(64),
	WC5 varchar(64), WC6 varchar(64), WC7 varchar(64), WC8 varchar(64),
	WC9 varchar(64), WC10 varchar(64),
	PRIMARY KEY (Id)
);

COMMIT;

-- Table for legislators
CREATE TABLE Legislators (
	Id int,
	Name varchar(128),
	PassedBills int,
	FailedBills int,
	-- Word cloud entries
	WC1 varchar(64), WC2 varchar(64), WC3 varchar(64), WC4 varchar(64),
	WC5 varchar(64), WC6 varchar(64), WC7 varchar(64), WC8 varchar(64),
	WC9 varchar(64), WC10 varchar(64),
	-- Major Areas of Concentration
	AC1 varchar(64), AC2 varchar(64), AC3 varchar(64),
	PRIMARY KEY (Id)
)

COMMIT;

-- Table for sponsors of bills
CREATE TABLE Sponsors (
	Bill int REFERENCES Bills(Id),
	Sponsor int REFERENCES Legislators(Id)
)

COMMIT;
