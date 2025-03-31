-- Bestehende Tabellen löschen, falls vorhanden
DROP TABLE IF EXISTS usb_stick;
DROP TABLE IF EXISTS stick_group;

-- Tabelle für Gruppen erstellen
CREATE TABLE stick_group (
                             group_id VARCHAR(255) PRIMARY KEY,
                             stick_type VARCHAR(50),
                             number_of_sticks INT
);

-- Tabelle für USB-Sticks erstellen
CREATE TABLE usb_stick (
                           inventarnummer VARCHAR(255) PRIMARY KEY,
                           typ VARCHAR(50) NOT NULL,
                           speicherkapazitaet VARCHAR(50),
                           hersteller VARCHAR(100),
                           modell VARCHAR(100),
                           seriennummer VARCHAR(100),
                           verfuegbarkeit VARCHAR(50),
                           zustand VARCHAR(50),
                           group_id VARCHAR(255),
                           CONSTRAINT fk_usb_stick_group
                               FOREIGN KEY (group_id)
                                   REFERENCES stick_group (group_id)
                                   ON DELETE CASCADE
                                   ON UPDATE CASCADE
);


-- Fremdschlüsselprüfungen deaktivieren
SET FOREIGN_KEY_CHECKS = 0;

-- Testdaten einfügen
INSERT INTO stick_group (group_id, stick_type, number_of_sticks)
VALUES
    ('GRP1', 'Bootstick', 2),
    ('GRP2', 'Datenstick', 2);

INSERT INTO usb_stick (inventarnummer, typ, speicherkapazitaet, hersteller, modell, seriennummer, verfuegbarkeit, zustand, group_id)
VALUES
    ('USB123', 'Bootstick', '16GB', 'Kingston', 'DataTraveler', 'SN123456', 'verfügbar', 'neu', 'GRP1'),
    ('USB124', 'Datenstick', '32GB', 'SanDisk', 'Ultra Flair', 'SN789101', 'reserviert', 'gebraucht', 'GRP2'),
    ('USB125', 'Bootstick', '64GB', 'Corsair', 'Voyager', 'SN111213', 'ausgeliehen', 'gebraucht', 'GRP1'),
    ('USB126', 'Datenstick', '128GB', 'Samsung', 'Bar Plus', 'SN456789', 'in Wartung', 'defekt', 'GRP2');

-- Trigger löschen, falls vorhanden
DROP TRIGGER IF EXISTS trg_usbstick_after_insert;
DROP TRIGGER IF EXISTS trg_usbstick_after_delete;
DROP TRIGGER IF EXISTS trg_usbstick_after_update;

-- Trigger nach INSERT erstellen
CREATE TRIGGER trg_usbstick_after_insert
    AFTER INSERT ON usb_stick
    FOR EACH ROW
BEGIN
    UPDATE stick_group
    SET number_of_sticks = (SELECT COUNT(*) FROM usb_stick WHERE group_id = NEW.group_id)
    WHERE group_id = NEW.group_id;
END;

-- Trigger nach DELETE erstellen
CREATE TRIGGER trg_usbstick_after_delete
    AFTER DELETE ON usb_stick
    FOR EACH ROW
BEGIN
    UPDATE stick_group
    SET number_of_sticks = (SELECT COUNT(*) FROM usb_stick WHERE group_id = OLD.group_id)
    WHERE group_id = OLD.group_id;
END;

-- Trigger nach UPDATE erstellen
CREATE TRIGGER trg_usbstick_after_update
    AFTER UPDATE ON usb_stick
    FOR EACH ROW
BEGIN
    IF NEW.group_id <> OLD.group_id THEN
        UPDATE stick_group
        SET number_of_sticks = (SELECT COUNT(*) FROM usb_stick WHERE group_id = OLD.group_id)
        WHERE group_id = OLD.group_id;
        UPDATE stick_group
        SET number_of_sticks = (SELECT COUNT(*) FROM usb_stick WHERE group_id = NEW.group_id)
        WHERE group_id = NEW.group_id;
    ELSE
        UPDATE stick_group
        SET number_of_sticks = (SELECT COUNT(*) FROM usb_stick WHERE group_id = NEW.group_id)
        WHERE group_id = NEW.group_id;
    END IF;
END;

-- Fremdschlüsselprüfungen wieder aktivieren
SET FOREIGN_KEY_CHECKS = 1;
