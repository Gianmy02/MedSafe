-- =====================================================
-- Script per promuovere un utente a ADMIN
-- =====================================================
--
-- IMPORTANTE: Esegui questo script su Azure Database tramite Azure CLI o Azure Portal
--
-- PREREQUISITO: L'utente deve essersi loggato almeno una volta
-- (in modo che sia stato creato automaticamente nel database con ruolo MEDICO)

-- 1. Verifica che l'utente esista nel database
SELECT id, email, full_name, role, enabled, created_at
FROM users
WHERE email = 'TUA-EMAIL@DOMINIO.COM';

-- 2. Se l'utente esiste, promuovilo a ADMIN
UPDATE users
SET role = 'ADMIN'
WHERE email = 'TUA-EMAIL@DOMINIO.COM';

-- 3. Verifica che la modifica sia stata applicata
SELECT id, email, full_name, role, enabled, created_at
FROM users
WHERE email = 'TUA-EMAIL@DOMINIO.COM';

-- =====================================================
-- ESEMPIO: Promuovi l'utente admin@medsafe.local
-- =====================================================
-- UPDATE users SET role = 'ADMIN' WHERE email = 'admin@medsafe.local';

-- =====================================================
-- Per visualizzare tutti gli utenti e i loro ruoli:
-- =====================================================
-- SELECT id, email, full_name, role, enabled, created_at FROM users ORDER BY created_at DESC;

-- =====================================================
-- Per rimuovere i privilegi di admin a un utente:
-- =====================================================
-- UPDATE users SET role = 'MEDICO' WHERE email = 'TUA-EMAIL@DOMINIO.COM';

