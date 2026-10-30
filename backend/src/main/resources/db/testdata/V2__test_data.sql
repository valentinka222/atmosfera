-- =====================================================
-- Тестовые данные салона «Атмосфера»
-- Подключаются только в средах dev и test
-- =====================================================

-- Мастера (id получат значения 1..5 по порядку)
INSERT INTO master (full_name, specialization, phone, active) VALUES
    ('Анна Смирнова',  'парикмахер',      '+7 900 111-11-11', TRUE),   -- 1
    ('Елена Козлова',  'парикмахер',      '+7 900 222-22-22', TRUE),   -- 2
    ('Ольга Иванова',  'мастер маникюра', '+7 900 333-33-33', TRUE),   -- 3
    ('Мария Петрова',  'косметолог',      '+7 900 444-44-44', TRUE),   -- 4
    ('Ирина Соколова', 'мастер маникюра', '+7 900 555-55-55', FALSE);  -- 5, уволена

-- Услуги (id 1..10)
INSERT INTO service (name, category, duration_min, price) VALUES
    ('Женская стрижка',         'Волосы',        60,  1800.00),  -- 1
    ('Мужская стрижка',         'Волосы',        45,  1200.00),  -- 2
    ('Окрашивание',             'Волосы',        120, 4500.00),  -- 3
    ('Укладка',                 'Волосы',        45,  1500.00),  -- 4
    ('Классический маникюр',    'Ногти',         60,  1300.00),  -- 5
    ('Маникюр с гель-лаком',    'Ногти',         90,  2200.00),  -- 6
    ('Педикюр',                 'Ногти',         90,  2500.00),  -- 7
    ('Чистка лица',             'Уход за лицом', 60,  3000.00),  -- 8
    ('Массаж лица',             'Уход за лицом', 30,  1500.00),  -- 9
    ('Коррекция бровей',        'Брови',         30,   800.00);  -- 10

-- Кто какие услуги делает
INSERT INTO master_service (master_id, service_id) VALUES
    (1, 1), (1, 2), (1, 3), (1, 4),   -- Анна: все услуги для волос
    (2, 1), (2, 2), (2, 4),           -- Елена: без окрашивания
    (3, 5), (3, 6), (3, 7),           -- Ольга: ногти
    (4, 8), (4, 9), (4, 10),          -- Мария: лицо и брови
    (5, 5), (5, 6);                   -- Ирина: маникюр (но она уволена)

-- График работы
-- generate_series(1, 5) выдаёт числа 1,2,3,4,5 — то есть пн..пт
INSERT INTO work_schedule (master_id, day_of_week, start_time, end_time)
SELECT 1, d, '10:00', '19:00' FROM generate_series(1, 5) AS d;     -- Анна: пн-пт

INSERT INTO work_schedule (master_id, day_of_week, start_time, end_time)
SELECT 2, d, '11:00', '20:00' FROM generate_series(2, 6) AS d;     -- Елена: вт-сб

INSERT INTO work_schedule (master_id, day_of_week, start_time, end_time) VALUES
    (3, 1, '10:00', '18:00'),                                      -- Ольга: пн, ср, пт, сб
    (3, 3, '10:00', '18:00'),
    (3, 5, '10:00', '18:00'),
    (3, 6, '10:00', '18:00');

INSERT INTO work_schedule (master_id, day_of_week, start_time, end_time)
SELECT 4, d, '09:00', '17:00' FROM generate_series(1, 5) AS d;     -- Мария: пн-пт

-- Клиенты (id 1..3)
INSERT INTO client (full_name, phone, email) VALUES
    ('Виктория Белова',  '+7 901 000-00-01', 'belova@example.com'),
    ('Дарья Новикова',   '+7 901 000-00-02', 'novikova@example.com'),
    ('Павел Морозов',    '+7 901 000-00-03', 'morozov@example.com');

-- Записи на СЛЕДУЮЩИЙ понедельник.
-- Даты считаются от текущего момента, поэтому записи всегда в будущем.
-- date_trunc('week', ...) даёт понедельник текущей недели 00:00,
-- + 7 дней = следующий понедельник.
INSERT INTO appointment (client_id, master_id, service_id, start_at, end_at, status)
VALUES
    (1, 1, 1,
     date_trunc('week', now()::timestamp) + INTERVAL '7 days 12 hours',
     date_trunc('week', now()::timestamp) + INTERVAL '7 days 13 hours',
     'BOOKED'),   -- Виктория у Анны, женская стрижка, пн 12:00–13:00
    (2, 3, 6,
     date_trunc('week', now()::timestamp) + INTERVAL '7 days 14 hours',
     date_trunc('week', now()::timestamp) + INTERVAL '7 days 15 hours 30 minutes',
     'BOOKED');   -- Дарья у Ольги, маникюр с гель-лаком, пн 14:00–15:30