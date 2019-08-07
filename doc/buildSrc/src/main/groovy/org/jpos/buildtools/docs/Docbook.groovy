package org.jpos.buildtools.docs;

import com.icl.saxon.TransformerFactoryImpl
import org.apache.xml.resolver.CatalogManager
import org.apache.xml.resolver.tools.CatalogResolver
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.logging.LogLevel
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.xml.sax.InputSource
import org.xml.sax.XMLReader

import javax.xml.parsers.SAXParserFactory
import javax.xml.transform.Result
import javax.xml.transform.Source
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.URIResolver
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamResult
import javax.xml.transform.stream.StreamSource
import javax.xml.transform.TransformerException

public class Docbook extends DefaultTask
{
    @Input
    String extension = 'html';

    @Input
    boolean XIncludeAware = true;

    @InputDirectory
    File sourceDirectory = new File(project.getProjectDir(), "build/reference-work");

    @Input
    String sourceFileName;

    @InputFile
    File stylesheet;

    @Input
    Map<String, Object> parameters = new HashMap<String,Object>();

    @OutputDirectory
    File docsDir = new File(project.getBuildDir(), "reference");

    @TaskAction
    public final void transform()
    {
        switch (project.gradle.startParameter.logLevel)
        {
            case LogLevel.DEBUG:
            case LogLevel.INFO:
                break;
            default:
                logging.captureStandardOutput(LogLevel.INFO)
                logging.captureStandardError(LogLevel.INFO)
        }

        SAXParserFactory factory = new org.apache.xerces.jaxp.SAXParserFactoryImpl();
        factory.setXIncludeAware(XIncludeAware);
        docsDir.mkdirs();

        File srcFile = new File(sourceDirectory, sourceFileName);
        String outputFilename = srcFile.getName().substring(0, srcFile.getName().length() - 4) + '.' + extension;

        File oDir = getDocsDir()
        File outputFile = new File(oDir, outputFilename);

        Result result = new StreamResult(outputFile.getAbsolutePath());
        CatalogManager catalogManager = createCatalogManager()
        CatalogResolver resolver = new CatalogResolver(catalogManager);
        URIResolver uriResolver = createStyleSheetResolver(resolver);
        InputSource inputSource = new InputSource(srcFile.getAbsolutePath());

        XMLReader reader = factory.newSAXParser().getXMLReader();
        reader.setEntityResolver(resolver);
        TransformerFactory transformerFactory = new TransformerFactoryImpl();
        transformerFactory.setURIResolver(uriResolver);

        URL url = stylesheet.toURL();
        Source source = new StreamSource(url.openStream(), url.toExternalForm());
        Transformer transformer = transformerFactory.newTransformer(source);
        transformer.setURIResolver(uriResolver)

        parameters?.each { k, v -> transformer.setParameter(k, v) };

        preTransform(transformer, srcFile, outputFile);
        transformer.transform(new SAXSource(reader, inputSource), result);
        postTransform(outputFile);
    }

    private URIResolver createStyleSheetResolver(CatalogResolver catalogResolver)
    {
        URIResolver uriResolver;
        try
        {
            URL url = this.getClass().getClassLoader().getResource("docbook/");
            uriResolver = new StylesheetResolver("http://docbook.sourceforge.net/release/xsl/current",
                                                 new StreamSource(url.openStream(), url.toExternalForm()), catalogResolver);
        }
        catch (IOException ioe)
        {
            throw new GradleException("Failed to read stylesheet.", ioe);
        }
        return uriResolver;
    }

    protected void preTransform(Transformer transformer, File sourceFile, File outputFile)
    {
    }

    protected void postTransform(File outputFile)
    {
    }

    private CatalogManager createCatalogManager()
    {
        CatalogManager manager = new CatalogManager();
        manager.setIgnoreMissingProperties(true);
        ClassLoader classLoader = this.getClass().getClassLoader();
        StringBuilder builder = new StringBuilder();
        String docbookCatalogName = "docbook/catalog.xml";
        URL docbookCatalog = classLoader.getResource(docbookCatalogName);

        if (docbookCatalog == null)
        {
            throw new IllegalStateException("Docbook catalog " + docbookCatalogName + " could not be found in " + classLoader);
        }

        builder.append(docbookCatalog.toExternalForm());

        docbookCatalogName = "catalog.xml";
        docbookCatalog = classLoader.getResource(docbookCatalogName);

        if (docbookCatalog == null)
        {
            throw new IllegalStateException("Docbook catalog " + docbookCatalogName + " could not be found in " + classLoader);
        }

        builder.append(';');
        builder.append(docbookCatalog.toExternalForm());

        String catalogFiles = builder.toString();
        manager.setCatalogFiles(catalogFiles);
        return manager;
    }

    public class StylesheetResolver implements URIResolver
    {
        private String urn;
        private Source stylesheet;
        private URIResolver wrapped;

        public StylesheetResolver(String urn, Source stylesheet, URIResolver wrapped)
        {
            this.urn = urn;
            this.stylesheet = stylesheet;
            this.wrapped = wrapped;
        }

        public Source resolve(String href, String base) throws TransformerException
        {
            if ((href != null) && href.startsWith(urn))
            {
                int dirIndex = stylesheet.getSystemId().lastIndexOf("/");
                String dirPath = stylesheet.getSystemId().substring(0, dirIndex);
                String newLocation = dirPath.concat(href.replace(urn, ""));
                return new StreamSource(newLocation);
            }
            else
            {
                return wrapped.resolve(href, base);
            }
        }
    }
}
