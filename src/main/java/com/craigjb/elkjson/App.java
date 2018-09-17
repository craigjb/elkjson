package com.craigjb.elkjson;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import org.apache.commons.cli.*;
import org.eclipse.elk.graph.json.ElkGraphJson;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.core.AbstractLayoutProvider;
import org.eclipse.elk.core.util.BasicProgressMonitor;

public class App 
{
    public static void main( String[] args )
    {
        Options options = new Options();
        
        Option input = new Option("i", "input", true, "input JSON graph file path");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output JSON graph file path");
        output.setRequired(true);
        options.addOption(output);

        Option layoutPackageOption = new Option("p", "layout-package", true, "layout package");
        layoutPackageOption.setRequired(true);
        options.addOption(layoutPackageOption);

        Option layoutProviderOption = new Option("l", "layout-provider", true, "layout provider class name");
        layoutProviderOption.setRequired(true);
        options.addOption(layoutProviderOption);
        
        Option prettyOption = new Option("j", "pretty-json", false, "output pretty JSON");
        options.addOption(prettyOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("elkjson", options);
            System.exit(1);
        }

        String inputFilePath = cmd.getOptionValue("input");
        String outputFilePath = cmd.getOptionValue("output");
        String layoutPackageName = cmd.getOptionValue("layout-package");
        String layoutProviderName = cmd.getOptionValue("layout-provider");
        boolean prettyJson = cmd.hasOption("pretty-json");

        // dynamically load up the ELK layout algorithm based on options
        Class layoutProviderClass = null;
        try {
            ClassLoader classLoader = App.class.getClassLoader();
            String className = ("org.eclipse.elk.alg." + layoutPackageName + "." + layoutProviderName);
            layoutProviderClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("error: could not find layout provider " + layoutProviderName + 
                    " in package org.eclipse.elk.alg." + layoutPackageName);
            System.exit(1);
        }

        AbstractLayoutProvider layoutProvider = null;
        try {
            layoutProvider = (AbstractLayoutProvider)layoutProviderClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // load input JSON input a String and parse into an ELK graph
        String inputJsonString = null;
        try {
            byte[] inputBytes = Files.readAllBytes(new File(inputFilePath).toPath());
            inputJsonString = new String(inputBytes, "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("error: input JSON graph file not found");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error: could not read input JSON graph file");
            System.exit(1);
        }
        ElkNode root = ElkGraphJson.forGraph(inputJsonString).toElk();

        // perform layout
        layoutProvider.initialize(null);
        layoutProvider.layout(root, new BasicProgressMonitor());
        layoutProvider.dispose();

        // write output
        String outputJson = ElkGraphJson.forGraph(root)
                                        .omitZeroPositions(false)
                                        .omitZeroDimension(false)
                                        .shortLayoutOptionKeys(false)
                                        .prettyPrint(prettyJson)
                                        .toJson();
        try {
            Files.write(new File(outputFilePath).toPath(), outputJson.getBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("error: output JSON graph file not found");
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("error: could not write output JSON graph file");
            System.exit(1);
        }
    }
}
