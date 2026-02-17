# vItems - Documentación Técnica

vItems es un sistema de herramientas custom para servidores de Minecraft orientado a rendimiento, animaciones progresivas, sistema de cooldown individual por herramienta y actualización dinámica de items en reload sin lag.

## Requisitos y Dependencias

Para un funcionamiento correcto:

- **Java**: 21 o superior.
- **Servidor**: Paper/Purpur 1.21+.
- **WorldGuard** (Opcional): protección de regiones durante uso de herramientas.

## Compilación

Este proyecto usa Maven.

### Instrucciones de Compilación

1. Clona el repositorio:
   ```bash
   git clone https://github.com/ValerinSMP/vItems.git
   ```

2. Entra al directorio del proyecto y compila:
   
   **Windows:**
   ```bash
   mvn clean package
   ```
   
   **Linux/macOS:**
   ```bash
   mvn clean package
   ```

3. El jar generado queda en:
   ```
   target/vItems-1.0.0.jar
   ```

## Características

### Sistema de Herramientas Custom

Las herramientas se identifican mediante **NBT persistente** (`vitems:item_type`), lo que permite:
- Actualización automática de nombres/lore al hacer reload.
- Compatibilidad con encantamientos vanilla (Fortune, Silk Touch, Efficiency, Unbreaking).
- Persistencia entre reinicios del servidor.

#### Herramientas Implementadas

| Herramienta | NBT ID | Comportamiento | Cooldown | Durabilidad |
|-------------|--------|----------------|----------|-------------|
| **Pico 3x3** | `vitems:3x3pickaxe` | Rompe 9 bloques en área 3x3 instantáneamente | No | 9 (1 por bloque) |
| **Pala 3x3** | `vitems:3x3shovel` | Excava 9 bloques en área 3x3 instantáneamente | No | 9 (1 por bloque) |
| **Veinminer** | `vitems:veinminer` | Mina venas de mineral conectadas progresivamente (max 25) | Sí (5s) | 25 (1 por bloque) |
| **Tree Capitator** | `vitems:treecapitator` | Tala árboles completos progresivamente (max 25) | Sí (5s) | 25 (1 por bloque) |

### Animaciones Progresivas

- **Veinminer** y **Tree Capitator** rompen bloques gradualmente con delay configurable.
- Sonidos específicos por tipo de bloque (stone, wood, ore).
- Efectos visuales de partículas por rotura.
- Sistema BFS (Breadth-First Search) para detección de bloques conectados en 26 direcciones (3x3x3).

### Sistema de Cooldown

- Cooldown **individual por herramienta** (no global).
- Tiempo de recarga configurable en `config.yml`.
- Mientras una herramienta está en cooldown, **no puede ser usada**.
- Limpieza automática de cooldowns expirados cada 30 segundos.
- Mensajes informativos mostrando tiempo restante.

### Detección Direccional (3x3)

Las herramientas 3x3 detectan la dirección del jugador:
- **Vertical**: Cuando mira arriba/abajo (pitch > 60°), rompe horizontal 3x3.
- **Horizontal**: Según orientación (norte/sur/este/oeste), rompe vertical 3x3.

### Integración con Encantamientos

- **Fortune**: Compatible con Veinminer - se aplica a **cada bloque** minado individualmente.
- **Silk Touch**: Compatible con todas las herramientas.
- **Efficiency**: Acelera la rotura del bloque inicial (comportamiento vanilla).
- **Unbreaking**: Reduce consumo de durabilidad (comportamiento vanilla).

### Actualización Dinámica de Items

- Al hacer `/vitems reload`, todos los items existentes en inventarios de jugadores se actualizan automáticamente.
- Actualización **progresiva en batches** (configurable, por defecto 50 items/tick).
- Evita lag spikes en servidores con muchos jugadores.
- Solo actualiza items que tengan el NBT de vItems.

### Integración con WorldGuard

- Verifica permisos de **block-break** antes de romper cada bloque.
- Soft-dependency: funciona perfectamente sin WorldGuard instalado.
- Respeta protecciones de regiones y flags.

### Compatibilidad PlugMan

- Soporte completo para `/plugman load`, `/plugman reload` y `/plugman unload`.
- Cancela todos los BukkitRunnables activos al deshabilitar.
- Limpia animaciones progresivas en curso.
- Limpia cooldowns y caches.
- Sin memory leaks.

## Comandos y Permisos

### Comandos de Usuario

Todos los comandos requieren **OP** (operador del servidor).

| Comando | Descripción | Permiso |
|---------|-------------|---------|
| `/vitems give <jugador> <item>` | Da una herramienta custom a un jugador | OP |
| `/vitems reload` | Recarga configuración y actualiza items existentes | OP |
| `/vitems list` | Lista todas las herramientas disponibles | OP |

**Aliases**: `/vi`, `/vitem`

