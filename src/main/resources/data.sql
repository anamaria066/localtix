-- Users (demo data) - password for both: Parola123!
INSERT INTO users (last_name, first_name, email, phone, birth_date, password)
VALUES ('Popescu', 'Ana', 'ana@localtix.ro', '0712345678', '2003-05-10', 'Parola123!');
INSERT INTO users (last_name, first_name, email, phone, birth_date, password)
VALUES ('Ionescu', 'Maria', 'maria@localtix.ro', '0723456789', '2002-11-21', 'Parola123!');
-- User 3: a minor (born 2010), used to test 18+ events (US-8)
INSERT INTO users (last_name, first_name, email, phone, birth_date, password)
VALUES ('Georgescu', 'Andrei', 'andrei@localtix.ro', '0734567890', '2010-03-15', 'Parola123!');

-- Events (id 1-5)
INSERT INTO events (title, category, city, location, date_time, base_price, description)
VALUES ('Metrophonic Live', 'CONCERT', 'București', 'Sala Palatului', '2026-12-15 20:00:00', 150.00,
        'An electrifying rock concert featuring Metrophonic and their latest album. Expect pyrotechnics and an unforgettable night.');
INSERT INTO events (title, category, city, location, date_time, base_price, description)
VALUES ('A Night of Laughter', 'STANDUP', 'Cluj-Napoca', 'Casa de Cultură a Studenților', '2026-12-20 19:00:00', 80.00,
        'Three of Romania''s best stand-up comedians on one stage. An evening full of jokes and good vibes.');
INSERT INTO events (title, category, city, location, date_time, base_price, description)
VALUES ('Hamlet', 'THEATRE', 'București', 'Teatrul Național', '2026-11-25 18:00:00', 100.00,
        'Shakespeare''s timeless tragedy brought to life by an award-winning cast. A must-see for theatre lovers.');
INSERT INTO events (title, category, city, location, date_time, base_price, description)
VALUES ('Riverside Tour', 'CONCERT', 'Iași', 'Sala Sporturilor', '2027-01-10 20:00:00', 120.00,
        'Polish progressive rock legends Riverside bring their world tour to Iași. An unmissable experience.');
-- Past event (should NOT appear in the upcoming events list)
INSERT INTO events (title, category, city, location, date_time, base_price, description)
VALUES ('Retro Night', 'CONCERT', 'București', 'Club A', '2026-10-01 21:00:00', 90.00,
        'A celebration of 80s and 90s hits. This event is in the past.');

-- Seats for event 1 (Metrophonic Live): rows A and B, 5 seats each; 2 SOLD
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'A', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'A', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'A', 3, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'A', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'A', 5, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'B', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'B', 2, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'B', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'B', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (1, 'B', 5, 'AVAILABLE');

-- Seats for event 2 (A Night of Laughter): 3 SOLD
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'A', 1, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'A', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'A', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'A', 4, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'A', 5, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'B', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'B', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'B', 3, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'B', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (2, 'B', 5, 'AVAILABLE');

-- Seats for event 3 (Hamlet): 1 LOCKED, 1 SOLD
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'A', 1, 'AVAILABLE');
-- A2 is in Maria's cart (user 2) for 10 minutes from application start (US-7), then it is released
INSERT INTO seats (event_id, row_label, seat_number, status, locked_until, locked_by_user_id)
VALUES (3, 'A', 2, 'LOCKED', DATEADD(MINUTE, 10, LOCALTIMESTAMP), 2);
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'A', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'A', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'A', 5, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'B', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'B', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'B', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'B', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (3, 'B', 5, 'AVAILABLE');

-- Seats for event 4 (Riverside Tour): all AVAILABLE
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'A', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'A', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'A', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'A', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'A', 5, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'B', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'B', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'B', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'B', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (4, 'B', 5, 'AVAILABLE');

