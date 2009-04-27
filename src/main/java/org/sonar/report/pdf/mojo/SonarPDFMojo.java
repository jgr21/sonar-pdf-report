/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 GMV-SGI
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.report.pdf.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.sonar.report.pdf.ExecutivePDFReporter;
import org.sonar.report.pdf.PDFReporter;
import org.sonar.report.pdf.util.Logger;

import com.lowagie.text.DocumentException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Generate a PDF report. WARNING, Sonar server must be started.
 * 
 * @goal generate
 * @aggregator
 */
public class SonarPDFMojo extends AbstractMojo {
  /**
   * Project build directory
   * 
   * @parameter expression="${project.build.directory}"
   * @required
   */
  private File outputDirectory;
  
  /**
   * Maven project info.
   *
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  private MavenProject project;
  
  /**
   * Sonar Base URL.
   * @parameter expression="${sonar.host.url}"
   * @optional
   */
  private String sonarHostUrl;


  public void execute() throws MojoExecutionException {
    
    Logger.setLog(getLog());  
      
    Properties config = new Properties();
    Properties configLang = new Properties();
    
    try {
      if(sonarHostUrl != null) {
        config.put("sonar.base.url", sonarHostUrl);
        config.put("front.page.logo", "sonar-large.png");
      } else {
        config.load(this.getClass().getResourceAsStream("/report.properties"));
      }
      configLang.load(this.getClass().getResourceAsStream("/report-texts-en.properties"));

      PDFReporter reporter = new ExecutivePDFReporter(new URL(config.getProperty("sonar.base.url")
          + "/images/sonar.png"), project.getGroupId() + ":" + project.getArtifactId(), config.getProperty("sonar.base.url"), config, configLang);
      
      ByteArrayOutputStream baos = reporter.getReport();
      FileOutputStream fos = null;
      if ( !outputDirectory.exists() ) {
          outputDirectory.mkdirs();
      }
      File reportFile = new File( outputDirectory, project.getArtifactId() + ".pdf" );
      fos = new FileOutputStream(reportFile);
      baos.writeTo(fos);
      fos.flush();
      fos.close();
      Logger.info("PDF report generated (see "+ project.getArtifactId() + ".pdf on build output directory)");
    } catch (IOException e) {
      e.printStackTrace();
    } catch (DocumentException e) {
      Logger.error("Problem generating PDF file.");
      e.printStackTrace();
    } catch (org.dom4j.DocumentException e) {
      Logger.error("Problem parsing response data.");
      e.printStackTrace();
    }
  }
}
