-- ============================================================
-- Coupe du Monde 2022 - Script d'initialisation (MySQL)
-- Conventions : tables au pluriel, colonnes en snake_case,
--               contraintes prefixees (pk_, fk_, uq_, ck_)
-- ============================================================

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS matchs;
DROP TABLE IF EXISTS stades;
DROP TABLE IF EXISTS pays;
SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- Table : pays
-- ------------------------------------------------------------
CREATE TABLE pays (
    nom_pays   VARCHAR(50) NOT NULL,
    groupe     CHAR(1)     NOT NULL,
    PRIMARY KEY (nom_pays),
    CONSTRAINT ck_pays_groupe CHECK (groupe IN ('A', 'B'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Table : stades
-- ------------------------------------------------------------
CREATE TABLE stades (
    id_stade   INT          NOT NULL AUTO_INCREMENT,
    pays       VARCHAR(50)  NOT NULL,
    ville      VARCHAR(50)  NOT NULL,
    nom_stade  VARCHAR(100) NOT NULL,
    PRIMARY KEY (id_stade),
    CONSTRAINT uq_stades_lieu UNIQUE (pays, ville, nom_stade)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------
-- Table : matchs
-- ------------------------------------------------------------
CREATE TABLE matchs (
    id_match     INT         NOT NULL AUTO_INCREMENT,
    nom_pays_1   VARCHAR(50) NOT NULL,
    nom_pays_2   VARCHAR(50) NOT NULL,
    id_stade     INT         NOT NULL,
    date_match   DATE        NOT NULL,
    pays_1_buts  INT         DEFAULT NULL,
    pays_2_buts  INT         DEFAULT NULL,

    PRIMARY KEY (id_match),

    CONSTRAINT fk_matchs_pays_1 FOREIGN KEY (nom_pays_1) REFERENCES pays(nom_pays),
    CONSTRAINT fk_matchs_pays_2 FOREIGN KEY (nom_pays_2) REFERENCES pays(nom_pays),
    CONSTRAINT fk_matchs_stade  FOREIGN KEY (id_stade)   REFERENCES stades(id_stade),

    CONSTRAINT ck_matchs_equipes_differentes CHECK (nom_pays_1 <> nom_pays_2),
    CONSTRAINT ck_matchs_buts_positifs CHECK (
        (pays_1_buts IS NULL OR pays_1_buts >= 0) AND
        (pays_2_buts IS NULL OR pays_2_buts >= 0)
    ),
    CONSTRAINT ck_matchs_scores_coherents CHECK (
        (pays_1_buts IS NULL AND pays_2_buts IS NULL)
        OR
        (pays_1_buts IS NOT NULL AND pays_2_buts IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- Donnees de test
-- ============================================================

-- 8 equipes des groupes A et B (Coupe du Monde 2022)
INSERT IGNORE INTO pays (nom_pays, groupe) VALUES
    ('Qatar',         'A'),
    ('Équateur',      'A'),
    ('Sénégal',       'A'),
    ('Pays-Bas',      'A'),
    ('Angleterre',    'B'),
    ('Iran',          'B'),
    ('États-Unis',    'B'),
    ('Pays de Galles','B');

-- 3 stades (assez pour tester le JComboBox de Mohamine)
INSERT IGNORE INTO stades (pays, ville, nom_stade) VALUES
    ('Qatar', 'Al Khor',   'Al Bayt Stadium'),
    ('Qatar', 'Doha',      'Stadium 974'),
    ('Qatar', 'Al Rayyan', 'Education City Stadium');

-- 2 matchs joues (pour tester le classement de Martin)
-- + 1 match non joue (pour tester l'UPDATE de Mohamine)
INSERT INTO matchs (nom_pays_1, nom_pays_2, id_stade, date_match, pays_1_buts, pays_2_buts) VALUES
    ('Qatar',      'Équateur', 1, '2022-11-20', 0,    2),
    ('Sénégal',    'Pays-Bas', 2, '2022-11-21', 0,    2),
    ('Angleterre', 'Iran',     3, '2022-11-21', NULL, NULL);