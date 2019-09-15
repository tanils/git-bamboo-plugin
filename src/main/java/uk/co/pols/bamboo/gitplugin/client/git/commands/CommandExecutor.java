package uk.co.pols.bamboo.gitplugin.client.git.commands;

import java.io.File;
import java.io.IOException;

public interface CommandExecutor {
    String execute(String[] commandLine, File sourceCodeDirectory) throws IOException;
}