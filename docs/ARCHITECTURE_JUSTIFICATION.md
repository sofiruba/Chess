# Justificación de la arquitectura

## Alcance

La arquitectura separa el núcleo de reglas de ajedrez de los detalles externos. El paquete `com.chess.domain` contiene el modelo y las decisiones del juego; `com.chess.ui`, `com.chess.persistence` y `com.chess.configuration` son capas externas.

La regla principal es que las dependencias apuntan hacia el dominio. El núcleo no importa JavaFX, JDBC ni clases de configuración. Esta separación está verificada por `ArchitectureTest`.

## 1. Modelo del dominio

### Qué

`Board`, `Piece`, `Position`, `Color`, `PieceType` y `PowerType` representan el estado esencial del juego. `Piece` recibe estrategias (`IMoveStrategy`) en vez de conocer una jerarquía de piezas concreta.

### Por qué

El tablero y las piezas deben poder probarse sin interfaz gráfica ni base de datos. La composición de estrategias permite expresar que una dama combina movimientos lineales y diagonales, y evita duplicar la validación geométrica.

### Cuándo romperlo

No agregues referencias a JavaFX, SQLite, archivos o servicios externos en `model`. Si una regla necesita contexto de partida, debe recibirlo explícitamente desde una regla, comando o servicio; no debe consultar estado global.

## 2. Reglas y estrategias

### Qué

`movement` contiene estrategias geométricas (`LinearMoveStrategy`, `DiagonalMoveStrategy`, `LShapeMoveStrategy`, `OneStepMoveStrategy` y `PawnMoveStrategy`). `rule` coordina la generación y validación de acciones, incluyendo jaque, enroque, captura al paso y promoción.

### Por qué

Las reglas geométricas son variaciones intercambiables, mientras que las reglas de legalidad necesitan coordinar tablero, turno y estado. Mantener ambas responsabilidades separadas facilita agregar una regla sin modificar todas las piezas.

### Cuándo romperlo

Una estrategia no debe decidir el resultado de la partida ni acceder a puertos externos. Una nueva regla puede entrar en `rule` cuando valida una acción del dominio; si solo transforma una acción reversible, corresponde a `command`.

## 3. Comandos e historial

### Qué

`ICommand` representa acciones reversibles. `MoveCommand`, `CastlingCommand`, `EnPassantCommand`, `PromotionCommand` y los comandos de poderes implementan ejecución y deshacer. `GameTimeline` conserva comandos ejecutados, comandos deshechos y el `GameSnapshot` actual.

### Por qué

Deshacer y rehacer requieren restaurar más que las posiciones de las piezas: derechos de enroque, contadores de tablas, poderes, cajas y estado de turno también forman parte de la partida.

### Cuándo romperlo

Toda acción que cambie el estado observable de una partida debe ser un comando o quedar incluida en uno. No actualices el tablero directamente desde la UI ni agregues un campo al estado sin incorporarlo al snapshot y a la lógica de undo/redo.

## 4. Estado de la partida

### Qué

`Game` implementa `IGameService` y coordina tablero, reglas, timeline, evaluación de finalización, parser de códigos y poderes. `TurnState`, `FinishedState` y sus fábricas representan el turno activo y la finalización.

### Por qué

`Game` actúa como controlador del caso de uso: recibe una acción, valida sus precondiciones, crea o ejecuta el comando, actualiza el estado y notifica a los observadores. Los estados hacen explícito que una partida terminada no acepta nuevas jugadas.

### Cuándo romperlo

No muevas reglas a controladores JavaFX ni hagas que `Game` persista directamente. Si `Game` empieza a conocer un detalle externo, extrae un puerto y conecta la implementación en configuración.

## 5. Puertos y adaptadores

### Qué

`domain.port` define interfaces segregadas para acciones (`IGameActions`), consultas (`IGameQueries`), servicio (`IGameService`), observación (`IGameObservable`/`IGameListener`), fábrica y repositorio. `ui` usa los puertos para presentar y solicitar acciones; `SqliteGameRepository` implementa la persistencia.

### Por qué

Los puertos protegen el dominio frente a cambios de UI y almacenamiento. También permiten dobles de prueba, repositorios alternativos y una composición distinta sin modificar las reglas.

### Cuándo romperlo

No hagas que la UI dependa de clases internas de `rule`, `power` o `game` si puede usar un puerto. Amplía un puerto existente cuando la capacidad pertenece al mismo contrato; crea otro cuando mezclar acciones, consultas, observación o persistencia obligaría a los consumidores a depender de métodos irrelevantes.

## 6. Configuración y extensiones

### Qué

`GameSetup` y `StandardGameFactory` ensamblan estrategias, reglas, poderes, cajas y evaluadores. `Main` conecta la composición con los adaptadores concretos.

### Por qué

La construcción queda fuera del dominio y las extensiones se agregan por composición. Así se puede configurar ajedrez clásico o la variante con cajas misteriosas sin introducir condicionales de infraestructura en `Game`.

### Cuándo romperlo

No instancies adaptadores concretos dentro del dominio. Si una nueva variante requiere una decisión de negocio, modelala como regla, estrategia o servicio; si solo cambia la implementación técnica, mantenela en configuración o en un adaptador.

## Invariantes que deben conservarse

1. El dominio no depende de `ui`, `persistence`, `configuration`, JavaFX ni JDBC.
2. Las jugadas ilegales nunca modifican el estado.
3. Toda mutación reversible puede deshacerse y rehacerse conservando el estado completo.
4. Una partida finalizada no acepta movimientos ni poderes.
5. La UI y la persistencia acceden al motor mediante puertos y el modelo público.
6. La generación aleatoria de cajas conserva la información necesaria para reproducir una partida cargada o rehecha.

Si una modificación necesita romper alguno de estos invariantes, debe actualizar primero esta justificación, las pruebas de arquitectura y los contratos afectados.
