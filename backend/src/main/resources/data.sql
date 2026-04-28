-- ================================================
-- Datos de prueba - Marketplace Orquídeas Combeima
-- Ejecutar una sola vez desde IntelliJ
-- ================================================

-- Orquídeas (primero productos, luego orquideas con el mismo id)
INSERT INTO productos (nombre, descripcion, precio, stock, stock_reservado, image_url, activo) VALUES
                                                                                                   ('Orquídea Cattleya Roja',     'Orquídea de flores grandes y vibrantes, ideal para interiores iluminados', 45000, 10, 0, 'https://placehold.co/400x400?text=Cattleya+Roja',     true),
                                                                                                   ('Orquídea Phalaenopsis Blanca','Orquídea elegante de larga duración, perfecta para principiantes',        38000, 15, 0, 'https://placehold.co/400x400?text=Phalaenopsis+Blanca', true),
                                                                                                   ('Orquídea Dendrobium Morada', 'Orquídea tropical con racimos de flores pequeñas y delicadas',            52000,  8, 0, 'https://placehold.co/400x400?text=Dendrobium+Morada',  true);

INSERT INTO orquideas (id, variedad, color_flor, tamanio, nivel_cuidado, tiempo_floracion) VALUES
                                                                                               (1, 'Cattleya',     'Rojo',   'Grande',  'Intermedio', '3 meses'),
                                                                                               (2, 'Phalaenopsis', 'Blanco', 'Mediano', 'Fácil',      '6 meses'),
                                                                                               (3, 'Dendrobium',   'Morado', 'Pequeño', 'Intermedio', '2 meses');

-- Macetas
INSERT INTO productos (nombre, descripcion, precio, stock, stock_reservado, image_url, activo) VALUES
                                                                                                   ('Maceta de Barro Rústica',  'Maceta artesanal de barro con excelente drenaje para orquídeas', 18000, 20, 0, 'https://placehold.co/400x400?text=Maceta+Barro',    true),
                                                                                                   ('Maceta Plástica Transparente','Permite ver las raíces para controlar el riego fácilmente',       12000, 30, 0, 'https://placehold.co/400x400?text=Maceta+Plastica',  true),
                                                                                                   ('Maceta Cerámica Moderna',  'Diseño elegante con acabado brillante, ideal para regalo',        25000, 12, 0, 'https://placehold.co/400x400?text=Maceta+Ceramica',  true);

INSERT INTO macetas (id, material, diametro_cm, color, estilo) VALUES
                                                                   (4, 'Barro',     15.0, 'Terracota', 'Rústico'),
                                                                   (5, 'Plástico',  12.0, 'Transparente', 'Moderno'),
                                                                   (6, 'Cerámica',  14.0, 'Blanco',    'Minimalista');

-- Guías de cuidado (una por orquídea)
INSERT INTO guia_cuidado (orquidea_id, titulo, variedad, contenido, frecuencia_riego, luz_requerida, temperatura_ideal, fertilizacion, image_url) VALUES
                                                                                                                                                      (1, 'Cómo cuidar tu Cattleya',     'Cattleya',     'La Cattleya necesita buena circulación de aire y luz brillante indirecta. Deja secar el sustrato entre riegos.',    'Cada 5 días',  'Luz brillante indirecta', '18°C - 27°C', 'Cada 15 días en época de crecimiento', 'https://placehold.co/800x400?text=Guia+Cattleya'),
                                                                                                                                                      (2, 'Cómo cuidar tu Phalaenopsis', 'Phalaenopsis', 'La Phalaenopsis es ideal para principiantes. Tolera poca luz y solo necesita riego cuando el sustrato está seco.',  'Cada 7 días',  'Luz indirecta baja',      '18°C - 25°C', 'Cada 20 días',                         'https://placehold.co/800x400?text=Guia+Phalaenopsis'),
                                                                                                                                                      (3, 'Cómo cuidar tu Dendrobium',   'Dendrobium',   'El Dendrobium necesita una temporada seca para estimular la floración. Riega abundante en verano, poco en invierno.','Cada 4 días',  'Luz brillante directa',   '15°C - 28°C', 'Cada 10 días en época de crecimiento', 'https://placehold.co/800x400?text=Guia+Dendrobium');

-- Recomendaciones de macetas para cada orquídea
INSERT INTO recomendacion_maceta (orquidea_id, maceta_id, descripcion) VALUES
                                                                           (1, 4, 'El barro poroso ayuda a que las raíces de la Cattleya respiren mejor y evita el exceso de humedad'),
                                                                           (1, 6, 'La maceta cerámica blanca resalta el color rojo intenso de la Cattleya, ideal si es para regalo'),
                                                                           (2, 5, 'La maceta transparente te permite ver cuándo las raíces de la Phalaenopsis necesitan agua'),
                                                                           (2, 6, 'La cerámica minimalista combina perfecto con el estilo elegante de la Phalaenopsis blanca'),
                                                                           (3, 4, 'El barro ayuda a regular la humedad del Dendrobium, clave durante su temporada seca'),
                                                                           (3, 5, 'La maceta plástica liviana facilita mover el Dendrobium según la luz que necesita cada temporada');