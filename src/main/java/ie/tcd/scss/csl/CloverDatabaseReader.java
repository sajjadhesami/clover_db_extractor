package ie.tcd.scss.csl;

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.atlassian.clover.CloverDatabase;
import com.atlassian.clover.CoverageData;
import com.atlassian.clover.CoverageDataSpec;
import com.atlassian.clover.api.CloverException;
import com.atlassian.clover.api.registry.HasMetrics;
import com.atlassian.clover.registry.entities.FullFileInfo;
import com.atlassian.clover.registry.entities.FullStatementInfo;
import com.atlassian.clover.registry.entities.LineInfo;
import com.atlassian.clover.registry.entities.TestCaseInfo;
import com.atlassian.clover.registry.metrics.HasMetricsFilter;
import com.atlassian.clover.util.SimpleCoverageRange;

public class CloverDatabaseReader {
    // path to the Clover database
    private String dbPath;
    // path to the output xml file (default: ./output.xml)
    private String outputPath = "./output.xml";

    // Constructor with default output path
    public CloverDatabaseReader(String dbPath) {
        this.dbPath = dbPath;
    }

    // Constructor with custom output path
    public CloverDatabaseReader(String dbPath, String outputPath) {
        this.dbPath = dbPath;
        this.outputPath = outputPath;
    }

    // Generate the XML file
    public void generateXML() throws CloverException, ParserConfigurationException, TransformerException {

        // Create the XML document
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.newDocument();

        // root element
        Element rootElement = doc.createElement("Database");
        doc.appendChild(rootElement);

        // Load the Clover database
        CloverDatabase db = CloverDatabase.loadWithCoverage(this.dbPath, new CoverageDataSpec());
        CoverageData cd = db.getCoverageData();

        // Get the files that have coverage
        List<FullFileInfo> fullFileInfos = db.getAppOnlyModel().getFiles(new HasMetricsFilter() {
            @Override
            public boolean accept(HasMetrics arg0) {
                return arg0.getMetrics().getNumElements() > 0;
            }
        });
        // loop through the files
        for (FullFileInfo fullFileInfo : fullFileInfos) {
            if (fullFileInfo != null && fullFileInfo instanceof FullFileInfo) {
                // for each file, create a file element
                Element fileElement = doc.createElement("file");
                // add the file name and path as attributes
                fileElement.setAttribute("path", fullFileInfo.getPhysicalFile().getPath());

                // Get the lines
                LineInfo[] lines = fullFileInfo.getLineInfo(true, true);

                // loop through the lines
                for (LineInfo line : lines) {
                    if (line != null) {
                        // for each line, create a line element
                        Element lineElement = doc.createElement("line");
                        // add the line number as an attribute
                        lineElement.setAttribute("number", String.valueOf(line.getLine()));
                        // Get the statements
                        FullStatementInfo[] statements = line.getStatements();
                        // loop through the statements
                        for (FullStatementInfo statement : statements) {
                            // for each statement, create a statement element
                            Element statementElement = doc.createElement("statement");
                            // add the hit count as an attribute
                            statementElement.setAttribute("hitCount", String.valueOf(statement.getHitCount()));

                            // add the test cases that cover the statement as child elements
                            Set<TestCaseInfo> caseInfos = cd.getTestsCovering(
                                    new SimpleCoverageRange(statement.getDataIndex(), statement.getDataLength()));

                            // loop through the test cases
                            for (TestCaseInfo testCaseInfo : caseInfos) {
                                // for each test case, create a test case element
                                Element testCaseElement = doc.createElement("testCase");
                                // add the test name and source method as attributes
                                testCaseElement.setAttribute("name", testCaseInfo.getTestName());
                                testCaseElement.setAttribute("SourceMethodName",
                                        testCaseInfo.getSourceMethodName());
                                testCaseElement.setAttribute("QualifiedName",
                                        testCaseInfo.getQualifiedName());
                                testCaseElement.setAttribute("RuntimeTypeName",
                                        testCaseInfo.getRuntimeTypeName());
                                testCaseElement.setAttribute("TestName",
                                        testCaseInfo.getTestName());
                                // add the test case element as a child element of the statement element
                                statementElement.appendChild(testCaseElement);
                            }
                            // add the statement element as a child element of the line element
                            lineElement.appendChild(statementElement);
                        }
                        // add the line element as a child element of the file element
                        fileElement.appendChild(lineElement);
                    }
                }
                // add the file element as a child element of the root element
                rootElement.appendChild(fileElement);
            } else {
                System.out.println("File not found in Clover database");
            }
        }
        // write the content into xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // indent the XML
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4"); // indent by 4 spaces
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(this.outputPath));
        transformer.transform(source, result);
    }
}
