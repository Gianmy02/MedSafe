-- Script SQL per inizializzare utenti di test in MedSafe
-- Usare SOLO in ambiente di sviluppo/testing
-- Questo script viene eseguito automaticamente da Spring Boot all'avvio

-- Nota: La tabella 'users' viene creata automaticamente da Hibernate
-- Questi INSERT vengono eseguiti dopo la creazione delle tabelle

-- Inserisci utenti di test (solo se non esistono già)
INSERT INTO users (email, azure_oid, full_name, genere, specializzazione, role, enabled)
SELECT * FROM (SELECT 'admin@medsafe.local', NULL, 'Admin Test', 'NON_SPECIFICATO', 'NESSUNA', 'ADMIN', TRUE) AS tmp
WHERE NOT EXISTS (
    SELECT email FROM users WHERE email = 'admin@medsafe.local'
) LIMIT 1;

INSERT INTO users (email, azure_oid, full_name, genere, specializzazione, role, enabled)
SELECT * FROM (SELECT 'medico1@medsafe.local', NULL, 'Mario Rossi', 'MASCHIO', 'CARDIOLOGIA', 'MEDICO', TRUE) AS tmp
WHERE NOT EXISTS (
    SELECT email FROM users WHERE email = 'medico1@medsafe.local'
) LIMIT 1;

INSERT INTO users (email, azure_oid, full_name, genere, specializzazione, role, enabled)
SELECT * FROM (SELECT 'medico2@medsafe.local', NULL, 'Anna Verdi', 'FEMMINA', 'PEDIATRIA', 'MEDICO', TRUE) AS tmp
WHERE NOT EXISTS (
    SELECT email FROM users WHERE email = 'medico2@medsafe.local'
) LIMIT 1;
