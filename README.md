# Super Chess Bros.

Trabajo práctico universitario de Ingeniería de Software: un juego de ajedrez clásico ambientado en el universo visual de Mario, extendido con cajas misteriosas y poderes especiales.

El proyecto está implementado en Java 17 con JavaFX y utiliza una arquitectura hexagonal para separar las reglas del dominio de la interfaz gráfica y la persistencia.

> Proyecto académico y no comercial. Los nombres, personajes y referencias visuales pertenecen a sus respectivos propietarios.

## 1. Descripción del juego

Super Chess Bros. conserva las reglas fundamentales del ajedrez:

- movimiento de todas las piezas;
- capturas;
- jaque y jaque mate;
- enroque;
- captura al paso;
- promoción de peones;
- tablas por material insuficiente;
- repetición triple;
- regla de las cincuenta jugadas;
- deshacer y rehacer movimientos.

La diferencia principal es la incorporación de **cajas misteriosas**. Al comenzar una partida aparecen cuatro cajas en posiciones simétricas del tablero. Cuando una pieza cae sobre una caja, recibe aleatoriamente un poder especial. Cada pieza puede conservar un solo poder y utilizarlo consume su turno.

La versión configurada para el trabajo utiliza:

- cuatro cajas iniciales;
- posiciones simétricas para evitar ventajas iniciales;
- aparición de nuevas cajas cada cinco turnos;
- generación especular de nuevas cajas;
- un máximo de cuatro cajas simultáneas;
- poderes visibles para ambos jugadores;
- persistencia de la información necesaria para reproducir las cajas al cargar o deshacer una partida.

## 2. Poderes especiales

### Flor de hielo

Congela una pieza rival durante un turno. El rey no puede ser congelado.

### Pluma

Permite que una pieza vuele a cualquier casilla libre, siempre que el movimiento no deje a su propio rey en jaque.

### Estrella

Vuelve invencible a una pieza durante un turno. Cuando la utiliza el rey, se transforma en una explosión que elimina las piezas enemigas que lo rodean.

### Caparazón

Se desplaza en línea recta y elimina a la primera pieza enemiga que encuentra. No puede eliminar al rey.

El jaque mate se determina teniendo en cuenta tanto los movimientos normales como los poderes disponibles. Por lo tanto, una posición no es mate si todavía existe una jugada o un poder legal capaz de salvar al rey.

## 3. Interfaz y experiencia de usuario

La aplicación cuenta con una interfaz JavaFX temática:

- tablero visual con piezas personalizadas;
- estética inspirada en Mario;
- bloques de preguntas para representar las cajas misteriosas;
- indicadores visuales para los poderes;
- efecto de hielo para las piezas congeladas;
- efecto especial para las piezas protegidas por una estrella;
- panel lateral con turno, capturas, poderes y actividad de la partida;
- historial de movimientos;
- listado de partidas guardadas;
- libro de reglas y explicación de las piezas;
- controles para nueva partida, guardar, deshacer, rehacer, usar poderes y controlar el sonido;
- diseño responsive para distintas dimensiones de ventana;
- scroll interno en las listas, evitando desplazar toda la pantalla.

La interfaz no contiene reglas de ajedrez. Utiliza los puertos del dominio para consultar el estado y solicitar acciones.

## 4. Arquitectura hexagonal

El núcleo del sistema contiene las reglas puras del juego y no depende de JavaFX, SQLite, sonidos ni componentes visuales.

### Dominio

El dominio se encuentra principalmente en:

```text
src/main/java/com/chess/domain
```

Incluye:

- modelo del tablero, piezas, colores, posiciones y tipos;
- estrategias de movimiento;
- comandos de movimientos;
- reglas de ajedrez;
- estados de turno y estado final;
- historial y snapshots;
- poderes y cajas misteriosas;
- reglas de tablas;
- puertos de entrada y salida.

### Adaptadores

Los adaptadores externos implementan o utilizan los puertos del dominio:

```text
src/main/java/com/chess/ui
src/main/java/com/chess/persistence
```

- `ui`: adaptador de entrada basado en JavaFX.
- `persistence`: adaptador de salida basado en SQLite.
- `Main`: composición de la aplicación y punto de entrada.

La dirección de dependencias apunta hacia el dominio. El núcleo no sabe que existe JavaFX ni SQLite, lo que permite probarlo completamente en memoria.

## 5. Patrones de diseño utilizados

### Strategy

Las piezas no se modelan mediante una jerarquía profunda de clases. Una pieza combina estrategias de movimiento:

- lineal;
- diagonal;
- en forma de L;
- de un paso;
- movimiento de peón.

Por ejemplo, la dama combina movimiento lineal y diagonal. Los poderes siguen la misma idea: cada poder encapsula su propio comportamiento.

### Command

Cada acción reversible se representa como un comando:

- movimiento normal;
- captura;
- enroque;
- captura al paso;
- promoción;
- uso de poderes.

Los comandos implementan `execute` y `undo`, guardando la información necesaria para restaurar el estado anterior.

### Memento

Los snapshots conservan información que no está representada únicamente por las piezas del tablero:

- derechos de enroque;
- contador de jugadas;
- historial relevante para tablas;
- piezas congeladas;
- poderes;
- cajas;
- semilla de generación.

Esto permite que deshacer, rehacer y cargar una partida produzcan el mismo estado, incluso cuando intervinieron elementos aleatorios.

### State

El ciclo de la partida se representa mediante estados:

- turno de blancas;
- turno de negras;
- partida terminada.

Cada estado determina qué acciones son válidas. Una partida terminada no acepta movimientos ni poderes.

### Observer

