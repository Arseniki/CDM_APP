CREATE TABLE PAYS (
    NomPays VARCHAR(50) PRIMARY KEY,
    Groupe CHAR(1) NOT NULL
);

CREATE TABLE STADE (
    IDStade SERIAL PRIMARY KEY,
    Pays VARCHAR(50) NOT NULL,
    Ville VARCHAR(50) NOT NULL,
    NomStade VARCHAR(100) NOT NULL
);

CREATE TABLE MATCH (
    IDMatch SERIAL PRIMARY KEY,
    NomPays1 VARCHAR(50) REFERENCES PAYS(NomPays),
    NomPays2 VARCHAR(50) REFERENCES PAYS(NomPays),
    IDStade INT REFERENCES STADE(IDStade),
    DateMatch DATE NOT NULL,
    Pays1Buts INT DEFAULT NULL,
    Pays2Buts INT DEFAULT NULL
);

-- Données de test initiales
INSERT INTO PAYS (NomPays, Groupe) VALUES 
('Qatar', 'A'), ('Équateur', 'A'), ('Sénégal', 'A'), ('Pays-Bas', 'A'),
('Angleterre', 'B'), ('Iran', 'B'), ('États-Unis', 'B'), ('Pays de Galles', 'B')
ON CONFLICT DO NOTHING;

INSERT INTO STADE (Pays, Ville, NomStade) VALUES ('Qatar', 'Al Khor', 'Al Bayt Stadium');
