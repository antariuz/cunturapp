CREATE DATABASE  IF NOT EXISTS `centur_app` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `centur_app`;
-- MySQL dump 10.13  Distrib 8.0.30, for Win64 (x86_64)
--
-- Host: localhost    Database: centur_app
-- ------------------------------------------------------
-- Server version	8.0.30

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `attribute_app`
--

DROP TABLE IF EXISTS `attribute_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `attribute_app` (
  `attribute_id` int NOT NULL AUTO_INCREMENT,
  `supplier_id` int NOT NULL,
  `supplier_title` varchar(255) NOT NULL,
  `opencart_title` varchar(255) NOT NULL,
  `replacement_from` varchar(255) DEFAULT NULL,
  `replacement_to` varchar(255) DEFAULT NULL,
  `math_sign` varchar(255) NOT NULL,
  `math_number` int NOT NULL,
  PRIMARY KEY (`attribute_id`),
  KEY `supplier_id` (`supplier_id`),
  CONSTRAINT `attribute_app_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier_app` (`supplier_app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `category_app`
--

DROP TABLE IF EXISTS `category_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category_app` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `supplier_id` int NOT NULL,
  `supplier_title` varchar(255) NOT NULL,
  `opencart_title` varchar(255) NOT NULL,
  `markup` int NOT NULL,
  PRIMARY KEY (`category_id`),
  KEY `supplier_id` (`supplier_id`),
  CONSTRAINT `category_app_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier_app` (`supplier_app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=333 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manufacturer_app`
--

DROP TABLE IF EXISTS `manufacturer_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manufacturer_app` (
  `manufacturer_id` int NOT NULL AUTO_INCREMENT,
  `supplier_id` int NOT NULL,
  `supplier_title` varchar(255) NOT NULL,
  `opencart_title` varchar(255) NOT NULL,
  `markup` int NOT NULL,
  PRIMARY KEY (`manufacturer_id`),
  KEY `supplier_id` (`supplier_id`),
  CONSTRAINT `manufacturer_app_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier_app` (`supplier_app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `option_app`
--

DROP TABLE IF EXISTS `option_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `option_app` (
  `option_id` int NOT NULL AUTO_INCREMENT,
  `product_profile_id` int NOT NULL,
  `value_id` int NOT NULL,
  `option_value` varchar(255) NOT NULL,
  `option_price` decimal(15,4) NOT NULL,
  PRIMARY KEY (`option_id`),
  KEY `option_product_profile` (`product_profile_id`),
  CONSTRAINT `option_product_profile` FOREIGN KEY (`product_profile_id`) REFERENCES `product_profile_app` (`product_profile_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `order_process_app`
--

DROP TABLE IF EXISTS `order_process_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_process_app` (
  `order_process_id` int NOT NULL AUTO_INCREMENT,
  `supplier_id` int NOT NULL,
  `start_process` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `end_process` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`order_process_id`),
  KEY `supplier_id` (`supplier_id`),
  CONSTRAINT `order_process_app_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier_app` (`supplier_app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_app`
--

DROP TABLE IF EXISTS `product_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_app` (
  `product_app_id` int NOT NULL AUTO_INCREMENT,
  `order_process_id` int NOT NULL,
  `url` varchar(255) NOT NULL DEFAULT '',
  `name` varchar(255) NOT NULL DEFAULT '',
  `status` varchar(255) NOT NULL DEFAULT '',
  `old_price` decimal(15,4) DEFAULT NULL,
  `new_price` decimal(15,4) DEFAULT NULL,
  PRIMARY KEY (`product_app_id`),
  KEY `order_process_id` (`order_process_id`),
  CONSTRAINT `product_app_ibfk_1` FOREIGN KEY (`order_process_id`) REFERENCES `order_process_app` (`order_process_id`)
) ENGINE=InnoDB AUTO_INCREMENT=501 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_attribute_app`
--

DROP TABLE IF EXISTS `product_attribute_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_attribute_app` (
  `product_profile_id` int NOT NULL,
  `attribute_id` int NOT NULL,
  `attribute_value` text NOT NULL,
  PRIMARY KEY (`product_profile_id`,`attribute_id`),
  KEY `attribute_id` (`attribute_id`),
  CONSTRAINT `product_attribute_app_ibfk_1` FOREIGN KEY (`product_profile_id`) REFERENCES `product_profile_app` (`product_profile_id`),
  CONSTRAINT `product_attribute_app_ibfk_2` FOREIGN KEY (`attribute_id`) REFERENCES `attribute_app` (`attribute_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `product_profile_app`
--

DROP TABLE IF EXISTS `product_profile_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_profile_app` (
  `product_profile_id` int NOT NULL AUTO_INCREMENT,
  `url` varbinary(8000) NOT NULL,
  `sku` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `supplier_id` int NOT NULL,
  `manufacturer_id` int NOT NULL,
  `category_id` int NOT NULL,
  `price` decimal(15,4) DEFAULT NULL,
  PRIMARY KEY (`product_profile_id`),
  KEY `supplier_id` (`supplier_id`),
  KEY `manufacturer_id` (`manufacturer_id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `product_profile_app_ibfk_1` FOREIGN KEY (`supplier_id`) REFERENCES `supplier_app` (`supplier_app_id`),
  CONSTRAINT `product_profile_app_ibfk_2` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturer_app` (`manufacturer_id`),
  CONSTRAINT `product_profile_app_ibfk_3` FOREIGN KEY (`category_id`) REFERENCES `category_app` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1541 DEFAULT CHARSET=utf8mb3;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `role_app`
--

DROP TABLE IF EXISTS `role_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `role_app` (
  `role_id` int NOT NULL AUTO_INCREMENT,
  `role_name` varchar(255) NOT NULL,
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `supplier_app`
--

DROP TABLE IF EXISTS `supplier_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `supplier_app` (
  `supplier_app_id` int NOT NULL AUTO_INCREMENT,
  `url` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `display_name` varchar(255) DEFAULT NULL,
  `markup` int NOT NULL,
  PRIMARY KEY (`supplier_app_id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_app`
--

DROP TABLE IF EXISTS `user_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_app` (
  `user_id` int NOT NULL AUTO_INCREMENT,
  `user_name` varchar(255) NOT NULL,
  `user_login` varchar(255) NOT NULL,
  `user_password` varchar(255) NOT NULL,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_role_app`
--

DROP TABLE IF EXISTS `user_role_app`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_role_app` (
  `user_id` int NOT NULL,
  `role_id` int NOT NULL,
  PRIMARY KEY (`user_id`,`role_id`),
  KEY `role_id` (`role_id`),
  CONSTRAINT `user_role_app_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_app` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `user_role_app_ibfk_2` FOREIGN KEY (`role_id`) REFERENCES `role_app` (`role_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2022-12-27 21:16:58
