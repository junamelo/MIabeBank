-- ===============================
-- Données de test pour Ega Bank
-- ===============================

-- Insertion des clients
INSERT INTO clients (id, nom, prenom, email, telephone, adresse, date_naissance, nationalite, sexe) VALUES
(1, 'Dupont', 'Jean', 'jean.dupont@email.com', '0612345678', '123 Rue de Paris, 75001 Paris', '1985-03-15', 'Française', 'MASCULIN'),
(2, 'Martin', 'Marie', 'marie.martin@email.com', '0623456789', '456 Avenue des Champs, 69001 Lyon', '1990-07-22', 'Française', 'FEMININ'),
(3, 'Bernard', 'Pierre', 'pierre.bernard@email.com', '0634567890', '789 Boulevard Central, 13001 Marseille', '1978-11-08', 'Française', 'MASCULIN'),
(4, 'Petit', 'Sophie', 'sophie.petit@email.com', '0645678901', '321 Rue du Commerce, 31000 Toulouse', '1995-01-30', 'Française', 'FEMININ');

-- Insertion des utilisateurs (mot de passe: "password123" encodé en BCrypt)
-- $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy = password123
INSERT INTO users (id, username, email, password, role, enabled, client_id, created_at) VALUES
(1, 'admin', 'admin@egabank.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'ADMIN', true, NULL, CURRENT_TIMESTAMP),
(2, 'jean.dupont', 'jean.dupont@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true, 1, CURRENT_TIMESTAMP),
(3, 'marie.martin', 'marie.martin@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true, 2, CURRENT_TIMESTAMP),
(4, 'pierre.bernard', 'pierre.bernard@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true, 3, CURRENT_TIMESTAMP),
(5, 'sophie.petit', 'sophie.petit@email.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'USER', true, 4, CURRENT_TIMESTAMP);

-- Insertion des comptes bancaires
INSERT INTO comptes (id, numero_compte, type_compte, solde, date_creation, client_id) VALUES
(1, 'FR7630001007941234567890185', 'COURANT', 2500.00, CURRENT_TIMESTAMP, 1),
(2, 'FR7630001007941234567890186', 'EPARGNE', 15000.00, CURRENT_TIMESTAMP, 1),
(3, 'FR7630001007941234567890187', 'COURANT', 3200.50, CURRENT_TIMESTAMP, 2),
(4, 'FR7630001007941234567890188', 'COURANT', 850.75, CURRENT_TIMESTAMP, 3),
(5, 'FR7630001007941234567890189', 'EPARGNE', 22000.00, CURRENT_TIMESTAMP, 3),
(6, 'FR7630001007941234567890190', 'COURANT', 1500.00, CURRENT_TIMESTAMP, 4);

-- Insertion des transactions
INSERT INTO transactions (id, type_transaction, montant, date_transaction, description, compte_source_id, compte_destination_id) VALUES
(1, 'DEPOT', 1000.00, DATEADD('DAY', -30, CURRENT_TIMESTAMP), 'Dépôt initial', NULL, 1),
(2, 'DEPOT', 5000.00, DATEADD('DAY', -25, CURRENT_TIMESTAMP), 'Virement employeur', NULL, 1),
(3, 'RETRAIT', 200.00, DATEADD('DAY', -20, CURRENT_TIMESTAMP), 'Retrait DAB', 1, NULL),
(4, 'VIREMENT', 500.00, DATEADD('DAY', -15, CURRENT_TIMESTAMP), 'Épargne mensuelle', 1, 2),
(5, 'DEPOT', 3000.00, DATEADD('DAY', -28, CURRENT_TIMESTAMP), 'Salaire', NULL, 3),
(6, 'RETRAIT', 150.00, DATEADD('DAY', -10, CURRENT_TIMESTAMP), 'Courses', 3, NULL),
(7, 'VIREMENT', 100.00, DATEADD('DAY', -5, CURRENT_TIMESTAMP), 'Remboursement resto', 3, 1),
(8, 'DEPOT', 10000.00, DATEADD('DAY', -60, CURRENT_TIMESTAMP), 'Épargne', NULL, 5),
(9, 'DEPOT', 500.00, DATEADD('DAY', -3, CURRENT_TIMESTAMP), 'Cadeau anniversaire', NULL, 6),
(10, 'RETRAIT', 50.00, DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'Retrait espèces', 6, NULL);
