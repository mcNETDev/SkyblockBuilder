package de.melanx.skyblockbuilder.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.melanx.skyblockbuilder.util.WorldUtil;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

public class LeaveCommand {
    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("leave")
                .executes(context -> leaveTeam(context.getSource()));
    }

    private static int leaveTeam(CommandSource source) throws CommandSyntaxException {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        ServerPlayerEntity player = source.asPlayer();

        if (!data.hasPlayerTeam(player)) {
            source.sendFeedback(new StringTextComponent("You currently in no team!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        player.inventory.dropAllItems();
        data.removePlayerFromTeam(player);
        source.sendFeedback(new StringTextComponent("Successfully left your teammates alone.").mergeStyle(TextFormatting.GOLD), false);
        WorldUtil.teleportToIsland(player, data.getSpawn());
        return 1;
    }
}
