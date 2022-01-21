/*
 * ShowGroupsViewerCommand.java Copyright (C) 2022 Daniel H. Huson
 *
 * (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package megan.commands.show;

import jloda.swing.commands.CommandBase;
import jloda.swing.commands.ICommand;
import jloda.swing.util.ResourceManager;
import jloda.util.Basic;
import jloda.util.parse.NexusStreamParser;
import megan.core.Director;
import megan.groups.GroupsViewer;
import megan.util.WindowUtilities;
import megan.viewer.ClassificationViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ShowGroupsViewerCommand extends CommandBase implements ICommand {
    public String getSyntax() {
        return "show window=groups;";
    }

    public void actionPerformed(ActionEvent event) {
        execute(getSyntax());
    }

    public boolean isApplicable() {
        return ((Director) getDir()).getDocument().getNumberOfReads() > 0 && getViewer() instanceof ClassificationViewer;

    }

    public String getName() {
        return "Groups Viewer...";
    }

    public ImageIcon getIcon() {
        return ResourceManager.getIcon("JoinNodes16.gif");
    }

    public String getDescription() {
        return "Show groups viewer";
    }

    /**
     * parses the given command and executes it
     *
     * @param np
     * @throws java.io.IOException
     */
    @Override
    public void apply(NexusStreamParser np) throws Exception {
        np.matchIgnoreCase(getSyntax());

        GroupsViewer groupsViewer = (GroupsViewer) getDir().getViewerByClass(GroupsViewer.class);
        if (groupsViewer == null) {
            try {
                groupsViewer = new GroupsViewer((Director) getDir(), getViewer().getFrame());
                getDir().addViewer(groupsViewer);
                WindowUtilities.toFront(groupsViewer.getFrame());
            } catch (Exception e) {
                Basic.caught(e);
            }
        } else {
            groupsViewer.updateView(Director.ENABLE_STATE);
            WindowUtilities.toFront(groupsViewer.getFrame());
        }
    }

    /**
     * gets the accelerator key  to be used in menu
     *
     * @return accelerator key
     */
    public KeyStroke getAcceleratorKey() {
        return null;
    }

    /**
     * is this a critical command that can only be executed when no other command is running?
     *
     * @return true, if critical
     */
    public boolean isCritical() {
        return true;
    }
}


