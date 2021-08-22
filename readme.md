# EmberCore
## What is this?
**EmberCore** is a [Spigot](https://www.spigotmc.org/) plugin and library designed to help streamline the development
process. I originally wrote it for my own use, but ultimately decided that releasing it publicly might help out other 
people.

## Okay, so what do I do with it?
If you're a developer, head down to [Getting Started](#gettingstarted).

If you're a server owner or administrator and one of your plugins requires this one, head to 
[Releases](https://github.com/Dewynion/EmberCore/releases), download the most relevant one, and drop the .jar into 
your plugins folder. All done - no further setup is necessary, but you may want to tweak the maximum allowed
number of custom projectiles.


<a name="gettingstarted"/>

## Getting Started
### Installation
Either [download a release](https://github.com/Dewynion/EmberCore/releases) or install via Maven by adding 
the following dependency to your POM:
```xml
<dependency>
  <groupId>com.github.dewynion</groupId>
  <artifactId>embercore</artifactId>
  <version>0.9.2.2</version>
</dependency>
```

### Usage
At its most basic level, EmberCore supports automated singleton setup. This is useful for plugins that use a multitude 
of listeners and managers. To make use of this functionality,

1. Annotate classes that you want to create and maintain a single instance of with [Singleton](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/reflection/Singleton.java).
2. Annotate methods that you want to run after all Singletons have been created with [PostSetup](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/reflection/PostSetup.java).
3. Optionally, declare a priority for these annotations. From low to high, this value affects the order in which classes will be created and post-setup methods will be called.
4. In your plugin's `OnEnable` method, call [EmberCore#setup](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/EmberCore.java#L67).

That's it - all Singletons are created and cached so that they persist as long as your plugin is loaded.

Additionally, any Singletons that implement [Listener](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/event/Listener.html) 
will be automatically registered with Spigot's plugin manager - no need to do it yourself.

## Quick Reference
EmberCore features several other things that I started on and probably haven't finished yet.

### Injection from Config
Fields in Singleton-marked classes that are annotated with [YamlSerialized](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/config/YamlSerialized.java)
will have their values automatically injected during setup. By default, the value is pulled from your plugin's `config.yml`
using a snake-case version of the field's camelCase name, but both the file and path to the configured value may be changed on a per-field basis.

You may also call this functionality on any instance of a class using [ConfigInjector](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/config/ConfigInjector.java).

#### Custom serialization
You may define the way in which a class serializes to and deserializes from YAML in that class by defining a 
`toConfigurationSection` method that returns an instance of [ConfigurationSection](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/configuration/ConfigurationSection.html)
and/or a static `fromConfigurationSection` method that accepts a ConfigurationSection as a parameter and returns an instance of your class.

Example (adapted from EmberRPG):
```java
public ConfigurationSection toConfigurationSection() {
        // Create a ConfigurationSection.
        ConfigurationSection cs = new MemoryConfiguration();
        // Set the value at path "base-value" to 10. 
        cs.set("base-value", 10);
        // Returns a configuration section with one path, base-value, that
        // has a value of 10.
        return cs;
    }

    // Since these methods are called reflectively, there shouldn't need to be an existing Value
    // on which to call a method that creates a new one.
    public static Value fromConfigurationSection(ConfigurationSection configurationSection) {
        // Retrieve the value from the base-value path.
        double val = configurationSection.getDouble("base-value");
        // Return a new instanceo of Value containing the configured value.
        return new Value(val);
    }
```

There's some deprecated shit from the old config system that offered virtually nothing over Spigot's methods.
Please don't use it; it'll be removed before 1.0.0.

### Formatted Logging Shortcuts
This is minor, but like most engineers, I am lazy and decided I needed a shortcut for writing
```java
plugin.getLogger().log(Level.INFO, String.format("message %s %s %s", object1, object2, object3));
```
If you're so inclined, the [EmberCore](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/EmberCore.java)
class contains shorthand `info`, `warn`, `severe` and generic `log` methods that allow you to simply pass in your plugin,
message, and optionally arguments for `String#format`.
```java
EmberCore.warn(yourPluginInstance, "There are %s eggs.", numEggs);
```

### Custom Projectiles
[VectorProjectile](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/physics/VectorProjectile.java)
allows you to create custom projectiles that operate entirely outside of Minecraft's rigid projectile physics and
collision detection.

The behavior of a VectorProjectile is completely customizable through code. Extend the VectorProjectile class and
override whichever methods you see fit. The source code contains information on all relevant methods.

The limit's pretty much your imagination. It's entirely possible to use VectorProjectile to create
a homing egg (tip: don't use the egg projectile!) that passes through solid objects and causes sheep within 5 meters to explode,
but that can be deflected by a player holding a stick named Terry.

All VectorProjectiles are tracked in [ProjectileRegistry](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/physics/ProjectileRegistry.java).

As a final note, I use this class myself in the abilities I write for EmberRPG.

### Geometry
**WARNING: INCOMPLETE**

EmberCore contains functionality relevant to three-dimensional geometry and transformations.

To maintain consistency with Minecraft's conventions, an object's local X axis passes through its center from left
to right, its local Z axis passes through its center from back to front, and its local Y axis passes through its center
from bottom to top.

[Vectors](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/Vectors.java)
contains constants referring to standard vectors (up, right, forward, etc.). 

[VectorUtil](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/VectorUtil.java)
contains helper methods for vector-based calculations, such as determing whether an entity can "see" another within its
vision range and radius.

[EulerAngles](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/EulerAngles.java)
stores a three-dimensional rotation in degrees and functions similarly to Spigot's Vector class.
* [fromDirectionVector](https://github.com/Dewynion/EmberCore/blob/4e7eb10426d043e8faa82b77fd1c8545549a94db/src/main/java/com/github/Dewynion/embercore/geometry/EulerAngles.java#L79)
can be used to calculate EulerAngles from a Vector, treating it as a direction.
  
[GeometryUtil](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/GeometryUtil.java),
at present, contains a variety of methods pertaining to the rotation of a [Location](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Location.html)
about another point.

[ShapeUtil](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/geometry/ShapeUtil.java)
contains methods to generate arrays of Locations in various shapes.

RotationMatrix and Transform are currently defunct and should not be used.

### Command Tree Baseline
[CommandNode](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/command/CommandNode.java)
provides a bare-bones framework for creating command trees.

### Other Utility
[MovingParticle](https://github.com/Dewynion/EmberCore/blob/master/src/main/java/com/github/Dewynion/embercore/util/MovingParticle.java)
provides utility for generating clusters of particles with an applied velocity or color. See [this link](https://www.spigotmc.org/threads/comprehensive-particle-spawning-guide-1-13-1-17.343001/)
for more information on which particle types support this additional data.

