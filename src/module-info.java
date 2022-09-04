module megan {
    requires transitive jloda;
    requires transitive javafx.swing;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive com.install4j.runtime;
    requires transitive java.sql;
    requires transitive jdk.httpserver;

    requires Jama;
    requires sis.jhdf5.batteries.included;
    requires org.xerial.sqlitejdbc;
    requires gson;
    requires commons.math3;
    requires contrasts;
    requires java.desktop;
    requires java.net.http;
    requires bcyrpt;

    exports megan.accessiondb;
    exports megan.algorithms;
    exports megan.alignment;
    exports megan.alignment.commands;
    exports megan.alignment.gui;
    exports megan.alignment.gui.colors;
    exports megan.fx.dialogs.decontam;
    exports megan.assembly;
    exports megan.assembly.alignment;
    exports megan.assembly.commands;
    exports megan.biom.biom1;
    exports megan.biom.biom2;
    exports megan.blastclient;
    exports megan.chart;
    exports megan.chart.cluster;
    exports megan.chart.commands;
    exports megan.chart.commandtemplates;
    exports megan.chart.data;
    exports megan.chart.drawers;
    exports megan.chart.gui;
    exports megan.classification;
    exports megan.classification.commandtemplates;
    exports megan.classification.data;
    exports megan.classification.util;
    exports megan.clusteranalysis;
    exports megan.clusteranalysis.commands;
    exports megan.clusteranalysis.commands.geom3d;
    exports megan.clusteranalysis.commands.zoom;
    exports megan.clusteranalysis.gui;
    exports megan.clusteranalysis.indices;
    exports megan.clusteranalysis.nnet;
    exports megan.clusteranalysis.pcoa;
    exports megan.clusteranalysis.pcoa.geom3d;
    exports megan.clusteranalysis.tree;
    exports megan.commands;
    exports megan.commands.additional;
    exports megan.commands.algorithms;
    exports megan.commands.clipboard;
    exports megan.commands.color;
    exports megan.commands.compare;
    exports megan.commands.export;
    exports megan.commands.find;
    exports megan.commands.format;
    exports megan.commands.load;
    exports megan.commands.mapping;
    exports megan.commands.preferences;
    exports megan.commands.select;
    exports megan.commands.show;
    exports megan.commands.zoom;
    exports megan.core;
    exports megan.daa;
    exports megan.daa.connector;
    exports megan.daa.io;
    exports megan.data;
    exports megan.data.merge;

    exports megan.dialogs.attributes;
    exports megan.dialogs.attributes.commands;
    exports megan.dialogs.compare;
    exports megan.dialogs.compare.commands;
    exports megan.dialogs.export;
    exports megan.dialogs.export.analysis;
    exports megan.dialogs.extractor;
    exports megan.dialogs.extractor.commands;
    exports megan.dialogs.importcsv;
    exports megan.dialogs.input;
    exports megan.dialogs.lrinspector;
    exports megan.dialogs.lrinspector.commands;
    exports megan.dialogs.meganize;
    exports megan.dialogs.meganize.commands;
    exports megan.dialogs.reanalyze.commands;
    exports megan.dialogs.parameters;
    exports megan.dialogs.parameters.commands;
    exports megan.dialogs.profile;
    exports megan.dialogs.profile.commands;
    exports megan.dialogs.reads;
    exports megan.fx;
    exports megan.genes;
    exports megan.groups;
    exports megan.groups.commands;
    exports megan.importblast;
    exports megan.importblast.commands;
    exports megan.inspector;
    exports megan.inspector.commands;
    exports megan.io;
    exports megan.io.experimental;
    exports megan.main;
    exports megan.parsers;
    exports megan.parsers.blast;
    exports megan.parsers.blast.blastxml;
    exports megan.parsers.maf;
    exports megan.parsers.sam;
    exports megan.rma2;
    exports megan.rma3;
    exports megan.rma6;
    exports megan.samplesviewer;
    exports megan.samplesviewer.commands;
    exports megan.samplesviewer.commands.attributes;
    exports megan.samplesviewer.commands.format;
    exports megan.samplesviewer.commands.samples;
    exports megan.stats;
    exports megan.timeseriesviewer;
    exports megan.timeseriesviewer.commands;
    exports megan.tools;
    exports megan.treeviewer;
    exports megan.util;
    exports megan.viewer;
    exports megan.viewer.commands;
    exports megan.viewer.commands.collapse;
    exports megan.viewer.gui;
    exports megan.xtra;

    opens megan.resources;
    opens megan.resources.css;
    opens megan.resources.icons;
    opens megan.resources.images;
    opens megan.resources.files;
    opens megan.resources.files.ms;

    opens megan.biom.biom1;
    opens megan.biom.biom2;
    opens megan.fx.dialogs.decontam;

    opens megan.dialogs.lrinspector;
    opens megan.dialogs.reads;
    exports megan.resources;

    exports megan.ms;
    exports megan.ms.client;
    exports megan.ms.client.connector;
    exports megan.ms.clientdialog;
    exports megan.ms.clientdialog.commands;
    opens megan.ms.clientdialog.commands;
    exports megan.ms.server;
    exports megan.ms.clientdialog.service;
	exports megan.tools.utils;
}