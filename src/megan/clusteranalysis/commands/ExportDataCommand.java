/*
 * ExportDataCommand.java Copyright (C) 2021. Daniel H. Huson
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
package megan.clusteranalysis.commands;

import jloda.swing.commands.ICommand;
import jloda.swing.util.ChooseFileDialog;
import jloda.swing.util.ResourceManager;
import jloda.swing.util.TextFileFilter;
import jloda.util.Basic;
import jloda.util.NexusFileFilter;
import jloda.util.ProgramProperties;
import jloda.util.parse.NexusStreamParser;
import megan.clusteranalysis.ClusterViewer;
import megan.core.Director;
import megan.main.MeganProperties;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * export data command
 * Daniel Huson, 6.2010
 */
public class ExportDataCommand extends CommandBase implements ICommand {
    /**
     * get the name to be used as a menu label
     *
     * @return name
     */
    public String getName() {
        return "Export Distances...";
    }

    /**
     * get description to be used as a tooltip
     *
     * @return description
     */
    public String getDescription() {
        return "Export the distances to a Nexus file for SplitsTree4";
    }

    /**
     * get icon to be used in menu or button
     *
     * @return icon
     */
    public ImageIcon getIcon() {
        return ResourceManager.getIcon("sun/Export16.gif");
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
     * parses the given command and executes it
     *
     * @param np
     * @throws java.io.IOException
     */
    @Override
    public void apply(NexusStreamParser np) throws IOException {
        ClusterViewer viewer = getViewer();

        np.matchIgnoreCase("export data=distances file=");

        String fileName = np.getAbsoluteFileName();
        boolean replace = false;
        if (np.peekMatchIgnoreCase("replace")) {
            np.matchIgnoreCase("replace=");
            replace = np.getBoolean();
        }
        np.matchIgnoreCase(";");

        File file = new File(fileName);
        if (!replace && file.exists())
            throw new IOException("File exists: " + fileName + ", use REPLACE=true to overwrite");

        try (BufferedWriter w = new BufferedWriter(new FileWriter(file))) {
            w.write("#NEXUS [generated by MEGAN]\n");
            w.write("[! Computed using " + viewer.getEcologicalIndex() + " applied to " + viewer.getDataType() + "data]\n");
            TableModel model = viewer.getMatrixTab().getTable().getModel();
            w.write("begin taxa;\ndimensions ntax=" + model.getRowCount() + ";\n taxlabels\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                w.write("'" + Basic.toCleanName(model.getValueAt(r, 0).toString()) + "'\n");
            }
            w.write(";\n");
            w.write("end;\n");
            w.write("begin distances;\ndimensions ntax=" + model.getRowCount() + ";\n");
            w.write("format triangle=both diagonal labels;\nmatrix\n");
            for (int r = 0; r < model.getRowCount(); r++) {
                w.write("'" + Basic.toCleanName(model.getValueAt(r, 0).toString()) + "'");
                for (int c = 1; c < model.getColumnCount(); c++) {
                    w.write(" " + model.getValueAt(r, c));
                }
                w.write("\n");
            }
            w.write(";\n");
            w.write("end;\n");

        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * action to be performed
     *
     * @param ev
     */
    public void actionPerformed(ActionEvent ev) {
        ClusterViewer viewer = getViewer();

        String name = Basic.replaceFileSuffix(((Director) getDir()).getDocument().getMeganFile().getName(), ".nex");
        File lastOpenFile = new File(name);
        String lastDir = ProgramProperties.get(MeganProperties.NETWORK_DIRECTORY, "");
        if (lastDir.length() > 0) {
            lastOpenFile = new File(lastDir, lastOpenFile.getName());
        }

        getDir().notifyLockInput();
        File file = ChooseFileDialog.chooseFileToSave(viewer.getFrame(), lastOpenFile, new TextFileFilter(), new NexusFileFilter(), ev, "Save as Nexus file", ".nexus");
        getDir().notifyUnlockInput();

        if (file != null) {
            execute("export data=distances file='" + file.getPath() + "' replace=true;");
            ProgramProperties.put(MeganProperties.NETWORK_DIRECTORY, file.getParent());
        }
    }

    /**
     * is this a critical command that can only be executed when no other command is running?
     *
     * @return true, if critical
     */
    public boolean isCritical() {
        return true;
    }

    /**
     * is the command currently applicable? Used to set enable state of command
     *
     * @return true, if command can be applied
     */
    public boolean isApplicable() {
        ClusterViewer viewer = getViewer();
        if (viewer.getMatrixTab() != null) {
            TableModel model = viewer.getMatrixTab().getTable().getModel();
            return model.getRowCount() > 1;
        }
        return false;
    }

    /**
     * get command-line usage description
     *
     * @return usage
     */
    @Override
    public String getSyntax() {
        return "export data=distances file=<filename> [replace=bool];";
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
