-- Not executed automatically. Run manually after the app has created the schema
-- (spring.jpa.hibernate.ddl-auto=update) if you want some starter departments
-- for testing the "assign complaint" flow.

INSERT INTO departments (name, code, description) VALUES
('Electrical Maintenance', 'ELEC', 'Handles wiring, lighting, and power related issues across campus'),
('Plumbing & Water Supply', 'PLUMB', 'Handles water leakage, drainage, and washroom related issues'),
('IT Services', 'IT', 'Handles Wi-Fi, LAN, and computer lab related issues'),
('Hostel Administration', 'HOSTEL', 'Handles hostel room, furniture, and general hostel upkeep issues'),
('Mess Committee', 'MESS', 'Handles mess food quality and hygiene related complaints'),
('Housekeeping', 'CLEAN', 'Handles cleanliness and sanitation across campus buildings')
ON CONFLICT (code) DO NOTHING;
