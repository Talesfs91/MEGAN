/*
 * ScaleByNoneCommand.java Copyright (C) 2020. Daniel H. Huson
 *
 *  (Some files contain contributions from other authors, who are then mentioned separately.)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package megan.commands;

import jloda.swing.commands.ICheckBoxCommand;
import jloda.swing.util.ResourceManager;
import jloda.util.parse.NexusStreamParser;
import megan.viewer.ViewerBase;
import megan.viewer.gui.NodeDrawer;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ScaleByNoneCommand extends CommandBase implements ICheckBoxCommand {
    public boolean isSelected() {
        ViewerBase viewer = (ViewerBase) getViewer();
        return viewer != null && viewer.getNodeDrawer().getScaleBy() == NodeDrawer.ScaleBy.None;
    }

    public void apply(NexusStreamParser np) throws Exception {
    }

    public boolean isApplicable() {
        return true;
    }

    public boolean isCritical() {
        return true;
    }

    public String getSyntax() {
        return null;
    }

    public void actionPerformed(ActionEvent event) {
        execute("set scaleBy=" + NodeDrawer.ScaleBy.None + ";");
    }

    public String getName() {
        return "Scale Nodes By None";
    }

    public String getDescription() {
        return "Don't scale nodes";
    }

    public ImageIcon getIcon() {
        return ResourceManager.getIcon("Empty16.gif");
    }
}

