package de.melanx.skyblockbuilder.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.melanx.skyblockbuilder.util.Team;
import de.melanx.skyblockbuilder.world.data.SkyblockSavedData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.server.ServerWorld;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ListCommand {

    public static ArgumentBuilder<CommandSource, ?> register() {
        return Commands.literal("list")
                .executes(context -> listTeams(context.getSource()))
                .then(Commands.argument("team", StringArgumentType.word()).suggests(TeamCommand.SUGGEST_TEAMS)
                        .executes(context -> listPlayers(context.getSource(), StringArgumentType.getString(context, "team"))));
    }

    private static int listTeams(CommandSource source) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);

        List<Team> teams = data.getTeams().stream().sorted(Comparator.comparing(Team::getName)).collect(Collectors.toList());
        IFormattableTextComponent info = new StringTextComponent(String.format("There's a total of %s teams where %s are empty.",
                teams.size() - 1,
                teams.stream().filter(Team::isEmpty).count()));
        info.mergeStyle(TextFormatting.GOLD);
        source.sendFeedback(info, true);

        for (Team team : teams) {
            if (!team.getName().equalsIgnoreCase("spawn")) {
                IFormattableTextComponent list = (new StringTextComponent("- " + team.getName()));
                if (team.isEmpty()) {
                    list.append(new StringTextComponent(" (Empty)"));
                    list.mergeStyle(TextFormatting.RED);
                } else {
                    list.mergeStyle(TextFormatting.GREEN);
                }

                source.sendFeedback(list, false);
            }
        }

        return 1;
    }

    private static int listPlayers(CommandSource source, String teamName) {
        ServerWorld world = source.getWorld();
        SkyblockSavedData data = SkyblockSavedData.get(world);
        Team team = data.getTeam(teamName);

        if (team == null) {
            source.sendFeedback(new StringTextComponent("Team does not exist!").mergeStyle(TextFormatting.RED), false);
            return 0;
        }

        PlayerProfileCache profileCache = source.getServer().getPlayerProfileCache();
        source.sendFeedback(new StringTextComponent(String.format("%s contains %s players.", team.getName(), team.getPlayers().size())).mergeStyle(TextFormatting.GOLD), false);
        team.getPlayers().forEach(id -> {
            GameProfile profile = profileCache.getProfileByUUID(id);
            if (profile != null) {
                String name = profile.getName();
                if (!StringUtils.isNullOrEmpty(name)) {
                    source.sendFeedback(new StringTextComponent("- " + name), false);
                }
            }
        });
        return 1;
    }
}
