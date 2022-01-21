# EmberCore

A [Spigot](https://www.spigotmc.org/) plugin and library designed to help streamline the development process.

## Features
- No more manual setup of Bukkit Listeners, data classes, etc. - just annotate these classes with `@OnEnable` and
  let EmberCore handle the rest with a single method call.
    - Like Spring Boot, but far dirtier.
- Makes use of [Jackson](https://github.com/FasterXML/jackson) to eliminate both manual configuration reads/writes
  while enabling developers to use custom configuration paths and specify the file to read from on a per-class
  or per-field basis.
    - **Supported data formats:** YAML, JSON, XML (limited)
    - Includes support for custom serialization through Jackson's `JsonSerializer` and `JsonDeserializer` classes
      and subtypes. Just annotate your custom serializers with an autorun annotation (`@Preload` or `@OnEnable`) and
      you're good to go.
        - Comes preconfigured with serialization modifiers and mixins for those pesky Spigot and JDK classes that just
          don't serialize nicely. These will automatically apply to the Object Mappers for all plugins managed by EmberCore.
            - **Vector (Spigot)** serializes to and from its components.
            - **ChatColor (Spigot)** serializes to and from the name of the color and its RGBA values.
            - **Level (Java)** serializes to and from the name of the log level.
        - **Planned:** Reimplementation of EmberCore's all-in-one serializer classes from an earlier version.
- Reflection utilities through [ReflectionUtil](https://github.com/Blu-Fantasy-Online/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/reflection/ReflectionUtil.java).
    - Retrieval of generic types from any field containing a `Map`, `Collection`, or single-parameter generic type.
    - Easy retrieval of all classes in a .jar file.

## Usage
If you're a developer, head down to [Getting Started](#gettingstarted).

If you're a server owner or administrator and one of your plugins requires this one, head to
[Releases](https://github.com/Blu-Fantasy-Online/EmberCore/releases), download the most relevant one, and drop the .jar into
your plugins folder. All done - no further setup is necessary, but you may want to tweak the maximum allowed
number of custom projectiles.

---
<a name="gettingstarted"/>

# Getting Started
## Installation
Either [download a release](https://github.com/Blu-Fantasy-Online/EmberCore/releases) or install via Maven by adding
the following dependency to your POM:
```xml
<dependency>
    <groupId>com.github.dewynion</groupId>
    <artifactId>embercore</artifactId>
    <version>LATEST</version>
</dependency>
```

## Usage
### Autoloading
The following annotations are used to automatically load plugins:

`@Preload` - Applied to **classes**. Will be instantiated and subjected to config injection before all other 
autoloads.

`@OnEnable` - Applied to **classes** and **methods**. Will be instantiated or called after preloads, but before
config injection.

`@AfterEnable` - Applied to **methods**. Will be called after all other autoloads, including config injection.

To initiate loading, call
```java
PluginLoader.load(this);
```
in your plugin's `OnEnable` method.

The autoloading sequence is as follows:
- Load is initiated.
- All classes within the plugin .jar are scanned and cached.
- Classes annotated with `@Preload` are instantiated and all fields not annotated with `@ExcludeFromSerialization`
  have their values injected from configuration files. (See [Injection](#injection) for more.)


<a name="injection"/>

### Injection from Config
The `ConfigInjector` class is used to reflectively set the values of fields according to the content of an external
configuration file.

For classes set to autoload, `injectIntoObject` is called automatically, but it can also be manually called with
one of the following:
```java
// Calls the method below with the File argument set to null.
ConfigInjector.injectIntoObject(JavaPlugin, Object);
// Injects a value into all applicable fields in the provided object using configuration files for the given plugin.
ConfigInjector.injectIntoObject(JavaPlugin, Object, File);
// Injects data into the specified field in the provided object using the data stored in the given PluginConfiguration.
ConfigInjector.injectIntoField(Field, Object, PluginConfiguration);
```

#### PluginConfigurations
The `PluginConfiguration` class and its subtypes (`JsonPluginConfiguration`, `YamlPluginConfiguration`, 
`XmlPluginConfiguration`) store data read in by the corresponding `ObjectMapper`. Typically, these are
created and cached as-needed by `ConfigInjector`, but you can also create your own:

```java
PluginConfiguration.create(File, JavaPlugin);
```

This will return an instance of a `PluginConfiguration` subtype corresponding to the data format indicated by the file 
extension - `.json`, `.yml`, or `.xml`.

By default, if a value is not configured at all, it will be written to the configuration in memory and saved to
the file associated with the `PluginConfiguration` when `saveConfiguration` is called on the `PluginConfiguration`
object.

**Upcoming:** `PluginConfiguration` and `ConfigInjector` will also feature support for alternate data sources, 
such as `String` and `InputStream`.

**Upcoming:** Actually add support for end users to inject into an object without creating new nodes in the 
config tree if the path isn't found. (Whoops.)

### Custom serialization
Since Embercore uses Jackson, custom serialization can be implemented by standard Jackson methods and modules can be 
added to the `ObjectMapper` for a plugin by calling
```java
ConfigInjector.registerModule(JavaPlugin, SimpleModule);
```

Future reads by that plugin will make use of the provided `SimpleModule`.

To avoid boilerplate code, EmberCore supports autoloading all classes that extend `JsonSerializer` and 
`JsonDeserializer`, as well as those that extend `TypedKeyDeserializer` (a generic extension of Jackson's 
`KeyDeserializer`). Since instantiation and module registry occur prior to config injection (except for classes 
marked with `@Preload`), your serializers will be used during the config injection phase.

EmberCore contains custom serializers and deserializers for several existing types that do not play nicely with
Jackson's serialization logic: `Vector` and `ChatColor` from Spigot and `Level` from the JDK itself.

### Annotations

Serialization may also be customized through annotations. While there is support for Jackson annotations,
Embercore also features several annotations that allow for the quick and easy configuration of some basic
properties.

#### @SerializationInfo
```java
@SerializationInfo(filename = "example.yml")
public class Example {
    private basicInteger = 1;
    @SerializationInfo(path = "countries.romania.geese")
    private boolean areThereGeeseInRomania = false;
    @SerializationInfo(path = "rodents", filename = "animals.yml")
    private String hamster = "hamster";
}
```
By default, fields are read from a node matching the `kebab-case` version of their name in `config.yml`.

`SerializationInfo` allows you to set a default file to use when serializing/deserializing a class. When applied 
to a field, if a filename is provided, it will be used instead of the class default. **All filenames are 
relative to the plugin's data folder.**

The `path` attribute allows you to specify the path from which the value of the field will be
read (or stored if it does not exist). Periods are used to indicate node hierarchy, as with Spigot's configuration
system.

The above class would read from and write to the following if subjected to `ConfigInjector`'s whims:
```yaml
# example.yml
basic-integer: 1
countries:
  romania:
    geese: false
```
```yaml
# animals.yml (theoretical rest of file omitted for brevity)
rodents:
  hamster: "hamster"
```

#### ~~@ExcludeFromSerialization~~

**Deprecation warning: In future versions, Jackson's `@JsonIgnore` will serve the same purpose, because having
two different versions of what is functionally the same annotation is just stupid. This is a holdover from a
previous version that did not utilize Jackson.**

`ExcludeFromSerialization` will prevent a field from being read at all through `ConfigInjector.`

It will ***not*** prevent a field from being read if applied to a class serialized or deserialized through 
Jackson. Assuming Jackson is configured to serialize properties not explicitly marked with `@JsonProperty`,

```java
@OnEnable
public class AnotherExample {
    
    // This will not be touched.
    @ExcludeFromSerialization
    private ExampleData hidingFromJackson = new ExampleData();
    
    // This will be read in through the plugin's ObjectMapper.
    private ExampleData exampleData = new ExampleData();

    // This will be deserialized by Jackson's ObjectMapper.
    public class ExampleData {
        
        // This will be serialized.
        private int data = 1;
        
        // This will still be serialized.
        @ExcludeFromSerialization
        private int hiddenData = 2;
        
        // This will not be serialized.
        @JsonIgnore
        private int actuallyHiddenData = 3;
    }
}
```

### Formatted Logging Shortcuts
Oh no, it's a clunky logging statement!
```java
plugin.getLogger().log(Level.INFO, String.format("message %s %s %s", object1, object2, object3));
```

Fortunately, the [EmberCore](https://github.com/Blu-Fantasy-Online/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/EmberCore.java)
class contains shorthand `info`, `warn`, `severe` and generic `log` methods that allow you to simply pass in your plugin,
message, and optionally arguments for `String#format`.

```java
EmberCore.warn(yourPluginInstance, "There are %s eggs.", numEggs);
```

## Game Mechanics
Aside from raw development aids that only you will ever see in action, Embercore contains some additional utilities
designed to provide more complex functionality within Spigot.

### Custom Projectiles
[VectorProjectile](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/physics/VectorProjectile.java)
allows you to create custom projectiles that operate entirely outside of Minecraft's rigid projectile physics and
collision detection.

The behavior of a VectorProjectile is completely customizable through code. Extend the VectorProjectile class and
override whichever methods you see fit. The source code contains information on all relevant methods.

Using this class, it is entirely possible to use VectorProjectile to create a homing egg (without using the egg
projectile) that passes through solid objects and causes sheep within 5 meters to explode, but that can be 
deflected by a player holding a stick named Terry.

All VectorProjectiles are tracked in [ProjectileRegistry](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/physics/ProjectileRegistry.java).

### Geometry
**This functionality is incomplete and subject to change.**

EmberCore contains functionality relevant to three-dimensional geometry and transformations.

To maintain consistency with Minecraft's conventions, an object's local X axis passes through its center from left
to right, its local Z axis passes through its center from back to front, and its local Y axis passes through its center
from bottom to top.

[Vectors](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/Vectors.java)
contains constants referring to standard vectors (up, right, forward, etc.).

[VectorUtil](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/VectorUtil.java)
contains helper methods for vector-based calculations, such as determing whether an entity can "see" another within its
vision range and radius.

[GeometryUtil](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/GeometryUtil.java),
at present, contains a variety of methods pertaining to the rotation of a [Location](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Location.html)
about another point.

[ShapeUtil](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/ShapeUtil.java)
contains methods to generate arrays of Locations in various shapes.

[PhysicsUtil](https://github.com/Blu-Fantasy-Online/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/physics/PhysicsUtil.java)
contains methods pertaining to kinematic equations, except I keep forgetting to actually add more of them.

### Command Tree Baseline
[CommandNode](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/command/CommandNode.java)
provides a bare-bones framework for creating command trees.

**Upcoming:** A command handler that, like the rest of the library, uses annotated classes to automate certain
functionality - in this case, setting up a robust command tree.

### Other Utility
[MovingParticle](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/util/MovingParticle.java)
provides utility for generating clusters of particles with an applied velocity or color. See [this link](https://www.spigotmc.org/threads/comprehensive-particle-spawning-guide-1-13-1-17.343001/)
for more information on which particle types support this additional data.