La partida notifica eventos a sus observadores. El tablero, el panel lateral y el sistema de sonido reaccionan a movimientos, poderes, deshacer, rehacer y finalización sin acoplarse directamente a la implementación interna del juego.

### Factory

`StandardGameFactory` compone el juego completo a partir de un `GameSetup`. La configuración determina:

- piezas y estrategias;
- reglas adicionales;
- piezas de promoción;
- cajas;
- poderes;
- aparición de nuevas cajas;
- reglas de tablas.

Así, el mismo motor puede crear ajedrez clásico o la edición con cajas misteriosas.

## 6. Principios de diseño

### SOLID

- **Single Responsibility:** cada clase tiene una responsabilidad concreta: generar movimientos, validar reglas, ejecutar comandos, persistir partidas o presentar información.
- **Open/Closed:** se pueden agregar poderes, reglas o estrategias sin modificar el núcleo existente.
- **Liskov Substitution:** las implementaciones de puertos y estrategias pueden sustituirse respetando sus contratos.
- **Interface Segregation:** el dominio expone interfaces específicas para acciones, consultas, observación, archivo y fábrica.
- **Dependency Inversion:** las capas de alto nivel dependen de interfaces, no de SQLite, JavaFX o implementaciones concretas.

### Inyección de dependencias

Las dependencias principales se reciben por constructor. La composición concreta se realiza en `Main` y en las fábricas de configuración.

Esto permite reemplazar:

- repositorios por dobles de prueba;
- generadores aleatorios por generadores deterministas;
- reproductores de sonido por implementaciones de prueba;
- selectores de promoción por implementaciones alternativas.

### GRASP

Se aplica especialmente:

- **Polymorphism:** cada pieza y cada poder encapsula su comportamiento mediante estrategias.
- **Controller:** `Game` coordina las acciones del juego sin delegar reglas en la interfaz.
- **Protected Variations:** los puertos protegen al dominio de cambios en la UI y la persistencia.

## 7. Persistencia y reproducibilidad

Las partidas se guardan mediante SQLite usando el adaptador:

```text
com.chess.persistence.SqliteGameRepository
```

La base de datos local se crea en el archivo `chess.db`.

Además de la posición del tablero, se conserva la información necesaria para reconstruir una partida con cajas y poderes. La semilla de generación permite que las cajas futuras aparezcan de la misma manera al cargar una partida, deshacer una acción o rehacerla.

## 8. Sistema de sonido

El audio está separado de las reglas del juego mediante interfaces y observadores.

Los eventos incluyen:

- movimiento;
- captura;
- jaque;
- finalización;
- poder;
- apertura de caja;
- promoción;
- deshacer y rehacer;
- navegación;
- selección;
- guardado;
- apertura de reglas;
- errores;
- menú.

La prioridad de eventos de juego es:

```text
FINISH > CHECK > POWER > BOX > PROMOTION > CAPTURE > MOVE
```

Los sonidos se encuentran en:

```text
src/main/resources/com/chess/ui/assets/sounds
```

El sistema puede activarse o desactivarse desde la interfaz.

## 9. Estructura del proyecto

```text
src/
├── main/
│   ├── java/com/chess/
│   │   ├── Main.java
│   │   ├── configuration/
│   │   ├── domain/
│   │   │   ├── command/
│   │   │   ├── draw/
│   │   │   ├── game/
│   │   │   ├── model/
│   │   │   ├── movement/
│   │   │   ├── port/
│   │   │   ├── power/
│   │   │   ├── rule/
│   │   │   └── state/
│   │   ├── persistence/
│   │   └── ui/
│   └── resources/com/chess/ui/
│       ├── assets/
│       └── chess-theme.css
└── test/
    └── java/com/chess/
```

## 10. Requisitos

- Java 17 o superior;
- Maven 3.8 o superior;
- JavaFX 21.0.5, descargado automáticamente por Maven;
- sistema operativo con soporte para JavaFX.

No es necesario instalar SQLite manualmente: el driver JDBC está declarado como dependencia del proyecto.

## 11. Ejecutar la aplicación

Desde la raíz del proyecto:

```bash
mvn clean javafx:run
```

También se puede ejecutar sin limpiar los artefactos anteriores:

```bash
mvn javafx:run
```

La clase principal es:

```text
com.chess.Main
```

## 12. Ejecutar las pruebas

Para ejecutar toda la suite:

```bash
mvn test
```

Las pruebas cubren, entre otros aspectos:

- movimientos de cada pieza;
- capturas;
- jaque y jaque mate;
- enroque;
- captura al paso;
- promoción;
- tablas;
- deshacer y rehacer;
- historial y snapshots;
- cajas misteriosas;
- poderes;
- persistencia;
- observadores;
- reproducción de partidas;
- contratos de interfaces;
- arquitectura;
- recursos de sonido.

La prueba de arquitectura verifica que las dependencias respeten la separación entre dominio, adaptadores y configuración.

## 13. Compilar sin ejecutar

```bash
mvn clean package
```

Para compilar sin ejecutar pruebas:

```bash
mvn -DskipTests package
```


## 14. Objetivo académico

El objetivo del trabajo no es solamente implementar un ajedrez funcional. También busca demostrar que un sistema puede evolucionar mediante diseño modular:

- las reglas se mantienen aisladas;
- las extensiones se agregan por composición;
- la interfaz puede cambiar sin reescribir el dominio;
- la persistencia puede reemplazarse;
- las acciones complejas pueden deshacerse;
- las decisiones arquitectónicas pueden verificarse con pruebas.

De esta manera, Super Chess Bros. funciona como una aplicación jugable y, al mismo tiempo, como una demostración práctica de arquitectura hexagonal, patrones de diseño, principios SOLID y testabilidad.
