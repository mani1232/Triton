package com.rexcantor64.triton.spigot.scheduler;

import com.rexcantor64.triton.api.scheduler.BaseScheduler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class SpigotScheduler implements BaseScheduler {

    BukkitTask bukkitTask;

    JavaPlugin plugin;

    public SpigotScheduler(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull BaseScheduler runTask(Location location, Runnable task) {
        bukkitTask = Bukkit.getScheduler().runTask(plugin, task);
        return this;
    }

    @Override
    public @NotNull BaseScheduler runTaskAsynchronously(Runnable task) {
        bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        return this;
    }

    @Override
    public @NotNull BaseScheduler runTaskLater(Location location, long delay, Runnable task) {
        bukkitTask = Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        return this;
    }

    @Override
    public @NotNull BaseScheduler runTaskLaterAsynchronously(long delay, Runnable task) {
        bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
        return this;
    }

    @Override
    public @NotNull BaseScheduler runTaskTimer(Location location, long delay, long period, Runnable task) {
        bukkitTask = Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        return this;
    }

    @Override
    public @NotNull BaseScheduler runTaskTimerAsynchronously(long delay, long period, Runnable task) {
        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
        return this;
    }

    @Override
    public void cancel() {
        if (!bukkitTask.isCancelled()) {
            bukkitTask.cancel();
        }
    }
}
