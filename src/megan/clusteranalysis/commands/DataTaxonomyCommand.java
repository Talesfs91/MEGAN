/*
 * DataTaxonomyCommand.java Copyright (C) 2022 Daniel H. Huson
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
package megan.clusteranalysis.commands;

import jloda.swing.commands.ICheckBoxCommand;
import megan.clusteranalysis.ClusterViewer;
import megan.core.ClassificationType;
import megan.viewer.MainViewer;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * data=taxonomy command
 * Daniel Huson, 6.2010
 */
public class DataTaxonomyCommand extends DataCommand implements ICheckBoxCommand {
    /**
     * this is currently selected?
     *
     * @return selected
     */
    public boolean isSelected() {
        ClusterViewer viewer = getViewer();
        return viewer.getDataType().equalsIgnoreCase(ClassificationType.Taxonomy.toString());
    }

    /**
     * set the selected status of this command
     *
     * @param selected
     */
    public void setSelected(boolean selected) {
        ClusterViewer viewer = getViewer();
        viewer.setDataType(ClassificationType.Taxonomy.toString());
    }

    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    public String getName() {
        return "Use Taxonomy";
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    public String getDescription() {
        return "Use taxonomy as basis of comparison";
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    public ImageIcon getIcon() {
        return null;
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
     * action to be performed
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {
        execute("set networkdata=" + ClassificationType.Taxonomy + ";");
    }

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    public boolean isApplicable() {
        MainViewer mainViewer = (MainViewer) getDir().getViewerByClass(MainViewer.class);
        return mainViewer != null && mainViewer.hasComparableData();
    }

    /**
     * gets the command needed to undo this command
     *
     * @return undo command
     */
    public String getUndo() {
        return null;
    }
}

