CREATE DATABASE IF NOT EXISTS vetclinic_db_2;
USE vetclinic_db_2;

-- ---------------------------------------------------------------- staff
CREATE TABLE IF NOT EXISTS staff (
                                     staff_id        VARCHAR(10)  PRIMARY KEY,
    staff_name      VARCHAR(100) NOT NULL,
    contact_number  VARCHAR(11)  NOT NULL,
    role            VARCHAR(20)  NOT NULL,                -- 'Veterinarian' or 'Vet Assistant'
    license_number  VARCHAR(50)  NULL,                    -- Veterinarian only
    specialization  VARCHAR(100) NULL,                    -- Veterinarian only
    shift           VARCHAR(20)  NULL,                    -- Vet Assistant only
    CONSTRAINT chk_staff_role CHECK (role IN ('Veterinarian', 'Vet Assistant'))
    );

-- ---------------------------------------------------------------- animals
CREATE TABLE IF NOT EXISTS animals (
                                       animal_id         VARCHAR(10)  PRIMARY KEY,
    species           VARCHAR(20)  NOT NULL,              -- 'Dog', 'Cat' or 'Livestock'
    name              VARCHAR(100) NOT NULL,
    age               INT          NOT NULL,
    weight_kg         DOUBLE       NOT NULL DEFAULT 0,
    owner_name        VARCHAR(100) NOT NULL,
    owner_contact     VARCHAR(11)  NOT NULL,
    owner_address     VARCHAR(200) NOT NULL,
    registered_date   DATE         NOT NULL,
    breed             VARCHAR(100) NULL,                  -- Dog only
    rabies_vaccinated BOOLEAN      NULL,                  -- Dog only
    indoor            BOOLEAN      NULL,                  -- Cat only
    coat_color        VARCHAR(50)  NULL,                  -- Cat only
    tag_number        VARCHAR(50)  NULL,                  -- Livestock only
    livestock_type    VARCHAR(50)  NULL,                  -- Livestock only
    CONSTRAINT chk_animal_species CHECK (species IN ('Dog', 'Cat', 'Livestock'))
    );

-- ---------------------------------------------------------------- treatment_records
-- ON DELETE CASCADE: deleting an animal deletes its history.
-- ON DELETE RESTRICT: a staff member who performed treatments cannot be deleted
-- (the program also blocks this in ClinicManager.removeStaff).
CREATE TABLE IF NOT EXISTS treatment_records (
                                                 record_id              VARCHAR(20)  PRIMARY KEY,
    animal_id              VARCHAR(10)  NOT NULL,
    treatment_date         DATE         NOT NULL,
    diagnosis              VARCHAR(200) NOT NULL,
    treatment              VARCHAR(200) NOT NULL,
    medication             VARCHAR(200) NOT NULL,
    follow_up_date         DATE         NULL,
    performed_by_staff_id  VARCHAR(10)  NOT NULL,
    CONSTRAINT fk_record_animal FOREIGN KEY (animal_id) REFERENCES animals (animal_id) ON DELETE CASCADE,
    CONSTRAINT fk_record_staff  FOREIGN KEY (performed_by_staff_id) REFERENCES staff (staff_id) ON DELETE RESTRICT
    );

-- ---------------------------------------------------------------- seeded staff accounts
-- These match ClinicManager.seedDefaultStaffIfEmpty()
INSERT IGNORE INTO staff (staff_id, staff_name, contact_number, role, license_number, specialization, shift)
VALUES ('S001', 'Dr. Olivia Rodrigo', '09171234567', 'Veterinarian', 'VET-2026-001', 'Small Animals', NULL);

INSERT IGNORE INTO staff (staff_id, staff_name, contact_number, role, license_number, specialization, shift)
VALUES ('S002', 'Julia Montes', '09181234567', 'Vet Assistant', NULL, NULL, 'Morning');

-- ---------------------------------------------------------------- view tables
SELECT * FROM animals;
SELECT * FROM staff;
SELECT * FROM treatment_records;