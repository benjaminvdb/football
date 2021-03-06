-- MySQL Script generated by MySQL Workbench
-- Tue Feb 23 00:34:04 2016
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema psv
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema psv
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `psv` DEFAULT CHARACTER SET utf8 ;
USE `psv` ;

-- -----------------------------------------------------
-- Table `psv`.`teams`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`teams` ;

CREATE TABLE IF NOT EXISTS `psv`.`teams` (
  `team_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`team_id`),
  UNIQUE INDEX `name_UNIQUE` (`name` ASC))
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`matches`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`matches` ;

CREATE TABLE IF NOT EXISTS `psv`.`matches` (
  `match_id` INT NOT NULL AUTO_INCREMENT,
  `pitch_size_x` FLOAT NOT NULL,
  `pitch_size_y` FLOAT NOT NULL,
  `home_team_id` INT NOT NULL,
  `away_team_id` INT NOT NULL,
  PRIMARY KEY (`match_id`),
  INDEX `matches_away_team_id_fk_idx` (`away_team_id` ASC),
  INDEX `matches_home_team_id_fk_idx` (`home_team_id` ASC),
  CONSTRAINT `matches_home_team_id_fk`
    FOREIGN KEY (`home_team_id`)
    REFERENCES `psv`.`teams` (`team_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `matches_away_team_id_fk`
    FOREIGN KEY (`away_team_id`)
    REFERENCES `psv`.`teams` (`team_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`frames`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`frames` ;

CREATE TABLE IF NOT EXISTS `psv`.`frames` (
  `frame_id` INT NOT NULL AUTO_INCREMENT,
  `frame_number` INT UNSIGNED NOT NULL,
  `minute` INT UNSIGNED NOT NULL,
  `section` TINYINT UNSIGNED NOT NULL,
  `match_id` INT NOT NULL,
  PRIMARY KEY (`frame_id`),
  INDEX `frames_match_id_fk_idx` (`match_id` ASC),
  CONSTRAINT `frames_match_id_fk`
    FOREIGN KEY (`match_id`)
    REFERENCES `psv`.`matches` (`match_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`players`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`players` ;

CREATE TABLE IF NOT EXISTS `psv`.`players` (
  `player_id` INT NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(45) NULL,
  PRIMARY KEY (`player_id`))
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`player_measurements`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`player_measurements` ;

CREATE TABLE IF NOT EXISTS `psv`.`player_measurements` (
  `player_measurement_id` INT NOT NULL AUTO_INCREMENT,
  `internal_id` TINYINT UNSIGNED NOT NULL COMMENT '0-10: team home\n11-21: team away',
  `x` FLOAT NOT NULL,
  `y` FLOAT NOT NULL,
  `speed` FLOAT NOT NULL,
  `player_id` INT NOT NULL,
  `frame_id` INT NOT NULL,
  PRIMARY KEY (`player_measurement_id`),
  INDEX `player_measurements_player_id_fk_idx` (`player_id` ASC),
  INDEX `player_measurements_frame_id_fk_idx` (`frame_id` ASC),
  CONSTRAINT `player_measurements_player_id_fk`
    FOREIGN KEY (`player_id`)
    REFERENCES `psv`.`players` (`player_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `player_measurements_frame_id_fk`
    FOREIGN KEY (`frame_id`)
    REFERENCES `psv`.`frames` (`frame_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`referees`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`referees` ;

CREATE TABLE IF NOT EXISTS `psv`.`referees` (
  `referee_id` INT NOT NULL AUTO_INCREMENT,
  `type` ENUM('main', 'line') NOT NULL,
  PRIMARY KEY (`referee_id`))
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`ball_measurements`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`ball_measurements` ;

CREATE TABLE IF NOT EXISTS `psv`.`ball_measurements` (
  `ball_measurement_id` INT NOT NULL AUTO_INCREMENT,
  `x` FLOAT NOT NULL,
  `y` FLOAT NOT NULL,
  `z` FLOAT NOT NULL,
  `flag` TINYINT(1) NOT NULL,
  `possession` INT NOT NULL,
  `frame_id` INT NOT NULL,
  PRIMARY KEY (`ball_measurement_id`),
  INDEX `ball_measurements_frame_id_fk_idx` (`frame_id` ASC),
  CONSTRAINT `ball_measurements_frame_id_fk`
    FOREIGN KEY (`frame_id`)
    REFERENCES `psv`.`frames` (`frame_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`referee_measurement`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`referee_measurement` ;

CREATE TABLE IF NOT EXISTS `psv`.`referee_measurement` (
  `id` INT NOT NULL AUTO_INCREMENT,
  `internal_id` TINYINT UNSIGNED NOT NULL,
  `x` FLOAT NOT NULL,
  `y` FLOAT NOT NULL,
  `speed` FLOAT NOT NULL,
  `referee_id` INT NOT NULL,
  `frame_id` INT NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `referee_measurement_referee_id_fk_idx` (`referee_id` ASC),
  INDEX `referee_measurement_frame_id_fk_idx` (`frame_id` ASC),
  CONSTRAINT `referee_measurement_referee_id_fk`
    FOREIGN KEY (`referee_id`)
    REFERENCES `psv`.`referees` (`referee_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `referee_measurement_frame_id_fk`
    FOREIGN KEY (`frame_id`)
    REFERENCES `psv`.`frames` (`frame_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`matches_referees_maps`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`matches_referees_maps` ;

CREATE TABLE IF NOT EXISTS `psv`.`matches_referees_maps` (
  `match_id` INT NOT NULL AUTO_INCREMENT,
  `referee_id` INT NOT NULL,
  PRIMARY KEY (`match_id`, `referee_id`),
  INDEX `matches_referees_maps_referee_id_fk_idx` (`referee_id` ASC),
  CONSTRAINT `matches_referees_maps_match_id_fk`
    FOREIGN KEY (`match_id`)
    REFERENCES `psv`.`matches` (`match_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `matches_referees_maps_referee_id_fk`
    FOREIGN KEY (`referee_id`)
    REFERENCES `psv`.`referees` (`referee_id`)
    ON DELETE CASCADE
    ON UPDATE CASCADE)
ENGINE = MyISAM;


-- -----------------------------------------------------
-- Table `psv`.`teams_players_maps`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `psv`.`teams_players_maps` ;

CREATE TABLE IF NOT EXISTS `psv`.`teams_players_maps` (
  `team_id` INT NOT NULL,
  `player_id` INT NOT NULL,
  `jersey_number` TINYINT UNSIGNED NOT NULL,
  PRIMARY KEY (`team_id`, `player_id`),
  INDEX `teams_players_maps_player_id_fk_idx` (`player_id` ASC),
  CONSTRAINT `teams_players_maps_team_id_fk`
    FOREIGN KEY (`team_id`)
    REFERENCES `psv`.`teams` (`team_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `teams_players_maps_player_id_fk`
    FOREIGN KEY (`player_id`)
    REFERENCES `psv`.`players` (`player_id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = MyISAM;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
