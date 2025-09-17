-- Script di inizializzazione per il database MySQL
-- Questo script viene eseguito automaticamente quando il container MySQL viene avviato per la prima volta

-- Creazione del database (se non esiste già)
CREATE DATABASE IF NOT EXISTS finanzanewsdb;

-- Utilizzo del database
USE finanzanewsdb;

-- Qui puoi aggiungere eventuali tabelle o dati iniziali
-- Ad esempio:
-- CREATE TABLE IF NOT EXISTS users (
--     id BIGINT AUTO_INCREMENT PRIMARY KEY,
--     username VARCHAR(255) NOT NULL UNIQUE,
--     email VARCHAR(255) NOT NULL UNIQUE,
--     password VARCHAR(255) NOT NULL,
--     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
-- );