### Items Disponibles

- `pickaxe_3x3` - Pico 3x3
- `shovel_3x3` - Pala 3x3
- `veinminer` - Pico Veinminer
- `tree_capitator` - Hacha Tree Capitator

### Ejemplos de Uso

```bash
# Dar un veinminer al jugador mtynnn
/vitems give mtynnn veinminer

# Dar pico 3x3 a todos los jugadores online
/vitems give @a pickaxe_3x3

# Recargar configuración
/vitems reload

# Ver lista de items
/vitems list
```

## Configuración

El plugin utiliza configuración segmentada:

- **`config.yml`**: configuración principal (herramientas, cooldowns, límites, optimización).
- **`messages.yml`**: textos, prefijo, mensajes de error y éxito (MiniMessage).

### config.yml

```yaml
settings:
  # Actualizar items existentes al hacer reload
  update-items-on-reload: true
  # Cantidad de items a actualizar por tick (evita lag)
  update-batch-size: 50
  # Debug mode
  debug: false

tools:
  pickaxe_3x3:
    enabled: true
    max-blocks: 9
    has-cooldown: false
    
  shovel_3x3:
    enabled: true
    max-blocks: 9
    has-cooldown: false
    
  veinminer:
    enabled: true
    max-blocks: 25                # Bloques máximos por uso
    has-cooldown: true
    cooldown-seconds: 5.0         # Tiempo de recarga en segundos
    only-same-ore: true           # Solo minar el mismo tipo de mena
    animation-delay-ticks: 2      # Delay entre bloques (ticks)
    
  tree_capitator:
    enabled: true
    max-blocks: 25
    has-cooldown: true
    cooldown-seconds: 5.0
    animation-delay-ticks: 2      # 2 ticks = 0.1s entre bloques
```

### messages.yml

Utiliza **MiniMessage** para formateo avanzado con tema morado (`#C77DFF`).

```yaml
general:
  prefix: '<dark_gray>[<color:#C77DFF>ᴠɪᴛᴇᴍs</color><dark_gray>] '
  config-reloaded: '%prefix%<color:#C77DFF>¡Configuración recargada!'

tool:
  cooldown: '%prefix%<gray>Debes esperar <color:#C77DFF>%time%s <gray>antes de usar esta herramienta.'
  disabled: '%prefix%<red>Esta herramienta está deshabilitada.'
  
items:
  veinminer:
    name: '<color:#C77DFF>⛏ Pico Veinminer'
    lore:
      - '<gray>Mina <color:#C77DFF>venas completas <gray>de mineral'
      - '<gray>Máximo: <color:#C77DFF>25 bloques'
      - '<dark_gray>• Cooldown: <color:#C77DFF>5s'
      - '<dark_gray>• Compatible con <color:#C77DFF>Fortuna'
```

## Notas Operativas

### Edición de Configuración

- Si editas `config.yml` o `messages.yml` en `plugins/vItems/`, los cambios **no se pierden** al reiniciar.
- Para aplicar cambios en caliente: `/vitems reload`.
- Los items existentes en inventarios se actualizan automáticamente tras reload.

### Recomendaciones de Producción

1. **Performance**: Mantén `animation-delay-ticks: 2` para balance entre fluidez y rendimiento.
2. **Batch Size**: Con más de 100 jugadores online, aumenta `update-batch-size` a 100.
3. **Cooldowns**: Ajusta según el balance de tu servidor (5s es un buen punto de partida).
4. **Max Blocks**: Limita `max-blocks` si quieres evitar lag en árboles/venas muy grandes.

### WorldGuard

- Si usas WorldGuard, las herramientas respetan automáticamente las protecciones.
- No requiere configuración adicional.
- Si WorldGuard no está instalado, las herramientas funcionan normalmente (sin restricciones).

### PlugMan

Compatible con carga/descarga en caliente:

```bash
# Cargar plugin
/plugman load vItems

# Recargar plugin (equivalente a /vitems reload)
/plugman reload vItems

# Descargar plugin
/plugman unload vItems
```

### Debug Mode

Activa debug en `config.yml` para ver logs detallados:

```yaml
settings:
  debug: true
```

Esto mostrará en consola:
- Items actualizados al hacer reload
- Bloques rotos por herramientas
- Cooldowns aplicados
- Errores de WorldGuard (si ocurren)

## Migraciones / Versiones Anteriores

- Primera versión del plugin (1.0.0).
- No hay migraciones necesarias.
- Los items se crean con NBT desde el inicio, asegurando compatibilidad futura.

## Soporte y Contribuciones

- **Autor**: mtynnn
- **Organización**: ValerinSMP
- **GitHub**: https://github.com/ValerinSMP/vItems

Para reportar bugs o sugerir features, abre un issue en GitHub.

---

**¡Disfruta de las herramientas custom en tu servidor!** 🚀
