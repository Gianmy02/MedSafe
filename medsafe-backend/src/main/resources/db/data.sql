-- Dati di test per MedSafe (solo ambiente di sviluppo)
-- Questo file viene eseguito automaticamente da Spring Boot dopo la creazione delle tabelle

-- Inserisci utenti di test
INSERT INTO users (email, azure_oid, full_name, genere, specializzazione, role, enabled, created_at)
SELECT 'admin@medsafe.local', NULL, 'Admin Test', 'NON_SPECIFICATO', 'NESSUNA', 'ADMIN', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'admin@medsafe.local');

INSERT INTO users (email, azure_oid, full_name, genere, specializzazione, role, enabled, created_at)
SELECT 'medico1@medsafe.local', NULL, 'Mario Rossi', 'MASCHIO', 'CARDIOLOGIA', 'MEDICO', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'medico1@medsafe.local');

INSERT INTO users (email, azure_oid, full_name, genere, specializzazione, role, enabled, created_at)
SELECT 'medico2@medsafe.local', NULL, 'Anna Verdi', 'FEMMINA', 'PEDIATRIA', 'MEDICO', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'medico2@medsafe.local');
