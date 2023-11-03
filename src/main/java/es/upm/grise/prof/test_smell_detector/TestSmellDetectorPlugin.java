package es.upm.grise.prof.test_smell_detector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import es.upm.grise.prof.test_smell_detector.testsmell.AbstractSmell;
import es.upm.grise.prof.test_smell_detector.testsmell.TestFile;
import es.upm.grise.prof.test_smell_detector.testsmell.TestSmellDetector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "detect", defaultPhase = LifecyclePhase.TEST_COMPILE)
public class TestSmellDetectorPlugin extends AbstractMojo {
	@Parameter(defaultValue = "${project}", readonly = true, required = true)
	private MavenProject project;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		File testDirectory = new File(project.getBuild().getTestSourceDirectory());
        File productionDirectory = new File(project.getBuild().getSourceDirectory());

        List<String> testClassNames = getClassesFromDir(testDirectory);
        List<String> productionClassNames = getClassesFromDir(productionDirectory);

        List<TestFile> testFiles = new ArrayList<>();
        
        for(int i=0; i<testClassNames.size(); i++) {
        	testFiles.add(new TestFile(
        			project.getArtifactId(),
        			testClassNames.get(i),
        			productionClassNames.get(i)));
        	
        	getLog().info("Project name: " + testFiles.get(i).getApp());
        	getLog().info("Production file: " + testFiles.get(i).getProductionFileName());
        	getLog().info("Test file: " + testFiles.get(i).getTestFileName());
        }
        
        TestSmellDetector testSmellDetector = new TestSmellDetector();
        BufferedWriter writer;
		try {
			writer = new BufferedWriter(new FileWriter("test-smells-in-"+ project.getName() + ".html"));
		
        TestFile tempFile;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        for (TestFile file : testFiles) {
   
            date = new Date();
            System.out.println(dateFormat.format(date) + " Processing: "+file.getTestFilePath());
            System.out.println("Processing: "+file.getTestFilePath());

            //detect smells
            tempFile = testSmellDetector.detectSmells(file);
            

            
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<body>\n");
            writer.write("<h1>");
            
            writer.write("Completado el análisis de test smells sobre el fichero " + file.getTestFileNameWithoutExtension() + "\n");
            writer.write("</h1>\n");
            
            for (AbstractSmell smell : tempFile.getTestSmells()) {
                	String value = String.valueOf(smell.getHasSmell());          
                	writer.write("<p><b>");
                	writer.write(smell.getSmellName() +"->"  + "</b>");
                	writer.write(value);
            
            }
            
            writer.write("</body>\n");
            writer.write("</html>");
            writer.close();
        	}
        }
         catch (IOException e) {
         }
        
        System.out.println("end");
    }
       
    

	private List<String> getClassesFromDir(File directory) {
		List<String> classNames = new ArrayList<>();
		if (directory.exists() && directory.isDirectory()) {
			getLog().info("Getting classes from directory: " + directory.getAbsolutePath());

			try {
				classNames = Files.walk(directory.toPath()).filter(path -> path.toString().endsWith(".java"))
						.map(Path::toAbsolutePath).map(Path::toString).collect(Collectors.toList());
			} catch (IOException e) {
				getLog().info("Error when trying to get the files from the directory", e);
			}
		} else {
			getLog().info("Directory does not exist.");
		}
		return classNames;
	}

}
