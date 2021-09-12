package net.craftium.brander;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;

import java.util.List;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public class Command implements SimpleCommand
{
    private final Brander plugin;

    public Command(Brander plugin)
    {
        this.plugin = plugin;
    }

    @Override
    public void execute(Invocation invocation)
    {
        CommandSource source = invocation.source();
        String[] arguments = invocation.arguments();

        if(arguments.length < 1)
        {
            source.sendMessage(text("Invalid subcommand").color(RED));
            return;
        }

        if("reload".equals(arguments[0]))
            reload(source);
        else
            source.sendMessage(text("Invalid subcommand").color(RED));
    }

    private void reload(CommandSource source)
    {
        if(plugin.reload())
            source.sendMessage(text("Reload successful").color(GREEN));
        else
            source.sendMessage(text("Failed to reload, check console for details").color(RED));
    }

    @Override
    public List<String> suggest(Invocation invocation)
    {
        return List.of("reload");
    }

    @Override
    public boolean hasPermission(Invocation invocation)
    {
        return invocation.source().hasPermission("brander");
    }
}
