package ie.tcd.scss.csl;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.w3c.dom.Node;

import com.atlassian.clover.CloverDatabase;
import com.atlassian.clover.CoverageData;
import com.atlassian.clover.CoverageDataSpec;
import com.atlassian.clover.api.CloverException;
import com.atlassian.clover.api.registry.HasMetrics;
import com.atlassian.clover.context.ContextSet;
import com.atlassian.clover.registry.CoverageDataRange;
import com.atlassian.clover.registry.entities.BasicElementInfo;
import com.atlassian.clover.registry.entities.FullBranchInfo;
import com.atlassian.clover.registry.entities.FullElementInfo;
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
                // add the file element as a child element of the root element
                Map<Integer, Set<TestCaseInfo>> lineElementMap = new HashMap<Integer, Set<TestCaseInfo>>();
                // add the test cases that cover the file as line -1
                lineElementMap.put(-1, cd.getTestsCovering(fullFileInfo));
                // Get the lines
                LineInfo[] lines = fullFileInfo.getLineInfo(true, true);

                // loop through the lines
                for (LineInfo line : lines) {
                    if (line != null) {
                        // for each line, create a line element
                        Set<TestCaseInfo> lineElement = null;
                        if (lineElementMap.containsKey(line.getLine())) {
                            lineElement = lineElementMap.get(line.getLine());
                        } else {
                            // lineElement = doc.createElement("line");
                            // lineElement.setAttribute("number", String.valueOf(line.getLine()));
                            // lineElement.setAttribute("hasBranch", String.valueOf(line.hasBranches()));
                            lineElement = new HashSet<TestCaseInfo>();
                        }
                        // Get the statements
                        FullStatementInfo[] statements = line.getStatements();
                        // loop through the statements
                        for (FullStatementInfo statement : statements) {
                            // add the hit count as an attribute

                            // lineElement.setAttribute("hitCount",
                            // String.valueOf(statement.getHitCount()));
                            // lineElement.setAttribute("StartLine",
                            // String.valueOf(statement.getStartLine()));
                            // lineElement.setAttribute("EndLine", String.valueOf(statement.getEndLine()));
                            // lineElement.setAttribute("StartColumn",
                            // String.valueOf(statement.getStartColumn()));
                            // lineElement.setAttribute("EndColumn",
                            // String.valueOf(statement.getEndColumn()));

                            // add the test cases that cover the statement as child elements
                            Set<TestCaseInfo> caseInfos = cd.getTestsCovering(statement);

                            // loop through the test cases
                            for (TestCaseInfo testCaseInfo : caseInfos) {
                                // for each test case, create a test case element
                                Element testCaseElement = doc.createElement("testCase");
                                // add the test name and source method as attributes
                                // testCaseElement.setAttribute("name", testCaseInfo.getTestName());
                                testCaseElement.setAttribute("QualifiedName",
                                        testCaseInfo.getQualifiedName());
                                // add the test case element as a child element of the statement element
                                lineElement.add(testCaseInfo);
                                // lineElement.appendChild(testCaseElement); // !!!!!!!!!
                            }
                            // add the statement element as a child element of the line element
                            lineElementMap.put(Integer.valueOf(line.getLine()), lineElement);
                            if (statement.getEndLine() != line.getLine()) {
                                for (int i = line.getLine() + 1; i <= statement.getEndLine(); i++) {
                                    if (!lineElementMap.containsKey(i)) {
                                        // Element element = (Element) lineElement.cloneNode(true);
                                        // element.setAttribute("number", String.valueOf(i));

                                        lineElementMap.put(Integer.valueOf(i), lineElement);
                                    } else {
                                        Set<TestCaseInfo> temp = lineElementMap.get(i);

                                        for (TestCaseInfo testCaseInfo_ : lineElement) {
                                            temp.add(testCaseInfo_);
                                            // Node node = lineElement.item(q);
                                            // temp.appendChild(node.cloneNode(true));
                                        }
                                        lineElementMap.replace(Integer.valueOf(i), temp);
                                    }
                                }
                            }
                        }
                    }
                }
                // add the line elements as a children elements of the file element
                for (Integer key : lineElementMap.keySet()) {
                    Set<TestCaseInfo> lineElement = lineElementMap.get(key);
                    Element lineElement_ = doc.createElement("line");
                    lineElement_.setAttribute("number", String.valueOf(key));
                    for (TestCaseInfo testCaseInfo : lineElement) {
                        Element testCaseElement = doc.createElement("testCase");
                        testCaseElement.setAttribute("QualifiedName", testCaseInfo.getQualifiedName());
                        lineElement_.appendChild(testCaseElement);
                    }
                    fileElement.appendChild(lineElement_);
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
