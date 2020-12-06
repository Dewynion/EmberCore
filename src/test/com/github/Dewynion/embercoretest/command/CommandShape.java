package com.github.Dewynion.embercoretest.command;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.util.GeometryUtil;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * This class/command is entirely for {@link GeometryUtil} testing/debug. As a result, I don't really care much
 * about making it a marvel of modern engineering.
 * End-users shouldn't need it.
 * Developers may want to use it to ensure the library is working.
 * Requires "debug=true" in config.yml.
 */
public class CommandShape implements CommandExecutor {
    static final int DISPLAY_TICKS = 100;
    static final int MAX_DISTANCE = 10;
    static final double DEFAULT_WIDTH = 5.0;
    static final int DEFAULT_POINTS = 10;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return false;
        }
        Player player = (Player) sender;
        if (args.length < 3) {
            sender.sendMessage("Shape command requires minimum 3 arguments (shape, type, and billboard).");
            return true;
        }
        String shapeName = args[0].toLowerCase();
        GeometryUtil.ShapeType type;
        try {
            type = GeometryUtil.ShapeType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException ex) {
            sender.sendMessage("Invalid shape type. Valid shape types are solid, wireframe, hollow, and corners.");
            return true;
        }
        boolean billboard = Boolean.valueOf(args[2]);

        double width = DEFAULT_WIDTH,
                length = DEFAULT_WIDTH,
                height = DEFAULT_WIDTH;
        int pointsWidth = DEFAULT_POINTS,
                pointsLength = DEFAULT_POINTS,
                pointsHeight = DEFAULT_POINTS;
        double radius = DEFAULT_WIDTH;
        int points = DEFAULT_POINTS;

        if (args.length > 3) {
            switch (shapeName) {
                case "quad":
                    try {
                        width = Double.valueOf(args[3]);
                        length = Double.valueOf(args[4]);
                        pointsWidth = Integer.valueOf(args[5]);
                        pointsLength = Integer.valueOf(args[6]);
                    } catch (Exception e) {
                        sender.sendMessage("Quad requires 4 arguments (w/l/points w/l). " +
                                "\nAll size arguments must be numbers. " +
                                "\nPoint counts must be integers");
                        return true;
                    }
                    break;
                case "cube":
                    try {
                        width = Double.valueOf(args[3]);
                        length = Double.valueOf(args[4]);
                        height = Double.valueOf(args[5]);
                        pointsWidth = Integer.valueOf(args[6]);
                        pointsLength = Integer.valueOf(args[7]);
                        pointsHeight = Integer.valueOf(args[8]);
                    } catch (Exception e) {
                        sender.sendMessage("Cube requires 6 arguments (w/l/h/points w/l/h). " +
                                "\nAll size arguments must be numbers. " +
                                "\nPoint counts must be integers");
                        return true;
                    }
                    break;
                case "circle":
                case "sphere":
                    try {
                        radius = Double.valueOf(args[3]);
                        points = Integer.valueOf(args[4]);
                    } catch (Exception e) {
                        sender.sendMessage("Circle/sphere requires 2 arguments (radius/points). " +
                                "\nAll size arguments must be numbers. " +
                                "\nPoint counts must be integers");
                        return true;
                    }
                    break;
                default:
                    sender.sendMessage("Invalid shape. Valid shapes are circle, quad, sphere, and cube.");
                    return true;
            }
        }
        final double w = width,
                l = length,
                h = height,
                r = radius;
        final int pW = pointsWidth,
                pL = pointsLength,
                pH = pointsHeight,
                p = points;
        new BukkitRunnable() {
            int ticks = 0;
            List<Location> shape = null;
            public void run() {
                if (billboard || shape == null) {
                    Location target = player.getTargetBlock(null, MAX_DISTANCE).getLocation();
                    switch (shapeName) {
                        case "quad":
                            shape = GeometryUtil.quad(target, w, l, pW, pL, type);
                            break;
                        case "cube":
                            shape = GeometryUtil.cube(target, w, l, h, pW, pL, pH, type);
                            break;
                        case "circle":
                            shape = GeometryUtil.circle(target, r, p, type);
                            break;
                        case "sphere":
                            shape = GeometryUtil.sphere(target, r, p, type);
                            break;
                        default:
                            player.sendMessage("Unrecognized shape '" + shapeName + "'.");
                            cancel();
                            return;
                    }
                    if (billboard) {
                        for (int i = 0; i < shape.size(); i++) {
                            Location loc = GeometryUtil.rotateAroundDegrees(shape.get(i), target, new Vector(player.getLocation().getPitch(),
                                    player.getLocation().getYaw(), 0f));
                            shape.set(i, loc);
                        }
                    }
                }
                shape.forEach(l -> l.getWorld().spawnParticle(Particle.CLOUD,
                        l, 1, 0, 0, 0, 0));
                if (ticks++ >= DISPLAY_TICKS)
                    cancel();
            }
        }.runTaskTimer(EmberCore.getInstance(), 0, 1);
        return true;
    }
}
