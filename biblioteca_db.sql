CREATE DATABASE biblioteca_db;
USE biblioteca_db;

CREATE TABLE autor (
    id_autor INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    nacionalidad VARCHAR(50)
);

CREATE TABLE categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre_categoria VARCHAR(100) NOT NULL
);

CREATE TABLE libro (
    id_libro INT AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(150) NOT NULL,
    año_publicacion INT,
    id_autor INT,
    id_categoria INT,
    FOREIGN KEY (id_autor) REFERENCES autor(id_autor),
    FOREIGN KEY (id_categoria) REFERENCES categoria(id_categoria)
);

INSERT INTO autor (id_autor, nombre, nacionalidad) VALUES
    (1, 'Gabriel García Márquez', 'Colombiana'),
    (2, 'Isabel Allende',         'Chilena'),
    (3, 'Miguel de Cervantes',    'Española'),
    (4, 'Julio Cortázar',         'Argentina'),
    (5, 'Salarrué',               'Salvadoreña'),
    (6, 'Roque Dalton',           'Salvadoreña');

INSERT INTO categoria (id_categoria, nombre_categoria) VALUES
    (1, 'Novela'),
    (2, 'Cuento'),
    (3, 'Poesía'),
    (4, 'Ensayo'),
    (5, 'Ciencia ficción'),
    (6, 'Historia');
 
INSERT INTO libro (id_libro, titulo, `año_publicacion`, id_autor, id_categoria) VALUES
    (1, 'Cien años de soledad',     1967, 1, 1),
    (2, 'La casa de los espíritus', 1982, 2, 1),
    (3, 'Don Quijote de la Mancha', 1605, 3, 1),
    (4, 'Rayuela',                  1963, 4, 1),
    (5, 'Cuentos de barro',         1933, 5, 2),
    (6, 'Taberna y otros lugares',  1969, 6, 3);

 