-- Seats for event 5 (Retro Night - past): all SOLD
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (5, 'A', 1, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (5, 'A', 2, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (5, 'A', 3, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (5, 'A', 4, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (5, 'A', 5, 'SOLD');

-- Event 6 (US-5 filters demo): a stand-up in București under 100 RON
INSERT INTO events (title, category, city, location, date_time, base_price, description)
VALUES ('Open Mic Comedy', 'STANDUP', 'București', 'Club Control', '2026-12-05 20:00:00', 60.00,
        'Fresh voices of Romanian stand-up try out their newest material. Laughs guaranteed, drinks recommended.');

-- Seats for event 6 (Open Mic Comedy): 1 SOLD
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (6, 'A', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (6, 'A', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (6, 'A', 3, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (6, 'A', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (6, 'A', 5, 'AVAILABLE');

-- Event 7 (US-8): an 18+ event - only adults can buy tickets
INSERT INTO events (title, category, city, location, date_time, base_price, description, age_restriction)
VALUES ('Late Night Roast', 'STANDUP', 'București', 'Club Control', '2026-12-12 22:00:00', 70.00,
        'An uncensored late-night roast with explicit language and adult humour. Adults only (18+).', 18);

-- Seats for event 7 (Late Night Roast): all AVAILABLE
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'A', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'A', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'A', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'A', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'A', 5, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'B', 1, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'B', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'B', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'B', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (7, 'B', 5, 'AVAILABLE');


-- Event 8 (US-12 / US-10): starts 24 hours after the application starts -> cancelling gives 0.00 RON
INSERT INTO events (title, category, city, location, date_time, base_price, description)
VALUES ('Jazz Tonight', 'CONCERT', 'Cluj-Napoca', 'Form Space', DATEADD(HOUR, 24, LOCALTIMESTAMP), 50.00,
        'An intimate evening of live jazz with local musicians. Starts tomorrow!');

-- Seats for event 8 (Jazz Tonight): A1 SOLD (Maria's ticket)
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (8, 'A', 1, 'SOLD');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (8, 'A', 2, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (8, 'A', 3, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (8, 'A', 4, 'AVAILABLE');
INSERT INTO seats (event_id, row_label, seat_number, status) VALUES (8, 'A', 5, 'AVAILABLE');

-- Tickets (US-12) of Maria (user 2), shown on "My Tickets"
-- Metrophonic Live, seat A3, Student price -> cancelling (more than 48h before) refunds 90%
INSERT INTO tickets (code, seat_id, user_id, customer_name, paid_price, status, created_at)
VALUES ('LTX-2026-MET0A3', (SELECT id FROM seats WHERE event_id = 1 AND row_label = 'A' AND seat_number = 3),
        2, 'Maria Ionescu', 105.00, 'ACTIVE', '2026-11-01 12:00:00');
-- Jazz Tonight, seat A1 -> starts in less than 48h, cancelling refunds 0.00 RON
INSERT INTO tickets (code, seat_id, user_id, customer_name, paid_price, status, created_at)
VALUES ('LTX-2026-JAZ0A1', (SELECT id FROM seats WHERE event_id = 8 AND row_label = 'A' AND seat_number = 1),
        2, 'Maria Ionescu', 50.00, 'ACTIVE', '2026-11-01 12:00:00');
-- Retro Night (past event), seat A1 -> cannot be cancelled anymore (red error)
INSERT INTO tickets (code, seat_id, user_id, customer_name, paid_price, status, created_at)
VALUES ('LTX-2026-RET0A1', (SELECT id FROM seats WHERE event_id = 5 AND row_label = 'A' AND seat_number = 1),
        2, 'Maria Ionescu', 90.00, 'ACTIVE', '2026-09-15 12:00:00');
-- A Night of Laughter, seat A2: already cancelled (US-12 AC4), 90% of 80.00 refunded; the seat is available again
INSERT INTO tickets (code, seat_id, user_id, customer_name, paid_price, refunded_amount, status, created_at)
VALUES ('LTX-2026-LAU0A2', (SELECT id FROM seats WHERE event_id = 2 AND row_label = 'A' AND seat_number = 2),
        2, 'Maria Ionescu', 80.00, 72.00, 'CANCELLED', '2026-11-01 12:00:00');
